package xtrem.download.mobile.ui.main

import xtrem.download.mobile.core.filter.DownloadFilter
import xtrem.download.mobile.ui.main.DownloadListAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Parcelable
import androidx.recyclerview.selection.SelectionTracker
import xtrem.download.mobile.ui.main.DownloadItem
import xtrem.download.mobile.ui.main.DownloadsViewModel
import io.reactivex.disposables.CompositeDisposable
import xtrem.download.mobile.ui.BaseAlertDialog
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import xtrem.download.mobile.R
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import xtrem.download.mobile.ui.main.DownloadsFragment
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.selection.SelectionPredicates
import io.reactivex.disposables.Disposable
import android.widget.CheckBox
import androidx.lifecycle.ViewModelProvider
import xtrem.download.mobile.core.model.data.entity.InfoAndPieces
import io.reactivex.SingleSource
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import androidx.recyclerview.selection.MutableSelection
import xtrem.download.mobile.core.model.data.entity.DownloadInfo
import android.content.Intent
import android.widget.Toast
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import xtrem.download.mobile.core.utils.Utils
import xtrem.download.mobile.databinding.FragmentDownloadListBinding
import xtrem.download.mobile.ui.details.DownloadDetailsDialog
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

/*
 * A base fragment for individual fragment with sorted content (queued and completed downloads)
 */
abstract class DownloadsFragment(private val fragmentDownloadsFilter: DownloadFilter) : Fragment(),
    DownloadListAdapter.ClickListener {
    protected var activity: AppCompatActivity? = null
    protected var adapter: DownloadListAdapter? = null
    protected var layoutManager: LinearLayoutManager? = null

    /* Save state scrolling */
    private var downloadListState: Parcelable? = null
    private var selectionTracker: SelectionTracker<DownloadItem>? = null
    private var actionMode: ActionMode? = null
    protected var binding: FragmentDownloadListBinding? = null
    @JvmField
    protected var viewModel: DownloadsViewModel? = null
    protected var disposables = CompositeDisposable()
    private var deleteDownloadsDialog: BaseAlertDialog? = null
    private var dialogViewModel: BaseAlertDialog.SharedViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_download_list, container, false)
        adapter = DownloadListAdapter(this)
        /*
         * A RecyclerView by default creates another copy of the ViewHolder in order to
         * fade the views into each other. This causes the problem because the old ViewHolder gets
         * the payload but then the new one doesn't. So needs to explicitly tell it to reuse the old one.
         */
        val animator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        layoutManager = LinearLayoutManager(activity)
        binding!!.downloadList.layoutManager = layoutManager
        binding!!.downloadList.itemAnimator = animator
        binding!!.downloadList.setEmptyView(binding!!.emptyViewDownloadList)
        binding!!.downloadList.adapter = adapter
        selectionTracker = SelectionTracker.Builder(
            SELECTION_TRACKER_ID,
            binding!!.downloadList,
            DownloadListAdapter.KeyProvider(adapter),
            DownloadListAdapter.ItemLookup(binding!!.downloadList),
            StorageStrategy.createParcelableStorage(DownloadItem::class.java)
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
        selectionTracker!!.addObserver(object :
            SelectionTracker.SelectionObserver<DownloadItem?>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (selectionTracker!!.hasSelection() && actionMode == null) {
                    actionMode = activity!!.startSupportActionMode(actionModeCallback)
                    setActionModeTitle(selectionTracker!!.selection.size())
                } else if (!selectionTracker!!.hasSelection()) {
                    if (actionMode != null) actionMode!!.finish()
                    actionMode = null
                } else {
                    setActionModeTitle(selectionTracker!!.selection.size())
                }
            }

            override fun onSelectionRestored() {
                super.onSelectionRestored()
                actionMode = activity!!.startSupportActionMode(actionModeCallback)
                setActionModeTitle(selectionTracker!!.selection.size())
            }
        })
        if (savedInstanceState != null) selectionTracker!!.onRestoreInstanceState(savedInstanceState)
        adapter!!.setSelectionTracker(selectionTracker)
        return binding!!.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppCompatActivity) activity = context
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onStart() {
        super.onStart()
        subscribeAlertDialog()
        subscribeForceSortAndFilter()
    }

    private fun subscribeAlertDialog() {
        val d = dialogViewModel!!.observeEvents()
            .subscribe { event: BaseAlertDialog.Event ->
                if (event.dialogTag == null || event.dialogTag != TAG_DELETE_DOWNLOADS_DIALOG || deleteDownloadsDialog == null) return@subscribe
                when (event.type) {
                    BaseAlertDialog.EventType.POSITIVE_BUTTON_CLICKED -> {
                        val dialog = deleteDownloadsDialog!!.dialog
                        if (dialog != null) {
                            val withFile = dialog.findViewById<CheckBox>(R.id.delete_with_file)
                            deleteDownloads(withFile.isChecked)
                        }
                        if (actionMode != null) actionMode!!.finish()
                        deleteDownloadsDialog!!.dismiss()
                    }
                    BaseAlertDialog.EventType.NEGATIVE_BUTTON_CLICKED -> deleteDownloadsDialog!!.dismiss()
                }
            }
        disposables.add(d)
    }

    private fun subscribeForceSortAndFilter() {
        disposables.add(
            viewModel!!.onForceSortAndFilter()
                .filter { force: Boolean? -> force!! }
                .observeOn(Schedulers.io())
                .subscribe { force: Boolean? ->
                    disposables.add(
                        downloadSingle
                    )
                })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity == null) activity = getActivity() as AppCompatActivity?
        val provider = ViewModelProvider(requireActivity())
        viewModel = provider.get(DownloadsViewModel::class.java)
        dialogViewModel = provider.get(BaseAlertDialog.SharedViewModel::class.java)
        val fm = childFragmentManager
        deleteDownloadsDialog =
            fm.findFragmentByTag(TAG_DELETE_DOWNLOADS_DIALOG) as BaseAlertDialog?
    }

    override fun onResume() {
        super.onResume()
        if (downloadListState != null) layoutManager!!.onRestoreInstanceState(downloadListState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) downloadListState = savedInstanceState.getParcelable(
            TAG_DOWNLOAD_LIST_STATE
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        downloadListState = layoutManager!!.onSaveInstanceState()
        outState.putParcelable(TAG_DOWNLOAD_LIST_STATE, downloadListState)
        selectionTracker!!.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    protected fun subscribeAdapter() {
        disposables.add(observeDownloads())
    }

    private fun observeDownloads(): Disposable {
        return viewModel!!.observerAllInfoAndPieces()
            .subscribeOn(Schedulers.io())
            .flatMapSingle { infoAndPiecesList: List<InfoAndPieces>? ->
                Flowable.fromIterable(infoAndPiecesList)
                    .filter(fragmentDownloadsFilter)
                    .filter(viewModel!!.downloadFilter)
                    .map { infoAndPieces: InfoAndPieces? ->
                        DownloadItem(
                            infoAndPieces!!
                        )
                    }
                    .sorted(viewModel!!.sorting)
                    .toList()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list: List<DownloadItem> -> adapter!!.submitList(list) }
            ) { t: Throwable? ->
                Log.e(
                    TAG, "Getting info and pieces error: " +
                            Log.getStackTraceString(t)
                )
            }
    }

    private val downloadSingle: Disposable
        get() = viewModel!!.allInfoAndPiecesSingle
            .subscribeOn(Schedulers.io())
            .flatMap { infoAndPiecesList: List<InfoAndPieces>? ->
                Observable.fromIterable(infoAndPiecesList)
                    .filter(fragmentDownloadsFilter)
                    .filter(viewModel!!.downloadFilter)
                    .map { infoAndPieces: InfoAndPieces? ->
                        DownloadItem(
                            infoAndPieces!!
                        )
                    }
                    .sorted(viewModel!!.sorting)
                    .toList()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list: List<DownloadItem> -> adapter!!.submitList(list) }
            ) { t: Throwable? ->
                Log.e(
                    TAG, "Getting info and pieces error: " +
                            Log.getStackTraceString(t)
                )
            }

    abstract override fun onItemClicked(item: DownloadItem)
    private fun setActionModeTitle(itemCount: Int) {
        actionMode!!.title = itemCount.toString()
    }

    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.download_list_action_mode, menu)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.delete_menu -> deleteDownloadsDialog()
                R.id.share_menu -> {
                    shareDownloads()
                    mode.finish()
                }
                R.id.select_all_menu -> selectAllDownloads()
                R.id.share_url_menu -> {
                    shareUrl()
                    mode.finish()
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectionTracker!!.clearSelection()
        }
    }

    private fun deleteDownloadsDialog() {
        if (!isAdded) return
        val fm = childFragmentManager
        if (fm.findFragmentByTag(TAG_DELETE_DOWNLOADS_DIALOG) == null) {
            deleteDownloadsDialog = BaseAlertDialog.newInstance(
                getString(R.string.deleting),
                if (selectionTracker!!.selection.size() > 1) getString(R.string.delete_selected_downloads) else getString(
                    R.string.delete_selected_download
                ),
                R.layout.dialog_delete_downloads,
                getString(R.string.ok),
                getString(R.string.cancel),
                null,
                false
            )
            deleteDownloadsDialog!!.show(fm, TAG_DELETE_DOWNLOADS_DIALOG)
        }
    }

    private fun deleteDownloads(withFile: Boolean) {
        val selections = MutableSelection<DownloadItem>()
        selectionTracker!!.copySelection(selections)
        disposables.add(
            Observable.fromIterable(selections)
                .map { selection: DownloadItem -> selection.info }
                .toList()
                .subscribe { infoList: List<DownloadInfo>? ->
                    viewModel!!.deleteDownloads(
                        infoList,
                        withFile
                    )
                })
    }

    private fun shareDownloads() {
        val selections = MutableSelection<DownloadItem>()
        selectionTracker!!.copySelection(selections)
        disposables.add(
            Observable.fromIterable(selections)
                .toList()
                .subscribe { items: List<DownloadItem> ->
                    val intent = Utils.makeFileShareIntent(
                        requireActivity().applicationContext, items
                    )
                    if (intent != null) {
                        startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
                    } else {
                        Toast.makeText(
                            requireActivity().applicationContext,
                            resources.getQuantityString(R.plurals.unable_sharing, items.size),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }

    @SuppressLint("RestrictedApi")
    private fun selectAllDownloads() {
        val n = adapter!!.itemCount
        if (n > 0) {
            selectionTracker!!.startRange(0)
            selectionTracker!!.extendRange(adapter!!.itemCount - 1)
        }
    }

    private fun shareUrl() {
        val selections = MutableSelection<DownloadItem>()
        selectionTracker!!.copySelection(selections)
        disposables.add(
            Observable.fromIterable(selections)
                .map { item: DownloadItem -> item.info.url }
                .toList()
                .subscribe { urlList: List<String>? ->
                    startActivity(
                        Intent.createChooser(
                            Utils.makeShareUrlIntent(urlList!!),
                            getString(R.string.share_via)
                        )
                    )
                })
    }

    protected fun showDetailsDialog(id: UUID?) {
        if (!isAdded) return
        val fm = childFragmentManager
        if (fm.findFragmentByTag(TAG_DOWNLOAD_DETAILS) == null) {
            val details = DownloadDetailsDialog.newInstance(id)
            details.show(fm, TAG_DOWNLOAD_DETAILS)
        }
    }

    companion object {
        private val TAG = DownloadsFragment::class.java.simpleName
        private const val TAG_DOWNLOAD_LIST_STATE = "download_list_state"
        private const val SELECTION_TRACKER_ID = "selection_tracker_0"
        private const val TAG_DELETE_DOWNLOADS_DIALOG = "delete_downloads_dialog"
        private const val TAG_DOWNLOAD_DETAILS = "download_details"
    }
}
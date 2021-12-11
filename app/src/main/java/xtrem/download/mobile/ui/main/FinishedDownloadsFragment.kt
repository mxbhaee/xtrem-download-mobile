package xtrem.download.mobile.ui.main

import xtrem.download.mobile.ui.main.DownloadsFragment
import xtrem.download.mobile.core.filter.DownloadFilter
import xtrem.download.mobile.core.model.data.entity.InfoAndPieces
import xtrem.download.mobile.core.model.data.StatusCode
import xtrem.download.mobile.ui.main.DownloadListAdapter.FinishClickListener
import xtrem.download.mobile.ui.main.DownloadListAdapter.ErrorClickListener
import xtrem.download.mobile.ui.BaseAlertDialog
import xtrem.download.mobile.core.model.data.entity.DownloadInfo
import io.reactivex.disposables.Disposable
import xtrem.download.mobile.ui.main.FinishedDownloadsFragment
import android.widget.CheckBox
import xtrem.download.mobile.R
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import xtrem.download.mobile.ui.main.DownloadItem
import android.content.Intent
import android.widget.Toast
import xtrem.download.mobile.core.utils.Utils
import xtrem.download.mobile.ui.adddownload.AddInitParams
import xtrem.download.mobile.ui.adddownload.AddDownloadActivity

class FinishedDownloadsFragment :
    DownloadsFragment(DownloadFilter { item: InfoAndPieces -> StatusCode.isStatusCompleted(item.info.statusCode) }),
    FinishClickListener, ErrorClickListener {
    private var deleteDownloadDialog: BaseAlertDialog? = null
    private var dialogViewModel: BaseAlertDialog.SharedViewModel? = null
    private var downloadForDeletion: DownloadInfo? = null
    override fun onStart() {
        super.onStart()
        subscribeAdapter()
        subscribeAlertDialog()
    }

    private fun subscribeAlertDialog() {
        val d = dialogViewModel!!.observeEvents()
            .subscribe { event: BaseAlertDialog.Event ->
                if (event.dialogTag == null || event.dialogTag != TAG_DELETE_DOWNLOAD_DIALOG || deleteDownloadDialog == null) return@subscribe
                when (event.type) {
                    BaseAlertDialog.EventType.POSITIVE_BUTTON_CLICKED -> {
                        val dialog = deleteDownloadDialog!!.dialog
                        if (dialog != null && downloadForDeletion != null) {
                            val withFile = dialog.findViewById<CheckBox>(R.id.delete_with_file)
                            deleteDownload(downloadForDeletion!!, withFile.isChecked)
                        }
                        downloadForDeletion = null
                        deleteDownloadDialog!!.dismiss()
                    }
                    BaseAlertDialog.EventType.NEGATIVE_BUTTON_CLICKED -> {
                        downloadForDeletion = null
                        deleteDownloadDialog!!.dismiss()
                    }
                    else -> {
                        //TODO
                    }
                }
            }
        disposables.add(d)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) downloadForDeletion = savedInstanceState.getParcelable(
            TAG_DOWNLOAD_FOR_DELETION
        )
        val fm = childFragmentManager
        deleteDownloadDialog = fm.findFragmentByTag(TAG_DELETE_DOWNLOAD_DIALOG) as BaseAlertDialog?
        dialogViewModel = ViewModelProvider(requireActivity()).get(
            BaseAlertDialog.SharedViewModel::class.java
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(TAG_DOWNLOAD_FOR_DELETION, downloadForDeletion)
        super.onSaveInstanceState(outState)
    }

    override fun onItemClicked(item: DownloadItem) {
        val file = activity?.let { Utils.createOpenFileIntent(it.applicationContext, item.info) }
        if (file != null) {
            startActivity(Intent.createChooser(file, getString(R.string.open_using)))
        } else {
            Toast.makeText(
                activity?.applicationContext,
                getString(R.string.file_not_available),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onItemMenuClicked(menuId: Int, item: DownloadItem) {
        when (menuId) {
            R.id.delete_menu -> {
                downloadForDeletion = item.info
                showDeleteDownloadDialog()
            }
            R.id.open_details_menu -> showDetailsDialog(item.info.id)
            R.id.share_menu -> shareDownload(item)
            R.id.share_url_menu -> shareUrl(item)
            R.id.redownload_menu -> showAddDownloadDialog(item.info)
        }
    }

    override fun onItemResumeClicked(item: DownloadItem) {
        viewModel!!.resumeIfError(item.info)
    }

    private fun showDeleteDownloadDialog() {
        if (!isAdded) return
        val fm = childFragmentManager
        if (fm.findFragmentByTag(TAG_DELETE_DOWNLOAD_DIALOG) == null) {
            deleteDownloadDialog = BaseAlertDialog.newInstance(
                getString(R.string.deleting),
                getString(R.string.delete_selected_download),
                R.layout.dialog_delete_downloads,
                getString(R.string.ok),
                getString(R.string.cancel),
                null,
                false
            )
            deleteDownloadDialog!!.show(fm, TAG_DELETE_DOWNLOAD_DIALOG)
        }
    }

    private fun deleteDownload(info: DownloadInfo, withFile: Boolean) {
        viewModel!!.deleteDownload(info, withFile)
    }

    private fun shareDownload(item: DownloadItem) {
        val intent = Utils.makeFileShareIntent(requireActivity().applicationContext, listOf(item))
        if (intent != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
        } else {
            Toast.makeText(
                requireActivity().applicationContext,
                resources.getQuantityString(R.plurals.unable_sharing, 1), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareUrl(item: DownloadItem) {
        startActivity(
            Intent.createChooser(
                Utils.makeShareUrlIntent(item.info.url),
                getString(R.string.share_via)
            )
        )
    }

    private fun showAddDownloadDialog(info: DownloadInfo) {
        val initParams = AddInitParams()
        initParams.url = info.url
        initParams.dirPath = info.dirPath
        initParams.fileName = info.fileName
        initParams.description = info.description
        initParams.userAgent = info.userAgent
        initParams.unmeteredConnectionsOnly = info.unmeteredConnectionsOnly
        initParams.retry = info.retry
        initParams.replaceFile = true
        val i = Intent(activity, AddDownloadActivity::class.java)
        i.putExtra(AddDownloadActivity.TAG_INIT_PARAMS, initParams)
        startActivity(i)
    }

    companion object {
        private val TAG = FinishedDownloadsFragment::class.java.simpleName
        private const val TAG_DELETE_DOWNLOAD_DIALOG = "delete_download_dialog"
        private const val TAG_DOWNLOAD_FOR_DELETION = "download_for_deletion"
        fun newInstance(): FinishedDownloadsFragment {
            val fragment = FinishedDownloadsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
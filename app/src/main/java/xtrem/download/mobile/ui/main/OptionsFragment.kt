package xtrem.download.mobile.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import xtrem.download.mobile.R
import xtrem.download.mobile.core.model.data.entity.DownloadInfo
import xtrem.download.mobile.core.utils.Utils
import xtrem.download.mobile.ui.BaseAlertDialog
import xtrem.download.mobile.ui.adddownload.AddDownloadActivity
import xtrem.download.mobile.ui.adddownload.AddInitParams

class OptionsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_options, container, false)
    }

    private fun shareDownload(item: DownloadItem) {
        val intent = activity?.let { Utils.makeFileShareIntent(it.applicationContext, listOf(item)) }
        if (intent != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
        } else {
            Toast.makeText(
                activity?.applicationContext,
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
        fun newInstance(): OptionsFragment {
            val fragment = OptionsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
package xtrem.download.mobile.ui.main

import xtrem.download.mobile.core.filter.DownloadFilter
import xtrem.download.mobile.core.model.data.entity.InfoAndPieces
import xtrem.download.mobile.core.model.data.StatusCode
import xtrem.download.mobile.ui.main.DownloadListAdapter.QueueClickListener
import xtrem.download.mobile.ui.main.DownloadItem
import xtrem.download.mobile.ui.main.QueuedDownloadsFragment
import android.os.Bundle

class QueuedDownloadsFragment :
    DownloadsFragment(DownloadFilter { item: InfoAndPieces -> !StatusCode.isStatusCompleted(item.info.statusCode) }),
    QueueClickListener {
    override fun onStart() {
        super.onStart()
        subscribeAdapter()
    }

    override fun onItemClicked(item: DownloadItem) {
        showDetailsDialog(item.info.id)
    }

    override fun onItemPauseClicked(item: DownloadItem) {
        viewModel!!.pauseResumeDownload(item.info)
    }

    override fun onItemCancelClicked(item: DownloadItem) {
        viewModel!!.deleteDownload(item.info, true)
    }

    companion object {
        private val TAG = QueuedDownloadsFragment::class.java.simpleName
        fun newInstance(): QueuedDownloadsFragment {
            val fragment = QueuedDownloadsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
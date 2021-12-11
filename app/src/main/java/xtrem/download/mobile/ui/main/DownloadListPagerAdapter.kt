package xtrem.download.mobile.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import xtrem.download.mobile.ui.main.DownloadListPagerAdapter
import xtrem.download.mobile.ui.main.QueuedDownloadsFragment
import xtrem.download.mobile.ui.main.FinishedDownloadsFragment
import androidx.viewpager2.widget.ViewPager2.OffscreenPageLimit

class DownloadListPagerAdapter(fa: FragmentActivity?) : FragmentStateAdapter(fa!!) {
    override fun createFragment(position: Int): Fragment {
        /* Stubs */
        return when (position) {
            QUEUED_FRAG_POS -> QueuedDownloadsFragment.newInstance()
            COMPLETED_FRAG_POS -> FinishedDownloadsFragment.newInstance()
            //SORT_FRAG_POS -> SortFragment.newInstance()
            OPTIONS_FRAG_POS -> OptionsFragment.newInstance()
            else -> Fragment()
        }
    }

    override fun getItemCount(): Int {
        return NUM_FRAGMENTS
    }

    companion object {
        @OffscreenPageLimit
        val NUM_FRAGMENTS = 3
        const val QUEUED_FRAG_POS = 0
        const val COMPLETED_FRAG_POS = 1
        const val OPTIONS_FRAG_POS = 2
    }
}
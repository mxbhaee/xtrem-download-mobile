package xtrem.download.mobile.ui.main

import xtrem.download.mobile.core.model.data.entity.InfoAndPieces
import xtrem.download.mobile.ui.main.DownloadItem

/*
 * Wrapper of InfoAndPieces class for DownloadListAdapter, that override Object::equals method
 * Necessary for other behavior in case if item was selected (see SelectionTracker).
 */
class DownloadItem(infoAndPieces: InfoAndPieces) : InfoAndPieces() {
    /*
     * Compare objects by their content (info, pieces)
     */
    fun equalsContent(item: DownloadItem?): Boolean {
        return super.equals(item)
    }

    /*
     * Compare objects by info id
     */
    override fun equals(o: Any?): Boolean {
        if (o !is DownloadItem) return false
        return if (o === this) true else info.id == o.info.id
    }

    init {
        info = infoAndPieces.info
        pieces = infoAndPieces.pieces
    }
}
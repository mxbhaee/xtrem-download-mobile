
package xtrem.download.mobile.ui.browser.bookmarks;

import androidx.annotation.NonNull;

import xtrem.download.mobile.core.model.data.entity.BrowserBookmark;

public class BrowserBookmarkItem extends BrowserBookmark implements Comparable<BrowserBookmarkItem>
{
    public BrowserBookmarkItem(@NonNull BrowserBookmark bookmark)
    {
        super(bookmark.url, bookmark.name, bookmark.dateAdded);
    }

    @Override
    public int compareTo(BrowserBookmarkItem o)
    {
        return Long.compare(o.dateAdded, dateAdded);
    }

    public boolean equalsContent(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrowserBookmark that = (BrowserBookmark) o;

        if (dateAdded != that.dateAdded) return false;
        if (!url.equals(that.url)) return false;
        return name.equals(that.name);
    }
}

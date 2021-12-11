
package xtrem.download.mobile.core.sorting;

import androidx.annotation.NonNull;

import xtrem.download.mobile.ui.main.DownloadItem;

import java.util.Comparator;

public class DownloadSortingComparator implements Comparator<DownloadItem>
{
    private DownloadSorting sorting;

    public DownloadSortingComparator(@NonNull DownloadSorting sorting)
    {
        this.sorting = sorting;
    }

    public DownloadSorting getSorting()
    {
        return sorting;
    }

    @Override
    public int compare(DownloadItem o1, DownloadItem o2)
    {
        return DownloadSorting.SortingColumns.fromValue(sorting.getColumnName())
                .compare(o1, o2, sorting.getDirection());
    }
}

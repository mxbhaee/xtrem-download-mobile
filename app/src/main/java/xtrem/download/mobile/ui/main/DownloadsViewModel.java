package xtrem.download.mobile.ui.main;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import xtrem.download.mobile.core.RepositoryHelper;
import xtrem.download.mobile.core.filter.DownloadFilter;
import xtrem.download.mobile.core.filter.DownloadFilterCollection;
import xtrem.download.mobile.core.model.DownloadEngine;
import xtrem.download.mobile.core.model.data.entity.DownloadInfo;
import xtrem.download.mobile.core.model.data.entity.InfoAndPieces;
import xtrem.download.mobile.core.sorting.DownloadSorting;
import xtrem.download.mobile.core.sorting.DownloadSortingComparator;
import xtrem.download.mobile.core.storage.DataRepository;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class DownloadsViewModel extends AndroidViewModel
{
    private DataRepository repo;
    private DownloadEngine engine;
    private DownloadSortingComparator sorting = new DownloadSortingComparator(
            new DownloadSorting(DownloadSorting.SortingColumns.none, DownloadSorting.Direction.ASC));
    private DownloadFilter categoryFilter = DownloadFilterCollection.all();
    private DownloadFilter statusFilter = DownloadFilterCollection.all();
    private DownloadFilter dateAddedFilter = DownloadFilterCollection.all();
    private PublishSubject<Boolean> forceSortAndFilter = PublishSubject.create();

    private String searchQuery;
    private DownloadFilter searchFilter = (infoAndPieces) -> {
      if (TextUtils.isEmpty(searchQuery))
          return true;

        String filterPattern = searchQuery.toLowerCase().trim();
        String fileName = infoAndPieces.info.fileName;
        String description = infoAndPieces.info.description;

        return fileName.toLowerCase().contains(filterPattern) ||
                (description != null && description.toLowerCase().contains(filterPattern));
    };

    public DownloadsViewModel(@NonNull Application application)
    {
        super(application);

        repo = RepositoryHelper.getDataRepository(application);
        engine = DownloadEngine.getInstance(application);
    }

    public Flowable<List<InfoAndPieces>> observerAllInfoAndPieces()
    {
        return repo.observeAllInfoAndPieces();
    }

    public Single<List<InfoAndPieces>> getAllInfoAndPiecesSingle()
    {
        return repo.getAllInfoAndPiecesSingle();
    }

    public void deleteDownload(DownloadInfo info, boolean withFile)
    {
        engine.deleteDownloads(withFile, info);
    }

    public void deleteDownloads(List<DownloadInfo> infoList, boolean withFile)
    {
        engine.deleteDownloads(withFile, infoList.toArray(new DownloadInfo[0]));
    }

    public void setSort(@NonNull DownloadSortingComparator sorting, boolean force)
    {
        this.sorting = sorting;
        if (force && !sorting.getSorting().getColumnName().equals(DownloadSorting.SortingColumns.none.name()))
            forceSortAndFilter.onNext(true);
    }

    public void setCategoryFilter(@NonNull DownloadFilter categoryFilter, boolean force)
    {
        this.categoryFilter = categoryFilter;
        if (force)
            forceSortAndFilter.onNext(true);
    }

    public void setStatusFilter(@NonNull DownloadFilter statusFilter, boolean force)
    {
        this.statusFilter = statusFilter;
        if (force)
            forceSortAndFilter.onNext(true);
    }

    public void setDateAddedFilter(@NonNull DownloadFilter dateAddedFilter, boolean force)
    {
        this.dateAddedFilter = dateAddedFilter;
        if (force)
            forceSortAndFilter.onNext(true);
    }

    @NonNull
    public DownloadSortingComparator getSorting()
    {
        return sorting;
    }

    public void setSearchQuery(@Nullable String searchQuery)
    {
        this.searchQuery = searchQuery;
        forceSortAndFilter.onNext(true);
    }

    public void resetSearch()
    {
        setSearchQuery(null);
    }

    @NonNull
    public DownloadFilter getDownloadFilter()
    {
        return (infoAndPieces) -> categoryFilter.test(infoAndPieces) &&
                statusFilter.test(infoAndPieces) &&
                dateAddedFilter.test(infoAndPieces) &&
                searchFilter.test(infoAndPieces);
    }

    public Observable<Boolean> onForceSortAndFilter()
    {
        return forceSortAndFilter;
    }

    public void pauseResumeDownload(@NonNull DownloadInfo info)
    {
        engine.pauseResumeDownload(info.id);
    }

    public void resumeIfError(@NonNull DownloadInfo info)
    {
        engine.resumeIfError(info.id);
    }
}

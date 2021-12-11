
package xtrem.download.mobile.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import xtrem.download.mobile.core.RepositoryHelper;
import xtrem.download.mobile.core.model.DownloadScheduler;
import xtrem.download.mobile.core.model.data.StatusCode;
import xtrem.download.mobile.core.model.data.entity.DownloadInfo;
import xtrem.download.mobile.core.storage.DataRepository;

import java.util.List;

/*
 * Used only by DownloadScheduler.
 */

public class RestoreDownloadsWorker extends Worker
{
    @SuppressWarnings("unused")
    private static final String TAG = RestoreDownloadsWorker.class.getSimpleName();

    public RestoreDownloadsWorker(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Context context = getApplicationContext();
        DataRepository repo = RepositoryHelper.getDataRepository(context);

        List<DownloadInfo> infoList = repo.getAllInfo();
        if (infoList.isEmpty())
            return Result.success();

        for (DownloadInfo info : infoList) {
            if (info == null)
                continue;
            /*
             * Also restore those downloads that are incorrectly completed and
             * have the wrong status (for example, after crashing)
             */
            if (info.statusCode == StatusCode.STATUS_PENDING ||
                info.statusCode == StatusCode.STATUS_RUNNING ||
                info.statusCode == StatusCode.STATUS_FETCH_METADATA)
                DownloadScheduler.run(context, info);
        }

        return Result.success();
    }
}


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

public class RunAllWorker extends Worker
{
    @SuppressWarnings("unused")
    private static final String TAG = RunAllWorker.class.getSimpleName();

    public static final String TAG_IGNORE_PAUSED = "ignore_paused";

    public RunAllWorker(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Context context = getApplicationContext();
        DataRepository repo = RepositoryHelper.getDataRepository(context);
        boolean ignorePaused = getInputData().getBoolean(TAG_IGNORE_PAUSED, false);

        List<DownloadInfo> infoList = repo.getAllInfo();
        if (infoList.isEmpty())
            return Result.success();

        for (DownloadInfo info : infoList) {
            if (info == null)
                continue;

            if (info.statusCode == StatusCode.STATUS_STOPPED ||
                (!ignorePaused && info.statusCode == StatusCode.STATUS_PAUSED))
                DownloadScheduler.run(context, info);
        }

        return Result.success();
    }
}

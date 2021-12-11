

package xtrem.download.mobile.core;

import android.content.Context;

import androidx.annotation.NonNull;

import xtrem.download.mobile.core.settings.SettingsRepository;
import xtrem.download.mobile.core.settings.SettingsRepositoryImpl;
import xtrem.download.mobile.core.storage.AppDatabase;
import xtrem.download.mobile.core.storage.BrowserRepository;
import xtrem.download.mobile.core.storage.BrowserRepositoryImpl;
import xtrem.download.mobile.core.storage.DataRepository;
import xtrem.download.mobile.core.storage.DataRepositoryImpl;

public class RepositoryHelper
{
    private static DataRepositoryImpl dataRepo;
    private static SettingsRepositoryImpl settingsRepo;
    private static BrowserRepository browserRepository;

    public synchronized static DataRepository getDataRepository(@NonNull Context appContext)
    {
        if (dataRepo == null)
            dataRepo = new DataRepositoryImpl(appContext,
                    AppDatabase.getInstance(appContext));

        return dataRepo;
    }

    public synchronized static SettingsRepository getSettingsRepository(@NonNull Context appContext)
    {
        if (settingsRepo == null)
            settingsRepo = new SettingsRepositoryImpl(appContext);

        return settingsRepo;
    }

    public synchronized static BrowserRepository getBrowserRepository(@NonNull Context appContext)
    {
        if (browserRepository == null)
            browserRepository = new BrowserRepositoryImpl(AppDatabase.getInstance(appContext));

        return browserRepository;
    }
}

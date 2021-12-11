
package xtrem.download.mobile.core.storage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import xtrem.download.mobile.core.model.data.entity.BrowserBookmark;
import xtrem.download.mobile.core.model.data.entity.DownloadInfo;
import xtrem.download.mobile.core.model.data.entity.DownloadPiece;
import xtrem.download.mobile.core.model.data.entity.Header;
import xtrem.download.mobile.core.model.data.entity.UserAgent;
import xtrem.download.mobile.core.storage.converter.UUIDConverter;
import xtrem.download.mobile.core.storage.dao.BrowserBookmarksDao;
import xtrem.download.mobile.core.storage.dao.DownloadDao;
import xtrem.download.mobile.core.storage.dao.UserAgentDao;
import xtrem.download.mobile.core.system.SystemFacade;
import xtrem.download.mobile.core.system.SystemFacadeHelper;
import xtrem.download.mobile.core.utils.UserAgentUtils;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

@Database(entities = {DownloadInfo.class,
        DownloadPiece.class,
        Header.class,
        UserAgent.class,
        BrowserBookmark.class},
        version = 5)
@TypeConverters({UUIDConverter.class})
public abstract class AppDatabase extends RoomDatabase
{
    private static final String DATABASE_NAME = "tachibana_downloader.db";

    private static volatile AppDatabase INSTANCE;

    public abstract DownloadDao downloadDao();

    public abstract UserAgentDao userAgentDao();

    public abstract BrowserBookmarksDao browserBookmarksDao();

    private final MutableLiveData<Boolean> isDatabaseCreated = new MutableLiveData<>();

    public static AppDatabase getInstance(Context context)
    {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(context.getApplicationContext());
                    INSTANCE.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }

        return INSTANCE;
    }

    private static AppDatabase buildDatabase(Context appContext)
    {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db)
                    {
                        super.onCreate(db);
                        Completable.fromAction(() -> {
                            AppDatabase database = AppDatabase.getInstance(appContext);
                            database.runInTransaction(() -> {
                                SystemFacade systemFacade = SystemFacadeHelper.getSystemFacade(appContext);
                                String userAgentStr = systemFacade.getSystemUserAgent();

                                UserAgent systemUserAgent;
                                if (userAgentStr == null)
                                    systemUserAgent = UserAgentUtils.defaultUserAgents[0];
                                else
                                    systemUserAgent = new UserAgent(userAgentStr);
                                systemUserAgent.readOnly = true;

                                database.userAgentDao().add(systemUserAgent);
                                database.userAgentDao().add(UserAgentUtils.defaultUserAgents);
                            });
                            database.setDatabaseCreated();
                        })
                       .subscribeOn(Schedulers.io())
                       .subscribe();
                    }
                })
                .addMigrations(DatabaseMigration.getMigrations())
                .build();
    }

    /*
     * Check whether the database already exists and expose it via getDatabaseCreated()
     */

    private void updateDatabaseCreated(final Context context)
    {
        if (context.getDatabasePath(DATABASE_NAME).exists())
            setDatabaseCreated();
    }

    private void setDatabaseCreated()
    {
        isDatabaseCreated.postValue(true);
    }


    public LiveData<Boolean> getDatabaseCreated()
    {
        return isDatabaseCreated;
    }
}
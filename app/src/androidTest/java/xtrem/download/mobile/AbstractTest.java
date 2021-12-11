package xtrem.download.mobile;

import android.Manifest;
import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import xtrem.download.mobile.core.FakeSystemFacade;
import xtrem.download.mobile.core.RepositoryHelper;
import xtrem.download.mobile.core.settings.SettingsRepository;
import xtrem.download.mobile.core.storage.AppDatabase;
import xtrem.download.mobile.core.storage.DataRepositoryImpl;
import xtrem.download.mobile.core.system.FileSystemFacade;
import xtrem.download.mobile.core.system.SystemFacadeHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

public class AbstractTest
{
    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE);

    protected Context context;
    protected AppDatabase db;
    protected DataRepositoryImpl repo;
    protected FakeSystemFacade systemFacade;
    protected SettingsRepository pref;
    protected FileSystemFacade fs;

    @Before
    public void init()
    {
        context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context,
                AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        repo = new DataRepositoryImpl(context, db);
        systemFacade = new FakeSystemFacade(context);
        pref = RepositoryHelper.getSettingsRepository(context);
        fs = SystemFacadeHelper.getFileSystemFacade(context);
    }

    @After
    public void finish()
    {
        db.close();
    }
}

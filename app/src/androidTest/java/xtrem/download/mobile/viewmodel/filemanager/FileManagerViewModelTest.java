package xtrem.download.mobile.viewmodel.filemanager;

import android.net.Uri;
import android.util.Log;

import xtrem.download.mobile.AbstractTest;
import xtrem.download.mobile.ui.filemanager.FileManagerConfig;
import xtrem.download.mobile.ui.filemanager.FileManagerViewModel;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileManagerViewModelTest extends AbstractTest
{
    private FileManagerViewModel viewModel;
    private FileManagerConfig config;

    @Before
    public void init()
    {
        super.init();

        config = new FileManagerConfig(fs.getUserDirPath(),
                null, FileManagerConfig.DIR_CHOOSER_MODE);
        viewModel = new FileManagerViewModel(context, config, fs.getUserDirPath());
    }

    @Test
    public void testOpenDirectory()
    {
        try {
            viewModel.jumpToDirectory(fs.getUserDirPath());
            viewModel.openDirectory("Android");
            Uri path = viewModel.getCurDirectoryUri();
            assertNotNull(path);
            assertTrue(path.getPath().endsWith("Android"));

        } catch (Exception e) {
            fail(Log.getStackTraceString(e));
        }
    }

    @Test
    public void testJumpToDirectory()
    {
        try {
            viewModel.jumpToDirectory(fs.getUserDirPath() + "/Android");
            Uri path = viewModel.getCurDirectoryUri();
            assertNotNull(path);
            assertTrue(path.getPath().endsWith("Android"));

        } catch (Exception e) {
            fail(Log.getStackTraceString(e));
        }
    }

    @Test
    public void testUpToParentDirectory()
    {
        try {
            viewModel.jumpToDirectory(fs.getUserDirPath() + "/Android");
            viewModel.upToParentDirectory();
            Uri path = viewModel.getCurDirectoryUri();
            assertNotNull(path);
            assertFalse(path.getPath().endsWith("Android"));

        } catch (Exception e) {
            fail(Log.getStackTraceString(e));
        }
    }

    @Test
    public void testCreateFile()
    {
        File f = null;
        try {
            viewModel.jumpToDirectory(fs.getUserDirPath());
            viewModel.createFile("test.txt");
            Uri filePath = viewModel.getFileUri("test.txt");
            assertNotNull(filePath);
            f = new File(filePath.getPath());
            assertTrue(f.exists());

        } catch (Exception e) {
            fail(Log.getStackTraceString(e));
        } finally {
            if (f != null)
                f.delete();
        }
    }
}
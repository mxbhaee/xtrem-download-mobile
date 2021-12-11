package xtrem.download.mobile.core.model;

import android.net.Uri;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import xtrem.download.mobile.AbstractTest;
import xtrem.download.mobile.core.model.data.PieceResult;
import xtrem.download.mobile.core.model.data.StatusCode;
import xtrem.download.mobile.core.model.data.entity.DownloadInfo;
import xtrem.download.mobile.core.model.data.entity.DownloadPiece;
import xtrem.download.mobile.core.utils.DigestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class PieceThreadTest extends AbstractTest
{
    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private String linuxName = "linux-1.0.tar.gz";
    private String linuxUrl = "https://mirrors.edge.kernel.org/pub/linux/kernel/v1.0/linux-1.0.tar.gz";
    private Uri dir;

    @Override
    public void init()
    {
        super.init();

        dir = Uri.parse("file://" + fs.getDefaultDownloadPath());
    }

    @Test
    public void downloadPieceTest()
    {
        long size = 1024L;
        /* Hash of 1 Kb data */
        String md5Hash = "68a17dd8eff5ba6abc70efd75705270f";

        /* Write download info */
        DownloadInfo info = new DownloadInfo(dir, linuxUrl, linuxName);
        UUID id = info.id;
        repo.addInfo(info, new ArrayList<>());
        DownloadPiece piece = repo.getPiece(0, id);
        piece.size = size;
        repo.updatePiece(piece);

        /* Create file for writing */
        File file = new File(dir.getPath(), linuxName);
        try {
            if (!file.exists())
                assertTrue(file.createNewFile());
            assertTrue(file.exists());

            /* Run piece task */
            Future<PieceResult> f = runTask(new PieceThreadImpl(id, 0, repo, fs, systemFacade, pref));
            assertTrue(f.isDone());
            assertFalse(f.isCancelled());

            PieceResult res = f.get();
            assertNotNull(res);
            assertEquals(id, res.infoId);
            assertEquals(0, res.pieceIndex);

            /* Read piece info */
            piece = repo.getPiece(0, id);
            assertNotNull(piece);
            assertEquals(getStatus(piece), StatusCode.STATUS_SUCCESS, piece.statusCode);

            /* Read and check piece chunk */
            try (FileInputStream is = new FileInputStream(file)) {
                assertEquals(md5Hash, DigestUtils.makeMd5Hash(is));
            }

        } catch (Throwable e) {
            fail(Log.getStackTraceString(e));
        } finally {
            file.delete();
        }

        assertEquals(size, piece.size);
    }

    private String getStatus(DownloadPiece piece)
    {
        return "{code=" + piece.statusCode + ", msg=" + piece.statusMsg + "}";
    }

    @Test
    public void zeroLengthTest()
    {
        /* Write download info */
        DownloadInfo info = new DownloadInfo(dir, linuxUrl, linuxName);
        UUID id = info.id;
        repo.addInfo(info, new ArrayList<>());
        DownloadPiece piece = repo.getPiece(0, id);
        piece.size = 0L;
        repo.updatePiece(piece);

        /* Create file for writing */
        File file = new File(dir.getPath(), linuxName);
        try {
            if (!file.exists())
                assertTrue(file.createNewFile());
            assertTrue(file.exists());

            /* Run piece task */
            Future<PieceResult> f = runTask(new PieceThreadImpl(id, 0, repo, fs, systemFacade, pref));
            assertTrue(f.isDone());
            assertFalse(f.isCancelled());

            PieceResult res = f.get();
            assertNotNull(res);
            assertEquals(id, res.infoId);
            assertEquals(0, res.pieceIndex);

            /* Read piece info */
            piece = repo.getPiece(0, id);
            assertNotNull(piece);
            assertEquals(getStatus(piece), StatusCode.STATUS_SUCCESS, piece.statusCode);

            assertEquals(0, piece.curBytes);
            assertEquals(0, file.length());

        } catch (Throwable e) {
            fail(Log.getStackTraceString(e));
        } finally {
            file.delete();
        }
    }

    private Future<PieceResult> runTask(PieceThread task) throws InterruptedException
    {
        Future<PieceResult> f = exec.submit(task);
        exec.shutdownNow();
        /* Wait 5 minutes */
        exec.awaitTermination(5, TimeUnit.MINUTES);

        return f;
    }
}

package xtrem.download.mobile.core.model.data.entity;

import android.net.Uri;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
// TODO: needs Java 9
@Config(sdk = Build.VERSION_CODES.P)
public class DownloadInfoTest
{
    private DownloadInfo info = new DownloadInfo(Uri.parse("file:///"),
            "http://example.org",
            "example");

    @Test
    public void testSetNumPieces()
    {
        try {
            info.setNumPieces(0);
            fail("Zero pieces test failed");
        } catch (IllegalArgumentException e) { }

        info.partialSupport = false;
        try {
            info.setNumPieces(3);
            fail("Partial support test failed");
        } catch (IllegalStateException e) { }

        info.partialSupport = true;
        info.totalBytes = -1;
        try {
            info.setNumPieces(3);
            fail("Multipart with unavailable size test failed");
        } catch (IllegalStateException e) { }

        info.totalBytes = 0;
        try {
            info.setNumPieces(3);
            fail("Multipart with zero size test failed");
        } catch (IllegalStateException e) { }

        info.totalBytes = 2;
        try {
            info.setNumPieces(3);
            fail("Size less than pieces test failed");
        } catch (IllegalStateException e) { }
    }

    @Test
    public void makePiecesTest()
    {
        info.totalBytes = 3;
        info.setNumPieces(3);
        checkPiecesAlignment(info,
                new long[][] {
                        {0, 0},
                        {1, 1},
                        {2, 2}
                });

        /* With different size of last piece */
        info.totalBytes = 1024;
        info.setNumPieces(15);
        checkPiecesAlignment(info,
                new long[][] {
                        {0, 67},
                        {68, 135},
                        {136, 203},
                        {204, 271},
                        {272, 339},
                        {340, 407},
                        {408, 475},
                        {476, 543},
                        {544, 611},
                        {612, 679},
                        {680, 747},
                        {748, 815},
                        {816, 883},
                        {884, 951},
                        {952, 1023},
                });
    }

    private void checkPiecesAlignment(DownloadInfo info, long[][] testAlignment) throws IllegalArgumentException
    {
        List<DownloadPiece> pieces = info.makePieces();
        for (int i = 0; i < pieces.size(); i++) {
            assertEquals(testAlignment[i][0], info.pieceStartPos(pieces.get(i)));
            assertEquals(testAlignment[i][1], info.pieceEndPos(pieces.get(i)));
        }
    }
}
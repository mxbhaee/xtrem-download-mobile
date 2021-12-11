
package xtrem.download.mobile.core.utils;

import xtrem.download.mobile.AbstractTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest extends AbstractTest
{
    @Test
    public void testGetHttpFileName()
    {
        String actual = Utils.getHttpFileName(fs, "http://example.org/file.txt", null, null, null);
        assertEquals("file.txt", actual);
        actual = Utils.getHttpFileName(fs, "http://example.org/file.txt?foo=bar", null, null, null);
        assertEquals("file.txt", actual);
    }

    @Test
    public void testGetHttpFileName_noExtension()
    {
        String actual = Utils.getHttpFileName(fs, "http://example.org/file", null, null, null);
        assertEquals("file.bin", actual);
        actual = Utils.getHttpFileName(fs, "http://example.org/file", null, null, "image/jpeg");
        assertEquals("file.jpg", actual);
        actual = Utils.getHttpFileName(fs, "http://example.org/file", null, null, "application/octet-stream");
        assertEquals("file.bin", actual);
    }

    @Test
    public void testGetHttpFileName_withDisposition()
    {
        String actual = Utils.getHttpFileName(fs, "http://example.org/file.txt",
                "attachment; filename=\"subdir/real.pdf\"", null, null);
        assertEquals("real.pdf", actual);
    }

    @Test
    public void testGetHttpFileName_withLocation()
    {
        String actual = Utils.getHttpFileName(fs, "http://example.org/file.txt",
                null, "Content-Location: subdir/real.pdf", null);
        assertEquals("real.pdf", actual);
    }

    @Test
    public void testGetHttpFileName_withDispositionAndLocation()
    {
        String actual = Utils.getHttpFileName(fs, "http://example.org/file.txt",
                "attachment; filename=\"subdir/real.pdf\"",
                "Content-Location: subdir/file.pdf", null);
        assertEquals("real.pdf", actual);
    }

    @Test
    public void testGetHttpFileName_dispositionWithEncoding()
    {
        String actual = Utils.getHttpFileName(fs, "http://example.org/file.pdf",
                "attachment;filename=\"foo.txt\";filename*=UTF-8''foo.txt", null, null);
        assertEquals("foo.txt", actual);
    }
}
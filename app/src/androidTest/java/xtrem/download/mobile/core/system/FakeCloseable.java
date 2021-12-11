package xtrem.download.mobile.core.system;

import java.io.Closeable;

public class FakeCloseable implements Closeable
{
    public boolean closed = false;

    @Override
    public void close()
    {
        closed = true;
    }
}

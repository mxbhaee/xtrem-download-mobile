package xtrem.download.mobile.core.system;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;

public class FakeSysCall implements SysCall
{
    private long availableBytes = -1;

    @Override
    public void lseek(@NonNull FileDescriptor fd, long offset) { }

    @Override
    public void fallocate(@NonNull FileDescriptor fd, long length) { }

    @Override
    public long availableBytes(@NonNull FileDescriptor fd)
    {
        return availableBytes;
    }

    public void setAvailableBytes(long availableBytes)
    {
        this.availableBytes = availableBytes;
    }
}

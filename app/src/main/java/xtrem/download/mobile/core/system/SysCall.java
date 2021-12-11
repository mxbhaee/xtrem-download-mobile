
package xtrem.download.mobile.core.system;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.IOException;

/*
 * A platform dependent interface for system calls.
 */

interface SysCall
{
    void lseek(@NonNull FileDescriptor fd, long offset) throws IOException, UnsupportedOperationException;

    void fallocate(@NonNull FileDescriptor fd, long length) throws IOException;

    long availableBytes(@NonNull FileDescriptor fd) throws IOException;
}

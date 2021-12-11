
package xtrem.download.mobile.core.system;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import xtrem.download.mobile.core.utils.Utils;

class FsModuleResolverImpl implements FsModuleResolver
{
    private Context appContext;
    private SafFsModule safModule;
    private DefaultFsModule defaultModule;

    public FsModuleResolverImpl(@NonNull Context appContext)
    {
        this.appContext = appContext;
        this.safModule = new SafFsModule(appContext);
        this.defaultModule = new DefaultFsModule(appContext);
    }

    @Override
    public FsModule resolveFsByUri(@NonNull Uri uri)
    {
        if (Utils.isSafPath(appContext, uri))
            return safModule;
        else if (Utils.isFileSystemPath(uri))
            return defaultModule;
        else
            throw new IllegalArgumentException("Cannot resolve file system for the given uri: " + uri);
    }
}

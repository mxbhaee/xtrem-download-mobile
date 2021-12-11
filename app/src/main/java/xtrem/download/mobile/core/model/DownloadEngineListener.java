
package xtrem.download.mobile.core.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public abstract class DownloadEngineListener
{
    public void onDownloadsCompleted() {}

    public void onApplyingParams(@NonNull UUID id) {}

    public void onParamsApplied(@NonNull UUID id, @Nullable String name, @Nullable Throwable e) {}
}

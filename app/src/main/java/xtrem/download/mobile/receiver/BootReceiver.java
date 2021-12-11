
package xtrem.download.mobile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import xtrem.download.mobile.core.RepositoryHelper;
import xtrem.download.mobile.core.model.DownloadEngine;
import xtrem.download.mobile.core.settings.SettingsRepository;

/*
 * The receiver for autostart stopped downloads.
 */

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction() == null)
            return;

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SettingsRepository pref = RepositoryHelper.getSettingsRepository(context.getApplicationContext());
            if (pref.autostart()) {
                DownloadEngine engine = DownloadEngine.getInstance(context.getApplicationContext());
                engine.restoreDownloads();
            }
        }
    }
}

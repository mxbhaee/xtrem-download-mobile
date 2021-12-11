
package xtrem.download.mobile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import xtrem.download.mobile.core.model.DownloadEngine;

/*
 * The receiver for power monitoring.
 */

public class PowerReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (action == null)
            return;
        switch (action) {
            case Intent.ACTION_BATTERY_LOW:
            case Intent.ACTION_BATTERY_OKAY:
            case Intent.ACTION_POWER_CONNECTED:
            case Intent.ACTION_POWER_DISCONNECTED:
            case Intent.ACTION_BATTERY_CHANGED:
                DownloadEngine.getInstance(context).rescheduleDownloads();
                break;
        }
    }

    public static IntentFilter getFilter()
    {
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        /* About BATTERY_LOW and BATTERY_OKAY see https://code.google.com/p/android/issues/detail?id=36712 */
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);

        return filter;
    }

    public static IntentFilter getCustomFilter()
    {
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        return filter;
    }
}

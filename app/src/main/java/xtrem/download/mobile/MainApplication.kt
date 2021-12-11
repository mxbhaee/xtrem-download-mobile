package xtrem.download.mobile

import android.util.Log
import org.acra.ACRA.init
import androidx.multidex.MultiDexApplication
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.config.MailSenderConfigurationBuilder
import org.acra.config.DialogConfigurationBuilder
import xtrem.download.mobile.ui.errorreport.ErrorReportActivity
import xtrem.download.mobile.core.DownloadNotifier

class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        val builder = CoreConfigurationBuilder(this)
        builder
            .withBuildConfigClass(BuildConfig::class.java)
            .withReportFormat(StringFormat.JSON)
        builder.getPluginConfigurationBuilder(
            MailSenderConfigurationBuilder::class.java
        )
            .withMailTo("nephilim148@mail.ru")
        builder.getPluginConfigurationBuilder(DialogConfigurationBuilder::class.java)
            .withEnabled(true)
            .reportDialogClass = ErrorReportActivity::class.java
        // Set stub handler
        if (Thread.getDefaultUncaughtExceptionHandler() == null) {
            Thread.setDefaultUncaughtExceptionHandler { t: Thread, e: Throwable? ->
                Log.e(
                    TAG,
                    "Uncaught exception in " + t + ": " + Log.getStackTraceString(e)
                )
            }
        }
        init(this, builder)
        val downloadNotifier = DownloadNotifier.getInstance(this)
        downloadNotifier.makeNotifyChans()
        downloadNotifier.startUpdate()
    }

    companion object {
        val TAG = MainApplication::class.java.simpleName
    }
}
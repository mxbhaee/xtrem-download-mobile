package xtrem.download.mobile.ui.adddownload

import androidx.appcompat.app.AppCompatActivity
import xtrem.download.mobile.ui.FragmentCallback
import xtrem.download.mobile.ui.adddownload.AddDownloadDialog
import android.os.Bundle
import xtrem.download.mobile.ui.adddownload.AddDownloadActivity
import xtrem.download.mobile.ui.adddownload.AddInitParams
import android.content.Intent
import xtrem.download.mobile.core.settings.SettingsRepository
import xtrem.download.mobile.core.RepositoryHelper
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceManager
import xtrem.download.mobile.R
import xtrem.download.mobile.core.model.data.entity.DownloadInfo
import xtrem.download.mobile.core.utils.Utils
import xtrem.download.mobile.ui.FragmentCallback.ResultCode

class AddDownloadActivity : AppCompatActivity(), FragmentCallback {
    private var addDownloadDialog: AddDownloadDialog? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.getTranslucentAppTheme(applicationContext))
        super.onCreate(savedInstanceState)
        val fm = supportFragmentManager
        addDownloadDialog = fm.findFragmentByTag(TAG_DOWNLOAD_DIALOG) as AddDownloadDialog?
        if (addDownloadDialog == null) {
            var initParams: AddInitParams? = null
            val i = intent
            if (i != null) initParams = i.getParcelableExtra(TAG_INIT_PARAMS)
            if (initParams == null) {
                initParams = AddInitParams()
            }
            fillInitParams(initParams)
            addDownloadDialog = AddDownloadDialog.newInstance(initParams)
            addDownloadDialog!!.show(fm, TAG_DOWNLOAD_DIALOG)
        }
    }

    private fun fillInitParams(params: AddInitParams) {
        val pref = RepositoryHelper.getSettingsRepository(applicationContext)
        val localPref = PreferenceManager.getDefaultSharedPreferences(this)
        if (params.url == null) {
            params.url = urlFromIntent
        }
        if (params.dirPath == null) {
            params.dirPath = Uri.parse(pref.saveDownloadsIn())
        }
        if (params.retry == null) {
            params.retry = localPref.getBoolean(
                getString(R.string.add_download_retry_flag),
                true
            )
        }
        if (params.replaceFile == null) {
            params.replaceFile = localPref.getBoolean(
                getString(R.string.add_download_replace_file_flag),
                false
            )
        }
        if (params.unmeteredConnectionsOnly == null) {
            params.unmeteredConnectionsOnly = localPref.getBoolean(
                getString(R.string.add_download_unmetered_only_flag),
                false
            )
        }
        if (params.numPieces == null) {
            params.numPieces = localPref.getInt(
                getString(R.string.add_download_num_pieces),
                DownloadInfo.MIN_PIECES
            )
        }
    }

    private val urlFromIntent: String?
        get() {
            val i = intent
            return if (i != null) {
                if (i.data != null) i.data.toString() else i.getStringExtra(Intent.EXTRA_TEXT)
            } else null
        }

    override fun fragmentFinished(intent: Intent, code: ResultCode) {
        finish()
    }

    override fun onBackPressed() {
        addDownloadDialog!!.onBackPressed()
    }

    companion object {
        const val TAG_INIT_PARAMS = "init_params"
        private const val TAG_DOWNLOAD_DIALOG = "add_download_dialog"
    }
}
package xtrem.download.mobile.ui.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import xtrem.download.mobile.R
import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.takisoft.preferencex.PreferenceFragmentCompat
import xtrem.download.mobile.core.utils.Utils
import xtrem.download.mobile.ui.settings.PreferenceActivityConfig
import xtrem.download.mobile.ui.settings.sections.AppearanceSettingsFragment
import xtrem.download.mobile.ui.settings.sections.BehaviorSettingsFragment
import xtrem.download.mobile.ui.settings.sections.StorageSettingsFragment
import xtrem.download.mobile.ui.settings.sections.BrowserSettingsFragment
import xtrem.download.mobile.ui.settings.sections.LimitationsSettingsFragment

class PreferenceActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.getSettingsTheme(applicationContext))
        super.onCreate(savedInstanceState)

        /* Prevent create activity in two pane mode (after resizing window) */if (Utils.isLargeScreenDevice(
                this
            )
        ) {
            finish()
            return
        }
        setContentView(R.layout.activity_preference)
        var fragment: String? = null
        var title: String? = null
        val intent = intent
        if (intent.hasExtra(TAG_CONFIG)) {
            val config: PreferenceActivityConfig? = intent.getParcelableExtra(TAG_CONFIG)
            fragment = config!!.fragment
            title = config.title
        }
        toolbar = findViewById(R.id.toolbar)
        if (title != null) toolbar!!.title = title
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (fragment != null && savedInstanceState == null) setFragment(
            getFragment<PreferenceFragmentCompat>(
                fragment
            )
        )
    }

    private fun <F : PreferenceFragmentCompat?> setFragment(fragment: F?) {
        if (fragment == null) return
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment).commit()
    }

    private fun <F : PreferenceFragmentCompat?> getFragment(fragment: String?): F? {
        return if (fragment != null) {
            if (fragment == AppearanceSettingsFragment::class.java.simpleName) AppearanceSettingsFragment.newInstance() as F else if (fragment == BehaviorSettingsFragment::class.java.simpleName) BehaviorSettingsFragment.newInstance() as F else if (fragment == StorageSettingsFragment::class.java.simpleName) StorageSettingsFragment.newInstance() as F else if (fragment == BrowserSettingsFragment::class.java.simpleName) BrowserSettingsFragment.newInstance() as F else if (fragment == LimitationsSettingsFragment::class.java.simpleName) LimitationsSettingsFragment.newInstance() as F else null
        } else null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    companion object {
        private val TAG = PreferenceActivity::class.java.simpleName
        const val TAG_CONFIG = "config"
    }
}
package xtrem.download.mobile.ui.settings

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import xtrem.download.mobile.ui.settings.SettingsViewModel
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import xtrem.download.mobile.R
import xtrem.download.mobile.core.utils.Utils
import xtrem.download.mobile.ui.settings.SettingsActivity

class SettingsActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var detailTitle: TextView? = null
    private var viewModel: SettingsViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.getSettingsTheme(applicationContext))
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        setContentView(R.layout.activity_settings)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.title = getString(R.string.settings)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        detailTitle = findViewById(R.id.detail_title)
        viewModel!!.detailTitleChanged.observe(
            this,
            { title: String? ->
                if (title != null && detailTitle != null) detailTitle!!.text = title
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }

    companion object {
        private val TAG = SettingsActivity::class.java.simpleName
        const val TAG_OPEN_PREFERENCE = "open_preference"
        const val AppearanceSettings = "AppearanceSettingsFragment"
        const val BehaviorSettings = "BehaviorSettingsFragment"
        const val LimitationsSettings = "LimitationsSettingsFragment"
        const val StorageSettings = "StorageSettingsFragment"
        const val BrowserSettings = "BrowserSettingsFragment"
    }
}
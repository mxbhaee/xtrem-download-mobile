package xtrem.download.mobile.ui.main

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import xtrem.download.mobile.ui.main.drawer.DrawerExpandableAdapter
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.google.android.material.tabs.TabLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import xtrem.download.mobile.core.model.DownloadEngine
import xtrem.download.mobile.core.settings.SettingsRepository
import io.reactivex.disposables.CompositeDisposable
import xtrem.download.mobile.ui.BaseAlertDialog
import xtrem.download.mobile.ui.PermissionDeniedDialog
import android.os.Bundle
import xtrem.download.mobile.receiver.NotificationReceiver
import androidx.lifecycle.ViewModelProvider
import xtrem.download.mobile.R
import xtrem.download.mobile.core.RepositoryHelper
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import android.content.Intent
import xtrem.download.mobile.ui.adddownload.AddDownloadActivity
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import xtrem.download.mobile.ui.main.drawer.DrawerGroup
import xtrem.download.mobile.ui.main.drawer.DrawerGroupItem
import androidx.core.view.GravityCompat
import android.app.SearchManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import xtrem.download.mobile.ui.settings.SettingsActivity
import xtrem.download.mobile.ui.browser.BrowserActivity
import android.widget.TextView
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.nambimobile.widgets.efab.ExpandableFab
import com.nambimobile.widgets.efab.ExpandableFabLayout
import xtrem.download.mobile.core.utils.Utils
import xtrem.download.mobile.service.DownloadService

open class MainActivity : AppCompatActivity() {
    /* Android data binding doesn't work with layout aliases */
    private var coordinatorLayout: CoordinatorLayout? = null
    private var toolbar: Toolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var toggle: ActionBarDrawerToggle? = null
    private var drawerItemsList: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private var drawerAdapter: DrawerExpandableAdapter? = null
    private var wrappedDrawerAdapter: RecyclerView.Adapter<*>? = null
    private var drawerItemManager: RecyclerViewExpandableItemManager? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null
    private var pagerAdapter: DownloadListPagerAdapter? = null
    private var fragmentViewModel: DownloadsViewModel? = null
    private var fab: FloatingActionButton? = null

    private var fabsmenu: FloatingActionButton? = null
    private var searchView: SearchView? = null
    private var engine: DownloadEngine? = null
    private var pref: SettingsRepository? = null
    protected var disposables = CompositeDisposable()
    private var dialogViewModel: BaseAlertDialog.SharedViewModel? = null
    private var aboutDialog: BaseAlertDialog? = null
    private var permDeniedDialog: PermissionDeniedDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.getAppTheme(applicationContext))
        super.onCreate(savedInstanceState)
        if (intent.action != null && intent.action == NotificationReceiver.NOTIFY_ACTION_SHUTDOWN_APP) {
            finish()
            return
        }
        val provider = ViewModelProvider(this)
        fragmentViewModel = provider.get(DownloadsViewModel::class.java)
        dialogViewModel = provider.get(BaseAlertDialog.SharedViewModel::class.java)
        val fm = supportFragmentManager
        aboutDialog = fm.findFragmentByTag(TAG_ABOUT_DIALOG) as BaseAlertDialog?
        permDeniedDialog = fm.findFragmentByTag(TAG_PERM_DENIED_DIALOG) as PermissionDeniedDialog?
        if (!Utils.checkStoragePermission(this) && permDeniedDialog == null) {
            storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        setContentView(R.layout.activity_main)
        pref = RepositoryHelper.getSettingsRepository(applicationContext)
        Utils.disableBrowserFromSystem(applicationContext, pref!!.browserDisableFromSystem())
        Utils.enableBrowserLauncherIcon(applicationContext, pref!!.browserLauncherIcon())
        engine = DownloadEngine.getInstance(applicationContext)
        initLayout()
        engine!!.restoreDownloads()
    }

    private val storagePermission = registerForActivityResult(
        RequestPermission()
    ) { isGranted: Boolean? ->
        if (!isGranted!! && Utils.shouldRequestStoragePermission(this)) {
            val fm = supportFragmentManager
            if (fm.findFragmentByTag(TAG_PERM_DENIED_DIALOG) == null) {
                permDeniedDialog = PermissionDeniedDialog.newInstance()
                val ft = fm.beginTransaction()
                ft.add(permDeniedDialog!!, TAG_PERM_DENIED_DIALOG)
                ft.commitAllowingStateLoss()
            }
        }
    }

    private fun initLayout() {
        toolbar = findViewById(R.id.toolbar)
        coordinatorLayout = findViewById(R.id.coordinator)
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        tabLayout = findViewById(R.id.download_list_tabs)
        viewPager = findViewById(R.id.download_list_viewpager)
        fab = findViewById(R.id.add_fab)
        fabsmenu = findViewById(R.id.add_fabs)
        drawerItemsList = findViewById(R.id.drawer_items_list)
        layoutManager = LinearLayoutManager(this)
        toolbar!!.setTitle(R.string.app_name)
        /* Disable elevation for portrait mode */
        if (!Utils.isTwoPane(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) toolbar!!.elevation =
            0f
        setSupportActionBar(toolbar)
        /**if (drawerLayout != null) {
            toggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_navigation_drawer,
                R.string.close_navigation_drawer
            )
            drawerLayout!!.addDrawerListener(toggle!!)
        } **/
        initDrawer()
        fragmentViewModel!!.resetSearch()
        pagerAdapter = DownloadListPagerAdapter(this)
        viewPager!!.adapter = pagerAdapter
        viewPager!!.offscreenPageLimit = DownloadListPagerAdapter.NUM_FRAGMENTS
        TabLayoutMediator(tabLayout!!, viewPager!!,
            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                when (position) {
                    DownloadListPagerAdapter.QUEUED_FRAG_POS -> tab.text = "DOWNLOAD"
                    DownloadListPagerAdapter.COMPLETED_FRAG_POS -> tab.setText(R.string.fragment_title_completed)
                    DownloadListPagerAdapter.OPTIONS_FRAG_POS -> tab.text = "ABOUT US"
                }
            }
        ).attach()
        fab!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    AddDownloadActivity::class.java
                )
            )
        })
    }

    private fun initDrawer() {
        drawerItemManager = RecyclerViewExpandableItemManager(null)
        drawerItemManager!!.defaultGroupsExpandedState = false
        drawerItemManager!!.setOnGroupCollapseListener { groupPosition: Int, fromUser: Boolean, payload: Any? ->
            if (fromUser) saveGroupExpandState(
                groupPosition,
                false
            )
        }
        drawerItemManager!!.setOnGroupExpandListener { groupPosition: Int, fromUser: Boolean, payload: Any? ->
            if (fromUser) saveGroupExpandState(
                groupPosition,
                true
            )
        }
        val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
        /*
         * Change animations are enabled by default since support-v7-recyclerview v22.
         * Need to disable them when using animation indicator.
         */animator.supportsChangeAnimations = false
        val groups = Utils.getNavigationDrawerItems(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        drawerAdapter = DrawerExpandableAdapter(
            groups,
            drawerItemManager!!
        ) { group: DrawerGroup, item: DrawerGroupItem -> onDrawerItemSelected(group, item) }
        wrappedDrawerAdapter = drawerItemManager!!.createWrappedAdapter(drawerAdapter!!)
        onDrawerGroupsCreated()
        drawerItemsList!!.layoutManager = layoutManager
        drawerItemsList!!.adapter = wrappedDrawerAdapter
        drawerItemsList!!.itemAnimator = animator
        drawerItemsList!!.setHasFixedSize(false)
        drawerItemManager!!.attachRecyclerView(drawerItemsList!!)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (toggle != null) toggle!!.syncState()
    }

    public override fun onStart() {
        super.onStart()
        subscribeAlertDialog()
        subscribeSettingsChanged()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    private fun subscribeAlertDialog() {
        val d = dialogViewModel!!.observeEvents()
            .subscribe { event: BaseAlertDialog.Event ->
                if (event.dialogTag == null) {
                    return@subscribe
                }
                if (event.dialogTag == TAG_ABOUT_DIALOG) {
                    when (event.type) {
                        BaseAlertDialog.EventType.NEGATIVE_BUTTON_CLICKED -> openChangelogLink()
                        BaseAlertDialog.EventType.DIALOG_SHOWN -> initAboutDialog()
                        else -> {
                            //TODO
                        }
                    }
                } else if (event.dialogTag == TAG_PERM_DENIED_DIALOG) {
                    if (event.type != BaseAlertDialog.EventType.DIALOG_SHOWN) {
                        permDeniedDialog!!.dismiss()
                    }
                    if (event.type == BaseAlertDialog.EventType.NEGATIVE_BUTTON_CLICKED) {
                        storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
        disposables.add(d)
    }

    private fun subscribeSettingsChanged() {
        invalidateOptionsMenu()
        disposables.add(
            pref!!.observeSettingsChanged()
                .subscribe { key: String ->
                    if (key == getString(R.string.pref_key_browser_hide_menu_icon)) {
                        invalidateOptionsMenu()
                    }
                })
    }

    private fun onDrawerGroupsCreated() {
        for (pos in 0 until drawerAdapter!!.groupCount) {
            val group = drawerAdapter!!.getGroup(pos) ?: return
            val res = resources
            when (group.id) {
                res.getInteger(R.integer.drawer_category_id).toLong() -> {
                    fragmentViewModel!!.setCategoryFilter(
                        Utils.getDrawerGroupCategoryFilter(this, group.selectedItemId), false
                    )
                }
                res.getInteger(R.integer.drawer_status_id).toLong() -> {
                    fragmentViewModel!!.setStatusFilter(
                        Utils.getDrawerGroupStatusFilter(this, group.selectedItemId), false
                    )
                }
                res.getInteger(R.integer.drawer_date_added_id).toLong() -> {
                    fragmentViewModel!!.setDateAddedFilter(
                        Utils.getDrawerGroupDateAddedFilter(this, group.selectedItemId), false
                    )
                }
                res.getInteger(R.integer.drawer_sorting_id).toLong() -> {
                    fragmentViewModel!!.setSort(
                        Utils.getDrawerGroupItemSorting(
                            this,
                            group.selectedItemId
                        ), false
                    )
                }
            }
            applyExpandState(group, pos)
        }
    }

    private fun applyExpandState(group: DrawerGroup, pos: Int) {
        if (group.defaultExpandState) drawerItemManager!!.expandGroup(pos) else drawerItemManager!!.collapseGroup(
            pos
        )
    }

    private fun saveGroupExpandState(groupPosition: Int, expanded: Boolean) {
        val group = drawerAdapter!!.getGroup(groupPosition) ?: return
        val res = resources
        var prefKey: String? = null
        when (group.id) {
            res.getInteger(R.integer.drawer_category_id).toLong() -> prefKey =
                getString(R.string.drawer_category_is_expanded)
            res.getInteger(R.integer.drawer_status_id)
                .toLong() -> prefKey =
                getString(R.string.drawer_status_is_expanded)
            res.getInteger(R.integer.drawer_date_added_id)
                .toLong() -> prefKey = getString(R.string.drawer_time_is_expanded)
            res.getInteger(
                R.integer.drawer_sorting_id
            ).toLong() -> prefKey = getString(R.string.drawer_sorting_is_expanded)
        }
        if (prefKey != null) PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(prefKey, expanded)
            .apply()
    }

    private fun onDrawerItemSelected(group: DrawerGroup, item: DrawerGroupItem) {
        val res = resources
        var prefKey: String? = null
        when (group.id) {
            res.getInteger(R.integer.drawer_category_id).toLong() -> {
                prefKey = getString(R.string.drawer_category_selected_item)
                fragmentViewModel!!.setCategoryFilter(
                    Utils.getDrawerGroupCategoryFilter(this, item.id), true
                )
            }
            res.getInteger(R.integer.drawer_status_id).toLong() -> {
                prefKey = getString(R.string.drawer_status_selected_item)
                fragmentViewModel!!.setStatusFilter(
                    Utils.getDrawerGroupStatusFilter(this, item.id), true
                )
            }
            res.getInteger(R.integer.drawer_date_added_id).toLong() -> {
                prefKey = getString(R.string.drawer_time_selected_item)
                fragmentViewModel!!.setDateAddedFilter(
                    Utils.getDrawerGroupDateAddedFilter(this, item.id), true
                )
            }
            res.getInteger(R.integer.drawer_sorting_id).toLong() -> {
                prefKey = getString(R.string.drawer_sorting_selected_item)
                fragmentViewModel!!.setSort(Utils.getDrawerGroupItemSorting(this, item.id), true)
            }
        }
        prefKey?.let { saveSelectionState(it, item) }
        if (drawerLayout != null) drawerLayout!!.closeDrawer(GravityCompat.START)
    }

    private fun saveSelectionState(prefKey: String, item: DrawerGroupItem) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putLong(prefKey, item.id)
            .apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        searchView = menu.findItem(R.id.search).actionView as SearchView
        initSearch()
        return true
    }

    private fun initSearch() {
        searchView!!.maxWidth = Int.MAX_VALUE
        searchView!!.setOnCloseListener {
            fragmentViewModel!!.resetSearch()
            false
        }
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                fragmentViewModel!!.setSearchQuery(query)
                /* Submit the search will hide the keyboard */
                searchView!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                fragmentViewModel!!.setSearchQuery(newText)
                return true
            }
        })
        searchView!!.queryHint = getString(R.string.search)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        /* Assumes current activity is the searchable activity */
        searchView!!.setSearchableInfo(
            searchManager.getSearchableInfo(
                componentName
            )
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.browser_menu).isVisible = !pref!!.browserHideMenuIcon()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.pause_all_menu -> {
                pauseAll()
            }
            R.id.resume_all_menu -> {
                resumeAll()
            }
            R.id.settings_menu -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.shutdown_app_menu -> {
                closeOptionsMenu()
                shutdown()
            }
            R.id.browser_menu -> {
                startActivity(Intent(this, BrowserActivity::class.java))
            }
        }
        return true
    }

    private fun pauseAll() {
        engine!!.pauseAllDownloads()
    }

    private fun resumeAll() {
        engine!!.resumeDownloads(false)
    }

    private fun showAboutDialog() {
        val fm = supportFragmentManager
        if (fm.findFragmentByTag(TAG_ABOUT_DIALOG) == null) {
            aboutDialog = BaseAlertDialog.newInstance(
                getString(R.string.about_title),
                null,
                R.layout.dialog_about,
                getString(R.string.ok),
                getString(R.string.about_changelog),
                null,
                true
            )
            aboutDialog!!.show(fm, TAG_ABOUT_DIALOG)
        }
    }

    private fun initAboutDialog() {
        if (aboutDialog == null) return
        val dialog = aboutDialog!!.dialog
        if (dialog != null) {
            val versionTextView = dialog.findViewById<TextView>(R.id.about_version)
            val descriptionTextView = dialog.findViewById<TextView>(R.id.about_description)
            val versionName = Utils.getAppVersionName(this)
            if (versionName != null) versionTextView.text = versionName
            //descriptionTextView.text = Html.fromHtml(getString(R.string.about_description))
            //descriptionTextView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun openChangelogLink() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(getString(R.string.about_changelog_link))
        startActivity(i)
    }

    private fun shutdown() {
        val i = Intent(applicationContext, DownloadService::class.java)
        i.action = DownloadService.ACTION_SHUTDOWN
        startService(i)
        finish()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val TAG_ABOUT_DIALOG = "about_dialog"
        private const val TAG_PERM_DENIED_DIALOG = "perm_denied_dialog"
    }
}
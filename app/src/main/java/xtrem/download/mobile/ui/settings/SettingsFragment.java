package xtrem.download.mobile.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import xtrem.download.mobile.R;
import xtrem.download.mobile.core.utils.Utils;
import xtrem.download.mobile.ui.settings.sections.AppearanceSettingsFragment;
import xtrem.download.mobile.ui.settings.sections.BehaviorSettingsFragment;
import xtrem.download.mobile.ui.settings.sections.BrowserSettingsFragment;
import xtrem.download.mobile.ui.settings.sections.LimitationsSettingsFragment;
import xtrem.download.mobile.ui.settings.sections.StorageSettingsFragment;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import static xtrem.download.mobile.ui.settings.SettingsActivity.AppearanceSettings;
import static xtrem.download.mobile.ui.settings.SettingsActivity.BehaviorSettings;
import static xtrem.download.mobile.ui.settings.SettingsActivity.BrowserSettings;
import static xtrem.download.mobile.ui.settings.SettingsActivity.LimitationsSettings;
import static xtrem.download.mobile.ui.settings.SettingsActivity.StorageSettings;

public class SettingsFragment extends PreferenceFragmentCompat
{
    @SuppressWarnings("unused")
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private AppCompatActivity activity;
    private SettingsViewModel viewModel;

    public static SettingsFragment newInstance()
    {
        SettingsFragment fragment = new SettingsFragment();

        fragment.setArguments(new Bundle());

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        if (context instanceof AppCompatActivity)
            activity = (AppCompatActivity)context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (activity == null)
            activity = (AppCompatActivity)getActivity();

        viewModel = new ViewModelProvider(activity).get(SettingsViewModel.class);

        String preference = activity.getIntent().getStringExtra(SettingsActivity.TAG_OPEN_PREFERENCE);
        if (preference != null) {
            openPreference(preference);
            if (!Utils.isLargeScreenDevice(activity))
                activity.finish();

        } else if (Utils.isTwoPane(activity)) {
            Fragment f = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.detail_fragment_container);
            if (f == null)
                setFragment(AppearanceSettingsFragment.newInstance(),
                        getString(R.string.pref_header_appearance));
        }

        Preference appearance = findPreference(AppearanceSettingsFragment.class.getSimpleName());
        appearance.setOnPreferenceClickListener(prefClickListener);

        Preference behavior = findPreference(BehaviorSettingsFragment.class.getSimpleName());
        behavior.setOnPreferenceClickListener(prefClickListener);

        Preference storage = findPreference(StorageSettingsFragment.class.getSimpleName());
        storage.setOnPreferenceClickListener(prefClickListener);

        Preference browser = findPreference(BrowserSettingsFragment.class.getSimpleName());
        browser.setOnPreferenceClickListener(prefClickListener);

        Preference limitations = findPreference(LimitationsSettingsFragment.class.getSimpleName());
        limitations.setOnPreferenceClickListener(prefClickListener);
    }

    private Preference.OnPreferenceClickListener prefClickListener = (preference) -> {
        openPreference(preference.getKey());
        return true;
    };

    private void openPreference(String prefName)
    {
        switch (prefName) {
            case AppearanceSettings:
                if (Utils.isLargeScreenDevice(activity)) {
                    setFragment(AppearanceSettingsFragment.newInstance(),
                            getString(R.string.pref_header_appearance));
                } else {
                    startActivity(AppearanceSettingsFragment.class,
                            getString(R.string.pref_header_appearance));
                }
                break;
            case BehaviorSettings:
                if (Utils.isLargeScreenDevice(activity)) {
                    setFragment(BehaviorSettingsFragment.newInstance(),
                            getString(R.string.pref_header_behavior));
                } else {
                    startActivity(BehaviorSettingsFragment.class,
                            getString(R.string.pref_header_behavior));
                }
                break;
            case StorageSettings:
                if (Utils.isLargeScreenDevice(activity)) {
                    setFragment(StorageSettingsFragment.newInstance(),
                            getString(R.string.pref_header_storage));
                } else {
                    startActivity(StorageSettingsFragment.class,
                            getString(R.string.pref_header_storage));
                }
                break;
            case BrowserSettings:
                if (Utils.isLargeScreenDevice(activity)) {
                    setFragment(BrowserSettingsFragment.newInstance(),
                            getString(R.string.pref_header_browser));
                } else {
                    startActivity(BrowserSettingsFragment.class,
                            getString(R.string.pref_header_browser));
                }
                break;
            case LimitationsSettings:
                if (Utils.isLargeScreenDevice(activity)) {
                    setFragment(LimitationsSettingsFragment.newInstance(),
                            getString(R.string.pref_header_limitations));
                } else {
                    startActivity(LimitationsSettingsFragment.class,
                            getString(R.string.pref_header_limitations));
                }
                break;
        }
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.pref_headers, rootKey);
    }

    private <F extends PreferenceFragmentCompat> void setFragment(F fragment, String title)
    {
        viewModel.detailTitleChanged.setValue(title);

        if (Utils.isLargeScreenDevice(activity)) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    private <F extends PreferenceFragmentCompat> void startActivity(Class<F> fragment, String title)
    {
        Intent i = new Intent(activity, PreferenceActivity.class);
        PreferenceActivityConfig config = new PreferenceActivityConfig(
                fragment.getSimpleName(),
                title);

        i.putExtra(PreferenceActivity.TAG_CONFIG, config);
        startActivity(i);
    }
}

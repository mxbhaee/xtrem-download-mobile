/*
 * Copyright (C) 2021 Tachibana General Laboratories, LLC
 * Copyright (C) 2021 Yaroslav Pronin <proninyaroslav@mail.ru>
 *
 * This file is part of Download Navi.
 *
 * Download Navi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Download Navi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Download Navi.  If not, see <http://www.gnu.org/licenses/>.
 */

package xtrem.download.mobile.ui.settings.sections;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;

import androidx.preference.Preference;

import xtrem.download.mobile.R;
import xtrem.download.mobile.core.InputFilterMinMax;
import xtrem.download.mobile.core.RepositoryHelper;
import xtrem.download.mobile.core.settings.SettingsRepository;
import com.takisoft.preferencex.EditTextPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;

public class LimitationsSettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener
{
    @SuppressWarnings("unused")
    private static final String TAG = LimitationsSettingsFragment.class.getSimpleName();

    private SettingsRepository pref;

    public static LimitationsSettingsFragment newInstance()
    {
        LimitationsSettingsFragment fragment = new LimitationsSettingsFragment();
        fragment.setArguments(new Bundle());

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        pref = RepositoryHelper.getSettingsRepository(getActivity().getApplicationContext());

        String keyMaxActiveDownloads = getString(R.string.pref_key_max_active_downloads);
        EditTextPreference maxActiveDownloads = findPreference(keyMaxActiveDownloads);
        if (maxActiveDownloads != null) {
            String value = Integer.toString(pref.maxActiveDownloads());
            maxActiveDownloads.setOnBindEditTextListener((editText) ->
                    editText.setFilters(new InputFilter[]{new InputFilterMinMax(1, Integer.MAX_VALUE)}));
            maxActiveDownloads.setSummary(value);
            maxActiveDownloads.setText(value);
            bindOnPreferenceChangeListener(maxActiveDownloads);
        }

        String keyMaxDownloadRetries = getString(R.string.pref_key_max_download_retries);
        EditTextPreference maxDownloadRetries = findPreference(keyMaxDownloadRetries);
        if (maxDownloadRetries != null) {
            String value = Integer.toString(pref.maxDownloadRetries());
            maxDownloadRetries.setOnBindEditTextListener((editText) ->
                    editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, Integer.MAX_VALUE)}));
            maxDownloadRetries.setSummary(value);
            maxDownloadRetries.setText(value);
            maxDownloadRetries.setDialogMessage(R.string.pref_max_download_retries_dialog_msg);
            bindOnPreferenceChangeListener(maxDownloadRetries);
        }

        String keySpeedLimit = getString(R.string.pref_key_speed_limit);
        EditTextPreference speedLimit = findPreference(keySpeedLimit);
        if (speedLimit != null) {
            String value = Long.toString(pref.speedLimit());
            speedLimit.setOnBindEditTextListener((editText) ->
                    editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, Integer.MAX_VALUE)})
            );
            speedLimit.setSummary(value);
            speedLimit.setText(value);
            speedLimit.setDialogMessage(R.string.pref_speed_limit_dialog_hint);
            bindOnPreferenceChangeListener(speedLimit);
        }
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.pref_limitations, rootKey);
    }

    private void bindOnPreferenceChangeListener(Preference preference)
    {
        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue)
    {
        if (preference.getKey().equals(getString(R.string.pref_key_max_active_downloads))) {
            int value = 1;
            if (!TextUtils.isEmpty((String)newValue))
                value = Integer.parseInt((String)newValue);
            pref.maxActiveDownloads(value);
            preference.setSummary(Integer.toString(value));

        } else if (preference.getKey().equals(getString(R.string.pref_key_max_download_retries))) {
            int value = 0;
            if (!TextUtils.isEmpty((String)newValue))
                value = Integer.parseInt((String)newValue);
            pref.maxDownloadRetries(value);
            preference.setSummary(Integer.toString(value));

        } else if (preference.getKey().equals(getString(R.string.pref_key_speed_limit))) {
            int value = 0;
            if (!TextUtils.isEmpty((String)newValue))
                value = Integer.parseInt((String)newValue);
            pref.speedLimit(value);
            preference.setSummary(Integer.toString(value));
        }

        return true;
    }
}

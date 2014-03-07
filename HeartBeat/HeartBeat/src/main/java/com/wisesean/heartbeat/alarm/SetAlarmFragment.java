package com.wisesean.heartbeat.alarm;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SetAlarmFragment extends PreferenceFragment {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.alarm_prefs);
    }
}

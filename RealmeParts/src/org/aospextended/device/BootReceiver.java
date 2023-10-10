/*
 * Copyright (C) 2020 The AospExtended Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aospextended.device;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.aospextended.device.RealmeParts;
import org.aospextended.device.gestures.TouchGestures;
import org.aospextended.device.util.Utils;
import org.aospextended.device.doze.DozeUtils;
import org.aospextended.device.vibration.VibratorStrengthPreference;
import org.aospextended.device.camerahelper.CameraService;
import org.aospextended.device.utils.FileUtils;

public class BootReceiver extends BroadcastReceiver {

    private static final String DC_DIMMING_ENABLE_KEY = "dc_dimming_enable";
    private static final String DC_DIMMING_NODE = "/sys/kernel/oppo_display/dimlayer_bl_en";
    private static final String HBM_ENABLE_KEY = "hbm_mode";
    private static final String HBM_NODE = "/sys/kernel/oppo_display/hbm";
    private static final String PREF_OTG = "otg";
    private static final String OTG_PATH = "/sys/class/power_supply/usb/otg_switch";
    private static final String GAMESWITCH_ENABLE_KEY = "game_switch";
    private static final String GAMESWITCH_NODE = "/proc/touchpanel/game_switch_enable";
    private static final String FASTCHARGE_ENABLE_KEY = "fast_charge";
    private static final String FASTCHARGE_NODE = "/sys/class/qcom-battery/restrict_chg";


    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            enableComponent(context, TouchGestures.class.getName());
            SharedPreferences prefs = Utils.getSharedPreferences(context);
            TouchGestures.enableGestures(prefs.getBoolean(
                TouchGestures.PREF_GESTURE_ENABLE, false));
            TouchGestures.enableDt2w(prefs.getBoolean(
                TouchGestures.PREF_DT2W_ENABLE, true));
        }
        DozeUtils.checkDozeService(context);
        String prj = Utils.getFileValue("/proc/oplusVersion/prjName", "");
        if ("206B1".equals(prj)) {
            context.startService(new Intent(context, CameraService.class));
        }
//        VibratorStrengthPreference.restore(context);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dcDimmingEnabled = sharedPrefs.getBoolean(DC_DIMMING_ENABLE_KEY, false);
        try {
            FileUtils.writeLine(DC_DIMMING_NODE, dcDimmingEnabled ? "1" : "0");
        } catch(Exception e) {}
        boolean hbmEnabled = sharedPrefs.getBoolean(HBM_ENABLE_KEY, false);
        try {
            FileUtils.writeLine(HBM_NODE, hbmEnabled ? "1" : "0");
        } catch(Exception e) {}
        boolean OTGEnabled = sharedPrefs.getBoolean(PREF_OTG, false);
        try {
            FileUtils.writeLine(OTG_PATH, OTGEnabled ? "1" : "0");
        } catch(Exception e) {}
        boolean GameSwitchEnabled = sharedPrefs.getBoolean(GAMESWITCH_ENABLE_KEY, false);
        try {
            FileUtils.writeLine(GAMESWITCH_NODE, GameSwitchEnabled ? "1" : "0");
        } catch(Exception e) {}
        boolean FastChargeEnabled = sharedPrefs.getBoolean(FASTCHARGE_ENABLE_KEY, false);
        try {
            FileUtils.writeLine(FASTCHARGE_NODE, FastChargeEnabled ? "1" : "0");
        } catch(Exception e) {}
    }

    private void enableComponent(Context context, String component) {
        ComponentName name = new ComponentName(context, component);
        PackageManager pm = context.getPackageManager();
        if (pm.getComponentEnabledSetting(name)
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(name,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}

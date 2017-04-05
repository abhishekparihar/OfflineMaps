package com.abhi.offlinemaps.sdktools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Defines a BroadcastReceiver that listens for the alarm manager that sends the
 * broadcast hourly in order to recalculate the sunrise / sunset hours
 */
public class SKToolsCalculateSunriseSunsetTimeAutoReceiver extends BroadcastReceiver {

    private static final String TAG = "CSrSstTimeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Received Broadcast from alarm manager to recalculate the sunrise / sunset hours");

        if (SKToolsLogicManager.lastUserPosition != null && !SKToolsLogicManager.getInstance().isNavigationStopped()) {
            SKToolsSunriseSunsetCalculator.calculateSunriseSunsetHours(SKToolsLogicManager.lastUserPosition.getCoordinate(),
                    SKToolsSunriseSunsetCalculator.OFFICIAL);

            SKToolsAutoNightManager.getInstance().setAlarmForDayNightModeWithSunriseSunset(SKToolsLogicManager
                    .getInstance().getCurrentActivity());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SKToolsAutoNightManager.getInstance().setAlarmForHourlyNotificationAfterKitKat(context, false);
        }

    }
}


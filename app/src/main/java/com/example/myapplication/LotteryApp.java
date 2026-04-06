package com.example.myapplication;

import android.app.Application;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 *
 * Schedules LotteryDrawWorker as a PeriodicWorkRequest with KEEP policy so it is
 * only ever enqueued once, no matter how many times the app is opened or the device
 * is rebooted. WorkManager persists the schedule across restarts automatically.
 *
 * 15 minutes is the minimum interval allowed by WorkManager for periodic work.
 * In practice Android may run it less frequently based on battery/Doze state,
 * which is why WaitingListActivity also has a lazy check as a safety net.
 */
public class LotteryApp extends Application {

    private static final String TAG = "LotteryApp";
    private static final String LOTTERY_WORK_NAME = "lottery_scanner";

    @Override
    public void onCreate() {
        super.onCreate();

        PeriodicWorkRequest lotteryScanner = new PeriodicWorkRequest.Builder(
                LotteryDrawWorker.class,
                15, TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                LOTTERY_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                lotteryScanner
        );

        Log.d(TAG, "Periodic lottery scanner scheduled.");
    }
}
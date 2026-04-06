package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.CountDownLatch;

/**
 *
 * Scheduled once at app startup (via LotteryApp) as a PeriodicWorkRequest running
 * approximately every 15 minutes. Each run calls LotteryDrawHelper.scanAndDrawAll(),
 * which queries Firestore for events whose registration deadline has passed and whose
 * draw has not yet been completed.
 *
 * This worker requires ZERO changes to event creation code - it finds all events
 * that need drawing on its own, regardless of when or how they were created.
 *
 * Returns Result.success() even if individual draws fail (so the periodic schedule
 * keeps running). Individual draw errors are logged inside LotteryDrawHelper.
 */
public class LotteryDrawWorker extends Worker {

    private static final String TAG = "LotteryDrawWorker";

    public LotteryDrawWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Periodic scan started.");

        // LotteryDrawHelper.scanAndDrawAll() is async.
        // Use a CountDownLatch to block until the scan query completes.
        CountDownLatch latch = new CountDownLatch(1);

        LotteryDrawHelper.scanAndDrawAll(new LotteryDrawHelper.OnDrawCompleteListener() {
            @Override
            public void onSuccess(int processedCount) {
                Log.d(TAG, "Scan complete. Events checked: " + processedCount);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Scan query failed", e);
                latch.countDown(); // release even on failure so the worker doesn't hang
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Worker interrupted", e);
        }

        // Always return success so the periodic work keeps running on schedule.
        return Result.success();
    }
}
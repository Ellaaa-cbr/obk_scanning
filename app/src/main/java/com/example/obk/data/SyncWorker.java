package com.example.obk.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class SyncWorker extends Worker {

    public static void enqueue(Context ctx) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(ctx).enqueue(work);
    }

    private final AppRepository repository;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        repository = AppRepository.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        repository.syncUnsyncedLogs();
        return Result.success();
    }
}

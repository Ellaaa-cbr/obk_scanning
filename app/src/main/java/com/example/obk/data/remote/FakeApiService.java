package com.example.obk.data.remote;

import android.util.Log;


import com.example.obk.data.local.entity.Charity;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Offline / demo implementation – simulates network latency & success/failure.
 */
public class FakeApiService {

    private static final String TAG = "FakeApi";
    private static final Random RANDOM = new Random();

    public static List<Charity> getCharities() {
        // simulate latency
        sleep(500);
        return Arrays.asList(
                new Charity("ORG1", "ORG1_name"),
                new Charity("ORG2", "ORG2_name"),
                new Charity("ORG3", "ORG3_name")
        );
    }

    /**
     * Returns true if "network" accepts submission; ~80% success rate.
     */
    public static boolean submitCheckout(String charityId, List<String> toteIds, long timestamp, String photoPath) {
        sleep(700);
        boolean success = RANDOM.nextFloat() < 0.8f;
        Log.d(TAG, "submitCheckout (fake): success=" + success + " totes=" + toteIds);
        return success;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
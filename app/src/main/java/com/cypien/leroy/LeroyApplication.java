package com.cypien.leroy;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.cypien.leroy.utils.WebServiceConnector;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.iainconnor.objectcache.CacheManager;
import com.iainconnor.objectcache.DiskCache;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;


/**
 * Created by Alex on 9/24/2015.
 */
public class LeroyApplication extends Application {
    public static boolean shownDialog = false;
    public static final float BYTES_IN_MB = 1024.0f * 1024.0f;
    private static LeroyApplication singleton;
    private static DiskCache diskCache;
    // public final String APIVersion="v1";
    public final String APIVersion = "v2";
    private Tracker mTracker;

    public static LeroyApplication getInstance() {
        return singleton;
    }

    public static CacheManager getCacheManager() {
        return CacheManager.getInstance(diskCache);
    }

    public static float megabytesFree() {
        final Runtime rt = Runtime.getRuntime();
        final float bytesUsed = rt.totalMemory();
        final float mbUsed = bytesUsed / BYTES_IN_MB;
        return megabytesAvailable() - mbUsed;
    }

    public static float megabytesAvailable() {
        final Runtime rt = Runtime.getRuntime();
        final float bytesAvailable = rt.maxMemory();
        return bytesAvailable / BYTES_IN_MB;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        String cachePath = getCacheDir().getPath();
        File cacheFile = new File(cachePath + File.separator + "com.cypien.leroy");
        try {
            diskCache = new DiskCache(cacheFile, BuildConfig.VERSION_CODE, 1024 * 1024 * 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        singleton = this;

    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    public JSONObject makeRequest(String... params) {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.addAll(Arrays.asList(params).subList(2, params.length));
        JSONObject request = new JSONObject();

        try {
            request.put("method", params[0]);
            request.put("params", new JSONArray(parameters));
            return new JSONObject(new WebServiceConnector().execute("http://www.facem-facem.ro/customAPI/privateEndpoint/" + APIVersion, params[1], "q=" + request.toString()).get());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            Log.e("eroare app", e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject makePublicRequest(String... params) {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.addAll(Arrays.asList(params).subList(1, params.length));
        JSONObject request = new JSONObject();
        try {
            request.put("method", params[0]);
            request.put("params", new JSONArray(parameters));

            return new JSONObject(new WebServiceConnector().execute("http://www.facem-facem.ro/customAPI/publicEndpoint/" + APIVersion, "q=" + request.toString()).get());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

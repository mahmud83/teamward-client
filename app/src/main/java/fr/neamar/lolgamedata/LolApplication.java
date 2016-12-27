package fr.neamar.lolgamedata;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Map;

import fr.neamar.lolgamedata.pojo.Account;
import fr.neamar.lolgamedata.service.RegistrationIntentService;

public class LolApplication extends Application {
    private static final String TAG = "LolApplication";

    private MixpanelAPI mixpanel = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();


        ImageLoader.getInstance().init(config);

        if (BuildConfig.DEBUG) {
            /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
                    */
        }

        // Register for push notifications, send token again in case it changed
        Intent intent = new Intent(this, RegistrationIntentService.class);
        Log.i(TAG, "Starting Service");
        startService(intent);


        // Tracking initialization
        final Runnable r = new Runnable() {
            public void run() {
                identifyOnMixpanel();
            }
        };

        Handler handler = new Handler();
        handler.post(r);


    }

    public MixpanelAPI getMixpanel() {
        if (mixpanel == null) {
            mixpanel = MixpanelAPI.getInstance(this, getString(R.string.MIXPANEL_TOKEN));
            mixpanel.getPeople().identify(mixpanel.getDistinctId());
        }

        return mixpanel;
    }

    public JSONArray getJSONArrayFromSingleItem(String item) throws JSONException {
        JSONArray a = new JSONArray();
        a.put(item);
        return a;
    }

    public void identifyOnMixpanel() {
        AccountManager accountManager = new AccountManager(getApplicationContext());
        List<Account> accounts = accountManager.getAccounts();
        Log.i(TAG, "Current size for accounts is " + accounts.size());

        if (!accountManager.getAccounts().isEmpty()) {
            Log.i(TAG, "Identifying as " + accounts.get(0).summonerName);
            getMixpanel().getPeople().set("accounts_length", accounts.size());
            getMixpanel().getPeople().set("$username", accounts.get(0).summonerName);
            getMixpanel().getPeople().set("$name", accounts.get(0).summonerName);
            getMixpanel().getPeople().set("region", accounts.get(0).region);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            Map<String, ?> properties = sp.getAll();
            for (Map.Entry<String, ?> entry : properties.entrySet()) {
                getMixpanel().getPeople().set("settings_" + entry.getKey(), entry.getValue());
            }
        }
    }

    public String getApiUrl() {
        return getString(R.string.API_URL);
    }
}
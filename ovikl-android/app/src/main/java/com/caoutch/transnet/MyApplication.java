package com.caoutch.transnet;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application {

    private static Context mContext;

    public static TCPService.TcpClient tcpClient;
    public void onCreate() {
        Log.i("MyApplication","onCreate");
        if (BuildConfig.DEBUG) {
            Constants.url=Constants.dev_url;
            Constants.nodejs_index_url=Constants.dev_nodejs_index_url;
        }
        Log.i("Constants.url",Constants.url);
        Log.i("Constants.nodejs_index_url",Constants.nodejs_index_url);

        super.onCreate();
        mContext = getApplicationContext();

    }

    public static Context getAppContext() {
        return mContext;
    }


}

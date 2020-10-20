package com.caoutch.transnet.activity;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.caoutch.transnet.R;
import com.caoutch.transnet.view.LoadingRelativeLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.util.Arrays;
import java.util.List;

public class SuperActivity extends AppCompatActivity {
    String tag;
    LoadingRelativeLayout loadingRelativeLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag,"onCreate");

    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(tag,"onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(tag,"onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(tag,"onDestroy");

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i(tag,"onPause");
    }

}

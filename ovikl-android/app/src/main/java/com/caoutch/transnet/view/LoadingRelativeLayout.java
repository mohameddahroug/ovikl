package com.caoutch.transnet.view;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.caoutch.transnet.BuildConfig;
import com.caoutch.transnet.R;
import com.caoutch.transnet.activity.MainActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.util.Arrays;
import java.util.List;

public class LoadingRelativeLayout extends RelativeLayout {
    public AdView mAdView;
    ProgressBar progressBar;
    RelativeLayout view;
    public Button retryBtn;
    boolean showIn=false;
    int density=1;
    public LoadingRelativeLayout(Context context) {
        super(context);
    }

    public LoadingRelativeLayout(Context context, AttributeSet attrs) {



        super(context, attrs);
         density = (int) getResources().getDisplayMetrics().density;



        //this.setPadding(0,50*density,0,0);
        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
        setPadding(0,actionBarHeight,0,0);

        view=new RelativeLayout(context);
        progressBar = new ProgressBar(context);
        retryBtn=new Button(context);
        view.addView(progressBar);
        view.addView(retryBtn);
        retryBtn.setText(R.string.retry);
        retryBtn.setAllCaps(false);

        view.setBackgroundColor(getResources().getColor(R.color.white30));
        addView(view);

        LayoutParams layoutParamsRelative = (LayoutParams) view.getLayoutParams();
        layoutParamsRelative.height= LayoutParams.MATCH_PARENT;
        layoutParamsRelative.width= LayoutParams.MATCH_PARENT;


        view.setLayoutParams(layoutParamsRelative);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setClickable(true);

        LayoutParams layoutParamsProgress = (LayoutParams) progressBar.getLayoutParams();
        layoutParamsProgress.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progressBar.setLayoutParams(layoutParamsProgress);

        LayoutParams layoutParamsBtn = (LayoutParams) retryBtn.getLayoutParams();
        layoutParamsBtn.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        retryBtn.setLayoutParams(layoutParamsBtn);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            retryBtn.setBackground(getResources().getDrawable(R.drawable.rounded_border));
        }
        retryBtn.setPadding(10*density,0,10*density,0);
        view.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        retryBtn.setVisibility(GONE);


        if(attrs.getAttributeValue("http://schemas.android.com/apk/res-auto","layout_behavior")!=null){
            showIn=true;
        }


    }

    public LoadingRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void loaded(){
        view.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        retryBtn.setVisibility(GONE);
        /*if(mAdView==null) {
            mAdView = new AdView(this.getContext());
            mAdView.setAdUnitId("ca-app-pub-6615275988084929/3257153734");
            mAdView.setAdSize(AdSize.BANNER);

            addView(mAdView);

//            List<String> testDeviceIds = Arrays.asList("285C720E7C469EA6EDF41F8B4F739D42");
//            RequestConfiguration configuration =
//                    new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
//            MobileAds.setRequestConfiguration(configuration);




            LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.ALIGN_PARENT_TOP);


            if(showIn){
                TypedValue tv = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
                int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
                p.setMargins(0,actionBarHeight,0,0);
            }
            mAdView.setLayoutParams(p);

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.i("Ads","onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    Log.i("Ads","onAdFailedToLoad" + errorCode);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                    Log.i("Ads","onAdOpened");
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                    Log.i("Ads","onAdClicked");
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                    Log.i("Ads","onAdLeftApplication");
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                    Log.i("Ads","onAdClosed");
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }*/
    }

    public void loading(){
        view.setVisibility(VISIBLE);
        progressBar.setVisibility(VISIBLE);
        retryBtn.setVisibility(GONE);
        view.bringToFront();
        progressBar.requestFocus();
    }

    public void loadingFailed(){
        view.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
        retryBtn.setVisibility(VISIBLE);
        view.bringToFront();
        view.requestFocus();
    }


}

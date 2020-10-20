package com.caoutch.transnet.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.caoutch.transnet.BuildConfig;
import com.caoutch.transnet.Constants;
import com.caoutch.transnet.Main4Activity;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.R;
import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.User;


public class CheckLocationActivity extends SuperFragment implements View.OnClickListener {

    private final String tag = "CheckLocationActivity";
    private final int MY_PERMISSIONS_LOCATION = 1001;
    String lang="en";
    View root;
    LinearLayout enableLocationLayout;
    LinearLayout enableGpsLayout;
    ImageView logo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.activity_check_location, container, false);
        enableLocationLayout=root.findViewById(R.id.enable_location_layout);
        enableGpsLayout=root.findViewById(R.id.enable_gps_layout);
        logo=root.findViewById(R.id.logo_image_view);
        root.findViewById(R.id.enable_location_ok_btn).setOnClickListener(this);
        root.findViewById(R.id.enable_location_cancel_btn).setOnClickListener(this);
        root.findViewById(R.id.enable_gps_ok_btn).setOnClickListener(this);
        root.findViewById(R.id.enable_gps_cancel_btn).setOnClickListener(this);
        // ATTENTION: This was auto-generated to handle app links.

        return root;
    }



    @Override
    public void onResume() {
        super.onResume();
        enableGpsLayout.setVisibility(View.GONE);
        enableLocationLayout.setVisibility(View.GONE);
        logo.setVisibility(View.VISIBLE);
        Log.i(tag,"onResume");
        if (User.getInstance().email == null||User.getInstance().email.isEmpty()
                ||User.getInstance().hashedKey==null||User.getInstance().hashedKey.isEmpty()) {

            //Intent i = new Intent(getContext(), LoginActivity.class);
            //startActivity(i);
            //finish();
            Navigation.findNavController(root).navigate(R.id.nav_login);
        }
        /*else if(!User.getInstance().emailVerified){
            Toast toast = Toast.makeText(this, getString(R.string.login_active_email), Toast.LENGTH_LONG);
            toast.show();
            Intent i = new Intent(this, InfoActivity.class);
            startActivity(i);
            finish();
        }*/
        else if(User.getInstance().isDriver()&&User.getInstance().driverStatus.contentEquals(Constants.pending)){
            /*if(User.getInstance().idNumber==null||User.getInstance().idNumber.isEmpty()) {
                Intent i = new Intent(getContext(), RegisterDriverActivity.class);
                startActivity(i);
            }
            else if(User.getInstance().carType==null||User.getInstance().carType.isEmpty()) {
                Intent i = new Intent(getContext(), RegisterCarActivity2.class);
                startActivity(i);
                this.overridePendingTransition(0, 0);
            }
            else{
                Intent i = new Intent(getContext(), RegisterPricesActivity.class);
                startActivity(i);
                this.overridePendingTransition(0, 0);
            }*/
            //Intent i = new Intent(getContext(), RegisterCarActivity2.class);
            //startActivity(i);
            //finish();
            Navigation.findNavController(root).navigate(R.id.nav_register_car);
        }
        else{
            checkLocation();
        }

    }









    void checkGPS(){
        try{
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network_enabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(gps_enabled||(network_enabled&&(User.getInstance().isClient()||User.getInstance().isAdmin()))) {
                //Intent i = new Intent(getContext(), Main4Activity.class);
                //startActivity(i);
                //finish();
                Navigation.findNavController(root).navigate(R.id.nav_home);
            }
            else{
                enableGpsLayout.setVisibility(View.VISIBLE);
                enableLocationLayout.setVisibility(View.GONE);
                logo.setVisibility(View.GONE);
            }
        }catch(Exception ex){
            //do nothing...
        }
    }

    void checkLocation(){
        if (User.getInstance().isDriver()&&ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            checkGPS();

        }
        else if ((User.getInstance().isClient()||User.getInstance().isAdmin())&&
                (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED||
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED      ) ) {
            checkGPS();

        }
        else {
            enableGpsLayout.setVisibility(View.GONE);
            enableLocationLayout.setVisibility(View.VISIBLE);
            logo.setVisibility(View.GONE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(tag, "onRequestPermissionsResult granted");
                        checkGPS();
                } else {
                    Log.i(tag, "onRequestPermissionsResult denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //finish();
                }
                return;
            }
        }
    }

    public void onClick(View view){
        if(view.getId()==R.id.enable_location_ok_btn){
            if((User.getInstance().isClient()||User.getInstance().isAdmin())) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_LOCATION);
            }
            else{
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        MY_PERMISSIONS_LOCATION);
            }
            enableLocationLayout.setVisibility(View.GONE);
        }
        else if(view.getId()==R.id.enable_gps_ok_btn){
            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }
        else{
            getActivity().finish();
        }
    }


}

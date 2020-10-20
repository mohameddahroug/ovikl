package com.caoutch.transnet;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.collection.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class User implements Serializable {
    private transient static User mInstance;
    @SerializedName("_id")
    public String _id;

    //public String id;
    public String fcmToken;
    public String type;
    //public String name;
    public String email;
    public String clientStatus;
    public String driverStatus;
    public String adminStatus;
    public String carType;
    //public String stripeCustomerId;
    //public String paymentMethod;
    //public String stripeLast4;
    public Integer ads=1;
    //public Integer stripe=0;
    //public String userName;

    public String firstName;
    public String lastName;
    //public String password;
    public String mobile;
    public Boolean emailVerified=false;
    public Boolean mobileVerified=false;

    public String driverLicenseNumber;
    public String carLicenseNumber;
    public String carManufacturer;
    public String carModel;
    public String carMadeYear;
    public String carColor;
    public String carNumber;
    public String idNumber;
    public String hashedKey;
    public Date createDate;
    public Float driverRate;
    public Float clientRate;
    public Float carRate;
    public Float totalDistance;
    public Integer claimsCount;
    public Integer tripsCount;
    public Float totalHours;
    public String zone;
    public Zone zoneContact=new Zone();
    public ArrayMap<String,String> images=new androidx.collection.ArrayMap<String,String>();
    public Cost cost = new Cost();
    public GsonResponse.Config config=new GsonResponse.Config();
    //public ArrayList<GsonResponse.VehicleImages> vehiclesImages;

    public User(){

    }




    public static  User getInstance() {
        if (mInstance == null) {

            mInstance = new User();
            try {
                mInstance.loadUser();
            }
            catch(Exception e){
                Log.e("User",e.getMessage());

            }
        }
        return mInstance;
    }

    public static  void setInstance(User user) {
        mInstance = user;
        mInstance.saveUser();
    }


    public boolean isDriver(){
        if(mInstance==null||type==null)
            return false;
        return type.contentEquals(Constants.driver);
    }
    public boolean isClient(){
        if(mInstance==null||type==null)
            return false;
        return type.contentEquals(Constants.client);
    }

    public boolean isAdmin(){
        if(mInstance==null||type==null)
            return false;
        return type.contentEquals(Constants.admin)||type.contentEquals(Constants.super_admin);
    }

    public boolean isSuperAdmin(){
        if(mInstance==null||type==null)
            return false;
        return type.contentEquals(Constants.super_admin);
    }

    public boolean isActive(){
        if(isDriver())
            return driverStatus.contentEquals(Constants.active);
        else if(isClient())
            return clientStatus.contentEquals(Constants.active);
        else if(isAdmin())
            return adminStatus.contentEquals(Constants.active);
        return false;
    }

    public void loadUser(){
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Constants.shared, Context.MODE_PRIVATE);
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

        Gson gson = new Gson();
        String json = sharedPref.getString("user", "");
        mInstance = gson.fromJson(json, User.class);
        if(mInstance==null){
            mInstance=new User();
        }

        images.put(Constants.idImage,sharedPref.getString(Constants.idImage,""));
        images.put(Constants.idImageSmall,sharedPref.getString(Constants.idImageSmall,""));
        images.put(Constants.driverLicenseImage,sharedPref.getString(Constants.driverLicenseImage,""));
        images.put(Constants.driverLicenseImageSmall,sharedPref.getString(Constants.driverLicenseImageSmall,""));
        images.put(Constants.personalImage,sharedPref.getString(Constants.personalImage,""));
        images.put(Constants.personalImageSmall,sharedPref.getString(Constants.personalImageSmall,""));
        images.put(Constants.carLicenseImage,sharedPref.getString(Constants.carLicenseImage,""));
        images.put(Constants.carLicenseImageSmall,sharedPref.getString(Constants.carLicenseImageSmall,""));
        images.put(Constants.frontImage,sharedPref.getString(Constants.frontImage,""));
        images.put(Constants.frontImageSmall,sharedPref.getString(Constants.frontImageSmall,""));
        images.put(Constants.backImage,sharedPref.getString(Constants.backImage,""));
        images.put(Constants.backImageSmall,sharedPref.getString(Constants.backImageSmall,""));
        images.put(Constants.sideImage,sharedPref.getString(Constants.sideImage,""));
        images.put(Constants.sideImageSmall,sharedPref.getString(Constants.sideImageSmall,""));


    }
//    void loadUser(){
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
//        userId = "5b52f6ac753c3604eafedf37";
//        id = "+966549621566";
//        type = "client";
//        auth = "mobile";
//        name = "Mohamed";
//        email = "";
//        status = "active";
//    }

//    void loadUser(){
    //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
//        userId = "5b53136c753c3604eafedf38";
//        id = "+966549621566";
//        type = "driver";
//        auth = "mobile";
//        name = "Mohamed";
//        email = "";
//        status = "active";
//    }

    private void saveUser(){
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Constants.shared, Context.MODE_PRIVATE);
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        Gson gson = new Gson();
        String json = gson.toJson(mInstance);
        editor.putString("user", json);

        editor.putString(Constants.idImage,images.get(Constants.idImage));
        editor.putString(Constants.idImageSmall,images.get(Constants.idImageSmall));
        editor.putString(Constants.driverLicenseImage,images.get(Constants.driverLicenseImage));
        editor.putString(Constants.driverLicenseImageSmall,images.get(Constants.driverLicenseImageSmall));
        editor.putString(Constants.personalImage,images.get(Constants.personalImage));
        editor.putString(Constants.personalImageSmall,images.get(Constants.personalImageSmall));
        editor.putString(Constants.carLicenseImage,images.get(Constants.carLicenseImage));
        editor.putString(Constants.carLicenseImageSmall,images.get(Constants.carLicenseImageSmall));
        editor.putString(Constants.frontImage,images.get(Constants.frontImage));
        editor.putString(Constants.frontImageSmall,images.get(Constants.frontImageSmall));
        editor.putString(Constants.backImage,images.get(Constants.backImage));
        editor.putString(Constants.backImageSmall,images.get(Constants.backImageSmall));
        editor.putString(Constants.sideImage,images.get(Constants.sideImage));
        editor.putString(Constants.sideImageSmall,images.get(Constants.sideImageSmall));

        editor.commit();
    }

    public void reset(){
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Constants.shared, Context.MODE_PRIVATE);
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("user");

        editor.remove(Constants.idImage);
        editor.remove(Constants.idImageSmall);
        editor.remove(Constants.driverLicenseImage);
        editor.remove(Constants.driverLicenseImageSmall);
        editor.remove(Constants.personalImage);
        editor.remove(Constants.personalImageSmall);
        editor.remove(Constants.carLicenseImage);
        editor.remove(Constants.carLicenseImageSmall);
        editor.remove(Constants.frontImage);
        editor.remove(Constants.frontImageSmall);
        editor.remove(Constants.backImage);
        editor.remove(Constants.backImageSmall);
        editor.remove(Constants.sideImage);
        editor.remove(Constants.sideImageSmall);



        editor.commit();
        mInstance=new User();
    }


    public static class Cost implements Serializable {
        public float minimum=0;
        public float base=0;
        public float km=0;
        public float minute=0;
        public String currency="";
    }

    public static class Zone implements Serializable {
        public String zone;
        public String email;
        public String mobile;
    }

}

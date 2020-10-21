package com.caoutch.transnet;

public class Constants {
    public static final String dev_url="http://10.0.2.2";
    public static final String dev_nodejs_index_url="http://10.0.2.2:8080";
    public static String url="https://yourdomain.com";
    public static String nodejs_index_url="https://subdomain.yourdomain.com";
    public static final String client="client";
    public static final String driver="driver";
    public static final String admin="admin";
    public static final String super_admin="super_admin";
    public static final String NO_TRIP="NO_TRIP";
    public static final String PENDING="PENDING";
    public static final String RESERVED="RESERVED";
    public static final String STARTED="STARTED";
    public static final String FINISHED="FINISHED";
    public static final String CANCELED="CANCELED";
    public static final String TIMEOUT="TIMEOUT";
    public static final String TRIP_STATE="TRIP_STATE";
    public static final String ServiceToMainActivity="ServiceToMainActivity";
    public static final String MainActivityTOService="MainActivityTOService";
    public static final String driverHasTrip ="driverHasTrip";
    public static final String cash="cash";
    public static final String visa="visa";
    public static final String pending="pending";
    public static final String active="active";
    public static final String blocked="blocked";
    public static final String pending_email="pending_email";

    public static final String emailRegexp="[\\w\\.-]{2,}\\@\\w+\\.\\w+";
    public static final String userNameRegexp="[\\w\\.-]{6,}";
    public static final String passwordRegexp="[\\w\\.!@#$%^&*+-= ]{6,}";
    public static final String mobileRegexp="\\+?\\w{6,}";
    public static final String nameRegexp="[\\w\\. ]{2,}";
    public static final String idRegexp="[\\w\\. ]{6,}";
    public static final String yearRegexp="(20|19)\\d{2}";


    public static final String idImage="idImage";
    public static final String idImageSmall="idImageSmall";
    public static final String driverLicenseImage="driverLicenseImage";
    public static final String driverLicenseImageSmall="driverLicenseImageSmall";
    public static final String personalImage="personalImage";
    public static final String personalImageSmall="personalImageSmall";
    public static final String carLicenseImage="carLicenseImage";
    public static final String carLicenseImageSmall="carLicenseImageSmall";
    public static final String frontImage="frontImage";
    public static final String frontImageSmall="frontImageSmall";
    public static final String backImage="backImage";
    public static final String backImageSmall="backImageSmall";
    public static final String sideImage="sideImage";
    public static final String sideImageSmall="sideImageSmall";

    public static final String shared="transnet";


    /*static String getKey(String s){
        int prime=5903;
        int prime1=6551;
        int prime2=2207;
        int prime3=3673;
        int prime4=1907;
        int k=0;
        for(int i=0;i<s.length();i++){
           k+=s.charAt(i);
        }
        return String.valueOf((k%prime1)+prime)+
                String.valueOf((k%prime2)+prime)+
                String.valueOf((k%prime3)+prime)+
                String.valueOf((k%prime4)+prime);
    }*/



}

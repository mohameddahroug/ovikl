package com.caoutch.transnet;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import androidx.annotation.NonNull;
import androidx.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.collection.ArrayMap;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.activity.MainActivity;
import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.database.Trip;
import com.caoutch.transnet.database.TripLocation;
import com.caoutch.transnet.database.TripMessage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/*
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
*/

public class TCPService extends Service {

    private static TCPService instance = null;

    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    //private final int interval= 10000;
    //private final int fastInterval= 3000;

    private final int locationServiceId=101;
    private final int keepNotification=102;
    private final String channelId="caoutch";
    private IBinder mBinder= new LocalBinder();        // interface for clients that bind
    private boolean mAllowRebind; // indicates whether onRebind should be used
    private LatLng latLng;
    private boolean oneLocation=false;
    private boolean socketOpened=false;

    private boolean isLogin=false;

    private SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.ENGLISH);
    private AppDatabase database;
    //Trip trip=new Trip();
    //SharedPreferences sharedPref ;
    //boolean tripStarted=false;
    //static final String TRIPID ="TRIPID";

    private ConcurrentLinkedQueue<JSONObject> messageQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<TCPResponse> mainActivityMessagesQueue = new ConcurrentLinkedQueue<>();
    private JSONObject currentMessage ;
    private String currentMsgId;
    private Date currentMsgSendTime;
    private TripSingleton tripSingleton= TripSingleton.getInstance();
    //private String lastMsgId;
    private boolean activityRunning=true;
    private String tag= "TCPService";
    private String version;
    private String lang;
    private RequestQueue queue ;

    private double distance=0;
    private double duration=0;
    private double cost=0;
    private TripLocation driverTripLocation;
    private NotificationManagerCompat notificationManager ;


    public static final String TAG = TCPService.class.getSimpleName();
    GsonResponse.Server server;


    GsonBuilder gsonBuilder=new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    Date lastTest=new Date();
    SocketFactory factory;
    //int timeout;



    private void onNewDriverLocation(TCPResponse response){


            if(tripSingleton.state == Constants.CANCELED)
                return;
            String id;
            String type;
            Double latitude;
            Double longitude;

                id = response._id;
                type = response.type;
                latitude = response.latitude;
                longitude = response.longitude;
                //if(data.has("msgId"))
                //    lastMsgId=data.getString("msgId");
                if(tripSingleton._id!=null&&tripSingleton.driverId!=null && id.contentEquals(tripSingleton.driverId)){
                    tripSingleton.driverLat=latitude;
                    tripSingleton.driverLng=longitude;

                    TripLocation tLoc = new TripLocation();
                    tLoc._id=tripSingleton._id;
                    tLoc.state=tripSingleton.state;
                    tLoc.latitude=latitude;
                    tLoc.longitude=longitude;
                    tLoc.time=new Date();
                    database.tripLocationDao().insertTripLocation(tLoc);
                }
                if(tripSingleton._id==null||(tripSingleton._id!=null&&tripSingleton.driverId!=null && id.contentEquals(tripSingleton.driverId))) {
                    response.event="newDriverLocation";
                    sendToMainActivity( response);
                }


        }



    private void onOffDriverLocation(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;

            String id;
            //String type;
            //Double latitude;
            //Double longitude;

                id = response._id;
                //type = data.getString("type");
                //if(data.has("msgId"))
                //    lastMsgId=data.getString("msgId");
                Map<String,String> m = new ArrayMap<>();
                m.put("_id",id);
                response.event="offDriverLocation";
                sendToMainActivity(response);


        }

    private void onNewClientLocation(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;

            String id;
            String type;

                id = response._id;
                if(tripSingleton._id==null || (tripSingleton._id!=null&&tripSingleton.clientId!=null && id.contentEquals(tripSingleton.clientId))) {


                    Map<String, String> m = new ArrayMap<>();
                    m.put("latitude", Double.toString(response.latitude));
                    m.put("longitude", Double.toString(response.longitude));
                    m.put("_id", id);
                    response.event="newClientLocation";
                    sendToMainActivity( response);
                }


        }


    private void onOffClientLocation(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;
            if(User.getInstance().isAdmin()){
                String id;
                String type;

                id = response._id;
                //type = data.getString("type");
                //if(data.has("msgId"))
                //    lastMsgId=data.getString("msgId");
                Map<String, String> m = new ArrayMap<>();
                m.put("_id", id);
                response.event = "offClientLocation";
                sendToMainActivity(response);

            }
        }


    private void onDriverConfirmTrip(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;

                //if(tripJson.has("msgId"))
                //    lastMsgId=tripJson.getString("msgId");
                if(tripSingleton._id!=null && tripSingleton._id.contentEquals(response.trip._id)) {
                    tripSingleton.setSingleton(response.trip);
                    response.event="onDriverConfirmTrip";
                    response.isEmpty=true;
                    sendToMainActivity(response);
                    showNotification(getString(R.string.notification_title),getString(R.string.driver_accept),locationServiceId,true,response);
                    database.tripDao().insertTrip(tripSingleton.getTrip());
                }
                //timeout=User.getInstance().config.timeoutLong;


        }




    private void onDriverCancelTrip(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;

                //if(tripJson.has("msgId"))
                //    lastMsgId=tripJson.getString("msgId");

                if(tripSingleton._id!=null && tripSingleton._id.contentEquals(response.trip._id)) {
                    tripSingleton.setSingleton(response.trip);

                    Map<String,String> m = new ArrayMap<>();
                    m.put("driverId",tripSingleton.driverId);
                    response.event="onDriverCancelTrip";

                    if(activityRunning) {
                        showNotification(getString(R.string.notification_title),getString(R.string.driver_cancel_trip),locationServiceId,true,response);
                    }
                    else{
                        showNotification(getString(R.string.notification_title),getString(R.string.driver_cancel_trip),keepNotification,true,response);
                        stop();
                    }
                    database.tripDao().insertTrip(tripSingleton.getTrip());
                    resetTrip();
                    sendToMainActivity( response);
                }



        }



    private void onClientCancelTrip(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;

                //if(tripJson.has("msgId"))
                //    lastMsgId=tripJson.getString("msgId");
                if(tripSingleton._id!=null && tripSingleton._id.contentEquals(response.trip._id)) {
                    tripSingleton.setSingleton(response.trip);

                    //response.event="onClientCancelTrip";

                    if(activityRunning) {
                        showNotification(getString(R.string.notification_title),getString(R.string.client_cancel_trip),locationServiceId,true,response);
                    }
                    else{
                        showNotification(getString(R.string.notification_title),getString(R.string.client_cancel_trip),keepNotification,true,response);
                        //stopSelf();
                    }
                    database.tripDao().insertTrip(tripSingleton.getTrip());
                    resetTrip();
                    sendToMainActivity( response);
                }



        }


    private void onDriverStartTrip(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;

                //if(tripJson.has("msgId"))
                //    lastMsgId=tripJson.getString("msgId");
                if(tripSingleton._id!=null && tripSingleton._id.contentEquals(response.trip._id)) {
                    tripSingleton.setSingleton(response.trip);

                    response.event="driverStartTrip";
                    sendToMainActivity( response);
                    showNotification(getString(R.string.notification_title),getString(R.string.driver_arrived),locationServiceId,true,response);
                    database.tripDao().insertTrip(tripSingleton.getTrip());
                }

        }


    private void onDriverFinishTrip(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;

                //if(tripJson.has("msgId"))
                //    lastMsgId=tripJson.getString("msgId");
                if(tripSingleton._id!=null && tripSingleton._id.contentEquals(response.trip._id)) {
                    tripSingleton.setSingleton(response.trip);

                    if(activityRunning) {
                        showNotification(getString(R.string.notification_title),
                                getString(R.string.FINISHED).toString()+". "+getString(R.string.cost_is)+" "+response.cost+" "+tripSingleton.cur
                                ,locationServiceId,true,response);
                    }
                    else{
                        showNotification(getString(R.string.notification_title),
                                getString(R.string.FINISHED).toString()+". "+getString(R.string.cost_is)+" "+tripSingleton.cost+
                                " "+tripSingleton.cur
                                ,keepNotification,true,response);

                    }


                    response.event="onDriverFinishTrip";

                    database.tripDao().insertTrip(tripSingleton.getTrip());
                    resetTrip();
                    sendToMainActivity(response);
                    stop();
                }


        }


    private void onDriverNewTrip(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;
            try {
                //if(data.has("msgId"))
                //    lastMsgId=data.getString("msgId");
                if (tripSingleton._id != null && !response.trip._id.contentEquals(tripSingleton._id)) {
                    JSONObject map = new JSONObject();
                    map.put("event", "driverCancel");
                    map.put("_id", response.trip._id);
                    map.put("driverId", response.trip.driverId);
                    map.put("clientId", response.trip.clientId);
                    addToQueue(map);
                } else {
                    tripSingleton.setSingleton(response.trip);
                    response.event="onDriverNewTrip";
                    sendToMainActivity( response);
                    showNotification(getString(R.string.notification_title), getString(R.string.new_trip), locationServiceId, true, response);
                    database.tripDao().insertTrip(tripSingleton.getTrip());
                }
            }
            catch (JSONException e){

            }


        }


    private void tripMessage(TCPResponse response){
            //if(lastMsgId!=null && ((String) args).contains("\"msgId\":"+lastMsgId+'"'))
            //    return;
            showNotification(getString(R.string.notification_title),null,locationServiceId,false,response);


        }


    private void onConnect(){
            Log.i(tag, "onConnect");
            lastTest=new Date();
            socketOpened=true;
            login();
            Map<String,String> m = new ArrayMap<>();
            TCPResponse response=new TCPResponse();
            response.event="onConnect";
            response.isEmpty=true;
            sendToMainActivity(response);


        }

    private void onDisconnent(){
            Log.i(tag, "onDisconnent");

            socketOpened=false;
            isLogin=false;
            currentMessage=null;
            currentMsgId=null;
            currentMsgSendTime=null;
            TCPResponse response=new TCPResponse();
            response.event="onDisconnent";
            response.isEmpty=true;
            sendToMainActivity(response);

        }





    public class LocalBinder extends Binder {
        public TCPService getService() {
            // Return this instance of LocalService so clients can call public methods

            return TCPService.this;
        }
    }

    public TCPService() {
        //timeout=User.getInstance().config.timeout;


    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        Log.i(tag, "onBind");
        /*if (tripSingleton._id == null) {
            if (User.getInstance().isClient())
                getOneLocation();
            else
                getLocationUpdates();
        }*/

        if(isLogin){
            TCPResponse response=new TCPResponse();
            response.event="loginDone";
            response.isEmpty=true;
            sendToMainActivity(response);
        }

        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(tag, "onUnbind");
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        Log.i(tag, "onRebind");
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onCreate() {
        tag=tag+"_"+User.getInstance().type;
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
//                Log.i(tag, "uncaughtException");
//
//                TCPResponse response1 = new TCPResponse();
//                response1.event="disconnect";
//                response1.isEmpty=true;
//                sendToMainActivity(response1);
//                LocalBroadcastManager.getInstance(TCPService.this).unregisterReceiver(mMessageReceiver);
//                if(MyApplication.tcpClient!=null) {
//                    try {
//                        MyApplication.tcpClient.stopClient();
//                    }
//                    catch (Exception e1){
//
//                    }
//                }
//                if(mFusedLocationClient!=null)
//                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//                stopForeground(true);
//            }
//        });
        Log.i(tag, "onCreate");
        super.onCreate();
        //TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.MainActivityTOService));
        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "caoutch").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        //dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        //sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // prepare last location
        try {
            Trip trip = database.tripDao().findLastTrip(User.getInstance()._id);
            if (trip != null && trip.state != null && trip.state.contentEquals(Constants.CANCELED) && trip.state.contentEquals(Constants.FINISHED)) {
                tripSingleton.setSingleton(trip);
            }
        }
        catch(Exception e){
            Log.i(tag,e.getMessage());
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //monitor location
        mLocationRequest = LocationRequest.create();
        if(User.getInstance().isClient())
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        else
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(User.getInstance().config.interval);
        mLocationRequest.setFastestInterval(User.getInstance().config.fastInterval);


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null &&locationResult.getLocations().size()>1) {
                    return;
                }
                final Location loc = locationResult.getLocations().get(locationResult.getLocations().size()-1) ;


                            Log.i(tag, "LocationCallback send message LatLng: " + loc.getLatitude() + " " + loc.getLongitude());
                            //setLocationPoint(location);
                            latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

                            if(User.getInstance().isDriver()&&tripSingleton._id!=null&&isLogin){
                                Date date= new Date();
                                if(tripSingleton.state.contains(Constants.STARTED)&&driverTripLocation!=null){
                                    Location locationB = new Location("point B");
                                    locationB.setLatitude(driverTripLocation.latitude);
                                    locationB.setLongitude(driverTripLocation.longitude);
                                    distance = distance + loc.distanceTo(locationB);
                                    duration = duration + date .getTime() - driverTripLocation.time.getTime();
                                    if(tripSingleton.prKM!=null&&tripSingleton.prMinute!=null&&tripSingleton.prBase!=null) {
                                        cost = tripSingleton.prBase + ((distance / 1000) * tripSingleton.prKM) + ((duration / 60000) * tripSingleton.prMinute);
                                        if (cost < tripSingleton.prMin) {
                                            cost = tripSingleton.prMin;
                                        }
                                    }

                                }
                                driverTripLocation = new TripLocation();
                                driverTripLocation._id=tripSingleton._id;
                                driverTripLocation.state=tripSingleton.state;
                                driverTripLocation.latitude=loc.getLatitude();
                                driverTripLocation.longitude=loc.getLongitude();
                                driverTripLocation.time=date;
                                driverTripLocation.duration=duration;
                                driverTripLocation.distance=distance;
                                database.tripLocationDao().insertTripLocation(driverTripLocation);
                            }
                            if(isLogin&&messageQueue.isEmpty())
                                sendLocationToServer();
                            TCPResponse response = new TCPResponse();
                            Map<String,String> m = new ArrayMap<>();
                            response.latitude=latLng.latitude;
                            response.longitude=latLng.longitude;
                            if(distance>0 || duration > 0 || cost > 0) {
                                response.distance= distance;
                                response.duration=duration;
                                response.cost=cost;
                            }
                            m.put("_id",User.getInstance()._id);
                            if(User.getInstance().isDriver()) {
                                response.event="driverLocation";
                                sendToMainActivity(response );
                            }
                            else if(User.getInstance().isClient()){
                                response.event="clientLocation";
                                sendToMainActivity(response);
                            }
                            else {
                                response.event="myLocation";
                                sendToMainActivity(response);
                            }
                            if(oneLocation){
                                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                                //mLocationCallback=null;
                                //mFusedLocationClient=null;
                            }
                            if(server==null){
                                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                                connectServer(Double.toString(latLng.latitude),Double.toString(latLng.longitude));

                            }



            }

        };
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.car)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                ;
        Notification n=mBuilder.build();
        notificationManager.notify(locationServiceId, n);
        startForeground(locationServiceId,n);

        //getLocationUpdates();
        version = "A"+BuildConfig.VERSION_CODE;
        lang=Locale.getDefault().getLanguage();


        ActivityResumed();
        //oneLocation=true;
        //getLocationUpdates();

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.i(tag,"getLastLocation success");
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            connectServer(Double.toString(location.getLatitude()),Double.toString(location.getLongitude()));

                            TCPResponse response = new TCPResponse();
                            Map<String,String> m = new ArrayMap<>();
                            response.latitude=latLng.latitude;
                            response.longitude=latLng.longitude;
                            response.event="myLocation";
                            sendToMainActivity(response);

                        }
                        else{
                            Log.i(tag,"getLastLocation failed");
                            oneLocation=true;
                            getLocationUpdates();
                        }
                    }


                });

        if(Constants.url.contentEquals("http://10.0.2.2"))
            factory =  SocketFactory.getDefault();
        else
            factory = SSLSocketFactory.getDefault();



       /* Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true){
                    if (socketOpened&&MyApplication.tcpClient != null) {
                        MyApplication.tcpClient.sendMessage("{\"event\":\"test\"}");
                    }

                    try {
                        Thread.sleep(2500);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();*/
    }

    void saveTripMessage(Date time){

            String messageStr=null;
            String senderId=null;
            if(tripSingleton.state.contentEquals(Constants.PENDING)) {
                messageStr=getString(R.string.PENDING);
                senderId=tripSingleton.clientId;
            }
            else if(tripSingleton.state.contentEquals(Constants.RESERVED)) {
                messageStr=getString(R.string.RESERVED);
                senderId=tripSingleton.driverId;
            }
            else if(tripSingleton.state.contentEquals(Constants.STARTED)){
                messageStr=getString(R.string.STARTED);
                senderId=tripSingleton.driverId;
            }
            else if(tripSingleton.state.contentEquals(Constants.FINISHED)){
                messageStr=getString(R.string.FINISHED);
                messageStr=messageStr+"\n"+getString(R.string.cost) + ": " + String.format(Locale.ENGLISH,"%.2f", tripSingleton.cost) + " " + tripSingleton.cur;
                messageStr = messageStr + "\n" + getString(R.string.baseCost) + ": " + String.format(Locale.ENGLISH,"%.2f", tripSingleton.prBase);
                messageStr = messageStr + "\n" + getString(R.string.duration) + ": " + String.format(Locale.ENGLISH,"%.2f", tripSingleton.duration) + " " + getString(R.string.minutes) + " * " + tripSingleton.prMinute+" " + tripSingleton.cur;
                messageStr = messageStr + "\n" + getString(R.string.distance) + ": " + String.format(Locale.ENGLISH,"%.2f", tripSingleton.distance) + " " + getString(R.string.km) + " * " + tripSingleton.prKM+" " + tripSingleton.cur;
                if (tripSingleton.cost <= tripSingleton.prMin) {
                    messageStr=messageStr+"\n"+getString(R.string.minimum)+": "+tripSingleton.prMin;
                }



                senderId=tripSingleton.driverId;
            }
            else if(tripSingleton.state.contentEquals(Constants.CANCELED) && tripSingleton.cancelledBy.contains("client")){
                messageStr=getString(R.string.client_cancel_trip);
                senderId=tripSingleton.clientId;
            }
            else if(tripSingleton.state.contentEquals(Constants.CANCELED) && tripSingleton.cancelledBy.contains("driver")){
                messageStr=getString(R.string.driver_cancel_trip);
                senderId=tripSingleton.driverId;
            }

            TripMessage message = new TripMessage();
            message.tripId = tripSingleton._id;
            message.state = tripSingleton.state;
            message.message = messageStr;
            //message.type = "received";
            message.senderId=senderId;
            message.msgId = tripSingleton.msgId;
            message._id = "";
            message.createTime = tripSingleton.updateTime;
            database.tripMessageDao().insertTripMessageOrIgnore(message);
            TCPResponse response2 = new TCPResponse();
            response2.tripMessage=new TripMessage();
            response2.tripMessage.tripId=tripSingleton._id ;
            response2.tripMessage.message=messageStr;
            response2.tripMessage.senderId=senderId ;
            response2.tripMessage.createTime=time ;
            response2.event="tripMessage";
            sendToMainActivity(response2);
    }

    void showNotification(String title, String messageStr,int id, boolean tripStateChanged,TCPResponse response){
        if(tripStateChanged) {
            saveTripMessage(response.trip.updateTime);
        }

        else if(response.tripMessage!=null){
            response.event="tripMessage";
                sendToMainActivity(response);
                //if(data.has("msgId"))
                //    lastMsgId=data.getString("msgId");
                //if (!data.getString("senderId").contentEquals(User.getInstance()._id)) {
                final TripMessage message =response.tripMessage;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        database.tripMessageDao().insertTripMessageOrIgnore(message);
                    }
                });
                //}

        }
        if(!activityRunning) {
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.car)
                    .setContentTitle(title)
                    .setContentText(messageStr)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(contentIntent)
                    ;

            Notification n = mBuilder.build();
            notificationManager.notify(id, n);
        }


        //return n;
    }


    void sendLocationToServer(){
        Log.i(tag, "sendLocationToServer LatLng: "+ socketOpened+ " " + latLng.latitude + " " + latLng.longitude);
        /*if(!mSocket.connected()){
            mSocket.connect();
            login();
        }*/
        if(User.getInstance().isDriver()) {
            JSONObject map = new JSONObject();
            try {
                map.put("event", "location");
                map.put("_id", User.getInstance()._id);
                map.put("type", User.getInstance().type);
                map.put("latitude", String.valueOf(latLng.latitude));
                map.put("longitude", String.valueOf(latLng.longitude));
                if(tripSingleton._id!=null) {
                    map.put("tripId", tripSingleton._id);
                    if (User.getInstance().isClient())
                        map.put("driverId", tripSingleton.driverId);
                    if (User.getInstance().isDriver())
                        map.put("clientId", tripSingleton.clientId);
                    if (distance != 0)
                        map.put("distance", String.format(Locale.ENGLISH, "%.2f", distance));
                    if (duration != 0)
                        map.put("duration", String.format(Locale.ENGLISH, "%.2f", duration));
                    if (cost != 0)
                        map.put("cost", String.format(Locale.ENGLISH, "%.2f", cost));
                }
                if(User.getInstance().isDriver())
                    map.put("carType", User.getInstance().carType);
                addToQueue(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(User.getInstance().isClient()) {
            if(tripSingleton._id==null) {
                JSONObject map = new JSONObject();
                try {
                    map.put("event", "location");
                    map.put("_id", User.getInstance()._id);
                    map.put("type", User.getInstance().type);
                    map.put("latitude", String.valueOf(latLng.latitude));
                    map.put("longitude", String.valueOf(latLng.longitude));
                    addToQueue(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                MyApplication.tcpClient.sendMessage("{}");
            }
        }
        else{
            MyApplication.tcpClient.sendMessage("{}");
        }
    }



    @Override
    public void onDestroy() {
        Log.i(tag, "onDestroy");
        //signOff();
        TCPResponse response1 = new TCPResponse();
        response1.event="disconnect";
        response1.isEmpty=true;
        sendToMainActivity(response1);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        if(MyApplication.tcpClient!=null) {
            try {
                MyApplication.tcpClient.stopClient();
            }
            catch (Exception e){

            }
        }
        if(mFusedLocationClient!=null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        stopForeground(true);

        super.onDestroy();

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(tag, "onStartCommand");
        return START_NOT_STICKY;

    }

    void stop(){
        TCPResponse response1 = new TCPResponse();
        response1.event="disconnect";
        response1.isEmpty=true;
        sendToMainActivity(response1);
        LocalBroadcastManager.getInstance(TCPService.this).unregisterReceiver(mMessageReceiver);
        if(MyApplication.tcpClient!=null) {
            try {
                MyApplication.tcpClient.stopClient();
            }
            catch (Exception e1){

            }
        }
        if(mFusedLocationClient!=null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        stopForeground(true);
        stopSelf();
    }

    void login(){
        Log.i(tag, "login socketOpened "+ socketOpened);

        if(socketOpened) {
            String loginStr="";
            try {
                String msgId=UUID.randomUUID().toString();
                String time=dateFormatter.format(new Date());
                JSONObject loginJson = new JSONObject();
                loginJson.put("event","login");
                loginJson.put("_id",User.getInstance()._id );
                loginJson.put("hashedKey", User.getInstance().hashedKey);
                loginJson.put("type",User.getInstance().type );
                loginJson.put("ver",version );
                loginJson.put("msgId",msgId);
                loginJson.put("time",time);
                loginJson.put("lang",lang);

                if(tripSingleton._id!=null)
                    loginJson.put("tripId",tripSingleton._id );
                loginStr=loginJson.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MyApplication.tcpClient.sendMessage(loginStr);
            /*mSocket.emit("login", loginStr, new Ack() {
                @Override
                public void call(Object... args) {
                    */
        }
    }


    /*void sendToMainActivity(String messageType,LatLng latLng,String userId) {
        Log.i(tag, "sendToMainActivity messageType: " + messageType);
        Intent intent = new Intent(MainActivity.MainActivityTOService);
        // You can also include some extra data.
        intent.putExtra("messageType", messageType);
        if(latLng != null) {
            intent.putExtra("latitude", latLng.latitude);
            intent.putExtra("longitude", latLng.longitude);
            intent.putExtra("_id", userId);
        }
        intent.putExtra("_id", userId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }*/

    void loginResponse(final TCPResponse response){





            if (response.retry) {
                TCPResponse response1 = new TCPResponse();
                response1.event="retry";
                response1.isEmpty=true;
                sendToMainActivity(response1);
                /*AsyncTask.execute(new Runnable() {


                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            login();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });*/
                return;
            }
            //User.setInstance(response.user);
            //User.getInstance().saveUser();
            if (response.user!=null&&response.user.isActive()) {
                if(response.tripArr!=null&&response.tripArr.size()>0) {
                    for (int i = response.tripArr.size() - 1; i >= 0; i--) {
                        Trip trip = response.tripArr.get(i);
                        tripSingleton.setSingleton(trip);
                        //trip = tripSingleton.getTrip();
                        database.tripDao().insertTripOrIgnore(tripSingleton.getTrip());
                        saveTripMessage(tripSingleton.updateTime);

                        if (trip.state.contentEquals(Constants.FINISHED) || trip.state.contentEquals(Constants.CANCELED)) {
                            resetTrip();
                        }

                    }

                    /*if(tripSingleton.state.contentEquals(Constants.RESERVED) ){
                        timeout=User.getInstance().config.timeoutLong;
                        mLocationRequest.setInterval(User.getInstance().config.intervalLong);
                        mLocationRequest.setFastestInterval(User.getInstance().config.fastIntervalLong);
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback,
                                Looper.getMainLooper());
                    }*/

                }
                if(response.tripMessageArr!=null&&response.tripMessageArr.size()>0) {
                    for (int i = response.tripMessageArr.size() - 1; i >= 0; i--) {
                        final TripMessage message = response.tripMessageArr.get(i);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                TripMessage message2 = database.tripMessageDao().getTripMessage(message.tripId, message.senderId, message._id);
                                if (message2 == null) {
                                    database.tripMessageDao().insertTripMessageOrIgnore(message);
                                }
                            }
                        });
                    }


                    if (response.driverLocation != null) {
                        tripSingleton.driverLat = response.driverLocation.latitude;
                        tripSingleton.driverLng = response.driverLocation.longitude;
                    }

                }
                if(distance==0&&duration==0) {
                    if (tripSingleton != null && tripSingleton.state != null &&
                            !(tripSingleton.state.contentEquals(Constants.FINISHED) || tripSingleton.state.contentEquals(Constants.CANCELED))) {
                        driverTripLocation = database.tripLocationDao().getLastTripLocation(tripSingleton._id);

                        if (driverTripLocation != null) {
                            distance = driverTripLocation.distance;
                            duration = driverTripLocation.duration;
                        }
                    }
                }

                isLogin = true;
                TCPResponse response1 = new TCPResponse();
                response1.event="loginDone";
                response1.isEmpty=true;
                sendToMainActivity(response1);

                if (User.getInstance().isDriver()) {
                    oneLocation = false;
                    getLocationUpdates();
                }
                else if (tripSingleton._id == null && User.getInstance().isClient()) {
                    //oneLocation=true;
                    //getLocationUpdates();
                    TCPResponse response2 = new TCPResponse();
                    response2.latitude=latLng.latitude;
                    response2.longitude=latLng.longitude;
                    response2.event="clientLocation";
                    sendToMainActivity(response2);
                    sendLocationToServer();

                }
                currentMessage = null;
                currentMsgId = null;
                currentMsgSendTime=null;
                if(User.getInstance().isClient()&&tripSingleton.driverId!=null) {
                    JSONObject map = new JSONObject();
                    try {
                        map.put("event", "userInfo");
                        map.put("user_id", tripSingleton.driverId);
                        map.put("_id", User.getInstance()._id);
                        addToQueue(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(User.getInstance().isDriver()&&tripSingleton.clientId!=null) {
                    JSONObject map = new JSONObject();
                    try {
                        map.put("event", "userInfo");
                        map.put("user_id", tripSingleton.clientId);
                        map.put("_id", User.getInstance()._id);
                        addToQueue(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }



            }
            else {
                if (User.getInstance().isDriver()) {
                    TCPResponse response1 = new TCPResponse();
                    response1.event="completeInfo";
                    response1.isEmpty=true;
                    sendToMainActivity(response1);
                }
                else {
                    TCPResponse response1 = new TCPResponse();
                    response1.event="notActive";
                    response1.isEmpty=true;
                    sendToMainActivity(response1);
                }
            }



    }



    void sendToMainActivity(TCPResponse response) {
        if(activityRunning) {
            Log.i(tag, "sendToMainActivity messageType: " + response.event);
            Intent intent = new Intent(Constants.ServiceToMainActivity);
            if (response != null)
                    intent.putExtra("response", response);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }else if(tripSingleton._id!=null&& response.event.contains("tripMessage")){
            mainActivityMessagesQueue.add(response);
        }
    }

    /*void sendToMainActivity(String messageType,String data,String userId) {
        Log.i(tag, "sendToMainActivity messageType: " + messageType);
        Intent intent = new Intent(MainActivity.MainActivityTOService);
        // You can also include some extra data.
        intent.putExtra("messageType", messageType);
        intent.putExtra("data", data);
        intent.putExtra("_id", userId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }*/


    void stopLocationUpdates() {
        Log.i(tag, "stopLocationUpdates");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        /*AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                trip.endTime=new Date();
                database.tripDao().updateTrip(trip);
                Log.i(tag, "AsyncTask stop trip id "+trip.id);
            }
        });*/

        stopForeground(true);

    }


    void getLocationUpdates() {
        Log.i(tag, "getLocationUpdates");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    Looper.getMainLooper());

        }
    }

    /*void getOneLocation(){
        Log.i(tag, "getOneLocation");
        oneLocation=true;
        //getLastLocation();
        getLocationUpdates();
    }*/

//    void startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
//            Log.i(tag, "startLocationUpdates");
//            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
//                    mLocationCallback,
//                    null /* Looper */);
//        }
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                //tripStarted=sharedPref.getBoolean(MainActivity.TRIPSTARTED,false);
//
//                Trip trip=database.tripDao().findLastTrip(User.getInstance()._id);
//                if(trip == null) {
//                    //trip = new Trip(new Date());
//                    database.tripDao().insertTrip(trip);
//                    trip=database.tripDao().findLastTrip(User.getInstance()._id);
//                }
//                //SharedPreferences.Editor editor = sharedPref.edit();
//                //editor.putInt(TRIPID, trip.id);
//                //editor.commit();
//                Log.i(tag, "AsyncTask start trip id "+trip._id);
//                //Notification notification = new Notification();
//                //startForeground(1, notification);
//
//            }
//        });
//    }



    /*void getLastLocation() {
        Log.i(tag, "getLastLocation");
        //if(serviceLocationStarted)
        //    return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            Log.i(tag, "getLastLocation got location ");
                            if (location != null) {
                                latLng=new LatLng(location.getLatitude(),location.getLongitude());
                                Map<String,String> m = new ArrayMap<>();
                                m.put("latitude",Double.toString(latLng.latitude));
                                m.put("longitude",Double.toString(latLng.longitude));
                                m.put("_id",User.getInstance()._id);
                                if(User.getInstance().isDriver())
                                    sendToMainActivity("driverLocation",response);
                                else
                                    sendToMainActivity("clientLocation",response);

                            }
                        }
                    });
        }
    }*/

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            String json =intent.getStringExtra("json");
            Log.i(tag, "BroadcastReceiver Got message: " + json);
            try {
                JSONObject map = new JSONObject(json);

                String event =map.getString("event");
                if(event.contentEquals("location")) {
                    latLng = new LatLng(Double.valueOf(map.getDouble("latitude")), Double.valueOf(map.getDouble("longitude")));
                    map.put("event", "location");
                    map.put("_id", User.getInstance()._id);
                    map.put("type", User.getInstance().type);
                    map.put("latitude", String.valueOf(latLng.latitude));
                    map.put("longitude", String.valueOf(latLng.longitude));


                    addToQueue(map);
                    if(server==null) {
                        connectServer(String.valueOf(latLng.latitude),String.valueOf(latLng.longitude));
                    }
                }
                /*else if(event.contentEquals("getLocationUpdates")) {
                    getLocationUpdates();
                }*/
                else if(event.contentEquals("getOneLocation")) {
                    if(latLng!=null) {
                        TCPResponse response = new TCPResponse();
                        response.latitude = latLng.latitude;
                        response.longitude = latLng.longitude;
                        if (distance > 0 || duration > 0 || cost > 0) {
                            response.distance = distance;
                            response.duration = duration;
                            response.cost = cost;
                        }
                        if (User.getInstance().isDriver()) {
                            response.event = "driverLocation";
                            sendToMainActivity(response);
                        } else if (User.getInstance().isClient()) {
                            response.event = "clientLocation";
                            sendToMainActivity(response);
                        } else {
                            response.event = "myLocation";
                            sendToMainActivity(response);
                        }
                    }
                }
                else if(event.contentEquals("finishTrip")) {
                    tripSingleton.cost =cost;
                    tripSingleton.distance=distance/ 1000;
                    tripSingleton.duration = duration/ 60000;

                    JSONObject map2 = tripSingleton.getMap();
                    map2.put("event", "finishTrip");
                    addToQueue(map2);
                    try {

                        database.tripDao().insertTrip(tripSingleton.getTrip());


                    } catch (Exception e) {
                        e.printStackTrace();
                        //sendToMainActivity("exception", null);
                        if (!activityRunning)
                            stop();
                    }



                }


                else if(event.contentEquals( "ActivityPaused")) {
                    activityRunning = false;
                    Log.i(tag, "ActivityPaused trip state " + tripSingleton.state);
//                        if(tripSingleton.state==null && User.getInstance().isClient()) {
//                            stop();
//                        }
                }
                else if(event.contentEquals( "ActivityDestroy")) {
                    Log.i(tag, "ActivityDestroy trip");
                    stop();

                }
                else if(event.contentEquals("ActivityResumed")) {
                    ActivityResumed();
                }
                else if(event.contentEquals("promo")) {
                    map.put("_id", User.getInstance()._id);
                    map.put("type", User.getInstance().type);
                    map.put("latitude", String.valueOf(latLng.latitude));
                    map.put("longitude", String.valueOf(latLng.longitude));
                    addToQueue(map);
                }
                else{
                        addToQueue(map);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    void ActivityResumed(){
        Log.i(tag, "activityRunning" + activityRunning);
        if(!activityRunning) {
            activityRunning = true;
            /*if (tripSingleton._id == null) {
                if (User.getInstance().isClient())
                    getOneLocation();
                else
                    getLocationUpdates();
            }*/


            while (tripSingleton._id != null && activityRunning && !mainActivityMessagesQueue.isEmpty()) {
                TCPResponse map2 = mainActivityMessagesQueue.poll();
                sendToMainActivity(map2);
            }

            if (isLogin) {
                TCPResponse response = new TCPResponse();
                response.event = "loginDone";
                response.isEmpty = true;
                sendToMainActivity(response);

                if(latLng!=null) {
                    response = new TCPResponse();
                    response.latitude = latLng.latitude;
                    response.longitude = latLng.longitude;
                    if (distance > 0 || duration > 0 || cost > 0) {
                        response.distance = distance;
                        response.duration = duration;
                        response.cost = cost;
                    }
                    if (User.getInstance().isDriver()) {
                        response.event = "driverLocation";
                        sendToMainActivity(response);
                    } else if (User.getInstance().isClient()) {
                        response.event = "clientLocation";
                        sendToMainActivity(response);
                    } else {
                        response.event = "myLocation";
                        sendToMainActivity(response);
                    }
                }
            }
        }

    }






    private void addToQueue(JSONObject map){
        Log.i(tag, "addToQueue "+ messageQueue.size()+socketOpened + isLogin );
        try {
            String msgId=UUID.randomUUID().toString();
            String time=dateFormatter.format(new Date());
            map.put("msgId",msgId);
            map.put("time",time);
            map.put("ver",version);
            map.put("lang",lang);
            map.put("auth_id",User.getInstance()._id);


            messageQueue.add(map);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        long d=0;
        if (currentMsgSendTime != null)
            d = (new Date()).getTime() - currentMsgSendTime.getTime();
        if ((d > User.getInstance().config.timeout || currentMessage == null) && !messageQueue.isEmpty() && socketOpened && isLogin)
            sendToServer();
    }

    private synchronized void sendToServer(){

        currentMessage = messageQueue.peek();
        try {
            currentMsgId=currentMessage.getString("msgId");
            currentMsgSendTime=new Date();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (MyApplication.tcpClient != null&&currentMsgId!=null) {
            Log.d(TAG, "Sending: " + currentMessage.toString());
            MyApplication.tcpClient.sendMessage(currentMessage.toString());
        }

    }

    private void connectServer(String lat,String lng){
        Log.i(tag, "connectServer");
        if(server==null) {
            Log.i(tag, "connectServer call server");
            Map<String, String> params = new HashMap<>();
            params.put("lat", lat);
            params.put("lng", lng);
            params.put("type", User.getInstance().type);

            MyVolley myVolley = MyVolley.getInstance(this);
            queue = myVolley.getRequestQueue();
            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                    Constants.nodejs_index_url + "/tcp_server/",
                    GsonResponse.class,
                    params,
                    createGetServerSuccessListener(),
                    createGetServerErrorListener());
            queue.add(myReq);
        }
    }

    //todo:add task to check message timeout, socket io grantee message will be send to server.


    private Response.Listener<GsonResponse> createGetServerSuccessListener() {
        Log.i(tag,"createGetUserSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;
                Log.i(tag, "json response code: "+ response.code);
                Log.i(tag, "json response message: "+ response.message);
                /*Log.i(tag, "maxWidth: "+ response.maxWidth);
                Log.i(tag, "scrollRefresh: "+ response.scrollRefresh);
                Log.i(tag, "messagesRefresh: "+ response.messagesRefresh);*/
                if(response.code.contentEquals("200")&&server==null) {
                    server = response.server;
                    /*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
                    if (response.maxWidth != null &&
                            response.maxWidth!= sharedPref.getInt("maxWidth",0)) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("maxWidth", response.maxWidth);
                        editor.commit();

                    }
                    if (response.scrollRefresh != null &&
                            response.scrollRefresh!= sharedPref.getInt("scrollRefresh",0)) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("scrollRefresh", response.scrollRefresh);
                        editor.commit();

                    }
                    if (response.messagesRefresh != null &&
                            response.messagesRefresh!= sharedPref.getInt("messagesRefresh",0)) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("messagesRefresh", response.messagesRefresh);
                        editor.commit();

                    }*/
                    if(MyApplication.tcpClient!=null) {
                        try {
                            MyApplication.tcpClient.stopClient();
                            MyApplication.tcpClient = new TcpClient();
                            MyApplication.tcpClient.start();
                        }
                        catch (Exception e){

                        }
                    }
                    else{
                        MyApplication.tcpClient = new TcpClient();
                        MyApplication.tcpClient.start();
                    }

                    //server="https://server100.caoutch.com";
                    /*try {
                        IO.Options options= new IO.Options();
                        options.timeout=5000;
                        options.reconnection=true;
                        options.reconnectionDelay=1000;
                        options.reconnectionDelayMax=5000;
                        options.reconnectionAttempts=99999999;
                        mSocket = IO.socket(server, options);
                    } catch (URISyntaxException e) {
                        Log.e(tag,e.getMessage());
                    }
                    mSocket.on( Socket.EVENT_CONNECT,onConnect);
                    mSocket.on( Socket.EVENT_DISCONNECT,onDisconnent);
                    mSocket.on( Socket.EVENT_ERROR,onError);
                    mSocket.on( Socket.EVENT_MESSAGE,onMessage);
                    mSocket.on( Socket.EVENT_CONNECT_ERROR,onConnectError);
                    mSocket.on( Socket.EVENT_CONNECT_TIMEOUT,onConnectTimeout);
                    mSocket.on( Socket.EVENT_RECONNECT,onReconnect);
                    mSocket.on( Socket.EVENT_RECONNECT_ERROR,onReconnectError);
                    mSocket.on( Socket.EVENT_RECONNECT_FAILED,onReconnectFailed);
                    mSocket.on( Socket.EVENT_RECONNECT_ATTEMPT,onReconnectAttempt);
                    mSocket.on( Socket.EVENT_RECONNECTING,onReconnecting);
                    if(User.getInstance().isClient()) {
                        mSocket.on("onNewDriverLocation", onNewDriverLocation);
                        mSocket.on("onOffDriverLocation", onOffDriverLocation);
                        //mSocket.on("onInfoDriverLocation", onInfoDriverLocation);
                        mSocket.on("onDriverConfirmTrip", onDriverConfirmTrip);
                        //mSocket.on("onDriverHasTrip", onDriverHasTrip);
                        mSocket.on("onDriverCancelTrip", onDriverCancelTrip);
                        mSocket.on("onDriverStartTrip", onDriverStartTrip);
                        mSocket.on("onDriverFinishTrip", onDriverFinishTrip);
                    }
                    else if(User.getInstance().isDriver()){
                        mSocket.on("onNewClientLocation",onNewClientLocation);
                        mSocket.on("onOffClientLocation",onOffClientLocation);
                        mSocket.on("onClientCancelTrip", onClientCancelTrip);
                        mSocket.on("onDriverNewTrip", onDriverNewTrip);
                    }
                    mSocket.on("tripMessage", tripMessage);
                    mSocket.connect();*/
                }

            }
        };
    }

    private Response.ErrorListener createGetServerErrorListener() {
        Log.i(tag,"createGetUserErrorListener");
        if(server==null)
            AsyncTask.execute(new Runnable() {


                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                        connectServer(Double.toString(latLng.latitude),Double.toString(latLng.longitude));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
        return new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
            }
        };
    }




    void resetTrip(){
        Log.i(tag,"resetTrip");
        tripSingleton.reset();
        mainActivityMessagesQueue.clear();
        driverTripLocation=null;
        distance=0;
        duration=0;
        cost = 0;
        /*timeout = User.getInstance().config.timeout;
        if(User.getInstance().isDriver()) {
            mLocationRequest.setInterval(User.getInstance().config.interval);
            mLocationRequest.setFastestInterval(User.getInstance().config.fastInterval);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    Looper.getMainLooper());
        }*/
    }



    public synchronized void messageReceived(String message) {
        TCPResponse response=null;
        Log.d(TAG+" messageReceived",message);
        Gson gson = gsonBuilder.create();

        try {
            response = gson.fromJson(message, TCPResponse.class);
        }
        catch (Exception e){

        }
        if(response!=null&&response.event!=null) {
            if (response.event.contentEquals("login"))
                loginResponse(response);
            else if (response.event.contentEquals("onNewDriverLocation"))
                onNewDriverLocation(response);
            else if (response.event.contentEquals("onOffDriverLocation"))
                onOffDriverLocation(response);
            else if (response.event.contentEquals("onDriverConfirmTrip"))
                onDriverConfirmTrip(response);
            else if (response.event.contentEquals("onDriverCancelTrip"))
                onDriverCancelTrip(response);
            else if (response.event.contentEquals("onDriverStartTrip"))
                onDriverStartTrip(response);
            else if (response.event.contentEquals("onDriverFinishTrip"))
                onDriverFinishTrip(response);
            else if (response.event.contentEquals("onNewClientLocation"))
                onNewClientLocation(response);
            else if (response.event.contentEquals("onOffClientLocation"))
                onOffClientLocation(response);
            else if (response.event.contentEquals("onClientCancelTrip"))
                onClientCancelTrip(response);
            else if (response.event.contentEquals("onDriverNewTrip"))
                onDriverNewTrip(response);
            else if (response.event.contentEquals("tripMessage"))
                tripMessage(response);
            else if (response.event.contentEquals("selectedDriver")&&response.trip!=null) {

                tripSingleton.setSingleton(response.trip);
                if (tripSingleton.state.contentEquals(Constants.driverHasTrip)) {
                    response.event="onDriverHasTrip";
                    resetTrip();
                    sendToMainActivity(response);

                }
                else{
                    database.tripDao().insertTrip(tripSingleton.getTrip());
                    saveTripMessage(tripSingleton.updateTime);
                }
            }
            else if (response.event.contentEquals("finishTrip")&&response.trip!=null) {
                tripSingleton.setSingleton(response.trip);
                database.tripDao().insertTripOrUpdate(tripSingleton.getTrip());
                saveTripMessage(tripSingleton.updateTime);
                resetTrip();
                response.event="finishTrip";
                sendToMainActivity(response);
                stopForeground(true);
                stopSelf();



            }
            else if ((response.event.contentEquals("driverConfirmed")||
                    response.event.contentEquals("driverCancel")||
                    response.event.contentEquals("clientCancel")||
                    response.event.contentEquals("startTrip"))
                    &&response.trip!=null) {
                tripSingleton.setSingleton(response.trip);
                database.tripDao().insertTripOrUpdate(tripSingleton.getTrip());
                saveTripMessage(tripSingleton.updateTime);
                if(response.event.contentEquals("driverCancel")||
                        response.event.contentEquals("clientCancel")) {
                    resetTrip();
                }

                /*if(response.event.contentEquals("driverConfirmed")){
                    timeout=User.getInstance().config.timeoutLong;
                    mLocationRequest.setInterval(User.getInstance().config.intervalLong);
                    mLocationRequest.setFastestInterval(User.getInstance().config.fastIntervalLong);
                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            Looper.getMainLooper());
                }*/
            }
            else
                sendToMainActivity( response);
        }


        if(currentMsgId!=null&&response!=null&&response.msgId!=null&&currentMsgId.contentEquals(response.msgId)) {
            messageQueue.remove(currentMessage);
            currentMessage = null;
            currentMsgId=null;

        }

        long d=0;
        if (currentMsgSendTime != null)
            d = (new Date()).getTime() - currentMsgSendTime.getTime();
        if ((d > User.getInstance().config.timeout || currentMessage == null) && !messageQueue.isEmpty() && socketOpened && isLogin)
            sendToServer();


    }


    public class TcpClient extends Thread {

        private String mServerMessage;
        // sends message received notifications
        // while this is true, the server will continue running
        private boolean mRun = false;
        // used to send messages
        private PrintWriter mBufferOut;
        // used to read messages from the server
        private BufferedReader mBufferIn;
        Socket socket;


        /**
         * Constructor of the class. OnMessagedReceived listens for the messages received from server
         */
        public TcpClient()  {

        }

        /**
         * Sends the message entered by client to the server
         *
         * @param message text entered by client
         */
        public synchronized void sendMessage(final String message) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mBufferOut != null) {
                            Log.d(TAG, "Sending: " + message);
                            mBufferOut.println(message);
                            mBufferOut.flush();
                        }
                    }
                    catch (Exception e){
                        Log.e("TCP", "sendMessage", e);
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }

        /**
         * Close the connection and release the members
         */
        public void stopClient() {
            mRun = false;
            isLogin=false;
            MyApplication.tcpClient.interrupt();
            if (mBufferOut != null) {
                mBufferOut.flush();
                mBufferOut.close();
            }

            mBufferIn = null;
            mBufferOut = null;
            mServerMessage = null;

        }

        public void run() {

            /*Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    while (mRun){
                        if(socket!=null){
                            if (mBufferOut != null) {
                                mBufferOut.println("{\"event\":\"test\"}");
                                mBufferOut.flush();
                            }
                            if((new Date()).getTime()-lastTest.getTime()<5000){
                                Log.d("TCP Client", "C: Connected...");
                            }
                            else{
                                Log.d("TCP Client", "C: Disconnected...");

                                try {
                                    socket.setSoTimeout(1000);
                                }
                                catch (Exception e){
                                    Log.e("TCP", "closing", e);
                                }
                            }
                        }
                        try {
                            sleep(2500);
                        }
                        catch (Exception e){

                        }
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();*/

            mRun = true;
            while(mRun) {
                try {
                    //here you must put your computer's IP address.
                    InetAddress serverAddr = InetAddress.getByName(server.ip);

                    Log.d("TCP Client", "C: Connecting...");

                    socket =factory.createSocket(serverAddr, server.port);
                    if(!server.ip.contentEquals("10.0.2.2"))
                        ((SSLSocket)socket).startHandshake();
                    //create a socket to make the connection with the server
                    //socket = new Socket(serverAddr, server.port);
                    socket.setKeepAlive(true);
                    socket.setSoTimeout(User.getInstance().config.timeout);


                    //sends the message to the server
                    mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    //receives the message which the server sends back
                    //mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    onConnect();

                    //in this while the client listens for the messages sent by the server
                    StringBuilder stringBuilder= new StringBuilder();
                    int t=0;
                    while (mRun) {
                        /*try {
                            mServerMessage = mBufferIn.readLine();
                            //Log.d("RmServerMessage",mServerMessage);
                            if (mServerMessage != null) {
                                //call the method messageReceived from MyActivity class
                                messageReceived(mServerMessage);
                                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
                            }
                        }
                        catch (Exception e){
                            Log.e(tag, "Read: Error", e);
                        }*/

                        try {
                            int i = socket.getInputStream().read();
                            if (i == 10) {
                                mServerMessage = stringBuilder.toString();
                                messageReceived(mServerMessage);
                                //Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
                                stringBuilder = new StringBuilder();
                            }
                            else if (i > 0) {
                                stringBuilder.append((char) i);
                            }
                            else {
                                Log.d("RESPONSE FROM SERVER", "End of stream " + i);
                                break;
                            }
                            t=0;
                            /*if(socket.getSoTimeout()!=timeout)
                                socket.setSoTimeout(timeout);*/
                        }
                        catch (SocketTimeoutException e ){
                            //if(socket.getInputStream().available()==0)
                            //    break;
                            Log.e(tag, "Read: Error"+socket.getInputStream().available(), e);
                            stringBuilder = new StringBuilder();
                            if(t>1)
                                break;
                            if(isLogin&&mRun) {
                                sendLocationToServer();
                                //MyApplication.tcpClient.sendMessage("{}");
                            }
                            t++;


                        }

                        //Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + (char)i + "'");
                    }
                    //onDisconnent();

                }
                catch (Exception e) {
                    Log.e(tag, "Open: Error", e);
                }
                finally {
                    //the socket must be closed. It is not possible to reconnect to this socket
                    // after it is closed, which means a new socket instance has to be created.
                    try{
                        socket.close();
                    } catch (Exception e) {
                        Log.e(tag, "Close: Error", e);

                    }
                }
                onDisconnent();
                try{
                    sleep(2000);
                }
                catch (Exception e){

                }
            }

        }

        //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
        //class at on AsyncTask doInBackground


    }


}

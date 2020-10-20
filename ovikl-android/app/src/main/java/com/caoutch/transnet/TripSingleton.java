package com.caoutch.transnet;

import android.util.Log;

import com.caoutch.transnet.database.Trip;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by caoutch on 4/21/2018.
 */

public class TripSingleton {
    private SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private transient static TripSingleton mInstance;
    private String TAG="TripSingleton";


    public String _id;
    public Date createTime;
    public Date updateTime;
    //public Date startTime;
    //public Date endTime;
    public String state;
    public String clientId;
    public String driverId;
    public Double clientLat;
    public Double clientLng;
    public Double driverLat;
    public Double driverLng;
    public Double prMin;
    public Double prBase;
    public Double prKM;
    public Double prMinute;
    //public Double prLngKM;
    //public Double prLngMinute;
    //public Double lngKM;
    public String cancelledBy;
    public String cur;
    public Double distance;
    public Double duration;
    public Double cost;
    public String msgId;


    private String lang;

    private TripSingleton(){
    }
    public static synchronized TripSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new TripSingleton();

            Log.i("TripSingleton", "new instance");

        }
        else
            Log.i("TripSingleton", "old instance");
        return mInstance;
    }

    public JSONObject getMap(){
        JSONObject map = new JSONObject();
        try {
            map.put("_id",_id);
            /*if(createTime!=null)
                map.put("createTime",dateFormatter.format(createTime));
            if(updateTime!=null)
                map.put("updateTime",dateFormatter.format(updateTime));
            if(startTime!=null)
                map.put("startTime",dateFormatter.format(startTime));
            if(endTime!=null)
                map.put("endTime",dateFormatter.format(endTime));*/
            if(state!=null)
                map.put("state",state);
            if(clientId!=null)
                map.put("clientId",clientId);
            if(driverId!=null)
                map.put("driverId",driverId);
            if(clientLat!=null)
                map.put("clientLat",clientLat.toString());
            if(clientLng!=null)
                map.put("clientLng",clientLng.toString());
            if(driverLat!=null)
                map.put("driverLat",driverLat.toString());
            if(driverLng!=null)
                map.put("driverLng",driverLng.toString());
            /*if(prMin!=null)
                map.put("prMin",prMin.toString());
            if(prBase!=null)
                map.put("prBase",prBase.toString());
            if(prKM!=null)
                map.put("prKM",prKM.toString());
            if(prMinute!=null)
                map.put("prMinute",prMinute.toString());
            if(prLngKM!=null)
                map.put("prLngKM",prLngKM.toString());
            if(prLngMinute!=null)
                map.put("prLngMinute",prLngMinute.toString());
            if(lngKM!=null)
                map.put("lngKM",lngKM.toString());*/
            if(cur!=null)
                map.put("cur",cur);
            if(cancelledBy!=null)
                map.put("cancelledBy",cancelledBy);
            if(distance!=null)
                map.put("distance",distance);
            if(duration!=null)
                map.put("duration",duration);
            if(cost!=null)
                map.put("cost",cost);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }



    public void setJson(JSONObject tripJson){
        try {
            if(lang==null)
                lang= Locale.getDefault().getLanguage();
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            if(tripJson.has("msgId"))
                msgId=tripJson.getString("msgId");
            if(tripJson.has("_id"))
                _id=tripJson.getString("_id");
            if(tripJson.has("createTime"))
                createTime=dateFormatter.parse(tripJson.getString("createTime"));
            if(tripJson.has("updateTime"))
                updateTime=dateFormatter.parse(tripJson.getString("updateTime"));
            /*if(tripJson.has("startTime"))
                startTime=dateFormatter.parse(tripJson.getString("startTime"));
            if(tripJson.has("endTime"))
                endTime=dateFormatter.parse(tripJson.getString("endTime"));*/
            if(tripJson.has("state"))
                state=tripJson.getString("state");
            if(tripJson.has("clientId"))
                clientId=tripJson.getString("clientId");
            if(tripJson.has("driverId"))
                driverId=tripJson.getString("driverId");
            if(tripJson.has("clientLat"))
                clientLat=tripJson.getDouble("clientLat");
            if(tripJson.has("clientLng"))
                clientLng=tripJson.getDouble("clientLng");
            if(tripJson.has("driverLat"))
                driverLat=tripJson.getDouble("driverLat");
            if(tripJson.has("driverLng"))
                driverLng=tripJson.getDouble("driverLng");
            if(tripJson.has("prMin"))
                prMin=tripJson.getDouble("prMin");
            if(tripJson.has("prBase"))
                prBase=tripJson.getDouble("prBase");
            if(tripJson.has("prKM"))
                prKM=tripJson.getDouble("prKM");
            if(tripJson.has("prMinute"))
                prMinute=tripJson.getDouble("prMinute");
            /*if(tripJson.has("prLngKM"))
                prLngKM=tripJson.getDouble("prLngKM");
            if(tripJson.has("prLngMinute"))
                prLngMinute=tripJson.getDouble("prLngMinute");
            if(tripJson.has("lngKM"))
                lngKM=tripJson.getDouble("lngKM");*/
            if(tripJson.has(lang+"cur"))
                cur=tripJson.getString(lang+"cur");
            else if(tripJson.has("cur"))
                cur=tripJson.getString("cur");

            if(tripJson.has("cancelledBy"))
                cancelledBy=tripJson.getString("cancelledBy");
            if(tripJson.has("distance"))
                distance=tripJson.getDouble("distance");
            if(tripJson.has("duration"))
                duration=tripJson.getDouble("duration");
            if(tripJson.has("cost"))
                cost=tripJson.getDouble("cost");

        } catch (JSONException e) {
            Log.e("",e.getMessage());
        } catch (ParseException e) {
            Log.e("",e.getMessage());
        }
    }

    public void reset(){
        if (mInstance != null) {
            mInstance._id= null;
            mInstance.createTime= null;
            mInstance.updateTime= null;
            //mInstance.startTime= null;
            //mInstance.endTime= null;
            mInstance.state= null;
            mInstance.clientId= null;
            mInstance.driverId= null;
            mInstance.clientLat= null;
            mInstance.clientLng= null;
            mInstance.driverLat= null;
            mInstance.driverLng= null;
            mInstance.prMin= null;
            mInstance.prBase= null;
            mInstance.prKM= null;
            mInstance.prMinute= null;
            /*mInstance.prLngKM= null;
            mInstance.prLngMinute= null;
            mInstance.lngKM= null;*/
            mInstance.cur= null;
            mInstance.cancelledBy= null;
            mInstance.distance= null;
            mInstance.duration= null;
            mInstance.cost= null;
            mInstance.msgId= null;

        }
    }

    public Trip getTrip(){

        Trip t = new Trip();
        t._id= _id;
        t.createTime= createTime;
        t.updateTime= updateTime;
        //t.startTime= startTime;
        //t.endTime= endTime;
        t.state= state;
        t.clientId= clientId;
        t.driverId= driverId;
        t.clientLat= clientLat;
        t.clientLng= clientLng;
        t.driverLat= driverLat;
        t.driverLng= driverLng;
        t.prMin= prMin;
        t.prBase= prBase;
        t.prKM= prKM;
        t.prMinute= prMinute;
        /*t.prLngKM= prLngKM;
        t.prLngMinute= prLngMinute;
        t.lngKM= lngKM;*/
        t.cur= cur;
        t.cancelledBy= cancelledBy;
        t.distance= distance;
        t.duration=duration;
        t.cost=cost;
        t.msgId=msgId;

        return t;
    }


    public void setSingleton(Trip trip){
        if(trip._id!=null)
            _id= trip._id;
        if(trip.createTime!=null)
            createTime= trip.createTime;
        if(trip.updateTime!=null)
            updateTime= trip.updateTime;
        //t.startTime= startTime;
        //t.endTime= endTime;
        if(trip.state!=null)
            state= trip.state;
        if(trip.clientId!=null)
            clientId= trip.clientId;
        if(trip.driverId!=null)
            driverId= trip.driverId;
        if(trip.clientLat!=null)
            clientLat= trip.clientLat;
        if(trip.clientLng!=null)
            clientLng= trip.clientLng;
        if(trip.driverLat!=null)
            driverLat= trip.driverLat;
        if(trip.driverLng!=null)
            driverLng= trip.driverLng;
        if(trip.prMin!=null)
            prMin= trip.prMin;
        if(trip.prBase!=null)
            prBase= trip.prBase;
        if(trip.prKM!=null)
            prKM= trip.prKM;
        if(trip.prMinute!=null)
            prMinute= trip.prMinute;
        /*prLngKM= trip.prLngKM;
        prLngMinute= trip.prLngMinute;
        lngKM= trip.lngKM;*/
        if(trip.cur!=null)
            cur= trip.cur;
        if(trip.cancelledBy!=null)
            cancelledBy= trip.cancelledBy;
        if(trip.distance!=null)
            distance= trip.distance;
        if(trip.distance!=null)
            distance=trip.distance;
        if(trip.cost!=null)
            cost=trip.cost;
        if(trip.msgId!=null)
            msgId=trip.msgId;
    }

    /*public void save(final AppDatabase database){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TripMessage message = new TripMessage();
                message.tripId = _id;
                message.state = state;

                message.message = getString(R.string.driver_cancel_trip);

                message.senderId=driverId;
                message.msgId = msgId;
                //message._id = _id;
                message.createTime = updateTime;
                database.tripMessageDao().insertTripMessageOrIgnore(message);
            }
        });
    }*/
}

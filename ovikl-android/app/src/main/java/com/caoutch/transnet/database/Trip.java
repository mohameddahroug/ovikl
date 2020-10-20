package com.caoutch.transnet.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.annotation.NonNull;

import com.caoutch.transnet.User;

import java.util.ArrayList;
import java.util.Date;

@Entity(tableName = "trip",primaryKeys={"_id","state"},indices = {@Index("_id"),@Index("updateTime"),@Index("clientId"),@Index("driverId")})
public class Trip {

    @NonNull
    public String _id;
    public Date createTime;
    public Date updateTime;
    //public Date startTime;
    //public Date endTime;
    @NonNull
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
    public Double prLngKM;
    public Double prLngMinute;
    public Double lngKM;
    public String cancelledBy;
    public String cur;
    public Double distance;
    public Double duration;
    public Double cost;
    public Double driverRate;
    public Double carRate;
    public String driverClaim;
    public Double clientRate;
    public String clientClaim;
    public String msgId;
    @Ignore
    public User client;
    @Ignore
    public User driver;
    @Ignore
    public Trip PENDING;
    @Ignore
    public Trip RESERVED;
    @Ignore
    public Trip STARTED;
    @Ignore
    public Trip FINISHED;
    @Ignore
    public Trip CANCELED;

    @Ignore
    public ArrayList<Location> locations=new ArrayList<>();

    @Ignore
    public String zone;

    public Trip(){

    }


    public Trip clone(){
        Trip trip = new Trip();
        trip._id=_id;
        trip.createTime=createTime;
        trip.updateTime=updateTime;
        trip.state=state;
        trip.clientId=clientId;
        trip.driverId=driverId;
        trip.clientLat=clientLat;
        trip.clientLng=clientLng;
        trip.driverLat=driverLat;
        trip.driverLng=driverLng;
        trip.prMin=prMin;
        trip.prBase=prBase;
        trip.prKM=prKM;
        trip.prMinute=prMinute;
        trip.prLngKM=prLngKM;
        trip.prLngMinute=prLngMinute;
        trip.lngKM=lngKM;
        trip.cancelledBy=cancelledBy;
        trip.cur=cur;
        trip.distance=distance;
        trip.duration=duration;
        trip.cost=cost;
        trip.driverRate=driverRate;
        trip.carRate=carRate;
        trip.driverClaim=driverClaim;
        trip.clientRate=clientRate;
        trip.clientClaim=clientClaim;
        trip.msgId=msgId;

        return trip;
    }


    /*public void setJson(JSONObject tripJson){
         SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss.SSS'Z'");

        try {
            _id=tripJson.getString("_id");
            if(tripJson.has("createTime"))
                createTime=dateFormatter.parse(tripJson.getString("createTime"));
            if(tripJson.has("updateTime"))
                updateTime=dateFormatter.parse(tripJson.getString("updateTime"));
            *//*if(tripJson.has("startTime"))
                startTime=dateFormatter.parse(tripJson.getString("startTime"));
            if(tripJson.has("endTime"))
                endTime=dateFormatter.parse(tripJson.getString("endTime"));*//*
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
            if(tripJson.has("prLngKM"))
                prLngKM=tripJson.getDouble("prLngKM");
            if(tripJson.has("prLngMinute"))
                prLngMinute=tripJson.getDouble("prLngMinute");
            if(tripJson.has("lngKM"))
                lngKM=tripJson.getDouble("lngKM");
            if(tripJson.has("cur"))
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
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }*/
    public class Location{
        public String i;

        public String state;

        public double latitude;

        public double longitude;

        public String time;

        public double duration;

        public double distance;
    }
}

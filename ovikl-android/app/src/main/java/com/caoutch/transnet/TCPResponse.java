package com.caoutch.transnet;

import com.caoutch.transnet.database.Trip;
import com.caoutch.transnet.database.TripMessage;

import java.util.ArrayList;
import java.util.Map;

public class TCPResponse implements java.io.Serializable{
    public String event;
    public Boolean isEmpty=false;

    public User user;
    //public User client;
    //public User driver;
    public Boolean retry=false;
    public Trip trip;
    public TripMessage tripMessage;
    public ArrayList<Trip> tripArr;
    public ArrayList<TripMessage> tripMessageArr;
    public Location driverLocation;


    public String _id;
    public String type;
    public Double latitude;
    public Double longitude;
    public Double distance;
    public Double duration;
    public Double cost;
    public String msgId;
    public String carType;
    public String clientId;
    public String driverId;
    public String tripId;
    class Location{

        Double latitude;
        Double longitude;
    }



}

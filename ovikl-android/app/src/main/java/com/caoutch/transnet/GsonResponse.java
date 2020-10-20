package com.caoutch.transnet;

import com.caoutch.transnet.database.Trip;

import java.util.ArrayList;

public class GsonResponse {


    public String code;
    public String message;
    public User user;
    public ArrayList<User> users;
    public ArrayList<Trip> trips;
    public ArrayList<Message> messages;
    public ArrayList<Price> prices;
    public Message newMessage;
    //public Integer maxWidth;
    //public Integer scrollRefresh;
    //public Integer messagesRefresh;
    public ArrayList<Vehicle> vehicles;
    //public ArrayList<VehicleImages> vehiclesImages;
    public Config config;
    public Zone zone;

    public Server server;
    public class Message{
        public String _id;
        public String message;
        public String createTime;
    }
    public class Price{
        public String type;
        public String typeAr;
        public Double prMin;
        public Double prBase;
        public Double prKM;
        public Double prMinute;
        public Double prLngKM;
        public Double prLngMinute;
        public Double lngKM;
        public String cur;
    }
    public class Vehicle{
        public String type;
        public String name;
        public String image;
        public String pointer;
        public String selectedPointer;
    }
    public class Server{
        public String ip;
        public int port;
    }

    public class Zone{
        public String zone;
        public String email;
        public String mobile;
    }


//    public class VehicleImages{
//        public String type;
//        public String image;
//        public String pointer;
//        public String selectedPointer;
//    }

    public static class Config{
        public int minAndroidVersion=0;
        public int minIOSVersion=0;
        public int timeout=5000;
        //public int timeoutLong=10000;
        public int interval=10000;
        public int fastInterval=3000;
        //public int intervalLong=15000;
        //public int fastIntervalLong=10000;
        public int smallImage=100;
        public int largeImage=500;
    }
}

package com.caoutch.transnet.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TripDao {



    @Query("SELECT * FROM trip WHERE _id = :tripId  and (clientId=:userId or driverId=:userId) order by updateTime")
    List<Trip>  findTrip(String tripId, String userId);

    @Query("SELECT * FROM trip WHERE _id = :tripId and state=:state  and (clientId=:userId or driverId=:userId)")
    Trip findTrip(String tripId, String state, String userId);

    @Query("SELECT * FROM trip WHERE (state='FINISHED' or state='CANCELED') and (clientId=:userId or driverId=:userId) order by _id desc limit 1")
    Trip findLastFinishedTrip(String userId);

    @Query("SELECT * FROM trip WHERE (state='FINISHED' or state='CANCELED') and (clientId=:userId or driverId=:userId) order by _id desc limit 1 offset :index")
    Trip findFinishedTrip(int index, String userId);


    @Query("SELECT * FROM trip where clientId=:userId or driverId=:userId order by updateTime desc limit 1")
    Trip findLastTrip(String userId);

    @Query("SELECT * FROM trip WHERE state='PENDING' and (clientId=:userId or driverId=:userId) order by _id  desc limit 20")
    List<Trip> findLastTrips(String userId);

    @Insert
    void insertTrip(Trip trip);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTripOrIgnore(Trip trip);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTripOrUpdate(Trip trip);

    @Delete
    void delete(Trip trip);

    @Update
    void updateTrip(Trip trip);



}

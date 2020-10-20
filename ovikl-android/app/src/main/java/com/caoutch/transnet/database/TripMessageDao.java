package com.caoutch.transnet.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TripMessageDao {

    @Query("SELECT * FROM trip_message where tripId=:tripId and state=:state order by id")
    List<TripMessage> getTripMessage(String tripId, String state);

    @Query("SELECT * FROM trip_message where tripId=:tripId order by id")
    List<TripMessage> getTripMessage(String tripId);

    @Query("SELECT * FROM trip_message where tripId=:tripId and senderId=:senderId and _id=:id")
    TripMessage getTripMessage(String tripId, String senderId, String id);

    @Insert
    void insertTripMessage(TripMessage message);

    @Delete
    void deleteTripMessage(TripMessage message);

    @Update
    public void updateTripMessage(TripMessage message);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTripMessageOrIgnore(TripMessage message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTripMessageOrUpdate(TripMessage message);

}

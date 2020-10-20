package com.caoutch.transnet.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TripLocationDao {

    @Query("SELECT * FROM trip_location where _id=:tripId")
    List<TripLocation> getTripLocations(String tripId);

    @Query("SELECT * FROM trip_location where _id=:tripId and state=:state order by id")
    List<TripLocation> getTripLocations(String tripId, String state);

    @Query("SELECT * FROM trip_location where _id=:tripId order by duration desc limit 1")
    TripLocation getLastTripLocation(String tripId);

    @Insert
    void insertTripLocation(TripLocation location);

    @Delete
    void deleteTripLocation(TripLocation location);

    @Update
    public void updateTripLocation(TripLocation location);
}

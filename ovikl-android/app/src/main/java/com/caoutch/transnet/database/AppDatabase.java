package com.caoutch.transnet.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.caoutch.transnet.Converters;

/**
 * Created by caoutch on 3/23/2018.
 */

@Database(entities = {Trip.class, TripLocation.class, Message.class, TripMessage.class,Image.class}, version = 8,exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TripDao tripDao();
    public abstract TripLocationDao tripLocationDao();
    public abstract MessageDao MessageDao();
    public abstract TripMessageDao tripMessageDao();
    public abstract ImageDao imageDao();
}
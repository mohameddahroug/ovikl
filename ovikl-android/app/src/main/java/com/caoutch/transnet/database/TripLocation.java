package com.caoutch.transnet.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
@Entity(tableName = "trip_location",indices = { @Index("_id"), @Index(value = {"_id", "state"})})
public class TripLocation {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "_id")
    public String _id;

    @ColumnInfo(name = "state")
    public String state;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "time")
    public Date time;

    @ColumnInfo(name = "duration")
    public double duration;

    @ColumnInfo(name = "distance")
    public double distance;

}

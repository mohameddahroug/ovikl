package com.caoutch.transnet.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
@Entity(tableName = "trip_message",indices = { @Index("tripId"),@Index(value = {"tripId", "state", "msgId"},unique = true)})
public class TripMessage {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String _id;

    public String tripId;

    public String state;

    public String message;

    public Date createTime;

    public String senderId;

    public String msgId;

}

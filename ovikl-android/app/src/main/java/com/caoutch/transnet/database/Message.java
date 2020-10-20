package com.caoutch.transnet.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "message",indices = {@Index("_id"),@Index("userId"),@Index("createTime")})
public class Message {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String message="";

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] image;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] smallImage;


    public Boolean sender;

    public Boolean sent=false;

    public String _id;

    public Date createTime;

    public String userId;

    public String type;
}

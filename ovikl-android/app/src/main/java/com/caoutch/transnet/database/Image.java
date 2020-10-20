package com.caoutch.transnet.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Date;
@Entity(tableName = "image")
public class Image {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] image;

    public Date createTime;
}
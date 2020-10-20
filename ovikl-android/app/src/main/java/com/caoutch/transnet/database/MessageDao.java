package com.caoutch.transnet.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message where userId=:userId order by createTime desc limit 50")
    List<Message> getMessage(String userId);

    @Query("SELECT * FROM message where userId=:userId limit 1 OFFSET :position")
    Message getMessage(String userId, int position);

    @Query("SELECT count(1) FROM message where userId=:userId")
    int countMessage(String userId);

    @Query("SELECT max(_id) FROM message where userId=:userId")
    String getMaxMessageId(String userId);

    @Insert
    void insertMessage(Message message);

    @Delete
    void deleteMessage(Message message);

    @Update
    public void updateMessage(Message message);
}

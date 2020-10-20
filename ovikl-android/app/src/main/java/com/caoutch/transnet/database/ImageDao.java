package com.caoutch.transnet.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ImageDao {

    @Query("SELECT * FROM image where id=:id limit 1" )
    Image getImage(String id);

    @Query("SELECT count(1) FROM image where id=:id limit 1" )
    int isCached(String id);

    @Insert
    void insertImage(Image message);

    @Delete
    void deleteImage(Image message);

    @Update
    public void updateImage(Image message);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertImageOrIgnore(Image message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImageOrUpdate(Image message);
}

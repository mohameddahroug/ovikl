package com.caoutch.transnet;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.database.Image;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class GetFile extends AsyncTask<String, Void, Bitmap > {
    private String tag="GetFile";
    Callback callback;

    private User user;
    private AppDatabase database;


    public GetFile(AppDatabase database,Callback callback){
        this.database=database;
        this.callback=callback;
    }


    private GetFile(){

    }


    public Bitmap getImage(String s) {
        try {
            Bitmap bitmap=null;
            if (database.imageDao().isCached(s) == 0) {
                Log.i(tag,"get "+s);
                URL url = new URL(Constants.url + s);
                InputStream inputStream = (InputStream) url.getContent();
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] bytes = byteBuffer.toByteArray();


                Image image = new Image();
                image.createTime = new Date();
                image.id = s;
                image.image = bytes;
                database.imageDao().insertImageOrIgnore(image);
                bitmap = BitmapFactory.decodeByteArray(image.image, 0, image.image.length);
                Log.i(tag,"cannot get "+s);
            } else {
                Image image = database.imageDao().getImage(s);
                bitmap = BitmapFactory.decodeByteArray(image.image, 0, image.image.length);
                Log.i(tag,s+"already cached");
            }
            return bitmap;
        }
        catch(Exception e){
            Log.e(tag,e.getMessage());
        }
        return null;
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        callback.callback(bitmap);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {

        return getImage(strings[0]);
    }


    public interface Callback{
        void callback(Bitmap bitmap);
    }

}





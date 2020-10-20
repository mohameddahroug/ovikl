package com.caoutch.transnet;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

public class UploadFile extends AsyncTask<UploadFile.File, Void, ArrayList<String> > {
    private String tag="UploadFile";
    Callback callback;

    // Random number generator
    private final Random mGenerator = new Random();
    private User user;
    private AppDatabase database;

    int maxWidth=500;
    int maxSmallWidth=100;


    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 32;
    int counter=1;


    byte[] bytes=  null;
    byte[] bytesSmall=  null;
    Bitmap bitmapImage;
    String imagetype;

    public UploadFile(){

    }


    public ArrayList uploadImage() {
        URL url;
        ArrayList<String> arr = new ArrayList<String>();
        try {
            url = new URL(Constants.nodejs_index_url + "/upload_image");
    }
        catch(Exception e){
            return arr;
        }

            Image image = new Image();
            Image imageSmall = new Image();
            JSONObject json = uploadMessage(bitmapImage,url);
            image.image=bytes;
            image.createTime=new Date();
            imageSmall.image=bytesSmall;
            imageSmall.createTime=new Date();
            try {
                if (json.has("image0")) {
                    image.id = json.getString("image0");
                    database.imageDao().insertImageOrUpdate(image);
                    arr.add(0,image.id);
                }
                if (json.has("image1")) {
                    imageSmall.id = json.getString("image1");
                    database.imageDao().insertImageOrUpdate(imageSmall);
                    arr.add(1,imageSmall.id);
                }
            }
            catch(Exception e){
                e.printStackTrace();
                Log.e(tag, "Exception : "
                        + e.getMessage(), e);
            }



            /*mBuilder.setProgress(listUri.size(),i+1,false);
            mBuilder.setContentText("uploading");
            notificationManagerCompat.notify(serviceId, mBuilder.build());*/

        return arr;
    }

    private JSONObject uploadFile(InputStream fileInputStream, InputStream fileInputStream2, final int size, URL url) {


        //final String fileName = sourceFileUri;


        JSONObject json2=null;

        //File sourceFile = new File(sourceFileUri);
        int serverResponseCode=0;

        if (fileInputStream == null ) {

            //dialog.dismiss();

            Log.e(tag, "Source File not exist " );
            return null;

        }
        else
        {
            try {



                // open a URL connection to the Servlet
                //Log.i(TAG,sourceFileUri.toString());
                //File file = new File(sourceFileUri.toString());
                //FileInputStream fileInputStream = new FileInputStream(file);
                //FileInputStream fileInputStream = new FileInputStream(sourceFileUri.getPath());
                //InputStream fileInputStream = getContentResolver().openInputStream(sourceFileUri.getPath());

                /*URL url = new URL(MyApplication.getAppContext().Constants.nodejs_index_url+"/upload");
                if(imageName!=null){
                    url = new URL(MyApplication.getAppContext().Constants.nodejs_index_url+"/upload_user_image");
                }*/

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                //conn.setRequestProperty("userId", "1111111");
                //conn.setRequestProperty("image", "image.jpg");

                dos = new DataOutputStream(conn.getOutputStream());

                addParameter("_id",user._id);

                addFile(fileInputStream);
                addFile(fileInputStream2);

                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                dos.close();

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                //String serverResponseContent = conn.getContent();


                Log.i(tag, "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){
                    //Toast.makeText(PersonalDataActivity.this, "File Upload Complete.",
                    //        Toast.LENGTH_SHORT).show();
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String response = readStream(in);
                    Log.i(tag,"Response:"+response);
                    JSONObject json = new JSONObject(response);

                    if (json.has("message")&&json.has("code")&&json.getInt("code") == 200) {
                        json2 = new JSONObject(json.getString("message"));

                        /*mAdapter.count++;
                        mAdapter.notifyItemInserted(mAdapter.count-1);
                        scroll=1;
                        if(mAdapter.count>0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRecyclerView.scrollToPosition(mAdapter.count - 1);
                                }
                            });
                        }*/
                    }
                    //User.getInstance(RegisterActivity).profileImageUri=reader.getString("message");
                }

                //close the streams //
                fileInputStream2.close();

                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBarUpload.setProgress(0);
                        progressBarUpload.setVisibility(View.INVISIBLE);
                    }
                });*/


            } catch (MalformedURLException ex) {

                //dialog.dismiss();
                ex.printStackTrace();

                //messageText.setText("MalformedURLException Exception : check script url.");
                //Toast.makeText(PersonalDataActivity.this, "MalformedURLException",
                //       Toast.LENGTH_SHORT).show();

                Log.e(tag, "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //dialog.dismiss();
                e.printStackTrace();
                //Toast.makeText(PersonalDataActivity.this, "Got Exception : see logcat ",
                //Toast.LENGTH_SHORT).show();

                Log.e(tag, "Exception : "
                        + e.getMessage(), e);
            }
            //dialog.dismiss();
            return json2;

        } // End else block
    }


    private JSONObject  uploadMessage(Bitmap bitmapImage,URL url){


            //main Image


            ByteArrayOutputStream fos = null;

            int width = bitmapImage.getWidth();
            int height = bitmapImage.getHeight();
            if(width>maxWidth || height>maxWidth) {
                Matrix matrix = new Matrix();
                float scale=1;
                if(width>=height)
                    scale = ((float) maxWidth) / width;
                else
                    scale = ((float) maxWidth) / height;
                int size = bitmapImage.getByteCount();
                Log.i(tag, "image size " + size);
                Log.i(tag, "image width " + width);
                Log.i(tag, "image height " + height);
                //float scaleHeight = ((float) MAX_HEIGHT) / height;

                Log.i(tag, "image compress scale " + scale);
                matrix.postScale(scale, scale);
                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, width, height, matrix, true);
            }
            Log.i(tag,"image width " +bitmapImage.getWidth());
            Log.i(tag,"image height " +bitmapImage.getHeight());
            try {
                fos = new ByteArrayOutputStream();
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                bytes=fos.toByteArray();
                Log.i(tag,"image size after compress " +bytes.length);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            //small Image
            fos = null;

            width = bitmapImage.getWidth();
            height = bitmapImage.getHeight();
            if(width>maxSmallWidth || height>maxSmallWidth) {
                Matrix matrix = new Matrix();
                float scale=1;
                if(width>=height)
                    scale = ((float) maxSmallWidth) / width;
                else
                    scale = ((float) maxSmallWidth) / height;
                int size = bitmapImage.getByteCount();
                Log.i(tag, "image size " + size);
                Log.i(tag, "image width " + width);
                Log.i(tag, "image height " + height);
                //float scaleHeight = ((float) MAX_HEIGHT) / height;

                Log.i(tag, "image compress scale " + scale);
                matrix.postScale(scale, scale);
                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, width, height, matrix, true);
            }
            Log.i(tag,"image width " +bitmapImage.getWidth());
            Log.i(tag,"image height " +bitmapImage.getHeight());
            try {
                fos = new ByteArrayOutputStream();
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                bytesSmall=fos.toByteArray();
                Log.i(tag,"image size after compress " +bytes.length);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //progressBarUpload.setProgress(5);

            //showMessage(message);
            return uploadFile(new ByteArrayInputStream(bytes),new ByteArrayInputStream(bytesSmall),bytes.length,url);



    }

    private void addParameter(String parameter,String value){
        try {
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\""+parameter+"\"" + lineEnd + lineEnd);
            dos.writeBytes(value + lineEnd);
        }
        catch (Exception e){

        }
    }
    private void addFile(InputStream fileInputStream){
        try {

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\""
                    + "profile.jpg" + "\"; imageName=\"image\"" + lineEnd);

            dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                counter++;
                //Log.i(tag, "progressBarUpload setProgress counter" + counter + " maxBufferSize " + maxBufferSize + " size " + size);
                final int progressValue = counter * maxBufferSize;

                    /*if(progressValue<size)
                        mBuilder.setProgress(size,progressValue,true);
                    else
                        mBuilder.setProgress(100,95,true);*/

                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(progressValue<size)
                                progressBarUpload.setProgress((progressValue/size));

                            else
                                progressBarUpload.setProgress(95);
                        }
                    });*/

            }
            dos.writeBytes(lineEnd);
        }
        catch (Exception e){

        }
    }

    private String readStream(InputStream inputStream){
        try {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(inputStream, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();
        }
        catch (UnsupportedEncodingException e){
            return "";
        }
        catch (IOException e){
            return "";
        }

    }


    @Override
    protected void onPostExecute(ArrayList<String>  arr) {
        super.onPostExecute(arr);
        callback.callback(arr,imagetype);
    }

    @Override
    protected ArrayList<String> doInBackground(File... files) {

        this.database=files[0].database;
        this.bitmapImage=files[0].bitmapImage;
        this.imagetype=files[0].imagetype;
        this.callback=files[0].callback;


        user = User.getInstance();
        if(user.config!=null){
            maxSmallWidth=user.config.smallImage;
            maxWidth=user.config.largeImage;
        }
        return uploadImage();
    }

    public static class File{
        String imagetype;
        Bitmap bitmapImage;
        AppDatabase database;
        Callback callback;

        public File(String imageType,Bitmap bitmapImage,AppDatabase database,Callback callback){
            this.imagetype=imageType;
            this.bitmapImage=bitmapImage;
            this.database=database;
            this.callback=callback;

        }
    }
}





package com.caoutch.transnet.activity;

import androidx.annotation.NonNull;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.Constants;
import com.caoutch.transnet.GsonRequest;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.R;
import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.User;
import com.caoutch.transnet.GsonResponse;
import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.database.Trip;
import com.caoutch.transnet.database.TripLocation;
import com.caoutch.transnet.database.TripMessage;
import com.caoutch.transnet.view.LoadingRelativeLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class LastTripActivity extends SuperFragment implements OnMapReadyCallback {
    AppDatabase database;
    String _id;
    List<Trip> trips;
    Trip tripFinished;
    //Trip tripStarted;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    PolylineOptions polylineOptionsReserved= new PolylineOptions();

    PolylineOptions polylineOptionsStarted = new PolylineOptions();

    TextView costTextView;
    RatingBar ratingBar;
    RatingBar carRatingBar;
    TextView carTextView;
    TextView rateTextView;
    EditText claimEditText;
    ScrollView chatScrollView;
    LinearLayout chatLinearLayout;
    Button previousButton;
    Button moreDetailsButton;
    Button nextButton;
    User user;
    private RequestQueue queue ;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.ENGLISH);
    DateFormat df2 = new SimpleDateFormat("HH:mm:ss",Locale.ENGLISH);
    LinearLayout.LayoutParams p1;
    LinearLayout.LayoutParams p2;
    LinearLayout.LayoutParams p3;
    List<TripMessage> messages;
    public LoadingRelativeLayout loadingRelativeLayout;
    int index=0;
    View root;

    @Override
    public void onAttach(@NonNull Context context) {
        tag= "LastTripActivity";
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        root = inflater.inflate(R.layout.activity_last_trip, container, false);
        user= User.getInstance();
        tag=tag+"_"+user.type;
        // add back arrow to toolbar
        //df.setTimeZone(TimeZone.getDefault());
        //df2.setTimeZone(TimeZone.getDefault());
        Log.i("TimeZone", TimeZone.getDefault().getDisplayName());
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));


        // prepare map
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.lastTripMap);
        mapFragment.getMapAsync(this);
        // prepare last location


        costTextView = root.findViewById(R.id.cost);

        ratingBar  = root.findViewById(R.id.ratingBar);
        carRatingBar = root.findViewById(R.id.carRatingBar);
        carTextView = root.findViewById(R.id.carTextView);
        claimEditText = root.findViewById(R.id.claimEditText);
        rateTextView = root.findViewById(R.id.rateTextView);
        chatScrollView=root.findViewById(R.id.chat_msg_scroll_last);
        chatLinearLayout=root.findViewById(R.id.chat_msg_layout_last);
        moreDetailsButton=root.findViewById(R.id.last_details_button);
        nextButton=root.findViewById(R.id.last_next_button);
        previousButton=root.findViewById(R.id.last_previous_button);

        moreDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatScrollView.getVisibility()==View.GONE){
                    moreDetailsButton.setText(R.string.hide_details);
                    chatScrollView.setVisibility(View.VISIBLE);
                    mapFragment.getView().setVisibility(View.GONE);

                }
                else{
                    moreDetailsButton.setText(R.string.show_details);
                    chatScrollView.setVisibility(View.GONE);
                    mapFragment.getView().setVisibility(View.VISIBLE);
                }

            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                tripFinished=null;
                index++;
                findTrip(index);

            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                tripFinished=null;
                if(index>0) {
                    index--;
                    findTrip(index);

                }
                else{
                    previousButton.setEnabled(false);
                    previousButton.setTextColor(Color.LTGRAY);
                }

            }
        });

        if(user.isClient()){
            carRatingBar.setVisibility(View.VISIBLE);
            carTextView.setVisibility(View.VISIBLE);
            rateTextView.setText(R.string.driver_rate);
        }
        else{
            carRatingBar.setVisibility(View.GONE);
            carTextView.setVisibility(View.GONE);
        }

        p1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p1.setMargins(5, 5, 5, 5);

        p2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f);
        p2.setMargins(5,0,5,0);

        p3 = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.profile_width),
                (int) getResources().getDimension(R.dimen.profile_height));

        MyVolley myVolley = MyVolley.getInstance(getActivity());
        queue = myVolley.getRequestQueue();
        loadingRelativeLayout = root.findViewById(R.id.activity_last_trip);
        return root;
    }

    @Override
    public void onPause() {
        save();
//        finish();
//        Intent i = new Intent(this, CheckLocationActivity.class);
//        startActivity(i);
        super.onPause();

    }

    @Override
    public void onResume() {
        chatScrollView.setVisibility(View.GONE);
        mapFragment.getView().setVisibility(View.VISIBLE);
        moreDetailsButton.setText(R.string.show_details);
        moreDetailsButton.setVisibility(View.VISIBLE);
        //chatScrollView.removeAllViews();
        carRatingBar.setRating(0);
        ratingBar.setRating(0);
        //costTextView.setText("");

        claimEditText.setText("");
        Bundle bundle=getArguments();

        if(bundle!=null) {
            _id = bundle.getString("_id",null);
            nextButton.setVisibility(View.GONE);
            previousButton.setVisibility(View.GONE);

        }
        else {
            _id = null;
            nextButton.setVisibility(View.VISIBLE);
            previousButton.setVisibility(View.VISIBLE);
            nextButton.setEnabled(true);
            previousButton.setEnabled(false);
            previousButton.setTextColor(Color.LTGRAY);
        }
        super.onResume();
        loadingRelativeLayout.loaded();

    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        database = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "caoutch").allowMainThreadQueries().fallbackToDestructiveMigration().build();

                if(_id!=null) {
                    JSONArray locations50 = new JSONArray();
                    tripFinished = database.tripDao().findTrip(_id, Constants.FINISHED,user._id);
                    //tripStarted = database.tripDao().findTrip(_id, Constants.STARTED);
                    if(tripFinished!=null) {
                        polylineOptionsStarted = new PolylineOptions();
                        polylineOptionsReserved = new PolylineOptions();
                        messages = database.tripMessageDao().getTripMessage(_id);

                        try{
                            List<TripLocation> locations = database.tripLocationDao().getTripLocations(_id, "RESERVED");
                            for (int i = 0; i < locations.size(); i++) {
                                polylineOptionsReserved.add(new LatLng(locations.get(i).latitude, locations.get(i).longitude));
                            }
                            if(User.getInstance().isDriver()) {
                                if (locations.size() <= 5) {
                                    for (int i = 0; i < locations.size(); i++) {
                                        JSONObject map = new JSONObject();
                                        map.put("i", String.valueOf(i));
                                        map.put("latitude", String.valueOf(locations.get(i).latitude));
                                        map.put("longitude", String.valueOf(locations.get(i).longitude));
                                        map.put("state", String.valueOf(locations.get(i).state));
                                        map.put("time", df2.format(locations.get(i).time));
                                        //map.put("distance", String.valueOf(locations.get(i).distance));
                                        //map.put("duration", String.valueOf(locations.get(i).duration));
                                        locations50.put(map);
                                    }
                                }
                                else {
                                    for (double j = 0; j < locations.size(); j=j+(locations.size()/5)) {
                                        int i = (int) Math.ceil(j);
                                        if (i < locations.size()) {
                                            JSONObject map = new JSONObject();
                                            map.put("i", String.valueOf(i));
                                            map.put("latitude", String.valueOf(locations.get(i).latitude));
                                            map.put("longitude", String.valueOf(locations.get(i).longitude));
                                            map.put("state", String.valueOf(locations.get(i).state));
                                            map.put("time", df2.format(locations.get(i).time));
                                            //map.put("distance", String.valueOf(locations.get(i).distance));
                                            //map.put("duration", String.valueOf(locations.get(i).duration));
                                            locations50.put(map);
                                        }
                                    }

                                }
                            }
                        }
                        catch (JSONException e){

                        }


                        try{
                            List<TripLocation> locations = database.tripLocationDao().getTripLocations(_id, "STARTED");
                            for (int i = 0; i < locations.size(); i++) {
                                polylineOptionsStarted.add(new LatLng(locations.get(i).latitude, locations.get(i).longitude));
                            }
                            if(User.getInstance().isDriver()) {
                                if (locations.size() <= 30) {
                                    for (int i = 0; i < locations.size(); i++) {
                                        JSONObject map = new JSONObject();
                                        map.put("i", String.valueOf(i));
                                        map.put("latitude", String.valueOf(locations.get(i).latitude));
                                        map.put("longitude", String.valueOf(locations.get(i).longitude));
                                        map.put("state", String.valueOf(locations.get(i).state));
                                        map.put("time", df2.format(locations.get(i).time));
                                        map.put("distance", String.valueOf(locations.get(i).distance));
                                        map.put("duration", String.valueOf(locations.get(i).duration));
                                        locations50.put(map);
                                    }
                                }
                                else {
                                    for (double j = 0; j < locations.size(); j=j+(locations.size()/30)) {
                                        int i = (int) Math.ceil(j);
                                        if (i < locations.size()) {
                                            JSONObject map = new JSONObject();
                                            map.put("i", String.valueOf(i));
                                            map.put("latitude", String.valueOf(locations.get(i).latitude));
                                            map.put("longitude", String.valueOf(locations.get(i).longitude));
                                            map.put("state", String.valueOf(locations.get(i).state));
                                            map.put("time", df2.format(locations.get(i).time));
                                            map.put("distance", String.valueOf(locations.get(i).distance));
                                            map.put("duration", String.valueOf(locations.get(i).duration));
                                            locations50.put(map);
                                        }
                                    }
                                }
                            }
                        }
                        catch (JSONException e){
                            Log.i(tag, "json error: "+ e.getMessage());
                            getActivity().finish();
                        }
                        if(User.getInstance().isDriver()){
                            Map<String, String> params = new HashMap<>();
                            params.put("_id", User.getInstance()._id);
                            params.put("hashedKey", User.getInstance().hashedKey);
                            params.put("trip_id", tripFinished._id);
                            params.put("locations", locations50.toString());
                            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                                    Constants.nodejs_index_url + "/trip_locations/",
                                    GsonResponse.class,
                                    params,
                                    new Response.Listener<GsonResponse>() {
                                        @Override
                                        public void onResponse(GsonResponse response) {
                                            Log.i(tag, "json response code2: "+ response.code);
                                            Log.i(tag, "json response message2: "+ response.message);
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.i(tag, "json error: "+ error.getMessage());
                                        }
                                    });
                            queue.add(myReq);
                        }
                    }
                    setTrip();

                }
                else {
                    findTrip(0);
                }
    }

    void setTrip(){

        if(tripFinished==null) {
            costTextView.setText(getString(R.string.no_trip));
            ratingBar.setEnabled(false);
            carRatingBar.setEnabled(false);
            nextButton.setVisibility(View.GONE);
            previousButton.setVisibility(View.GONE);
            moreDetailsButton.setVisibility(View.GONE);
            claimEditText.setEnabled(false);
            mMap.clear();
            chatScrollView.removeAllViews();

            return;
        }
        else {

            if(tripFinished.state.contentEquals(Constants.CANCELED)) {
                if (tripFinished.cancelledBy.contains("client"))
                    costTextView.setText(getString(R.string.client_cancel_trip));
                else if (tripFinished.cancelledBy.contains("driver"))
                    costTextView.setText(getString(R.string.driver_cancel_trip));
                mMap.clear();
                ratingBar.setVisibility(View.GONE);
                rateTextView.setVisibility(View.GONE);
                carRatingBar.setVisibility(View.GONE);
                carTextView.setVisibility(View.GONE);


            }
            else if(tripFinished.state.contentEquals(Constants.FINISHED)) {

                costTextView.setText(String.format(Locale.ENGLISH,"%.2f", tripFinished.cost) + " " + tripFinished.cur );
                if(user.isClient()){
                    ratingBar.setVisibility(View.VISIBLE);
                    rateTextView.setVisibility(View.VISIBLE);
                    carRatingBar.setVisibility(View.VISIBLE);
                    carTextView.setVisibility(View.VISIBLE);
                }
                else{
                    ratingBar.setVisibility(View.VISIBLE);
                    rateTextView.setVisibility(View.VISIBLE);
                }
            }

//            if (getSupportActionBar() != null&&tripFinished.createTime!=null) {
//                //getSupportActionBar().setTitle(df.format(tripStarted.updateTime));
//                if(_id!=null)
//                    getSupportActionBar().setTitle(df.format(tripFinished.createTime));
//                else
//                    getSupportActionBar().setTitle("("+(index+1)+") "+df.format(tripFinished.createTime));
//            }



            if (user.isClient()) {
                if (tripFinished.driverClaim != null)
                    claimEditText.setText(tripFinished.driverClaim);
                if (tripFinished.driverRate != null)
                    ratingBar.setRating(tripFinished.driverRate.floatValue());
                if (tripFinished.carRate != null)
                    carRatingBar.setRating(tripFinished.carRate.floatValue());
            } else {
                if (tripFinished.clientClaim != null)
                    claimEditText.setText(tripFinished.clientClaim);
                if (tripFinished.clientRate != null)
                    ratingBar.setRating(tripFinished.clientRate.floatValue());
            }

            if (messages.size() > 0) {
                for (int j = 0; j < messages.size(); j++) {
                    TripMessage message = messages.get(j);
                    addToChat(df2.format(message.createTime), message.message, message.senderId);
                }
            }
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                    new LatLng(tripFinished.clientLat < tripFinished.driverLat ? tripFinished.clientLat : tripFinished.driverLat,
                            tripFinished.clientLng < tripFinished.driverLng ? tripFinished.clientLng : tripFinished.driverLng),
                    new LatLng(tripFinished.clientLat > tripFinished.driverLat ? tripFinished.clientLat : tripFinished.driverLat,
                            tripFinished.clientLng > tripFinished.driverLng ? tripFinished.clientLng : tripFinished.driverLng)), 120));
            Polyline polylineReserved = mMap.addPolyline(polylineOptionsReserved);
            polylineReserved.setWidth(5);
            polylineReserved.setColor(Color.GRAY);


            Polyline polylineStarted = mMap.addPolyline(polylineOptionsStarted);
            polylineStarted.setWidth(5);
            polylineStarted.setColor(getResources().getColor(R.color.orange));
        }

    }


    void findTrip(final int index){



//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
                polylineOptionsStarted = new PolylineOptions();
                polylineOptionsReserved = new PolylineOptions();
                tripFinished = database.tripDao().findFinishedTrip(index,user._id);

                if (tripFinished != null){
                    messages = database.tripMessageDao().getTripMessage(tripFinished._id);
                    List<TripLocation> locations = database.tripLocationDao().getTripLocations(tripFinished._id, "RESERVED");
                    for (TripLocation location : locations) {
                        polylineOptionsReserved.add(new LatLng(location.latitude, location.longitude));
                    }
                    Trip tripNext = database.tripDao().findFinishedTrip(index+1,user._id);
                    Boolean hasNext = false;
                    if(tripNext!=null){
                        hasNext=true;
                    }
                    final Boolean hasNextFinal=hasNext;
                    List<TripLocation> locations2 = database.tripLocationDao().getTripLocations(tripFinished._id, "STARTED");
                    for (TripLocation location : locations2) {
                        polylineOptionsStarted.add(new LatLng(location.latitude, location.longitude));
                    }
//                    try {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
                                chatLinearLayout.removeAllViews();
                                claimEditText.setText("");
                                carRatingBar.setRating(0);
                                ratingBar.setRating(0);
                                costTextView.setText("");
                                LastTripActivity.this.setTrip();

                                if (hasNextFinal) {
                                    nextButton.setEnabled(true);
                                    nextButton.setTextColor(getResources().getColor(R.color.primaryTextColor));
                                } else {
                                    nextButton.setEnabled(false);
                                    nextButton.setTextColor(Color.LTGRAY);
                                }
                                if (index > 0) {
                                    previousButton.setEnabled(true);
                                    previousButton.setTextColor(getResources().getColor(R.color.primaryTextColor));
                                } else {
                                    previousButton.setEnabled(false);
                                    previousButton.setTextColor(Color.LTGRAY);
                                }
//                            }
//                        });
//                    }
//                    catch (Exception e){
//                        Log.i(tag,e.getLocalizedMessage());
//                    }
                }

//            }
//
//        });
    }


    void save(){
        if(tripFinished!=null){
            if (ratingBar.getRating()>0 ||carRatingBar.getRating()>0 ||
                    claimEditText.getText().toString().length() >0) {

            Map<String,String> params = new HashMap<>();
            params.put("_id",tripFinished._id);
            if(user.isClient()) {
                tripFinished.driverRate = new Double(ratingBar.getRating());
                tripFinished.carRate = new Double(carRatingBar.getRating());
                tripFinished.driverClaim = claimEditText.getText().toString();

                params.put("driverClaim",tripFinished.driverClaim);
                params.put("driverRate",tripFinished.driverRate.toString());
                params.put("carRate",tripFinished.carRate.toString());
                params.put("clientId",user._id);

            }
            else{
                tripFinished.clientRate = new Double(ratingBar.getRating());
                tripFinished.clientClaim = claimEditText.getText().toString();
                params.put("clientClaim",tripFinished.clientClaim);
                params.put("clientRate",tripFinished.clientRate.toString());
                params.put("driverId",user._id);
            }

                final Trip trip=tripFinished.clone();
//                AsyncTask.execute(new Runnable() {
//                    @Override
//                    public void run() {
                        database.tripDao().updateTrip(trip);
//                    }
//                });



                GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                        Constants.nodejs_index_url + "/trip/",
                        GsonResponse.class,
                        params,
                        createGetUserSuccessListener(),
                        createGetUserErrorListener());

                queue.add(myReq);
            }
        }
        //Intent i = new Intent(this, MainActivity.class);
        //startActivity(i);

    }

    private Response.Listener<GsonResponse> createGetUserSuccessListener() {
        Log.i(tag,"createGetUserSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;
                Log.i(tag, "json response code: "+ response.code);
                Log.i(tag, "json response message: "+ response.message);


            }
        };
    }

    private Response.ErrorListener createGetUserErrorListener() {
        Log.i(tag,"createGetUserErrorListener");
        return new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
            }
        };
    }

    /*String getCurrentTimezoneOffset() {

        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis())*2;

        String offset = String.format(Locale.ENGLISH,"%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "GMT+" : "GMT-") + (offset);

        return offset;
    }*/

    void addToChat(String time, String message, String senderId){

        /*LinearLayout linearLayout = new LinearLayout(LastTripActivity.this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(p1);
        ImageView profile = new ImageView(LastTripActivity.this);TextView newMsg = new TextView(LastTripActivity.this);
        profile.setLayoutParams(p3);
        newMsg.setLayoutParams(p2);

        if(!user._id.contentEquals(senderId)) {
            newMsg.setText(time+": "+message);
            newMsg.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            newMsg.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            if (user.isDriver()) {
                profile.setBackground(getDrawable(R.drawable.car));

            } else {
                profile.setBackground(getDrawable(R.drawable.client));
            }
            linearLayout.addView(profile);
            linearLayout.addView(newMsg);
        }
        else {
            newMsg.setTextColor(getResources().getColor(R.color.black));
            newMsg.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            newMsg.setText(message+" :"+time);
            if (user.isDriver()) {
                profile.setBackground(getDrawable(R.drawable.client));
            } else {
                profile.setBackground(getDrawable(R.drawable.car));
            }
            linearLayout.addView(newMsg);
            linearLayout.addView(profile);

        }*/


        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(chatLinearLayout.getContext())
                .inflate(R.layout.support_view_holder, chatLinearLayout, false);


        if(!user._id.contentEquals(senderId)) {
            RelativeLayout senderLayout = linearLayout.findViewById(R.id.sender_layout);
            RelativeLayout receiverLayout = linearLayout.findViewById(R.id.receiver_layout);
            senderLayout.setVisibility(View.GONE);
            receiverLayout.setVisibility(View.VISIBLE);
            TextView t = receiverLayout.findViewById(R.id.receiver_text_view);
            t.setText(message);
            if(user.isDriver()){
                ImageView p = receiverLayout.findViewById(R.id.receiver_image);
                p.setImageResource(R.drawable.client);
            }
            else if(user.isClient()){
                ImageView p = receiverLayout.findViewById(R.id.receiver_image);
                p.setImageResource(R.drawable.caoutch_small);
            }
            TextView timeV = receiverLayout.findViewById(R.id.receiver_time);
            timeV.setText(time);

        }
        else {
            RelativeLayout senderLayout = linearLayout.findViewById(R.id.sender_layout);
            RelativeLayout receiverLayout = linearLayout.findViewById(R.id.receiver_layout);
            senderLayout.setVisibility(View.VISIBLE);
            receiverLayout.setVisibility(View.GONE);
            TextView t = senderLayout.findViewById(R.id.sender_text_view);
            t.setText(message);
            if(user.isDriver()){
                ImageView p = senderLayout.findViewById(R.id.sender_image);
                p.setImageResource(R.drawable.caoutch_small);
            }
            else if(user.isClient()){
                ImageView p = senderLayout.findViewById(R.id.sender_image);
                p.setImageResource(R.drawable.client);
            }
            TextView timeV = senderLayout.findViewById(R.id.sender_time);
            timeV.setText(time);

        }

        chatLinearLayout.addView(linearLayout);
    }


}

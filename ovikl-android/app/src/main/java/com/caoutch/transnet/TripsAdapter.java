package com.caoutch.transnet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.database.Trip;
import com.caoutch.transnet.database.TripLocation;
import com.caoutch.transnet.view.BtnImage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.MyViewHolder> {
    private String tag = "MyAdapter";
    private RequestQueue queue;
    public ArrayList<Trip> mDataset=new ArrayList<Trip>();
    private AppDatabase database;
    int density=1;
    String search="";
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    LinearLayout.LayoutParams p2;

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
    TimeZone timeZone=TimeZone.getDefault();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder  implements OnMapReadyCallback {
        // each data item is just a string in this case
        public LinearLayout linearLayout;
        public CardView cardView;
        GoogleMap googleMap;
        MapView mapView;
        Trip trip;


        public MyViewHolder(CardView v) {
            super(v);
            cardView=v;
            linearLayout = new LinearLayout(v.getContext());
            v.addView(linearLayout);
            linearLayout.setOrientation(LinearLayout.VERTICAL);


            int density = (int) linearLayout.getContext().getResources().getDisplayMetrics().density;
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            p.setMargins(10*density,10*density,10*density,10*density);
            //v.setPadding(10*density,10*density,10*density,10*density);
            v.setLayoutParams(p);
            //v.setBackgroundColor(v.getContext().getResources().getColor(R.color.white));
            //v.setRadius(5*density);
            v.setContentPadding(10*density,10*density,10*density,10*density);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.i("MyViewHolder","onMapReady");
            MapsInitializer.initialize(MyApplication.getAppContext());
            this.googleMap=googleMap;

            //googleMap.clear();
            if(trip.clientLat!=null&&trip.clientLng!=null) {
                int density = (int) linearLayout.getResources().getDisplayMetrics().density;

                BitmapDrawable bitmapDrawPointer=(BitmapDrawable)linearLayout.getResources().getDrawable(R.drawable.client_pointer);
                Bitmap bPointer=bitmapDrawPointer.getBitmap();
                Bitmap bitmap = Bitmap.createScaledBitmap(bPointer, 7*density, 10*density, false);
                final BitmapDescriptor icon =BitmapDescriptorFactory.fromBitmap(bitmap);

                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng =new LatLng(trip.clientLat, trip.clientLng);
                markerOptions.position(latLng);
                Marker marker = this.googleMap.addMarker(markerOptions);
                marker.setIcon(icon);
                marker.showInfoWindow();
            }

            if(trip.locations!=null&&trip.locations.size()>0) {
                Double minLat=trip.locations.get(0).latitude;
                Double minLng=trip.locations.get(0).longitude;
                Double maxLat=trip.locations.get(0).latitude;
                Double maxLng=trip.locations.get(0).longitude;
                PolylineOptions polylineOptionsReserved= new PolylineOptions();
                PolylineOptions polylineOptionsStarted = new PolylineOptions();
                for (Trip.Location location:trip.locations ) {
                    if(minLat>location.latitude)
                        minLat=location.latitude;
                    if(minLng>location.longitude)
                        minLng=location.longitude;
                    if(maxLat<location.latitude)
                        maxLat=location.latitude;
                    if(maxLng<location.longitude)
                        maxLng=location.longitude;

                    if(location.state.contains(Constants.RESERVED))
                        polylineOptionsReserved.add(new LatLng(location.latitude,location.longitude));
                    polylineOptionsStarted.add(new LatLng(location.latitude,location.longitude));
                }
                if(polylineOptionsStarted.getPoints().size()>1) {
                    Polyline polylineStarted = this.googleMap.addPolyline(polylineOptionsStarted);
                    polylineStarted.setWidth(5);
                    polylineStarted.setColor(Color.parseColor("#ff5722"));
                }
                if(polylineOptionsReserved.getPoints().size()>1) {
                    Polyline polylineReserved = this.googleMap.addPolyline(polylineOptionsReserved);
                    polylineReserved.setWidth(5);
                    polylineReserved.setColor(Color.GRAY);
                }

                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                        new LatLng(minLat,minLng),
                        new LatLng(maxLat,maxLng)), 120));

            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TripsAdapter(MyVolley myVolley, Context context,String search) {
        //mDataset = myDataset;
        this.search=search;
        queue = myVolley.getRequestQueue();
        database = Room.databaseBuilder(context,
                AppDatabase.class, "caoutch").fallbackToDestructiveMigration().build();

        mDataset.add(null);
        density = (int) context.getResources().getDisplayMetrics().density;
        p.setMargins(10*density,10*density,10*density,10*density);

        p2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                300*density);

        df.setTimeZone(timeZone);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {

        //LinearLayout v = new LinearLayout(parent.getContext());
        CardView v = new CardView(parent.getContext());
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element


        //p.setMargins(10*density,10*density,10*density,10*density);


        LinearLayout linearLayout = holder.linearLayout;
        linearLayout.removeAllViews();




        if (mDataset.get(position) == null) {
            ProgressBar progressBar = new ProgressBar(linearLayout.getContext());
            linearLayout.addView(progressBar);
            progressBar.animate();

            Map<String, String> params = new HashMap<>();
            params.put("_id", User.getInstance()._id);
            params.put("hashedKey", User.getInstance().hashedKey);
            params.put("zone", User.getInstance().zone);
            params.put("search", search);
            if(mDataset.size()>1){
                params.put("last_user_id", mDataset.get(mDataset.size()-2)._id);
            }
            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                    Constants.nodejs_index_url + "/trips/",
                    GsonResponse.class,
                    params,
                    createGetUserSuccessListener(),
                    createGetUserErrorListener());
            queue.add(myReq);


        }
        else{

            CardView cardView2 = new CardView(linearLayout.getContext());
            cardView2.setLayoutParams(p);
            linearLayout.addView(cardView2);
            LinearLayout linearLayout2 = new LinearLayout(linearLayout.getContext());
            linearLayout2.setOrientation(LinearLayout.VERTICAL);
            cardView2.addView(linearLayout2);
            TableLayout tableLayout2 = new TableLayout(linearLayout.getContext());
            tableLayout2.setLayoutParams(p);
            tableLayout2.setStretchAllColumns(true);
            linearLayout2.addView(tableLayout2);

            final Trip trip = mDataset.get(position);
            holder.trip=trip;
            if (trip.createTime!=null) {

                TableRow tableRow = new TableRow(tableLayout2.getContext());
                TextView textView = new TextView(tableLayout2.getContext());
                textView.setText(tableLayout2.getContext().getResources().getString(R.string.time));
                textView.setPadding(5 * density, 0, 5 * density, 0);
                TextView textView2 = new TextView(tableLayout2.getContext());
                textView2.setText(df.format(trip.createTime));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout2.addView(tableRow);
            }

            if (trip.state!=null) {
                TableRow tableRow = new TableRow(tableLayout2.getContext());
                TextView textView = new TextView(tableLayout2.getContext());
                textView.setText(tableLayout2.getContext().getResources().getString(R.string.state));
                textView.setPadding(5 * density, 0, 5 * density, 0);
                TextView textView2 = new TextView(tableLayout2.getContext());
                textView2.setText(trip.state);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout2.addView(tableRow);
            }

            if (trip.cancelledBy!=null) {
                TableRow tableRow = new TableRow(tableLayout2.getContext());
                TextView textView = new TextView(tableLayout2.getContext());
                textView.setText(tableLayout2.getContext().getResources().getString(R.string.cancelled_by));
                textView.setPadding(5 * density, 0, 5 * density, 0);
                TextView textView2 = new TextView(tableLayout2.getContext());
                textView2.setText(trip.cancelledBy);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout2.addView(tableRow);
            }

            if (trip.cost != null) {
                TableRow tableRow = new TableRow(tableLayout2.getContext());
                TextView textView = new TextView(tableLayout2.getContext());
                textView.setText(tableLayout2.getContext().getResources().getString(R.string.cost));
                textView.setPadding(5 * density, 0, 5 * density, 0);
                TextView textView2 = new TextView(tableLayout2.getContext());
                textView2.setText(String.format(Locale.ENGLISH,"%.2f", trip.cost));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout2.addView(tableRow);
            }

            if (trip.cur != null) {
                TableRow tableRow = new TableRow(tableLayout2.getContext());
                TextView textView = new TextView(tableLayout2.getContext());
                textView.setText(tableLayout2.getContext().getResources().getString(R.string.currency));
                textView.setPadding(5 * density, 0, 5 * density, 0);
                TextView textView2 = new TextView(tableLayout2.getContext());
                textView2.setText( trip.cur);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout2.addView(tableRow);
            }

            if (trip.distance != null) {
                TableRow tableRow = new TableRow(tableLayout2.getContext());
                TextView textView = new TextView(tableLayout2.getContext());
                textView.setText(tableLayout2.getContext().getResources().getString(R.string.distance));
                textView.setPadding(5 * density, 0, 5 * density, 0);
                TextView textView2 = new TextView(tableLayout2.getContext());
                textView2.setText(String.format(Locale.ENGLISH,"%.2f", trip.distance));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout2.addView(tableRow);
            }


            if (trip.duration != null) {
                TableRow tableRow = new TableRow(tableLayout2.getContext());
                TextView textView = new TextView(tableLayout2.getContext());
                textView.setText(tableLayout2.getContext().getResources().getString(R.string.duration));
                textView.setPadding(5 * density, 0, 5 * density, 0);
                TextView textView2 = new TextView(tableLayout2.getContext());
                textView2.setText(String.format(Locale.ENGLISH,"%.2f", trip.duration));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout2.addView(tableRow);
            }

            if (trip.zone!=null&&User.getInstance().isSuperAdmin()){
                TableRow tableRow = new TableRow(tableLayout2.getContext());
                TextView textView = new TextView(tableLayout2.getContext());
                textView.setText(tableLayout2.getContext().getResources().getString(R.string.zone));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout2.getContext());
                textView2.setText(trip.zone);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout2.addView(tableRow);
            }

            if(trip.clientLat!=null&&trip.clientLng!=null&&trip.driverLat!=null&&trip.driverLng!=null){

                GoogleMapOptions googleMapOptions = new GoogleMapOptions();
                googleMapOptions.liteMode(true);
                googleMapOptions.mapToolbarEnabled(false);
                googleMapOptions.zoomControlsEnabled(true);
                if(holder.mapView == null) {
                    holder.mapView = new MapView(linearLayout2.getContext(), googleMapOptions);
                    holder.mapView.setClickable(false);

                    holder.mapView.setLayoutParams(p2);
                    holder.mapView.onCreate(null);
                    holder.mapView.getMapAsync(holder);
                }
                else
                    ((LinearLayout)holder.mapView.getParent()).removeAllViews();
                linearLayout2.addView(holder.mapView);

                BitmapDrawable bitmapDrawPointer=(BitmapDrawable)linearLayout2.getResources().getDrawable(R.drawable.client_pointer);
                Bitmap bPointer=bitmapDrawPointer.getBitmap();
                Bitmap bitmap = Bitmap.createScaledBitmap(bPointer, 7*density, 10*density, false);
                final BitmapDescriptor icon =BitmapDescriptorFactory.fromBitmap(bitmap);
                if(holder.googleMap!=null){
                        Log.i(tag,"holder.googleMap!=null");
                        holder.googleMap.clear();

                    if(trip.clientLat!=null&&trip.clientLng!=null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng =new LatLng(trip.clientLat, trip.clientLng);
                        markerOptions.position(latLng);
                        Marker marker = holder.googleMap.addMarker(markerOptions);
                        marker.setIcon(icon);
                        marker.showInfoWindow();
                    }


                    if(trip.locations!=null&&trip.locations.size()>0) {
                            Double minLat=trip.locations.get(0).latitude;
                            Double minLng=trip.locations.get(0).longitude;
                            Double maxLat=trip.locations.get(0).latitude;
                            Double maxLng=trip.locations.get(0).longitude;
                            PolylineOptions polylineOptionsReserved= new PolylineOptions();
                            PolylineOptions polylineOptionsStarted = new PolylineOptions();
                            for (Trip.Location location:trip.locations ) {
                                if(minLat>location.latitude)
                                    minLat=location.latitude;
                                if(minLng>location.longitude)
                                    minLng=location.longitude;
                                if(maxLat<location.latitude)
                                    maxLat=location.latitude;
                                if(maxLng<location.longitude)
                                    maxLng=location.longitude;

                                if(location.state.contains(Constants.RESERVED))
                                    polylineOptionsReserved.add(new LatLng(location.latitude,location.longitude));
                                polylineOptionsStarted.add(new LatLng(location.latitude,location.longitude));
                            }
                            if(polylineOptionsStarted.getPoints().size()>1) {
                                Polyline polylineStarted = holder.googleMap.addPolyline(polylineOptionsStarted);
                                polylineStarted.setWidth(5);
                                polylineStarted.setColor(Color.parseColor("#ff5722"));
                            }
                            if(polylineOptionsReserved.getPoints().size()>1) {
                                Polyline polylineReserved = holder.googleMap.addPolyline(polylineOptionsReserved);
                                polylineReserved.setWidth(5);
                                polylineReserved.setColor(Color.GRAY);
                            }

                            holder.googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                                    new LatLng(minLat,minLng),
                                    new LatLng(maxLat,maxLng)), 120));

                        }

                    }
                }





            User driverData = trip.driver;
            if(driverData!=null) {
                CardView cardView = new CardView(linearLayout.getContext());
                cardView.setLayoutParams(p);
                linearLayout.addView(cardView);
                LinearLayout linearLayout1 = new LinearLayout(linearLayout.getContext());
                linearLayout1.setOrientation(LinearLayout.VERTICAL);
                cardView.addView(linearLayout1);

                HorizontalScrollView horizontalScrollView = new HorizontalScrollView(linearLayout.getContext());
                horizontalScrollView.setLayoutParams(p);
                LinearLayout imagesLinearLayout = new LinearLayout(linearLayout.getContext());
                imagesLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                imagesLinearLayout.setLayoutParams(p);
                horizontalScrollView.addView(imagesLinearLayout);
                linearLayout1.addView(horizontalScrollView);
                horizontalScrollView.setVisibility(View.GONE);

                TableLayout tableLayout = new TableLayout(linearLayout.getContext());
                tableLayout.setLayoutParams(p);
                tableLayout.setStretchAllColumns(true);
                linearLayout1.addView(tableLayout);
                if (driverData.type != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.account_type));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    if (driverData.isClient())
                        textView2.setText(tableLayout.getContext().getResources().getString(R.string.client));
                    else if (driverData.isDriver())
                        textView2.setText(tableLayout.getContext().getResources().getString(R.string.driver));
                    else if (driverData.isAdmin())
                        textView2.setText(tableLayout.getContext().getResources().getString(R.string.admin));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.firstName != null && User.getInstance().isAdmin()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.first_name));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.firstName);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.lastName != null && User.getInstance().isAdmin()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.last_name));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.lastName);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.isDriver()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.driver_rate));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    LinearLayout l = new LinearLayout(tableLayout.getContext());
                    RatingBar r = new RatingBar(tableLayout.getContext(), null, android.R.attr.ratingBarStyleSmall);
                    r.setNumStars(5);
                    l.addView(r);
                    tableRow.addView(textView);
                    tableRow.addView(l);
                    tableLayout.addView(tableRow);

                    if (driverData.driverRate != null)
                        r.setRating((float) driverData.driverRate);
                    else
                        r.setRating(0);
                }

                if (driverData.isDriver()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.car_rate));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    LinearLayout l = new LinearLayout(tableLayout.getContext());
                    RatingBar r = new RatingBar(tableLayout.getContext(), null, android.R.attr.ratingBarStyleSmall);
                    r.setNumStars(5);
                    l.addView(r);
                    tableRow.addView(textView);
                    tableRow.addView(l);
                    tableLayout.addView(tableRow);

                    if (driverData.carRate != null)
                        r.setRating((float) driverData.carRate);
                    else
                        r.setRating(0);
                }




                if (driverData.mobile != null && User.getInstance().isAdmin()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.mobile));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.mobile);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);

                    Linkify.addLinks(textView2, Patterns.PHONE, "tel:", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter);
                    textView2.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (driverData.email != null && User.getInstance().isAdmin()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.email));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.email);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                    textView2.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);

                    textView2.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (driverData.cost.minimum > 0) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.minimum));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(String.valueOf(driverData.cost.minimum));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.cost.base > 0) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.baseCost));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(String.valueOf(driverData.cost.base));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.cost.km > 0) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.kmCost));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(String.valueOf(driverData.cost.km));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.cost.minute > 0) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.minuteCost));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(String.valueOf(driverData.cost.minute));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.cost.currency != null && !driverData.cost.currency.isEmpty()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.currency));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(String.valueOf(driverData.cost.currency));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }


                if (driverData.carNumber != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.carNumber));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.carNumber);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.createDate != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.create_date));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.createDate.toString());
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }


                if (driverData.totalHours != null && driverData.totalDistance != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.total_trips));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.totalHours +
                            " " + tableLayout.getContext().getString(R.string.hr) + "/" + driverData.totalDistance + " " + tableLayout.getContext().getString(R.string.km));
                    tableLayout.addView(tableRow);
                }

                if (driverData.claimsCount != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.claims));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.claimsCount);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.tripsCount != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.trips_count));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.tripsCount);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.carManufacturer!=null&&driverData.carManufacturer.length()>0){
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.carManufacturer));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.carManufacturer);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.carModel!=null&&driverData.carModel.length()>0){
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.carModel));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.carModel);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.carModel != null && driverData.carModel.length() > 0) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.carModel));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.carModel);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.carMadeYear != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.carMadeYear));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(driverData.carMadeYear);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }


                if (driverData.images.containsKey(Constants.frontImageSmall) && driverData.images.get(Constants.frontImageSmall).length() > 0) {
                    BtnImage btnImage = new BtnImage(tableLayout.getContext());
                    btnImage.setImage(driverData.images.get(Constants.frontImageSmall), database);
                    imagesLinearLayout.addView(btnImage);
                    horizontalScrollView.setVisibility(View.VISIBLE);
                }

                if (driverData.images.containsKey(Constants.sideImageSmall) && driverData.images.get(Constants.sideImageSmall).length() > 0) {
                    BtnImage btnImage = new BtnImage(tableLayout.getContext());
                    btnImage.setImage(driverData.images.get(Constants.sideImageSmall), database);
                    imagesLinearLayout.addView(btnImage);
                    horizontalScrollView.setVisibility(View.VISIBLE);
                }
                if (driverData.images.containsKey(Constants.backImageSmall) && driverData.images.get(Constants.backImageSmall).length() > 0) {
                    BtnImage btnImage = new BtnImage(tableLayout.getContext());
                    btnImage.setImage(driverData.images.get(Constants.backImageSmall), database);
                    imagesLinearLayout.addView(btnImage);
                    horizontalScrollView.setVisibility(View.VISIBLE);
                }


            }

            User clientData = trip.client;
            if(clientData!=null) {
                CardView cardView = new CardView(linearLayout.getContext());
                cardView.setLayoutParams(p);
                linearLayout.addView(cardView);
                LinearLayout linearLayout1 = new LinearLayout(linearLayout.getContext());
                linearLayout1.setOrientation(LinearLayout.VERTICAL);
                cardView.addView(linearLayout1);

                HorizontalScrollView horizontalScrollView = new HorizontalScrollView(linearLayout.getContext());
                horizontalScrollView.setLayoutParams(p);
                LinearLayout imagesLinearLayout = new LinearLayout(linearLayout.getContext());
                imagesLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                imagesLinearLayout.setLayoutParams(p);
                horizontalScrollView.addView(imagesLinearLayout);
                linearLayout1.addView(horizontalScrollView);
                horizontalScrollView.setVisibility(View.GONE);

                TableLayout tableLayout = new TableLayout(linearLayout.getContext());
                tableLayout.setLayoutParams(p);
                tableLayout.setStretchAllColumns(true);
                linearLayout1.addView(tableLayout);
                if (clientData.type != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.account_type));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    if (clientData.isClient())
                        textView2.setText(tableLayout.getContext().getResources().getString(R.string.client));
                    else if (clientData.isDriver())
                        textView2.setText(tableLayout.getContext().getResources().getString(R.string.driver));
                    else if (clientData.isAdmin())
                        textView2.setText(tableLayout.getContext().getResources().getString(R.string.admin));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (clientData.firstName != null && User.getInstance().isAdmin()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.first_name));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(clientData.firstName);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (clientData.lastName != null && User.getInstance().isAdmin()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.last_name));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(clientData.lastName);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }


                if (clientData.isClient()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.client_rate));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    LinearLayout l = new LinearLayout(tableLayout.getContext());
                    RatingBar r = new RatingBar(tableLayout.getContext(), null, android.R.attr.ratingBarStyleSmall);
                    r.setNumStars(5);

                    if (clientData.clientRate != null)
                        r.setRating((float) clientData.clientRate);
                    else
                        r.setRating(0);
                    l.addView(r);
                    tableRow.addView(textView);
                    tableRow.addView(l);
                    tableLayout.addView(tableRow);
                }


                if (clientData.mobile != null && User.getInstance().isAdmin()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.mobile));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(clientData.mobile);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);

                    Linkify.addLinks(textView2, Patterns.PHONE, "tel:", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter);
                    textView2.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (clientData.email != null && User.getInstance().isAdmin()) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.email));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(clientData.email);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                    textView2.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);

                    textView2.setMovementMethod(LinkMovementMethod.getInstance());
                }


                if (clientData.claimsCount != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.claims));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(clientData.claimsCount);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (clientData.tripsCount != null) {
                    TableRow tableRow = new TableRow(tableLayout.getContext());
                    TextView textView = new TextView(tableLayout.getContext());
                    textView.setText(tableLayout.getContext().getResources().getString(R.string.trips_count));
                    textView.setPadding(5 * density, 0, 5 * density, 0);
                    TextView textView2 = new TextView(tableLayout.getContext());
                    textView2.setText(clientData.tripsCount);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

            }

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    private Response.Listener<GsonResponse> createGetUserSuccessListener() {
        Log.i(tag, "createGetUserSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;
                Log.i(tag, "json response code2: " + response.code);
                Log.i(tag, "json response message2: " + response.message);

                if (response.code.contentEquals("200")) {
                    if(response.trips!=null&&response.trips.size()>0){
                        mDataset.remove(null);
                        mDataset.addAll(response.trips);
                        mDataset.add(null);
                        notifyDataSetChanged();
                    }
                    else {
                        mDataset.remove(null);
                        notifyDataSetChanged();
                    }
                } else if (response.code.contentEquals("201")) {
                    //Toast toast = Toast.makeText(UsersActivity.tableLayout.getContext(), getString(R.string.retry), Toast.LENGTH_LONG);
                    //toast.show();
                }
            }
        };
    }


    private Response.ErrorListener createGetUserErrorListener() {
        Log.i(tag, "createGetUserErrorListener2");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: " + error.getMessage());
                //loadingRelativeLayout.loadingFailed();

            }
        };
    }

}
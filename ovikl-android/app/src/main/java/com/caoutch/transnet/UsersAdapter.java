package com.caoutch.transnet;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.view.BtnImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {
    private String tag = "MyAdapter";
    private RequestQueue queue;
    public ArrayList<User> mDataset=new ArrayList<User>();
    private AppDatabase database;
    String search="";


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,Response.Listener<GsonResponse>,Response.ErrorListener {
        // each data item is just a string in this case
        public LinearLayout linearLayout;
        public CardView cardView;
        Button deleteBtn;
        Button confirmBtn;
        Button cancelBtn;
        Button undoBtn;
        User user;
        RequestQueue queue;

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
        public void onClick(View v) {
            if(v==deleteBtn){
                deleteBtn.setVisibility(View.GONE);
                confirmBtn.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.VISIBLE);
                undoBtn.setVisibility(View.GONE);
            }
            else if(v==confirmBtn){

                Map<String, String> params = new HashMap<>();
                params.put("_id", User.getInstance()._id);
                params.put("hashedKey", User.getInstance().hashedKey);
                params.put("user_email", user.email);
                params.put("user_id", user._id);
                params.put("user_type", user.type);
                GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                        Constants.nodejs_index_url + "/block_zone_user/",
                        GsonResponse.class,
                        params,
                        this,
                        this);
                queue.add(myReq);


            }
            else if(v==cancelBtn){
                deleteBtn.setVisibility(View.VISIBLE);
                confirmBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                undoBtn.setVisibility(View.GONE);
            }
            else if(v==undoBtn){
                Map<String, String> params = new HashMap<>();
                params.put("_id", User.getInstance()._id);
                params.put("hashedKey", User.getInstance().hashedKey);
                params.put("user_email", user.email);
                params.put("user_id", user._id);
                params.put("user_type", user.type);
                GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                        Constants.nodejs_index_url + "/unblock_zone_user/",
                        GsonResponse.class,
                        params,
                        this,
                        this);
                queue.add(myReq);
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            deleteBtn.setVisibility(View.VISIBLE);
            confirmBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
            undoBtn.setVisibility(View.GONE);
            Toast toast = Toast.makeText(deleteBtn.getContext(), deleteBtn.getResources().getString(R.string.retry), Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        public void onResponse(GsonResponse response) {
            Log.i("MyViewHolder", "json response code2: " + response.code);
            Log.i("MyViewHolder", "json response message2: " + response.message);

            if (response.code.contentEquals("200")) {
                deleteBtn.setVisibility(View.GONE);
                confirmBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                undoBtn.setVisibility(View.VISIBLE);
            }
            else if (response.code.contentEquals("210")) {
                deleteBtn.setVisibility(View.VISIBLE);
                confirmBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                undoBtn.setVisibility(View.GONE);
            }
            else{
                deleteBtn.setVisibility(View.VISIBLE);
                confirmBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                undoBtn.setVisibility(View.GONE);
                Toast toast = Toast.makeText(deleteBtn.getContext(), deleteBtn.getResources().getString(R.string.retry), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UsersAdapter(MyVolley myVolley, Context context,String search) {
        //mDataset = myDataset;
        this.search=search;
        queue = myVolley.getRequestQueue();
        database = Room.databaseBuilder(context,
                AppDatabase.class, "caoutch").fallbackToDestructiveMigration().build();

        mDataset.add(null);


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
        int density = (int) holder.linearLayout.getContext().getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //p.setMargins(10*density,10*density,10*density,10*density);


        LinearLayout linearLayout = holder.linearLayout;
        linearLayout.removeAllViews();
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(linearLayout.getContext());
        horizontalScrollView.setLayoutParams(p);
        LinearLayout imagesLinearLayout = new LinearLayout(linearLayout.getContext());
        imagesLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        imagesLinearLayout.setLayoutParams(p);
        horizontalScrollView.addView(imagesLinearLayout);
        linearLayout.addView(horizontalScrollView);
        horizontalScrollView.setVisibility(View.GONE);

        TableLayout tableLayout = new TableLayout(linearLayout.getContext());
        tableLayout.setLayoutParams(p);
        tableLayout.setStretchAllColumns(true);
        linearLayout.addView(tableLayout);
        linearLayout.setBackgroundColor(linearLayout.getResources().getColor(R.color.white));

        User driverData = mDataset.get(position);
        if (driverData == null) {
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
                    Constants.nodejs_index_url + "/users/",
                    GsonResponse.class,
                    params,
                    createGetUserSuccessListener(),
                    createGetUserErrorListener());
            queue.add(myReq);


        }
        else{
            holder.user=driverData;
            holder.queue=queue;
            if (driverData.type != null){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.account_type));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                if(driverData.isClient())
                    textView2.setText(tableLayout.getContext().getResources().getString(R.string.client));
                else if(driverData.isDriver())
                    textView2.setText(tableLayout.getContext().getResources().getString(R.string.driver));
                else if(driverData.isAdmin())
                    textView2.setText(tableLayout.getContext().getResources().getString(R.string.admin));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.firstName != null && User.getInstance().isAdmin()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.first_name));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.firstName);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.lastName != null && User.getInstance().isAdmin()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.last_name));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.lastName);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if(driverData.isDriver()) {
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.driver_rate));
                textView.setPadding(5*density,0,5*density,0);
                LinearLayout l = new LinearLayout(tableLayout.getContext());
                RatingBar r = new RatingBar(tableLayout.getContext(),null, android.R.attr.ratingBarStyleSmall);
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

            if(driverData.isDriver()) {
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.car_rate));
                textView.setPadding(5*density,0,5*density,0);
                LinearLayout l = new LinearLayout(tableLayout.getContext());
                RatingBar r = new RatingBar(tableLayout.getContext(),null, android.R.attr.ratingBarStyleSmall);
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

            if(driverData.isClient()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.client_rate));
                textView.setPadding(5*density,0,5*density,0);
                LinearLayout l = new LinearLayout(tableLayout.getContext());
                RatingBar r = new RatingBar(tableLayout.getContext(),null, android.R.attr.ratingBarStyleSmall);
                r.setNumStars(5);

                if (driverData.clientRate != null)
                    r.setRating((float) driverData.clientRate);
                else
                    r.setRating(0);
                l.addView(r);
                tableRow.addView(textView);
                tableRow.addView(l);
                tableLayout.addView(tableRow);
            }



            if (driverData.mobile != null && User.getInstance().isAdmin()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.mobile));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.mobile);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);

                Linkify.addLinks(textView2, Patterns.PHONE,"tel:",Linkify.sPhoneNumberMatchFilter,Linkify.sPhoneNumberTransformFilter);
                textView2.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if (driverData.email!=null && User.getInstance().isAdmin()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.email));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.email);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
                textView2.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);

                textView2.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if (driverData.cost.minimum>0){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.minimum));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(String.valueOf(driverData.cost.minimum));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.cost.base>0){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.baseCost));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(String.valueOf(driverData.cost.base));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.cost.km>0){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.kmCost));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(String.valueOf(driverData.cost.km));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.cost.minute>0){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.minuteCost));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(String.valueOf(driverData.cost.minute));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.cost.currency!=null&&!driverData.cost.currency.isEmpty()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.currency));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(String.valueOf(driverData.cost.currency));
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }


            if (driverData.carNumber!=null){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.carNumber));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.carNumber);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.createDate!=null){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.create_date));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.createDate.toString());
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }


            if (driverData.totalHours!=null && driverData.totalDistance!=null) {
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.total_trips));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.totalHours +
                        " " + tableLayout.getContext().getString(R.string.hr) + "/" + driverData.totalDistance + " " + tableLayout.getContext().getString(R.string.km));
                tableLayout.addView(tableRow);
            }

            if (driverData.claimsCount!=null){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.claims));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.claimsCount);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.tripsCount!=null){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.trips_count));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.tripsCount);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }
            if (driverData.carType != null&&driverData.carType.length()>0) {
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.car_type));
                textView.setPadding(5 * density, 0, 5 * density, 0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.carType);
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

            if (driverData.carMadeYear!=null){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.carMadeYear));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.carMadeYear);
                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.zone!=null&&User.getInstance().isSuperAdmin()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.zone));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.zone);

                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.driverStatus!=null&&User.getInstance().isSuperAdmin()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.state));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.driverStatus);

                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if (driverData.clientStatus!=null&&User.getInstance().isSuperAdmin()){
                TableRow tableRow = new TableRow(tableLayout.getContext());
                TextView textView = new TextView(tableLayout.getContext());
                textView.setText(tableLayout.getContext().getResources().getString(R.string.state));
                textView.setPadding(5*density,0,5*density,0);
                TextView textView2 = new TextView(tableLayout.getContext());
                textView2.setText(driverData.clientStatus);

                tableRow.addView(textView);
                tableRow.addView(textView2);
                tableLayout.addView(tableRow);
            }

            if(User.getInstance().isAdmin()){
                LinearLayout linearLayout1 = new LinearLayout(linearLayout.getContext());


                holder.deleteBtn = new Button(linearLayout.getContext());
                if(User.getInstance().isSuperAdmin())
                    holder.deleteBtn.setText(tableLayout.getContext().getResources().getString(R.string.block));
                else
                    holder.deleteBtn.setText(tableLayout.getContext().getResources().getString(R.string.remove_from_zone));
                linearLayout1.addView(holder.deleteBtn);
                holder.deleteBtn.setAllCaps(false);
                holder.deleteBtn.setOnClickListener(holder);

                holder.cancelBtn = new Button(linearLayout.getContext());
                holder.cancelBtn.setText(tableLayout.getContext().getResources().getString(R.string.cancel));
                holder.cancelBtn.setAllCaps(false);
                linearLayout1.addView(holder.cancelBtn);
                holder.cancelBtn.setOnClickListener(holder);

                holder.confirmBtn = new Button(linearLayout.getContext());
                holder.confirmBtn.setText(tableLayout.getContext().getResources().getString(R.string.confirm));
                holder.confirmBtn.setAllCaps(false);
                linearLayout1.addView(holder.confirmBtn);
                holder.confirmBtn.setOnClickListener(holder);

                holder.undoBtn = new Button(linearLayout.getContext());
                holder.undoBtn.setText(tableLayout.getContext().getResources().getString(R.string.undo));
                holder.undoBtn.setAllCaps(false);
                linearLayout1.addView(holder.undoBtn);
                holder.undoBtn.setOnClickListener(holder);

                linearLayout.addView(linearLayout1);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linearLayout1.getLayoutParams();
                params.weight = 1.0f;
                holder.deleteBtn.setLayoutParams(params);
                holder.confirmBtn.setLayoutParams(params);
                holder.cancelBtn.setLayoutParams(params);
                holder.undoBtn.setLayoutParams(params);
                holder.deleteBtn.setBackgroundColor(linearLayout.getResources().getColor(R.color.white));
                holder.confirmBtn.setBackgroundColor(linearLayout.getResources().getColor(R.color.white));
                holder.cancelBtn.setBackgroundColor(linearLayout.getResources().getColor(R.color.white));
                holder.undoBtn.setBackgroundColor(linearLayout.getResources().getColor(R.color.white));
                holder.deleteBtn.setTextColor(linearLayout.getResources().getColor(R.color.primaryTextColor));
                holder.confirmBtn.setTextColor(linearLayout.getResources().getColor(R.color.BUTTON_POSITIVE));
                holder.cancelBtn.setTextColor(linearLayout.getResources().getColor(R.color.BUTTON_NEGATIVE));
                holder.undoBtn.setTextColor(linearLayout.getResources().getColor(R.color.primaryTextColor));


                holder.confirmBtn.setVisibility(View.GONE);
                holder.cancelBtn.setVisibility(View.GONE);
                if(driverData.clientStatus!=null&&driverData.clientStatus.contentEquals(Constants.active)) {
                    holder.deleteBtn.setVisibility(View.VISIBLE);
                    holder.undoBtn.setVisibility(View.GONE);
                }
                else if(driverData.clientStatus!=null&&driverData.clientStatus.contentEquals(Constants.blocked)) {
                    holder.deleteBtn.setVisibility(View.GONE);
                    holder.undoBtn.setVisibility(View.VISIBLE);
                }
                else if(driverData.driverStatus!=null&&driverData.driverStatus.contentEquals(Constants.active)) {
                    holder.deleteBtn.setVisibility(View.VISIBLE);
                    holder.undoBtn.setVisibility(View.GONE);
                }
                else if(driverData.driverStatus!=null&&driverData.driverStatus.contentEquals(Constants.blocked)) {
                    holder.deleteBtn.setVisibility(View.GONE);
                    holder.undoBtn.setVisibility(View.VISIBLE);
                }
                else{
                    holder.deleteBtn.setVisibility(View.GONE);
                    holder.undoBtn.setVisibility(View.GONE);
                }



            }

            if(User.getInstance().isSuperAdmin()){

            }








            if(driverData.images.containsKey(Constants.frontImageSmall)&&driverData.images.get(Constants.frontImageSmall).length()>0) {
                BtnImage btnImage = new BtnImage(tableLayout.getContext());
                btnImage.setImage(driverData.images.get(Constants.frontImageSmall),database);
                imagesLinearLayout.addView(btnImage);
                horizontalScrollView.setVisibility(View.VISIBLE);
            }

            if(driverData.images.containsKey(Constants.sideImageSmall)&&driverData.images.get(Constants.sideImageSmall).length()>0) {
                BtnImage btnImage = new BtnImage(tableLayout.getContext());
                btnImage.setImage(driverData.images.get(Constants.sideImageSmall),database);
                imagesLinearLayout.addView(btnImage);
                horizontalScrollView.setVisibility(View.VISIBLE);
            }
            if(driverData.images.containsKey(Constants.backImageSmall)&&driverData.images.get(Constants.backImageSmall).length()>0) {
                BtnImage btnImage = new BtnImage(tableLayout.getContext());
                btnImage.setImage(driverData.images.get(Constants.backImageSmall),database);
                imagesLinearLayout.addView(btnImage);
                horizontalScrollView.setVisibility(View.VISIBLE);
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
                    if(response.users!=null&&response.users.size()>0){
                        mDataset.remove(null);
                        mDataset.addAll(response.users);
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
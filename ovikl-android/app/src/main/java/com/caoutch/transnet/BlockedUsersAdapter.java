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

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.MyViewHolder> {
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
                GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                        Constants.nodejs_index_url + "/unblock_zone_user/",
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
                GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                        Constants.nodejs_index_url + "/block_zone_user/",
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

            if (response.code.contentEquals("210")) {
                deleteBtn.setVisibility(View.GONE);
                confirmBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                undoBtn.setVisibility(View.VISIBLE);
            }
            else if (response.code.contentEquals("200")) {
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
    public BlockedUsersAdapter(MyVolley myVolley, Context context, String search) {
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
                    Constants.nodejs_index_url + "/blocked_zone_users/",
                    GsonResponse.class,
                    params,
                    createGetUserSuccessListener(),
                    createGetUserErrorListener());
            queue.add(myReq);


        }
        else{
            holder.user=driverData;
            holder.queue=queue;

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

            if(User.getInstance().isAdmin()){
                LinearLayout linearLayout1 = new LinearLayout(linearLayout.getContext());


                holder.deleteBtn = new Button(linearLayout.getContext());
                holder.deleteBtn.setText(tableLayout.getContext().getResources().getString(R.string.remove_block));
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
                holder.deleteBtn.setVisibility(View.VISIBLE);
                holder.undoBtn.setVisibility(View.GONE);



            }

            if(User.getInstance().isSuperAdmin()){

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
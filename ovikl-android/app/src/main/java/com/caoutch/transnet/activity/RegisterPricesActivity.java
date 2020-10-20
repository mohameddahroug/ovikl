package com.caoutch.transnet.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.BuildConfig;
import com.caoutch.transnet.Constants;
import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.view.EditTextLayout;
import com.caoutch.transnet.GsonRequest;
import com.caoutch.transnet.view.LoadingRelativeLayout;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.R;
import com.caoutch.transnet.User;
import com.caoutch.transnet.GsonResponse;
import com.caoutch.transnet.database.AppDatabase;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class RegisterPricesActivity extends SuperFragment implements View.OnClickListener {


    EditTextLayout baseCostET;
    EditTextLayout kmCostET;
    EditTextLayout minuteCostET;
    EditTextLayout minimumCostET;
    EditTextLayout currencyCostET;

    Button nextDriverBtn;
    private RequestQueue queue;

    boolean isUploading=false;

    AppDatabase database;
    View root;

    User oldUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_register_prices, container, false);

        tag="RegisterPricesActivity";

        database = Room.databaseBuilder(getContext(),
                AppDatabase.class, "caoutch").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        minimumCostET = root.findViewById(R.id.minimumCostET);
        baseCostET = root.findViewById(R.id.baseCostET);
        kmCostET = root.findViewById(R.id.kmCostET);
        minuteCostET = root.findViewById(R.id.minuteCostET);

        nextDriverBtn=root.findViewById(R.id.nextDriverBtn);
        currencyCostET=root.findViewById(R.id.currencyCostET);

        MyVolley myVolley = MyVolley.getInstance(getContext());
        queue = myVolley.getRequestQueue();
        nextDriverBtn.setOnClickListener(this);

        loadingRelativeLayout=root.findViewById(R.id.RegisterPricesActivity);
        loadingRelativeLayout.retryBtn.setOnClickListener(this);

        return root;
    }


    @Override
    public void onResume() {
        Log.i(tag,"onResume");
        super.onResume();
        if(User.getInstance()._id==null)
            getActivity().finish();
        nextDriverBtn.setEnabled(false);
        loadingRelativeLayout.loading();
        Map<String, String> params = new HashMap<>();
        params.put("_id", User.getInstance()._id);
        params.put("hashedKey", User.getInstance().hashedKey);
        GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                Constants.nodejs_index_url + "/user/",
                GsonResponse.class,
                params,
                createGetUserSuccessListener(),
                createGetUserErrorListener());
        queue.add(myReq);
    }

    @Override
    public void onClick(View v) {
        Log.i("RegisterPricesActivity",String.valueOf(v.getId()));
        if (v.getId() == R.id.nextDriverBtn) {
            if (validate())
                callServer();
        }

        else if(v==loadingRelativeLayout.retryBtn){
            loadingRelativeLayout.loading();
            Map<String, String> params = new HashMap<>();
            params.put("_id", User.getInstance()._id);
            params.put("hashedKey", User.getInstance().hashedKey);
            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                    Constants.nodejs_index_url + "/user/",
                    GsonResponse.class,
                    params,
                    createGetUserSuccessListener(),
                    createGetUserErrorListener());
            queue.add(myReq);
        }


    }



    private boolean validate(){



        if(!minimumCostET.isValid()){
            return false;
        }
        if(!baseCostET.isValid()){
            return false;
        }
        if(!kmCostET.isValid()){
            return false;
        }
        if(!minuteCostET.isValid()){
            return false;
        }
        if(!currencyCostET.isValid()){
            return false;
        }
        return true;
    }

    private void callServer(){
        nextDriverBtn.setEnabled(false);
        loadingRelativeLayout.loading();
        Map<String,String> params = new HashMap<>();
        params.put("_id",User.getInstance()._id);
        params.put("hashedKey", User.getInstance().hashedKey);
        params.put("cost.minimum",minimumCostET.getText());
        params.put("cost.base",baseCostET.getText());
        params.put("cost.km",kmCostET.getText());
        params.put("cost.minute",minuteCostET.getText());
        params.put("cost.currency",currencyCostET.getText());
        if(User.getInstance().driverStatus==null||User.getInstance().driverStatus.contentEquals(Constants.pending))
            params.put("driverStatus",Constants.active);
        GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                Constants.nodejs_index_url + "/register2/",
                GsonResponse.class,
                params,
                createSaveUserSuccessListener(),
                createSaveUserErrorListener());

        queue.add(myReq);
    }


    private Response.Listener<GsonResponse> createSaveUserSuccessListener() {
        Log.i(tag,"createGetUserSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;

                Log.i(tag, "json response code: "+ response.code);


                if(response.code.contentEquals("200")) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response);
                    Log.i(tag, "json response message: "+ json);
                    if(User.getInstance().zoneContact!=null&&(response.user.zoneContact==null||response.user.zoneContact.zone==null))
                        response.user.zoneContact=User.getInstance().zoneContact;
                    User.setInstance(response.user);
                    Log.i(tag, "saveUser user id " + User.getInstance()._id);
                    //User.getInstance().saveUser();
                    //AccountKit.logOut();
                    updateUI();
                }
                else if(response.code.contentEquals("201")) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                    toast.show();


                }
                nextDriverBtn.setEnabled(true);
                loadingRelativeLayout.loaded();
            }
        };
    }

    private Response.ErrorListener createSaveUserErrorListener() {
        Log.i(tag,"createSaveUserErrorListener");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
                Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                toast.show();

                nextDriverBtn.setEnabled(true);
                loadingRelativeLayout.loaded();
            }
        };
    }


    private Response.Listener<GsonResponse> createGetUserSuccessListener() {
        Log.i(tag,"createGetUserSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;
                Log.i(tag, "json response code: "+ response.code);
                Log.i(tag, "json response message: "+ response.message);

                if(response.code.contentEquals("200")) {
                    oldUser=response.user;
                    User.setInstance(response.user);
                    Log.i(tag, "saveUser user id " + User.getInstance()._id);
                    //User.getInstance().saveUser();
                    //AccountKit.logOut();
                    if(User.getInstance().cost.minimum!=0)
                        minimumCostET.setText(String.valueOf(User.getInstance().cost.minimum));
                    if(User.getInstance().cost.base!=0)
                        baseCostET.setText(String.valueOf(User.getInstance().cost.base));
                    if(User.getInstance().cost.km!=0)
                        kmCostET.setText(String.valueOf(User.getInstance().cost.km));
                    if(User.getInstance().cost.minute!=0)
                        minuteCostET.setText(String.valueOf(User.getInstance().cost.minute));
                    if(!User.getInstance().cost.currency.isEmpty())
                        currencyCostET.setText(String.valueOf(User.getInstance().cost.currency));
                    nextDriverBtn.setEnabled(true);
                    loadingRelativeLayout.loaded();

                }
                else{
                    loadingRelativeLayout.loadingFailed();
                }


            }
        };
    }

    private Response.ErrorListener createGetUserErrorListener() {
        Log.i(tag,"createGetUserErrorListener");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
                loadingRelativeLayout.loadingFailed();

            }
        };
    }


    private void updateUI(){
        //hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(minimumCostET.getWindowToken(), 0);

        if (oldUser.cost==null || oldUser.cost.minimum==0){
            Navigation.findNavController(root).navigate(R.id.nav_checklocation);
        }
        else{
            Toast toast = Toast.makeText(getContext(), getString(R.string.updated_successfully), Toast.LENGTH_LONG);
            toast.show();
        }
    }





}

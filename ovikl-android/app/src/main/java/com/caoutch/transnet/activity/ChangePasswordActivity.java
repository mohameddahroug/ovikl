package com.caoutch.transnet.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.Constants;
import com.caoutch.transnet.GsonRequest;
import com.caoutch.transnet.GsonResponse;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.R;
import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.User;
import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.view.EditTextLayout;
import com.caoutch.transnet.view.LoadingRelativeLayout;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends SuperFragment implements View.OnClickListener {

    Button nextBtn;
    EditTextLayout oldPasswordET;
    EditTextLayout newPasswordET;
    //ProgressBar registerProgressBar;

    private RequestQueue queue;
    AppDatabase database;
    View root;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        tag="ChangePasswordActivity";
        root = inflater.inflate(R.layout.activity_change_password, container, false);
        loadingRelativeLayout=root.findViewById(R.id.MyInfoActivity);
        nextBtn=root.findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(this);
        oldPasswordET=root.findViewById(R.id.oldPasswordET);
        newPasswordET=root.findViewById(R.id.newPasswordET);
        MyVolley myVolley = MyVolley.getInstance(getActivity());
        queue = myVolley.getRequestQueue();

        database = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "caoutch").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        loadingRelativeLayout.retryBtn.setOnClickListener(this);


        return root;
    }



    @Override
    public void onResume() {
        Log.i(tag,"onResume");
        super.onResume();
        if(User.getInstance()._id==null)
            getActivity().finish();
            nextBtn.setEnabled(false);
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
        if(v==nextBtn) {
            //user.status = Constants.active;
            if (validate())
                callServer();
        }
        else if(v==loadingRelativeLayout.retryBtn) {
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
        }


        }



    private boolean validate(){

        if(oldPasswordET.isValid()&&newPasswordET.isValid() ){
            return true;
        }
        return false;
    }

    private void callServer(){
        loadingRelativeLayout.loading();
        nextBtn.setEnabled(false);
        Map<String,String> params = new HashMap<>();
        params.put("_id",User.getInstance()._id);
        params.put("hashedKey", User.getInstance().hashedKey);
        params.put("oldPassword",oldPasswordET.getText());
        params.put("newPassword",newPasswordET.getText());
        GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                Constants.nodejs_index_url + "/register2/",
                GsonResponse.class,
                params,
                createSaveUserSuccessListener(),
                createSaveUserErrorListener());
        queue.add(myReq);
    }


    private Response.Listener<GsonResponse> createSaveUserSuccessListener() {
        Log.i(tag,"createSaveUserSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;
                Log.i(tag, "json response code: "+ response.code);
                Log.i(tag, "json response message: "+ response.message);


                if(response.code.contentEquals("200")) {
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
                else if(response.code.contentEquals("204")) {
                    oldPasswordET.textViewErr.setVisibility(View.VISIBLE);
                }
                loadingRelativeLayout.loaded();
                nextBtn.setEnabled(true);
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

                nextBtn.setEnabled(true);
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
                Log.i(tag, "json response code2: "+ response.code);
                Log.i(tag, "json response message2: "+ response.message);

                if(response.code.contentEquals("200")) {
                    User.setInstance(response.user);

                    Log.i(tag, "saveUser user id " + User.getInstance()._id);
                    if(response.config!=null)
                        User.getInstance().config=response.config;
                    //User.getInstance().saveUser();
                    //AccountKit.logOut();

                    loadingRelativeLayout.loaded();
                    nextBtn.setEnabled(true);
                }
                else if(response.code.contentEquals("201")) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                    toast.show();
                    loadingRelativeLayout.loadingFailed();
                }
            }
        };
    }

    private Response.ErrorListener createGetUserErrorListener() {
        Log.i(tag,"createGetUserErrorListener2");
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
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(oldPasswordET.getWindowToken(), 0);
//        Intent i;
//        if(User.getInstance().isClient()||User.getInstance().isAdmin()||(User.getInstance().carType!=null&&User.getInstance().cost!=null&&User.getInstance().cost.km!=0))
//            i = new Intent(getActivity(), CheckLocationActivity.class);
//        else
//            i = new Intent(getActivity(), RegisterPricesActivity.class);
//        startActivity(i);
//        getActivity().finish();
       Toast toast = Toast.makeText(getContext(), getString(R.string.updated_successfully), Toast.LENGTH_LONG);
       toast.show();
    }




}

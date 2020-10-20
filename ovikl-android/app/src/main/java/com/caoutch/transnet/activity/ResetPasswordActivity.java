package com.caoutch.transnet.activity;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
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
import com.caoutch.transnet.Constants;
import com.caoutch.transnet.GsonRequest;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.R;
import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.User;
import com.caoutch.transnet.GsonResponse;
import com.caoutch.transnet.view.EditTextLayout;
import com.caoutch.transnet.view.LoadingRelativeLayout;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends SuperFragment implements View.OnClickListener {


    Button nextBtn;
    Button resetBtn;
    EditTextLayout passwordET;
    //EditTextLayout repasswordET;
    EditTextLayout resetKeyET;
    EditTextLayout emailET;
    private RequestQueue queue;
    private User user;

    View root;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_reset_password, container, false);
        tag="ResetPasswordActivity";
        super.onCreate(savedInstanceState);
        loadingRelativeLayout=root.findViewById(R.id.ResetPasswordActivity);
        passwordET=root.findViewById(R.id.passwordET);
        //repasswordET=findViewById(R.id.repasswordET);
        resetKeyET=root.findViewById(R.id.resetKeyET);
        emailET=root.findViewById(R.id.emailET);
        nextBtn=root.findViewById(R.id.nextBtn);
        resetBtn=root.findViewById(R.id.resetBtn);

        MyVolley myVolley = MyVolley.getInstance(getActivity());
        queue = myVolley.getRequestQueue();

        user = User.getInstance();

        nextBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);

        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        //user = User.getInstance();
        Log.i(tag, "onResume");
        loadingRelativeLayout.loaded();
    }

    @Override
    public void onClick(View v) {
        if(v==nextBtn) {
            //user.status = Constants.active;
            if (validate())
                callServer();
        }
        if(v==resetBtn){
            if(emailET.isValid()){
                loadingRelativeLayout.loading();
                Map<String,String> params = new HashMap<>();
                params.put("email",emailET.getText());
                GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                        Constants.nodejs_index_url + "/generate_reset_key/",
                        GsonResponse.class,
                        params,
                        createResetSuccessListener(),
                        createResetErrorListener());

                queue.add(myReq);
            }
        }


    }



    private boolean validate(){
        if(emailET.isValid()&&resetKeyET.isValid()&&passwordET.isValid()/*&&
                repasswordET.isValid()&&
                repasswordET.match(passwordET.getText())*/){
            return true;
        }
        return false;
    }

    private void callServer(){
        loadingRelativeLayout.loading();
        Map<String,String> params = new HashMap<>();
        params.put("resetKey",resetKeyET.getText());
        params.put("password",passwordET.getText());
        params.put("email",emailET.getText());
        GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                Constants.nodejs_index_url + "/reset_password/",
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
                Log.i(tag, "json response message: "+ response.message);

                if(response.code.contentEquals("200")) {
                    User.setInstance(response.user);
                    user=User.getInstance();
                    Log.i(tag, "saveUser user id " + user._id);
                    //user.saveUser();
                    //AccountKit.logOut();
                    updateUI();
                }
                else if(response.code.contentEquals("201")) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.invalid_reset), Toast.LENGTH_LONG);
                    toast.show();
                    loadingRelativeLayout.loaded();
                }
            }
        };
    }

    private Response.ErrorListener createSaveUserErrorListener() {
        Log.i(tag,"createGetUserErrorListener");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
                Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                toast.show();

                loadingRelativeLayout.loaded();
            }
        };
    }


    private Response.Listener<GsonResponse> createResetSuccessListener() {
        Log.i(tag,"createResetSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;
                Log.i(tag, "json response code: "+ response.code);

                if(response.code.contentEquals("200")) {
                    loadingRelativeLayout.loaded();
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.check_reset_email), Toast.LENGTH_LONG);
                    toast.show();
                }
                else if(response.code.contentEquals("201")) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                    toast.show();
                    loadingRelativeLayout.loaded();
                }
            }
        };
    }

    private Response.ErrorListener createResetErrorListener() {
        Log.i(tag,"createResetErrorListener");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
                Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                toast.show();

                loadingRelativeLayout.loaded();
            }
        };
    }


    private void updateUI(){
        //hide keyboard
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        catch(Exception e){

        }
        //Intent i = new Intent(this, CheckLocationActivity.class);
        //startActivity(i);


        if(User.getInstance().isClient()&&User.getInstance().clientStatus.contentEquals(Constants.active)) {
            Intent i = new Intent(getActivity(), CheckLocationActivity.class);
            startActivity(i);

        }
        else if(User.getInstance().isDriver()&&User.getInstance().driverStatus.contentEquals(Constants.active)) {
            Intent i = new Intent(getActivity(), CheckLocationActivity.class);
            startActivity(i);
        }
        else if(User.getInstance().isDriver() && User.getInstance().driverStatus.contentEquals(Constants.pending)){
            Intent i = new Intent(getActivity(), RegisterCarActivity2.class);
            startActivity(i);
        }
        else{
            Intent i = new Intent(getActivity(), LoginActivity.class);
            startActivity(i);
        }
    }




}

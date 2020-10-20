package com.caoutch.transnet.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.room.Room;

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
import com.caoutch.transnet.view.BtnImage;
import com.caoutch.transnet.view.EditTextLayout;
import com.caoutch.transnet.view.LoadingRelativeLayout;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends SuperFragment implements View.OnClickListener {


    BtnImage clientBtn;
    BtnImage driverBtn;
    Button nextBtn;
    EditTextLayout firstNameET;

    EditTextLayout lastNameET;

    EditTextLayout passwordET;

    //EditTextLayout repasswordET;

    EditTextLayout mobileET;

    EditTextLayout emailET;

    EditTextLayout zoneET;

    TextView registerTV;
    TextView contactZoneTV;

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
        root = inflater.inflate(R.layout.activity_register, container, false);

        tag="RegisterActivity";
        loadingRelativeLayout=root.findViewById(R.id.RegisterActivity);

        clientBtn=root.findViewById(R.id.clientBtn);
        driverBtn=root.findViewById(R.id.driverBtn);
        nextBtn=root.findViewById(R.id.nextBtn);

        clientBtn.setOnClickListener(this);
        driverBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        firstNameET=root.findViewById(R.id.firstNameET);
        lastNameET=root.findViewById(R.id.lastNameET);
        passwordET=root.findViewById(R.id.passwordET);
        //repasswordET=root.findViewById(R.id.repasswordET);
        mobileET=root.findViewById(R.id.mobileET);
        emailET=root.findViewById(R.id.emailET);
        zoneET=root.findViewById(R.id.zoneET);
        registerTV=root.findViewById(R.id.register_tv);
        contactZoneTV=root.findViewById(R.id.contactZoneTV);
        MyVolley myVolley = MyVolley.getInstance(getActivity());
        queue = myVolley.getRequestQueue();

        database = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "caoutch").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        clientBtn.setImage("/client.png",database);
        driverBtn.setImage("/driver.png",database);

        return root;
    }




    @Override
    public void onResume() {
        clientBtn.setVisibility(View.VISIBLE);
        driverBtn.setVisibility(View.VISIBLE);
        loadingRelativeLayout.loaded();
        super.onResume();
    }




    @Override
    public void onClick(View v) {
        if(v==clientBtn){
            Log.v(tag,"clientBtn");
            User.getInstance().type=Constants.client;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                clientBtn.setBackground(getResources().getDrawable(R.drawable.rounded_border));
                driverBtn.setBackground(null);
            }
            else {
                clientBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_border));
                driverBtn.setBackgroundDrawable(null);
            }
            registerTV.setText(getResources().getString(R.string.register_as_client));
            registerTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        else if(v==driverBtn){
            Log.v(tag,"driverBtn");
            User.getInstance().type=Constants.driver;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                driverBtn.setBackground(getResources().getDrawable(R.drawable.rounded_border));
                clientBtn.setBackground(null);
            }
            else {
                driverBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_border));
                clientBtn.setBackgroundDrawable(null);
            }
            registerTV.setText(getResources().getString(R.string.register_as_driver));
            registerTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        else if(v==nextBtn) {
            //user.status = Constants.active;
            if (validate())
                callServer();
        }


    }



    private boolean validate(){
        //mobileET.setText(mobileET.getText().replace("+","00"));
        contactZoneTV.setVisibility(View.GONE);
        if(User.getInstance().type==null||User.getInstance().type.isEmpty()){
            registerTV.setTextColor(getResources().getColor(R.color.red));
            registerTV.requestFocus();
            return false;
        }

        if(firstNameET.isValid()&&lastNameET.isValid()&&passwordET.isValid()&&
                /*repasswordET.isValid()&&*/mobileET.isValid()&&emailET.isValid()/*&&
                repasswordET.match(passwordET.getText())*/&&zoneET.isValid()){
            return true;
        }
        return false;
    }

    private void callServer(){
        //clientBtn.setVisibility(View.GONE);
        //driverBtn.setVisibility(View.GONE);
        loadingRelativeLayout.loading();
        Map<String,String> params = new HashMap<>();
        params.put("firstName",firstNameET.getText());
        params.put("lastName",lastNameET.getText());
        params.put("password",passwordET.getText());
        params.put("mobile",mobileET.getText());
        params.put("email",emailET.getText());
        params.put("type",User.getInstance().type);
        params.put("zone",zoneET.getText());

        if(User.getInstance()._id==null||User.getInstance()._id.isEmpty()) {
            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                    Constants.nodejs_index_url + "/register/",
                    GsonResponse.class,
                    params,
                    createGetUserSuccessListener(),
                    createGetUserErrorListener());

            queue.add(myReq);
        }
        else{
            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                    Constants.nodejs_index_url + "/register/",
                    GsonResponse.class,
                    params,
                    createGetUserSuccessListener(),
                    createGetUserErrorListener());

            queue.add(myReq);
        }
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
                    User.setInstance(response.user);
                    Log.i(tag, "saveUser user id " + User.getInstance()._id);
                    //User.getInstance().saveUser();
                    //AccountKit.logOut();
                    updateUI();
                }
                else if(response.code.contentEquals("201")) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.login_err), Toast.LENGTH_LONG);
                    toast.show();

                    clientBtn.setVisibility(View.VISIBLE);
                    driverBtn.setVisibility(View.VISIBLE);
                    loadingRelativeLayout.loaded();
                }
                else if(response.code.contentEquals("202")) {
                    loadingRelativeLayout.loaded();
                    zoneET.textViewErr.setVisibility(View.VISIBLE);
                }
                else if(response.code.contentEquals("203")) {

                    loadingRelativeLayout.loaded();
                    String errString = getString(R.string.contact_zone_admin);
                    errString=errString.replace("%mobile%",response.zone.mobile);
                    errString=errString.replace("%email%",response.zone.email);
                    errString=errString.replace("%zone%",response.zone.zone);
                    contactZoneTV.setText(errString);
                    contactZoneTV.setVisibility(View.VISIBLE);
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
                Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                toast.show();

                //clientBtn.setVisibility(View.VISIBLE);
                //driverBtn.setVisibility(View.VISIBLE);
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

        Toast toast = Toast.makeText(getContext(), getString(R.string.updated_successfully), Toast.LENGTH_LONG);
        toast.show();
        if (User.getInstance().isDriver())
            Navigation.findNavController(root).navigate(R.id.nav_register_car);
        else{
            Navigation.findNavController(root).navigate(R.id.nav_checklocation);
        }
    }



}

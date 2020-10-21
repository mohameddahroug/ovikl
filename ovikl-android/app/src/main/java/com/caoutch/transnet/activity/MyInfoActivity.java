package com.caoutch.transnet.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
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
import com.caoutch.transnet.view.BtnImage;
import com.caoutch.transnet.view.EditTextLayout;
import com.caoutch.transnet.view.LoadingRelativeLayout;

import java.util.HashMap;
import java.util.Map;

public class MyInfoActivity extends SuperFragment implements View.OnClickListener {

    Button nextBtn;
    EditTextLayout firstNameET;
    EditTextLayout lastNameET;
    EditTextLayout mobileET;
    EditTextLayout zoneET;
    TextView contactZoneTV;
    //ProgressBar registerProgressBar;


    private RequestQueue queue;
    AppDatabase database;
    View root;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.my_info_register, container, false);
        tag="MyInfoActivity";
        super.onCreate(savedInstanceState);
        loadingRelativeLayout=root.findViewById(R.id.MyInfoActivity);
        nextBtn=root.findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(this);
        firstNameET=root.findViewById(R.id.firstNameET);
        lastNameET=root.findViewById(R.id.lastNameET);
        mobileET=root.findViewById(R.id.mobileET);
        MyVolley myVolley = MyVolley.getInstance(getActivity());
        queue = myVolley.getRequestQueue();

        database = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "caoutch").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        loadingRelativeLayout.retryBtn.setOnClickListener(this);
        zoneET=root.findViewById(R.id.myZoneET);
        contactZoneTV=root.findViewById(R.id.myContactZoneTV);
        if(User.getInstance().isAdmin())
            zoneET.editText.setEnabled(false);

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i(tag,"onResume");
        super.onResume();
        if(User.getInstance()._id==null)
            nextBtn.setVisibility(View.GONE);
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

        contactZoneTV.setVisibility(View.GONE);
        if(firstNameET.isValid()&&lastNameET.isValid()&&mobileET.isValid()&& zoneET.isValid() ){
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
        params.put("email", User.getInstance().email);
        params.put("firstName",firstNameET.getText());
        params.put("lastName",lastNameET.getText());
        params.put("mobile",mobileET.getText());
        params.put("zone",zoneET.getText());
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
                else if(response.code.contentEquals("202")) {
                    zoneET.textViewErr.setVisibility(View.VISIBLE);

                }
                else if(response.code.contentEquals("203")) {
                    String errString = getString(R.string.contact_zone_admin);
                    errString=errString.replace("%mobile%",response.zone.mobile);
                    errString=errString.replace("%email%",response.zone.email);
                    errString=errString.replace("%zone%",response.zone.zone);
                    contactZoneTV.setText(errString);
                    contactZoneTV.setVisibility(View.VISIBLE);

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

                loadingRelativeLayout.loaded();
                nextBtn.setEnabled(true);
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

                    if(User.getInstance().firstName!=null&&!User.getInstance().firstName.isEmpty())
                        firstNameET.setText(User.getInstance().firstName);
                    if(User.getInstance().lastName!=null&&!User.getInstance().lastName.isEmpty())
                        lastNameET.setText(User.getInstance().lastName);
                    if(User.getInstance().mobile!=null&&!User.getInstance().mobile.isEmpty())
                        mobileET.setText(User.getInstance().mobile);
                    if(User.getInstance().zone!=null&&!User.getInstance().zone.isEmpty())
                        zoneET.setText(User.getInstance().zone);
                    nextBtn.setVisibility(View.VISIBLE);
                    loadingRelativeLayout.loaded();


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
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(firstNameET.getWindowToken(), 0);

        if(User.getInstance().isDriver()&&(User.getInstance().carType==null||User.getInstance().cost==null||User.getInstance().cost.km==0)){
            Navigation.findNavController(root).navigate(R.id.nav_register_prices);
        }
        else{
            Toast toast = Toast.makeText(getContext(), getString(R.string.updated_successfully), Toast.LENGTH_LONG);
            toast.show();
        }

    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.support_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case R.id.item_support:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "support@yourdomain.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Ovikl support");
                //need this to prompts email client only
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email client :"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, CheckLocationActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent i = new Intent(this, CheckLocationActivity.class);
        startActivity(i);
        finish();
        return true;
    }*/

}

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.Constants;
import com.caoutch.transnet.GsonRequest;
import com.caoutch.transnet.Main4Activity;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.R;
import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.User;
import com.caoutch.transnet.GsonResponse;
import com.caoutch.transnet.view.BtnImage;
import com.caoutch.transnet.view.EditTextLayout;
import com.caoutch.transnet.view.LoadingRelativeLayout;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;
/*import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;*/

public class LoginActivity extends SuperFragment {
    public static int APP_REQUEST_CODE = 99;
    private RequestQueue queue ;
    //private User user;
    Button loginClientBtn2;
    //Button loginDriverBtn2;
    EditTextLayout userNameEditText;
    EditTextLayout passwordEditText;

    TextView loginErrTextView;
    Button forgetPasswordBtn;
    Button registerBtn;
    Button sendVerifyEmailBtn;
    //UIManager uiManager;
    EditTextLayout zoneET;
    TextView contactZoneTV;
    View root;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tag = "LoginActivity";
        root = inflater.inflate(R.layout.activity_login, container, false);
        
        loadingRelativeLayout=root.findViewById(R.id.activity_info);
        Log.i(tag,"onCreate");
        loginClientBtn2=root.findViewById(R.id.loginClientBtn2);
        //loginDriverBtn2=root.findViewById(R.id.loginDriverBtn2);
        loginErrTextView=root.findViewById(R.id.loginErrTextView);
        userNameEditText=root.findViewById(R.id.userNameEditText);
        passwordEditText=root.findViewById(R.id.passwordEditText);
        sendVerifyEmailBtn=root.findViewById(R.id.sendVerifyEmailBtn);
        loginClientBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //user.type="client";
                login();
            }
        });

        forgetPasswordBtn=root.findViewById(R.id.forgetPasswordBtn);
        forgetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Navigation.findNavController(root).navigate(R.id.nav_resetpassword);
            }
        });
        registerBtn=root.findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.nav_register);
            }
        });
        sendVerifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resend();
            }
        });
        MyVolley myVolley = MyVolley.getInstance(getContext());
        queue = myVolley.getRequestQueue();

        zoneET=root.findViewById(R.id.infoZoneET);
        contactZoneTV=root.findViewById(R.id.infoContactZoneTV);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        //user = User.getInstance();
        Log.i(tag,"onResume");
        loadingRelativeLayout.loaded();
        /*AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                Log.i(tag, "getCurrentAccount phone:%s..."+ account.getPhoneNumber().toString());
                user.auth="mobile";
                user.id=account.getPhoneNumber().toString();
                user.saveUser(InfoActivity.this);
                loginClientBtn2.setVisibility(View.VISIBLE);
                loginDriverBtn2.setVisibility(View.VISIBLE);
                userNameEditText.setVisibility(View.VISIBLE);
                if(user.name!=null)
                    userNameEditText.setText(user.name);

            }

            @Override
            public void onError(final AccountKitError error) {
                Log.i(tag, "getCurrentAccount error:"+ error.getDetailErrorCode());
                phoneLogin();
            }
        });*/
        loginErrTextView.setText("");
        if(User.getInstance().email!=null){
            userNameEditText.setText(User.getInstance().email);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    void login(){
        loginErrTextView.setText("");
        //userNameEditText.setText(userNameEditText.getText().toString().trim());
        //passwordEditText.setText(passwordEditText.getText().toString().trim());
        contactZoneTV.setVisibility(View.GONE);
        if(userNameEditText.isValid()&& passwordEditText.isValid() && zoneET.isValid()) {

            loadingRelativeLayout.loading();
            Map<String,String> params = new HashMap<>();
            params.put("email",userNameEditText.getText());
            params.put("password",passwordEditText.getText());
            params.put("zone",zoneET.getText());
            GsonBuilder gsonBuilder=new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                    Constants.nodejs_index_url + "/login/",
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
                loadingRelativeLayout.loaded();
                if(response.code.contentEquals("200")) {
                    User.setInstance(response.user);
                    //user=User.getInstance();
                    //Log.i(tag, "saveUser user id " + user._id);
                    //User.getInstance().saveUser();
                    //AccountKit.logOut();
                    updateUI();
                }
                else if(response.code.contentEquals("201")) {
                    //Toast toast = Toast.makeText(InfoActivity.this, getString(R.string.retry), Toast.LENGTH_LONG);
                    //toast.show();
                    loginErrTextView.setText(R.string.login_err2);
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
            }
        };
    }

    private Response.ErrorListener createGetUserErrorListener() {
        Log.i(tag,"createGetUserErrorListener");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
                //Toast toast = Toast.makeText(InfoActivity.this, getString(R.string.retry), Toast.LENGTH_LONG);
                //toast.show();

                loadingRelativeLayout.loaded();
                loginErrTextView.setText(R.string.retry);
            }
        };
    }


    void resend(){
        loginErrTextView.setText("");
        //userNameEditText.setText(userNameEditText.getText().toString().trim());
        //passwordEditText.setText(passwordEditText.getText().toString().trim());
        if(userNameEditText.isValid()) {

            //loadingRelativeLayout.loading();
            Map<String,String> params = new HashMap<>();
            params.put("email",userNameEditText.getText());
            GsonBuilder gsonBuilder=new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                    Constants.nodejs_index_url + "/resend/",
                    GsonResponse.class,
                    params,
                    createGetUserSuccessListener2(),
                    createGetUserErrorListener2());
            queue.add(myReq);
        }

    }

    private Response.Listener<GsonResponse> createGetUserSuccessListener2() {
        Log.i(tag,"createGetUserSuccessListener2");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;
                Log.i(tag, "json response code: "+ response.code);
                loadingRelativeLayout.loaded();
                if(response.code.contentEquals("200")) {
                    Toast toast = Toast.makeText(getContext(), getString(R.string.login_active_email), Toast.LENGTH_LONG);
                    toast.show();
                    sendVerifyEmailBtn.setVisibility(View.GONE);
                }
                else if(response.code.contentEquals("201")) {
                    Toast toast = Toast.makeText(getContext(), getString(R.string.retry), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        };
    }

    private Response.ErrorListener createGetUserErrorListener2() {
        Log.i(tag,"createGetUserErrorListener2");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
                Toast toast = Toast.makeText(getContext(), getString(R.string.retry), Toast.LENGTH_LONG);
                toast.show();

            }
        };
    }

    private void updateUI(){
        //hide keyboard
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(userNameEditText.getWindowToken(), 0);


        /*if(!User.getInstance().emailVerified){
            loginErrTextView.setText(R.string.login_active_email);
            sendVerifyEmailBtn.setVisibility(View.VISIBLE);
        }
        else */




        if((User.getInstance().isClient()&&User.getInstance().clientStatus.contentEquals(Constants.active)) ||
            (User.getInstance().isDriver()&&User.getInstance().driverStatus.contentEquals(Constants.active))||
            (User.getInstance().isAdmin() && User.getInstance().adminStatus.contentEquals(Constants.active))){
            ((Main4Activity)getActivity()).updateNavMenu();

            Navigation.findNavController(root).navigate(R.id.nav_checklocation);
        }
        else if(User.getInstance().isDriver()&&User.getInstance().driverStatus.contentEquals(Constants.pending)) {
            Navigation.findNavController(root).navigate(R.id.nav_register_car);
        }
        else{
            loginErrTextView.setText(R.string.user_blocked);
        }


    }





    /*public void phoneLogin() {
        final Intent intent = new Intent(InfoActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        uiManager = new SkinManager(SkinManager.Skin.CLASSIC, getResources().getColor(R.color.colorPrimary),R.drawable.background ,SkinManager.Tint.BLACK,0.75);
        configurationBuilder.setUIManager(uiManager);
        //configurationBuilder.setDefaultCountryCode("EG");
        //String[] countries={"EG", "SA"};
        //configurationBuilder.setSMSWhitelist(countries);
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }*/



    /*@Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                Log.i(tag, "onActivityResult Error: "+ loginResult.getError().getErrorType().getMessage());
                Log.i(tag, "onActivityResult Error: "+ loginResult.getError() );
            } else if (loginResult.wasCancelled()) {
                Log.i(tag, "onActivityResult Cancelled: "+ loginResult.getError() );
            } else {
                if (loginResult.getAccessToken() != null) {
                    Log.i(tag, "onActivityResult Success:" + loginResult.getAccessToken().getAccountId());
                } else {
                    Log.i(tag, "onActivityResult Success:%s..."+ loginResult.getAuthorizationCode());
                }


            }
        }
    }*/


}

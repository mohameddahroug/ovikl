package com.caoutch.transnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.caoutch.transnet.activity.LoginActivity;
import com.caoutch.transnet.activity.MainActivity;
import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.view.BtnImage;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import java.util.TimeZone;

public class Main4Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private Intent intentService;
    private String tag = "Main4Activity";
    public NavigationView navigationView;
    NavController navController;
    AppDatabase database;
    DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main4);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        database = Room.databaseBuilder(this,
                AppDatabase.class, "caoutch").fallbackToDestructiveMigration().build();


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(tag, "onResume");
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,R.id.nav_my_info,R.id.nav_change_password,R.id.nav_last_trip,R.id.nav_register_car,R.id.nav_register_prices
                ,R.id.nav_users,R.id.nav_trips,R.id.nav_blocked_users)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        Menu nav_Menu = navigationView.getMenu();
        if(User.getInstance()._id!=null) {
            if (User.getInstance().isDriver()) {

                //nav_Menu.findItem(R.id.nav_register_driver).setVisible(true);
                nav_Menu.findItem(R.id.nav_register_car).setVisible(true);
                nav_Menu.findItem(R.id.nav_register_prices).setVisible(true);
            }
            if (User.getInstance().isAdmin()) {
                nav_Menu.findItem(R.id.nav_last_trip).setVisible(false);
                nav_Menu.findItem(R.id.nav_users).setVisible(true);
                nav_Menu.findItem(R.id.nav_trips).setVisible(true);
            }

            if (User.getInstance().type.contentEquals("admin")) {
                nav_Menu.findItem(R.id.nav_blocked_users).setVisible(true);
            }

            if (User.getInstance().zoneContact != null && User.getInstance().zoneContact.mobile != null) {
                nav_Menu.findItem(R.id.nav_call_support).setVisible(true);
            }
        }


        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.i(tag,"onDestinationChanged");
                if(destination.getId() == R.id.nav_home) {


                }
                else {

                }
            }
        });


        AdView mAdView = findViewById(R.id.adViewMain);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (User.getInstance().email == null||User.getInstance().email.isEmpty()
                ||User.getInstance().hashedKey==null||User.getInstance().hashedKey.isEmpty()) {




        }
        else {



            View headerView = navigationView.getHeaderView(0);
            TextView nameTextView = headerView.findViewById(R.id.nameTextView);
            //emailTextView = headerView.findViewById(R.id.emailTextView);
            //typeTextView=headerView.findViewById(R.id.typeTextView);

            //nameTextView.setText(User.getInstance().firstName == null ? "" : User.getInstance().firstName+" "+User.getInstance().lastName);
            //emailTextView.setText(User.getInstance().email == null ? "" : User.getInstance().email);
            //typeTextView.setText(User.getInstance().isDriver() ? getResources().getString(R.string.driver) : getResources().getString(R.string.client));
            String s = User.getInstance().firstName == null ? "" : User.getInstance().firstName + " " + User.getInstance().lastName;
            s = s + (User.getInstance().email == null ? "" : "\n" + User.getInstance().email);
            if (User.getInstance().isDriver())
                s = s + "\n" + getResources().getString(R.string.driver);
            else if (User.getInstance().isClient())
                s = s + "\n" + getResources().getString(R.string.client);
            else if (User.getInstance().isAdmin())
                s = s + "\n" + getResources().getString(R.string.admin);
            if (User.getInstance().zone != null && !User.getInstance().zone.isEmpty()) {
                s = s + " " + getResources().getString(R.string.in) + " " + User.getInstance().zone;
            }
            nameTextView.setText(s);

            if(User.getInstance().isDriver()&&User.getInstance().images!=null&&User.getInstance().images.get(Constants.frontImageSmall)!=null){

                BtnImage btnImage = headerView.findViewById(R.id.imageView);
                btnImage.setImage(User.getInstance().images.get(Constants.frontImageSmall),database);
            }
        }

        Intent appLinkIntent = getIntent();
        if(appLinkIntent != null) {
            Uri appLinkData = appLinkIntent.getData();
            if (appLinkData != null) {
                String url = "https://" + appLinkData.getHost() + appLinkData.getPath() + "?" + appLinkData.getQuery();
                Log.i(tag, url);
                if (appLinkData.getHost().contains("index.ovikl.com") && appLinkData.getPath().contains("/verify_email")) {
                    MyVolley myVolley = MyVolley.getInstance(this);
                    RequestQueue queue = myVolley.getRequestQueue();
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, null, null);
                    queue.add(stringRequest);
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.app_bar_layout).setOutlineProvider(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(tag, "onPause");
        if ((User.getInstance().isClient()||User.getInstance().isAdmin())
                ||(User.getInstance().isDriver()&&User.getInstance().driverStatus!=null&&!User.getInstance().driverStatus.contentEquals(Constants.active))
        ){
            stopTCPService();
            //finish();
        }

    }

    @Override
    public void onDestroy() {
        Log.i(tag, "onDestroy");
        stopTCPService();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.support_main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }





    void support(){
        Log.i(tag, "support");
        Intent email = new Intent(Intent.ACTION_SEND);

        if(User.getInstance().zoneContact!=null&&User.getInstance().zoneContact.email!=null){
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{ User.getInstance().zoneContact.email});
            email.putExtra(Intent.EXTRA_CC, new String[]{ "support@ovikl.com"});
        }
        else{
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "support@ovikl.com"});
        }
        email.putExtra(Intent.EXTRA_SUBJECT, "Ovikl support");
        //need this to prompts email client only
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Choose an Email client :"));


    }



    public void stopTCPService(){
        Log.i(tag, "stopTCPService");
        try {
            stopService(intentService);
        } catch (Exception e) {
            Log.i(tag,"stopTCPService"+e.getMessage());
        }
        intentService=null;
    }


    public void startTCPService(){
        Log.i(tag, "startTCPService");
        if(intentService==null) {
            intentService = new Intent(this, TCPService.class);
            startService(intentService);
        }
    }


    public void updateNavMenu(){
        Menu nav_Menu = navigationView.getMenu();
        if (User.getInstance().isDriver()) {

            //nav_Menu.findItem(R.id.nav_register_driver).setVisible(true);
            nav_Menu.findItem(R.id.nav_register_car).setVisible(true);
            nav_Menu.findItem(R.id.nav_register_prices).setVisible(true);
        }
        if (User.getInstance().isAdmin()) {
            nav_Menu.findItem(R.id.nav_last_trip).setVisible(false);
            nav_Menu.findItem(R.id.nav_users).setVisible(true);
            nav_Menu.findItem(R.id.nav_trips).setVisible(true);
        }

        if (User.getInstance().type.contentEquals("admin")) {
            nav_Menu.findItem(R.id.nav_blocked_users).setVisible(true);
        }

        if (User.getInstance().zoneContact != null && User.getInstance().zoneContact.mobile != null) {
            nav_Menu.findItem(R.id.nav_call_support).setVisible(true);
        }

        View headerView = navigationView.getHeaderView(0);
        TextView nameTextView = headerView.findViewById(R.id.nameTextView);String s = User.getInstance().firstName == null ? "" : User.getInstance().firstName + " " + User.getInstance().lastName;
        s = s + (User.getInstance().email == null ? "" : "\n" + User.getInstance().email);
        if (User.getInstance().isDriver())
            s = s + "\n" + getResources().getString(R.string.driver);
        else if (User.getInstance().isClient())
            s = s + "\n" + getResources().getString(R.string.client);
        else if (User.getInstance().isAdmin())
            s = s + "\n" + getResources().getString(R.string.admin);
        if (User.getInstance().zone != null && !User.getInstance().zone.isEmpty()) {
            s = s + " " + getResources().getString(R.string.in) + " " + User.getInstance().zone;
        }
        nameTextView.setText(s);

        if(User.getInstance().isDriver()&&User.getInstance().images!=null&&User.getInstance().images.get(Constants.frontImageSmall)!=null){

            BtnImage btnImage = headerView.findViewById(R.id.imageView);
            btnImage.setImage(User.getInstance().images.get(Constants.frontImageSmall),database);
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i(tag,"onNavigationItemSelected");
        int id = item.getItemId();
        drawer.closeDrawer(GravityCompat.START);
        if (id == R.id.nav_home) {
            navController.navigate(item.getItemId());
            return true;
        }
        if (id == R.id.nav_support) {
            support();
            return false;
        }
        else if (id == R.id.nav_about) {
            String url = Constants.url;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            //finish();
            return false;
        }
        else if (id == R.id.nav_logout) {
            AlertDialog.Builder logoutBuilder= new AlertDialog.Builder(Main4Activity.this, R.style.CustomDialog);
            logoutBuilder.setMessage(R.string.sure_logout);
            logoutBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    User.getInstance().reset();
                    navController.navigate(R.id.nav_checklocation);
                    stopTCPService();
                }
            });
            logoutBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog

                }
            });
            AlertDialog dialog = logoutBuilder.create();
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getColor(R.color.BUTTON_NEGATIVE));
                Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(getColor(R.color.BUTTON_POSITIVE));
            }
            return false;
        }


        else if(id == R.id.nav_call_support){
            if(User.getInstance().zoneContact!=null&&User.getInstance().zoneContact.mobile!=null) {
                String tel = User.getInstance().zoneContact.mobile;


                if (tel != null) {
                    AlertDialog.Builder callBuilder = new AlertDialog.Builder(Main4Activity.this, R.style.CustomDialog);

                    final String finalTel = tel;
                    callBuilder.setMessage(getString(R.string.call_to) + " " + finalTel);

                    callBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + finalTel));
                            startActivity(intent);
                            Main4Activity.this.overridePendingTransition(0, 0);
                            //finish();
                        }
                    });

                    AlertDialog dialog = callBuilder.create();
                    dialog.show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        nbutton.setTextColor(getColor(R.color.BUTTON_NEGATIVE));
                        Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        pbutton.setTextColor(getColor(R.color.BUTTON_POSITIVE));
                    }
                }
            }
            return false;
        }
        else if(id == R.id.nav_checklocation){
            navController.navigate(item.getItemId());
            return true;
        }
        else {
            navController.navigate(item.getItemId());
            stopTCPService();
            return true;
        }
    }
}
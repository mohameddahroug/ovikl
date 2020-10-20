package com.caoutch.transnet.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.room.Room;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.BuildConfig;
import com.caoutch.transnet.GetFile;
import com.caoutch.transnet.GsonRequest;
import com.caoutch.transnet.GsonResponse;
import com.caoutch.transnet.Main4Activity;
import com.caoutch.transnet.MyApplication;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.view.BtnImage;
import com.caoutch.transnet.view.EditTextLayout;
import com.caoutch.transnet.view.LoadingRelativeLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.collection.ArrayMap;

import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.caoutch.transnet.Constants;
import com.caoutch.transnet.R;
import com.caoutch.transnet.TCPResponse;
import com.caoutch.transnet.TripSingleton;
import com.caoutch.transnet.TCPService;
import com.caoutch.transnet.User;
import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.database.TripMessage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
//import com.stripe.android.model.Card;
//import com.stripe.android.view.CardMultilineWidget;

public class MainActivity extends SuperFragment
        implements  OnMapReadyCallback,
        View.OnClickListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener,
        CompoundButton.OnCheckedChangeListener{

    private GoogleMap mMap;


    private static final int DEFAULT_ZOOM = 15;
    //private TCPService mService;
    private boolean mBound = false;
    /** Defines callbacks for service binding, passed to bindService() */
    //private Handler handler=new Handler();
    //private final int handlerDelay=5000;
    //private Intent intent ;

    private SharedPreferences sharedPref;
    final static String TRIPSTARTED = "serviceLocationStarted";

    private AppDatabase database;
    //private Trip trip;
    private LatLng lastLatLng;

    //FloatingActionButton fab;
    private SupportMapFragment mapFragment;

    private ImageButton cancelBtn;
    private Button arrivedBtn;
    private Button finishBtn;
    private ImageButton okBtn;
    private ImageButton chatBtn;
    private ImageButton mapBtn;
    private ImageButton callBtn;

    private Button sendMessageBtn;
    private TextView summaryTV;
    private LinearLayout chatLayout;
    private LinearLayout chatMsgLayout;
    private LinearLayout expectedLayout;

    private EditText chatMsgEditText;
    private ScrollView chatMsgScroll;
    private boolean firstLocation = true;
    //private User user;

    private LinearLayout tripLayout;
    private TextView tripDurationTextView;
    private TextView tripDistanceTextView;
    private TextView tripCostTextView;

    private Marker clientLocationMarker;
    private Map<String, Marker> drivers;
    private Map<String, User> driversInfo;
    private Map<String, Marker> clients;
    //private boolean clientChangeLocationEnabled;
    //private boolean clientChooseDriverEnabled;

    private User clientJson;
    private int unreadedMessagesCount = 0;
    private Toast toast;
    ConcurrentLinkedQueue<JSONObject> toastQueue = new ConcurrentLinkedQueue<>();
    /*Polyline polylinePending;
    PolylineOptions polylineOptionsPending;
    Polyline polylineReserved;
    PolylineOptions polylineOptionsReserved;
    Polyline polylineStarted;
    PolylineOptions polylineOptionsStarted;*/
    private TripSingleton tripSingleton = TripSingleton.getInstance();
    private LatLng latLng;

    private TextView nameTextView;
    //private TextView emailTextView;
    //private TextView typeTextView;
    private AdView mAdView;
    private ProgressBar loading;
    private TextView loadingTextView;
    private RelativeLayout progressBarLayout;

    //private boolean mapIsReady = false;
    //private boolean isResumed = false;
    //private boolean menuIsReady = false;
    private Menu menu;
    private MenuItem cancelItem;
    private MenuItem arrivedItem;
    private MenuItem finishItem;
    private MenuItem okItem;
    private MenuItem chatItem;
    private MenuItem mapItem;
    private MenuItem callItem;
    private MenuItem backItem;
    private MenuItem changeLocationItem;
    private MenuItem confirmLocationItem;
    private MenuItem exitItem;
    private MenuItem item_expected;
    private MenuItem item_lastTrip;

    private final int MY_PERMISSIONS_LOCATION = 1000;
    private String lang="en";
    //private SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.ENGLISH);
    private SimpleDateFormat dateFormatter2=new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
    private SimpleDateFormat dateFormatter3=new SimpleDateFormat("HH:mm:ss",Locale.ENGLISH);
    private AlertDialog.Builder finishBuilder;
    private AlertDialog.Builder checkGpsBuilder;
    private AlertDialog.Builder checkLocationBuilder;
    private AlertDialog.Builder notActiveBuilder;
    private AlertDialog.Builder logoutBuilder;
    private AlertDialog.Builder completeInfoBuilder;
    private AlertDialog.Builder cancelBuilder;
    private AlertDialog.Builder callBuilder;
    private AlertDialog.Builder exitBuilder;


    private AlertDialog.Builder driverInfoBuilder ;
    private LayoutInflater inflater ;
    private View dialogView ;

    private int height = 96;
    private int width = 68;
    private Bitmap clientPointer;
    private Bitmap clientOrderPointer;

    private Bitmap pointer;
    private Bitmap orderPointer;
    private LinearLayout.LayoutParams p1;
    private LinearLayout.LayoutParams p2;
    private LinearLayout.LayoutParams p3;


    private FusedLocationProviderClient mFusedLocationClient;
    private Boolean loginDone=false;
    private AdRequest adRequest;
    Menu nav_Menu;
    private RequestQueue queue;
    int density ;
    private Switch menuSwitch;
    private EditTextLayout expectedKMET;
    private EditTextLayout expectedMinutesET;
    private ArrayList<GsonResponse.Vehicle> vehiclesImages=new ArrayList<>();

    private ArrayMap<String,Bitmap> vehiclesCachedImages=new ArrayMap<>();
    private boolean showExpectedTrip=true;
    private TimeZone timeZone=TimeZone.getDefault();
    View root;

    @Override
    public void onAttach(@NonNull Context context) {
        tag = "MainActivity";
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(tag,"onCreate");
        super.onCreate(savedInstanceState);

    }



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.i(tag,"onCreateView");
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
//                Log.i(tag, "uncaughtException");
//                try {
//                    JSONObject map = new JSONObject();
//                    map.put("event", "ActivityDestroy");
//                    sendToService(map);
//                } catch (JSONException e1) {
//                    e1.printStackTrace();
//                }
//                stopTCPService();
//
//            }
//        });
        sharedPref = getActivity().getSharedPreferences(Constants.shared, Context.MODE_PRIVATE);
        dateFormatter2.setTimeZone(timeZone);
        dateFormatter3.setTimeZone(timeZone);
        Log.i("TimeZone", TimeZone.getDefault().getDisplayName());

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        lang = Locale.getDefault().getLanguage();
        tag = tag + "_" + User.getInstance().type;
        Log.i(tag, "onCreate");
        density = (int) getResources().getDisplayMetrics().density;
        if (User.getInstance().isDriver())
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        root = inflater.inflate(R.layout.content_main, container, false);
        this.inflater = inflater;

        width = (int) getResources().getDimension(R.dimen.pointer_width);
        height = (int) getResources().getDimension(R.dimen.pointer_height);

        BitmapDrawable bitmapdrawPointer = (BitmapDrawable) getResources().getDrawable(R.drawable.client_pointer);
        Bitmap bPointer = bitmapdrawPointer.getBitmap();
        clientPointer = Bitmap.createScaledBitmap(bPointer, width, height, false);

        bitmapdrawPointer = (BitmapDrawable) getResources().getDrawable(R.drawable.client_order_pointer);
        Bitmap bClientOrderPointer = bitmapdrawPointer.getBitmap();
        clientOrderPointer = Bitmap.createScaledBitmap(bClientOrderPointer, width, height, false);


        bitmapdrawPointer = (BitmapDrawable) getResources().getDrawable(R.drawable.marker);
        bPointer = bitmapdrawPointer.getBitmap();
        pointer = Bitmap.createScaledBitmap(bPointer, width, height, false);

        bitmapdrawPointer = (BitmapDrawable) getResources().getDrawable(R.drawable.marker_selected);
        bPointer = bitmapdrawPointer.getBitmap();
        orderPointer = Bitmap.createScaledBitmap(bPointer, width, height, false);

        p1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p1.setMargins(5, 5, 5, 5);

        p2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f);
        p2.setMargins(5, 0, 0, 5);
        //p2.setMarginStart(5);
        //p2.setMarginEnd(5);

        p3 = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.profile_width),
                (int) getResources().getDimension(R.dimen.profile_height));

        summaryTV = (TextView) root.findViewById(R.id.tv_summary);

        cancelBtn = root.findViewById(R.id.btn_cancel);
        arrivedBtn = root.findViewById(R.id.btn_arrived);
        finishBtn = root.findViewById(R.id.btn_finish);

        Button expectedOkBtn = root.findViewById(R.id.expectedOkBtn);
        Button expectedCancelBtn = root.findViewById(R.id.expectedCancelBtn);

        okBtn = root.findViewById(R.id.btn_ok);
        chatBtn = root.findViewById(R.id.btn_chat);
        mapBtn = root.findViewById(R.id.btn_map);
        callBtn = root.findViewById(R.id.call);
        sendMessageBtn = root.findViewById(R.id.btn_send_msg);
        chatLayout = root.findViewById(R.id.chat_layout);
        chatMsgLayout = root.findViewById(R.id.chat_msg_layout);
        expectedLayout = root.findViewById(R.id.expectedLayout);
        chatMsgEditText = root.findViewById(R.id.chat_msg_edit_text);
        chatMsgScroll = root.findViewById(R.id.chat_msg_scroll);
        loading = root.findViewById(R.id.progressBarLoading);
        loadingTextView = root.findViewById(R.id.progressBarText);
        progressBarLayout = root.findViewById(R.id.progressBarLayout);
        tripLayout = root.findViewById(R.id.tripLayout);

        tripDurationTextView = root.findViewById(R.id.tripDurationTextView);
        tripDistanceTextView = root.findViewById(R.id.tripDistanceTextView);
        tripCostTextView = root.findViewById(R.id.tripCostTextView);
        expectedKMET = root.findViewById(R.id.expectedKMET);
        expectedMinutesET = root.findViewById(R.id.expectedMinutesET);

        cancelBtn.setOnClickListener(this);
        arrivedBtn.setOnClickListener(this);
        finishBtn.setOnClickListener(this);

        okBtn.setOnClickListener(this);
        chatBtn.setOnClickListener(this);
        mapBtn.setOnClickListener(this);
        sendMessageBtn.setOnClickListener(this);
        callBtn.setOnClickListener(this);
        expectedOkBtn.setOnClickListener(this);
        expectedCancelBtn.setOnClickListener(this);


        chatMsgEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 1 && s.charAt(s.length() - 1) == '\n') {
                    sendMsg();
                    chatMsgEditText.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*pendingCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
        /*fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tripStarted){
                    stopServiceLocation();
                }else{
                    mMap.clear();
                    startServiceLocation();
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //       .setAction("Action", null).show();
            }
        });*/


        // prepare map
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // prepare last location


        sharedPref = getActivity().getSharedPreferences(Constants.shared, Context.MODE_PRIVATE);

        database = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "caoutch").allowMainThreadQueries().fallbackToDestructiveMigration().build();


        if (driversInfo == null)
            driversInfo = new ArrayMap();

        if (drivers == null)
            drivers = new ArrayMap();

        if (clients == null)
            clients = new ArrayMap();

        finishBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        finishBuilder.setMessage(R.string.sure_finish);
        finishBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishTrip();
            }
        });
        finishBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog

            }
        });


        checkGpsBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        checkGpsBuilder.setMessage(R.string.enable_gps);

        /*View view = inflater.inflate(R.layout.enable_gps, null);
        checkGpsBuilder.setView(view);

        if(lang.contentEquals("ar")){
            ImageView i = view.findViewById(R.id.gps_image);
            i.setImageDrawable(getResources().getDrawable(R.drawable.enable_gps_ar));
        }*/
        checkGpsBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);

                stopTCPService();

            }
        });
        checkGpsBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                stopTCPService();

            }
        });

        checkLocationBuilder = new AlertDialog.Builder(getActivity());
        checkLocationBuilder.setMessage(R.string.enable_location);
        checkLocationBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (User.getInstance().isClient() || User.getInstance().isAdmin()) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_LOCATION);
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            MY_PERMISSIONS_LOCATION);
                }
            }
        });
        checkLocationBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog

            }
        });

        notActiveBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        notActiveBuilder.setMessage(R.string.not_active);
        notActiveBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                support();
            }
        });
        notActiveBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog

            }
        });

        logoutBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        logoutBuilder.setMessage(R.string.sure_logout);
        logoutBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                User.getInstance().reset();
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);

                stopTCPService();

            }
        });
        logoutBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog

            }
        });

        completeInfoBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        completeInfoBuilder.setMessage(R.string.user_blocked);
        completeInfoBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                support();
            }
        });
        completeInfoBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog

            }
        });

        cancelBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        cancelBuilder.setMessage(R.string.sure_cancel);
        cancelBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                cancelTrip();
            }
        });
        cancelBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog

            }
        });

        exitBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        exitBuilder.setMessage(R.string.sure_exit);
        exitBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                close();
            }
        });
        exitBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog

            }
        });


        callBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        /*callBuilder.setMessage(getString(R.string.call_to) + " " + finalTel);
        callBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + finalTel));
                startActivity(intent);
            }
        });*/
        callBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        driverInfoBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        loadingRelativeLayout = root.findViewById(R.id.main_activity);
        loadingRelativeLayout.retryBtn.setOnClickListener(this);
        loadingRelativeLayout.loading();
        loadingRelativeLayout.setPadding(0,0,0,0);
        //GoogleApiAvailability.makeGooglePlayServicesAvailable();

        setHasOptionsMenu(true);
        return root;

    }



    private Response.Listener<GsonResponse> createGetUserSuccessListener() {
        Log.i(tag, "createGetSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                if (response.code.contentEquals("200")) {
                    if(User.getInstance().zone!=null&&response.user.zone!=null&&!User.getInstance().zone.isEmpty()&&User.getInstance().zone.compareTo(response.user.zone)!=0)
                         {
                        Toast toast = Toast.makeText(getActivity(), getString(R.string.removed_from_zone)+" "+User.getInstance().zone, Toast.LENGTH_LONG);
                        toast.show();
                    }
                    User.setInstance(response.user);
                    if (response.config!=null&&response.config.minAndroidVersion!=0&& BuildConfig.VERSION_CODE < response.config.minAndroidVersion) {

                        final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));

                        } catch (ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));

                        }
                        stopTCPService();

                        return;

                    }
                    if(response.user.isDriver()&&response.user.driverStatus.contentEquals(Constants.blocked)){
                        User.getInstance().reset();
                        Intent i = new Intent(getActivity(), LoginActivity.class);
                        startActivity(i);

                        stopTCPService();

                        return;
                    }
                    else if(response.user.isClient()&&response.user.clientStatus.contentEquals(Constants.blocked)){
                        User.getInstance().reset();
                        Intent i = new Intent(getActivity(), LoginActivity.class);
                        startActivity(i);

                        stopTCPService();

                        return;
                    }
                    if (response.config!=null) {
                        User.getInstance().config = response.config;
                    }
                    if (response.vehicles!=null) {
                        vehiclesImages = response.vehicles;
                        for (final GsonResponse.Vehicle s: response.vehicles) {
                            GetFile getFile = new GetFile(database, new GetFile.Callback() {
                                @Override
                                public void callback(Bitmap bitmap) {
                                    if(bitmap!=null) {
                                        bitmap=Bitmap.createScaledBitmap(bitmap, width, height, false);
                                        vehiclesCachedImages.put(s.type, bitmap);
                                    }
                                }
                            });
                            getFile.execute(s.pointer);

                            GetFile getFile2 = new GetFile(database, new GetFile.Callback() {
                                @Override
                                public void callback(Bitmap bitmap) {
                                    if(bitmap!=null) {
                                        bitmap=Bitmap.createScaledBitmap(bitmap, width, height, false);
                                        vehiclesCachedImages.put("selected_" + s.type, bitmap);
                                    }
                                }
                            });
                            getFile2.execute(s.selectedPointer);
                        }
                    }
                    loadingRelativeLayout.loaded();

                    loading.setVisibility(View.VISIBLE);
                    progressBarLayout.setVisibility(View.VISIBLE);
                    loadingTextView.setText(R.string.connecting);

                    //FirebaseApp.initializeApp(getContext());
                    FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("FCMService", "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            if(token!=null&&(User.getInstance().fcmToken==null||!token.contentEquals(User.getInstance().fcmToken))){
                                Log.d("FCMService", token);
                                User.getInstance().fcmToken=token;
                                User.setInstance(User.getInstance());
                                Map<String, String> params = new HashMap<>();
                                params.put("_id", User.getInstance()._id);
                                params.put("hashedKey", User.getInstance().hashedKey);
                                params.put("fcmToken", User.getInstance().fcmToken);
                                params.put("iosToken", "");
                                GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                                        Constants.nodejs_index_url + "/register2/",
                                        GsonResponse.class,
                                        params,
                                        createGetSuccessListener2(),
                                        createGetErrorListener2());
                                queue.add(myReq);
                            }

                            }
                        });
                    checkLocation();

                } else if (response.code.contentEquals("201")) {
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

    private Response.Listener<GsonResponse> createGetSuccessListener2() {
        Log.i(tag, "createGetUserSuccessListener2");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {

            }
        };
    }

    private Response.ErrorListener createGetErrorListener2() {
        Log.i(tag,"createGetUserErrorListener2");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: "+ error.getMessage());
                User user = User.getInstance();
                user.fcmToken=null;
                User.setInstance(user);

            }
        };
    }


    void checkGPS() {
        Log.i(tag,"checkGPS");
        try {
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network_enabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(gps_enabled||(network_enabled&&(User.getInstance().isClient()||User.getInstance().isAdmin()))) {
                //if(!isResumed&&mapIsReady&&menuIsReady)
                //    getUser();
                //if(!isResumed/*&&mapIsReady&&menuIsReady*/)
                    continueResume();
            } else {

                AlertDialog dialog = checkGpsBuilder.create();
                dialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    nbutton.setTextColor(getContext().getColor(R.color.BUTTON_NEGATIVE));
                    Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbutton.setTextColor(getContext().getColor(R.color.BUTTON_POSITIVE));
                }

            }
        } catch (Exception ex) {
            //do nothing...
            Log.i(tag, ex.getMessage());
        }
    }

    void checkLocation() {
        Log.i(tag,"checkLocation");

        if (User.getInstance().isDriver()&&ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            checkGPS();

        }
        else if ((User.getInstance().isClient()||User.getInstance().isAdmin())&&
                (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED||
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED      ) ) {
            checkGPS();

        }
        else {

            AlertDialog dialog = checkLocationBuilder.create();
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getContext().getColor(R.color.BUTTON_NEGATIVE));
                Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(getContext().getColor(R.color.BUTTON_POSITIVE));
            }
        }
        /*LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

// ...

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(),
                                MY_PERMISSIONS_LOCATION);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });*/
    }

    @Override
    public void onResume() {
        Log.i(tag, "onResume");
        super.onResume();
        //isResumed=true;
        /*JSONObject map = new JSONObject();
        try {

            map.put("event", "ActivityResumed");
            sendToService(map);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        getUser();

    }

    void continueResume() {
        Log.i(tag, "continueResume");
        //isResumed=true;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

        }


        if (tripSingleton._id == null) {
            if (clientLocationMarker != null)
                clientLocationMarker.remove();
            clientLocationMarker = null;
            resetVisibility(true);

        }
        if (driversInfo == null)
            driversInfo = new ArrayMap();

        if (drivers == null)
            drivers = new ArrayMap();

        if (clients == null)
            clients = new ArrayMap();
        //if(intentService==null) {
            //intentService = new Intent(this, TCPService.class);

            if(menuSwitch!=null){
                Boolean online = sharedPref.getBoolean("online", true);
                menuSwitch.setChecked(online);
                if(online){
                    //intentService = new Intent(this, TCPService.class);
                    startTCPService();
                    menuSwitch.setText(R.string.online);
                }
                else {

                    //unbindService(mConnection);
                    stopTCPService();
                    menuSwitch.setText(R.string.offline);
                    loadingTextView.setText(R.string.offline_now);
                    loading.setVisibility(View.GONE);
                }
            }
            else
                startTCPService();
        //}
        //bindService(intentService, mConnection, BIND_AUTO_CREATE);
        JSONObject map = new JSONObject();
        try {

            map.put("event", "ActivityResumed");
            sendToService(map);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onPause() {
        Log.i(tag, "onPause loginDone:"+loginDone);
        //isResumed=false;
        /*if (intent != null) {
            //getApplicationContext().unbindService(mConnection);
            if(tripSingleton.state==null) {
                stopService(intent);
                //
            }
        }*/

        //mMap.clear();
        try {
            JSONObject map = new JSONObject();
            map.put("event", "ActivityPaused");
            sendToService(map);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*if(tripSingleton._id==null) {

        }*/

        if ((User.getInstance().isDriver()&&!menuSwitch.isChecked())
                || !loginDone
            ){

                //unbindService(mConnection);
                stopTCPService();

        }
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(tag, "onStop");
        super.onStop();
        //menuIsReady=false;
        //mapIsReady=false;
        try {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
            Log.i(tag,"onDestroy"+e.getMessage());
        }
    }


    @Override
    public void onDestroy() {
        Log.i(tag, "onDestroy");

        //stopTCPService();
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
            Log.i(tag,"onDestroy"+e.getMessage());
        }
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(tag, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.

        menu.clear();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu,inflater);
        cancelItem = menu.findItem(R.id.item_cancal);
        arrivedItem = menu.findItem(R.id.item_arrived);
        finishItem = menu.findItem(R.id.item_finish);
        okItem = menu.findItem(R.id.item_accept);
        chatItem = menu.findItem(R.id.item_chat);
        mapItem = menu.findItem(R.id.item_map);
        callItem = menu.findItem(R.id.item_call);
        backItem = menu.findItem(R.id.item_back);
        exitItem = menu.findItem(R.id.item_exit);
        changeLocationItem = menu.findItem(R.id.item_change_start_point);
        confirmLocationItem = menu.findItem(R.id.item_confirm_start_point);
        item_expected=menu.findItem(R.id.item_expected);
        //promoItem = menu.findItem(R.id.item_promo);
        if (User.getInstance().isClient()) {
            chatItem.setTitle(R.string.chat_with_driver);
            callItem.setTitle(R.string.call_the_driver);
            exitItem.setVisible(false);
        }
        else if (User.getInstance().isDriver()){
            chatItem.setTitle(R.string.chat_with_client);
            callItem.setTitle(R.string.call_the_client);
            exitItem.setVisible(true);
        }
        else if (User.getInstance().isAdmin()){
            exitItem.setVisible(true);
        }
        else{
            exitItem.setVisible(false);
        }
        item_lastTrip=menu.findItem(R.id.action_lastTrip);
        if (User.getInstance().isAdmin())
            item_lastTrip.setVisible(false);

        exitItem.setActionView(R.layout.switch_item);

        menuSwitch = exitItem.getActionView().findViewById(R.id.menu_switch);
        menuSwitch.setOnCheckedChangeListener(this);
        menuSwitch.setChecked(true);
        menuSwitch.setText(R.string.online);
        //if(!isResumed)
        //    checkLocation();

        //if(!isResumed&&mapIsReady&&menuIsReady)
        //    getUser();
        //menuIsReady=true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_lastTrip) {
            Navigation.findNavController(root).navigate(R.id.nav_last_trip);
            stopTCPService();
        }
        else if (id == R.id.action_support) {
            support();
        }
        else if (id == R.id.item_change_start_point) {
            setOnCameraMove();
        }
        else if (id == R.id.item_confirm_start_point) {
            unsetOnCameraMove();
        }
        else if (id == R.id.item_accept) {
            okTrip();
        }
        else if (id == R.id.item_expected) {
            expectedLayout.setVisibility(View.VISIBLE);
        }



        else if (id == R.id.item_cancal)
            cancelTrip();

        else if (id == R.id.item_arrived)
            arrivedTrip();

        else if (id == R.id.item_finish)
            finishTrip();

        else if (id == R.id.item_chat)
            showChat();

        else if (id == R.id.item_map)
            hideChat();

        else if (id == R.id.item_back)
            hideChat();

        else if (id == R.id.item_call)
            call();

        else if (id == R.id.item_exit)
            close();

        /*else if (id == R.id.item_promo) {
            promoLayout.setVisibility(View.VISIBLE);
        }*/


        return super.onOptionsItemSelected(item);
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.i(tag, "onMapReady");
        mMap = googleMap;
        if (User.getInstance().isClient()||User.getInstance().isAdmin()) {
            mMap.setOnMarkerClickListener(this);
        }
        else {
            mMap.setInfoWindowAdapter(new ClientInfoWindowAdapter());
        }
        TypedValue tv = new TypedValue();

        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
        {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            mMap.setPadding(0,actionBarHeight+(50*density),0,100*density);
            //chatLayout.setPadding(10,actionBarHeight+120,0,210);

        }

        //mapIsReady = true;
        //if(!isResumed)
        //    checkLocation();
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(29.4080607, 31.2439862), 7));
        //startServiceLocation();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }







    }

   @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i(tag, "onRequestPermissionsResult");
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(tag, "onRequestPermissionsResult granted");
                    if(tripSingleton._id!=null)
                        checkGPS();
                } else {
                    Log.i(tag, "onRequestPermissionsResult denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    //move map to last location


    /*private void setLines(LatLng newLatLng) {
        if (lastLatLng != null) {

            Polyline polylineStarted=this.polylineStarted;
            Polyline polylinePending=this.polylinePending;
            Polyline polylineReserved=this.polylineReserved;
            if (tripSingleton != null && tripSingleton.state != null && tripSingleton.state.contentEquals(Constants.PENDING)) {
                if (polylineOptionsPending == null)
                    polylineOptionsPending = new PolylineOptions();
                polylineOptionsPending.add(lastLatLng, newLatLng);
                this.polylinePending = mMap.addPolyline(polylineOptionsPending);
                this.polylinePending.setWidth(5);
                this.polylinePending.setColor(Color.LTGRAY);
                if (polylinePending != null)
                    polylinePending.remove();
            } else if (tripSingleton != null && tripSingleton.state != null && tripSingleton.state.contentEquals(Constants.RESERVED)) {
                if (polylineOptionsReserved == null)
                    polylineOptionsReserved = new PolylineOptions();
                polylineOptionsReserved.add(lastLatLng, newLatLng);
                this.polylineReserved = mMap.addPolyline(polylineOptionsReserved);
                this.polylineReserved.setWidth(5);
                this.polylineReserved.setColor(Color.GRAY);
                if (polylineReserved != null)
                    polylineReserved.remove();
            } else if (tripSingleton != null && tripSingleton.state != null && tripSingleton.state.contentEquals(Constants.STARTED)) {
                if (polylineOptionsStarted == null)
                    polylineOptionsStarted = new PolylineOptions();
                polylineOptionsStarted.add(lastLatLng, newLatLng);
                this.polylineStarted = mMap.addPolyline(polylineOptionsStarted);
                this.polylineStarted.setWidth(5);
                this.polylineStarted.setColor(getResources().getContext().getColor(R.color.colorPrimary));
                if (polylineStarted != null)
                    polylineStarted.remove();
            }
        }
        lastLatLng = newLatLng;
    }*/


    private void setPoint(LatLng newLatLng) {
        try{
            if (firstLocation) {
                firstLocation = false;
                if(User.getInstance().isSuperAdmin())
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 1));
                else if(User.getInstance().isAdmin())
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 10));
                else
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, DEFAULT_ZOOM));
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));
            }
        }
        catch (Exception e){
            Log.i(tag,e.getLocalizedMessage());
        }

    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent


            LatLng newLatLng;
            String driverId;
            String clientId;
            String data;
            TCPResponse response=(TCPResponse) intent.getSerializableExtra("response");
            Log.i(tag, "BroadcastReceiver Got message: " + response.event);
            String messageType=response.event;
            if (messageType.contentEquals("loginDone")) {
                loginDone=true;

                if(tripSingleton!=null && tripSingleton._id==null) {

                    progressBarLayout.setVisibility(View.VISIBLE);
                    if (User.getInstance().isClient()) {
                        loading.setVisibility(View.VISIBLE);
                        loadingTextView.setText(R.string.search_driver);
                    }
                    else if (User.getInstance().isDriver()) {
                        loading.setVisibility(View.VISIBLE);
                        loadingTextView.setText(R.string.wait_requests);
                    }
                    else if (User.getInstance().isAdmin()) {
                        loading.setVisibility(View.GONE);
                        loadingTextView.setText(R.string.monitor_zone);
                    }
                }


//                if(User.getInstance().ads==1&&adRequest==null){
//                    MobileAds.initialize(getActivity(), "ca-app-pub-6615275988084929~4953378785");
//                    mAdView = root.findViewById(R.id.adView);
//                    adRequest = new AdRequest.Builder().build();
//                    mAdView.loadAd(adRequest);
//                }
                setMarkers();
            }
            if (messageType.contentEquals("driverLocation") && User.getInstance().isDriver()) {
                if (response.latitude!=null&&response.longitude!=null) {
                    Double latitude = response.latitude;
                    Double longitude = response.longitude;
                    latLng = new LatLng(latitude, longitude);
                    setPoint(latLng);
                    /*if (tripSingleton != null && tripSingleton.state != null)
                        setLines(latLng);*/

                    if (response.duration!=null)
                        tripDurationTextView.setText(String.format(Locale.ENGLISH,"%.2f", response.duration / 60000) + " " + getString(R.string.minutes));
                    if (response.distance!=null)
                        tripDistanceTextView.setText(String.format(Locale.ENGLISH,"%.2f", response.distance / 1000) + " " + getString(R.string.km));
                    if (response.cost!=null)
                        tripCostTextView.setText(String.format(Locale.ENGLISH,"%.2f", response.cost) + " " + tripSingleton.cur);
                }
            }
            else if (messageType.contentEquals("clientLocation") && User.getInstance().isClient()) {

                if (response.latitude!=null&&response.longitude!=null) {
                    Double latitude = response.latitude;
                    Double longitude = response.longitude;

                    LatLng clientLatLng = new LatLng(latitude, longitude);
                    setPoint(clientLatLng);
                    try{
                        if (clientLocationMarker == null) {
                            clientLocationMarker = mMap.addMarker(new MarkerOptions()
                                    .position(clientLatLng));
                            //clientLocationMarker.setAlpha(0.8f);
                            clientLocationMarker.setIcon(BitmapDescriptorFactory.fromBitmap(clientPointer));
                            clientLocationMarker.setTag(User.getInstance()._id);


                        } else {
                            clientLocationMarker.setPosition(clientLatLng);
                        }
                        /*if (tripSingleton._id == null) {
                            setOnCameraMove();

                        }*/
                    }
                    catch (Exception e){
                        Log.i(tag,e.getLocalizedMessage());
                    }
                }
            }
            else if (messageType.contentEquals("myLocation")) {
                if (response.latitude!=null&&response.longitude!=null) {
                    Double latitude = response.latitude;
                    Double longitude = response.longitude;

                    LatLng clientLatLng = new LatLng(latitude, longitude);
                    setPoint(clientLatLng);
                }
            }
            else if (messageType.contentEquals("newDriverLocation")&&(User.getInstance().isClient()||User.getInstance().isAdmin())) {
                if(User.getInstance().isClient())
                    loadingTextView.setText(R.string.select_driver);
                loading.setVisibility(View.GONE);
                //if (mapIsReady) {
                    Double latitude = response.latitude;
                    Double longitude = response.longitude;
                    driverId = response._id;
                    newLatLng = new LatLng(latitude, longitude);
                    if (!driversInfo.containsKey(driverId)) {
                        try {
                            JSONObject map = new JSONObject();
                            map.put("event", "userInfo");
                            map.put("user_id", driverId);
                            map.put("_id", User.getInstance()._id);
                            sendToService(map);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        if (drivers.containsKey(driverId)) {
                            drivers.get(driverId).setPosition(newLatLng);
                            if(User.getInstance().isAdmin()&&response.tripId!=null){
                                if(vehiclesCachedImages.containsKey("selected_"+response.carType)) {
                                    drivers.get(driverId).setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get("selected_" + response.carType)));
                                }
                            }
                            else if(User.getInstance().isAdmin()){
                                if(vehiclesCachedImages.containsKey(response.carType)) {
                                    drivers.get(driverId).setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get( response.carType)));
                                }
                            }
                        }

                        else {
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,DEFAULT_ZOOM));

                            //m.setAlpha(0.8f);
                            //User driverData = driversInfo.get(driverId);
                            //if (driverData != null) {

                                if (response.carType!=null) {
                                    String carType=response.carType;
                                    try {
                                        if (tripSingleton.driverId != null && driverId.contentEquals(tripSingleton.driverId)) {
                                            Marker m = mMap.addMarker(new MarkerOptions()
                                                    .position(newLatLng));
                                            if (vehiclesCachedImages.containsKey("selected_" + carType) && User.getInstance().isClient()) {
                                                m.setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get("selected_" + carType)));
                                            } else {
                                                m.setIcon(BitmapDescriptorFactory.fromBitmap(orderPointer));
                                            }
                                            m.setTag(driverId);
                                            drivers.put(driverId, m);
                                        } else if (User.getInstance().isAdmin() && response.clientId != null) {
                                            Marker m = mMap.addMarker(new MarkerOptions()
                                                    .position(newLatLng));
                                            if (vehiclesCachedImages.containsKey("selected_" + carType) && User.getInstance().isClient()) {
                                                m.setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get("selected_" + carType)));
                                            } else {
                                                m.setIcon(BitmapDescriptorFactory.fromBitmap(orderPointer));
                                            }
                                            m.setTag(driverId);
                                            drivers.put(driverId, m);
                                        } else if (vehiclesCachedImages.containsKey(carType)) {
                                            Marker m = mMap.addMarker(new MarkerOptions()
                                                    .position(newLatLng));
                                            m.setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get(carType)));
                                            m.setTag(driverId);
                                            drivers.put(driverId, m);
                                        }
                                    }
                                    catch (Exception e){
                                        Log.i(tag,e.getLocalizedMessage());
                                    }

                                }

                            //}



                            //mService.sendGetDriverInfoToServer(driverId);


                        //}
                    }
                    if (tripSingleton.driverId != null && driverId.contentEquals(tripSingleton.driverId)) {
                        //setLines(newLatLng);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));
                    }
                    try {
                        if (response.duration != null)
                            tripDurationTextView.setText(String.format(Locale.ENGLISH, "%.2f", response.duration / 60000) + " " + getString(R.string.minutes));
                        if (response.distance != null)
                            tripDistanceTextView.setText(String.format(Locale.ENGLISH, "%.2f", response.distance / 1000) + " " + getString(R.string.km));
                        if (response.cost != null)
                            tripCostTextView.setText(String.format(Locale.ENGLISH, "%.2f", response.cost) + " " + tripSingleton.cur);
                    }
                    catch (Exception e){
                        Log.i(tag,e.getLocalizedMessage());
                    }
                }
            }
            else if (messageType.contentEquals("userInfo")&&response.user != null) {
                if ( User.getInstance().isClient()) {
                    driverId = response.user._id;
                    driversInfo.put(driverId, response.user);

                    if ( drivers.containsKey(driverId)) {

                        if (response.user.carType!=null) {
                            String carType=response.user.carType;
                            Marker m = drivers.get(driverId);
                            if (tripSingleton!=null&&tripSingleton.driverId != null && driverId.contentEquals(tripSingleton.driverId)) {
                                if(vehiclesCachedImages.containsKey("selected_"+carType))
                                    m.setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get("selected_"+carType)));
                                else
                                    m.setIcon(BitmapDescriptorFactory.fromBitmap(orderPointer));

                            }
                            else {
                                if(vehiclesCachedImages.containsKey(carType))
                                    m.setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get(carType)));
                                else
                                    m.setIcon(BitmapDescriptorFactory.fromBitmap(pointer));
                            }

                        }

                    }


                }
                else if ( User.getInstance().isAdmin()) {
                    driversInfo.put(response.user._id, response.user);

                    if ( drivers.containsKey(response.user._id)) {

                        if (response.user.carType!=null) {
                            String carType=response.user.carType;
                            Marker m = drivers.get(response.user._id);

                            if(vehiclesCachedImages.containsKey(carType))
                                m.setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get(carType)));
                            else
                                m.setIcon(BitmapDescriptorFactory.fromBitmap(pointer));


                        }

                    }
                }
                else if(tripSingleton.clientId.contentEquals(response.user._id)){

                    clientJson=response.user;
                }
            } else if (messageType.contentEquals("offDriverLocation")&&(User.getInstance().isClient()||User.getInstance().isAdmin())) {
                driverId = response._id;
                if (drivers.containsKey(driverId) && tripSingleton._id == null) {

                    drivers.get(driverId).remove();
                    drivers.remove(driverId);
                    driversInfo.remove(driverId);
                    if(drivers.size()==0&&User.getInstance().isClient()) {
                        loadingTextView.setText(R.string.search_driver);
                        loading.setVisibility(View.VISIBLE);
                    }
                }
            } else if (messageType.contentEquals("newClientLocation")) {
                try {
                    Double latitude = response.latitude;
                    Double longitude = response.longitude;
                    clientId = response._id;
                    newLatLng = new LatLng(latitude, longitude);
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,DEFAULT_ZOOM));
                    if (!driversInfo.containsKey(clientId)&&User.getInstance().isAdmin()) {
                        try {
                            JSONObject map = new JSONObject();
                            map.put("event", "userInfo");
                            map.put("user_id", clientId);
                            map.put("_id", User.getInstance()._id);
                            sendToService(map);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (clients.containsKey(clientId)) {
                        clients.get(clientId).setPosition(newLatLng);
                    } else {
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,DEFAULT_ZOOM));
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(newLatLng));
                        //m.setAlpha(0.8f);
                        m.setIcon(BitmapDescriptorFactory.fromBitmap(clientPointer));
                        m.setTag(clientId);
                        clients.put(clientId, m);
                    }
                }
                catch (Exception e){
                    Log.i(tag,e.getLocalizedMessage());
                }
            }
            else if (messageType.contentEquals("offClientLocation") && User.getInstance().isAdmin()) {
                clientId = response._id;
                if (clients.containsKey(clientId)) {
                    clients.get(clientId).remove();
                    clients.remove(clientId);
                }
            }
            else if (messageType.contentEquals("onDriverNewTrip")&&User.getInstance().isDriver()) {
                try{
                        clientJson = response.trip.client;
                        Marker marker;
                        if (clients.containsKey(tripSingleton.clientId)) {
                            marker = clients.get(tripSingleton.clientId);
                        } else {
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(tripSingleton.clientLat, tripSingleton.clientLng)));
                            //marker.setAlpha(0.8f);

                        }
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(clientOrderPointer));
                        marker.setTag(tripSingleton.clientId);
                        clients.put(tripSingleton.clientId, marker);
                        //mMap.setInfoWindowAdapter(new ClientInfoWindowAdapter());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                        marker.showInfoWindow();
                        setPending();
                }
                catch (Exception e){
                    Log.i(tag,e.getLocalizedMessage());
                }
            } else if (messageType.contentEquals("onDriverConfirmTrip")&&User.getInstance().isClient()) {
                /*if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(context, getString(R.string.driver_confirm), Toast.LENGTH_LONG);
                toast.show();*/
                Marker d = drivers.get(tripSingleton.driverId);
                for (Map.Entry<String, Marker> m : drivers.entrySet()) {
                    if (!m.getKey().contentEquals(tripSingleton.driverId)) {
                        m.getValue().remove();
                    }
                }
                drivers.clear();
                drivers.put(tripSingleton.driverId, d);

            } else if (messageType.contentEquals("onDriverHasTrip")) {
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(context, getString(R.string.driver_has_trip), Toast.LENGTH_LONG);
                toast.show();
                Marker marker = drivers.get(tripSingleton.driverId);
                if (marker != null) {
                    marker.remove();
                    drivers.remove(response.trip.driver._id);
                }
                resetVisibility(false);

            } else if (messageType.contentEquals("onDriverCancelTrip")&&User.getInstance().isClient()) {
                try {
                    Marker marker = drivers.get(response.trip.driverId);
                    if (marker != null) {
                        marker.remove();
                        drivers.remove(response.trip.driverId);
                    }
                    resetVisibility(false);
                   /* if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(context, getString(R.string.driver_reject), Toast.LENGTH_LONG);
                    toast.show();*/
                    if (clientLocationMarker != null) {
                        JSONObject map = new JSONObject();
                        map.put("event", "location");
                        map.put("_id", User.getInstance()._id);
                        map.put("type", User.getInstance().type);
                        map.put("latitude", Double.toString(clientLocationMarker.getPosition().latitude));
                        map.put("longitude", Double.toString(clientLocationMarker.getPosition().longitude));
                        sendToService(map);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else if (messageType.contentEquals("onClientCancelTrip")&&User.getInstance().isDriver()) {
                try{
                    Marker marker = clients.get(response.trip.clientId);
                    if (marker != null) {
                        marker.remove();
                        clients.remove(response.trip.clientId);
                    }
                    resetVisibility(false);
                    /*if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(context, getString(R.string.client_cancel), Toast.LENGTH_LONG);
                    toast.show();*/
                    //mMap.setInfoWindowAdapter(null);


                }
                catch (Exception e){
                    Log.i(tag,e.getLocalizedMessage());
                }
            } else if (messageType.contentEquals("onDisconnent")) {
                loading.setVisibility(View.VISIBLE);
                progressBarLayout.setVisibility(View.VISIBLE);
                loadingTextView.setText(R.string.connecting);
                for (Map.Entry<String, Marker> d : drivers.entrySet()) {
                    d.getValue().remove();
                }
                drivers.clear();
                for (Map.Entry<String, Marker> d : clients.entrySet()) {
                    d.getValue().remove();
                }
                clients.clear();
            } else if (messageType.contentEquals("onConnect")) {

            } else if (messageType.contentEquals("tripMessage")) {
                if (tripSingleton._id != null && tripSingleton._id.contentEquals(response.tripMessage.tripId)) {
                    addToChat(response.tripMessage.createTime != null ? dateFormatter3.format(response.tripMessage.createTime):"", response.tripMessage.message, response.tripMessage.senderId);
                    /*chatMsgScroll.post(new Runnable() {
                        @Override
                        public void run() {
                            chatMsgScroll.smoothScrollBy(0, chatMsgScroll.FOCUS_DOWN);
                        }
                    });*/
                    //todo:multi toast handel
                    if (chatLayout.getVisibility() == View.GONE) {
                        unreadedMessagesCount++;
                        if (toast != null)
                            toast.cancel();

                        toast = Toast.makeText(context, response.tripMessage.message, Toast.LENGTH_SHORT);
                        toast.show();

                        chatBtn.setImageResource(R.drawable.chat);
                        try {
                            if (User.getInstance().isClient())
                                chatItem.setTitle(getString(R.string.chat_with_driver) + "(" + unreadedMessagesCount + ")");
                            else
                                chatItem.setTitle(getString(R.string.chat_with_client) + "(" + unreadedMessagesCount + ")");
                        }
                        catch (Exception e){
                            Log.i(tag,e.getLocalizedMessage());
                        }
                    }
                }
            } else if (messageType.contentEquals("onDriverFinishTrip")&&User.getInstance().isClient()) {
                //resetVisibility(true);

                    //unbindService(mConnection);
                 //   stopTCPService();


                //data = intent.getStringExtra("_id");

                Bundle bundle = new Bundle();
                bundle.putString("_id",response.trip._id);
                Navigation.findNavController(root).navigate(R.id.nav_last_trip,bundle);
                stopTCPService();



            } else if (messageType.contentEquals("finishTrip")) {
                Bundle bundle = new Bundle();
                bundle.putString("_id",response.trip._id);
                Navigation.findNavController(root).navigate(R.id.nav_last_trip,bundle);
                stopTCPService();
            } else if (messageType.contentEquals("notActive")) {

                AlertDialog dialog = notActiveBuilder.create();
                dialog.show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    nbutton.setTextColor(getContext().getColor(R.color.BUTTON_NEGATIVE));
                    Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbutton.setTextColor(getContext().getColor(R.color.BUTTON_POSITIVE));
                }

            } else if (messageType.contentEquals("completeInfo")) {

                AlertDialog dialog = completeInfoBuilder.create();
                dialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    nbutton.setTextColor(getContext().getColor(R.color.BUTTON_NEGATIVE));
                    Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbutton.setTextColor(getContext().getColor(R.color.BUTTON_POSITIVE));
                }
            } else if (messageType.contentEquals("driverStartTrip")) {
                //tripLayout.setVisibility(View.VISIBLE);
                setStarted();

            }
            else if (messageType.contentEquals("disconnect")) {

                stopTCPService();
                //getActivity().finish();
            }
            else if (messageType.contentEquals("retry")) {

                stopTCPService();
                //getActivity().finish();
                //isResumed=false;
                loadingRelativeLayout.loadingFailed();
            }

        }
    };

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Log.i(tag,"online "+b);
        if(b){
            //intentService = new Intent(this, TCPService.class);
            startTCPService();
            menuSwitch.setText(R.string.online);
            toast = Toast.makeText(getActivity(), R.string.online_now, Toast.LENGTH_SHORT);
            toast.show();

        }
        else {

            //unbindService(mConnection);
            stopTCPService();
            menuSwitch.setText(R.string.offline);
            //toast = Toast.makeText(this, R.string.offline_now, Toast.LENGTH_SHORT);
            //toast.show();
            loadingTextView.setText(R.string.offline_now);
            loading.setVisibility(View.GONE);

            if (drivers != null) {
                for (Map.Entry<String, Marker> m : drivers.entrySet()) {
                    m.getValue().remove();
                }
                drivers.clear();
            }

            if (clients != null) {
                for (Map.Entry<String, Marker> m : clients.entrySet()) {
                    m.getValue().remove();
                }
                clients.clear();
            }
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("online",b);
        editor.commit();

    }

//    private void startServiceLocation(){
//        //todo:check duplicate message send and recieve
//        //tripStarted=true;
//        //SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
//        Log.i(tag, "startServiceLocation" );
//        if(isServiceRunning()) {
//
//            JSONObject map = new JSONObject();
//            try {
//
//                map.put("event","ActivityResumed");
//                sendToService(map);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        else{
//            Intent intent = new Intent(this, TCPService.class);
//            getApplicationContext().startService(intent);
//        }
//        //getApplicationContext().bindService(intent, mConnection, BIND_AUTO_CREATE);
//
//        /*SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean(TRIPSTARTED, tripStarted);
//        editor.commit();*/
//
//
//    }


//    private void stopServiceLocation(){
//        //if(tripStarted) {
//            //tripStarted = false;
//            Log.i(tag, "stopServiceLocation" );
//            /*SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putBoolean(TRIPSTARTED, tripStarted);
//            editor.commit();*/
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
//            if (intent != null) {
//                //getApplicationContext().unbindService(mConnection);
//                getApplicationContext().stopService(intent);
//            }
//        //}
//
//    }

    /*private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(tag, "onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TCPService.LocalBinder binder = (TCPService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            *//*Handler handler = new Handler();
            handler.post(new Runnable(){
                @Override'
                public void run() {
                    setMarkers();
                }
            });*//*

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(tag, "onServiceDisconnected");
            mBound = false;
        }

    };

    private class loadTripTask extends AsyncTask<Void, Void, List<TripLocation>> {
        @Override
        protected List<TripLocation> doInBackground(Void... params) {
            Trip trip = database.tripDao().findLastTrip(User.getInstance()._id);
            if (trip == null) {
                Log.i(tag, "AsyncTask new trip");
                return new ArrayList<TripLocation>();
            } else {
                Log.i(tag, "AsyncTask start trip id " + trip._id);
                List<TripLocation> locations = database.tripLocationDao().getTripLocations(trip._id);
                return locations;
            }

        }

        @Override
        protected void onPostExecute(List<TripLocation> locations) {
            for (TripLocation location : locations) {
                Log.i(tag, "AsyncTask DB location: " + location.latitude + " " + location.longitude);
                setPoint(new LatLng(location.latitude, location.longitude));
            }
            //startServiceLocation();

        }
    }*/

    void sendToService(JSONObject map) {
        //if(intentService!=null) {
            Log.i(tag, "sendToService : " + map.toString());
            Intent intent = new Intent(Constants.MainActivityTOService);
            // You can also include some extra data.
            intent.putExtra("json", map.toString());
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        //}
    }

    class ClientInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {


        ClientInfoWindowAdapter() {

        }

        @Override
        public View getInfoWindow(Marker marker) {
            Log.i(tag, "getInfoWindow " + marker.getTag());
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            Log.i(tag, "getInfoContents " + marker.getTag());
            if (clientJson != null) {

                View view = getLayoutInflater().inflate(R.layout.client_info, null);
                if (marker.getTag()!=null && clients.get(clientJson._id).getTag()!=null &&
                        marker.getTag().toString().contentEquals(clients.get(clientJson._id).getTag().toString())
                ) {
                    if(clientJson.clientRate!=null)
                        ((RatingBar) view.findViewById(R.id.ratingBar)).setRating((float) clientJson.clientRate);

                    if(clientJson.createDate!=null)
                        ((TextView) view.findViewById(R.id.client_create_date)).setText(dateFormatter2.format(clientJson.createDate));

                    if(clientJson.totalHours!=null&&clientJson.totalDistance!=null)
                        ((TextView) view.findViewById(R.id.client_total_trips)).setText(
                            clientJson.totalHours  +
                                    getString(R.string.hr) + "/" +
                                    clientJson.totalDistance +
                                    getString(R.string.km));
                    else
                         view.findViewById(R.id.client_total_trips_row).setVisibility(View.GONE);
                    if(clientJson.claimsCount!=null)
                        ((TextView) view.findViewById(R.id.client_claims)).setText( clientJson.claimsCount);
                    else
                        view.findViewById(R.id.client_claims_row).setVisibility(View.GONE);
                    if(clientJson.tripsCount!=null)
                        ((TextView) view.findViewById(R.id.client_trips_count)).setText(clientJson.tripsCount );
                    else
                        view.findViewById(R.id.client_trips_count_row).setVisibility(View.GONE);
                    //((TextView) view.findViewById(R.id.client_cancel_count)).setText(clientJson.has("cancelCount") ? clientJson.getString("cancelCount") : "0");
                    return view;
                }


            }
            return null;
        }

        private void render(Marker marker, View view) {


        }
    }

    private void selectDriver(String driverId) {
        User driverData = driversInfo.get(driverId);
        if (driverData != null) {
            try {
                if (driverData.carType!=null){
                    String carType = driverData.carType;
                    Marker m = drivers.get(driverId);

                    if(vehiclesCachedImages.containsKey("selected_"+carType))
                        m.setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get("selected_"+carType)));
                    else
                        m.setIcon(BitmapDescriptorFactory.fromBitmap(orderPointer));


                    tripSingleton.clientId = User.getInstance()._id;
                    tripSingleton.driverId = driverId;
                    tripSingleton.state = Constants.PENDING;
                    tripSingleton.clientLat = clientLocationMarker.getPosition().latitude;
                    tripSingleton.clientLng = clientLocationMarker.getPosition().longitude;
                    LatLng latLng = new LatLng(drivers.get(driverId).getPosition().latitude, drivers.get(driverId).getPosition().longitude);
                    tripSingleton.driverLat = latLng.latitude;
                    tripSingleton.driverLng = latLng.longitude;

                    JSONObject map = tripSingleton.getMap();
                    map.put("event", "selectedDriver");
                    map.put("carType", carType);
                    sendToService(map);

                    setPending();

                }
            } catch(JSONException e){
                Log.e(tag, e.getMessage());
            }
        }
    }

    private void okTrip() {
        if (User.getInstance().isDriver()) {
            tripSingleton.state = Constants.RESERVED;
            tripSingleton.driverLat = latLng.latitude;
            tripSingleton.driverLng = latLng.longitude;
            try {
                JSONObject map = tripSingleton.getMap();
                map.put("event", "driverConfirmed");
                sendToService(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Marker marker = clients.get(tripSingleton.clientId);
            marker.hideInfoWindow();

            setReserved();
            Marker m = clients.get(tripSingleton.clientId);
            for (Map.Entry<String, Marker> d : clients.entrySet()) {
                if (!d.getKey().contentEquals(tripSingleton.clientId))
                    d.getValue().remove();

            }
            clients.clear();
            clients.put(tripSingleton.clientId, m);

        }
    }

    private void cancelTrip() {

        tripSingleton.state = Constants.CANCELED;
        if (User.getInstance().isDriver()) {
            tripSingleton.cancelledBy = "driver";
            try {
                JSONObject map = tripSingleton.getMap();
                map.put("event", "driverCancel");
                sendToService(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Marker marker = clients.get(tripSingleton.clientId);
            if (marker != null) {
                marker.remove();
                clients.remove(tripSingleton.clientId);
            }
            //mMap.setInfoWindowAdapter(null);
            chatMsgLayout.removeAllViewsInLayout();
        } else {
            tripSingleton.cancelledBy = "client";
            try {
                JSONObject map = tripSingleton.getMap();
                map.put("event", "clientCancel");
                sendToService(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Marker marker = drivers.get(tripSingleton.driverId);
            if (marker != null) {
                marker.remove();
                drivers.remove(tripSingleton.driverId);
            }
            //send client location to join zone rooms
            if (clientLocationMarker != null) {
                try {
                    JSONObject map = new JSONObject();
                    map.put("event", "location");
                    map.put("_id", User.getInstance()._id);
                    map.put("type", User.getInstance().type);
                    map.put("latitude", Double.toString(clientLocationMarker.getPosition().latitude));
                    map.put("longitude", Double.toString(clientLocationMarker.getPosition().longitude));
                    sendToService(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        resetVisibility(false);

    }


    private void arrivedTrip() {
        if (User.getInstance().isDriver()) {
            tripSingleton.state = Constants.STARTED;
            tripSingleton.driverLat = latLng.latitude;
            tripSingleton.driverLng = latLng.longitude;
            try {
                JSONObject map = tripSingleton.getMap();
                map.put("event", "startTrip");
                sendToService(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Marker marker = clients.get(tripSingleton.clientId);
            marker.hideInfoWindow();
            setStarted();
            Marker m = clients.get(tripSingleton.clientId);
            for (Map.Entry<String, Marker> d : clients.entrySet()) {
                if (!d.getKey().contentEquals(tripSingleton.clientId))
                    d.getValue().remove();

            }
            clients.clear();
            clients.put(tripSingleton.clientId, m);

        }
    }

    private void finishTrip() {
        if (User.getInstance().isDriver()) {
            tripSingleton.state = Constants.FINISHED;
            tripSingleton.driverLat = latLng.latitude;
            tripSingleton.driverLng = latLng.longitude;
            try {
                JSONObject map = tripSingleton.getMap();
                map.put("event", "finishTrip");
                sendToService(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Marker marker = clients.get(tripSingleton.clientId);
            marker.hideInfoWindow();
            setFinish();

        }
    }




    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i(tag, "onMarkerClick");

        if (!((String) marker.getTag()).contentEquals(User.getInstance()._id)) {
            Log.i(tag, "onMarkerClick enabled");
            //int position = (int)(marker.getTag());
            //Using position get Value from arraylist
            unsetOnCameraMove();

            dialogView = inflater.inflate(R.layout.select_driver, null);
            User driverData = driversInfo.get(marker.getTag());
            //Log.i(tag, "onMarkerClick driver " + marker.getTag() + " " + data);
            if (driverData != null) {

                TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout);
                if (driverData.zone != null && !driverData.zone.isEmpty()  && User.getInstance().isSuperAdmin()){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.zone));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.zone);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }
                if (driverData.firstName != null && User.getInstance().isAdmin()){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.first_name));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.firstName);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.lastName != null && User.getInstance().isAdmin()){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.last_name));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.lastName);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if(driverData.isDriver()) {
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.driver_rate));
                    textView.setPadding(5*density,0,5*density,0);
                    LinearLayout l = new LinearLayout(getActivity());
                    RatingBar r = new RatingBar(getActivity(),null, android.R.attr.ratingBarStyleSmall);
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
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.car_rate));
                    textView.setPadding(5*density,0,5*density,0);
                    LinearLayout l = new LinearLayout(getActivity());
                    RatingBar r = new RatingBar(getActivity(),null, android.R.attr.ratingBarStyleSmall);
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
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.client_rate));
                    textView.setPadding(5*density,0,5*density,0);
                    LinearLayout l = new LinearLayout(getActivity());
                    RatingBar r = new RatingBar(getActivity(),null, android.R.attr.ratingBarStyleSmall);
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
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.mobile));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.mobile);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);

                    Linkify.addLinks(textView2, Patterns.PHONE,"tel:",Linkify.sPhoneNumberMatchFilter,Linkify.sPhoneNumberTransformFilter);
                    textView2.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (driverData.email!=null && User.getInstance().isAdmin()){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.email));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.email);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                    textView2.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);

                    textView2.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (driverData.cost.minimum>0){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.minimum));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(String.valueOf(driverData.cost.minimum));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.cost.base>0){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.baseCost));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(String.valueOf(driverData.cost.base));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.cost.km>0){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.kmCost));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(String.valueOf(driverData.cost.km));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.cost.minute>0){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.minuteCost));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(String.valueOf(driverData.cost.minute));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.cost.currency!=null&&!driverData.cost.currency.isEmpty()){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.currency));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(String.valueOf(driverData.cost.currency));
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }


                if (driverData.carNumber!=null&&tripSingleton._id!=null){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.carNumber));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.carNumber);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.createDate!=null){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.create_date));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.createDate.toString());
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }


                if (driverData.totalHours!=null && driverData.totalDistance!=null) {
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.total_trips));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.totalHours +
                            " " + getString(R.string.hr) + "/" + driverData.totalDistance + " " + getString(R.string.km));
                    tableLayout.addView(tableRow);
                }

                if (driverData.claimsCount!=null){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.claims));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.claimsCount);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.tripsCount!=null){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.trips_count));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.tripsCount);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.carManufacturer!=null){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.carManufacturer));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.carManufacturer);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.carModel!=null&&driverData.carModel.length()>0){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.carModel));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.carModel);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }

                if (driverData.carMadeYear!=null){
                    TableRow tableRow = new TableRow(getActivity());
                    TextView textView = new TextView(getActivity());
                    textView.setText(getResources().getString(R.string.carMadeYear));
                    textView.setPadding(5*density,0,5*density,0);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(driverData.carMadeYear);
                    tableRow.addView(textView);
                    tableRow.addView(textView2);
                    tableLayout.addView(tableRow);
                }




                if (driverData.carColor!=null&&driverData.carColor.length()>0) {
                    dialogView.findViewById(R.id.car_color_row).setVisibility(View.VISIBLE);
                    TextView b = dialogView.findViewById(R.id.car_color);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Drawable r = b.getBackground();
                        r.setTint(Color.parseColor(driverData.carColor));
                        b.setBackground(r);
                    }
                    else{
                        b.setBackgroundColor(Color.parseColor(driverData.carColor));
                    }
                }



                String bTitle="";
                if(!expectedKMET.getText().isEmpty()&&!expectedMinutesET.getText().isEmpty()){
                    float expectedKM = Float.parseFloat(expectedKMET.getText());
                    float expectedMinutes = Float.parseFloat(expectedMinutesET.getText());
                    float cost = driverData.cost.base+(driverData.cost.km*expectedKM)+(driverData.cost.minute+expectedMinutes);
                    if(cost<driverData.cost.minimum)
                        cost=driverData.cost.minimum;
                    bTitle= getResources().getString(R.string.expected)+" "+cost+" "+driverData.cost.currency;

                }else {
                    if (driverData.carManufacturer != null)
                        bTitle = driverData.carManufacturer + " ";

                    if (driverData.carModel != null&&driverData.carModel.length()>0)
                        bTitle = bTitle + driverData.carModel + " ";

                    if (driverData.carMadeYear != null)
                        bTitle = bTitle + driverData.carMadeYear;
                }
                //driverInfoBuilder.setTitle(bTitle);
                TextView titleTV = dialogView.findViewById(R.id.title);
                titleTV.setText(bTitle);

                LinearLayout imagesLinearLayout = dialogView.findViewById(R.id.imagesLinearLayout);

                if (driverData.carType!=null&&vehiclesImages!=null) {
                    for(GsonResponse.Vehicle s : vehiclesImages) {
                        if(driverData.carType.contentEquals(s.type)) {
                            BtnImage btnImage = new BtnImage(getActivity());
                            btnImage.setImage(s.image, database);
                            imagesLinearLayout.addView(btnImage);
                        }
                    }
                }

                if(driverData.images.containsKey(Constants.frontImageSmall)&&driverData.images.get(Constants.frontImageSmall).length()>0) {
                    BtnImage btnImage = new BtnImage(getActivity());
                    btnImage.setImage(driverData.images.get(Constants.frontImageSmall),database);
                    imagesLinearLayout.addView(btnImage);
                }

                if(driverData.images.containsKey(Constants.sideImageSmall)&&driverData.images.get(Constants.sideImageSmall).length()>0) {
                    BtnImage btnImage = new BtnImage(getActivity());
                    btnImage.setImage(driverData.images.get(Constants.sideImageSmall),database);
                    imagesLinearLayout.addView(btnImage);
                }
                if(driverData.images.containsKey(Constants.backImageSmall)&&driverData.images.get(Constants.backImageSmall).length()>0) {
                    BtnImage btnImage = new BtnImage(getActivity());
                    btnImage.setImage(driverData.images.get(Constants.backImageSmall),database);
                    imagesLinearLayout.addView(btnImage);
                }




            }



            driverInfoBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (tripSingleton._id == null&&User.getInstance().isClient()) {
                        selectDriver((String) marker.getTag());
                    }
                }
            });


            driverInfoBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });


            driverInfoBuilder.setView(dialogView);
            AlertDialog selectDriverDialog = driverInfoBuilder.create();

            selectDriverDialog.show();
            if (tripSingleton._id != null||User.getInstance().isAdmin()) {
                selectDriverDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
            }
            //mService.sendGetDriverInfoToServer((String) marker.getTag());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Button nbutton = selectDriverDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getContext().getColor(R.color.BUTTON_NEGATIVE));
                Button pbutton = selectDriverDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(getContext().getColor(R.color.BUTTON_POSITIVE));
            }
        }
        return true;
    }

    private void setOnCameraMove() {
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraIdleListener(this);
        changeLocationItem.setVisible(false);
        //promoItem.setVisible(false);

        confirmLocationItem.setVisible(true);
    }

    private void unsetOnCameraMove() {
        mMap.setOnCameraMoveListener(null);
        mMap.setOnCameraIdleListener(null);
        changeLocationItem.setVisible(true);
        //promoItem.setVisible(true);


        confirmLocationItem.setVisible(false);
    }

    @Override
    public void onCameraMove() {
        if (clientLocationMarker != null && tripSingleton._id == null) {
            LatLng latLng = mMap.getCameraPosition().target;
            clientLocationMarker.setPosition(latLng);

        }

    }

    @Override
    public void onCameraIdle() {
        if (tripSingleton._id == null) {
            LatLng latLng = mMap.getCameraPosition().target;
            JSONObject map = new JSONObject();
            try {
                map.put("event", "location");
                map.put("_id", User.getInstance()._id);
                map.put("type", User.getInstance().type);
                map.put("latitude", Double.toString(latLng.latitude));
                map.put("longitude", Double.toString(latLng.longitude));
                sendToService(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v==okBtn)
                okTrip();
        else if(v==cancelBtn){

            AlertDialog dialog = cancelBuilder.create();
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getContext().getColor(R.color.BUTTON_NEGATIVE));
                Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(getContext().getColor(R.color.BUTTON_POSITIVE));
            }

        }
        else if(v==arrivedBtn)
                arrivedTrip();
        else if(v==finishBtn){

            AlertDialog dialog = finishBuilder.create();
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getContext().getColor(R.color.BUTTON_NEGATIVE));
                Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(getContext().getColor(R.color.BUTTON_POSITIVE));
            }
        }
        else if(v==chatBtn) {
            showChat();
        }
        else if(v==mapBtn) {
            hideChat();
        }
        else if(v==sendMessageBtn) {
            sendMsg();
        }
        else if(v==callBtn){
            call();
        }
        else if(v==loadingRelativeLayout.retryBtn) {
            getUser();
        }
        else if(v.getId()==R.id.expectedOkBtn){
            if(expectedKMET.isValid()&&expectedMinutesET.isValid())
                expectedLayout.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(expectedKMET.getWindowToken(), 0);
        }
        else if(v.getId()==R.id.expectedCancelBtn){
            expectedLayout.setVisibility(View.GONE);
            expectedKMET.setText("");
            expectedMinutesET.setText("");
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(expectedKMET.getWindowToken(), 0);
        }
    }

    private void getUser(){
        Log.i(tag, "getUser");
        resetVisibility(true);
        MyVolley myVolley = MyVolley.getInstance(getActivity());
        queue = myVolley.getRequestQueue();
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


    private void resetVisibility(Boolean removeMarker) {
        Log.i(tag, "resetVisibility");
        okBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);
        chatBtn.setVisibility(View.GONE);
        callBtn.setVisibility(View.GONE);
        mapBtn.setVisibility(View.GONE);
        arrivedBtn.setVisibility(View.GONE);
        finishBtn.setVisibility(View.GONE);
        try {
            cancelItem.setVisible(false);
            chatItem.setVisible(false);
            callItem.setVisible(false);
            okItem.setVisible(false);
            arrivedItem.setVisible(false);
            finishItem.setVisible(false);
            mapItem.setVisible(false);
            backItem.setVisible(false);
            if (User.getInstance().isClient()) {
                changeLocationItem.setVisible(true);
                item_expected.setVisible(true);
                //promoItem.setVisible(true);

                confirmLocationItem.setVisible(false);
            }
            unreadedMessagesCount = 0;
            if (User.getInstance().isClient()) {
                chatItem.setTitle(getString(R.string.chat_with_driver));
                exitItem.setVisible(false);
            }
            else if(User.getInstance().isDriver()){
                chatItem.setTitle(getString(R.string.chat_with_client));
                exitItem.setVisible(true);
            }
        }
        catch (Exception e){
            Log.i(tag,e.getLocalizedMessage());
        }
        chatLayout.setVisibility(View.GONE);
        try{
            mapFragment.getView().setVisibility(View.VISIBLE);
            //hide keyboard
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(chatMsgEditText.getWindowToken(), 0);
        }
        catch (Exception e){
            Log.i(tag,e.getLocalizedMessage());
        }
        //todo: reset chat
        chatBtn.setImageResource(R.drawable.chat_free);
        chatMsgLayout.removeAllViewsInLayout();
        unreadedMessagesCount = 0;

        tripLayout.setVisibility(View.GONE);
        tripDurationTextView.setText("");
        tripDistanceTextView.setText("");
        tripCostTextView.setText("");

        /*if (polylinePending != null)
            polylinePending.remove();
        polylineOptionsPending = null;
        if (polylineReserved != null)
            polylineReserved.remove();
        polylineOptionsReserved = null;
        if (polylineStarted != null)
            polylineStarted.remove();
        polylineOptionsStarted = null;*/
        lastLatLng = null;

        clientJson = null;
        if(removeMarker) {
            if (drivers != null) {
                for (Map.Entry<String, Marker> m : drivers.entrySet()) {
                    m.getValue().remove();
                }
                drivers.clear();
            }

            if (clients != null) {
                for (Map.Entry<String, Marker> m : clients.entrySet()) {
                    m.getValue().remove();
                }
                clients.clear();
            }
        }
        else {
            loading.setVisibility(View.VISIBLE);
            progressBarLayout.setVisibility(View.VISIBLE);
        }
        if(loginDone) {
            if (User.getInstance().isClient())
                loadingTextView.setText(R.string.search_driver);
            else if(User.getInstance().isDriver())
                loadingTextView.setText(R.string.wait_requests);
            else if(User.getInstance().isAdmin())
                loadingTextView.setText(R.string.monitor_zone);
        }
        /*if (clientLocationMarker != null)
            clientLocationMarker.remove();
        clientLocationMarker = null;*/
        if (User.getInstance().isClient() ) {
            if (clientLocationMarker != null) {
                try {
                    JSONObject map = new JSONObject();
                    map.put("event", "location");
                    map.put("_id", User.getInstance()._id);
                    map.put("type", User.getInstance().type);
                    map.put("latitude", Double.toString(clientLocationMarker.getPosition().latitude));
                    map.put("longitude", Double.toString(clientLocationMarker.getPosition().longitude));
                    sendToService(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    JSONObject map = new JSONObject();
                    map.put("event", "getOneLocation");
                    sendToService(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }



        if((User.getInstance().isClient()||User.getInstance().isAdmin())) {

            if (getContext()!=null&&ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
               try {
                   mMap.setMyLocationEnabled(true);
                   mMap.getUiSettings().setMyLocationButtonEnabled(true);
               }
               catch (Exception e){
                   Log.i(tag,e.getLocalizedMessage());
               }
            }
        }

    }

    private void sendMsg() {

        final String msg = chatMsgEditText.getText().toString().trim();
        chatMsgEditText.setText("");
        if (msg.length() == 0)
            return;
        JSONObject map = new JSONObject();
        try {
            map.put("event", "tripMessage");
            map.put("senderId", User.getInstance()._id);
            map.put("tripId", tripSingleton._id);
            map.put("state", tripSingleton.state);
            map.put("type", User.getInstance().type);
            map.put("clientId", tripSingleton.clientId);
            map.put("driverId", tripSingleton.driverId);
            map.put("message", msg);
            sendToService(map);
            /*AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    TripMessage message = new TripMessage();
                    message.tripId = tripSingleton._id;
                    message.state = tripSingleton.state;
                    message.message = msg;
                    message.type = "sent";
                    message.time = new Date();
                    database.tripMessageDao().insertTripMessage(message);
                }
            });*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        p.setMargins(5, 5, 5, 5);
        linearLayout.setLayoutParams(p);
        ImageView profile = new ImageView(getActivity());

        if (User.getInstance().isDriver()) {
            profile.setBackground(getDrawable(R.drawable.car));
        } else {
            profile.setBackground(getDrawable(R.drawable.client));
        }
        p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 0.9f);
        p.setMarginEnd(5);
        TextView newMsg = new TextView(getActivity());
        newMsg.setLayoutParams(p);
        newMsg.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        newMsg.setText(msg);
        newMsg.setTextColor(getResources().getContext().getColor(R.color.colorPrimary));
        linearLayout.addView(newMsg);
        linearLayout.addView(profile);
        chatMsgLayout.addView(linearLayout);
        //chatMsgScroll.smoothScrollBy(0,ScrollView.FOCUS_DOWN);
        chatMsgScroll.post(new Runnable() {
            @Override
            public void run() {
                chatMsgScroll.smoothScrollBy(0, chatMsgScroll.FOCUS_DOWN);
            }
        });*/

    }

    void setMarkers() {
        Log.i(tag, "setMarkers");
        //TripSingleton tripSingleton = TripSingleton.getInstance();
        Log.i(tag, "trip:" + tripSingleton.getMap());
        if (tripSingleton._id == null) {
            resetVisibility(true);
            if(User.getInstance().isClient()&&showExpectedTrip){
                expectedLayout.setVisibility(View.VISIBLE);
                showExpectedTrip=false;
            }
        }
        else{
            exitItem.setVisible(false);
            if (!driversInfo.containsKey(tripSingleton.driverId)) {
                try {
                    JSONObject map = new JSONObject();
                    map.put("event", "userInfo");
                    map.put("user_id", tripSingleton.driverId);
                    //map.put("_id", User.getInstance()._id);
                    sendToService(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (User.getInstance().isClient()) {
                if (tripSingleton.driverLat != null) {
                    Log.i(tag, "driverLatLng:" + tripSingleton.driverLat + " " + tripSingleton.driverLng);
                    LatLng driverLatLng = new LatLng(tripSingleton.driverLat, tripSingleton.driverLng);
                    if (drivers.containsKey(tripSingleton.driverId)) {
                        drivers.get(tripSingleton.driverId).setIcon(BitmapDescriptorFactory.fromBitmap(orderPointer));

                    } else {
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(driverLatLng));
                        //m.setAlpha(0.8f);
                        m.setIcon(BitmapDescriptorFactory.fromBitmap(orderPointer));
                        m.setTag(tripSingleton.driverId);
                        drivers.put(tripSingleton.driverId, m);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, DEFAULT_ZOOM));
                    }

                    User driverData = driversInfo.get(tripSingleton.driverId);
                    if (driverData != null) {
                        try {
                            if (driverData.carType!=null) {
                                String carType = driverData.carType;
                                Marker m = drivers.get(tripSingleton.driverId);

                                if(vehiclesCachedImages.containsKey("selected_"+carType))
                                    m.setIcon(BitmapDescriptorFactory.fromBitmap(vehiclesCachedImages.get("selected_"+carType)));
                                else
                                    m.setIcon(BitmapDescriptorFactory.fromBitmap(orderPointer));
                            }
                        } catch (Exception e) {

                        }
                    }
                }
                LatLng latLng = new LatLng(tripSingleton.clientLat, tripSingleton.clientLng);
                if (clientLocationMarker == null) {
                    clientLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng));
                    //clientLocationMarker.setAlpha(0.8f);
                    clientLocationMarker.setIcon(BitmapDescriptorFactory.fromBitmap(clientPointer));
                    clientLocationMarker.setTag(User.getInstance()._id);


                } else {
                    clientLocationMarker.setPosition(latLng);
                }

            } else if (User.getInstance().isDriver()) {
                LatLng latLng = new LatLng(tripSingleton.clientLat, tripSingleton.clientLng);
                if (clients.containsKey(tripSingleton.clientId)) {
                    clients.get(tripSingleton.clientId).setIcon(BitmapDescriptorFactory.fromBitmap(clientOrderPointer));
                } else {
                    Marker m = mMap.addMarker(new MarkerOptions()
                            .position(latLng));
                    //m.setAlpha(0.8f);
                    m.setIcon(BitmapDescriptorFactory.fromBitmap(clientOrderPointer));
                    m.setTag(tripSingleton.clientId);
                    clients.put(tripSingleton.clientId, m);
                }


            }

            if (Constants.PENDING.contentEquals(tripSingleton.state)) {
                setPending();
            } else if (Constants.RESERVED.contentEquals(tripSingleton.state)) {
                setReserved();
            } else if (Constants.STARTED.contentEquals(tripSingleton.state)) {
                setStarted();
            }
            final String _id=tripSingleton._id;
//            AsyncTask.execute(new Runnable() {
//                @Override
//                public void run() {

                    final List<TripMessage> messages = database.tripMessageDao().getTripMessage(_id);
//                    try {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
                                chatMsgLayout.removeAllViews();
                                if (messages.size() > 0) {
                                    for (int j = 0; j < messages.size(); j++) {
                                        TripMessage message = messages.get(j);
                                        if (message.createTime != null)
                                            addToChat(dateFormatter3.format(message.createTime), message.message, message.senderId);
                                        else
                                            addToChat("", message.message, message.senderId);
                                    }
                                }
//                            }
//                        });
//                    }
//                    catch (Exception e){
//                        Log.i(tag,e.getLocalizedMessage());
//                    }

                }
//            });


//        }
        /*else if(User.getInstance().isClient()&&isServiceRunning()&&latLng!=null){
            if (clientLocationMarker == null) {
                clientLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng));
                clientLocationMarker.setAlpha(0.8f);
                clientLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.client_pointer));
                clientLocationMarker.setTag(User.getInstance().id);


            } else {
                clientLocationMarker.setPosition(latLng);
            }
        }else if(User.getInstance().isClient()){
            try {
                JSONObject map = new JSONObject();
                map.put("event","getOneLocation");
                sendToService(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
    }

    /*private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TCPService.class.getName().equals(service.service.getClassName())) {
                Log.i(tag, "Service is running");
                return true;
            }
        }
        Log.i(tag, "Service is down");
        return false;
    }*/


   /* @Override
    public boolean onSupportNavigateUp() {

        return true;
    }*/



    void setPending() {
        Log.i(tag, "setPending");
        cancelBtn.setVisibility(View.VISIBLE);
        callBtn.setVisibility(View.VISIBLE);
        tripLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.GONE);
        cancelItem.setVisible(true);
        chatItem.setVisible(true);
        callItem.setVisible(true);
        exitItem.setVisible(false);
        if (User.getInstance().isClient()) {
            changeLocationItem.setVisible(false);
            item_expected.setVisible(false);
            //promoItem.setVisible(false);

            confirmLocationItem.setVisible(false);
        }

        else if (User.getInstance().isDriver()) {
            okBtn.setVisibility(View.VISIBLE);
            okItem.setVisible(true);
            //menu.findItem(R.id.item_accept).setVisible(true);
        }

        hideChat();
//        if(User.getInstance().isClient()&&mapIsReady) {
//            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
//                //mMap.setMyLocationEnabled(false);
//                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
//            }
//        }


    }

    void setReserved(){
        Log.i(tag, "setReserved");
        cancelBtn.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
        callBtn.setVisibility(View.VISIBLE);
        tripLayout.setVisibility(View.GONE);

        cancelItem.setVisible(true);
        chatItem.setVisible(true);
        callItem.setVisible(true);
        if (User.getInstance().isClient()) {
            changeLocationItem.setVisible(false);
            item_expected.setVisible(false);
            //promoItem.setVisible(false);

            confirmLocationItem.setVisible(false);
        }

        else if(User.getInstance().isDriver()) {
            okBtn.setVisibility(View.GONE);
            arrivedBtn.setVisibility(View.VISIBLE);
            finishBtn.setVisibility(View.GONE);

            okItem.setVisible(false);
            arrivedItem.setVisible(true);
            finishItem.setVisible(false);
        }
        if(mapBtn.getVisibility()==View.VISIBLE)
            showChat();
        else
            hideChat();
//        if(User.getInstance().isClient()&&mapIsReady) {
//            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
//                //mMap.setMyLocationEnabled(false);
//                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
//            }
//        }
    }

    void setStarted(){
        Log.i(tag, "setStarted");
        cancelBtn.setVisibility(View.VISIBLE);
        callBtn.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
        cancelItem.setVisible(true);
        chatItem.setVisible(true);
        callItem.setVisible(true);

        if (User.getInstance().isClient()) {
            changeLocationItem.setVisible(false);
            item_expected.setVisible(false);
            //promoItem.setVisible(false);

            confirmLocationItem.setVisible(false);
        }

        else if(User.getInstance().isDriver()) {
            okBtn.setVisibility(View.GONE);
            arrivedBtn.setVisibility(View.GONE);
            finishBtn.setVisibility(View.VISIBLE);

            okItem.setVisible(false);
            arrivedItem.setVisible(false);
            finishItem.setVisible(true);
        }
        if(mapBtn.getVisibility()==View.VISIBLE)
            showChat();
        else
            hideChat();
//        if(User.getInstance().isClient()&&mapIsReady) {
//            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
//                //mMap.setMyLocationEnabled(false);
//                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
//            }
//        }
    }

    void setFinish(){
        Log.i(tag, "setFinish");
        cancelBtn.setVisibility(View.GONE);
        callBtn.setVisibility(View.GONE);
        tripLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.GONE);
        cancelItem.setVisible(false);
        chatItem.setVisible(false);
        callItem.setVisible(false);

        if (User.getInstance().isClient()) {
            changeLocationItem.setVisible(false);
            item_expected.setVisible(false);
            //promoItem.setVisible(false);

            confirmLocationItem.setVisible(false);
        }

        else if(User.getInstance().isDriver()) {
            okBtn.setVisibility(View.GONE);
            arrivedBtn.setVisibility(View.GONE);
            finishBtn.setVisibility(View.GONE);

            okItem.setVisible(false);
            arrivedItem.setVisible(false);
            finishItem.setVisible(false);
        }

        hideChat();
        chatBtn.setVisibility(View.GONE);
        chatItem.setVisible(false);
//        if(User.getInstance().isClient()&&mapIsReady) {
//            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
//                //mMap.setMyLocationEnabled(false);
//                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
//            }
//        }
    }


    void showChat(){
        Log.i(tag, "showChat");
        chatLayout.setVisibility(View.VISIBLE);
        //mapFragment.getView().setVisibility(View.GONE);
        chatBtn.setVisibility(View.GONE);
        chatItem.setVisible(false);
        mapBtn.setVisibility(View.VISIBLE);

        chatBtn.setImageResource(R.drawable.chat_free);
        if(User.getInstance().isClient())
            chatItem.setTitle(getString(R.string.chat_with_driver));
        else if(User.getInstance().isDriver())
            chatItem.setTitle(getString(R.string.chat_with_client));
        backItem.setVisible(true);
        mapItem.setVisible(true);
        unreadedMessagesCount=0;
        tripLayout.setVisibility(View.GONE);


    }

    void hideChat(){
        Log.i(tag, "hideChat");
        chatLayout.setVisibility(View.GONE);
        //mapFragment.getView().setVisibility(View.VISIBLE);
        chatBtn.setVisibility(View.VISIBLE);
        chatItem.setVisible(true);
        mapBtn.setVisibility(View.GONE);
        backItem.setVisible(false);
        mapItem.setVisible(false);
        if(tripSingleton!=null&&tripSingleton.state!=null&&tripSingleton.state.contentEquals(Constants.STARTED))
            tripLayout.setVisibility(View.VISIBLE);
            try {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(chatMsgEditText.getWindowToken(), 0);
            }
            catch (Exception e){
                Log.i(tag,e.getLocalizedMessage());
            }

    }

    void call(){
        Log.i(tag, "call");
        String tel=null;

        if (User.getInstance().isClient() && tripSingleton.driverId != null && driversInfo.containsKey(tripSingleton.driverId) && driversInfo.get(tripSingleton.driverId)!=null){
            User driverData = driversInfo.get(tripSingleton.driverId);
            tel=driverData.mobile;
        }
        else if (clientJson != null ) {
            tel=clientJson.mobile;
        }

        if(tel!=null) {
            final String finalTel=tel;
            callBuilder.setMessage(getString(R.string.call_to) + " " + finalTel);

            callBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + finalTel));
                    startActivity(intent);
                    
                }
            });

            AlertDialog dialog = callBuilder.create();
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getContext().getColor(R.color.BUTTON_NEGATIVE));
                Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(getContext().getColor(R.color.BUTTON_POSITIVE));
            }
        }
    }

    void close(){
        if (tripSingleton._id == null) {

                //unbindService(mConnection);
                stopTCPService();
            
        }
    }

    void addToChat(String time, String message, String senderId){

        /*LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(p1);
        ImageView profile = new ImageView(this);TextView newMsg = new TextView(getActivity());
        profile.setLayoutParams(p3);
        newMsg.setLayoutParams(p2);
        newMsg.setTextIsSelectable(true);
        if(User.getInstance()._id.contentEquals(senderId)) {
            newMsg.setText(*//*time+": "+*//*message);
            newMsg.setTextColor(getResources().getContext().getColor(R.color.colorPrimaryDark));
            newMsg.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            if (User.getInstance().isDriver()) {
                profile.setBackground(getDrawable(R.drawable.car));

            } else {
                profile.setBackground(getDrawable(R.drawable.client));
            }
            linearLayout.addView(profile);
            linearLayout.addView(newMsg);
        }
        else {
            newMsg.setTextColor(getResources().getContext().getColor(R.color.black));
            newMsg.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            newMsg.setText(message*//*+" :"+time*//*);
            if (User.getInstance().isDriver()) {
                profile.setBackground(getDrawable(R.drawable.client));
            } else {
                profile.setBackground(getDrawable(R.drawable.car));
                profile.setBackground(getDrawable(R.drawable.car));
            }
            linearLayout.addView(newMsg);
            linearLayout.addView(profile);

        }*/
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(chatMsgLayout.getContext())
                .inflate(R.layout.support_view_holder, chatMsgLayout, false);


        if(!User.getInstance()._id.contentEquals(senderId)) {
            RelativeLayout senderLayout = linearLayout.findViewById(R.id.sender_layout);
            RelativeLayout receiverLayout = linearLayout.findViewById(R.id.receiver_layout);
            senderLayout.setVisibility(View.GONE);
            receiverLayout.setVisibility(View.VISIBLE);
            TextView t = receiverLayout.findViewById(R.id.receiver_text_view);
            t.setText(message);
            if(User.getInstance().isDriver()){
                ImageView p = receiverLayout.findViewById(R.id.receiver_image);
                p.setImageResource(R.drawable.client);
            }
            else if(User.getInstance().isClient()){
                ImageView p = receiverLayout.findViewById(R.id.receiver_image);
                p.setImageResource(R.drawable.caoutch_small);
            }
            TextView timeV = receiverLayout.findViewById(R.id.receiver_time);
            timeV.setText(time);

        }
        else {
            RelativeLayout senderLayout = linearLayout.findViewById(R.id.sender_layout);
            RelativeLayout receiverLayout = linearLayout.findViewById(R.id.receiver_layout);
            senderLayout.setVisibility(View.VISIBLE);
            receiverLayout.setVisibility(View.GONE);
            TextView t = senderLayout.findViewById(R.id.sender_text_view);
            t.setText(message);
            if(User.getInstance().isDriver()){
                ImageView p = senderLayout.findViewById(R.id.sender_image);
                p.setImageResource(R.drawable.caoutch_small);
            }
            else if(User.getInstance().isClient()){
                ImageView p = senderLayout.findViewById(R.id.sender_image);
                p.setImageResource(R.drawable.client);
            }
            TextView timeV = senderLayout.findViewById(R.id.sender_time);
            timeV.setText(time);

        }
        chatMsgLayout.addView(linearLayout);
        Log.i(tag,"height"+chatMsgLayout.getHeight());
        chatMsgScroll.post(
                new Runnable() {
                    @Override
                    public void run() {
                        chatMsgScroll.scrollTo(0,chatMsgLayout.getHeight());
                    }
                }
        );
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
        stopTCPService();

    }


    void stopTCPService(){
        Log.i(tag, "stopService");
        Activity activity = getActivity();
        if(activity instanceof Main4Activity){
            ((Main4Activity) activity).stopTCPService();
        }
        try {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
            Log.i(tag,"onDestroy"+e.getMessage());
        }
        TimeZone.setDefault(timeZone);
    }


    void startTCPService(){
        Log.i(tag, "startService");
        Activity activity = getActivity();
        if(activity instanceof Main4Activity){
            ((Main4Activity) activity).startTCPService();
        }
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.ServiceToMainActivity));

    }



}

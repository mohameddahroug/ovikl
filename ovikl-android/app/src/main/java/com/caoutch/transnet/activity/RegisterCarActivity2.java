package com.caoutch.transnet.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.caoutch.transnet.Callback;
import com.caoutch.transnet.Constants;
import com.caoutch.transnet.GsonRequest;
import com.caoutch.transnet.GsonResponse;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.R;
import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.UploadFile;
import com.caoutch.transnet.User;
import com.caoutch.transnet.database.AppDatabase;
import com.caoutch.transnet.view.BtnImage;
import com.caoutch.transnet.view.EditTextLayout;
import com.caoutch.transnet.view.LoadingRelativeLayout;
import com.caoutch.transnet.view.SelectImage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterCarActivity2 extends SuperFragment implements View.OnClickListener, Callback {

    LinearLayout carType;
    TextView carTypeErrTV;
    EditTextLayout carModelET;
    EditTextLayout carMadeYearET;

    EditTextLayout carNumberET;
    EditTextLayout carManufacturerET;
    SelectImage carFrontBtn;
    SelectImage carSideBtn;
    SelectImage carBackBtn;
    Button nextDriverBtn;
    private RequestQueue queue;

    boolean isUploading = false;
    public static int PICK_IMAGE = 0;
    boolean mBound = false;


    HorizontalScrollView typesScroll;

    AppDatabase database;

    ArrayList<GsonResponse.Vehicle> vehicles;
    HashMap<BtnImage, String> vehicleBtns = new HashMap<>();
    int density;
    View root;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        tag = "RegisterCarActivity";

        root = inflater.inflate(R.layout.activity_register_car2, container, false);

        density = (int) getResources().getDisplayMetrics().density;
        database = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "caoutch").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        carType = root.findViewById(R.id.carType);
        carTypeErrTV = root.findViewById(R.id.carTypeErrTV);
        carModelET = root.findViewById(R.id.carModelET);
        carMadeYearET = root.findViewById(R.id.carMadeYearET);
        carNumberET = root.findViewById(R.id.carNumberET);
        carManufacturerET = root.findViewById(R.id.carManufacturerET);
        carFrontBtn = root.findViewById(R.id.carFrontBtn);
        carSideBtn = root.findViewById(R.id.carSideBtn);
        carBackBtn = root.findViewById(R.id.carBackBtn);


        carFrontBtn.setOnClickListener(this);
        carSideBtn.setOnClickListener(this);
        carBackBtn.setOnClickListener(this);

        loadingRelativeLayout = root.findViewById(R.id.registerCarActivity);
        loadingRelativeLayout.retryBtn.setOnClickListener(this);


        typesScroll = root.findViewById(R.id.typesScroll);
        nextDriverBtn = root.findViewById(R.id.nextDriverBtn);
        nextDriverBtn.setEnabled(false);
        nextDriverBtn.setOnClickListener(this);

        loadingRelativeLayout.loading();
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

        return root;

    }



    @Override
    public void onPause() {
        Log.i(tag, "onPause "+PICK_IMAGE);
        super.onPause();
    }


    @Override
    public void onResume() {
        Log.i(tag, "onResume");
        super.onResume();
        if (User.getInstance()._id == null)
            getActivity().finish();
        if (PICK_IMAGE == 0 && vehicles != null) {
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
    }


    @Override
    public void onClick(View v) {

        if (v == nextDriverBtn) {
            PICK_IMAGE = 0;
            if (validate())
                callServer();
        } else if (isUploading) {
            Toast toast = Toast.makeText(getActivity(), getString(R.string.wait_upload), Toast.LENGTH_LONG);
            toast.show();
        } else if (v == carFrontBtn.selectBtn) {
            PICK_IMAGE = 5;
            selectImage();
        } else if (v == carFrontBtn.deleteBtn) {
            PICK_IMAGE = 0;
            carFrontBtn.reset();
            User.getInstance().images.put(Constants.frontImage, "");
            User.getInstance().images.put(Constants.frontImageSmall, "");
        } else if (v == carSideBtn.selectBtn) {
            PICK_IMAGE = 6;
            selectImage();
        } else if (v == carSideBtn.deleteBtn) {
            PICK_IMAGE = 0;
            carSideBtn.reset();
            User.getInstance().images.put(Constants.sideImage, "");
            User.getInstance().images.put(Constants.sideImageSmall, "");
        } else if (v == carBackBtn.selectBtn) {
            PICK_IMAGE = 7;
            selectImage();
        } else if (v == carBackBtn.deleteBtn) {
            PICK_IMAGE = 0;
            carBackBtn.reset();
            User.getInstance().images.put(Constants.backImage, "");
            User.getInstance().images.put(Constants.backImageSmall, "");
        } else if (v == loadingRelativeLayout.retryBtn) {
            PICK_IMAGE = 0;
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
        } else if (vehicleBtns.containsKey(v)) {
            User.getInstance().carType = vehicleBtns.get(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                for (BtnImage b : vehicleBtns.keySet()) {
                    b.setBackground(null);
                }
                v.setBackground(getResources().getDrawable(R.drawable.rounded_border));
            } else {
                for (BtnImage b : vehicleBtns.keySet()) {
                    b.setBackgroundDrawable(null);
                }
                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_border));


            }
        }


    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        //    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private boolean validate() {

        carTypeErrTV.setVisibility(View.GONE);

        if (User.getInstance().carType == null || User.getInstance().carType.isEmpty()) {
            carTypeErrTV.setVisibility(View.VISIBLE);
            return false;
        }

        if (!carManufacturerET.isValid()) {
            return false;
        }
        if (!carModelET.isValid()) {
            return false;
        }
        if (!carMadeYearET.isValid()) {
            return false;
        }

        if (!carNumberET.isValid()) {
            return false;
        }
        if (!carFrontBtn.isValid()) {
            return false;
        }
        if (!carSideBtn.isValid()) {
            return false;
        }
        if (!carBackBtn.isValid()) {
            return false;
        }


        return true;
    }

    private void callServer() {
        nextDriverBtn.setEnabled(false);
        loadingRelativeLayout.loading();
        Map<String, String> params = new HashMap<>();
        params.put("_id", User.getInstance()._id);
        params.put("hashedKey", User.getInstance().hashedKey);
        params.put("carType", User.getInstance().carType);
        params.put("carManufacturer", carManufacturerET.getText());
        params.put("carModel", carModelET.getText());
        params.put("carMadeYear", carMadeYearET.getText());
        params.put("carNumber", carNumberET.getText());

        if(User.getInstance().images.get(Constants.frontImage) != null)
            params.put("images." + Constants.frontImage, User.getInstance().images.get(Constants.frontImage));
        if(User.getInstance().images.get(Constants.frontImageSmall) != null)
            params.put("images." + Constants.frontImageSmall, User.getInstance().images.get(Constants.frontImageSmall));
        if(User.getInstance().images.get(Constants.sideImage) != null)
            params.put("images." + Constants.sideImage, User.getInstance().images.get(Constants.sideImage));
        if(User.getInstance().images.get(Constants.sideImageSmall) != null)
            params.put("images." + Constants.sideImageSmall, User.getInstance().images.get(Constants.sideImageSmall));
        if (User.getInstance().images.get(Constants.backImage) != null)
            params.put("images." + Constants.backImage, User.getInstance().images.get(Constants.backImage));
        if (User.getInstance().images.get(Constants.backImageSmall) != null)
            params.put("images." + Constants.backImageSmall, User.getInstance().images.get(Constants.backImageSmall));

        GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                Constants.nodejs_index_url + "/register2/",
                GsonResponse.class,
                params,
                createSaveUserSuccessListener(),
                createSaveUserErrorListener());

        queue.add(myReq);
    }


    private Response.Listener<GsonResponse> createSaveUserSuccessListener() {
        Log.i(tag, "createSaveUserSuccessListener");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {
                // Do whatever you want to do with response;
                Log.i(tag, "json response code: " + response.code);
                Log.i(tag, "json response message: " + response.message);


                if (response.code.contentEquals("200")) {
                    if(User.getInstance().zoneContact!=null&&(response.user.zoneContact==null||response.user.zoneContact.zone==null))
                        response.user.zoneContact=User.getInstance().zoneContact;
                    User.setInstance(response.user);

                    Log.i(tag, "saveUser user id " + User.getInstance()._id);
                    //User.getInstance().saveUser();
                    //AccountKit.logOut();
                    updateUI();

                } else if (response.code.contentEquals("201")) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                    toast.show();

                    nextDriverBtn.setEnabled(true);
                    loadingRelativeLayout.loaded();

                }
            }
        };
    }

    private Response.ErrorListener createSaveUserErrorListener() {
        Log.i(tag, "createSaveUserErrorListener");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: " + error.getMessage());
                Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                toast.show();

                nextDriverBtn.setEnabled(true);
                loadingRelativeLayout.loaded();
            }
        };
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

                    vehicles = response.vehicles;
                    for (GsonResponse.Vehicle v : vehicles) {
                        Log.i(tag, "json response Vehicle: " + v.name);

                        BtnImage btn = new BtnImage(getActivity());
                        btn.setText(v.name);
                        btn.setOnClickListener(RegisterCarActivity2.this);
                        if (!v.image.isEmpty()) {
                            btn.setImage(v.image, database);
                        }
                        carType.addView(btn);
                        //LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) btn.getLayoutParams();
                        //layoutParams.setMargins(5,5,5,5);
                        //btn.setLayoutParams(layoutParams);
                        btn.setOnClickListener(RegisterCarActivity2.this);
                        btn.setPadding(5, 5, 5, 5);
                        vehicleBtns.put(btn, v.type);
                    }


                    User.setInstance(response.user);

                    Log.i(tag, "saveUser user id " + User.getInstance()._id);
                    if (response.config != null)
                        User.getInstance().config = response.config;
                    //User.getInstance().saveUser();
                    //AccountKit.logOut();

                    if (User.getInstance().carModel != null && !User.getInstance().carModel.isEmpty())
                        carModelET.setText(User.getInstance().carModel);
                    if (User.getInstance().carMadeYear != null && !User.getInstance().carMadeYear.isEmpty())
                        carMadeYearET.setText(User.getInstance().carMadeYear);
                    //if(User.getInstance().carColor!=null&&!User.getInstance().carColor.isEmpty())
                    //    carColorTV.setText(User.getInstance().carColor);
                    if (User.getInstance().carNumber != null && !User.getInstance().carNumber.isEmpty())
                        carNumberET.setText(User.getInstance().carNumber);
                    if (User.getInstance().carManufacturer != null && !User.getInstance().carManufacturer.isEmpty())
                        carManufacturerET.setText(User.getInstance().carManufacturer);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        for (BtnImage b : vehicleBtns.keySet()) {
                            if (User.getInstance().carType != null && !User.getInstance().carType.isEmpty() && User.getInstance().carType.contentEquals(vehicleBtns.get(b))) {
                                b.setBackground(getResources().getDrawable(R.drawable.rounded_border));
                                typesScroll.scrollTo((int) b.getX(), 0);
                            } else {
                                b.setBackground(null);
                            }
                        }
                    } else {
                        for (BtnImage b : vehicleBtns.keySet()) {
                            if (User.getInstance().carType != null && !User.getInstance().carType.isEmpty() && User.getInstance().carType.contentEquals(vehicleBtns.get(b))) {
                                b.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_border));
                            } else {
                                b.setBackgroundDrawable(null);
                            }
                        }
                    }


                } else if (response.code.contentEquals("201")) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                    toast.show();
                }

                carFrontBtn.setImage(User.getInstance().images.get(Constants.frontImageSmall), User.getInstance().images.get(Constants.frontImage), database);
                carSideBtn.setImage(User.getInstance().images.get(Constants.sideImageSmall), User.getInstance().images.get(Constants.sideImage), database);
                carBackBtn.setImage(User.getInstance().images.get(Constants.backImageSmall), User.getInstance().images.get(Constants.backImage), database);

                nextDriverBtn.setEnabled(true);
                loadingRelativeLayout.loaded();
            }
        };
    }

    private Response.ErrorListener createGetUserErrorListener() {
        Log.i(tag, "createGetUserErrorListener2");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(tag, "json error: " + error.getMessage());
                loadingRelativeLayout.loadingFailed();

            }
        };
    }




    private void updateUI() {
        //hide keyboard
        nextDriverBtn.setEnabled(true);
        loadingRelativeLayout.loaded();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(carNumberET.getWindowToken(), 0);

        if (User.getInstance().carType == null || User.getInstance().cost == null || User.getInstance().cost.km == 0)
            Navigation.findNavController(root).navigate(R.id.nav_register_prices);
        else{
            Toast toast = Toast.makeText(getContext(), getString(R.string.updated_successfully), Toast.LENGTH_LONG);
            toast.show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(tag, "onActivityResult " + PICK_IMAGE+" "+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        String imagetype = "";

        ArrayList<Uri> listUri = new ArrayList<>();
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            if (PICK_IMAGE == 3)
                imagetype = Constants.carLicenseImage;
            else if (PICK_IMAGE == 5)
                imagetype = Constants.frontImage;
            else if (PICK_IMAGE == 6)
                imagetype = Constants.sideImage;
            else if (PICK_IMAGE == 7)
                imagetype = Constants.backImage;
            isUploading = true;
            nextDriverBtn.setEnabled(false);
            //profileImage.postInvalidate();
            //profileImage.setImageURI(data.getData());
            //uploadFile(data.getData());
            //final Uri finalUri=data.getData();
            //Toast toast = Toast.makeText(SupportActivity.this, getString(R.string.uploadingImage), Toast.LENGTH_LONG);
            //toast.show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    for (int i = 0; i < count; i++)
                        listUri.add(data.getClipData().getItemAt(i).getUri());
                }
            }
            if (data.getData() != null) {
                listUri.add(data.getData());
            }
            if (listUri.size() > 0 && !imagetype.isEmpty()) {

                callService(listUri, imagetype);
            }
        }


    }


    private void callService(final ArrayList<Uri> listUri, final String imagetype) {
        if (imagetype.contentEquals(Constants.frontImage)) {
            carFrontBtn.uploading();
        } else if (imagetype.contentEquals(Constants.sideImage)) {
            carSideBtn.uploading();
        } else if (imagetype.contentEquals(Constants.backImage)) {
            carBackBtn.uploading();
        }


        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(listUri.get(0));
            Bitmap bitmapImage = BitmapFactory.decodeStream(inputStream);
            UploadFile uploadFile = new UploadFile();
            uploadFile.execute(new UploadFile.File(imagetype, bitmapImage, database, this));
        } catch (Exception e) {
            Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
            toast.show();
            if (imagetype.contentEquals(Constants.frontImage)) {
                carFrontBtn.reset();
            } else if (imagetype.contentEquals(Constants.sideImage)) {
                carSideBtn.reset();
            } else if (imagetype.contentEquals(Constants.backImage)) {
                carBackBtn.reset();
            }

        }

    }

    @Override
    public void callback(final ArrayList<String> arr, final String imagetype) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isUploading = false;
                PICK_IMAGE=0;
                nextDriverBtn.setEnabled(true);
                if (arr.size() == 2) {
                    if (imagetype.contentEquals(Constants.frontImage)) {
                        User.getInstance().images.put(Constants.frontImage, arr.get(0));
                        User.getInstance().images.put(Constants.frontImageSmall, arr.get(1));
                        carFrontBtn.setImage(User.getInstance().images.get(Constants.frontImageSmall), User.getInstance().images.get(Constants.frontImage), database);
                    } else if (imagetype.contentEquals(Constants.sideImage)) {
                        User.getInstance().images.put(Constants.sideImage, arr.get(0));
                        User.getInstance().images.put(Constants.sideImageSmall, arr.get(1));
                        carSideBtn.setImage(User.getInstance().images.get(Constants.sideImageSmall), User.getInstance().images.get(Constants.sideImage), database);
                    } else if (imagetype.contentEquals(Constants.backImage)) {
                        User.getInstance().images.put(Constants.backImage, arr.get(0));
                        User.getInstance().images.put(Constants.backImageSmall, arr.get(1));
                        carBackBtn.setImage(User.getInstance().images.get(Constants.backImageSmall), User.getInstance().images.get(Constants.backImage), database);
                    }

                } else {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG);
                    toast.show();
                    if (imagetype.contentEquals(Constants.frontImage)) {
                        carFrontBtn.reset();
                    } else if (imagetype.contentEquals(Constants.sideImage)) {
                        carSideBtn.reset();
                    } else if (imagetype.contentEquals(Constants.backImage)) {
                        carBackBtn.reset();
                    }
                }
            }
        });
    }







}

package com.caoutch.transnet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caoutch.transnet.GetFile;
import com.caoutch.transnet.R;
import com.caoutch.transnet.database.AppDatabase;

public class SelectImage extends LinearLayout implements GetFile.Callback {
    private String large;
    private String small;
    private boolean required=false;
    public ImageView imageView;
    public Button selectBtn;
    public Button deleteBtn;
    ProgressBar progressBar;
    TextView textView;
    LinearLayout linearLayout;
    int density = 1;

    public SelectImage(Context context) {
        super(context);

    }

    public SelectImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        density = (int) context.getResources().getDisplayMetrics().density;
        progressBar=new ProgressBar(context);
        imageView=new ImageView(context);
        selectBtn=new Button(context);
        deleteBtn=new Button(context);
        textView=new TextView(context);
        progressBar.setVisibility(GONE);
        imageView.setVisibility(GONE);

        //selectBtn.setVisibility(GONE);
        deleteBtn.setVisibility(GONE);
        textView.setVisibility(GONE);
        textView.setTextColor(getResources().getColor(R.color.red));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }
        selectBtn.setAllCaps(false);
        deleteBtn.setAllCaps(false);

        selectBtn.setMinWidth(180*density);
        deleteBtn.setMinWidth(100*density);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            selectBtn.setBackground(getResources().getDrawable(R.drawable.rounded_border));
            deleteBtn.setBackground(getResources().getDrawable(R.drawable.rounded_border));
            selectBtn.setPadding(5*density,0,5*density,0);
            deleteBtn.setPadding(5*density,0,5*density,0);
        }

        linearLayout=new LinearLayout(context);
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.addView(progressBar);
        linearLayout.addView(imageView);
        linearLayout.addView(selectBtn);
        linearLayout.addView(deleteBtn);




        addView(linearLayout);
        addView(textView);
        selectBtn.setText(getResources().getString( Integer.parseInt(attrs.getAttributeValue("http://ovikl.com","select_text").replace("@",""))));
        deleteBtn.setText(getResources().getString( Integer.parseInt(attrs.getAttributeValue("http://ovikl.com","delete_text").replace("@",""))));
        textView.setText(getResources().getString( Integer.parseInt(attrs.getAttributeValue("http://ovikl.com","error_text").replace("@",""))));
        required=attrs.getAttributeBooleanValue("http://ovikl.com","required",false);

        LayoutParams layoutParams= (LayoutParams) linearLayout.getLayoutParams();
        layoutParams.width=ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height=ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity=Gravity.CENTER;
        linearLayout.setLayoutParams(layoutParams);

        LayoutParams layoutParams2= (LayoutParams) deleteBtn.getLayoutParams();
        layoutParams2.gravity=Gravity.CENTER;
        layoutParams2.leftMargin=5*density;
        layoutParams2.leftMargin=5*density;
        deleteBtn.setLayoutParams(layoutParams2);

    }

    public SelectImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void reset(){
        imageView.setImageBitmap(null);
        small=null;
        progressBar.setVisibility(GONE);
        selectBtn.setVisibility(VISIBLE);
        deleteBtn.setVisibility(GONE);
        imageView.setVisibility(GONE);
    }

    public void uploading(){
        progressBar.setVisibility(VISIBLE);
        selectBtn.setVisibility(GONE);
        deleteBtn.setVisibility(GONE);
        imageView.setVisibility(GONE);
    }
    public void setImage(String small,String large, AppDatabase database) {
        if(small==null||small.isEmpty()){
            reset();
            return;
        }
        progressBar.setVisibility(VISIBLE);
        selectBtn.setVisibility(GONE);
        this.small=small;

        GetFile getFile = new GetFile(database,this);
        getFile.execute(small);



    }

    public boolean isValid(){

        if(imageView.getVisibility()==GONE&&required) {
            textView.setVisibility(VISIBLE);
            return false;
        }
        else {
            textView.setVisibility(GONE);
            return true;
        }
    }


    public void callback(Bitmap bitmap) {
        if(bitmap!=null) {
            LayoutParams layoutParams = new LayoutParams(100*density, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity=Gravity.CENTER_VERTICAL;
            imageView.setLayoutParams(layoutParams);
            imageView.setMaxHeight(100*density);
            imageView.setImageBitmap(bitmap);
            imageView.setAdjustViewBounds(true);
            imageView.setVisibility(VISIBLE);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            progressBar.setVisibility(GONE);
            textView.setVisibility(GONE);
            deleteBtn.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        selectBtn.setOnClickListener(l);
        deleteBtn.setOnClickListener(l);
    }
}

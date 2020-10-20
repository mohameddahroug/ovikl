package com.caoutch.transnet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caoutch.transnet.GetFile;
import com.caoutch.transnet.database.AppDatabase;

public class BtnImage extends LinearLayout implements GetFile.Callback {
    public ImageView imageView;
    private TextView textView;
    private ProgressBar progressBar;

    public BtnImage(Context context) {
        super(context);
        int w = (int) (80*context.getResources().getDisplayMetrics().density);
        LayoutParams layoutParams = new LayoutParams(w,w);
        layoutParams.gravity= Gravity.CENTER;

        //LayoutParams layoutParams2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        setOrientation(VERTICAL);
        imageView=new ImageView(context);
        textView=new TextView(context);
        progressBar=new ProgressBar(context);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }
        imageView.setLayoutParams(layoutParams);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setPadding(5,5,5,5);
        imageView.setVisibility(GONE);

        //textView.setLayoutParams(layoutParams2);

        addView(progressBar);
        addView(imageView);
        addView(textView);

        textView.setVisibility(GONE);

    }




    public BtnImage(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        int w = (int) (80*context.getResources().getDisplayMetrics().density);
        String width=attrs.getAttributeValue("http://schemas.android.com/apk/res/android","layout_width");
        if(width!=null&&width.contains("dip")) {
            w=Integer.parseInt(width.replace(".0dip","")) * (int) context.getResources().getDisplayMetrics().density;
            Log.i("layout_width",attrs.getAttributeValue("http://schemas.android.com/apk/res/android","layout_width"));

        }
        LayoutParams layoutParams = new LayoutParams(w,w);
        layoutParams.gravity= Gravity.CENTER_HORIZONTAL;
        setOrientation(VERTICAL);
        imageView=new ImageView(context);
        textView=new TextView(context);
        progressBar=new ProgressBar(context);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setPadding(5,5,5,5);

        if(attrs.getAttributeValue("http://ovikl.com","src")!=null) {
            imageView.setImageResource(Integer.parseInt(attrs.getAttributeValue("http://ovikl.com", "src").replace("@", "")));
            progressBar.setVisibility(GONE);
        }
        if(attrs.getAttributeValue("http://ovikl.com","text")!=null) {
            textView.setText(getResources().getString(Integer.parseInt(attrs.getAttributeValue("http://ovikl.com", "text").replace("@", ""))));
            textView.setVisibility(VISIBLE);
        }
        else{
            textView.setVisibility(GONE);
        }
        textView.setPadding(0,0,0,0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }
        imageView.setLayoutParams(layoutParams);

        addView(progressBar);
        addView(imageView);
        addView(textView);

    }

    public BtnImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setImage(final String s, final AppDatabase database) {

        progressBar.setVisibility(VISIBLE);
        imageView.setVisibility(GONE);
        GetFile getFile = new GetFile(database,this);
        getFile.execute(s);
    }


    @Override
    public void callback(Bitmap bitmap) {
        if(bitmap!=null) {
            //LayoutParams layoutParams = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);


            //imageView.setLayoutParams(layoutParams);
            //imageView.setMaxHeight(100);
            imageView.setImageBitmap(bitmap);
            imageView.setAdjustViewBounds(true);
            imageView.setVisibility(VISIBLE);
            progressBar.setVisibility(GONE);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    public void setText(String s){
        textView.setText(s);
        textView.setVisibility(VISIBLE);
    }
}

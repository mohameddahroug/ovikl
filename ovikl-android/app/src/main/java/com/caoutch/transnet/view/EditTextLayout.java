package com.caoutch.transnet.view;

import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caoutch.transnet.R;

public class EditTextLayout extends LinearLayout implements TextWatcher, View.OnClickListener {
    LinearLayout linearLayout;
    LinearLayout linearLayout2;
    TextView textView;
    public EditText editText;
    public TextView textViewErr;
    public TextView buttonShow;
    public TextView buttonHide;
    private String regexp;
    private boolean required=false;

    public EditTextLayout(Context context) {
        super(context);
    }

    public EditTextLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setOrientation(VERTICAL);
        int density = (int) getResources().getDisplayMetrics().density;
        linearLayout=new LinearLayout(context);
        linearLayout2=new LinearLayout(context);
        linearLayout2.setOrientation(HORIZONTAL);
        textView=new TextView(context);
        editText=new EditText(context);
        textViewErr=new TextView(context);
        buttonShow=new TextView(context);
        buttonHide=new TextView(context);
        editText.setLines(1);
        editText.setMaxLines(1);
        editText.setSingleLine();
        buttonShow.setText(R.string.show);
        buttonShow.setVisibility(GONE);
        buttonHide.setText(R.string.hide);
        buttonHide.setVisibility(GONE);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.addView(textView);
        linearLayout2.addView(editText);
        linearLayout2.addView(buttonShow);
        linearLayout2.addView(buttonHide);
        linearLayout.addView(linearLayout2);
        linearLayout2.getLayoutParams().width= LayoutParams.MATCH_PARENT;
        LayoutParams params = (LayoutParams) editText.getLayoutParams();
        params.weight = 1.0f;
        editText.setLayoutParams(params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            linearLayout.setBackground(getResources().getDrawable(R.drawable.rounded_border_light));
        }
        linearLayout.setPadding(5*density,5*density,5*density,5*density);
        addView(linearLayout);
        addView(textViewErr);

        if(attrs.getAttributeValue("http://ovikl.com","title")==null)
            textView.setVisibility(GONE);
        else
            textView.setText(getResources().getString( Integer.parseInt(attrs.getAttributeValue("http://ovikl.com","title").replace("@",""))));
        textViewErr.setText(getResources().getString( Integer.parseInt(attrs.getAttributeValue("http://ovikl.com","error").replace("@",""))));
        editText.setHint(getResources().getString( Integer.parseInt(attrs.getAttributeValue("http://ovikl.com","hint").replace("@",""))));
        if(attrs.getAttributeValue("http://ovikl.com","inputType")!=null){
            if(attrs.getAttributeValue("http://ovikl.com","inputType").contentEquals("textPassword")) {
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                buttonShow.setVisibility(VISIBLE);
            }
            else if(attrs.getAttributeValue("http://ovikl.com","inputType").contentEquals("textEmailAddress"))
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS|InputType.TYPE_CLASS_TEXT);
            else if(attrs.getAttributeValue("http://ovikl.com","inputType").contentEquals("phone"))
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PHONETIC|InputType.TYPE_CLASS_PHONE);
            else if(attrs.getAttributeValue("http://ovikl.com","inputType").contentEquals("decimal"))
                editText.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_DECIMAL);
            else if(attrs.getAttributeValue("http://ovikl.com","inputType").contentEquals("number"))
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        required=attrs.getAttributeBooleanValue("http://ovikl.com","required",false);
        regexp=attrs.getAttributeValue("http://ovikl.com","regexp");
        textViewErr.setVisibility(GONE);
        editText.addTextChangedListener(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            editText.setBackground(null);
        }
        editText.setPadding(5*density,0,5*density,0);
        buttonShow.setPadding(5*density,0,5*density,0);
        buttonHide.setPadding(5*density,0,5*density,0);
        textView.setPadding(0,0,0,0);
        textViewErr.setPadding(0,0,0,0);
        textViewErr.setTextColor(getResources().getColor(R.color.red));
        textViewErr.setGravity(Gravity.CENTER);

        buttonShow.setOnClickListener(this);
        buttonHide.setOnClickListener(this);
    }

    public EditTextLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isValid(){
        if(editText.getInputType()==(InputType.TYPE_TEXT_VARIATION_PHONETIC|InputType.TYPE_CLASS_PHONE))
            editText.setText(editText.getText().toString().replace("+","00"));

        String s = editText.getText().toString();
        if(s.isEmpty()&&!required) {
            textViewErr.setVisibility(View.GONE);
            return true;
        }

        if(s.matches(regexp)){
            if(editText.getInputType()==(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_DECIMAL)) {
                Float f =Float.parseFloat(editText.getText().toString());
                editText.setText(f.toString());
                if(f<=0) {
                    textViewErr.setVisibility(View.VISIBLE);
                    textViewErr.setVisibility(View.VISIBLE);
                    return false;
                }
            }
            textViewErr.setVisibility(View.GONE);
            return true;
        }
        textViewErr.setVisibility(View.VISIBLE);
        return false;
    }

    public boolean match(String s){
        String t = editText.getText().toString();
        if(textViewErr.getVisibility()==GONE){
            if(!s.isEmpty()&&!t.isEmpty()&&!s.contentEquals(t)){
                textViewErr.setVisibility(View.VISIBLE);
                return false;
            }
        }
        return true;
    }

    public void reset(){
        textViewErr.setVisibility(GONE);
        editText.setText("");
    }

    public void setTextTitle(String text){
        textView.setText(text);
    }

    public void setTextHint(String text){
        editText.setHint(text);
    }

    public void setTextErr(String text){
        textViewErr.setText(text);
    }

    public void setText(String text){
        editText.setText(text);
    }

    public String getText(){
        if(editText.getText().toString()==null)
            return "";
        else
            return editText.getText().toString();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        //Log.i("EditTextLayout",s.toString()+regexp+s.toString().matches(regexp));
        if(s.toString().matches(regexp)||(!required&&s.toString().length()==0)){
            textViewErr.setVisibility(View.GONE);
        }
        else{
            textViewErr.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if(v==buttonShow){
            editText.setInputType( InputType.TYPE_CLASS_TEXT);
            buttonShow.setVisibility(GONE);
            buttonHide.setVisibility(VISIBLE);
        }
        else if(v==buttonHide){
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            buttonShow.setVisibility(VISIBLE);
            buttonHide.setVisibility(GONE);
        }
    }
}

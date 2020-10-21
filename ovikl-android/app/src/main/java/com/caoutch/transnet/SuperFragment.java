package com.caoutch.transnet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.caoutch.transnet.activity.LoginActivity;
import com.caoutch.transnet.activity.RegisterPricesActivity;
import com.caoutch.transnet.view.LoadingRelativeLayout;

public class SuperFragment extends Fragment {
    public String tag="SuperFragment";
    public LoadingRelativeLayout loadingRelativeLayout;


    @Override
    public void onAttach(@NonNull Context context) {
        Log.i(tag,"onAttach");
        super.onAttach(context);
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag,"onCreate");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(tag,"onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i(tag,"onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(tag,"onStart");

    }

    @Override
    public void onResume() {
        Log.i(tag,"onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(tag,"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(tag,"onStop");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(tag,"onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag,"onDestroy");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(tag,"onDetach");
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
            case R.id.item_logout:
//                User.getInstance().reset();
//                Intent i = new Intent(RegisterPricesActivity.this, LoginActivity.class);
//                startActivity(i);
//                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}

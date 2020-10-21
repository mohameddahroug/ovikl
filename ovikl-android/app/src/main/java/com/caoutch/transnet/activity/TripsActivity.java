package com.caoutch.transnet.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caoutch.transnet.SuperFragment;
import com.caoutch.transnet.TripsAdapter;
import com.caoutch.transnet.MyVolley;
import com.caoutch.transnet.R;
import com.caoutch.transnet.view.LoadingRelativeLayout;

public class TripsActivity extends SuperFragment implements View.OnClickListener, TextWatcher {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    EditText editText;
    TextView button ;

    int y=0;
    View root;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_trips, container, false);
        tag = "TripsActivity";
        Log.i(tag, "onCreateView");

        loadingRelativeLayout=root.findViewById(R.id.activity_trips);
        recyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TripsAdapter(MyVolley.getInstance(getContext()),getContext(),"");
        recyclerView.setAdapter(mAdapter);


        editText = root.findViewById(R.id.search_edittext);
        editText.addTextChangedListener(this);
        button =  root.findViewById(R.id.search_button2);
        button.setOnClickListener(this);
        /*final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                .getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.i(tag, "onScrollStateChanged "+newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                y=y+dy;
                Log.i(tag, "onScrolled "+dx+" "+dy+" " +" "+linearLayoutManager.getItemCount()+" "+linearLayoutManager.findLastCompletelyVisibleItemPosition()+" "+linearLayoutManager.findFirstCompletelyVisibleItemPosition());



            }


        });*/
        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadingRelativeLayout.loaded();
        editText.setText("");
        editText.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(tag, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.

        menu.clear();
        inflater.inflate(R.menu.support_main, menu);

        super.onCreateOptionsMenu(menu,inflater);
        menu.findItem(R.id.item_refresh).setVisible(true);
        menu.findItem(R.id.item_search).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_support:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@yourdomain.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Ovikl support");
                //need this to prompts email client only
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email client :"));
                break;
            case R.id.item_refresh:

                editText.setText("");
                editText.setVisibility(View.GONE);
                button.setVisibility(View.GONE);

                mAdapter = new TripsAdapter(MyVolley.getInstance(getContext()),getContext(),"");
                recyclerView.setAdapter(mAdapter);

                break;

            case R.id.item_search:
                if(editText.getVisibility()==View.GONE) {
                    editText.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                }
                else{
                    editText.setText("");
                    editText.setVisibility(View.GONE);
                    button.setVisibility(View.GONE);

                    mAdapter = new TripsAdapter(MyVolley.getInstance(getContext()),getContext(),"");
                    recyclerView.setAdapter(mAdapter);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View v) {
        String c= editText.getText().toString().replace(" ","");

        //editText.moveCursorToVisibleOffset();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        Log.i(tag,c);

        mAdapter = new TripsAdapter(MyVolley.getInstance(getContext()),getContext(),c);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String c = editText.getText().toString();
        if(c.contains("\n")){
            c= c.replace("\n","").replace(" ","");
            editText.setText(c);
            editText.setSelection(c.length());
            //editText.moveCursorToVisibleOffset();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            Log.i(tag,c);
            mAdapter = new TripsAdapter(MyVolley.getInstance(getContext()),getContext(),c);
            recyclerView.setAdapter(mAdapter);
        }
    }
}



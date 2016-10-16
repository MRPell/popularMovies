package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by MRPell on 4/22/2016.
 */
public class DetailActivity extends ActionBarActivity {


    /*
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new DetailActivityFragment())
                    .commit();
        }
    }





}

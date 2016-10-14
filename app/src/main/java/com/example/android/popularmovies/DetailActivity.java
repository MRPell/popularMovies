package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import static com.example.android.popularmovies.R.id.container;

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
                    .add(container, new DetailActivityFragment())
                    .commit();
        }
    }

    public void rename(View view){
        final Button button = (Button) view.findViewById(R.id.favorite_button);
        button.setText("Favorite");
    }



}

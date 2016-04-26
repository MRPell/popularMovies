package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    /**
     * Fragment containing the movie information display
     */
    public static class DetailFragment extends Fragment {

        private final String LOG_TAG = DetailFragment.class.getSimpleName();


        public DetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for movie data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                ArrayList<String> posterPathStr = intent.
                        getStringArrayListExtra(Intent.EXTRA_TEXT);


                //format string into year only
                String movieReleaseDate = posterPathStr.get(4);
                SimpleDateFormat date = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
                Date d = new Date(00000000);
                try {
                    d = date.parse(movieReleaseDate);
                    Log.v("test", d.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                int year = cal.get(Calendar.YEAR);




                //set all text views in the activity with movie data
                ((TextView) rootView.findViewById(R.id.detail_title_text))
                        .setText(posterPathStr.get(0));
                ((TextView) rootView.findViewById(R.id.detail_synopsis_text))
                        .setText(posterPathStr.get(2));
                ((TextView) rootView.findViewById(R.id.detail_rating_text))
                        .setText(posterPathStr.get(3) + "/10");
//                ((TextView) rootView.findViewById(R.id.detail_release_date_text))
//                        .setText(Integer.toString(year));
                ((TextView) rootView.findViewById(R.id.detail_release_date_text)).setText(posterPathStr.get(4));
                //populate image view with movie poster
                Picasso.with(getContext()).load(
                        getString(R.string.poster_base_url) + posterPathStr.get(1)).
                        into((ImageView) rootView.findViewById(R.id.detail_poster_image));
            }
            return rootView;
        }

    }
}


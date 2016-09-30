package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
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
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String MOVIE_SHARE_HASHTAG = " #PopularMoviesApp";

        private ShareActionProvider mShareActionProvider;
        private String mMovieDetails;

        private static final int DETAIL_LOADER = 0;


        private static final String[] MOVIE_COLUMNS = {
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_POSTER_BITMAP,
                MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                MovieContract.MovieEntry.COLUMN_USER_RATING,
                MovieContract.MovieEntry.COLUMN_SYNOPSIS,
                MovieContract.MovieEntry._ID
        };

        // These indices are tied to MOVIE_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        static final int COL_MOVIE_ID = 0;
        static final int COL_MOVIE_POSTER = 1;
        static final int COL_MOVIE_TITLE = 2;
        static final int COL_MOVIE_RELEASE_DATE = 3;
        static final int COL_MOVIE_USER_RATING = 4;
        static final int COL_MOVIE_SYNOPIS = 5;
        static final int ID = 6;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // If onLoadFinished happens before this, we can go ahead and set the share intent now.
            if (mMovieDetails != null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }
        }

        private Intent createShareMovieIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mMovieDetails + MOVIE_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            Log.v("On create Loader", intent.getData().toString());
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) {
                return;
            }
            Log.v("On finished loeader", data.getString(COL_MOVIE_ID));
            Log.i("movie id", Integer.toString(data.getInt(ID)));
            //data.moveToPosition(data.getInt(ID));
            String posterPath = data.getString(COL_MOVIE_POSTER);

            String title = data.getString(COL_MOVIE_TITLE);

            String releaseDate = data.getString(COL_MOVIE_RELEASE_DATE);

            String userRating = data.getString(COL_MOVIE_USER_RATING);

            String synopsis = data.getString(COL_MOVIE_SYNOPIS);


            //format string into year only
            String movieReleaseDate = releaseDate;
            SimpleDateFormat date = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
            Date d = new Date(00000000);
            try {
                d = date.parse(movieReleaseDate);
                Log.v("test", d.toString());
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            int year = cal.get(Calendar.YEAR);


            //set all text views in the activity with movie data
            TextView detailTextView1 = (TextView) getView().findViewById(R.id.detail_title_text);
            detailTextView1.setText(title);

            TextView detailTextView2 = (TextView) getView().findViewById(R.id.detail_synopsis_text);
            detailTextView2.setText(synopsis);
            TextView detailTextView3 = (TextView) getView().findViewById(R.id.detail_rating_text);
            detailTextView3.setText(userRating + "/10");
            TextView detailTextView4 = (TextView) getView().findViewById(R.id.detail_release_date_text);
            detailTextView4.setText(Integer.toString(year));
            //populate image view with movie poster
            Picasso.with(getContext()).load(
                    getString(R.string.poster_base_url) + posterPath).
                    into((ImageView) getView().findViewById(R.id.detail_poster_image));


            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }
}

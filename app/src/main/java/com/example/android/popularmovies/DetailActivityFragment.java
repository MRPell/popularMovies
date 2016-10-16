package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by MRPell on 10/1/2016.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends android.support.v4.app.Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    ArrayAdapter<String> mTrailerAdapter;
    private String mMovieId;
    private String mShareMovieKey;
    private String mMovieTitle;

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final String MOVIE_SHARE_HASHTAG = " #PopularMoviesApp";

    private ShareActionProvider mShareActionProvider;
    private HashMap mMovieDetails = new HashMap();

    private static final int DETAIL_LOADER = 0;


    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_BITMAP,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_FAVORITE

    };

    // These indices are tied to MOVIE_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_POSTER = 2;
    static final int COL_MOVIE_TITLE = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_USER_RATING = 5;
    static final int COL_MOVIE_SYNOPIS = 6;
    static final int COL_MOVIE_FAVORITE = 7;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    public void displayTrailers() {
        FetchTrailerTask fetchTrailers = new FetchTrailerTask();
        FetchReviewsTask fetchReviews = new FetchReviewsTask();
        FetchRuntimeTask fetchRuntime = new FetchRuntimeTask();

        fetchTrailers.execute(mMovieId);
        fetchReviews.execute(mMovieId);
        fetchRuntime.execute(mMovieId);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        final Button button = (Button) rootView.findViewById(R.id.favorite_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//            }            // do something
                if (mMovieId != null) {

                    String strFilter = MovieContract.MovieEntry.COLUMN_MOVIE_ID +
                            "=" +
                            mMovieId;

                    ContentValues args = new ContentValues();
                    if (Utilities.isFavorite(getContext(), mMovieId)) {
                        args.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0);
                    } else {
                        args.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 1);
                    }
                    getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                            args,
                            strFilter,
                            null);
                }
                setButtonText(button);
                Log.d(LOG_TAG + "Button Movie ID: ", mMovieId);
            }
        });
        //Retrieve Trailers

        return rootView;

    }


    public void setButtonText(Button button) {
        if (mMovieId != null) {
            Log.d(LOG_TAG + "mMovieId SetButton", "set Button Text");
            Log.d(LOG_TAG + "IS FAVORITE? ", Boolean.toString(Utilities.isFavorite(getContext(), mMovieId)));
            if (Utilities.isFavorite(getContext(), mMovieId)) {
                button.setText("Remove From\nFavorites");
            } else {
                button.setText("Mark as\nFavorite");
            }
        }
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
//        if (mMovieDetails != null && mShareMovieKey != null) {
//            mShareActionProvider.setShareIntent(createShareMovieIntent());
//        }
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mMovieTitle + "-" +
                        mShareMovieKey + ": " +
                        mMovieDetails.get(mShareMovieKey).toString() +
                        MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null || intent.getData() == null) {
            return null;
        }

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
        if (!data.moveToFirst()) {
            return;
        }
        //data.moveToPosition(data.getInt(ID));
        mMovieId = Integer.toString(data.getInt(COL_MOVIE_ID));

        String posterPath = data.getString(COL_MOVIE_POSTER);

        mMovieTitle = data.getString(COL_MOVIE_TITLE);

        String releaseDate = data.getString(COL_MOVIE_RELEASE_DATE);

        String userRating = data.getString(COL_MOVIE_USER_RATING);

        String synopsis = data.getString(COL_MOVIE_SYNOPIS);

        setButtonText((Button) getActivity().findViewById(R.id.favorite_button));

        //format string into year only
//        String movieReleaseDate = releaseDate;
//        SimpleDateFormat date = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
//        Date d = new Date(00000000);
//        try {
//            d = date.parse(movieReleaseDate);
//        } catch (java.text.ParseException e) {
//            e.printStackTrace();
//        }
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(d);
//        int year = cal.get(Calendar.YEAR);


        //set all text views in the activity with movie data
        TextView detailTextView1 = (TextView) getView().findViewById(R.id.detail_title_text);
        detailTextView1.setText(mMovieTitle);

        TextView detailTextView2 = (TextView) getView().findViewById(R.id.detail_synopsis_text);
        detailTextView2.setText(synopsis);
        TextView detailTextView3 = (TextView) getView().findViewById(R.id.detail_rating_text);
        detailTextView3.setText(userRating + "/10");
        TextView detailTextView4 = (TextView) getView().findViewById(R.id.detail_release_date_text);
        detailTextView4.setText(Utilities.formatReleaseDate(releaseDate));
        //populate image view with movie poster
        Picasso.with(getContext()).load(
                getString(R.string.poster_base_url) + posterPath).
                into((ImageView) getView().findViewById(R.id.detail_poster_image));
        displayTrailers();


        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public class FetchTrailerTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getTrailerDataFromJson(String trailerJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TRAILER_RESULTS = "results";
            final String TRAILER_LINK = "key";
            final String TRAILER_NAME = "name";
            final String TRAILER_WEBSITE = "site";


            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray(TRAILER_RESULTS);


            String[] resultStrs = new String[trailerArray.length()];
            for (int i = 0; i < trailerArray.length(); i++) {
                // Get the JSON object representing the day
                JSONObject trailerObject = trailerArray.getJSONObject(i);

                // description is in a child array called "weather", which is 1 element long.
                String trailerLink = trailerObject.getString(TRAILER_LINK);
                String trailerName = trailerObject.getString(TRAILER_NAME);
                String trailerWebsite = trailerObject.getString(TRAILER_WEBSITE);

                //resultStrs[i] = trailerName; //
                //"http://www.youtube.com/watch?v=cxLG2wtE7TM")
                //"http://www." + trailerWebsite + ".com/watch?v=" + trailerLink;

                resultStrs[i] = trailerName + "-"
                        + "http://www." + trailerWebsite + ".com/watch?v=" + trailerLink;
            }

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String TrailerJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String TMDB_BASE_URL =
                        "http://api.themoviedb.org/3/movie/";
                final String ID_PARAM = mMovieId;
                final String TRAILER_PARAM = "videos";
                final String API_ID = "api_key";
                final String REVIEWS_PARAM = "reviews";


                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(ID_PARAM)
                        .appendPath(TRAILER_PARAM)
                        .appendQueryParameter(API_ID, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                TrailerJsonStr = buffer.toString();


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailerDataFromJson(TrailerJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mTrailerAdapter =
                    new ArrayAdapter<String>(
                            getActivity(), // The current context (this activity)
                            R.layout.list_item_trailer, // The name of the layout ID.
                            R.id.list_item_trailer_textview);
//                            android.R.layout.simple_list_item_1);
            //weekForecast);
//            ArrayAdapter<String> reviewAdapter =
//                    new ArrayAdapter<String>(
//                            getActivity(), // The current context (this activity)
//                            R.layout.list_item_reviews, // The name of the layout ID.
//                            R.id.list_item_review_textview);


            // Get a reference to the ListView, and attach this adapter to it.
            ListView listView = (ListView) getActivity().findViewById(R.id.listview_trailer);
            listView.setAdapter(mTrailerAdapter);
//            mTrailerAdapter.setNotifyOnChange(true);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // CursorAdapter returns a cursor at the correct position for getItem(), or null
                    // if it cannot seek to that position.
                    String trailerUrl = (String) adapterView.getItemAtPosition(position);
                    if (trailerUrl != null) {
                        Log.v(LOG_TAG + "Movie ID", trailerUrl);
                        Uri detailUri = Uri.parse(mMovieDetails.get(trailerUrl).toString());
                        Log.v(LOG_TAG, detailUri.toString());
                        startActivity(new Intent(Intent.ACTION_VIEW, detailUri));
                    }
                }
            });


            if (result != null) {
                mTrailerAdapter.clear();
                mMovieDetails.clear();
                String shareKey[] = result[0].split("-");
                mShareMovieKey = shareKey[0];
                Log.d(LOG_TAG + "MOVIE SHARE KEY", mShareMovieKey);

                for (String trailerStr : result) {
                    String movieInfo[] = trailerStr.split("-");
                    Log.d(LOG_TAG + "MOVIE INFO", movieInfo[0]);
                    Log.d(LOG_TAG + "MOVIE INFO", movieInfo[1]);
                    mMovieDetails.put(movieInfo[0], movieInfo[1]);
                    mTrailerAdapter.add(movieInfo[0]);
                }
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareMovieIntent());
                }
            }

        }


    }

    public class FetchReviewsTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getReviewDataFromJson(String ReviewJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String REVIEW_RESULTS = "results";
            final String REVIEW_TEXT = "content";
            final String REVIEW_AUTHOR = "author";


            JSONObject reviewJson = new JSONObject(ReviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(REVIEW_RESULTS);


            String[] resultStrs = new String[reviewArray.length()];
            for (int i = 0; i < reviewArray.length(); i++) {
                // Get the JSON object representing the day
                JSONObject trailerObject = reviewArray.getJSONObject(i);

                // description is in a child array called "weather", which is 1 element long.

                String reviewAUTHOR = trailerObject.getString(REVIEW_AUTHOR);
                String reviewContent = trailerObject.getString(REVIEW_TEXT);

                resultStrs[i] = reviewContent + "\n" + reviewAUTHOR; //+ " - " + "http://www." + trailerWebsite + ".com/" + trailerLink;
            }

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String ReviewJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String TMDB_BASE_URL =
                        "http://api.themoviedb.org/3/movie/";
                final String ID_PARAM = mMovieId;
                final String API_ID = "api_key";
                final String REVIEWS_PARAM = "reviews";


                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(ID_PARAM)
                        .appendPath(REVIEWS_PARAM)
                        .appendQueryParameter(API_ID, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.d("REVIEW URL", url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                ReviewJsonStr = buffer.toString();


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewDataFromJson(ReviewJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            //weekForecast);
            ArrayAdapter<String> reviewAdapter =
                    new ArrayAdapter<String>(
                            getActivity(), // The current context (this activity)
                            R.layout.list_item_reviews, // The name of the layout ID.
                            R.id.list_item_review_textview);


            ListView listViewReviews = (ListView) getActivity().findViewById(R.id.listview_reviews);
            listViewReviews.setAdapter(reviewAdapter);
            reviewAdapter.setNotifyOnChange(true);

            if (result != null) {
                reviewAdapter.clear();
                for (String trailerStr : result) {
                    reviewAdapter.add(trailerStr);
                }
            }

        }


    }

    public class FetchRuntimeTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchRuntimeTask.class.getSimpleName();


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getRuntimeDataFromJson(String RuntimeJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String RUNTIME = "runtime";

            JSONObject runtimeJson = new JSONObject(RuntimeJsonStr);

            String[] runtime = new String[1];
            runtime[0] = runtimeJson.getString(RUNTIME) + "min";
            return runtime;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String RuntimeJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String TMDB_BASE_URL =
                        "http://api.themoviedb.org/3/movie/";
                final String ID_PARAM = mMovieId;
                final String API_ID = "api_key";


                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(ID_PARAM)
                        .appendQueryParameter(API_ID, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.d("REVIEW URL", url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                RuntimeJsonStr = buffer.toString();


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getRuntimeDataFromJson(RuntimeJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            if (result != null) {
                TextView textViewRuntime = (TextView) getActivity().findViewById(R.id.detail_running_time_text);
                textViewRuntime.setText(result[0]);
            }
        }

    }

}

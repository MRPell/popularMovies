package com.example.android.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by MRPell on 8/6/2016.
 */

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MOVIE_NOTIFICATION_ID = 3004;


    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     *
     * @param movieJsonStr the json data from TMDB API
     */
//    private static final String[] NOTIFY_MOVIE_PROJECTION = new String[]{
//            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
//            MovieContract.MovieEntry.COLUMN_POSTER_BITMAP,
//            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
//            MovieContract.MovieEntry.COLUMN_SYNOPSIS,
//            MovieContract.MovieEntry.COLUMN_USER_RATING
//            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
//
//    };
//
//    //these indices must match the projection
//    private static final int INDEX_MOVIE_ID = 0;
//    private static final int INDEX_POSTER_BITMAP = 1;
//    private static final int INDEX_MOVIE_TITLE = 2;
//    private static final int INDEX_SYNOPSIS = 3;
//    private static final int INDEX_USER_RATING = 4;
//    private static final int INDEX_RELEASE_DATE = 5;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "starting sync");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            // Construct the URL for TMDB query
            // Possible parameters are avaiable at TMDB API page, at
            // https://www.themoviedb.org/documentation/api
            final String TMDB_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie/";

            //get sort preference from settings selection
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String sortPref = sharedPref.getString(
                    "Sort Method",
                    "popularity.desc");

            //set minimum number of votes required to show movie so that movies
            //without strong rating verification aren't displayed
            //eventually this should be made a default user setting that can be switched off
            String MIN_VOTES = "vote_count.gte";
            String numVotes;
            if(sortPref.equals("vote_average.desc")){
                numVotes = "50";
            }
            else
            {
                numVotes = "0";
            }

            final String API_ID = "api_key";
            final String SORT_TYPE = "sort_by";

            //api call. Add api key to gradle properties
            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendQueryParameter(MIN_VOTES, numVotes)
                    .appendQueryParameter(SORT_TYPE, sortPref)
                    .appendQueryParameter(API_ID, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            Log.d(LOG_TAG, builtUri.toString());
            URL url = new URL(builtUri.toString());
            //runtime
            //https://api.themoviedb.org/3/movie/271110?api_key=cd93405233ea65dd6b464a88cbc57515


            // Create the request to TMDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
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
                return;
            }
            movieJsonStr = buffer.toString();
            Log.v(LOG_TAG, movieJsonStr);
            getMovieDataFromJson(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data,
            // there's no point in attemping to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
        return;
    }

    private void getMovieDataFromJson(String movieJsonStr) throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        // Movie information.  Each movie's info is an element of the "results" array.
        final String MDB_MOVIE_ID = "id";
        final String MDB_RESULTS = "results";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_TITLE = "original_title";
        final String MDB_SYNOPSIS = "overview";
        final String MDB_USER_RATING = "vote_average";
        final String MDB_RELEASE_DATE = "release_date";


        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {
                // These are the values that will be collected.
                int movieId;
                String posterPath;
                String originalTitle;
                String synopsis;
                double userRating;
                String releaseDate;

                // Get the JSON object representing a movie
                JSONObject movieObject = movieArray.getJSONObject(i);
                movieId = movieObject.getInt(MDB_MOVIE_ID);
                posterPath = movieObject.getString(MDB_POSTER_PATH);
                originalTitle = movieObject.getString(MDB_TITLE);
                synopsis = movieObject.getString(MDB_SYNOPSIS);
                userRating = movieObject.getDouble(MDB_USER_RATING);
                releaseDate = movieObject.getString(MDB_RELEASE_DATE);


                Log.v(LOG_TAG + "movie ID", Integer.toString(movieId));
                ContentValues movieValues = new ContentValues();


                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_BITMAP, posterPath);
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, originalTitle);
                movieValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, synopsis);
                movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, userRating);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0);

                cVVector.add(movieValues);
            }

            //****************fix delete **********************
            //check this for accuracy
            // delete old data so we don't build up an endless history
            String[] rowsToDelete = new String[1];
            rowsToDelete[0] = "0";
            getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_FAVORITE + "= ?",
                    rowsToDelete);
//                    MovieContract.MovieEntry._ID + "<= ?", new String[] {"10"});

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);

            }

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've fd an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
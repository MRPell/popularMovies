package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by MRPell on 10/15/2016.
 */

public class Utilities {
    private static final String LOG_TAG = Utilities.class.getSimpleName();


    public static boolean isFavoriteSortPref(Context context) {
        SharedPreferences SharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String sortPref = SharedPref.getString(
                "Sort Method",
                "popularity.desc");
        return sortPref.contentEquals("favorites");
    }

    public static boolean isFavorite(Context context, String movieId) {
        Log.d(LOG_TAG, "In Is Favorite");
        final String[] MOVIE_COLUMNS = {
                MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_FAVORITE
        };

        boolean returnVal = false;

        final int COL_MOVIE_ID = 1;
        final int COL_MOVIE_FAVORITE = 2;
        final String[] QUERY_PARAMS = {"1"};

        Cursor c = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS,
                MovieContract.MovieEntry.COLUMN_FAVORITE + " = ? ",
                QUERY_PARAMS,
                null);

        if (!c.moveToFirst()) {
            Log.d(LOG_TAG + "CURSOR STATUS", "CURSOR IS EMPTY");
            return returnVal;
        }


        do {
            Log.d(LOG_TAG + "Is FAVORITE MOVIE ID: ", c.getString(COL_MOVIE_ID));
            Log.d(LOG_TAG + "Is FAVORITE Favorite: ", c.getString(COL_MOVIE_FAVORITE));
            if (c.getString(COL_MOVIE_ID).equals(movieId)) {
                if (c.getString(COL_MOVIE_FAVORITE).equals("1")) {
                    returnVal = true;
                }
            }
        }
        while (c.moveToNext());

        c.close();
        Log.d(LOG_TAG, "End is favorites");
        return returnVal;
    }

    public static String formatReleaseDate(String releaseDate) {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        Date d = new Date(00000000);
        try {
            d = date.parse(releaseDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int year = cal.get(Calendar.YEAR);
        return Integer.toString(year);
    }
}

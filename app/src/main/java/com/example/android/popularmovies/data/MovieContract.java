package com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by MRPell on 8/5/2016.
 */

public class MovieContract {

    //empty constructor to prevent someone from accidentally instantiating it
    MovieContract() {
    }

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.popularmovies.app/movie/ is a valid path for
    // looking at movie data. content://com.example.android.popularmovies.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_MOVIE_DETAILS = "movies";

    // To make it easy to query for the exact release date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long releasDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(releasDate);
        int julianDay = Time.getJulianDay(releasDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner class that defines the table contents of the movie details table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_DETAILS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_DETAILS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_DETAILS;

        // Table name
        public static final String TABLE_NAME = "movies";

        // The movie name string is what will be sent to the movie db
        // as the movie query.
        public static final String COLUMN_MOVIE_ID = "MovieId";
        public static final String COLUMN_MOVIE_TITLE = "movie_name";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_RUNNING_TIME = "running_time";
        public static final String COLUMN_USER_RATING = "user_rating";
        public static final String COLUMN_POSTER_BITMAP = "poster_bitmap";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_TRAILER_LINK = "trailer";
        public static final String COLUMN_USER_REVIEWS = "user_reviews";
        public static final String COLUMN_FAVORITE = "favorite";

        public static Uri buildMovieUri(long id) {
            Uri returnUri;
            if (id != -1)
                returnUri = ContentUris.withAppendedId(CONTENT_URI, id);
else
                returnUri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_DETAILS).build();

            return returnUri;
        }

        public static long getMovieIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

    }


}


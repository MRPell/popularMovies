package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.sync.MovieSyncAdapter;

/**
 * Fragment of main activity, displays a grid view of movie poster images
 * for user to select
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    ImageAdapter mMovieImageAdapter; //used to place image views into gridview
    private int mPosition = GridView.INVALID_POSITION;
    private static final int MOVIE_LOADER = 0;
    private static final String SELECTED_KEY = "selected_position";
    private final String LOG_TAG = MovieFragment.class.getSimpleName();
    private GridView mGridView;
    private boolean mUseTodayLayout;

    private static final String[] MOVIE_COLUMNS = {
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
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_POSTER = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_RELEASE_DATE = 3;
    static final int COL_MOVIE_USER_RATING = 4;
    static final int COL_MOVIE_SYNOPIS = 5;
    static final int COL_MOVIE_FAVORITE = 6;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri movieIdUri);
    }

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //open settings activity
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getContext(),
                    com.example.android.popularmovies.SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        mMovieImageAdapter = new ImageAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //the view that handles all the images
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mMovieImageAdapter);


        //handles what to do when user clicks on an image
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    //Log.v(LOG_TAG + "Movie ID", Long.toString(cursor.getLong(COL_MOVIE_ID)));
                    //Uri detailUri = MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID));
                    ((Callback) getActivity()).onItemSelected(MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID)));
//                    //content://com.example.android.popularmovies.app/movies/0
//                    ((Callback) getActivity())
//                            .onItemSelected(MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID)));
//                }
//                mPosition = position;
//            }
//        });

                }
            }
        });
        return rootView;
    }

    /*
     *this method updates the home screen movie list based on the user
     * setting selection
     */
        private void updateMovies () {
            MovieSyncAdapter.syncImmediately(getActivity());
        }

        @Override
        public void onActivityCreated (Bundle savedInstanceState){
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }
//
//        //get user sort setting
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//        String sortPref = sharedPref.getString(
//                getString(R.string.sort_method_key),
//                getString(R.string.sort_pref_default));
//
//        //get data and display it in background thread
//        moviesTask.execute(sortPref, numOfPages);
//    }

    /*
     *start app with movie list populated by last user setting
     */
        @Override
        public void onStart () {
            super.onStart();
            updateMovies();
        }

        @Override
        public void onSaveInstanceState (Bundle outState){
            // When tablets rotate, the currently selected list item needs to be saved.
            // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
            // so check for that before storing.
            if (mPosition != ListView.INVALID_POSITION) {
                outState.putInt(SELECTED_KEY, mPosition);
            }
            super.onSaveInstanceState(outState);
        }

        @Override
        public Loader<Cursor> onCreateLoader ( int i, Bundle bundle){
            // This is called when a new Loader needs to be created.  This
            // fragment only uses one loader, so we don't care about checking the id.

            // To only show current and future dates, filter the query to return weather only for
            // dates after or including today.

            // Sort order:  Ascending, by date.
//            SharedPreferences SharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//            String sortPref = SharedPref.getString(
//                    "Sort Method",
//                    "popularity.desc");
            String sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " ASC";

            Uri movieUri = MovieContract.MovieEntry.buildMovieUri(-1);

            String[] rowsToAdd = new String[1];
            if(Utility.isFavoriteSortPref(getContext())) {
                rowsToAdd[0] = "1";
            }
            else {
                rowsToAdd[0] = "0";
            }

//            getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
//                    MovieContract.MovieEntry.COLUMN_FAVORITE + "= ?",
//                    rowsToDelete);


            Log.d(LOG_TAG + "cursor loader uri", movieUri.toString());
            Log.d(LOG_TAG + "Where arg", MovieContract.MovieEntry.COLUMN_FAVORITE + "= ?" );
            Log.d(LOG_TAG + "Where variable", rowsToAdd[0].toString());

            return new CursorLoader(getActivity(),
                    movieUri,
                    MOVIE_COLUMNS,
                    //null,
                    //null,
                    MovieContract.MovieEntry.COLUMN_FAVORITE + " = ? ",
                    rowsToAdd,
                    sortOrder);
        }

        @Override
        public void onLoadFinished (Loader < Cursor > loader, Cursor data){
            Log.d(LOG_TAG + "Cursor Size; ", Integer.toString(data.getCount()));
            mMovieImageAdapter.swapCursor(data);
            if (mPosition != ListView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                mGridView.smoothScrollToPosition(mPosition);
            }
        }

        @Override
        public void onLoaderReset (Loader < Cursor > loader) {
            mMovieImageAdapter.swapCursor(null);
        }


    }
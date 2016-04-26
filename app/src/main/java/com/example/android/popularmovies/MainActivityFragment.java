package com.example.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Fragment of main activity, displays a grid view of movie poster images
 * for user to select
 */
public class MainActivityFragment extends Fragment {
    ImageAdapter movieImageAdapter; //used to place image views into gridview
    ArrayList<ArrayList<String>> allMovieInfo; //will contain parsed json data for other activities
    String numOfPages = "1";

    public MainActivityFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        movieImageAdapter = new ImageAdapter(getContext(),
                R.layout.movie_image_grid_view, // The name of the layout ID.
                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //the view that handles all the images
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(movieImageAdapter);

        //handles what to do when user clicks on an image
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String posterPath = movieImageAdapter.getItem(position);
                ArrayList<String> movieDetails = allMovieInfo.get(position);
                //open detail activity where more information about the movie is displayed
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putStringArrayListExtra(Intent.EXTRA_TEXT, movieDetails);
                startActivity(intent);
            }
        });
        return rootView;
    }

    /*
     *this method updates the home screen movie list based on the user
     * setting selection
     */
    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        //get user sort setting
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sortPref = sharedPref.getString(
                getString(R.string.sort_method_key),
                getString(R.string.sort_pref_default));

        //get data and display it in background thread
        moviesTask.execute(sortPref, numOfPages);
    }

    /*
     *start app with movie list populated by last user setting
     */
    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }


    /**
     * Handles getting and parsing movie json data in background thread
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         *
         * @param movieJsonStr the json data from TMDB API
         */
        private String[] parseJsonMovieData(String movieJsonStr)
                throws JSONException {


            // These are the names of the JSON objects that need to be extracted.

            final String MDB_PAGES = numOfPages;
            final String MDB_POSTER_PATH = "poster_path";
            final String MDB_RESULTS = "results";
            final String MDB_TITLE = "original_title";
            final String MDB_SYNOPSIS = "overview";
            final String MDB_USER_RATING = "vote_average";
            final String MDB_RELEASE_DATE = "release_date";


            //get one page of results
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);
            int numOfResults = movieArray.length();

            //will contain all the movies as arraylist of movie details
            //will contain all the movies as arraylist of movie details
            allMovieInfo = new ArrayList<ArrayList<String>>(0);
            //the movie poster urls will be stored in this array
            String[] posterPaths = new String[numOfResults];

            //parse through a page of results
            for (int i = 0; i < movieArray.length(); i++) {
                ArrayList<String> movieDetails = new ArrayList<String>(numOfResults);

                //a single movie object
                JSONObject movieObject = movieArray.getJSONObject(i);

                //retrieve wanted movie details from the movie object
                String synopsis = movieObject.getString(MDB_SYNOPSIS);
                String posterPath = movieObject.getString(MDB_POSTER_PATH);
                String title = movieObject.getString(MDB_TITLE);
                String releaseDate = movieObject.getString(MDB_RELEASE_DATE);
                double rating = movieObject.getDouble(MDB_USER_RATING);

                //complete poster url and store it
                posterPaths[i] = getString(R.string.poster_base_url) + posterPath;
                //add movie details to array list
                movieDetails.add(title);
                movieDetails.add(posterPath);
                movieDetails.add(synopsis);
                movieDetails.add(Double.toString(rating));
                movieDetails.add(releaseDate);

                //add arraylist of movie details to list of movies
                allMovieInfo.add(i, movieDetails);
            }

            return posterPaths;

        }

        @Override
        protected String[] doInBackground(String... params) {
            //Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;
            //the user setting for sorting
            String sortType = params[0];
            numOfPages = params[1] != null ? params[1].toString() : "1";

            try {
                // Construct the URL for TMDB query
                // Possible parameters are avaiable at TMDB API page, at
                // https://www.themoviedb.org/documentation/api
                final String TMDB_BASE_URL =
                        getString(R.string.tmdb_base_url) + params[0] + "?";
                Log.v(LOG_TAG, params[0]);
                Log.v(LOG_TAG, TMDB_BASE_URL);
                final String API_ID = "api_key";
                //final String SORT_BY = "sort_by";
                final String PAGE = "page";

                //api call. Add api key to gradle properties
                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendQueryParameter(PAGE, numOfPages)
                        //.appendQueryParameter(SORT_BY, sortType)
                        .appendQueryParameter(API_ID, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to TMDB, and open the connection
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
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, movieJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data,
                // there's no point in attemping to parse it.
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

            //parse the data and return the result which will be an array list of poster paths
            try {
                return parseJsonMovieData(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movie data
            return null;
        }

        /*
         *update the display by adding movies to the image adapter
         */
        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                movieImageAdapter.clear();
                for (String movieDetailsStr : result) {
                    Log.v(LOG_TAG, movieDetailsStr);
                    movieImageAdapter.add(movieDetailsStr);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }
}
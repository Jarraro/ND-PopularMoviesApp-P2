package jarrar.com.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
import java.util.Map;


public class MainFragment extends Fragment {

    View rootView;
    private MoviesAdapter moviesAdapter;
    private ArrayList<Movie> moviesList;
    SharedPreferences sharedPreferences;
    public final String LOG_TAG = MainFragment.class.getSimpleName();
    GridView gridViewMovies;
    Boolean allowUpdate = true;
    String SORT_BY;
    private int mPosition = -1; //to keep track of the selected item and save it onSaveInstanceState

    public MainFragment() {
    }

    public interface Callback {
        public void onItemSelected(Movie movie);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moviesList = new ArrayList<>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SORT_BY = sharedPreferences.getString("sortBy", "popularity");
        if (savedInstanceState != null && savedInstanceState.containsKey("movies")) {
            moviesList = savedInstanceState.getParcelableArrayList("movies");
            allowUpdate = false;
        }

        if (savedInstanceState != null && savedInstanceState.containsKey("selectedItem"))
            mPosition = savedInstanceState.getInt("selectedItem");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("movies", moviesList);
        outState.putInt("selectedItem", mPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridViewMovies = (GridView) rootView.findViewById(R.id.movies_list_gridView);
        moviesAdapter = new MoviesAdapter(getActivity(), moviesList);
        gridViewMovies.setAdapter(moviesAdapter);

        gridViewMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((Callback) getActivity()).onItemSelected(moviesList.get(position));
                mPosition = position;
            }
        });

        if (allowUpdate && !SORT_BY.equals(getString(R.string.FAVORITE_PARAM)))
            new RequestMoviesTask().execute(SORT_BY);
        return rootView;

    }

    public class RequestMoviesTask extends AsyncTask<String, Void, Void> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJsonStr = null;
        Uri builtUri;
        Boolean SINGLE_MOVIE = false;

        @Override
        protected Void doInBackground(String... params) {
            if (params[0].equals(getString(R.string.RATING_PARAM)) || params[0].equals(getString(R.string.POPULARITY_PARAM))) {
                // Get List of movies by either popularity or rating
                SINGLE_MOVIE = false;
                builtUri = Uri.parse(getString(R.string.BASE_URL)).buildUpon()
                        .appendQueryParameter(getString(R.string.API_KEY_PARAM), getString(R.string.API_KEY))
                        .appendQueryParameter(getString(R.string.LANGUAGE_PARAM), getString(R.string.LANGUAGE_EN_PARAM))
                        .appendQueryParameter(getString(R.string.SORT_PARAM), params[0] + getString(R.string.SORT_DESC_PARAM))
                        .build();
            } else { // Get a single movie by ID
                SINGLE_MOVIE = true;
                builtUri = Uri.parse(getString(R.string.MOVIE_PREFIX)).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(getString(R.string.API_KEY_PARAM), getString(R.string.API_KEY))
                        .build();
            }

            try {
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty
                }
                resultJsonStr = buffer.toString();

                if (!SINGLE_MOVIE) {
                    moviesList.clear();
                    extractListFromJSON(resultJsonStr);
                } else extractMovieFromJSON(resultJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error ", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            moviesAdapter.notifyDataSetChanged();
        }
    }

    private void extractListFromJSON(String jsonStr) {

        try {
            moviesList.clear();
            JSONObject moviesJson = new JSONObject(jsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray("results");

            for (int i = 0; i < moviesArray.length(); i++) {
                Movie tempMovie = new Movie();
                JSONObject jo = moviesArray.getJSONObject(i);
                tempMovie.setTitle(jo.getString(getString(R.string.ORIGIONAL_TITLE_PARAM)));
                tempMovie.setId(jo.getString(getString(R.string.ID)));
                tempMovie.setPoster_Location(jo.getString(getString(R.string.POSTER_PATH_PARAM)));
                tempMovie.setBackdrop_path(jo.getString(getString(R.string.BACKDROP_PATH_PARAM)));
                tempMovie.setOverview(jo.getString(getString(R.string.OVERVIEW_PARAM)));
                tempMovie.setRelease_Date(jo.getString(getString(R.string.RELEASE_DATE_PARAM)));
                tempMovie.setRating(jo.getString(getString(R.string.RATING_PARAM)));
                tempMovie.setPopularity(jo.getString(getString(R.string.POPULARITY_PARAM)));
                moviesList.add(tempMovie);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractMovieFromJSON(String jsonStr) {

        try {
            JSONObject jo = new JSONObject(jsonStr);

            Movie tempMovie = new Movie();
            tempMovie.setTitle(jo.getString(getString(R.string.ORIGIONAL_TITLE_PARAM)));
            tempMovie.setId(jo.getString(getString(R.string.ID)));
            tempMovie.setPoster_Location(jo.getString(getString(R.string.POSTER_PATH_PARAM)));
            tempMovie.setBackdrop_path(jo.getString(getString(R.string.BACKDROP_PATH_PARAM)));
            tempMovie.setOverview(jo.getString(getString(R.string.OVERVIEW_PARAM)));
            tempMovie.setRelease_Date(jo.getString(getString(R.string.RELEASE_DATE_PARAM)));
            tempMovie.setRating(jo.getString(getString(R.string.RATING_PARAM)));
            tempMovie.setPopularity(jo.getString(getString(R.string.POPULARITY_PARAM)));
            moviesList.add(tempMovie);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getFavoriteList() {
        if (isPreferencesSet()) {
            moviesList.clear();
            SharedPreferences sharedPreferencesFavorite = getActivity().getSharedPreferences(getString(R.string.FAVORITE_KEY), Context.MODE_PRIVATE);
            // iterate through all movie ID's
            Map<String, ?> allEntries = sharedPreferencesFavorite.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                new RequestMoviesTask().execute(entry.getValue().toString());
            }
        } else
            Toast.makeText(getActivity(), R.string.Warning_Favorite_Empty, Toast.LENGTH_LONG).show();
    }

    public boolean isPreferencesSet() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.FAVORITE_KEY), Context.MODE_PRIVATE);
        ;
        return (sharedPreferences.getAll().size() > 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPosition != -1)
            gridViewMovies.smoothScrollToPosition(mPosition);

        if (MainActivity.prefChanged) {
            SORT_BY = sharedPreferences.getString("sortBy", "popularity");
            if (SORT_BY.equals(getString(R.string.FAVORITE_PARAM)))
                getFavoriteList();
            else {
                new RequestMoviesTask().execute(SORT_BY);
                moviesList.clear();
            }
            MainActivity.prefChanged = false;
        } else {  // OnResume, if SortBy favorite, force refresh;
            // so if a movie was removed from the favorite list
            if (SORT_BY.equals(getString(R.string.FAVORITE_PARAM)))
                getFavoriteList();
        }
    }

}
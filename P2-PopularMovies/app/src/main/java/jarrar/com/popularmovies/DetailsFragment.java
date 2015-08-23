package jarrar.com.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {

    public final String LOG_TAG = MainFragment.class.getSimpleName();
    View rootView;
    Intent intent;
    Movie mMovie;
    String roundedRate;
    ArrayList<Review> mReviews = new ArrayList<>();
    ArrayList<Trailer> mTrailers = new ArrayList<>();
    static final String MOVIE_KEY = "Movie";
    private final String TRAILER = "trailer";
    private final String REVIEW = "reviews";
    LinearLayout trailersLinearLayout;
    LinearLayout reviewsLinearLayout;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Menu mMenu;
    ImageButton favoriteButton;
    Boolean isFavorite = false;

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getActivity().getIntent();
        mMovie = intent.getParcelableExtra(MOVIE_KEY);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovie = arguments.getParcelable(MOVIE_KEY);
        }
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.FAVORITE_KEY), Context.MODE_PRIVATE);
        rootView = inflater.inflate(R.layout.fragment_details, container, false);

        ImageView image_backdrop = (ImageView) rootView.findViewById(R.id.imageView_backdrop);
        ImageView image_poster = (ImageView) rootView.findViewById(R.id.imageView_poster);
        TextView text_title = (TextView) rootView.findViewById(R.id.textView_movie_title);
        TextView text_releaseDate = (TextView) rootView.findViewById(R.id.textView_release_date);
        TextView text_rating = (TextView) rootView.findViewById(R.id.textView_rating);
        TextView text_overview = (TextView) rootView.findViewById(R.id.textView_overview);
        favoriteButton = (ImageButton) rootView.findViewById(R.id.imageButtonFavorite);
        trailersLinearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayoutTrailers);
        reviewsLinearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayoutReviews);

        Picasso.with(getActivity()).load(getString(R.string.BASE_URL_DROPBACK) + mMovie.backdrop_path)
                .into(image_backdrop);
        Picasso.with(getActivity()).load(getString(R.string.BASE_URL_POSTER) + mMovie.poster_Location)
                .into(image_poster);
        text_title.setText(mMovie.title);
        text_releaseDate.setText(mMovie.release_Date);
        roundedRate = String.valueOf(Math.round(Float.parseFloat(mMovie.rating)));
        text_rating.setText(roundedRate + "/10");
        text_overview.setText(mMovie.overview);

        trailersLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = ((ViewGroup) trailersLinearLayout.getParent()).indexOfChild(v);
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.YOUTUBE_PATH)
                                + mTrailers.get(position).getPath())));
            }
        });
        if (sharedPreferences.contains(mMovie.title)) {
            isFavorite = true;
            setFavorite(true);
        }
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorite) {
                    setFavorite(false);
                    removeFavorite();
                    isFavorite = false;
                } else {
                    setFavorite(true);
                    addFavorite();
                    isFavorite = true;
                }
            }
        });

        if (savedInstanceState != null) {
            if (!savedInstanceState.containsKey(TRAILER)) {
                new GetMoreDataTask().execute(TRAILER);
            } else {
                mTrailers = savedInstanceState.getParcelableArrayList(TRAILER);
                inflateTrailers();
            }
            if (!savedInstanceState.containsKey(REVIEW)) {
                new GetMoreDataTask().execute(REVIEW);
            } else {
                mReviews = savedInstanceState.getParcelableArrayList(REVIEW);
                inflateReviews();
            }
        } else { // savedInstance == null
            new GetMoreDataTask().execute(TRAILER);
            new GetMoreDataTask().execute(REVIEW);
        }

        return rootView;
    }

    private void setFavorite(boolean b) {
        if (b)
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
        else
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRAILER, mTrailers);
        outState.putParcelableArrayList(REVIEW, mReviews);
    }

    private void addFavorite() {
        editor = sharedPreferences.edit();
        editor.putString(mMovie.title, mMovie.id);
        editor.apply();
        Toast.makeText(getActivity(), "Added to favorite list", Toast.LENGTH_SHORT).show();
    }

    private void removeFavorite() {
        editor = sharedPreferences.edit();
        editor.remove(mMovie.title);
        editor.apply();
        Toast.makeText(getActivity(), "Removed from favorite list", Toast.LENGTH_SHORT).show();
    }

    // AsyncTask to get Trailers/Reviews
    public class GetMoreDataTask extends AsyncTask<String, Void, Void> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJsonStr = null;
        String queryParam;

        @Override
        protected Void doInBackground(String... params) {
            Uri builtUri = null;
            queryParam = params[0];
            if (queryParam.equals(TRAILER)) { //Get trailer

                builtUri = Uri.parse(getString(R.string.MOVIE_PREFIX))
                        .buildUpon()
                        .appendPath(mMovie.id)
                        .appendPath(getString(R.string.TRAILER_SUFFIX))
                        .appendQueryParameter(getString(R.string.API_KEY_PARAM), getString(R.string.API_KEY))
                        .build();

            } else if (queryParam.equals(REVIEW)) { //Get Reviews
                builtUri = Uri.parse(getString(R.string.MOVIE_PREFIX))
                        .buildUpon()
                        .appendPath(mMovie.id)
                        .appendPath(getString(R.string.REVIEWS_SUFFIX))
                        .appendQueryParameter(getString(R.string.API_KEY_PARAM), getString(R.string.API_KEY))
                        .build();
            }

            try {
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, url.toString());
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
                if (queryParam.equals(TRAILER))
                    extractTrailersFromJSON(resultJsonStr);
                else
                    extractReviewsFromJSON(resultJsonStr);


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
            if (queryParam.equals(TRAILER)) { // refresh trailersListView
                inflateTrailers();
            } else { // refresh reviewsList
                inflateReviews();
            }

        }

    }

    private void extractTrailersFromJSON(String jsonStr) {

        try {
            JSONObject moviesJson = new JSONObject(jsonStr);
            JSONArray trailersArray = moviesJson.getJSONArray("results");

            for (int i = 0; i < trailersArray.length(); i++) {
                Trailer tempTrailer = new Trailer();
                JSONObject jo = trailersArray.getJSONObject(i);
                tempTrailer.setName(jo.getString(getString(R.string.TRAILER_NAME_KEY)));
                tempTrailer.setPath(jo.getString(getString(R.string.YOUTUBE_LINK_KEY)));
                mTrailers.add(tempTrailer);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractReviewsFromJSON(String jsonStr) {

        try {
            JSONObject moviesJson = new JSONObject(jsonStr);
            JSONArray trailersArray = moviesJson.getJSONArray("results");

            for (int i = 0; i < trailersArray.length(); i++) {
                Review tempReview = new Review();
                JSONObject jo = trailersArray.getJSONObject(i);
                tempReview.setAuthor(jo.getString(getString(R.string.REVIEW_AUTHOR_KEY)));
                tempReview.setContent(jo.getString(getString(R.string.REVIEW_CONTENT_KEY)));
                mReviews.add(tempReview);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void inflateTrailers() {
        for (int i = 0; i < mTrailers.size(); i++) {
            View row = View.inflate(getActivity(), R.layout.list_item_trailer, null);
            TextView title = (TextView) row.findViewById(R.id.textViewTrailerTitle);
            trailersLinearLayout.removeView(row);
            title.setText(mTrailers.get(i).getName());
            trailersLinearLayout.addView(row);
        }
    }

    private void inflateReviews() {
        for (int i = 0; i < mReviews.size(); i++) {
            View row = View.inflate(getActivity(), R.layout.list_item_review, null);
            TextView author = (TextView) row.findViewById(R.id.textViewReviewAuthor);
            TextView content = (TextView) row.findViewById(R.id.textViewReviewContent);
            reviewsLinearLayout.removeView(row);
            author.setText(mReviews.get(i).getAuthor());
            content.setText(mReviews.get(i).getContent());
            reviewsLinearLayout.addView(row);
        }
    }


}

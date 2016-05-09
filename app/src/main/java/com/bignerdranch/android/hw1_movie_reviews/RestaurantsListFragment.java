package com.bignerdranch.android.hw1_movie_reviews;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class RestaurantsListFragment extends Fragment {

    private ArrayAdapter<String> mRestaurantsAdapter;

    public RestaurantsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        //get reviews up on startup
        FetchRestaurantsTask reviewsTask = new FetchRestaurantsTask();
        reviewsTask.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reviews_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchRestaurantsTask reviewsTask = new FetchRestaurantsTask();
            reviewsTask.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] data = {};
        List<String> movieReviews = new ArrayList<String>(Arrays.asList(data));
        mRestaurantsAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_review,
                R.id.list_item_review_textview,
                movieReviews);

        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_reviews);
        listView.setAdapter(mRestaurantsAdapter);

        //set the listener for each list item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String title = mRestaurantsAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), RestaurantActivity.class).putExtra(Intent.EXTRA_TEXT, title);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchRestaurantsTask extends AsyncTask<Void, Void, String[]> {
        private final String LOG_TAG = FetchRestaurantsTask.class.getSimpleName();

        //get the movie data (titles for now) from the jsonStr passed in and popoulate titles
        protected String[] getMovieDataFromJson(String movieReviewsJsonStr) throws JSONException {

            //json keys
            final String NYTMV_RESULTS = "results";
            final String NYTMV_TITLE = "display_title";

            JSONObject reviewJson = new JSONObject(movieReviewsJsonStr);
            JSONArray resultsArray = reviewJson.getJSONArray(NYTMV_RESULTS);
            String[] titleStrs = new String[20];
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject review = resultsArray.getJSONObject(i);
                String title = review.getString(NYTMV_TITLE);
                Log.v(LOG_TAG, "Got title: " + title);
                titleStrs[i] = title;
            }

            return titleStrs;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            System.out.println("Hello");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewJsonStr = null;

            try {
                final String REVIEWS_BASE_URL = "http://api.nytimes.com/svc/movies/v2/reviews/search.json?";
                final String THOUSAND_BEST_PARAM = "thousand-best";
                final String API_KEY_PARAM = "api-key";

                Uri builtUri = Uri.parse(REVIEWS_BASE_URL).buildUpon()
                        .appendQueryParameter(THOUSAND_BEST_PARAM, "Y")
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.NYT_MOVIE_REVIEWS_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream input = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (input == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(input));

                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                reviewJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Movie Review string: " + reviewJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
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
                return getMovieDataFromJson(reviewJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] titles) {
            if (titles != null && titles.length != 0) {
                mRestaurantsAdapter.clear();
                for (String title : titles) {
                    mRestaurantsAdapter.add(title);
                }
            }
        }
    }
}

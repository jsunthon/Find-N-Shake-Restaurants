package com.bignerdranch.android.randomrestaurants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * A placeholder fragment containing a simple view.
 */
public class RestaurantsListFragment extends Fragment {

    //YELP API STUFF
    private static final String API_HOST = "api.yelp.com";
    private static final String DEFAULT_TERM = "restaurants";
    private static String LOCATION = "90706"; //zip code
    private static final int DEFAULT_RADIUS = 10; //in miles
    private static final int SEARCH_LIMIT = 20;
    private static final String SEARCH_PATH = "/v2/search";
    OAuthService service;
    Token accessToken;

    private boolean chineseChecked = false;
    private ArrayAdapter<String> mRestaurantsAdapter;

    public HashMap<String, Integer> categoryFilter = new HashMap<>();

    String[] categories = {
            "japanese", "tradamerican", "chinese",
            "indpak", "pizza", "newamerican",
            "mediterranean", "mexican", "mideastern",
            "french", "thai", "steak", "latin",
            "seafood", "italian", "greek"
    };

    //logging constants
    private final String LOG_TAG_FETCH_TASK = FetchRestaurantsTask.class.getSimpleName();
    private final String LOG_TAG_RESTAURANT_LIST = this.getClass().getSimpleName();

    /**
     * Set up the yelp api oauth credentials.
     */
    public RestaurantsListFragment() {
        this.service = new ServiceBuilder().provider(TwoStepOAuth.class)
                .apiKey(BuildConfig.YELP_CONSUMER_KEY)
                .apiSecret(BuildConfig.YELP_CONSUMER_SECRET).build();
        this.accessToken = new Token(BuildConfig.YELP_TOKEN, BuildConfig.YELP_TOKEN_SECRET);
    }

    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://" + API_HOST + path);
        return request;
    }

    //returns JSON response
    private String sendRequestAndGetResponse(OAuthRequest request) {
        Log.v(LOG_TAG_FETCH_TASK, request.getCompleteUrl());
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    public String searchForRestaurantsByLocation(String term, String location, double miles, String categoryFilter) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        request.addQuerystringParameter("radius_filter", Double.toString(convertMilesToMeters(miles)));
//        categoryFilter = "greek,pizza,newamerican";
        request.addQuerystringParameter("category_filter", categoryFilter);
        return sendRequestAndGetResponse(request);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        //get reviews up on startup
        FetchRestaurantsTask reviewsTask = new FetchRestaurantsTask();
        populateCategoryFilter(categories);
        printFilters(categoryFilter);
        LOCATION = getLocationPref();
        reviewsTask.execute(categoryFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG_RESTAURANT_LIST, "On resume called");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.restaurant_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchRestaurantsTask reviewsTask = new FetchRestaurantsTask();
            populateCategoryFilter(categories); //update
            printFilters(categoryFilter); //verify
            LOCATION = getLocationPref();
            reviewsTask.execute(categoryFilter);
//            validatePrefs(categories);
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
                R.layout.list_item_restaurant,
                R.id.list_item_restaurant_textview,
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

    public class FetchRestaurantsTask extends AsyncTask<Object, Void, String[]> {

        protected String[] getYelpDataFromJson(String yelpDataJsonStr) throws JSONException {

            //json keys
            final String YELP_BUSINESSES = "businesses";
            final String YELP_BUSINESS_NAME = "name";

            JSONObject response = new JSONObject(yelpDataJsonStr);
            JSONArray businesses = response.getJSONArray(YELP_BUSINESSES);
            String[] restaurants = new String[20];
            for (int i = 0; i < businesses.length(); i++) {
                JSONObject business = businesses.getJSONObject(i);
                String restaurantName = business.getString(YELP_BUSINESS_NAME);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant: " + restaurantName);
                restaurants[i] = restaurantName;
            }
            return restaurants;
        }

        @Override
        protected String[] doInBackground(Object... params) {
            HashMap<String, Integer> categoryFilter = (HashMap<String, Integer>) params[0];
            String filterString = parseFilter(categoryFilter);
            System.out.println("filter String:" + filterString);
            String yelpDataJsonStr = searchForRestaurantsByLocation(DEFAULT_TERM, LOCATION, DEFAULT_RADIUS, filterString);
            System.out.println("YELP STR" + yelpDataJsonStr.length());
            String[] yelpRestaurants = null;
            try {
                yelpRestaurants = getYelpDataFromJson(yelpDataJsonStr);
            } catch (JSONException e) {
                Log.v(LOG_TAG_FETCH_TASK, "failed to parse json");
            }
            return yelpRestaurants;
        }

        @Override
        protected void onPostExecute(String[] restaurants) {
            if (restaurants != null && restaurants.length != 0) {
                mRestaurantsAdapter.clear();
                for (String restaurant : restaurants) {
                    mRestaurantsAdapter.add(restaurant);
                }
            }
        }
    }

    // *********************** *HELPER METHODS *************************************//
    public double convertMilesToMeters(double miles) {
        return miles * 1609.344;
    }

    private void validatePrefs(String[] keys) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        for (String key: keys) {
            boolean isChecked = sharedPref.getBoolean(key, false);
            Log.v(LOG_TAG_RESTAURANT_LIST, key + "checked: " + isChecked);
        }
    }

    private void populateCategoryFilter(String[] keys) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        for (String key: keys) {
            boolean isChecked = sharedPref.getBoolean(key, false);
            if (isChecked) {
                categoryFilter.put(key, 1);
            } else {
                categoryFilter.put(key, 0);
            }
        }
    }

    private void printFilters(HashMap<String, Integer> categoryFilter) {
        for (Map.Entry<String, Integer> entry : categoryFilter.entrySet()) {
            Log.d(LOG_TAG_RESTAURANT_LIST, entry.getKey() + ": " + entry.getValue());
        }
    }

    private String parseFilter(HashMap<String, Integer> categoryFilter) {
        String filterString = ""; //start empty
        List<String> filterList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : categoryFilter.entrySet()) {
            if (entry.getValue() == 1) {
                filterList.add(entry.getKey());
            }
        }
        for (String filter: filterList) {
            filterString += filter;
            if (filterList.indexOf(filter) != filterList.size() - 1) {
                filterString += ",";
            }
        }
        return filterString;
    }

    private String getLocationPref() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPref.getString("location", "");
    }
}

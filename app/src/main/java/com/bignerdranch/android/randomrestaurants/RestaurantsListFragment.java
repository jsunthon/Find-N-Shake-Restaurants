package com.bignerdranch.android.randomrestaurants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bignerdranch.android.models.Restaurant;
import com.bignerdranch.android.models.RestaurantLab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    //Shake sensor variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    //Yelp api variables
    private static final String API_HOST = "api.yelp.com";
    private static final String DEFAULT_TERM = "restaurants";
    private static String LOCATION = "90706"; //zip code
    private static final int DEFAULT_SEARCH_RADIUS = 10; //in miles
    private static final int DEFAULT_SEARCH_LIMIT = 20;
    private static final String SORT = "2"; //sort of "2" means we will sort from highest to lowest rated
    private static final String SEARCH_PATH = "/v2/search";
    private OAuthService service;
    private Token accessToken;

    //adapter for the list view
    private RecyclerView mRestaurantRecyclerView;
    private RestaurantAdapter mAdapter; //adapter for the restaurantrecycerview
    private LinearLayout mLinearLayout; //used for the recycler view


    //contain a mapping of categories vs checked , e.g. "chinese : 1" means chinese checked
    public HashMap<String, Integer> categoryFilter = new HashMap<>();

    //an array of all the possible categories in Settings
    String[] categories = {
            "japanese", "tradamerican", "chinese",
            "indpak", "pizza", "newamerican",
            "mediterranean", "mexican", "mideastern",
            "french", "thai", "steak", "latin",
            "seafood", "italian", "greek"
    };

    //Logging constants used to indicate where a piece of code executed
    private final String LOG_TAG_FETCH_TASK = FetchRestaurantsTask.class.getSimpleName();
    private final String LOG_TAG_RESTAURANT_LIST = this.getClass().getSimpleName();

    /**
     * Set up the yelp api oauth credentials in the constructor
     */
    public RestaurantsListFragment() {
        this.service = new ServiceBuilder().provider(TwoStepOAuth.class)
                .apiKey(BuildConfig.YELP_CONSUMER_KEY)
                .apiSecret(BuildConfig.YELP_CONSUMER_SECRET).build();
        this.accessToken = new Token(BuildConfig.YELP_TOKEN, BuildConfig.YELP_TOKEN_SECRET);
    }

    //more yelp api authentication stuff
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://" + API_HOST + path);
        return request;
    }

    //This actually executes the yelp API call with the appropriate authentication values
    private String sendRequestAndGetResponse(OAuthRequest request) {
        Log.v(LOG_TAG_FETCH_TASK, request.getCompleteUrl());
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Construct a search query, then execute it
     *
     * @param term           Search for a term. Can be restaurants, businesses, or any term that Yelp allows
     * @param location       Location within the search should be contained
     * @param miles          Specify a radius in miles within the search should be contained
     * @param categoryFilter Specify which restaurant categories to search. Comma delimited string
     * @return JSON string that represents the YELP API response
     */
    public String searchForRestaurantsByLocation(String term, String location, double miles, String categoryFilter) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("limit", String.valueOf(DEFAULT_SEARCH_LIMIT));
        request.addQuerystringParameter("radius_filter", Double.toString(convertMilesToMeters(miles)));
        request.addQuerystringParameter("category_filter", categoryFilter);
        request.addQuerystringParameter("sort", SORT);
        return sendRequestAndGetResponse(request);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpShake();
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        Log.v(LOG_TAG_RESTAURANT_LIST, "on Create called");
        FetchRestaurantsTask reviewsTask = new FetchRestaurantsTask();
        populateCategoryFilter(categories);
        String categoryFilterString = parseFilter(categoryFilter);
        printFilters(categoryFilter);
        LOCATION = getLocationPref();
        reviewsTask.execute(categoryFilterString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG_RESTAURANT_LIST, "onCreateView called");

        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

        mLinearLayout = (LinearLayout) view.findViewById(R.id.default_linear_layout);
        mRestaurantRecyclerView = (RecyclerView) view.findViewById(R.id.restaurant_recycler_view);
        mRestaurantRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(LOG_TAG_RESTAURANT_LIST, "on Start called");
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        Log.v(LOG_TAG_RESTAURANT_LIST, "On resume called");
    }

    @Override
    public void onPause() {
        //unregister the Sensor manager on pause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
        Log.v(LOG_TAG_RESTAURANT_LIST, "On pause called");
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
            String categoryFilterString = parseFilter(categoryFilter);
            printFilters(categoryFilter); //verify
            LOCATION = getLocationPref();
            reviewsTask.execute(categoryFilterString);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateUI() {
        RestaurantLab restaurantLab = RestaurantLab.get(getActivity());
        List<Restaurant> restaurants = restaurantLab.getRestaurants();

        Log.v(LOG_TAG_RESTAURANT_LIST, "Size: " + restaurants.size());
        mAdapter = new RestaurantAdapter(restaurants);
        mRestaurantRecyclerView.setAdapter(mAdapter);
    }

    private class RestaurantHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mRestaurantNameTextView;

        private Restaurant mRestaurant;

        public RestaurantHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mRestaurantNameTextView = (TextView) itemView.findViewById(R.id.list_item_restaurant_textview);
        }

        public void bindRestaurant(Restaurant restaurant) {
            mRestaurant = restaurant;
            mRestaurantNameTextView.setText(mRestaurant.getName());
        }
        @Override
        public void onClick(View v) {
            Intent intent = RestaurantActivity.newIntent(getActivity(), mRestaurant.getId());
            startActivity(intent);
        }
    }

    private class RestaurantAdapter extends RecyclerView.Adapter<RestaurantHolder> {
        private List<Restaurant> mRestaurants;

        public RestaurantAdapter(List<Restaurant> restaurants) {
            mRestaurants = restaurants;
        }

        @Override
        public RestaurantHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_restaurant, parent, false);
            return new RestaurantHolder(view);
        }

        @Override
        public void onBindViewHolder(RestaurantHolder holder, int position) {
            Restaurant restaurant = mRestaurants.get(position);
            holder.bindRestaurant(restaurant);
        }

        @Override
        public int getItemCount() {
            return mRestaurants.size();
        }
    }

    public class FetchRestaurantsTask extends AsyncTask<Object, Void, Void> {
        private RestaurantLab restaurantLab = RestaurantLab.get(getActivity());

        protected void getYelpDataFromJson(String yelpDataJsonStr) throws JSONException {

            //json keys
            final String YELP_BUSINESSES = "businesses";
            final String YELP_BUSINESS_NAME = "name";

            JSONObject response = new JSONObject(yelpDataJsonStr);
            JSONArray businesses = response.getJSONArray(YELP_BUSINESSES);

            for (int i = 0; i < businesses.length(); i++) {
                JSONObject business = businesses.getJSONObject(i);
                String restaurantName = business.getString(YELP_BUSINESS_NAME);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant: " + restaurantName);
                restaurantLab.addRestaurant(new Restaurant(restaurantName, "123", 5, "nowhere"));
            }
        }

        @Override
        protected Void doInBackground(Object... params) {
            String filterCategories = (String) params[0];
            Log.v(LOG_TAG_FETCH_TASK, "Categories to search: " + filterCategories);
            String yelpDataJsonStr = searchForRestaurantsByLocation(DEFAULT_TERM, LOCATION, DEFAULT_SEARCH_RADIUS, filterCategories);
            Log.v(LOG_TAG_FETCH_TASK, "YELP STR" + yelpDataJsonStr.length());
            restaurantLab.resetRestaurants();
            try {
                getYelpDataFromJson(yelpDataJsonStr);
            } catch (JSONException e) {
                Log.v(LOG_TAG_FETCH_TASK, "failed to parse json");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            updateUI();
        }
    }

    // *********************** *HELPER METHODS *************************************//
    public double convertMilesToMeters(double miles) {
        return miles * 1609.344;
    }

    private void validatePrefs(String[] keys) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        for (String key : keys) {
            boolean isChecked = sharedPref.getBoolean(key, false);
            Log.v(LOG_TAG_RESTAURANT_LIST, key + "checked: " + isChecked);
        }
    }

    private void populateCategoryFilter(String[] keys) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        for (String key : keys) {
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

    /**
     * Generate a non-random set of categories of restaurants
     * that are enabled in settings
     *
     * @param categoryFilter contains key, value pairs specifying which categories are checked
     * @return a string containing the categories to filter, e.g. "french, chinese, mexican"
     */
    private String parseFilter(HashMap<String, Integer> categoryFilter) {
        String filterCategories = ""; //start empty
        List<String> filterList = generateFilterList(categoryFilter);
        if (filterList.size() == 0) {
            return generateRandomFilter();
        }
        for (String filter : filterList) {
            filterCategories += filter;
            if (filterList.indexOf(filter) != filterList.size() - 1) {
                filterCategories += ",";
            }
        }
        return filterCategories;
    }

    /**
     * Generate a random category of restaurants
     * that are enabled in settings
     *
     * @param categoryFilter contains key, value pairs specifying which categories are checked
     * @return a string containing one random category
     */
    private String parseRandomizedFilter(HashMap<String, Integer> categoryFilter) {
        List<String> filterList = generateFilterList(categoryFilter);
        if (filterList.size() == 0) {
            return generateRandomFilter();
        }
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(filterList.size());
        String filterCategory = filterList.get(index);
        return filterCategory;
    }

    /**
     * Generate a list containing the categories that are checked in Settings
     *
     * @param categoryFilter contains key, value pairs specifying which categories are checked
     * @return a list specifying the categories that are checked
     */
    private List<String> generateFilterList(HashMap<String, Integer> categoryFilter) {
        List<String> filterList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryFilter.entrySet()) {
            if (entry.getValue() == 1) {
                filterList.add(entry.getKey());
            }
        }
        return filterList;
    }

    //return a random category to search for. used when the user hasn't indicated any filtering options
    private String generateRandomFilter() {
        Random random = new Random();
        int index = random.nextInt(categories.length);
        String randomCategory = categories[index];
        return randomCategory;
    }

    //Get the value of the location preference
    private String getLocationPref() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPref.getString("location", "");
    }

    //handle shake events
    private void setUpShake() {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                FetchRestaurantsTask reviewsTask = new FetchRestaurantsTask();
                populateCategoryFilter(categories);
                String categoryFilterString = parseRandomizedFilter(categoryFilter);
                LOCATION = getLocationPref();
                reviewsTask.execute(categoryFilterString);
            }
        });
    }
}

package com.bignerdranch.android.randomrestaurants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.bignerdranch.android.models.Restaurant;
import com.bignerdranch.android.models.RestaurantLab;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//Thanks to Big Nerd Ranch Guide
public class RestaurantsListFragment extends Fragment {

    private final String LOG_TAG_FETCH_TASK = FetchRestaurantsTask.class.getSimpleName();
    private final String LOG_TAG_RESTAURANT_LIST = this.getClass().getSimpleName();

    //Yelp api variables
    private static final String API_HOST = "api.yelp.com";
    private static final String SEARCH_TERM = "restaurants";
    private static final String SEARCH_PATH = "/v2/search";
    private static String SEARCH_LOCATION;
    private static String SEARCH_RADIUS;
    private static String SEARCH_LIMIT;
    private static String SEARCH_SORT;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    private OAuthService service;
    private Token accessToken;
    private Animation anim;

    //Shake sensor variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    //adapter for the list view
    private RecyclerView mRestaurantRecyclerView;
    private RestaurantAdapter mAdapter;

    //contain a mapping of categories vs checked , e.g. "chinese : 1" means chinese checked
    private HashMap<String, Integer> categoryFilter;

    //an array of all the possible categories in Settings
    private final String[] categories = {
            "japanese", "tradamerican", "chinese",
            "indpak", "pizza", "newamerican",
            "mediterranean", "mexican", "mideastern",
            "french", "thai", "steak", "latin",
            "seafood", "italian", "greek"
    };

    /**
     * Set up the yelp api oauth credentials in the constructor
     */
    public RestaurantsListFragment() {
        Log.v(LOG_TAG_RESTAURANT_LIST, "fragment constructed");
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
    public String searchForRestaurantsByLocation(String term, String location, String searchLimit, String miles, String categoryFilter, int offset) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("limit", String.valueOf(searchLimit));
        request.addQuerystringParameter("radius_filter", Double.toString(convertMilesToMeters(Double.parseDouble(miles))));
        request.addQuerystringParameter("category_filter", categoryFilter);
        request.addQuerystringParameter("sort", SEARCH_SORT);
        request.addQuerystringParameter("offset", Integer.toString(offset));
        return sendRequestAndGetResponse(request);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpShake();
        setHasOptionsMenu(true);
        int numberOfRestaurants = RestaurantLab.get(getActivity()).getRestaurants().size();
        Log.v(LOG_TAG_RESTAURANT_LIST, "From ON create, res size: " + numberOfRestaurants);
        if (numberOfRestaurants == 0) {
            makeAPICall();
        }
        SharedPreferences sharedPref = getActivity().getSharedPreferences("location_prefs", 0);
        mCurrentLatitude = Double.valueOf(sharedPref.getString("mLatitude", "0"));
        mCurrentLongitude = Double.valueOf(sharedPref.getString("mLongitude", "0"));
        anim = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG_RESTAURANT_LIST, "onCreateView called");

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_restaurant_list, null);
        mRestaurantRecyclerView = (RecyclerView) view.findViewById(R.id.restaurant_recycler_view);
        mRestaurantRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRestaurantRecyclerView
                .addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(getActivity())
                                .colorResId(R.color.orange)
                                .sizeResId(R.dimen.divider)
                                .build());
        retrieveUI();
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

    private void makeAPICall() {
        FetchRestaurantsTask reviewsTask = new FetchRestaurantsTask();
        populateCategoryFilter(categories); //update
        String categoryFilterString = parseRandomizedFilter(categoryFilter);
        updateSearchPrefs();
        reviewsTask.execute(categoryFilterString);
    }

    private void retrieveUI() {
        RestaurantLab restaurantLab = RestaurantLab.get(getActivity());
        List<Restaurant> restaurants = restaurantLab.getRestaurants();
        Log.v(LOG_TAG_RESTAURANT_LIST, "Size: " + restaurants.size());
        mAdapter = new RestaurantAdapter(restaurants);
        mRestaurantRecyclerView.setAdapter(mAdapter);
    }

    private class RestaurantHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mRestaurantNameTextView;
        private TextView mRestaurantCategoryTextView;
        private TextView mRestaurantDistanceTextView;

        private Restaurant mRestaurant;

        public RestaurantHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mRestaurantNameTextView = (TextView) itemView.findViewById(R.id.list_item_restaurant_textview);
            mRestaurantCategoryTextView = (TextView) itemView.findViewById(R.id.restaurant_category_textview);
            mRestaurantDistanceTextView = (TextView) itemView.findViewById(R.id.restaurant_distance_textview);
        }

        public void bindRestaurant(Restaurant restaurant) {
            mRestaurant = restaurant;
            mRestaurantNameTextView.setText(mRestaurant.getName());
            mRestaurantCategoryTextView.setText(mRestaurant.getCategories());

            if (mCurrentLatitude != 0.00 && mCurrentLongitude != 0.00) {
                String restaurantDist = getRestaurantDist(mCurrentLatitude, mCurrentLongitude, mRestaurant.getLatitude(), mRestaurant.getLongitude());
                mRestaurantDistanceTextView.setText(restaurantDist + " miles");
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = RandomRestaurantPagerActivity.newIntent(getActivity(), mRestaurant.getId());
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
            final String YELP_PHONE = "display_phone";
            final String YELP_RATING = "rating";
            final String YELP_LOCATION = "location";
            final String YELP_ADDRESS = "display_address";
            final String YELP_IMG_MAIN = "image_url";
            final String YELP_IMG_SNIPPET = "snippet_image_url";
            final String YELP_IMG_RATING = "rating_img_url_large";
            final String YELP_COORDINATE = "coordinate";
            final String YELP_LATITUDE = "latitude";
            final String YELP_LONGITUDE = "longitude";
            final String YELP_CATEGORIES = "categories";


            JSONObject response = new JSONObject(yelpDataJsonStr);
             JSONArray businesses = response.getJSONArray(YELP_BUSINESSES);

            for (int i = 0; i < businesses.length(); i++) {
                JSONObject business = businesses.getJSONObject(i);
                String restaurantName = business.getString(YELP_BUSINESS_NAME);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant: " + restaurantName);
                String restaurantPhone = "123";
                try {
                    restaurantPhone = business.getString(YELP_PHONE);
                    Log.v(LOG_TAG_FETCH_TASK, "Got restaurant phone: " + restaurantPhone);
                } catch (JSONException e) {
                    Log.e(LOG_TAG_FETCH_TASK, "Restaurant phone # for " + restaurantName + " not available.");
                }
                double restaurantRating = business.getDouble(YELP_RATING);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant rating: " + restaurantRating);
                JSONObject restaurantLocation = business.getJSONObject(YELP_LOCATION);
                JSONArray restaurantAddrComp = restaurantLocation.getJSONArray(YELP_ADDRESS);
                String restaurantAddress = parseAddress(restaurantAddrComp);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant address: " + restaurantAddress);
                JSONObject restaurantCoordinates;
                double restaurantLatitude = 0.00;
                double restaurantLongitude = 0.00;
                try {
                    restaurantCoordinates = restaurantLocation.getJSONObject(YELP_COORDINATE);
                    restaurantLatitude = restaurantCoordinates.getDouble(YELP_LATITUDE);
                    restaurantLongitude = restaurantCoordinates.getDouble(YELP_LONGITUDE);
                    Log.v(LOG_TAG_FETCH_TASK, "Got restaurant latitude " + restaurantLatitude);
                    Log.v(LOG_TAG_FETCH_TASK, "Got restaurant longitude: " + restaurantLongitude);
                } catch (JSONException e) {
                    Log.v(LOG_TAG_FETCH_TASK, "Restaurant has no coordinate location.");
                }
                String imageUrl = business.getString(YELP_IMG_MAIN);
                String snippetImageUrl = business.getString(YELP_IMG_SNIPPET);
                String ratingImgUrl = business.getString(YELP_IMG_RATING);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant img main: " + imageUrl);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant img snip: " + snippetImageUrl);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant img rating: " + ratingImgUrl);

                JSONArray restaurantCategory = business.getJSONArray(YELP_CATEGORIES);
                String restaurantCategories = parseCategories(restaurantCategory);
                Log.v(LOG_TAG_FETCH_TASK, "Got restaurant category: " + restaurantCategories);

                restaurantLab.addRestaurant(new Restaurant(
                        restaurantName, restaurantPhone,
                        restaurantRating, restaurantAddress,
                        imageUrl, snippetImageUrl, ratingImgUrl,
                        restaurantLatitude, restaurantLongitude,
                        restaurantCategories));
            }
        }

        @Override
        protected Void doInBackground(Object... params) {
            String filterCategories = (String) params[0];
            Log.v(LOG_TAG_FETCH_TASK, "Categories to search: " + filterCategories);
            int offset = generateOffset();
            String yelpDataJsonStr = searchForRestaurantsByLocation(SEARCH_TERM, SEARCH_LOCATION, SEARCH_LIMIT, SEARCH_RADIUS, filterCategories, offset);
            Log.v(LOG_TAG_FETCH_TASK, "YELP STR LEN" + yelpDataJsonStr.length());
            Log.v(LOG_TAG_FETCH_TASK, "YELP STR" + yelpDataJsonStr);
            restaurantLab.resetRestaurants();
            try {
                getYelpDataFromJson(yelpDataJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.v(LOG_TAG_FETCH_TASK, "failed to parse json");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            retrieveUI();
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
        categoryFilter = new HashMap<>();
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

    private int generateOffset() {
        Random random = new Random();
        int randLimit;
        int searchRadius = Integer.valueOf(SEARCH_RADIUS);
        if (searchRadius <= 3) {
            randLimit = 2;
        } else if (searchRadius <= 8) {
            randLimit = 6;
        } else {
            randLimit = 10;
        }
        int offset = random.nextInt(randLimit);
        return offset;
    }

    private void updateSearchPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SEARCH_LOCATION = sharedPref.getString("location", "");
        SEARCH_RADIUS = sharedPref.getString("search_radius", "10");
        SEARCH_LIMIT = sharedPref.getString("max_results", "5");
        SEARCH_SORT = sharedPref.getString("sort", "2");
    }

    private String parseAddress(JSONArray jsonAddress) throws JSONException {
        String address = "";
        for (int i = 0; i < jsonAddress.length(); i++) {
            address += jsonAddress.getString(i);
            if (i != jsonAddress.length() - 1) {
                address += ", ";
            }
        }
        return address;
    }

    private String parseCategories(JSONArray jsonCategories) throws JSONException {
        String categories = "";
        for (int i = 0; i < jsonCategories.length(); i++) {
            JSONArray category = jsonCategories.getJSONArray(i);
            categories += category.get(0);
            if (i != jsonCategories.length() - 1) {
                categories += ", ";
            }
        }
        return categories;
    }

    private String getRestaurantDist(double mCurrentLatitude, double mCurrentLongitude, double restaurantLatitude, double restaurantLongitude) {
        double longitudeDelta = Math.abs(Math.toRadians(restaurantLongitude) - Math.toRadians(mCurrentLongitude));
        double mCurrentLatRads = Math.toRadians(mCurrentLatitude);
        double restaurantLatRads = Math.toRadians(restaurantLatitude);
        double centralAngle = Math.acos(Math.sin(mCurrentLatRads) * Math.sin(restaurantLatRads)
                + Math.cos(mCurrentLatRads) * Math.cos(restaurantLatRads) * Math.cos(longitudeDelta));
        double distance = centralAngle * 3961;
        distance = (double) Math.round(distance * 100d) / 100d;
        return String.valueOf(distance);
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
                makeAPICall();
                mRestaurantRecyclerView.startAnimation(anim);
            }
        });
    }
}

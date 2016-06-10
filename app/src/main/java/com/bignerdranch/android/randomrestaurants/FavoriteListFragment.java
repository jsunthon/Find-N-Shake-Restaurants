package com.bignerdranch.android.randomrestaurants;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bignerdranch.android.data.FavoritesDbHelper;
import com.bignerdranch.android.models.Restaurant;
import com.bignerdranch.android.models.RestaurantLab;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import java.util.List;
import java.util.UUID;

public class FavoriteListFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();
    private RecyclerView mFavoriteRestaurantRecyclerView;
    private RestaurantAdapter mAdapter;
    private FavoritesDbHelper db;
    private RestaurantLab restaurantLab = RestaurantLab.get(getActivity());

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        fetchFavsData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_favorite_list, null);
        mFavoriteRestaurantRecyclerView = (RecyclerView) v.findViewById(R.id.favorite_restaurant_recycler_view);
        mFavoriteRestaurantRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFavoriteRestaurantRecyclerView
                .addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(getActivity())
                                .colorResId(R.color.orange)
                                .sizeResId(R.dimen.divider)
                                .build());
        retrieveUI();
        return v;
    }

    private class RestaurantHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mFavoriteRestaurantNameTextView;
        private TextView mFavoriteRestaurantCategoryTextView;

        private Restaurant mRestaurant;

        public RestaurantHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mFavoriteRestaurantNameTextView = (TextView) itemView.findViewById(R.id.list_item_favorite_restaurant_textview);
            mFavoriteRestaurantCategoryTextView = (TextView) itemView.findViewById(R.id.favorite_restaurant_category_textview);
        }

        public void bindRestaurant(Restaurant restaurant) {
            mRestaurant = restaurant;
            mFavoriteRestaurantNameTextView.setText(mRestaurant.getName());
            mFavoriteRestaurantCategoryTextView.setText(mRestaurant.getCategories());

        }

        @Override
        public void onClick(View v) {
            Intent intent = FavoriteRestaurantPagerActivity.newIntent(getActivity(), mRestaurant.getId());
            startActivity(intent);
        }
    }

    private class RestaurantAdapter extends RecyclerView.Adapter<RestaurantHolder> {
        private List<Restaurant> mFavoriteRestaurants;

        public RestaurantAdapter(List<Restaurant> restaurants) {
            mFavoriteRestaurants = restaurants;
        }

        @Override
        public RestaurantHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_favorite_restaurant, parent, false);
            return new RestaurantHolder(view);
        }

        @Override
        public void onBindViewHolder(RestaurantHolder holder, int position) {
            Restaurant restaurant = mFavoriteRestaurants.get(position);
            holder.bindRestaurant(restaurant);
        }

        @Override
        public int getItemCount() {
            return mFavoriteRestaurants.size();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            refreshFavList();
        }
    }

    public void refreshFavList() {
        fetchFavsData();
        retrieveUI();
    }

    private void retrieveUI() {
        List<Restaurant> favoriteRestaurants = restaurantLab.getFavoriteRestaurants();
        mAdapter = new RestaurantAdapter(favoriteRestaurants);
        mFavoriteRestaurantRecyclerView.setAdapter(mAdapter);
    }

    private void fetchFavsData() {
        db = new FavoritesDbHelper(this.getContext());
        Cursor res = db.getAllData();
        RestaurantLab restaurantLab = RestaurantLab.get(getActivity());
        restaurantLab.resetFavoriteRestaurants();
        if(res.getCount() == 0){
            //show message
        } else{
            StringBuffer buffer = new StringBuffer();
            while(res.moveToNext()){
                Restaurant temp = new Restaurant();
                try {
                    temp.setId(UUID.fromString(res.getString(1)));
                    temp.setName(res.getString(2));
                    temp.setPhone(res.getString(3));
                    temp.setRating(Double.parseDouble(res.getString(4)));
                    temp.setAddress(res.getString(5));
                    temp.setImageUrl(res.getString(6));
                    temp.setSnippetImageUrl(res.getString(7));
                    temp.setRatingUrl(res.getString(8));
                    temp.setLatitude(Double.parseDouble(res.getString(9)));
                    temp.setLongitude(Double.parseDouble(res.getString(10)));
                    temp.setCategories(res.getString(11));
                    restaurantLab.addFavoriteRestaurant(temp);
                } catch(Exception e){
                    Log.e(LOG_TAG, "Error");
                }
            }
        }
        res.close();
    }
}

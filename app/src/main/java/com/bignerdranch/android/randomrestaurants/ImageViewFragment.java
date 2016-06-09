package com.bignerdranch.android.randomrestaurants;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

// based off Big Nerd Ranch Guide
public class ImageViewFragment extends DialogFragment {
    private ImageView mFoodImgView;
    private LinearLayout mLinearOuter;

    private static final String ARG_RESTAURANT_TITLE = "res_title";
    private static final String ARG_RESTAURANT_IMG_URL = "res_img_url";

    public static ImageViewFragment newInstance(String restaurantName, String restaurantImgUrl) {
        Bundle args = new Bundle();
        args.putString(ARG_RESTAURANT_TITLE, restaurantName);
        args.putString(ARG_RESTAURANT_IMG_URL, restaurantImgUrl);
        ImageViewFragment imageViewFragment = new ImageViewFragment();
        imageViewFragment.setArguments(args);
        return imageViewFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String restaurantName = getArguments().getString(ARG_RESTAURANT_TITLE);
        String imgUrl = getArguments().getString(ARG_RESTAURANT_IMG_URL);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.view_food, null);

        mFoodImgView = (ImageView) v.findViewById(R.id.food_img_view);

//        http://stackoverflow.com/questions/4668001/android-stretch-image-in-imageview-to-fit-screen
        mFoodImgView.setAdjustViewBounds(true);
        mFoodImgView.setScaleType(ImageView.ScaleType.FIT_XY);
        new DownloadImageTask(mFoodImgView)
                .execute(imgUrl);

        mLinearOuter = (LinearLayout) v.findViewById(R.id.view_food_linear_layout);
        mLinearOuter.removeAllViews();

        return new AlertDialog.Builder(getActivity())
                .setView(mFoodImgView)
                .setTitle(restaurantName)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}

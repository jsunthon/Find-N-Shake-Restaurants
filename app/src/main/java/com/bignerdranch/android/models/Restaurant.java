package com.bignerdranch.android.models;

import java.util.UUID;

/**
 * Created by jsunthon on 5/10/2016.
 */
public class Restaurant {

    private UUID mId;
    private String name;
    private String address;
    private String phone;
    private double rating;
    private String categories;
    private String imageUrl;
    private String snippetImageUrl;
    private String ratingUrl;
    private double latitude;
    private double longitude;

    public Restaurant(String name, String phone, double rating,
                      String address, String imageUrl,
                      String snippetImageUrl, String ratingUrl,
                      double latitude, double longitude) {
        mId = UUID.randomUUID(); //random ID for a restaurant is auto generated
        this.name = name;
        this.phone = phone;
        this.rating = rating;
        this.address = address;
        this.imageUrl = imageUrl;
        this.snippetImageUrl = snippetImageUrl;
        this.ratingUrl = ratingUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getRatingUrl() {
        return ratingUrl;
    }

    public void setRatingUrl(String ratingUrl) {
        this.ratingUrl = ratingUrl;
    }

    public String getSnippetImageUrl() {
        return snippetImageUrl;
    }

    public void setSnippetImageUrl(String snippetImageUrl) {
        this.snippetImageUrl = snippetImageUrl;
    }
}

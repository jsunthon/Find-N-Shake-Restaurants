package com.bignerdranch.android.randomrestaurants;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

/**
 * Credits go to https://github.com/Yelp/yelp-api/blob/master/v2/java/TwoStepOAuth.java
 */
public class TwoStepOAuth extends DefaultApi10a {

    @Override
    public String getAccessTokenEndpoint() {
        return null;
    }

    @Override
    public String getAuthorizationUrl(Token arg0) {
        return null;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return null;
    }
}
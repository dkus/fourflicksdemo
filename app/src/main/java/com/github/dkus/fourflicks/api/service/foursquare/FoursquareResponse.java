package com.github.dkus.fourflicks.api.service.foursquare;

import com.github.dkus.fourflicks.api.model.Venue;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class FoursquareResponse {

    @SerializedName("meta")
    private Meta mMeta;

    @SerializedName("response")
    private Response mResponse;

    public Meta getMeta() {
        return mMeta;
    }

    public Response getResponse() {
        return mResponse;
    }

    @Override
    public String toString() {
        return "meta="+mMeta+", response="+mResponse;
    }

    public static class Meta {

        @SerializedName("code")
        private String mCode;

        public String getCode() {
            return mCode;
        }

        @Override
        public String toString() {
            return "Meta [code="+mCode+"]";
        }
    }

    public static class Response {

        @SerializedName("venues")
        private List<Venue> mVenues;

        public List<Venue> getVenues() {
            return mVenues;
        }

        @Override
        public String toString() {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Response [");
            if (mVenues!=null) {
                for (Venue venue : mVenues) {
                    stringBuilder.append(venue);
                }
            }
            stringBuilder.append("]");

            return stringBuilder.toString();
        }
    }

}

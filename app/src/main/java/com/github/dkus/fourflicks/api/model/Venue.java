package com.github.dkus.fourflicks.api.model;

import com.google.gson.annotations.SerializedName;

import com.github.dkus.fourflicks.util.Utils;


public class Venue {

    @SerializedName("id")
    private String mId;

    @SerializedName("name")
    private String mName;

    @SerializedName("location")
    private Location mLocation;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }


    @Override
    public String toString() {
        return "[id="+mId+", name="+mName+", location="+mLocation+"]";
    }

    public static class Location {

        @SerializedName("address")
        private String mAddress;

        @SerializedName("lat")
        private double mLat;

        @SerializedName("lng")
        private double mLng;

        public String getAddress() {
            return mAddress;
        }

        public void setAddress(String address) {
            mAddress = address;
        }

        public double getLat() {
            return mLat;
        }

        public void setLat(double lat) {
            mLat = lat;
        }

        public double getLng() {
            return mLng;
        }

        public void setLng(double lng) {
            mLng = lng;
        }

        @Override
        public String toString() {
            return "[address="+mAddress+", lat="+mLat+", lng="+mLng+"]";
        }

        @Override
        public boolean equals(Object o) {

            return !(o==null || !(o instanceof Location)) &&
                    Utils.areEquals(mAddress, ((Location)o).getAddress()) &&
                    Utils.areEquals(mLat, ((Location)o).getLat()) &&
                    Utils.areEquals(mLng, ((Location) o).getLng());
        }
    }

    @Override
    public boolean equals(Object o) {

        return !(o==null || !(o instanceof Venue)) &&
                Utils.areEquals(mId, ((Venue)o).getId()) &&
                Utils.areEquals(mName, ((Venue)o).getName()) &&
                Utils.areEquals(mLocation, ((Venue) o).getLocation());
    }

}

package com.github.dkus.fourflicks.api.service.foursquare;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;


public interface FoursquareService {

    @GET("/v2/venues/search")
    public void getNearbyObjects(@Query("ll")String location,
                                 @Query("radius") String radius,
                                 Callback<FoursquareResponse> callback);

}

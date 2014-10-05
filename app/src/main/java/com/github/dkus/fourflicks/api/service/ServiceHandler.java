package com.github.dkus.fourflicks.api.service;

import com.github.dkus.fourflicks.api.service.foursquare.FoursquareService;
import com.github.dkus.fourflicks.util.Logger;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;


public class ServiceHandler {

    private FoursquareService mFoursquareService;

    public FoursquareService getFoursquareService() {

        if (mFoursquareService!=null) return mFoursquareService;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.foursquare.com")
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        requestFacade.addQueryParam("client_id",
                                "BDOHE1LDESITK4DM022HYTK1RRPCOPYMKY4POPWYQ21KUTSI");
                        requestFacade.addQueryParam("client_secret",
                                "ALKNAKOZK5K1GD3V0NUORZOXNYCN3FMHNNFYOCBUTPYQMDSQ");
                        requestFacade.addQueryParam("intent", "browse");
                        requestFacade.addQueryParam("v", "20130815");
                    }
                })
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String s) {
                        Logger.log(s, FoursquareService.class);
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        return mFoursquareService=restAdapter.create(FoursquareService.class);

    }

}

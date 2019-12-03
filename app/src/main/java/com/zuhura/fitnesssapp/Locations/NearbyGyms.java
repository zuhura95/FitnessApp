package com.zuhura.fitnesssapp.Locations;

import android.content.Context;

import java.util.List;

public class NearbyGyms {

    private Context context;
    private List<Result> results;
    Result result;


    public NearbyGyms(Context applicationContext, List<Result> results) {
        this.context = applicationContext;
        this.results = results;
    }


    public String getLocationName(int i){

        while(results.size()>i) {
            result = results.get(i);
            String name = result.getName();
            return name;

        }
        return null;
    }
}

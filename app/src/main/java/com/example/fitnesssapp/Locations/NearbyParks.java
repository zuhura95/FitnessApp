package com.example.fitnesssapp.Locations;

import android.content.Context;

import java.util.List;

public class NearbyParks {
    private Context context;
    private List<Result> results;
    Result result;

    public NearbyParks(Context applicationContext, List<Result> results) {
        this.context = applicationContext;
        this.results = results;
    }

    public String getLocationName(int count) {
        while(results.size()>count) {
            result = results.get(count);
            String name = result.getName();
            return name;

        }
        return null;
    }
}

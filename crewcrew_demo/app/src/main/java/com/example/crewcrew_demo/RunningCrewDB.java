package com.example.crewcrew_demo;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RunningCrewDB {
    String name;
    String level;
    String memberNumber;
    String time;
    ArrayList<LatLng> path = new ArrayList<LatLng>();

    public RunningCrewDB(){};
    public RunningCrewDB(String name, String level, String memberNumber, String time){
        this.name = name;
        this.level = level;
        this.memberNumber = memberNumber;
        this.time = time;
    }
}

package com.example.crewcrew_demo;

import android.view.View;
import android.widget.Button;

public class ListViewItem {
    private String crewName;
    private String crewLevel;
    private String crewNum;
    private String crewRuntime;

    public void setCrewName(String name){
        crewName = name;
    }
    public void setCrewLevel(String level){
        crewLevel = level;
    }
    public void setCrewNum(String num){
        crewNum = num;
    }
    public void setCrewRuntime(String runtime){
        crewRuntime = runtime;
    }

    public String getCrewName(){
        return this.crewName;
    }
    public String getCrewLevel(){
        return this.crewLevel;
    }
    public String getCrewNum(){
        return this.crewNum;
    }
    public String getCrewRuntime(){
        return this.crewRuntime;
    }
}

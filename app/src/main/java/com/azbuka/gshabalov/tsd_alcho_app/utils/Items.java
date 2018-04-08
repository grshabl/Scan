package com.azbuka.gshabalov.tsd_alcho_app.utils;



public class Items {
    public String item1;
    public String item2;
    private String lpb = "";
    public Items(String it, String it2){
        this.item1 = it;
        this.item2 = it2;

    }

    public void setLpb(String lpb) {
        this.lpb = lpb;
    }

    public String getLpb() {
        return lpb;
    }
}

package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Aqi {
    public AQICity aqiCity;
    public class AQICity
    {
        public String aqi;
        public String pm25;
    }
}

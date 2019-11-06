package com.weather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public String status;

    public com.weather.android.gson.Basic basic;

    public com.weather.android.gson.AQI aqi;

    public com.weather.android.gson.Now now;

    public com.weather.android.gson.Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<com.weather.android.gson.Forecast> forecastList;

}

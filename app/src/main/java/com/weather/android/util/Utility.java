package com.weather.android.util;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import com.weather.android.db.City;
import com.weather.android.db.County;
import com.weather.android.db.Province;
import com.weather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.PendingIntent.getActivity;
import static org.litepal.LitePalApplication.getContext;

public class Utility {
  private static ProgressDialog progressDialog;
  /**
   * 解析和处理服务器返回的省级数据
   */
  public static boolean handleProvinceResponse(String response) {
    if (!TextUtils.isEmpty(response)) {
      try {
        JSONArray allProvinces = new JSONArray(response);
        for (int i = 0; i < allProvinces.length(); i++) {
          JSONObject provinceObject = allProvinces.getJSONObject(i);
          Province province = new Province();
          province.setProvinceName(provinceObject.getString("name"));
          province.setProvinceCode(provinceObject.getInt("id"));
          province.save();
        }
        return true;
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  /**
   * 解析和处理服务器返回的市级数据
   */
  public static boolean handleCityResponse(String response, int provinceId) {
    if (!TextUtils.isEmpty(response)) {
      try {
        JSONArray allCities = new JSONArray(response);
        for (int i = 0; i < allCities.length(); i++) {
          JSONObject cityObject = allCities.getJSONObject(i);
          City city = new City();
          city.setCityName(cityObject.getString("name"));
          city.setCityCode(cityObject.getInt("id"));
          city.setProvinceId(provinceId);
          city.save();
        }
        return true;
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  /**
   * 解析和处理服务器返回的县级数据
   */
  public static boolean handleCountyResponse(String response, int cityId) {
    if (!TextUtils.isEmpty(response)) {
      try {
        JSONArray allCounties = new JSONArray(response);
        for (int i = 0; i < allCounties.length(); i++) {
          JSONObject countyObject = allCounties.getJSONObject(i);
          County county = new County();
          county.setCountyName(countyObject.getString("name"));
          county.setWeatherId(countyObject.getString("weather_id"));
          county.setCityId(cityId);
          county.save();
        }
        return true;
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  /**
   * 将返回的JSON数据解析成Weather实体类
   */
  public static Weather handleWeatherResponse(String response) {
    try {
      JSONObject jsonObject = new JSONObject(response);
      JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
      String weatherContent = jsonArray.getJSONObject(0).toString();
      return new Gson().fromJson(weatherContent, Weather.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 检查获取位置权限
   */
  public static boolean checkLocationPermission(Activity activity) {
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
   *
   * @param
   * @return true 表示开启
   */
  public static final boolean isGPSOPen(final Context context) {
    LocationManager locationManager
        = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
    boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
    boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    if (gps || network) {
      return true;
    }
    return false;
  }

}

package com.weather.android.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

import com.weather.android.BuildConfig;
import com.weather.android.R;
import com.weather.android.customview.SharePopupWindow;
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

  public static void rotateImageView(Animation animation, View view) {
    animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    animation.setRepeatCount(Animation.INFINITE);
    animation.setDuration(2000);
    animation.setInterpolator(new LinearInterpolator());
    view.startAnimation(animation);
  }
  public static void shareTo(View l_root, View l_cut) {
    Uri shareContent=getCutImageUri(l_cut);
    Log.d("lpl", "URI: "+shareContent.toString());
    SharePopupWindow spw = new SharePopupWindow(getContext(), shareContent);
    // 显示窗口
    spw.showAtLocation(l_root, Gravity.BOTTOM, 0, 0);
  }

  private static Uri getCutImageUri(View view) {
    String imagePath;
    Uri contentUri;
    File file = null;
    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    view.draw(canvas);
    if (bitmap != null) {
      try {
        // 获取内置SD卡路径
        String sdCardPath = Environment.getExternalStorageDirectory().getPath();
        // 图片文件路径
        imagePath = sdCardPath + File.separator + Calendar.getInstance().getTimeInMillis()+".png";
        Log.d("lpl", "path: "+imagePath);
         file = new File(imagePath);
        if(file.exists()){
          file.mkdirs();
        }else{
          file.createNewFile();
        }
        FileOutputStream os = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        os.flush();
        os.close();
      } catch (Exception e) {
        Log.d("lpl", "getCutImageUri: "+"没有");
        e.printStackTrace();
      }
    }
    //判断是否是AndroidN以及更高的版本
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      contentUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
    }else{
      contentUri=Uri.fromFile(file);
    }
    return contentUri;
  }
  public static void verifyStoragePermissions(Activity context) {
    String[] SdCardPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    String[] READ_EXTERNAL_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
    String[] WRITE_EXTERNAL_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    if (ContextCompat.checkSelfPermission(context, SdCardPermission[0]) != PackageManager.PERMISSION_GRANTED) {
      // 如果没有授予该权限，就去提示用户请求
      ActivityCompat.requestPermissions( context, SdCardPermission, 100);
    }
    if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
      // 如果没有授予该权限，就去提示用户请求
      ActivityCompat.requestPermissions( context, READ_EXTERNAL_STORAGE, 500);
    }

    if (ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
      // 如果没有授予该权限，就去提示用户请求
      ActivityCompat.requestPermissions( context, WRITE_EXTERNAL_STORAGE, 600);
    }
  }
}

package com.weather.android;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.weather.android.service.LocationService;

import static com.weather.android.util.Utility.checkLocationPermission;
import static com.weather.android.util.Utility.isGPSOPen;
import static com.weather.android.util.Utility.rotateImageView;

public class MainActivity extends AppCompatActivity {
  private LocationService locationService;
  private static final int LOCATION_CODE = 1;
  private View loadinglayout;
  private Animation animation;
  private ImageView loading;
  private String mProvince;
  private String mCity;
  private String mCounty;
  private BDLocationListener mListener = new BDLocationListener() {
    @Override
    public void onReceiveLocation(BDLocation location) {
      mProvince = location.getProvince();
      mCity = location.getCity();
      mCounty = location.getDistrict();
      Log.d("TAg", "onReceiveLocation: " + mProvince + mCity + mCounty);
      if (!"".equals(mCounty) && locationService != null) {
        locationService.stop();
        Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
        intent.putExtra("provinceName", mProvince);
        intent.putExtra("cityName", mCity);
        intent.putExtra("countyName", mCounty);
        startActivity(intent);
//        loading.clearAnimation();
//        loadinglayout.setVisibility(View.GONE);
        finish();
      } else {
        loading.clearAnimation();
        loadinglayout.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this, "定位失败！", Toast.LENGTH_SHORT).show();
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    loadinglayout = findViewById(R.id.loadinglayout);
    loading = findViewById(R.id.loading);
    locationService = new LocationService(getApplicationContext());
    locationService.registerListener(mListener);
    //注册监听
    locationService.setLocationOption(locationService.getDefaultLocationClientOption());
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (prefs.getString("weather", null) != null) {
      Intent intent = new Intent(this, WeatherActivity.class);
      startActivity(intent);
      finish();
    } else {
      if (!checkLocationPermission(this)) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
      } else {
        if (isGPSOPen(MainActivity.this)) {
          locationService.start();
          loadinglayout.setVisibility(View.VISIBLE);
          loadinglayout.bringToFront();
          rotateImageView(animation, loading);
        } else {
          Intent intent = new Intent();
          intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
          startActivityForResult(intent, 1315);
        }
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1315) {
      if (isGPSOPen(this)) {
        locationService.start();
        loadinglayout.setVisibility(View.VISIBLE);
        loadinglayout.bringToFront();
        rotateImageView(animation, loading);
      } else {
        Toast.makeText(this, "GPS未开启！请手动选择城市", Toast.LENGTH_LONG).show();
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case LOCATION_CODE: {
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          if (isGPSOPen(this)) {
            locationService.start();
            loadinglayout.setVisibility(View.VISIBLE);
            loadinglayout.bringToFront();
            rotateImageView(animation, loading);
          } else {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
          }
        } else {
          // 权限被用户拒绝了。
          Toast.makeText(MainActivity.this, "定位权限被禁止，相关地图功能无法使用！", Toast.LENGTH_LONG).show();
        }
      }
    }
  }
}
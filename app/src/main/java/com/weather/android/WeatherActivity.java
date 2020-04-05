package com.weather.android;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.bumptech.glide.Glide;
import com.weather.android.db.City;
import com.weather.android.db.County;
import com.weather.android.db.Province;
import com.weather.android.gson.Forecast;
import com.weather.android.gson.Weather;
import com.weather.android.service.AutoUpdateService;
import com.weather.android.service.LocationService;
import com.weather.android.util.HttpUtil;
import com.weather.android.util.Utility;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.litepal.crud.DataSupport;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.weather.android.util.DataUtil.appendZero;
import static com.weather.android.util.Utility.checkLocationPermission;
import static com.weather.android.util.Utility.isGPSOPen;
import static com.weather.android.util.Utility.rotateImageView;
import static org.litepal.LitePalApplication.getContext;

public class WeatherActivity extends AppCompatActivity {

  public DrawerLayout drawerLayout;
  public SwipeRefreshLayout swipeRefresh;
  private SharedPreferences schedulePreferences;
  private ScrollView weatherLayout;
  private Button navButton;
  private TextView titleCity;
  private TextView schedule;
  private TextView titleUpdateTime;
  private TextView degreeText;
  private TextView weatherInfoText;
  private LinearLayout forecastLayout;
  private TextView aqiText;
  private TextView pm25Text;
  private TextView comfortText;
  private TextView carWashText;
  private TextView sportText;
  private TextView scheduleInfo;
  private ImageView bingPicImg;
  private ImageView fixLocation;
  private ImageView loading;
  private View loadinglayout;
  private Map<String, String> map;
  private String mWeatherId;
  /**
   * 省列表
   */
  private List<Province> provinceList;

  /**
   * 市列表
   */
  private List<City> cityList;
  /**
   * 定位的省份
   */
  private Province selectedProvince;
  private City selectedCity;
  private List<County> countyList;
  private String mprovince;
  private String mcity;
  private String mcounty;
  private volatile boolean flag = false;
  private LocationService locationService;
  private ProgressDialog progressDialog;
  private Animation animation;
  private static final int LOCATION_CODE = 1;
  private BDLocationListener mListener = new BDLocationListener() {
    @Override
    public void onReceiveLocation(BDLocation location) {
      mprovince = location.getProvince();
      mcity = location.getCity();
      mcounty = location.getDistrict();
      Log.d("TAg", "onReceiveLocation: " + mprovince + mcity + mcounty);
      if (!"".equals(mcounty) && locationService != null) {
        locationService.stop();
        getWeatherID();
        requestWeather(mWeatherId);
        loading.clearAnimation();
        loadinglayout.setVisibility(View.GONE);
      } else {
        loading.clearAnimation();
        loadinglayout.setVisibility(View.GONE);
        Toast.makeText(WeatherActivity.this, "定位失败！", Toast.LENGTH_SHORT).show();
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (animation != null) {
      loading.clearAnimation();
      loadinglayout.setVisibility(View.GONE);
    }
    if (Build.VERSION.SDK_INT >= 21) {
      View decorView = getWindow().getDecorView();
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
    setContentView(R.layout.activity_weather);
    // 初始化各控件
    bingPicImg = findViewById(R.id.bing_pic_img);
    weatherLayout = findViewById(R.id.weather_layout);
    titleCity = findViewById(R.id.title_city);
    schedule = findViewById(R.id.schedule);
    fixLocation = findViewById(R.id.fix_location);
    titleUpdateTime = findViewById(R.id.title_update_time);
    degreeText = findViewById(R.id.degree_text);
    weatherInfoText = findViewById(R.id.weather_info_text);
    forecastLayout = findViewById(R.id.forecast_layout);
    aqiText = findViewById(R.id.aqi_text);
    pm25Text = findViewById(R.id.pm25_text);
    comfortText = findViewById(R.id.comfort_text);
    carWashText = findViewById(R.id.car_wash_text);
    sportText = findViewById(R.id.sport_text);
    swipeRefresh = findViewById(R.id.swipe_refresh);
    swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
    drawerLayout = findViewById(R.id.drawer_layout);
    loadinglayout = findViewById(R.id.loadinglayout);
    loading = findViewById(R.id.loading);
    navButton = findViewById(R.id.nav_button);
    scheduleInfo = findViewById(R.id.schedule_info);
    locationService = new LocationService(getApplicationContext());
    locationService.registerListener(mListener);
    //注册监听
    locationService.setLocationOption(locationService.getDefaultLocationClientOption());
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String weatherString = prefs.getString("weather", null);
    if (weatherString != null) {
      // 有缓存时直接解析天气数据
      Weather weather = Utility.handleWeatherResponse(weatherString);
      mWeatherId = weather.basic.weatherId;
      showWeatherInfo(weather);
    } else {
      // 无缓存时去服务器查询天气
      mWeatherId = getIntent().getStringExtra("weather_id");
      Log.d("tag", "onCreate: " + mWeatherId);
      if (mWeatherId == null || "".equals(mWeatherId)) {
        mprovince = getIntent().getStringExtra("provinceName");
        mcity = getIntent().getStringExtra("cityName");
        mcounty = getIntent().getStringExtra("countyName");
        getWeatherID();
        Log.d("TAG", "传过来：" + mprovince + mcity + mcounty + mWeatherId);
      }
      weatherLayout.setVisibility(View.INVISIBLE);
      requestWeather(mWeatherId);
    }
    swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        requestWeather(mWeatherId);
      }
    });
    navButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        drawerLayout.openDrawer(GravityCompat.START);
      }
    });
    fixLocation.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!checkLocationPermission(WeatherActivity.this)) {
          ActivityCompat.requestPermissions(WeatherActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
        } else {
          if (isGPSOPen(WeatherActivity.this)) {
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
    });
    schedule.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(WeatherActivity.this, scheduleActivity.class);
        intent.putExtra("currentCounty", titleCity.getText().toString());
        startActivity(intent);
      }
    });
    String bingPic = prefs.getString("bing_pic", null);
    if (bingPic != null) {
      Glide.with(this).load(bingPic).into(bingPicImg);
    } else {
      loadBingPic();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    scheduleInfoIsShow();
  }

  private void scheduleInfoIsShow() {
    Calendar calendar = Calendar.getInstance();
    String currentdata = calendar.get(Calendar.YEAR) + appendZero(calendar.get(Calendar.MONTH) + 1) + appendZero(calendar.get(Calendar.DAY_OF_MONTH));
    schedulePreferences = getSharedPreferences("schedule", MODE_PRIVATE);
    map = (Map<String, String>) schedulePreferences.getAll();
    if (map.containsKey(currentdata) && !map.get(currentdata).equals("其它")) {
      scheduleInfo.setVisibility(View.VISIBLE);
      scheduleInfo.setText("亲，别忘了您今天的\"" + map.get(currentdata) + "\"计划哦！");
    } else {
      scheduleInfo.setVisibility(View.GONE);
    }
  }

  /**
   * 根据获取的位置得到weatherID
   */
  private void getWeatherID() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        provinceList = DataSupport.findAll(Province.class);
        for (Province province : provinceList) {
          if (mprovince.indexOf(province.getProvinceName()) != -1) {
            selectedProvince = province;
            break;
          }
        }
        String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
        queryFromServer(address, "city");
        while (flag != true) {
        }
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        Log.d("TAG", cityList.size() + "  " + provinceList.size() + selectedProvince.getProvinceName());
        for (City city : cityList) {
          if (mcity.indexOf(city.getCityName()) != -1) {
            selectedCity = city;
            break;
          }
        }
        address = address + "/" + selectedCity.getCityCode();
        flag = false;
        queryFromServer(address, "county");
        while (flag != true) {
        }
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        for (County county : countyList) {
          if (mcounty.indexOf(county.getCountyName()) != -1) {
            mWeatherId = county.getWeatherId();
            break;
          }
        }
      }
    });
  }

  /**
   * 根据传入的地址和类型从服务器上查询省市县数据。
   */
  private void queryFromServer(String address, final String type) {
    HttpUtil.sendOkHttpRequest(address, new Callback() {
      @Override
      public void onResponse(Call call, Response response) throws IOException {
        String responseText = response.body().string();
        if ("city".equals(type)) {
          Utility.handleCityResponse(responseText, selectedProvince.getId());
          flag = true;
        } else if ("county".equals(type)) {
          Utility.handleCountyResponse(responseText, selectedCity.getId());
          flag = true;
        }
      }

      @Override
      public void onFailure(Call call, IOException e) {
        // 通过runOnUiThread()方法回到主线程处理逻辑
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
  }

  /**
   * 根据天气id请求城市天气信息。
   */
  public void requestWeather(final String weatherId) {
    String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
    HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
      @Override
      public void onResponse(Call call, Response response) throws IOException {
        final String responseText = response.body().string();
        final Weather weather = Utility.handleWeatherResponse(responseText);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (weather != null && "ok".equals(weather.status)) {
              SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
              editor.putString("weather", responseText);
              editor.apply();
              mWeatherId = weather.basic.weatherId;
              showWeatherInfo(weather);
            } else {
              Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
            }
            swipeRefresh.setRefreshing(false);
          }
        });
      }

      @Override
      public void onFailure(Call call, IOException e) {
        e.printStackTrace();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
          }
        });
      }
    });
    loadBingPic();
  }

  /**
   * 加载必应每日一图
   */
  private void loadBingPic() {
    String requestBingPic = "http://guolin.tech/api/bing_pic";
    HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
      @Override
      public void onResponse(Call call, Response response) throws IOException {
        final String bingPic = response.body().string();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putString("bing_pic", bingPic);
        editor.apply();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
          }
        });
      }

      @Override
      public void onFailure(Call call, IOException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * 处理并展示Weather实体类中的数据。
   */
  private void showWeatherInfo(Weather weather) {
    String cityName = weather.basic.cityName;
    String updateTime = weather.basic.update.updateTime.split(" ")[1];
    String degree = weather.now.temperature + "℃";
    String weatherInfo = weather.now.more.info;
    titleCity.setText(cityName);
    titleUpdateTime.setText(updateTime);
    degreeText.setText(degree);
    weatherInfoText.setText(weatherInfo);
    forecastLayout.removeAllViews();
    for (Forecast forecast : weather.forecastList) {
      View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
      TextView dateText = view.findViewById(R.id.date_text);
      TextView infoText = view.findViewById(R.id.info_text);
      TextView maxText = view.findViewById(R.id.max_text);
      TextView minText = view.findViewById(R.id.min_text);
      dateText.setText(forecast.date);
      infoText.setText(forecast.more.info);
      maxText.setText(forecast.temperature.max);
      minText.setText(forecast.temperature.min);
      forecastLayout.addView(view);
    }
    if (weather.aqi != null) {
      aqiText.setText(weather.aqi.city.aqi);
      pm25Text.setText(weather.aqi.city.pm25);
    }
    String comfort = "舒适度：" + weather.suggestion.comfort.info;
    String carWash = "洗车指数：" + weather.suggestion.carWash.info;
    String sport = "运行建议：" + weather.suggestion.sport.info;
    comfortText.setText(comfort);
    carWashText.setText(carWash);
    sportText.setText(sport);
    weatherLayout.setVisibility(View.VISIBLE);
    Intent intent = new Intent(this, AutoUpdateService.class);
    startService(intent);
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
          Toast.makeText(WeatherActivity.this, "定位权限被禁止，相关地图功能无法使用！", Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  /**
   * 显示进度对话框
   */
  private void showProgressDialog() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getContext());
      progressDialog.setMessage("正在定位并获取天气信息...");
      progressDialog.setCanceledOnTouchOutside(false);
    }
    progressDialog.show();
  }

  /**
   * 关闭进度对话框
   */
  private void closeProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
    }
  }
}

package com.weather.android;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weather.android.customview.SpiderView;
import com.weather.android.util.HttpUtil;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class weatherhistoryActivity extends AppCompatActivity {
  private TextView historydata,history_title,describe;
  private Button opencitylist;
  private String weather_item[] = {"晴","多云","阴", "雨","雪","其它"};
  private final int Level=6,Item=6;
  private int datalevel[] = new int[6];
  private int datanumber[] = new int[6];
  private String countyName;
  private volatile boolean flag=false;
  private ProgressDialog progressDialog;
  public DrawerLayout drawerLayout;
  public SwipeRefreshLayout viewRefresh;
  private String address;


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weatherhistory);
    opencitylist=findViewById(R.id.citylist);
    opencitylist.setVisibility(View.VISIBLE);
    historydata=findViewById(R.id.summary_history);
    history_title=findViewById(R.id.title_summary);
    describe=findViewById(R.id.describe);
    drawerLayout=findViewById(R.id.drawer_city_layout);
    viewRefresh=findViewById(R.id.view_refresh);
    history_title.setText("天气回顾");
    describe.setText(R.string.wh_level_Introduction);
    opencitylist.setOnClickListener(new onClickLisenter());
    countyName=getIntent().getStringExtra("currentCounty");
    Toast.makeText(this, countyName, Toast.LENGTH_LONG).show();
    address="http://www.tianqihoubao.com/weather/top/mentougou.html";
    viewRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        gethistoryweather(address,countyName);
      }
    });
    gethistoryweather(address,countyName);
  }

  public void gethistoryweather(String address,String countyname) {
    countyName=countyname;
    flag=false;
    for(int i=0;i<Item;i++){
      datalevel[i]=0;
      datanumber[i]=0;
    }
    HttpUtil.sendOkHttpRequest(address, new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            closeProgressDialog();
            Toast.makeText(weatherhistoryActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
          }
        });
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        String responseText = response.body().string();
        String weather;
        Document doc= Jsoup.parse(responseText);
        Elements rows=doc.select("table[class=b]").get(0).select("tr");
        Log.d("lpl", "onResponse: "+rows.size());
        if(rows.size()>1){
          for(int i=2;i<rows.size();i++)
          {
            Element row = rows.get(i);
            weather=row.select("td").get(2).text();
            if(weather.indexOf(weather_item[0])!=-1){
              datanumber[0]++;
            }else if(weather.indexOf(weather_item[1])!=-1){
              datanumber[1]++;
            }else if(weather.indexOf(weather_item[2])!=-1){
              datanumber[2]++;
            }else if(weather.indexOf(weather_item[3])!=-1){
              datanumber[3]++;
            }else if(weather.indexOf(weather_item[4])!=-1){
              datanumber[4]++;
            }else{
              datanumber[5]++;
            }
            System.out.println("天气:" + row.select("td").get(2).text());
          }
        }
        flag=true;
        Log.d("lpl", "onResponse: "+datanumber[0]);
      }
    });
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        showProgressDialog();
        while(!flag){}
      }
    });
    closeProgressDialog();
    for (int i = 0; i < Item; i++) {
      if (datanumber[i] > 15) {
        datalevel[i] = 6;
      } else if (datanumber[i] >= 10) {
        datalevel[i] = 5;
      } else if (datalevel[i] >= 7) {
        datalevel[i] = 4;
      } else if (datanumber[i] >= 4) {
        datalevel[i] = 3;
      } else if (datanumber[i] >= 1) {
        datalevel[i] = 2;
      } else {
        datalevel[i] = 1;
      }
    }
    LinearLayout root = findViewById(R.id.summary_layout);
    root.removeAllViews();
    SpiderView spiderView = new SpiderView(this, Item, Level, datalevel);
    spiderView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    root.addView(spiderView);
    historydata = findViewById(R.id.summary_history);
    String schdata = "";
    for (int i = 0, j = 1; i < Item; i++, j++) {
      schdata += j + "." + weather_item[i] + ": " + datanumber[i] + "天\n";
    }
    history_title.setText("天气回顾"+"（"+countyname+")");
    historydata.setText(schdata);
    viewRefresh.setRefreshing(false);
  }
  private void showProgressDialog() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(this);
      progressDialog.setMessage("正在加载...");
      progressDialog.setCanceledOnTouchOutside(false);
    }
    progressDialog.show();
  }
  private void closeProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
    }
  }

  private class onClickLisenter implements View.OnClickListener {

    @Override
    public void onClick(View v) {
      if(v.getId()==R.id.citylist){
        drawerLayout.openDrawer(GravityCompat.START);
      }
    }
  }
}

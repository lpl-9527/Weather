package com.weather.android;

import java.util.Calendar;
import java.util.Map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weather.android.customview.SpiderView;

import static com.weather.android.util.DataUtil.appendZero;

public class summaryActivity extends AppCompatActivity {
  private String schedule_item[] = {"运动", "购物", "拜访", "旅游", "学习", "聚会", "活动", "其它"};
  private int datalevel[] = new int[8];
  private int datanumber[] = new int[8];
  private final int Level=6,Item=8;
  private TextView scheduledata,schedlue_title,describe;
  private Calendar calendar;
  private String currentdata;
  private SharedPreferences preferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_summary);
    schedlue_title=findViewById(R.id.title_summary);
    schedlue_title.setText("日程回顾");
    describe=findViewById(R.id.describe);
    describe.setText(R.string.sc_level_Introduction);
    preferences = getSharedPreferences("schedule", MODE_PRIVATE);
    calendar = Calendar.getInstance();
    currentdata = calendar.get(Calendar.YEAR) + appendZero(calendar.get(Calendar.MONTH) + 1) + appendZero(calendar.get(Calendar.DAY_OF_MONTH));
    Log.d("lpl", "当前日期: " + currentdata);
    Map<String, ?> map = preferences.getAll();
    getScheduleData(map);
    LinearLayout root = findViewById(R.id.summary_layout);
    SpiderView spiderView = new SpiderView(this, Item, Level, datalevel);
    spiderView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    root.addView(spiderView);
    scheduledata = findViewById(R.id.summary_history);
    String schdata = "";
    for (int i = 0, j = 1; i < Item; i++, j++) {
      schdata += j + "." + schedule_item[i] + ": " + datanumber[i] + "天\n";
    }
    scheduledata.setText(schdata);
  }

  private void getScheduleData(Map<String, ?> map) {
    for (Map.Entry<String, ?> entry : map.entrySet()) {
      if (Long.valueOf(entry.getKey()) < Long.valueOf(currentdata)) {
        switch (entry.getValue().toString()) {
          case "运动":
            datanumber[0]++;
            break;
          case "购物":
            datanumber[1]++;
            break;
          case "拜访":
            datanumber[2]++;
            break;
          case "旅游":
            datanumber[3]++;
            break;
          case "学习":
            datanumber[4]++;
            break;
          case "聚会":
            datanumber[5]++;
            break;
          case "活动":
            datanumber[6]++;
            break;
          case "其它":
            datanumber[7]++;
            break;
          default:
            throw new IllegalStateException("Unexpected value: " + entry.getValue());
        }
      }
    }
    for (int i = 0; i < Item; i++) {
      if (datanumber[i] > 10) {
        datalevel[i] = 6;
      } else if (datanumber[i] >= 7) {
        datalevel[i] = 5;
      } else if (datalevel[i] >= 5) {
        datalevel[i] = 4;
      } else if (datanumber[i] >= 3) {
        datalevel[i] = 3;
      } else if (datanumber[i] >= 1) {
        datalevel[i] = 2;
      } else {
        datalevel[i] = 1;
      }
    }
  }
}

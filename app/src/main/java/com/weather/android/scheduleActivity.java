package com.weather.android;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.weather.android.util.DataUtil.appendZero;

public class scheduleActivity extends AppCompatActivity {
  private SharedPreferences preferences;
  private SharedPreferences.Editor edit;
  private TextView choose_day, data_schedule, item_schedule;
  private Button summary, clearschedule, lookschedule, weatherhistory;
  private DatePicker datepicker;
  private AlertDialog saveialog, deleateDialog, clearDialog;
  private Spinner spinner;
  private String schedule_item[] = {"运动", "购物", "拜访", "旅游", "学习", "聚会", "活动", "其它"};
  private ArrayAdapter adapter;
  private Calendar maxcalendar, mincalendar;
  private ListView schedule_listview;
  private List<Map.Entry> list_item = new ArrayList<>();
  private Map map;
  private String dialogType;
  private LVAdapter lv_dapter;
  private String currentCounty;


  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_schedule);
    currentCounty = getIntent().getStringExtra("currentCounty");
    preferences = getSharedPreferences("schedule", MODE_PRIVATE);
    edit = preferences.edit();
    summary = findViewById(R.id.summary);
    weatherhistory = findViewById(R.id.weather_history);
    clearschedule = findViewById(R.id.clear_schedule);
    lookschedule = findViewById(R.id.lookschedule);
    schedule_listview = findViewById(R.id.schedule_listview);
    datepicker = findViewById(R.id.datepicker);
    mincalendar = Calendar.getInstance();
    mincalendar.add(Calendar.DAY_OF_MONTH, 1);
    maxcalendar = Calendar.getInstance();
    maxcalendar.add(Calendar.MONTH, 1);
    datepicker.setMinDate(mincalendar.getTimeInMillis());
    datepicker.setMaxDate(maxcalendar.getTimeInMillis());
    adapter = new ArrayAdapter(this, R.layout.schedule_item, schedule_item);
    datepicker.setOnDateChangedListener(new dateChangeListener());
    summary.setOnClickListener(new onClicklistener());
    weatherhistory.setOnClickListener(new onClicklistener());
    clearschedule.setOnClickListener(new onClicklistener());
    lookschedule.setOnClickListener(new onClicklistener());
  }

  private class dialogOnLicklistener implements DialogInterface.OnClickListener {
    private String deletedata;
    private int position;

    public dialogOnLicklistener() {
    }

    public dialogOnLicklistener(String deletedata, int position) {
      this.deletedata = deletedata;
      this.position = position;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
      Log.d("lpl", "onClick: " + dialogType + " " + deletedata);
      if (dialogType.equals("save")) {
        if (which == BUTTON_POSITIVE) {
          String chooseday = choose_day.getText().toString().replace(".", "");
          String item = spinner.getSelectedItem().toString();
          isOverFlow(preferences);
          if (!TextUtils.isEmpty(preferences.getString(chooseday, null))) {
            edit.remove(chooseday);
          }
          edit.putString(chooseday, item);
          edit.apply();
          refreshList();
          Toast.makeText(scheduleActivity.this, "已经保存", Toast.LENGTH_LONG).show();
        }
      } else if (dialogType.equals("delete")) {
        if (which == BUTTON_POSITIVE) {
          if (!TextUtils.isEmpty(preferences.getString(deletedata, null))) {
            edit.remove(deletedata);
            list_item.remove(position);
          }
          edit.apply();
          lv_dapter.notifyDataSetChanged();
          Log.d("lpl", "deletedata: " + preferences.getString(deletedata, null));
          Toast.makeText(scheduleActivity.this, "已经删除", Toast.LENGTH_LONG).show();
        }
      } else if (dialogType.equals("clear")) {
        if (which == BUTTON_POSITIVE) {
          edit.clear();
          list_item.clear();
          edit.apply();
          lv_dapter.notifyDataSetChanged();
          Toast.makeText(scheduleActivity.this, "清空成功", Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  private void isOverFlow(SharedPreferences preferences) {
    Map<String, ?> map = preferences.getAll();
    Log.d("lpl", "isOverFlow: " + map.keySet().size());
    if (map.keySet().size() > 60) {
      long min = Long.MAX_VALUE;
      for (String day : map.keySet()) {
        if (Long.valueOf(day) < min) {
          min = Long.valueOf(day);
        }
        edit.remove(String.valueOf(min));
        edit.apply();
      }
    } else {
      return;
    }
  }

  private class dateChangeListener implements DatePicker.OnDateChangedListener {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
      monthOfYear += 1;
      saveialog = new AlertDialog.Builder(scheduleActivity.this)
          .setView(R.layout.dialog_layout).
              setPositiveButton("保存", new dialogOnLicklistener())
          .setNegativeButton("取消", new dialogOnLicklistener())
          .create();
      dialogType = "save";
      saveialog.show();
      saveialog.getButton(BUTTON_POSITIVE).setTextColor(Color.BLACK);
      choose_day = saveialog.findViewById(R.id.day_text);
      choose_day.setText(year + "." + appendZero(monthOfYear) + "." + appendZero(dayOfMonth));
      spinner = saveialog.findViewById(R.id.spinner);
      spinner.setAdapter(adapter);
    }
  }


  private class onClicklistener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      Intent intent;
      switch (v.getId()) {
        case R.id.summary:
          intent = new Intent(scheduleActivity.this, summaryActivity.class);
          startActivity(intent);
          break;
        case R.id.clear_schedule:
          showClearDialog();
          break;
        case R.id.lookschedule:
          handlelv();
          break;
        case R.id.weather_history:
          intent = new Intent(scheduleActivity.this, weatherhistoryActivity.class);
          intent.putExtra("currentCounty", currentCounty);
          startActivity(intent);
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + v);
      }
    }
  }

  private void handlelv() {
    if (lookschedule.getText().equals("查看已有计划")) {
      lookschedule.setText("隐藏已有计划");
      schedule_listview.setVisibility(View.VISIBLE);
      clearschedule.setVisibility(View.VISIBLE);
      summary.setVisibility(View.GONE);
      weatherhistory.setVisibility(View.GONE);
      lv_dapter = new LVAdapter();
      schedule_listview.setAdapter(lv_dapter);
      refreshList();
      schedule_listview.setOnItemLongClickListener(new onItemLongClickListener());
    } else {
      schedule_listview.setVisibility(View.GONE);
      lookschedule.setText("查看已有计划");
      clearschedule.setVisibility(View.GONE);
      summary.setVisibility(View.VISIBLE);
      weatherhistory.setVisibility(View.VISIBLE);
    }
  }

  private void refreshList() {
    if (lookschedule.getText().equals("隐藏已有计划")) {
      list_item.clear();
      map = preferences.getAll();
      for (Object entry : map.entrySet()) {
        list_item.add((Map.Entry) entry);
      }
      lv_dapter.notifyDataSetChanged();
    }
  }

  private class LVAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return list_item.size();
    }

    @Override
    public Object getItem(int position) {
      return position;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view;
      if (convertView == null) {
        view = LayoutInflater.from(scheduleActivity.this).inflate(R.layout.scheduleitem_layout, null);
      } else {
        view = convertView;
      }
      data_schedule = view.findViewById(R.id.data_schedule);
      item_schedule = view.findViewById(R.id.item_schedule);
      data_schedule.setText(list_item.get(position).getKey().toString());
      item_schedule.setText(list_item.get(position).getValue().toString());
      return view;
    }
  }

  private class onItemLongClickListener implements AdapterView.OnItemLongClickListener {

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
      String data = list_item.get(position).getKey().toString();
      ;
      String item = list_item.get(position).getValue().toString();
      showDeleateDialog(data, item, position);
      return false;
    }
  }

  private void showDeleateDialog(String data, String item, int position) {
    deleateDialog = new AlertDialog.Builder(this)
        .setMessage("是否要删除" + data + "的" + item + "计划？")
        .setPositiveButton("删除", new dialogOnLicklistener(data, position))
        .setNegativeButton("取消", new dialogOnLicklistener())
        .create();
    dialogType = "delete";
    deleateDialog.show();
  }

  private void showClearDialog() {
    clearDialog = new AlertDialog.Builder(this)
        .setMessage("请确认是否要清空所有的的计划？")
        .setPositiveButton("清空", new dialogOnLicklistener())
        .setNegativeButton("取消", new dialogOnLicklistener())
        .create();
    dialogType = "clear";
    clearDialog.show();
  }
}

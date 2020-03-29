package com.weather.android;


import java.util.Map;

import org.w3c.dom.Text;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class scheduleActivity extends AppCompatActivity {
  private SharedPreferences preferences;
  private SharedPreferences.Editor edit;
  private TextView choose_day;
  private Button summary,clearschedule;
  private DatePicker datepicker;
  private AlertDialog alertDialog;
  private Spinner spinner;
  private String schedule_item[] = {"运动", "购物","拜访","旅游","学习","聚会","其它"};
  private ArrayAdapter adapter;
  private Calendar maxcalendar,mincalendar;


  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_schedule);
    preferences=getSharedPreferences("schedule",MODE_PRIVATE);
    edit=preferences.edit();
    summary = findViewById(R.id.summary);
    clearschedule=findViewById(R.id.clear_schedule);
    datepicker = findViewById(R.id.datepicker);
    mincalendar=Calendar.getInstance();
    mincalendar.add(Calendar.DAY_OF_MONTH,1);
    maxcalendar=Calendar.getInstance();
    maxcalendar.add(Calendar.MONTH,1);
    datepicker.setMinDate(mincalendar.getTimeInMillis());
    datepicker.setMaxDate(maxcalendar.getTimeInMillis());
    adapter = new ArrayAdapter(this, R.layout.schedule_item, schedule_item);
    alertDialog = new AlertDialog.Builder(this)
        .setView(R.layout.dialog_layout).
            setPositiveButton("保存", new dialogOnLicklistener())
        .setNegativeButton("取消", new dialogOnLicklistener())
        .create();
    datepicker.setOnDateChangedListener(new dateChangeListener());
    summary.setOnClickListener(new onLicklistener());
    clearschedule.setOnClickListener(new onLicklistener());
  }

  private class dialogOnLicklistener implements DialogInterface.OnClickListener {
    @Override
    public void onClick(DialogInterface dialog, int which) {
      if (which == BUTTON_POSITIVE) {
        String chooseday=choose_day.getText().toString().replace(".","");
        String item=spinner.getSelectedItem().toString();
        isOverFlow(preferences);
        if (!TextUtils.isEmpty(preferences.getString(chooseday,null))) {
          edit.remove(chooseday);
        }
        edit.putString(chooseday,item);
        edit.apply();
        Toast.makeText(scheduleActivity.this, "已经保存:"+choose_day.getText()+item, Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(scheduleActivity.this, "已经取消", Toast.LENGTH_LONG).show();
      }
    }
  }

  private void isOverFlow(SharedPreferences preferences) {
    Map<String, ?> map = preferences.getAll();
    Log.d("lpl", "isOverFlow: "+map.keySet().size());
    if(map.keySet().size()>60){
      long min=Long.MAX_VALUE;
      for(String day:map.keySet()){
        if(Long.valueOf(day)<min){
          min=Long.valueOf(day);
        }
        edit.remove(String.valueOf(min));
        edit.apply();
      }
    }else{
      return;
    }
  }

  private class dateChangeListener implements DatePicker.OnDateChangedListener {
    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
      monthOfYear += 1;
      alertDialog.show();
      alertDialog.getButton(BUTTON_POSITIVE).setTextColor(Color.BLACK);
      choose_day=alertDialog.findViewById(R.id.day_text);
      choose_day.setText(year+"."+appendZero(monthOfYear)+"."+appendZero(dayOfMonth));
      spinner = alertDialog.findViewById(R.id.spinner);
      spinner.setAdapter(adapter);
    }
  }

  private String appendZero(int number) {
    if(Integer.valueOf(number)<10){
      return "0"+number;
    }else{
      return String.valueOf(number);
    }
  }

  private class onLicklistener implements View.OnClickListener{
    @Override
    public void onClick(View v) {
      switch (v.getId()){
        case R.id.summary:
          Intent intent = new Intent(scheduleActivity.this, summaryActivity.class);
          startActivity(intent);
          break;
        case R.id.clear_schedule:
          edit.clear();
          edit.apply();
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + v);
      }
    }
  }
}

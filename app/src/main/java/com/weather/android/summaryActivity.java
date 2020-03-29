package com.weather.android;

import java.util.Map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.weather.android.customview.SpiderView;

public class summaryActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_summary);
    SharedPreferences preferences=getSharedPreferences("schedule",MODE_PRIVATE);
    Map<String, ?> map=preferences.getAll();
    for(Map.Entry<String,?> entry:map.entrySet()){
      Log.d("lpl", "onCreate: "+entry.getKey()+"  "+entry.getValue());
    }
    LinearLayout root=findViewById(R.id.summary_layout);
    SpiderView spiderView=new SpiderView(this,6,5,new double[]{1,2,3,4,3,5});
    spiderView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    root.addView(spiderView);
  }
}

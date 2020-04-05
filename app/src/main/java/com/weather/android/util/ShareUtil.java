package com.weather.android.util;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import com.weather.android.db.AppInfo;

public class ShareUtil {
  /**
   * 获取手机内所有支持分享的应用列表
   */
  public static ArrayList<AppInfo> getShareAppList(Context context, Intent intent) {
    ArrayList<AppInfo> shareAppInfos = new ArrayList<AppInfo>();
    PackageManager packageManager = context.getPackageManager();
    List<ResolveInfo> resolveInfos = getShareApps(context);
    if (null == resolveInfos) {
      return null;
    } else {
      for (ResolveInfo resolveInfo : resolveInfos) {
        AppInfo appInfo = new AppInfo();
        appInfo.setPkgName(resolveInfo.activityInfo.packageName);
        appInfo.setLaunchClassName(resolveInfo.activityInfo.name);
        appInfo.setAppName(resolveInfo.loadLabel(packageManager).toString());
        appInfo.setAppIcon(resolveInfo.loadIcon(packageManager));
        shareAppInfos.add(appInfo);
      }
    }
    return shareAppInfos;
  }

  /**
   * 查询手机内所有支持分享的应用列表
   */
  public static List<ResolveInfo> getShareApps(Context context) {
    List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
    Intent intent = new Intent(Intent.ACTION_SEND, null);
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    //intent.setType("text/plain"); //纯文本
    intent.setType("*/*");
    PackageManager pManager = context.getPackageManager();
    mApps = pManager.queryIntentActivities(intent,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    return mApps;
  }
}

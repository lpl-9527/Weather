package com.weather.android.customview;

import java.util.List;



import android.app.ActionBar.LayoutParams;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.weather.android.R;
import com.weather.android.db.AppInfo;
import com.weather.android.util.ShareUtil;

public class SharePopupWindow extends PopupWindow {

  //每行显示多少个
  private static final int APP_SIZE = 8;

  private View mMenuView;
  private GridView mGridView;
  private TextView mTextViewClose;
  private AppInfoAdapter mAdapter;
  private Intent shareIntent;

  private List<AppInfo> mAppinfoList;
  private List<GridView> mGridViewList;

  /**
   *
   * @param context
   * @param shareContent 要分享的内容
   */
  public SharePopupWindow(final Context context,final Uri shareContent) {
    super(context);
    LayoutInflater inflater = (LayoutInflater) context  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mMenuView = inflater.inflate(R.layout.share_dialog, null);
    //获取控件
    mGridView= mMenuView.findViewById(R.id.sharePopupWindow_gridView);
    mTextViewClose= mMenuView.findViewById(R.id.sharePopupWindow_close);
    //获取有分享功能的应用
    shareIntent = new Intent(Intent.ACTION_SEND);
    //shareIntent.setType("text/plain"); //纯文本
    shareIntent.setType("image/*");
    shareIntent.putExtra(Intent.EXTRA_STREAM, shareContent);
    mAppinfoList = ShareUtil.getShareAppList(context, shareIntent);

    //适配GridView
    mAdapter=new AppInfoAdapter(context, mAppinfoList);
    mGridView.setAdapter(mAdapter);

    //修改GridView
    changeGridView(context);

    mGridView.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
                              long id) {
        // TODO Auto-generated method stub
        AppInfo appInfo=mAppinfoList.get(position);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(new ComponentName(appInfo.getPkgName(), appInfo.getLaunchClassName()));
        //intent.setType("text/plain"); //纯文本
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, shareContent);
        context.startActivity(intent);
      }
    });
    //取消按钮
    mTextViewClose.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub
        dismiss();
      }
    });
    //设置SelectPicPopupWindow的View
    this.setContentView(mMenuView);
    //设置SelectPicPopupWindow弹出窗体的宽
    this.setWidth(LayoutParams.MATCH_PARENT);
    //设置SelectPicPopupWindow弹出窗体的高
    this.setHeight(LayoutParams.WRAP_CONTENT);
    //设置SelectPicPopupWindow弹出窗体可点击
    this.setFocusable(true);
    //设置窗口外也能点击（点击外面时，窗口可以关闭）
    this.setOutsideTouchable(true);
    //设置SelectPicPopupWindow弹出窗体动画效果
    this.setAnimationStyle(R.style.circleDialog);
    //实例化一个ColorDrawable颜色为半透明
    ColorDrawable dw = new ColorDrawable(0x00000000);
    //设置SelectPicPopupWindow弹出窗体的背景
    this.setBackgroundDrawable(dw);
  }

  /**
   * 将GridView改成单行横向布局
   */
  private void changeGridView(Context context) {
    // item宽度
    int itemWidth = dip2px(context, 100);
    // item之间的间隔
    int itemPaddingH = dip2px(context, 1);
    int size = mAppinfoList.size();
    // 计算GridView宽度
    int gridviewWidth = size/2 * (itemWidth + itemPaddingH);

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        gridviewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
    mGridView.setLayoutParams(params);
    mGridView.setColumnWidth(itemWidth);
    mGridView.setHorizontalSpacing(itemPaddingH);
    mGridView.setStretchMode(GridView.NO_STRETCH);
    mGridView.setNumColumns(size/2);
  }

  /**
   * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
   * @param context   上下文
   * @param dpValue   dp值
   * @return  px值
   */
  public static int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

}

package com.weather.android.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class SpiderView extends View {
  private Paint radarPaint,valuepaint;
  private float radius;
  private int centerX,centerY;
  private int count,level;
  private double angle;
  private double []data;

  public SpiderView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SpiderView(Context context,int count, int level, double[] data) {
    super(context);
    this.count = count;
    this.level = level;
    this.data = data;
    this.angle=Math.toRadians(360/count);
    init();
  }

  private void init() {
    radarPaint=new Paint();
    radarPaint.setStyle(Paint.Style.STROKE);
    radarPaint.setStrokeWidth(5);
    radarPaint.setColor(Color.GREEN);
    valuepaint=new Paint();
    valuepaint.setStyle(Paint.Style.FILL);
    valuepaint.setColor(Color.BLUE);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    radius=Math.min(h,w)/2f*0.8f;
    centerX=w/2;
    centerY=h/2;
    postInvalidate();
    super.onSizeChanged(w, h, oldw, oldh);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawPolygon(canvas);
    drawLines(canvas);
    drawRegion(canvas);
  }


  private void drawPolygon(Canvas canvas) {
    Path path=new Path();
    float r=radius/(level);
    float x,y;
    for(int i=1;i<=level;i++){
      float curR=r*i;
      path.reset();
      for(int j=0;j<count;j++){
        if(j==0){
          x=centerX+curR;
          y=centerY;
          path.moveTo(x,y);
        }else{
           x=(float)(centerX+curR*Math.cos(angle*j));
           y=(float)(centerY+curR*Math.sin(angle*j));
          path.lineTo(x,y);
        }
        if(i==level){
          Paint textpaint=new TextPaint();
          textpaint.setTextSize(40);
          textpaint.setTextAlign(Paint.Align.CENTER);
          textpaint.setStyle(Paint.Style.FILL_AND_STROKE);
          canvas.drawText(String.valueOf(j+1), (float) (centerX+radius*1.1f*Math.cos(angle*j)), (float) (centerY+radius*1.1f*Math.sin(angle*j)),textpaint);
        }
      }
      path.close();
      canvas.drawPath(path,radarPaint);

    }
  }
  private void drawLines(Canvas canvas) {
    Path path=new Path();
    for(int i=0;i<count;i++){
      path.reset();
      path.moveTo(centerX,centerY);
      float x=(float)(centerX+radius*Math.cos(angle*i));
      float y=(float)(centerY+radius*Math.sin(angle*i));
      path.lineTo(x,y);
      canvas.drawPath(path,radarPaint);
    }
  }

  private void drawRegion(Canvas canvas) {
    Path path=new Path();
    valuepaint.setAlpha(127);
    for(int i=0;i<count;i++){
      double percent=data[i]/(double)level;
      float x=(float)(centerX+radius*Math.cos(angle*i)*percent);
      float y=(float)(centerY+radius*Math.sin(angle*i)*percent);
      if(i==0){
        path.moveTo(x,centerY);
      }else{
        path.lineTo(x,y);
      }
      canvas.drawCircle(x,y,10,valuepaint);
    }
    valuepaint.setStyle(Paint.Style.FILL_AND_STROKE);
    canvas.drawPath(path,valuepaint);
  }
}

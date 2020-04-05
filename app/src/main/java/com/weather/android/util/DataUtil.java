package com.weather.android.util;

public class DataUtil {
  public static String appendZero(int number) {
    if (Integer.valueOf(number) < 10) {
      return "0" + number;
    } else {
      return String.valueOf(number);
    }
  }
}

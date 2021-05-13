package com.hks.netty.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classname:DateUtil
 *
 * @description:
 * @author: 陌意随影
 * @Date: 2021-05-14 00:59
 * @Version: 1.0
 **/
public class DateUtil {
    private static  final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss");
    public static String dateToStr(Date date){
        String format = dateFormat.format(date);
        return format;
    }
    public static Date strToDate(String dateStr){
        try {
            return  dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            return null;
        }
    }
}

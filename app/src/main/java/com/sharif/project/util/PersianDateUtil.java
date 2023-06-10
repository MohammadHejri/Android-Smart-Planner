package com.sharif.project.util;

import android.annotation.SuppressLint;
import android.text.InputFilter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class PersianDateUtil {

    public static Date truncateToStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static int getMinutesPassedFromStartOfDay(Date date) {
        Date startOfDay = truncateToStartOfDay(date);
        return (int) ((date.getTime() - startOfDay.getTime()) / 60000);
    }

    public static Date today() {
        return truncateToStartOfDay(new Date());
    }

    public static Date nextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static Date prevDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeString(Date date) {
        return new SimpleDateFormat("HH:mm").format(date);
    }

    public static String getDateString(Date date) {
        PersianDate pDate = new PersianDate(date);
        PersianDateFormat formatter = new PersianDateFormat("l j F Y");
        return new PersianNumberConverter().convert(formatter.format(pDate));
    }

    public static String getShortDateString(Date date) {
        PersianDate pDate = new PersianDate(date);
        PersianDateFormat formatter = new PersianDateFormat("Y/m/d");
        return new PersianNumberConverter().convert(formatter.format(pDate));
    }

    public static InputFilter[] getTimeInputFilter() {
        return new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if (source.length() == 0) {
                return null;// deleting, keep original editing
            }
            String result = "";
            result += dest.toString().substring(0, dstart);
            result += source.toString().substring(start, end);
            result += dest.toString().substring(dend, dest.length());

            if (result.length() > 5) {
                return "";// do not allow this edit
            }
            boolean allowEdit = true;
            boolean allowEdit2 = true;
            char c;
            if (result.length() > 0) {
                c = result.charAt(0);
                allowEdit &= (c >= '0' && c <= '2');
            }
            if (result.length() > 1) {
                c = result.charAt(1);
                if(result.charAt(0) == '0' || result.charAt(0) == '1')
                    allowEdit &= (c >= '0' && c <= '9');
                else
                    allowEdit &= (c >= '0' && c <= '3');
            }
            if (result.length() > 2) {
                c = result.charAt(2);
                allowEdit &= (c == ':');
            }
            if (result.length() > 3) {
                c = result.charAt(3);
                allowEdit &= (c >= '0' && c <= '5');
            }
            if (result.length() > 4) {
                c = result.charAt(4);
                allowEdit &= (c >= '0' && c <= '9');
            }

            if (result.length() > 0) {
                c = result.charAt(0);
                allowEdit2 &= (c >= '0' && c <= '9');
            }
            if (result.length() > 1) {
                c = result.charAt(1);
                allowEdit2 &= (c == ':');
            }
            if (result.length() > 2) {
                c = result.charAt(2);
                allowEdit2 &= (c >= '0' && c <= '5');
            }
            if (result.length() > 3) {
                c = result.charAt(3);
                allowEdit2 &= (c >= '0' && c <= '9');
            }
            if (result.length() > 4) {
                allowEdit2 = false;
            }

            return allowEdit || allowEdit2? null : "";
        }};
    }

    public static InputFilter[] getHourInputFilter() {
        return new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if (source.length() == 0) {
                return null;// deleting, keep original editing
            }
            String result = "";
            result += dest.toString().substring(0, dstart);
            result += source.toString().substring(start, end);
            result += dest.toString().substring(dend, dest.length());

            if (result.length() > 3) {
                return "";// do not allow this edit
            }
            boolean allowEdit = true;
            char c;
            if (result.length() > 0) {
                c = result.charAt(0);
                allowEdit &= (c >= '1' && c <= '9');
            }
            if (result.length() > 1) {
                c = result.charAt(1);
                allowEdit &= (c >= '0' && c <= '9');
            }
            if (result.length() > 2) {
                c = result.charAt(2);
                allowEdit &= (c >= '0' && c <= '9');
            }

            return allowEdit? null : "";
        }};
    }

}

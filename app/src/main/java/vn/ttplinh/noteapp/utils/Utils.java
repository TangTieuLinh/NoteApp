package vn.ttplinh.noteapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by linhtang on 7/28/17.
 */

public class Utils {

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return true;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    public static String convertTime(long time) {
        try {
            Date date = new Date(time);
            Format format = new SimpleDateFormat("dd/MM/yyyyy");
            return format.format(date);
        } catch (Exception e) {
            return "";
        }

    }
}

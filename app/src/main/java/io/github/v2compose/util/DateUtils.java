package io.github.v2compose.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DateUtils {

    public static String parseDate(long time) {
        return new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date(time));
    }
}

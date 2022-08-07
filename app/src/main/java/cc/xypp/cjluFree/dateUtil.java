package cc.xypp.cjluFree;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

public class dateUtil {
    public static String getDateStr(){
        return new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(new Date());
    }
}

package project.saporo.code;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public abstract class TimeConst {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    public static final DateTimeFormatter KST_PRETTY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'KST'", Locale.KOREAN);
}
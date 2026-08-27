package id.my.alan.minikasir.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public final class DateUtils {
    private DateUtils() {}

    private static final SimpleDateFormat DATE_TIME_FORMAT = 
            new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
    private static final SimpleDateFormat SHORT_DATE_FORMAT = 
            new SimpleDateFormat("dd/MM/yy HH:mm", new Locale("id", "ID"));

    public static synchronized String formatDateTime(long timestamp) {
        if (timestamp <= 0) return "-";
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    public static synchronized String formatShortDateTime(long timestamp) {
        if (timestamp <= 0) return "-";
        return SHORT_DATE_FORMAT.format(new Date(timestamp));
    }

    public static String generateTransactionCode() {
        String uuidSnippet = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return "TRX-" + System.currentTimeMillis() + "-" + uuidSnippet;
    }
}

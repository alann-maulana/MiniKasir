package id.my.alan.minikasir.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtils {
    private CurrencyUtils() {}

    private static final Locale LOCALE_ID = new Locale("id", "ID");

    public static String formatRupiah(long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_ID);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount).replace(",00", "");
    }

    public static long parseRupiah(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0L;
        }
        try {
            // Remove non-digit characters
            String clean = input.replaceAll("[^0-9]", "");
            if (clean.isEmpty()) return 0L;
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}

package id.my.alan.minikasir.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CurrencyUtilsTest {

    @Test
    public void formatRupiah_formatsCorrectly() {
        String formatted = CurrencyUtils.formatRupiah(15000L);
        assertTrue(formatted.contains("15.000") || formatted.contains("15,000"));
    }

    @Test
    public void parseRupiah_parsesCorrectly() {
        assertEquals(15000L, CurrencyUtils.parseRupiah("Rp 15.000"));
        assertEquals(50000L, CurrencyUtils.parseRupiah("50.000"));
        assertEquals(1000000L, CurrencyUtils.parseRupiah("Rp 1.000.000"));
        assertEquals(0L, CurrencyUtils.parseRupiah(""));
        assertEquals(0L, CurrencyUtils.parseRupiah(null));
    }
}

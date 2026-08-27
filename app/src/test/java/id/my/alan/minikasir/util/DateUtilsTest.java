package id.my.alan.minikasir.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DateUtilsTest {

    @Test
    public void formatDateTime_returnsNonEmptyString() {
        long now = System.currentTimeMillis();
        String formatted = DateUtils.formatDateTime(now);
        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
    }

    @Test
    public void generateTransactionCode_generatesUniqueCodes() {
        String code1 = DateUtils.generateTransactionCode();
        String code2 = DateUtils.generateTransactionCode();

        assertTrue(code1.startsWith("TRX-"));
        assertTrue(code2.startsWith("TRX-"));
        assertNotEquals(code1, code2);
    }
}

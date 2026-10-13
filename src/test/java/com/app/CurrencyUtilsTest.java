package com.app;

import com.app.Utils.CurrencyUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyUtilsTest {

    @Test
    public void testFormatBigDecimal() {
        assertEquals("$0", CurrencyUtils.format((BigDecimal) null));
        String formatted = CurrencyUtils.format(new BigDecimal("1500000"));
        assertNotNull(formatted);
        assertTrue(formatted.contains("1.500.000") || formatted.contains("1500000"));
    }

    @Test
    public void testFormatDouble() {
        String formatted = CurrencyUtils.format(250000.0);
        assertNotNull(formatted);
        assertTrue(formatted.contains("250.000") || formatted.contains("250000"));
    }

    @Test
    public void testParse() {
        assertEquals(BigDecimal.ZERO, CurrencyUtils.parse(null));
        assertEquals(BigDecimal.ZERO, CurrencyUtils.parse(""));
        assertEquals(BigDecimal.ZERO, CurrencyUtils.parse("   "));

        BigDecimal parsed = CurrencyUtils.parse("$1.500.000");
        assertEquals(new BigDecimal("1500000.00"), parsed);
    }
}

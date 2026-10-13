package com.app;

import com.app.Utils.DateUtils;
import com.app.Utils.StringUtils;
import com.app.Utils.ValidationUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    public void testDateUtils() {
        LocalDate date = LocalDate.of(2026, 5, 20);
        assertEquals("20/05/2026", DateUtils.format(date));
        assertEquals("", DateUtils.format((LocalDate) null));

        LocalDate parsed = DateUtils.parseDate("20/05/2026");
        assertEquals(date, parsed);
        assertNull(DateUtils.parseDate("invalid-date"));
        assertNull(DateUtils.parseDate(null));

        LocalDateTime dt = LocalDateTime.of(2026, 5, 20, 14, 30);
        assertEquals("20/05/2026 14:30", DateUtils.format(dt));
        assertEquals("", DateUtils.format((LocalDateTime) null));
    }

    @Test
    public void testStringUtils() {
        assertTrue(StringUtils.isNullOrBlank(null));
        assertTrue(StringUtils.isNullOrBlank(""));
        assertTrue(StringUtils.isNullOrBlank("   "));
        assertFalse(StringUtils.isNullOrBlank("hola"));

        assertEquals("Juan Perez", StringUtils.capitalizeWords("juan perez"));
        assertEquals("Juan Perez", StringUtils.capitalizeWords("  JUAN   PEREZ  "));
        assertEquals("", StringUtils.capitalizeWords(null));

        assertEquals("Hola...", StringUtils.truncate("Hola mundo cruel", 4));
        assertEquals("Hola", StringUtils.truncate("Hola", 10));
    }

    @Test
    public void testValidationUtils() {
        assertTrue(ValidationUtils.isValidEmail("usuario@ejemplo.com"));
        assertFalse(ValidationUtils.isValidEmail("invalido"));
        assertFalse(ValidationUtils.isValidEmail(null));

        assertTrue(ValidationUtils.isValidPhone("3001234567"));
        assertTrue(ValidationUtils.isValidPhone("+573001234567"));
        assertFalse(ValidationUtils.isValidPhone("123"));
        assertFalse(ValidationUtils.isValidPhone(null));

        assertTrue(ValidationUtils.isPositivePrice(new BigDecimal("100")));
        assertFalse(ValidationUtils.isPositivePrice(BigDecimal.ZERO));
        assertFalse(ValidationUtils.isPositivePrice(new BigDecimal("-50")));
        assertFalse(ValidationUtils.isPositivePrice(null));

        assertTrue(ValidationUtils.isValidAmount(0));
        assertTrue(ValidationUtils.isValidAmount(10));
        assertFalse(ValidationUtils.isValidAmount(-1));
    }
}

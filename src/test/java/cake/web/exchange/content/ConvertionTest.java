package cake.web.exchange.content;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ConvertionTest {

    // ==================== INTEGER TYPE CONVERSIONS ====================

    @Test
    void shouldConvertInteger() {
        assertEquals(123, Conversion.convert("123", Integer.class));
    }

    @Test
    void shouldConvertNegativeInteger() {
        assertEquals(-456, Conversion.convert("-456", Integer.class));
    }

    @Test
    void shouldConvertByte() {
        assertEquals((byte) 127, Conversion.convert("127", Byte.class));
    }

    @Test
    void shouldConvertShort() {
        assertEquals((short) 32767, Conversion.convert("32767", Short.class));
    }

    @Test
    void shouldConvertLong() {
        assertEquals(9223372036854775807L, Conversion.convert("9223372036854775807", Long.class));
    }

    @Test
    void shouldConvertBigInteger() {
        BigInteger expected = new BigInteger("12345678901234567890");
        assertEquals(expected, Conversion.convert("12345678901234567890", BigInteger.class));
    }

    @Test
    void shouldThrowExceptionWhenIntegerOverflows() {
        assertThrows(IllegalArgumentException.class,
                () -> Conversion.convert("99999999999999999999", Integer.class));
    }

    // ==================== FLOATING POINT CONVERSIONS ====================

    @Test
    void shouldConvertFloat() {
        assertEquals(123.45f, Conversion.convert("123.45", Float.class));
    }

    @Test
    void shouldConvertDouble() {
        assertEquals(123.456789, Conversion.convert("123.456789", Double.class));
    }

    @Test
    void shouldConvertScientificNotation() {
        assertEquals(1.23e-4, Conversion.convert("1.23E-4", Double.class));
    }

    @Test
    void shouldConvertBigDecimal() {
        BigDecimal expected = new BigDecimal("123.45678901234567890");
        assertEquals(expected, Conversion.convert("123.45678901234567890", BigDecimal.class));
    }

    // ==================== STRING CONVERSIONS ====================

    @Test
    void shouldConvertString() {
        assertEquals("hello", Conversion.convert("hello", String.class));
    }

    @Test
    void shouldConvertObjectToString() {
        assertEquals("world", Conversion.convert("world", Object.class));
    }

    // ==================== BOOLEAN CONVERSIONS ====================

    @Test
    void shouldConvertBooleanTrue() {
        assertTrue((Boolean) Conversion.convert("true", Boolean.class));
        assertTrue((Boolean) Conversion.convert("TRUE", Boolean.class));
        assertTrue((Boolean) Conversion.convert("  true  ", Boolean.class));
    }

    @Test
    void shouldConvertBooleanFalse() {
        assertFalse((Boolean) Conversion.convert("false", Boolean.class));
        assertFalse((Boolean) Conversion.convert("FALSE", Boolean.class));
    }

    @Test
    void shouldNotConvertNonBooleanAsBoolean() {
        // "not a boolean" should not be caught by boolean check because regex only
        // matches true/false
        // It will fall through to String conversion
        assertEquals("not a boolean", Conversion.convert("not a boolean", String.class));
    }

    // ==================== UUID CONVERSIONS ====================

    @Test
    void shouldConvertValidUUID() {
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";
        UUID expected = UUID.fromString(uuidString);
        assertEquals(expected, Conversion.convert(uuidString, UUID.class));
    }

    @Test
    void shouldThrowExceptionForInvalidUUID() {
        assertThrows(ClassCastException.class,
                () -> Conversion.convert("not-a-uuid", UUID.class));
    }

    // ==================== DATE/TIME CONVERSIONS ====================

    @Test
    void shouldConvertLocalTime() {
        LocalTime expected = LocalTime.of(14, 30, 45);
        assertEquals(expected, Conversion.convert("14:30:45", LocalTime.class));
    }

    @Test
    void shouldConvertLocalTimeWithMillis() {
        LocalTime expected = LocalTime.of(14, 30, 45, 123000000);
        assertEquals(expected, Conversion.convert("14:30:45.123", LocalTime.class));
    }

    @Test
    void shouldConvertLocalDateTime() {
        LocalDateTime expected = LocalDateTime.of(2024, 5, 15, 14, 30, 45);
        assertEquals(expected, Conversion.convert("2024-05-15T14:30:45", LocalDateTime.class));
    }

    @Test
    void shouldConvertOffsetDateTime() {
        OffsetDateTime expected = OffsetDateTime.parse("2024-05-15T14:30:45-03:00");
        assertEquals(expected, Conversion.convert("2024-05-15T14:30:45-03:00", OffsetDateTime.class));
    }

    @Test
    void shouldConvertOffsetDateTimeWithZ() {
        OffsetDateTime expected = OffsetDateTime.parse("2024-05-15T14:30:45Z");
        assertEquals(expected, Conversion.convert("2024-05-15T14:30:45Z", OffsetDateTime.class));
    }

    @Test
    void shouldConvertOffsetTime() {
        OffsetTime expected = OffsetTime.parse("14:30:45-03:00");
        assertEquals(expected, Conversion.convert("14:30:45-03:00", OffsetTime.class));
    }

    @Test
    void shouldConvertZonedDateTime() {
        ZonedDateTime expected = ZonedDateTime.parse("2024-05-15T14:30:45-03:00[America/Sao_Paulo]");
        assertEquals(expected, Conversion.convert("2024-05-15T14:30:45-03:00[America/Sao_Paulo]", ZonedDateTime.class));
    }

    @Test
    void shouldThrowExceptionForInvalidDateTime() {
        assertThrows(ClassCastException.class,
                () -> Conversion.convert("not-a-date", LocalDateTime.class));
    }

    // ==================== NULL HANDLING ====================

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertNull(Conversion.convert(null, String.class));
        assertNull(Conversion.convert(null, Integer.class));
    }

    // ==================== UNSUPPORTED TYPES ====================

    @Test
    void shouldThrowExceptionForUnsupportedType() {
        assertThrows(ClassCastException.class,
                () -> Conversion.convert("test", UnsupportedType.class));
    }

    // ==================== kindOfParamType TESTS ====================

    @Test
    void kindOfParamTypeShouldReturnInteger() {
        assertEquals("integer", Conversion.kindOfParamType("123"));
        assertEquals("integer", Conversion.kindOfParamType("-456"));
        assertEquals("integer", Conversion.kindOfParamType("0"));
    }

    @Test
    void kindOfParamTypeShouldReturnFloatingPoint() {
        assertEquals("floating-point", Conversion.kindOfParamType("123.45"));
        assertEquals("floating-point", Conversion.kindOfParamType("-67.89"));
        assertEquals("floating-point", Conversion.kindOfParamType("1.23E-4"));
    }

    @Test
    void kindOfParamTypeShouldReturnLocalTime() {
        assertEquals("java.time.LocalTime", Conversion.kindOfParamType("14:30:45"));
        assertEquals("java.time.LocalTime", Conversion.kindOfParamType("14:30:45.123"));
    }

    @Test
    void kindOfParamTypeShouldReturnOffsetDateTime() {
        assertEquals("java.time.OffsetDateTime", Conversion.kindOfParamType("2024-05-15T14:30:45-03:00"));
        assertEquals("java.time.OffsetDateTime", Conversion.kindOfParamType("2024-05-15T14:30:45Z"));
    }

    @Test
    void kindOfParamTypeShouldReturnOffsetTime() {
        assertEquals("java.time.OffsetTime", Conversion.kindOfParamType("14:30:45-03:00"));
        assertEquals("java.time.OffsetTime", Conversion.kindOfParamType("14:30:45Z"));
    }

    @Test
    void kindOfParamTypeShouldReturnZonedDateTime() {
        assertEquals("java.time.ZonedDateTime",
                Conversion.kindOfParamType("2024-05-15T14:30:45-03:00[America/Sao_Paulo]"));
    }

    @Test
    void kindOfParamTypeShouldReturnUUID() {
        assertEquals("java.util.UUID", Conversion.kindOfParamType("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void kindOfParamTypeShouldReturnBoolean() {
        assertEquals("boolean", Conversion.kindOfParamType("true"));
        assertEquals("boolean", Conversion.kindOfParamType("false"));
        assertEquals("boolean", Conversion.kindOfParamType("  true  "));
    }

    @Test
    void kindOfParamTypeShouldReturnString() {
        assertEquals("string", Conversion.kindOfParamType("any random string"));
        assertEquals("string", Conversion.kindOfParamType("abc123"));
        assertEquals("string", Conversion.kindOfParamType(""));
    }

    // ==================== EDGE CASES ====================

    @Test
    void shouldTrimBooleanValue() {
        assertTrue((Boolean) Conversion.convert("  true  ", Boolean.class));
    }

    @Test
    void shouldHandleEmptyString() {
        assertEquals("", Conversion.convert("", String.class));
    }

    @Test
    void shouldConvertLargeLong() {
        Long maxLong = 9223372036854775807L;
        assertEquals(maxLong, Conversion.convert("9223372036854775807", Long.class));
    }

    // Helper class for unsupported type test
    private static class UnsupportedType {
    }
}
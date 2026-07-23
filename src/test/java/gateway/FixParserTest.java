package gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FixParserTest {

    /** Turns an ASCII string into the byte buffer the parser would receive. */
    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    @Test
    @DisplayName("single digit")
    void singleDigit() {
        byte[] buf = bytes("7");
        assertEquals(7L, FixParser.parseLong(buf, 0, buf.length));
    }

    @Test
    @DisplayName("multi-digit")
    void multiDigit() {
        byte[] buf = bytes("15025");
        assertEquals(15025L, FixParser.parseLong(buf, 0, buf.length));
    }

    @Test
    @DisplayName("leading zeros collapse")
    void leadingZeros() {
        byte[] buf = bytes("007");
        assertEquals(7L, FixParser.parseLong(buf, 0, buf.length));
    }

    @Test
    @DisplayName("zero parses to zero, not the sentinel")
    void zero() {
        byte[] buf = bytes("0");
        assertEquals(0L, FixParser.parseLong(buf, 0, buf.length));
    }

    @Test
    @DisplayName("non-digit in the middle rejects")
    void nonDigitInMiddle() {
        byte[] buf = bytes("12a34");
        assertEquals(-1L, FixParser.parseLong(buf, 0, buf.length));
    }

    @Test
    @DisplayName("leading sign rejects")
    void leadingSign() {
        byte[] buf = bytes("-5");
        assertEquals(-1L, FixParser.parseLong(buf, 0, buf.length));
    }

    @Test
    @DisplayName("empty range rejects")
    void emptyRange() {
        byte[] buf = bytes("123");
        assertEquals(-1L, FixParser.parseLong(buf, 1, 1));
    }

    @Test
    @DisplayName("parses a sub-range, ignoring surrounding bytes")
    void subRange() {
        // The scanner hands parseLong a slice of a larger message.
        byte[] buf = bytes("AA150BB");
        assertEquals(150L, FixParser.parseLong(buf, 2, 5));
    }

    @Test
    @DisplayName("Long.MAX_VALUE parses exactly")
    void maxValueExact() {
        byte[] buf = bytes("9223372036854775807");
        assertEquals(Long.MAX_VALUE, FixParser.parseLong(buf, 0, buf.length));
    }

    @Test
    @DisplayName("one past Long.MAX_VALUE overflows and rejects")
    void overflowByOne() {
        byte[] buf = bytes("9223372036854775808");
        assertEquals(-1L, FixParser.parseLong(buf, 0, buf.length));
    }

    @Test
    @DisplayName("obvious overflow rejects")
    void overflowLarge() {
        byte[] buf = bytes("99999999999999999999999");
        assertEquals(-1L, FixParser.parseLong(buf, 0, buf.length));
    }
}

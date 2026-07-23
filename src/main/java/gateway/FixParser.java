package gateway;

/**
 * Hand-rolled FIX tag-value parser. Steps 1–5 assemble here.
 * Pure byte[]-in: no io.netty, no com.lmax imports, ever.
 */
final class FixParser {

    // ---- Step 1: numeric primitives ----

    /**
     * Parses the ASCII digits in {@code buf[start, end)} into a non-negative long.
     * Returns -1L on any reject: empty range, non-digit byte, or overflow.
     */
    static long parseLong(byte[] buf, int start, int end) {
        if (start >= end) {
            return -1L;
        }
        long val = 0L;
        for (int i = start; i < end; i++) {
            int digit = buf[i] - '0';
            if (digit < 0 || digit > 9) {
                return -1L;
            }
            if (val > (Long.MAX_VALUE - digit) / 10) {
                return -1L;
            }
            val = val * 10 + digit;
        }
        return val;
    }
}
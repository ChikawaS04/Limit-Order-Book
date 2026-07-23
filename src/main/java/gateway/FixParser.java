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

    // Local to parsePrice — the only caller that knows about cents-scaling.
    private static final int MAX_DECIMALS = 2;
    private static final long PRICE_SCALE = 100;  // 2 decimal places -> cents
    private static final byte DOT = '.';

    /**
     * Parses a FIX price field (bytes [start, end)) to long cents.
     * "150" -> 15000, "150.2" -> 15020, "150.25" -> 15025.
     *
     * Returns -1L on any reject: empty, non-digit, empty integer part,
     * more than two decimals, or a non-positive result. Zero/negative
     * price policy is applied here (the > 0 check) because parseLong
     * deliberately treats "0" as a valid parse.
     *
     * @return long cents, or -1L on reject
     */
    static long parsePrice(byte[] buf, int start, int end) {
        if (start >= end) { return -1L; }

        // Locate the first '.'. Absence means integer-only price.
        int dot = -1;
        for (int i = start; i < end; i++) {
            if (buf[i] == DOT) {
                dot = i;
                break;
            }
        }

        long intPart;
        long fracScaled;

        if (dot == -1) {
            // No fraction: whole range is the integer part.
            intPart = parseLong(buf, start, end);
            if (intPart == -1L) { return -1L; }  // non-digit somewhere
            fracScaled = 0L;
        } else {
            // Integer part is [start, dot). Empty here (".25", ".") rejects,
            // because parseLong returns -1 on an empty range.
            intPart = parseLong(buf, start, dot);
            if (intPart == -1L) { return -1L; }

            int fracStart = dot + 1;
            int fracLen = end - fracStart;

            if (fracLen == 0 || fracLen > MAX_DECIMALS) { return -1L; }  // "150." or "150.256"

            long frac = parseLong(buf, fracStart, end);
            if (frac == -1L) { return -1L; }  // non-digit in fraction, e.g. "150.2x"

            // Scale by length, not value: one digit -> *10, two digits -> *1.
            fracScaled = (fracLen == 1) ? frac * 10 : frac;
        }

        // intPart * PRICE_SCALE has enormous headroom in long cents; ASML's
        // four-figure price is nowhere near overflow. Left un-guarded to keep
        // the happy path clean.
        long cents = intPart * PRICE_SCALE + fracScaled;

        // Price policy: zero or negative rejects. parseLong lets "0" through
        // as a valid 0, so the field-level check lives here, not in parseLong.
        return cents > 0 ? cents : -1L;
    }
}
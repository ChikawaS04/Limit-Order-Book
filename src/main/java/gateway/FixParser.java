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

    // Scanner output: parallel arrays, index-aligned, valid up to the
    // count returned by scan(). No per-message allocation; no reset
    // needed between messages since count bounds every read.
    final int[] tags       = new int[FixConstants.MAX_FIELDS];
    final int[] valStarts  = new int[FixConstants.MAX_FIELDS];
    final int[] valEnds    = new int[FixConstants.MAX_FIELDS];

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

    /**
     * Tokenises one complete FIX message into (tag, valueStart, valueEnd)
     * triples stored in the parallel arrays. Pure structure — no checksum,
     * no dispatch, no field-type judgment. Unknown tags pass through.
     *
     * @return number of fields scanned, or -1 on any structural reject.
     */
    int scan(byte[] buf, int offset, int length) {
        final int end = offset + length;
        int pos = offset;
        int count = 0;

        while (pos < end) {
            // --- locate '=' ending the tag; SOH-before-'=' means no '=' ---
            final int tagStart = pos;
            int eq = pos;
            while (eq < end && buf[eq] != FixConstants.EQUALS) {
                if (buf[eq] == FixConstants.SOH) {
                    return -1;                 // field has no '='
                }
                eq++;
            }
            if (eq == end) {
                return -1;                     // ran off the end seeking '='
            }

            // --- locate SOH ending the value; '=' inside the value is legal ---
            final int valStart = eq + 1;
            int soh = valStart;
            while (soh < end && buf[soh] != FixConstants.SOH) {
                soh++;
            }
            if (soh == end) {
                return -1;                     // value never terminated by SOH
            }

            // --- parse the tag; empty or non-numeric falls out as -1 ---
            final long tag = parseLong(buf, tagStart, eq);
            if (tag < 0) {
                return -1;                     // empty tag or non-digit tag
            }

            // --- bound the storage before writing ---
            if (count == FixConstants.MAX_FIELDS) {
                return -1;                     // too many fields
            }

            tags[count]      = (int) tag;
            valStarts[count] = valStart;
            valEnds[count]   = soh;
            count++;

            pos = soh + 1;                     // step past SOH to next field
        }

        return count;
    }

    /**
     * Verifies the FIX checksum (tag 10) for the message in buf[offset, offset+length).
     *
     * Relies on the frame decoder's trailer invariant: the message ends with the
     * 7-byte field 10=xxx<SOH>. The checksum covers every byte from the start of the
     * message up to and including the SOH preceding 10=, summed mod 256, compared
     * against the 3-digit value of tag 10.
     *
     * @return true if the computed checksum matches tag 10; false on any reject.
     */
    static boolean validateChecksum(byte[] buf, int offset, int length) {
        // Room for at least one summed byte plus the 7-byte trailer.
        if (length < 8) { return false; }

        int end = offset + length;
        int trailerStart = end - 7;  // index of '1' in "10="

        int sum = 0;
        for (int i = offset; i < trailerStart; i++) { sum += buf[i] & 0xFF; }  // byte is signed; checksum is unsigned mod 256

        long expected = parseLong(buf, end - 4, end - 1);  // the three "xxx" digits
        return expected == (sum & 0xFF);
    }
}
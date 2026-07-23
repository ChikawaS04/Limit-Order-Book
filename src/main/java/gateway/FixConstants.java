package gateway;

public final class FixConstants {

    private FixConstants() {
        //no instances
    }

    // --- Wire-level delimiters ---
    public static final byte SOH = 0x01;  // field terminator
    public static final byte EQUALS = '=';  // tag/value separator

    // --- Envelope tags (header + trailer) ---
    public static final int BEGIN_STRING = 8;
    public static final int BODY_LENGTH = 9;
    public static final int MESSAGE_TYPE = 35;
    public static final int CHECKSUM = 10;

    // --- Message body tags (in scope) ---
    public static final int CL_ORD_ID = 11;
    public static final int SIDE = 54;
    public static final int PRICE = 44;
    public static final int ORDER_QTY = 38;
    public static final int SYMBOL = 55;
    public static final int ORIG_CL_ORD_ID = 41;  // cancels only

    // --- Configured instrument ---
    // Single-symbol book. Stored as byte[] so tag 55 can be compared against a
    // buf[start..end) range without allocating a String on the parse path.
    // NOTE: a public static final byte[] is not truly immutable — the contents
    // can be mutated. Acceptable for this demo; the parse path only reads it.
    public static final byte[] SYMBOL_BYTES = {'A', 'S', 'M', 'L'};

    // Human-readable form for SLF4J reject logging at the gateway boundary.
    // Boundary-only, off the hot path.
    public static final String SYMBOL_DISPLAY = "ASML";

}

package resolutedesk.core;

/**
 * Seeds for java.util.Random built from several values.
 *
 * <p>Seeds that differ by small steps give correlated first draws in java.util.Random, which once
 * gave many cabinet members the same surname. So the values are mixed first. The mixing stays
 * below 2^53 at every step, so the browser version, where numbers are doubles, gets exactly the
 * same seeds.
 */
public final class Seeds {
    /** The Mersenne prime 2^31 - 1, the modulus of the Park-Miller generator. */
    private static final long M = 2147483647L;

    private Seeds() {
    }

    /** A well-mixed seed in [0, 2^31 - 1) from any number of values; the same values always give the same seed. */
    public static long mix(long... parts) {
        long h = 1234567L;
        for (long p : parts) {
            long v = p % M;
            if (v < 0) v += M;
            h = scramble((h + v) % M);
        }
        return h;
    }

    /** Three rounds of a Park-Miller step followed by an xor-shift; every intermediate value is below 2^48. */
    private static long scramble(long x) {
        for (int i = 0; i < 3; i++) {
            x = (x * 48271 + 11) % M;
            x ^= x >>> 15;
        }
        return x;
    }
}

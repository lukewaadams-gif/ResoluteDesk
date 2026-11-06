package resolutedesk.data;

/** The winner's popular-vote margin in each real election, which the new president's mandate is built on. */
public final class ElectionResults {
    private ElectionResults() {
    }

    /** Inauguration years, and the winner's margin in the election before each (points; negative: lost the popular vote). */
    static final int[] ER_YEAR = {1933, 1937, 1941, 1945, 1949, 1953, 1957, 1961, 1965, 1969, 1973, 1977, 1981, 1985, 1989, 1993, 1997, 2001, 2005, 2009, 2013, 2017, 2021, 2025};
    static final double[] ER_MARGIN = {17.8, 24.3, 10.0, 7.5, 4.5, 10.9, 15.4, 0.2, 22.6, 0.7, 23.2, 2.1, 9.7, 18.2, 7.7, 5.6, 8.5, -0.5, 2.4, 7.2, 3.9, -2.1, 4.5, 1.5};

    /** The margin a president who didn't really win that year is given: a narrow win. */
    public static final double WHAT_IF_MARGIN = 1.5;

    /** The real winner's margin for an inauguration year, or {@code Double.NaN} for a projected year such as 2029. */
    public static double realMargin(int year) {
        for (int i = 0; i < ER_YEAR.length; i++) if (ER_YEAR[i] == year) return ER_MARGIN[i];
        return Double.NaN;
    }
}

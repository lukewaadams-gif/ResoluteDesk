package resolutedesk.core;

/** The game calendar: day numbers, dates and their names. */
public final class Dates {
    private Dates() {
    }

    public static int CAL_Y = 2029, CAL_M = 0, CAL_D = 20;      // day 0 of the game: inauguration day

    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public static final String[] MONTH_FULL = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    public static final String[] DOW = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public static final String[] DOW3 = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    /** {year, month0, day} of a game day. */
    public static int[] dateOf(int day) {
        java.time.LocalDate d = java.time.LocalDate.of(CAL_Y, CAL_M + 1, CAL_D).plusDays(day);
        return new int[]{d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth()};
    }

    /** Day of the week of a game day, 0 = Monday. */
    public static int dowOf(int day) {
        return java.time.LocalDate.of(CAL_Y, CAL_M + 1, CAL_D).plusDays(day).getDayOfWeek().getValue() - 1;
    }

    public static String dateStr(int day) { int[] d = dateOf(day); return MONTHS[d[1]] + " " + d[2] + ", " + d[0]; }

    public static String dateLong(int day) { int[] d = dateOf(day); return DOW[dowOf(day)] + ", " + MONTH_FULL[d[1]] + " " + d[2] + ", " + d[0]; }

    public static String monthYear(int day) { int[] d = dateOf(day); return MONTH_FULL[d[1]] + " " + d[0]; }

    /** The game day of a calendar date (may be negative). */
    public static int dayOfDate(int y, int m0, int d) {
        int lo = -9000, hi = 9000;
        while (lo < hi) { int mid = lo + (hi - lo) / 2; int[] t = dateOf(mid); long k = t[0] * 10000L + t[1] * 100 + t[2], want = y * 10000L + m0 * 100 + d; if (k < want) lo = mid + 1; else hi = mid; }
        return lo;
    }
}

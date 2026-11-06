package resolutedesk.core;

import static resolutedesk.core.Dates.*;

import resolutedesk.testkit.Check;
import resolutedesk.testkit.Test;

public class DatesTest {
    @Test
    public void dayNumbersAndDatesRoundTrip() {
        for (int d = -8000; d <= 8000; d += 37) {
            int[] ymd = dateOf(d);
            Check.equal(d, dayOfDate(ymd[0], ymd[1], ymd[2]), "day " + d);
        }
    }

    @Test
    public void inaugurationDaysFallOnTheRightWeekday() {
        int y = CAL_Y, m = CAL_M, d = CAL_D;
        try {
            CAL_Y = 2029; CAL_M = 0; CAL_D = 20;                 // day numbers count from the game's first day
            Check.equal(-1461, dayOfDate(2025, 0, 20), "four years before 2029");
            Check.equal(0, dowOf(dayOfDate(2025, 0, 20)), "January 20, 2025 was a Monday (0)");
            CAL_Y = 1961;
            Check.equal(4, dowOf(0), "January 20, 1961 was a Friday (4)");
            Check.equal("Friday, January 20, 1961", dateLong(0), "the long form");
        } finally {
            CAL_Y = y; CAL_M = m; CAL_D = d;
        }
    }
}

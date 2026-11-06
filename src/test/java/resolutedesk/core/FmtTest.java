package resolutedesk.core;

import static resolutedesk.core.Fmt.*;

import resolutedesk.testkit.Check;
import resolutedesk.testkit.Test;

public class FmtTest {
    @Test
    public void thousandsGroupsDigits() {
        Check.equal("1,234,567", thousands(1234567), "seven digits");
        Check.equal("-12,000", thousands(-12000), "negative");
        Check.equal("999", thousands(999), "three digits");
    }

    @Test
    public void moneyPicksAUnit() {
        Check.equal("$2.50B", money(2.5e9), "billions");
        Check.equal("$12.3M", money(12.3e6), "tens of millions");
        Check.equal("$750K", money(750e3), "thousands");
        Check.equal("-$40", money(-40), "small negative");
    }

    @Test
    public void marginNamesTheLeadingParty() {
        Check.equal("D+3.2", margin(3.2), "Democratic lead");
        Check.equal("R+0.4", margin(-0.4), "Republican lead");
        Check.equal("TIED", margin(0.01), "a tie");
    }
}

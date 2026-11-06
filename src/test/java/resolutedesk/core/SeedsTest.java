package resolutedesk.core;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Test;

public class SeedsTest {
    @Test
    public void theSameValuesGiveTheSameSeed() {
        Check.equal(Seeds.mix(7, 3, 2026), Seeds.mix(7, 3, 2026), "repeatable");
        Check.isTrue(Seeds.mix(7, 3, 2026) != Seeds.mix(7, 2026, 3), "order matters");
    }

    /** The bug behind repeated cabinet names: neighbouring seeds gave java.util.Random the same first draws. */
    @Test
    public void neighbouringValuesGiveUnrelatedFirstDraws() {
        Set<Integer> firsts = new HashSet<Integer>();
        for (int post = 0; post < 21; post++) firsts.add(new Random(Seeds.mix(1000, 0xCAB1L, post, 0, 0)).nextInt(80));
        Check.isTrue(firsts.size() >= 15, "21 neighbouring seeds should give mostly different first draws, got " + firsts.size());
    }
}

package resolutedesk.legacy;

import static resolutedesk.legacy.LegacyData.*;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

public class LegacyTest {
    @Test
    public void theHistoriansRankingHasEveryPresident() {
        Check.equal(44, RANKED.length, "presidents in the 2021 C-SPAN survey");
        Check.equal("Abraham Lincoln", RANKED[0], "first place");
    }

    @Test
    public void aFinishedPresidencyIsScoredAndRanked() {
        Game g = Games.start(8, 1977);
        Games.playToEnd(g);
        int[] s = Legacy.scores(g);
        Check.equal(MEASURE.length, s.length, "one score per measure");
        for (int v : s) Check.between(0, 100, v, "a measure");
        Legacy.rank(g, Legacy.total(s));
        Check.between(1, 45, Legacy.rankPos, "place among the presidents");
    }
}

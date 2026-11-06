package resolutedesk.world;

import static resolutedesk.world.Countries.*;
import static resolutedesk.world.WorldData.*;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

public class CountriesTest {
    @Test
    public void thereIsNoTradeWithAnEnemyInWartime() {
        Game g = Games.start(1, 1941);
        Games.play(g, 52);                       // into 1942, after Pearl Harbor
        Check.isTrue(atWar(g, CC_DE), "at war with Germany in 1942");
        Check.equal(0.0, tradeOf(g, CC_DE), "trade with Germany");
    }

    @Test
    public void theWorldOf1961HasTheSovietUnion() {
        Game g = Games.start(1, 1961);
        Check.equal("Soviet Union", cname(g, CC_RU), "name");
        Check.equal("the Soviet Union", cnameIn(g, CC_RU), "in a sentence");
    }
}

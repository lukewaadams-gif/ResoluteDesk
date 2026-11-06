package resolutedesk.world;

import static resolutedesk.world.Countries.*;
import static resolutedesk.world.WarData.*;
import static resolutedesk.world.WorldData.*;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** Wars of choice: starting them, what they cost, how an invasion becomes an occupation, and every way out. */
public class WarsTest {
    static War war(Game g, int c) {
        for (War x : g.world.wars) if (x.enemy == c && x.kind >= 0) return x;
        throw new AssertionError("no war with " + cname(g, c));
    }

    @Test
    public void aWarCanBeStartedAgainstAlmostAnyone() {
        Game g = Games.start(5, 2025);
        Check.isTrue(Wars.cannot(g, CC_VE, WK_INVASION) == null, "Venezuela can be invaded");
        Check.isTrue(Wars.cannot(g, CC_CA, WK_STRIKES) == null, "even a friend can be struck");
        Check.isTrue(Wars.cannot(g, CC_RU, WK_INVASION) != null, "but no one plans an invasion of a nuclear power");
        Check.isTrue(Wars.cannot(g, CC_RU, WK_STRIKES) == null, "strikes on one are possible");
        Check.isTrue(Wars.cannot(g, CC_GB, WK_NOFLY) != null, "a no-fly zone needs fighting to stop");
    }

    @Test
    public void startingAWarHasHardConsequences() {
        Game g = Games.start(5, 2025);
        World w = g.world;
        double terror = w.terror, oil = g.econ.oilBase, protest = g.home.protest, nato = w.nato;
        Wars.start(g, CC_IR, WK_STRIKES);
        War x = war(g, CC_IR);
        Check.equal(WS_FIGHTING, x.stage, "fighting");
        Check.near(-100, w.rel[CC_IR], 1e-9, "Iran's government at war with us");
        Check.isTrue(w.terror > terror, "terror threat rises after striking a Middle Eastern country");
        Check.isTrue(g.econ.oilBase > oil, "oil jumps");
        Check.isTrue(g.home.protest > protest, "protests at home");
        Check.isTrue(w.nato < nato, "allies uneasy about a war Congress never authorized");
        Check.isTrue(atWar(g, CC_IR), "at war");
        Check.isTrue(Wars.start(g, CC_IR, WK_STRIKES).contains("already"), "one war of each kind at a time");
    }

    @Test
    public void attackingATreatyAllyShattersTheAlliances() {
        Game g = Games.start(5, 2025);
        double nato = g.world.nato, jp = g.world.rel[CC_JP];
        Wars.start(g, CC_CA, WK_STRIKES);
        Check.isTrue(g.world.nato < nato - 20, "NATO in crisis");
        Check.isTrue(g.world.rel[CC_JP] < jp - 25, "every ally recoils");
        Check.isTrue(war(g, CC_CA).support < 20, "and Americans won't back it");
    }

    @Test
    public void aSuccessfulInvasionBecomesAnOccupation() {
        Game g = Games.start(5, 2025);
        Wars.start(g, CC_VE, WK_INVASION);
        War x = war(g, CC_VE);
        x.authorized = true;                                   // keep Congress out of it for this test
        for (int k = 0; k < 30 && !g.world.toppled[CC_VE]; k++) Games.play(g, 1);
        Check.isTrue(g.world.toppled[CC_VE], "the government falls within months");
        Check.equal(WK_OCCUPATION, x.kind, "now an occupation");
        Check.equal(WS_FIGHTING, x.stage, "and the fighting goes on");
        Check.isTrue(x.name.startsWith("The occupation"), "renamed: " + x.name);
        Check.isTrue(x.kia > 0, "Americans have died");
        Check.isTrue(g.world.unrest[CC_VE] >= 50, "the country is in turmoil");
    }

    @Test
    public void aGreatPowerFightsForItsClient() {
        Game g = Games.start(5, 1961);
        Check.equal(CC_CN, Wars.protector(g, CC_KP), "China fights for North Korea");
        Check.equal(-1, Wars.protector(g, CC_PA), "nobody fights for Panama");
        Check.isTrue(Wars.pace(g, CC_KP, WK_INVASION) < Wars.pace(g, CC_PA, WK_INVASION) / 5, "the invasion bogs down");
        Check.isTrue(Wars.expect(g, CC_KP, WK_INVASION).contains("promised to fight"), "and the warning says so");
    }

    @Test
    public void aTimetableBringsTheTroopsHome() {
        Game g = Games.start(5, 2025);
        Wars.start(g, CC_VE, WK_STRIKES);
        War x = war(g, CC_VE);
        Wars.timetable(g, x, 4);
        Games.play(g, 2);
        Check.isTrue(x.troops < x.troops0, "troops leaving");
        Games.play(g, 3);
        Check.equal(WS_TIMETABLE, x.stage, "ended on the timetable");
        Check.isTrue(!atWar(g, CC_VE), "no longer at war");
    }

    @Test
    public void declaringVictoryEarlyCostsCredibility() {
        Game g = Games.start(5, 2025);
        Wars.start(g, CC_VE, WK_BLOCKADE);
        War x = war(g, CC_VE);
        x.front = 45;
        double deter = g.world.deter;
        Check.isTrue(Wars.declareVictory(g, x).contains("Few believe it"), "nobody is fooled");
        Check.equal(WS_DECLARED, x.stage, "over");
        Check.isTrue(g.world.deter < deter, "enemies take note");
    }

    @Test
    public void aHandoverNeedsASteadyFront() {
        Game g = Games.start(5, 2025);
        Wars.start(g, CC_VE, WK_STRIKES);
        War x = war(g, CC_VE);
        x.front = 40;
        Check.isTrue(Wars.handoff(g, x).contains("steadier"), "too soon");
        x.front = 70;
        Wars.handoff(g, x);
        Check.isTrue(x.handoff > 0, "under way");
        Check.isTrue(Wars.cannotHand(x) != null, "only once");
        Games.play(g, 9);
        Check.isTrue(x.stage == WS_HANDED || x.stage == WS_WON, "handed over (or won first): " + WAR_STAGE[x.stage]);
    }

    @Test
    public void congressAuthorizesForceAgainstEnemiesNotFriends() {
        Game g = Games.start(5, 2025);
        Check.isTrue(Wars.authorizeOdds(g, CC_IR) > 0.2, "Iran: a real chance " + Wars.authorizeOdds(g, CC_IR));
        Check.isTrue(Wars.authorizeOdds(g, CC_GB) < 0.01, "Britain: none");
        g.world.authorized[CC_IR] = Math.max(1, g.week);
        Check.isTrue(Wars.authorize(g, CC_IR).contains("already"), "once is enough");
        Wars.start(g, CC_IR, WK_STRIKES);
        Check.isTrue(war(g, CC_IR).authorized, "the war starts authorized");
    }

    @Test
    public void anUnauthorizedWarFacesTheWarPowersResolution() {
        Game g = Games.start(5, 2025);
        Wars.start(g, CC_VE, WK_BLOCKADE);
        War x = war(g, CC_VE);
        x.support = 30;
        Games.play(g, 10);
        Check.isTrue(x.wpVoted, "Congress has voted");
    }

    @Test
    public void unpopularWarsDragOnThePresident() {
        double peace = 0, war = 0;
        for (int seed = 1; seed <= 3; seed++) {
            Game a = Games.start(seed, 2025), b = Games.start(seed, 2025);
            Wars.start(b, CC_MX, WK_INVASION);
            war(b, CC_MX).authorized = true;                   // Congress can't end it early
            Games.play(a, 52);
            Games.play(b, 52);
            peace += a.approval / 3;
            war += b.approval / 3;
        }
        Check.isTrue(war < peace - 3, "a year after invading Mexico: " + Math.round(peace) + " without the war, " + Math.round(war) + " with it");
    }

    @Test
    public void aWarEndsInTheRecord() {
        Game g = Games.start(5, 2025);
        Wars.start(g, CC_VE, WK_STRIKES);
        War x = war(g, CC_VE);
        x.front = 99.5;
        x.drift = 5;
        for (int k = 0; k < 10 && x.stage == WS_FIGHTING; k++) Games.play(g, 1);
        Check.isTrue(x.stage != WS_FIGHTING, "over");
        boolean recorded = false;
        for (String r : g.world.record) if (r.contains("air war against Venezuela")) recorded = true;
        Check.isTrue(recorded, "in the record");
    }
}

package resolutedesk.world;

import static resolutedesk.world.Countries.*;
import static resolutedesk.world.StatecraftData.*;
import static resolutedesk.world.WorldData.*;

import java.util.HashSet;
import java.util.Set;
import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** Statecraft: every country's objectives, the requirements their steps name, and the tools short of war. */
public class StatecraftTest {
    @Test
    public void everyObjectiveRowIsWellFormed() {
        Set<String> ids = new HashSet<String>();
        for (ObjectiveDef d : Objectives.defs()) {
            Check.isTrue(ids.add(d.id), "objective ids are unique: " + d.id);
            Check.isTrue(d.generic() || ctIdx(d.who) >= 0, d.id + " names a country");
            Check.isTrue(d.from <= d.to, d.id + " years");
            Check.between(1, 4, d.steps.length, d.id + " steps");
            for (String[] st : d.steps) {
                Check.equal(7, st.length, d.id + " step fields");
                Check.isTrue("dmij".indexOf(st[1]) >= 0 && st[1].length() == 1, d.id + " odds kind " + st[1]);
                Check.between(0, 1, Double.parseDouble(st[2]), d.id + " base odds");
            }
        }
        Check.isTrue(ids.size() >= 70, "a big table of objectives: " + ids.size());
    }

    @Test
    public void everyCountryHasTwoToFourObjectivesInEveryEra() {
        for (int year : new int[]{1933, 1953, 1965, 1985, 2001, 2025}) {
            Game g = Games.start(3, year);
            for (int c = 0; c < NC; c++) {
                if (gone(g, c)) continue;
                Check.between(2, 4, Objectives.of(g, c).size(), cname(g, c) + " in " + year);
            }
        }
    }

    @Test
    public void everyObjectiveCanBeTriedOrSaysWhatItNeeds() {
        Game g = Games.start(3, 2025);
        for (Objective o : g.world.objectives) {
            Check.isTrue(Objectives.name(g, o).indexOf('{') < 0, "names fill in the country: " + o.id);
            String no = Objectives.cannot(g, o);
            if (no == null) Check.between(0, 1, Objectives.odds(g, o), o.id + " odds");
            else Check.isTrue(no.length() > 10, o.id + " explains itself: " + no);
        }
    }

    @Test
    public void aStepWaitsForTheToolsItNeeds() {
        Game g = Games.start(3, 2025);
        g.world.milAid[CC_UA] = 0;
        Check.equal("First: military aid to Ukraine at substantial or more.", Objectives.unmet(g, CC_UA, "milaid>=2"), "aid not yet given");
        g.world.milAid[CC_UA] = 2;
        Check.isTrue(Objectives.unmet(g, CC_UA, "milaid>=2") == null, "met once the aid flows");
        g.world.troopsIn[CC_PL] = 0;
        Check.isTrue(Objectives.unmet(g, CC_UA, "troops@PL>=5") != null, "a requirement about another country");
        Statecraft.setTroops(g, CC_PL, 2);
        Check.isTrue(Objectives.unmet(g, CC_UA, "troops@PL>=5") == null, "a brigade in Poland");
    }

    @Test
    public void succeedingAtEveryStepAchievesTheObjective() {
        Game g = Games.start(3, 2025);
        Objective o = null;
        for (Objective x : Objectives.of(g, CC_GB)) if (Objectives.cannot(g, x) == null) { o = x; break; }
        Check.isTrue(o != null, "something to work on with Britain");
        int done = g.world.done;
        for (int tries = 0; tries < 200 && o.status != 1; tries++) {
            o.tryAfter = 0;
            if (o.status == 2) o.status = 0;
            Objectives.attempt(g, o);
        }
        Check.equal(1, o.status, "achieved");
        Check.equal(done + 1, g.world.done, "counted");
    }

    @Test
    public void militaryAidWarmsAFriendAndAngersItsRival() {
        Game g = Games.start(3, 2025);
        World w = g.world;
        w.milAid[CC_UA] = 0;
        double ua = w.rel[CC_UA], ru = w.rel[CC_RU], fiscal = g.econ.fiscal;
        Statecraft.setMilAid(g, CC_UA, 3);
        Check.isTrue(w.rel[CC_UA] > ua, "Kyiv grateful");
        Check.isTrue(w.rel[CC_RU] < ru, "Moscow angry");
        Check.isTrue(g.econ.fiscal > fiscal, "it costs money");
    }

    @Test
    public void adversariesWontHostAmericanTroops() {
        Game g = Games.start(3, 2025);
        Check.isTrue(Statecraft.setTroops(g, CC_IR, 2).contains("won't host"), "Iran refuses");
        Check.near(0, g.world.troopsIn[CC_IR], 1e-9, "no troops in Iran");
    }

    @Test
    public void recognizingTaiwanIsAnActOfHostilityToBeijing() {
        Game g = Games.start(3, 2025);
        double cn = g.world.rel[CC_CN], twt = g.world.twt;
        Statecraft.setTies(g, CC_TW, 2);
        Check.isTrue(g.world.rel[CC_CN] < cn - 50, "Beijing's fury");
        Check.isTrue(g.world.twt > twt + 20, "the Strait heats up");
    }

    @Test
    public void treatiesAndTradeDealsGetHarderWithDistance() {
        Game g = Games.start(3, 2025);
        Check.isTrue(Statecraft.tradeOdds(g, CC_GB) > Statecraft.tradeOdds(g, CC_BR), "trade with Britain easier than with Brazil");
        Check.isTrue(Statecraft.tradeOdds(g, CC_CN) < 0.01, "no trade agreement with an adversary");
        Check.isTrue(Statecraft.treatyOdds(g, CC_UA) < 0.05, "no treaty with a country at war with Russia");
        Check.isTrue(Statecraft.defensePact(g, CC_ZA).contains("isn't interested"), "a cool relationship can't become an alliance");
        Check.isTrue(Statecraft.defensePact(g, CC_IR).contains("adversary"), "or an enemy");
    }

    @Test
    public void aCovertProgramResolvesOnSchedule() {
        Game g = Games.start(3, 2025);
        World w = g.world;
        Statecraft.startCovert(g, CC_IR, CV2_CYBER);
        Check.equal(CV2_CYBER + 1, w.cvProg[CC_IR], "under way");
        Check.isTrue(Statecraft.startCovert(g, CC_IR, CV2_INFLUENCE).contains("already"), "one program at a time");
        Games.play(g, CV2_WEEKS[CV2_CYBER] + 1);
        Check.equal(0, w.cvProg[CC_IR], "resolved");
        Check.isTrue(Statecraft.startCovert(g, CC_GB, CV2_COUP).contains("treaty ally"), "no coups against allies");
    }

    @Test
    public void aToppledGovernmentBecomesAPartnerInTurmoil() {
        Game g = Games.start(3, 2025);
        World w = g.world;
        double unrest = w.unrest[CC_VE];
        Statecraft.topple(g, CC_VE, "a test");
        Check.isTrue(w.toppled[CC_VE], "toppled");
        Check.equal(AL_PARTNER, align(g, CC_VE), "a partner now");
        Check.isTrue(w.unrest[CC_VE] > unrest + 30, "turmoil");
    }
}

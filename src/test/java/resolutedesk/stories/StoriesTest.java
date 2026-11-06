package resolutedesk.stories;

import static resolutedesk.executive.Posts.ADV_LABEL;
import static resolutedesk.sim.DecisionKinds.D_STORY;
import static resolutedesk.stories.StoryData.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import resolutedesk.model.Game;
import resolutedesk.model.News;
import resolutedesk.sim.Decision;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** The era's news and the storylines that grow out of it. */
public class StoriesTest {
    static int line(String id) {
        for (int i = 0; i < Storylines.defs().size(); i++) if (Storylines.defs().get(i).id.equals(id)) return i;
        throw new AssertionError("no storyline " + id);
    }

    @Test
    public void thereAreMoreThanFiveHundredStoriesAndEachIsWellFormed() {
        Check.isTrue(Stories.defs().size() >= 500, "500+ stories: " + Stories.defs().size());
        Set<String> heads = new HashSet<String>();
        for (StoryDef d : Stories.defs()) {
            Check.isTrue(heads.add(d.head), "headlines are unique: " + d.head);
            Check.between(1933, 2037, d.from, d.head + " from");
            Check.between(d.from, 2037, d.to, d.head + " to");
            Check.isTrue(d.head.indexOf('"') < 0 && d.body.indexOf('"') < 0, "no one is quoted: " + d.head);
            Check.isTrue(!d.body.isEmpty(), d.head + " has a story");
            for (String tok : d.fx.split(" ")) if (tok.startsWith("sal:")) Check.isTrue(Stories.issue(tok.substring(4, tok.indexOf('='))) >= 0, "an issue: " + tok);
        }
    }

    @Test
    public void everyYearHasPlentyOfStories() {
        for (int y = 1933; y <= 2036; y++) Check.isTrue(Stories.inYear(y) >= 60, y + " has " + Stories.inYear(y));
    }

    @Test
    public void storiesRunOnceAndInTheirOwnYears() {
        Game g = Games.start(4, 1961);
        Games.play(g, 156);
        Check.isTrue(g.stories.ran >= 40, "a steady stream of stories: " + g.stories.ran);
        Set<String> seen = new HashSet<String>();
        for (News n : g.news) if (n.section != null) Check.isTrue(seen.add(n.head), "never twice: " + n.head);
        for (int i = 0; i < g.stories.used.length; i++) {
            if (!g.stories.used[i]) continue;
            StoryDef d = Stories.defs().get(i);
            Check.isTrue(d.to >= 1961 && d.from <= 1964, d.head + " belongs to " + d.from + "-" + d.to);
        }
    }

    @Test
    public void conditionsReadTheStateOfTheCountry() {
        Game g = Games.start(4, 2025);
        g.econ.unemp = 9;
        Check.isTrue(Stories.holds(g, "unemp>8"), "high unemployment");
        Check.isTrue(!Stories.holds(g, "unemp>8,unemp<8.5"), "every clause must hold");
        Check.isTrue(Stories.holds(g, "year>2024,year<2026"), "the year");
        Check.isTrue(Stories.holds(g, ""), "no conditions");
    }

    @Test
    public void storiesCanMakeVotersCareMoreAboutAnIssue() {
        Game g = Games.start(4, 2025);
        int crime = Stories.issue("CRIME");
        double before = g.issueSal[crime];
        Stories.fx(g, "sal:CRIME=0.2");
        Check.near(before + 0.2, g.issueSal[crime], 1e-9, "salience of crime");
    }

    @Test
    public void everyStorylineIsWellFormed() {
        Check.isTrue(Storylines.defs().size() >= 15, "storylines: " + Storylines.defs().size());
        for (StorylineDef d : Storylines.defs()) {
            Check.between(3, 5, d.stages(), d.id + " stages");
            for (int i = 0; i < d.stages(); i++) {
                Check.between(2, 4, d.options[i].length, d.id + " options");
                Check.between(2, 8, d.weeks[i], d.id + " weeks");
                for (String[] o : d.options[i]) {
                    boolean adviser = false;
                    for (String a : ADV_LABEL) if (a.equals(o[1])) adviser = true;
                    Check.isTrue(adviser, d.id + ": an adviser " + o[1]);
                    Check.between(0, 1, Double.parseDouble(o[3]), d.id + " odds");
                }
            }
        }
    }

    @Test
    public void aStorylineStartsWithADecision() {
        Game g = Games.start(4, 2025);
        g.inbox.clear();
        Storyline l = Storylines.start(g, line("strike"));
        Check.equal(SL_ACTIVE, l.status, "under way");
        Decision d = g.inbox.get(g.inbox.size() - 1);
        Check.equal(D_STORY, d.kind, "a storyline decision");
        Check.isTrue(d.story == l, "about this storyline");
        Check.equal(Storylines.def(l).options[0].length, d.opts.length, "every option offered");
        Check.isTrue(!d.title.contains("{"), "blanks filled: " + d.title);
    }

    @Test
    public void aStoryThatCoolsFades() {
        Game g = Games.start(4, 2025);
        Storyline l = Storylines.start(g, line("strike"));
        g.inbox.clear();
        for (int k = 0; k < 20 && l.status == SL_ACTIVE; k++) { l.heat = 10; Games.play(g, 1); }
        Check.equal(SL_FADED, l.status, "faded");
        Check.equal(SL_REST, g.stories.cool[l.def], "and rests before it can start again");
    }

    @Test
    public void aStoryKeptHotGrowsAndTakesOver() {
        Game g = Games.start(4, 2025);
        Storyline l = Storylines.start(g, line("bankrun"));
        int stages = Storylines.def(l).stages(), seen = 0;
        for (int k = 0; k < 60 && l.status == SL_ACTIVE; k++) { l.heat = 99; Games.play(g, 1); seen = Math.max(seen, l.stage); }
        Check.equal(SL_RUNAWAY, l.status, "it took over");
        Check.equal(stages - 1, seen, "after every stage");
        boolean recorded = false;
        for (String m : g.milestones) if (m.contains("a run on the banks took over")) recorded = true;
        Check.isTrue(recorded, "in the record");
    }

    @Test
    public void theHandsOffPresidentPicksWhatCoolsAStoryMost() {
        Game g = Games.start(4, 2025);
        for (int i = 0; i < Storylines.defs().size(); i++) {
            g.inbox.clear();
            Storyline l = Storylines.start(g, i);
            Decision d = g.inbox.get(g.inbox.size() - 1);
            int k = Storylines.auto(g, d);
            String[][] o = Storylines.def(l).options[0];
            Check.between(0, o.length - 1, k, Storylines.def(l).id + " choice");
            Check.isTrue(Double.parseDouble(o[k][4]) <= 0, Storylines.def(l).id + ": the choice cools it when it works");
            Storylines.end(g, l);
        }
    }

    @Test
    public void aSavedStorylineCarriesOn() throws Exception {
        Game g = Games.start(4, 2025);
        Storyline l = Storylines.start(g, line("aijobs"));
        Games.play(g, 3);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(g);
        out.close();
        Game copy = (Game) new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        Games.play(g, 12);
        Games.play(copy, 12);
        Storyline a = g.stories.lines.get(g.stories.lines.indexOf(l)), b = copy.stories.lines.get(g.stories.lines.indexOf(l));
        Check.equal(a.status, b.status, "status");
        Check.equal(a.stage, b.stage, "stage");
        Check.near(a.heat, b.heat, 1e-9, "heat");
        Check.equal(g.stories.ran, copy.stories.ran, "stories run");
    }
}

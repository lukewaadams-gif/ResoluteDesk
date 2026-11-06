package resolutedesk.transition;

import static resolutedesk.data.Candidates.candById;
import static resolutedesk.data.Eras.era;
import static resolutedesk.executive.Posts.NPOST;
import static resolutedesk.executive.Posts.NSTAFF;
import static resolutedesk.executive.Posts.P_DEF;
import static resolutedesk.executive.Posts.P_STATE;
import static resolutedesk.executive.Posts.postOn;
import static resolutedesk.sim.Rosters.defaultVp;
import static resolutedesk.sim.Rosters.eraRoster;
import static resolutedesk.transition.TransitionData.JOB_STAFF;
import static resolutedesk.transition.TransitionData.NJOB;
import static resolutedesk.transition.TransitionData.PK_REAL;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.executive.Cabinet;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;
import resolutedesk.sim.Setup;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** The transition: shortlists, the team's work, and Inauguration Day. */
public class TransitionTest {
    static Game elect(long seed, int year) {
        Games.start(seed, year);                       // loads the electorate and the era
        Settings s = new Settings();
        s.year = year;
        s.seed = seed;
        s.presId = eraRoster(era(year)).get(0).id;
        s.vpId = defaultVp(era(year), candById(s.presId));
        s.transition = true;
        return Setup.newGame(s);
    }

    @Test
    public void everyJobHasThreeToFiveProspects() {
        Game g = elect(5, 2029);
        for (int job = 0; job < NJOB; job++) {
            if (job < JOB_STAFF && !postOn(job)) continue;
            Check.between(3, 5, Transitions.prospects(g, job).size(), Transitions.jobTitle(job) + " shortlist");
        }
    }

    @Test
    public void noTwoProspectsShareASurname() {
        Game g = elect(5, 2029);
        List<String> names = new ArrayList<String>();
        names.add(g.pres.last);
        if (g.vp != null) names.add(g.vp.last);
        for (Prospect q : g.trans.prospects) names.add(Cabinet.surname(q.person.name));
        Check.distinct(names, "surnames in the transition");
    }

    @Test
    public void theRealCabinetIsOneChoiceIn2025() {
        Game g = elect(5, 2025);
        boolean found = false;
        for (Prospect q : Transitions.prospects(g, P_STATE)) {
            if (q.kind != PK_REAL) continue;
            found = q.person.name.equals("Marco Rubio");
            Check.isTrue(q.weaknesses.isEmpty() && q.person.baggageWeight == 0, "real people come with strengths only");
        }
        Check.isTrue(found, "the real nominee for State is on the list");
        for (Prospect q : g.trans.prospects) if (q.kind != PK_REAL) Check.isTrue(!q.person.real, "everyone else is fictional");
    }

    @Test
    public void theTransitionRunsFromElectionDayToTheInauguration() {
        Check.equal(11, elect(5, 2025).trans.weeks, "Nov 6, 2024 to Jan 20, 2025");
        Check.equal(17, elect(5, 1933).trans.weeks, "Nov 9, 1932 to Mar 4, 1933");
    }

    @Test
    public void anInterviewRevealsTheTruth() {
        Game g = elect(5, 2029);
        Prospect q = Transitions.prospects(g, P_DEF).get(0);
        Transitions.act(g, q, Transitions.TA_INTERVIEW);
        Check.near(q.person.skill, q.skillSeen, 1e-9, "skill");
        Check.near(q.person.loyalty, q.loyaltySeen, 1e-9, "loyalty");
        Check.near(1, g.trans.hoursUsed, 1e-9, "an hour of the team's week");
    }

    @Test
    public void backgroundChecksTakeTwoWeeksAndFourAtATime() {
        Game g = elect(5, 2029);
        List<Prospect> l = g.trans.prospects;
        for (int i = 0; i < 4; i++) Transitions.act(g, l.get(i), Transitions.TA_VET);
        Check.isTrue(Transitions.cannot(g, l.get(4), Transitions.TA_VET) != null, "a fifth has to wait");
        Transitions.week(g);
        Check.isTrue(!Transitions.vetted(g, l.get(0)), "one week in, still checking");
        Transitions.week(g);
        Check.isTrue(Transitions.vetted(g, l.get(0)), "done after two");
    }

    @Test
    public void inaugurationDayFillsEveryJob() {
        Game g = elect(5, 2029);
        Prospect pick = Transitions.prospects(g, P_STATE).get(1);
        Transitions.act(g, pick, Transitions.TA_ANNOUNCE);
        while (!Transitions.lastWeek(g)) Transitions.week(g);
        Setup.inaugurate(g);
        Check.isTrue(g.ex.nominee[P_STATE] == pick.person, "your pick is the nominee");
        for (int s = 0; s < NSTAFF; s++) Check.isTrue(g.ex.staff[s] != null, "staff job " + s);
        for (int k = 0; k < NPOST; k++) if (postOn(k)) Check.isTrue(g.ex.nominee[k] != null && g.ex.voteAt[k] >= 0, "a nominee for post " + k + " on the Senate's calendar");
        Games.play(g, 30);
        Check.isTrue(g.ex.nominee[P_STATE] == null, "the Senate has voted on State");
    }
}

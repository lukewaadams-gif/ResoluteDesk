package resolutedesk.io;

import static resolutedesk.core.Dates.*;
import static resolutedesk.data.Eras.*;
import static resolutedesk.data.Names.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.Schedule.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Theme.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import resolutedesk.ResoluteDesk;
import resolutedesk.congress.Favor;
import resolutedesk.congress.Hill;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Person;
import resolutedesk.history.History;
import resolutedesk.home.HomeFront;
import resolutedesk.legacy.Legacy;
import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.opinion.Elect;
import resolutedesk.opinion.Groups;
import resolutedesk.policy.Policies;
import resolutedesk.sim.Decision;
import resolutedesk.sim.WatchPlan;
import resolutedesk.stories.Stories;
import resolutedesk.world.Diplo;
import resolutedesk.world.Statecraft;

/** Saving and loading games, and bringing saves from earlier versions up to date. */
public final class SaveGame {
    public static final String AUTO = "autosave", SLOT = "slot1";

    public static File dir() {
        File d = new File(System.getProperty("user.home"), ".resolutedesk");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static boolean exists(String name) { return new File(dir(), name + ".sav").exists(); }

    public static String describe(String name) {
        try {
            File f = new File(dir(), name + ".txt");
            if (!f.exists()) return "";
            BufferedReader r = new BufferedReader(new FileReader(f));
            String s = r.readLine();
            r.close();
            return s == null ? "" : s;
        } catch (Throwable t) { return ""; }
    }

    public static void save(ResoluteDesk u, String name) {
        Game g = u.game;
        if (g == null) return;
        try {
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(dir(), name + ".sav"))));
            o.writeObject(g);
            o.close();
            FileWriter w = new FileWriter(new File(dir(), name + ".txt"));
            w.write(g.pres.last + " (" + PARTY_SHORT[g.party] + "), " + monthYear(g.day()));
            w.close();
            if (name.equals(SLOT)) u.toast("Game saved.", C_GOOD);
        } catch (Throwable t) {
            if (name.equals(SLOT)) u.toast("Could not save: " + t.getMessage(), C_BAD);
        }
    }

    public static void load(ResoluteDesk u, String name) {
        try {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(dir(), name + ".sav"))));
            Game g = (Game) in.readObject();
            in.close();
            resume(u, g);
        } catch (Throwable t) {
            u.info("Could not load", "The saved game could not be read (" + t.getClass().getSimpleName() + ").");
        }
    }

    /** Fills in what a save from an earlier update doesn't have. */
    static void upgrade(Game g) {
        // games started in the browser before the seed fix carry a seed too big for exact arithmetic there
        if (g.set.seed > 2000000011L || g.set.seed < 0) g.set.seed = Math.abs(g.set.seed % 2000000011L);
        if (g.inbox == null) g.inbox = new ArrayList<Decision>();
        if (g.era.midSeats == null) g.era.midSeats = era(g.era.year).midSeats;
        if (g.cong == null) Hill.setup(g);
        if (g.cong.favors == null) { g.cong.favors = new ArrayList<Favor>(); g.cong.appointments = 4; g.cong.nextAsk = g.week + 6; }
        if (g.ex == null) {
            Cabinet.setup(g);
            for (int k = 0; k < NPOST; k++) if (g.ex.nominee[k] != null) { Person p = g.ex.nominee[k]; p.confirmed = true; p.since = g.week; g.ex.post[k] = p; g.ex.nominee[k] = null; g.ex.voteAt[k] = -1; }
            g.ex.nextPardon = g.week + 8; g.ex.nextProbe = g.week + 14;
        }
        if (g.world == null) Diplo.setup(g);
        if (g.home == null) HomeFront.setup(g);
        Policies.upgrade(g);
        if (g.world.name == null) History.world(g, g.era.year, false);
        if (g.world.wars == null) History.setup(g);
        if (g.world.milAid == null) Statecraft.upgrade(g);
        Stories.upgrade(g);
        if (g.plan == null) g.plan = new WatchPlan();
        if (g.termWeeks <= 0 || g.term == 1 && g.termWeeks == 208 && g.era.inM != 0) g.termWeeks = termEnd(g);
        if (g.milestones == null) g.milestones = new ArrayList<String>();
        if (g.histOk == null) g.histOk = new ArrayList<String>();
        if (g.econ.gasRef == 0) { g.econ.gasRef = 3.1; g.econ.uRef0 = 4.5; g.econ.piRef0 = 2.5; g.econ.uRef = 4.5; g.econ.piRef = 2.5; }
        if (g.minApproval == 0) g.minApproval = 100;
    }

    static void resume(ResoluteDesk u, Game g) {
        if (g.version < Game.VERSION) { u.info("An older save", "This game was saved by an earlier version and can't be continued in this one."); return; }
        Elect.init();
        Groups.init();
        if (g.era.inD == 0) { Era e = era(g.era.year); g.era.inM = e.inM; g.era.inD = e.inD; g.era.termOf = e.termOf; g.era.fedCred = e.fedCred; g.era.labels = e.labels; }
        applyEra(g.era);
        applyYear(yearNow(g));
        GEN_YEAR = yearNow(g);
        if (g.re != null) setEv(g.re.year);
        upgrade(g);
        g.dirty = true;
        u.game = g;
        u.setup = g.set;
        if (g.over || g.removed) { Legacy.lastScores = null; Legacy.epi = null; u.go(SC_LEGACY); return; }
        if (g.trans != null && !g.trans.done) { u.go(SC_TRANS); u.toast("Welcome back to the transition.", C_ACCENT); return; }
        u.go(SC_OVAL);
        u.toast("Welcome back. " + dateLong(g.day()) + ".", C_ACCENT);
    }
}

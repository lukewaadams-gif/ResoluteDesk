package resolutedesk.executive;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.VicePresData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;

import java.util.Random;
import resolutedesk.congress.Bloc;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;

/** The vice president's missions: tours, task forces, the Senate, fundraising and trips abroad. */
public final class VicePres {
    public static void start(Game g, int mission, int arg) {
        Exec x = g.ex;
        x.vpMission = mission; x.vpArg = arg; x.vpUntil = VM_WEEKS[mission] > 0 ? g.week + VM_WEEKS[mission] : -1;
        if (mission != VM_NONE) { x.vpMissions++; g.log(NK_INFO, "Vice President " + g.vp.last + (mission == VM_TASK ? " will lead the administration's work on " + PISSUE[arg].toLowerCase() : mission == VM_TOUR ? " heads out on a swing-state tour" : mission == VM_MONEY ? " hits the fundraising circuit" : mission == VM_ABROAD ? " leaves on a trip to reassure allies" : " takes charge of the White House's Senate operation"), null, 0.8); }
    }

    static void week(Game g) {
        Exec x = g.ex;
        Cand v = g.vp;
        if (v == null) return;
        Random r = g.rng;
        double k = (v.a[A_CHA] + v.a[A_EST]) / 140.0;
        switch (x.vpMission) {
            case VM_TOUR: { String[] sw = {"PA", "MI", "WI", "AZ", "GA", "NC", "NV"}; int s = stateIdx(sw[g.week % sw.length]); if (s >= 0) g.stateBump[s] += 1.2 * k; g.perf += 0.01; break; }
            case VM_TASK: if (x.vpArg >= 0) { g.issueAdj[x.vpArg] = clamp(g.issueAdj[x.vpArg] + 0.015 * k, -1.5, 1.5); g.command[x.vpArg] = Math.min(100, g.command[x.vpArg] + 0.6); } break;
            case VM_SENATE: for (Bloc b : g.cong.blocs) if (b.chamber == SENATE) b.rel = Math.min(100, b.rel + 0.25 * k); break;
            case VM_MONEY: g.funds += 1.2 * v.a[A_BIG] / 70; break;
            case VM_ABROAD: g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] + 0.012 * k, -1.5, 1.5); break;
            default: break;
        }
        if (x.vpMission != VM_NONE && r.nextDouble() < (100 - v.a[A_DIS]) / 2600.0) {
            g.log(NK_BAD, "Vice President " + v.last + "'s off-the-cuff remarks make headlines", "The White House says the vice president was making a broader point.", 1.0);
            g.perf -= 0.25;
        }
        // a vice president with nothing to do grows restless
        x.vpLoyalty = clamp(x.vpLoyalty + (x.vpMission == VM_NONE ? -0.15 : 0.1) + (g.approval - 45) * 0.01, 0, 100);
        if (x.vpUntil >= 0 && g.week >= x.vpUntil) { g.log(NK_INFO, "Vice President " + v.last + " wraps up: " + VM_NAME[x.vpMission].toLowerCase(), null, 0.4); x.vpMission = VM_NONE; x.vpUntil = -1; }
    }
}

package resolutedesk.executive;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.sim.DecisionKinds.*;

import resolutedesk.congress.Hill;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;

/** Congress pushing back on executive action. */
public final class Congressional {
    static boolean oppBoth(Game g) { return !Hill.majority(g, HOUSE, g.party) && !Hill.majority(g, SENATE, g.party); }

    /** The Congressional Review Act: a new rule can be overturned by a simple majority, but you can veto. */
    static void cra(Game g, Action a) {
        if (!oppBoth(g) || a.lean != g.party || g.rng.nextDouble() > 0.6) return;
        Decision d = Decisions.add(g, D_CRA, "Congress votes to overturn your rule", "Under the Congressional Review Act, both chambers have passed a resolution to strike down " + a.name.toLowerCase() + ". It needs your signature, or two-thirds to override a veto.",
                new String[]{"Veto the resolution", "Sign it and let the rule go"},
                new String[]{"Legislative director: \"They don't have two-thirds. Veto it.\"", "Chief of staff: \"It's not worth a fight we'll have to explain.\""});
        d.action = a;
    }

    static void emergencyVote(Game g, Action a) {
        if (!oppBoth(g) && (Hill.majority(g, SENATE, g.party) || g.rng.nextDouble() > 0.4)) return;
        if (g.rng.nextDouble() > 0.65) return;
        Decision d = Decisions.add(g, D_EMERG_END, "Congress votes to end your emergency", "Both chambers have passed a joint resolution ending the national emergency over " + EMERG_NAME[a.def] + ".",
                new String[]{"Veto it", "Accept it and end the emergency"},
                new String[]{"Counsel: \"The law lets you veto. They can't override.\"", "Chief of staff: \"A veto keeps the story going another month.\""});
        d.action = a;
    }
}

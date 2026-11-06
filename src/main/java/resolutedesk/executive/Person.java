package resolutedesk.executive;

import static resolutedesk.executive.Posts.*;

import java.io.Serializable;
import resolutedesk.model.Cand;

/** Someone who works for the president: a staffer, a cabinet secretary or a nominee. */
public final class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name, background, rosterId;
    public int party, role = -1, post = -1, trait, since, nominated = -1;
    public boolean real, acting, confirmed;
    public double skill = 60, loyalty = 60, morale = 70, ideo;       // ideo: -1 (left) to 1 (right)
    public Cand face;
    public int lastVote = -1, voteYes, voteNo;
    // from the transition: what a background check found (or would have), and goodwill from meeting senators
    public String baggage;
    public double baggageWeight;
    public boolean baggageKnown;
    public double senate;

    public String title() { return role >= 0 ? STAFF_TITLE[role] : post >= 0 ? (acting ? "Acting " + lcTitle(POST_TITLE[post]) : POST_TITLE[post]) : ""; }

    /** How much good this person does: skill, tempered by morale. */
    double q() { return skill / 100 * (0.7 + 0.3 * morale / 100); }
}

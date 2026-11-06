package resolutedesk.executive;

import java.io.Serializable;

/** A lawsuit against an action of the administration, on its way through the courts. */
public final class Case implements Serializable {
    private static final long serialVersionUID = 1L;
    public int action, stage, decideWeek, votesFor, votesAgainst;
    public boolean blocked, done, won;
    public String plaintiff, note = "";
}

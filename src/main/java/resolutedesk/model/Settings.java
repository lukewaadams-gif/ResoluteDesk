package resolutedesk.model;

import java.io.Serializable;

/** The choices made before a game starts: the era, the president, the running mate, the scenario and the difficulty. */
public final class Settings implements Serializable {
    private static final long serialVersionUID = 1L;
    public int year = 2029;                    // inauguration year
    public int scenario;
    public int difficulty = 1;                 // 0 easy, 1 normal, 2 hard
    public String presId, vpId;
    public Cand custom;                        // a president you created
    public boolean tutorial = true;
    public double cApproval = Double.NaN, cUnemp = Double.NaN, cInfl = Double.NaN, cGdp = Double.NaN;
    public int cHouse = -1, cSenate = -1;      // the president's party's seats, or -1 for the era's
    public long seed;
    public String bgLine;                      // a Battleground 2028 Hall of Fame line the president came from
}

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
    public boolean transition;                 // play the ten weeks from Election Day to the inauguration
    public double cApproval = Double.NaN, cUnemp = Double.NaN, cInfl = Double.NaN, cGdp = Double.NaN;
    public int cHouse = -1, cSenate = -1;      // the president's party's seats, or -1 for the era's
    public double cMargin = Double.NaN;        // the popular-vote margin won by, or NaN for the era's
    // the world a projected era inherits (NaN or -1 for the era's own)
    public double cDeficit = Double.NaN;       // % of GDP
    public int cCourtR = -1;                   // justices appointed by Republican presidents
    public double cTaiwan = Double.NaN, cUkraine = Double.NaN, cMideast = Double.NaN;   // tension, 0 (calm) to 100 (war or the brink of it)
    public long seed;
    public String bgLine;                      // a Battleground 2028 Hall of Fame line the president came from
}

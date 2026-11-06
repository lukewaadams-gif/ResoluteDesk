package resolutedesk.io;

import resolutedesk.model.Cand;

/**
 * Brings in the winner of a Battleground 2028 game: their face, record and running mate, the mandate,
 * the new Congress and the economy they inherit. The desktop version reads Battleground's save files
 * (in a folder named .battleground2028 in your home folder); the browser version reads its saves in this browser.
 */
public final class BgPres {
    Cand pres, vp;
    int house = -1, senate = -1;        // the president's party's seats
    double unemp = Double.NaN, infl = Double.NaN, gdp = Double.NaN;
    String desc, slot;
    boolean won, mine, independent;
}

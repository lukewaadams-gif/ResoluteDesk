package resolutedesk.opinion;

import static resolutedesk.data.StateData.*;
import static resolutedesk.opinion.Demographics.*;

/**
 * National approval follows the economy, prices, events and the honeymoon, squeezed by partisanship.
 * Below it, the electorate's 96 voter cells in every state give approval by state and by group.
 */
public final class Groups {
    static float[][][] mem;            // [group][state][cell]
    static volatile boolean ready;

    public static synchronized void init() {
        if (ready) return;
        Elect.init();
        mem = new float[NG][NS][NCELL];
        for (int gr = 0; gr < NG; gr++) {
            int o = GROUP_OV[gr];
            for (int s = 0; s < NS; s++) for (int c = 0; c < NCELL; c++) mem[gr][s][c] = (float) (o >= 0 ? Elect.ov[s][c][o] : inGroup(gr, c));
        }
        ready = true;
    }
}

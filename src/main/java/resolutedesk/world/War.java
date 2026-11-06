package resolutedesk.world;

import java.io.Serializable;

/** A war the United States fights: the front, the troops and strategy, the casualties, support at home and how it ends. */
public final class War implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id, name;
    public int enemy = -1, ally = -1;
    public int stage;                          // WS_* in WarData: 0 not begun, 2 fighting, then how it ended (won, lost, agreed, timetable, handed over, declared)
    public int strategy = 1;                   // 0 hold the line, 1 balanced, 2 on the offensive, 3 seek a settlement
    public double front = 50, troops, kia, support = 60, talks, weeks;
    public double need, drift, lethal, cost, stakes;
    public int started = -1, ended = -1;
    // wars of choice (version 2): what kind of war, whether Congress authorized it, and how it is being ended
    public int kind = -1;                      // -1 a war of history; otherwise WK_* in WarData
    public boolean authorized;                 // Congress voted to authorize force
    public int timetable = -1, timetableStart = -1, handoff = -1;   // weeks a withdrawal timetable or a handover ends
    public double troops0;                     // troops when a timetable was set
    public boolean wpVoted;                    // Congress has voted under the War Powers Resolution
    public double legit;                       // the support a war of choice settles toward: higher if authorized and against a real enemy
}

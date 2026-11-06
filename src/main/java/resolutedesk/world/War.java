package resolutedesk.world;

import java.io.Serializable;

/** A war the United States fights: the front, the troops and strategy, the casualties, support at home and how it ends. */
public final class War implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id, name;
    public int enemy = -1, ally = -1;
    public int stage;                          // 0 not begun, 2 fighting, 3 won, 4 lost or abandoned, 5 ended by agreement
    public int strategy = 1;                   // 0 hold the line, 1 balanced, 2 on the offensive, 3 seek a settlement
    public double front = 50, troops, kia, support = 60, talks, weeks;
    public double need, drift, lethal, cost, stakes;
    public int started = -1, ended = -1;
}

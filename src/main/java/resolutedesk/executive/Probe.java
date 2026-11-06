package resolutedesk.executive;

import java.io.Serializable;

/** An investigation of the administration by Congress, an inspector general or a special counsel. */
public final class Probe implements Serializable {
    private static final long serialVersionUID = 1L;
    public String topic, chair;
    public int chamber, started, until, response = -1;
    Scandal scandal;
    public boolean over, privilegeCase;
}

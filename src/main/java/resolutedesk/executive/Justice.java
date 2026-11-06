package resolutedesk.executive;

import java.io.Serializable;
import resolutedesk.model.Cand;

/** A justice of the Supreme Court. */
public final class Justice implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public int born, party;
    double ideo;
    public boolean real = true;
    public Cand face;
}

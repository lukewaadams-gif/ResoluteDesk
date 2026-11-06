package resolutedesk.executive;

import java.io.Serializable;

/** A scandal: what it is about, how serious it has become, and who is involved. */
public final class Scandal implements Serializable {
    private static final long serialVersionUID = 1L;
    public String title, body, who;
    public double severity;
    public int started, stage, kind;                 // stage 0 a story, 1 under investigation, 2 special counsel; kind see SK_*
    public boolean impeachable, answered, over;
    Person person;
}

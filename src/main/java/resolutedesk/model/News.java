package resolutedesk.model;

import java.io.Serializable;

/** One item in the weekly news log. */
public final class News implements Serializable {
    private static final long serialVersionUID = 1L;
    public int day, kind;
    public String head, body;
    public double weight;                      // how big a story it is
    public String section;                     // the paper's section for a story from beyond the White House, or null
}

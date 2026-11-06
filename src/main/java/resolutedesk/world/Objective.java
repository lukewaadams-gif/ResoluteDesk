package resolutedesk.world;

import java.io.Serializable;

/** A goal the United States is pursuing with one country: its steps, how far it has come, and when it can be tried again. */
public final class Objective implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id;                          // the row in ObjectiveData
    public int country, step, status;          // status: 0 open, 1 achieved, 2 stalled for now
    public int tryAfter, fails;                // the week the next attempt can be made; failures on this step
}

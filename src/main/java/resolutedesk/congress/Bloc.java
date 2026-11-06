package resolutedesk.congress;

import java.io.Serializable;

/** A bloc of members of Congress who tend to vote together. */
public final class Bloc implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public int party, chamber, seats;
    public double econ, cult, loyalty, hawk, rel = 50;
}

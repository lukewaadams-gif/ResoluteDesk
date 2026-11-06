package resolutedesk.sim;

/** Kinds of decisions that reach the president's desk. */
public final class DecisionKinds {
    private DecisionKinds() {
    }

    //  Things only the president can decide land in the inbox. Each choice comes with what your advisers say.
    public static final int D_SIGN = 1, D_CR = 2, D_SHUTDOWN = 3, D_DEBT = 4, D_BUDGET = 5, D_INFO = 6, D_MUSTPASS = 7,
            D_CRA = 8, D_EMERG_END = 9, D_COURT = 10, D_SCANDAL = 11, D_LEAK = 12, D_PARDON = 13, D_SUBPOENA = 14, D_IMPEACH = 15,
            D_WORLD = 16, D_SUMMIT = 17, D_GSUMMIT = 18, D_HOME = 19, D_DISASTER = 20, D_SPEECH = 21, D_QA = 22, D_HIST = 23, D_CAMPAIGN = 24, D_ASK = 25;
}

package resolutedesk.congress;

import java.io.Serializable;

/**
 * Something owed between the president and a member of Congress.
 *
 * <p>Either the member owes you (you did something for them, and can call it in on a hard vote), or you made
 * them a promise: to be seen in their state by a deadline. Keeping a promise earns a favor; breaking one costs
 * the member's trust and their bloc's.
 */
public final class Favor implements Serializable {
    private static final long serialVersionUID = 1L;
    public Member member;
    public boolean theyOwe;              // true: a favor you can call in; false: a promise you made
    public String why;
    public int week;                     // when it was made
    public int state = -1;               // a promise: the state you said you'd visit
    public int due = -1;                 // a promise: the week it must be kept by
    public boolean settled;              // called in, kept or broken
}

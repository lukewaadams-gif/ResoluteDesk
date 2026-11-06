package resolutedesk.sim;

import java.io.Serializable;
import resolutedesk.congress.Bill;
import resolutedesk.congress.Member;
import resolutedesk.executive.Action;
import resolutedesk.executive.Case;
import resolutedesk.executive.Person;
import resolutedesk.executive.Probe;
import resolutedesk.executive.Scandal;

/** A decision waiting on the president's desk, with its options and the advice that comes with them. */
public final class Decision implements Serializable {
    private static final long serialVersionUID = 1L;
    public int kind, a, b;
    public String title, body;
    public String[] opts, advice;
    public Bill bill;
    public Action action;
    public Case caseRef;
    public Scandal scandalRef;
    public Probe probeRef;
    public Person person;
    public Member member;
    public int[] codes;
    Person[] advisers;
}

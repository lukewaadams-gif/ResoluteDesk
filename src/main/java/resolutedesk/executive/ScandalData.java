package resolutedesk.executive;

/** Kinds of scandal, and pardons a president can grant. */
public final class ScandalData {
    private ScandalData() {
    }

    public static final int SK_LEAK = 0, SK_ETHICS = 1, SK_PARDON = 2, SK_DEFY = 3, SK_POWER = 4, SK_PAST = 5, SK_FAILURE = 6;

    /** Pardon requests: title|body|kind  (kind 0 mercy, 1 ally, 2 donor, 3 soldier, 4 whistleblower, 5 protesters, 6 celebrity). */
    public static final String[] PARDONS = {
            "A grandmother serving life for a drug offense|She has served 28 years for a nonviolent cocaine conspiracy. Her case has bipartisan support from reform groups.|0",
            "A former campaign aide|He was convicted of lying to federal investigators about contacts during your campaign. Your allies call the case political.|1",
            "A major donor convicted of fraud|A businessman who gave millions to your party was convicted of defrauding investors. His lawyers say he has repaid them.|2",
            "A soldier convicted of a war crime|A decorated special-forces soldier was convicted of killing a detainee. Veterans' groups are split.|3",
            "A whistleblower|A former contractor who leaked documents about domestic surveillance has lived abroad for years. Civil libertarians want him home.|4",
            "Protesters convicted after a riot|Dozens of people convicted after a protest turned violent. Your base sees them as political prisoners; others see rioters.|5",
            "A rapper serving time on gun charges|A famous musician's sentence for a gun charge. Celebrities are lobbying the White House.|6",
            "A former governor convicted of corruption|A former governor from your party was convicted of selling state contracts. He says it was a witch hunt.|1",
            "Nonviolent drug offenders, all at once|Commute the sentences of about 1,500 people serving long terms for nonviolent drug crimes.|0"};
}

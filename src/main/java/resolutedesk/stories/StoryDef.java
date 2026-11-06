package resolutedesk.stories;

import static resolutedesk.stories.StoryData.*;

/** One story of its era, read from its row: first year|last year|conditions|section letter|headline|body|effects. */
public final class StoryDef {
    public final int from, to, section;
    public final String when, head, body, fx;

    StoryDef(String row) {
        String[] t = row.split("\\|", -1);
        if (t.length != 7) throw new IllegalArgumentException("a story needs 7 fields: " + row);
        from = Integer.parseInt(t[0]);
        to = Integer.parseInt(t[1]);
        when = t[2];
        section = SECTION_CODES.indexOf(t[3]);
        if (section < 0) throw new IllegalArgumentException("no section " + t[3] + ": " + row);
        head = t[4];
        body = t[5];
        fx = t[6];
    }
}

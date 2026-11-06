package resolutedesk.stories;

/**
 * One storyline, read from its rows in StorylineTable. The first row is
 * id|first year|last year|what starts it|what feeds it|name|its ending's effects|its ending's headline|its ending's story|how it fades.
 * Each row after it is a stage: headline|story|weeks|effects each week|options, with options separated by ";;" and each
 * option label|adviser|odds kind|base odds|heat if it works|heat if it fails|effects if it works|effects if it fails|news if it works|news if it fails.
 */
public final class StorylineDef {
    public final String id, trigger, fuel, name, endFx, endHead, endBody, fade;
    public final int from, to;
    public final String[][] stage;          // headline and story of each stage
    public final int[] weeks;
    public final String[] weekFx;
    public final String[][][] options;      // stage, option, field

    StorylineDef(String[] rows) {
        String[] h = rows[0].split("\\|", -1);
        if (h.length != 10) throw new IllegalArgumentException("a storyline's first row needs 10 fields: " + rows[0]);
        id = h[0]; from = Integer.parseInt(h[1]); to = Integer.parseInt(h[2]); trigger = h[3]; fuel = h[4]; name = h[5];
        endFx = h[6]; endHead = h[7]; endBody = h[8]; fade = h[9];
        int n = rows.length - 1;
        stage = new String[n][];
        weeks = new int[n];
        weekFx = new String[n];
        options = new String[n][][];
        for (int i = 0; i < n; i++) {
            String[] t = rows[i + 1].split("\\|", 5);
            if (t.length != 5) throw new IllegalArgumentException(id + " stage " + (i + 1) + " needs 5 fields");
            stage[i] = new String[]{t[0], t[1]};
            weeks[i] = Integer.parseInt(t[2]);
            weekFx[i] = t[3];
            String[] opts = t[4].split(";;");
            options[i] = new String[opts.length][];
            for (int k = 0; k < opts.length; k++) {
                options[i][k] = opts[k].split("\\|", -1);
                if (options[i][k].length != 10) throw new IllegalArgumentException(id + " stage " + (i + 1) + " option " + (k + 1) + " needs 10 fields");
            }
        }
    }

    public int stages() { return stage.length; }
}

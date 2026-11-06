package resolutedesk.ui;

import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import resolutedesk.ResoluteDesk;
import resolutedesk.model.Game;

/** The tour of the Oval Office shown at the start of a first game. */
public final class Tutorial {
    static final String[] TITLE = {"Welcome to the Oval Office", "The numbers that matter", "The Resolute Desk", "The windows", "The briefing book", "Through the doors",
            "Decisions", "Ending the week", "Let it play"};
    static final String[] BODY = {
            "This is where you'll spend your presidency. Everything you can do is a click away. A quick tour takes a minute; skip it whenever you like.",
            "Your approval, stamina and stress, and the state of the economy, are always up here. Approval is your currency: it moves Congress, the press and, in the end, the voters.",
            "Click the desk to plan your week: seven days of morning, afternoon and evening. Policy time builds your command of an issue; trips and speeches move opinion; rest keeps you going.",
            "The windows show the country: your approval by issue, by group of voters and state by state.",
            "The briefing book on the side table is the economy: jobs, prices, markets, the budget and the Fed.",
            "The doors lead to the rest of the White House: the West Wing for your staff and cabinet, the Roosevelt Room for Congress, the Press Briefing Room, and the globe for the Situation Room. Keys 1 to 8 jump between rooms.",
            "When something only the president can decide comes up (a bill to sign, a crisis abroad, a turning point in history), it appears in front of you with your advisers' views and the odds.",
            "When you're ready, end the week here or press Space. The week plays out, and the morning paper tells you what happened.",
            "If you'd rather watch for a while, the Watch button lets the game play itself. Click again to take back the controls. Good luck, Mr. or Madam President."};

    /** The spot to light up for each step: x, y, w, h (or none). */
    static double[] spot(int i) {
        switch (i) {
            case 1: return new double[]{350, 4, 980, 50};
            case 2: return Oval.SPOT[0];
            case 3: return Oval.SPOT[1];
            case 4: return Oval.SPOT[2];
            case 5: return new double[]{40, 140, 1520, 440};
            case 7: return new double[]{DW - 194, 6, 178, 44};
            case 8: return new double[]{DW - 382, 6, 66, 44};
            default: return null;
        }
    }

    static void draw(ResoluteDesk u, Graphics2D g, Game game) {
        int i = game.tut;
        if (i < 0 || i >= TITLE.length || u.modal != null) return;
        double[] s = spot(i);
        if (s != null) {
            g.setColor(new Color(242, 193, 78, 200));
            g.setStroke(new BasicStroke(3f));
            g.draw(new RoundRectangle2D.Double(s[0] - 4, s[1] - 4, s[2] + 8, s[3] + 8, 12, 12));
            g.setStroke(new BasicStroke(1f));
        }
        double w = 520, h = 196, x = DW / 2 - w / 2, y = i == 2 || i == 4 ? 140 : 600;
        if (i == 3) y = 420;
        g.setColor(new Color(8, 13, 26, 245));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 14, 14));
        g.setColor(C_GOLD);
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 14, 14));
        label(g, "THE TOUR, " + (i + 1) + " OF " + TITLE.length, x + 20, y + 26, C_GOLD);
        text(g, TITLE[i], x + 20, y + 52, F_H2, Color.white);
        wrap(g, BODY[i], x + 20, y + 76, w - 40, F_S, C_TEXT);
        u.button(g, x + w - 140, y + h - 46, 120, 32, i == TITLE.length - 1 ? "Done" : "Next", B_GOLD, () -> { game.tut = game.tut + 1 >= TITLE.length ? -1 : game.tut + 1; });
        u.button(g, x + 20, y + h - 46, 140, 32, "Skip the tour", B_GHOST, () -> game.tut = -1);
    }
}

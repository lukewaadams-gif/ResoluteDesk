package resolutedesk.ui;

import static resolutedesk.ui.Theme.*;

import java.awt.Color;
import resolutedesk.model.Cand;

/** A modal dialog: a title, a body and buttons, drawn over the current screen. */
public final class Modal {
    public String title, body;
    public String[] buttons, descs;
    public Runnable[] actions;
    public Color col = C_ACCENT;
    public Painter painter;
    public double w = 660, extraH;
    public boolean stacked;                     // options as full-width stacked buttons with descriptions
    public Cand face;                           // portrait shown at the left
}

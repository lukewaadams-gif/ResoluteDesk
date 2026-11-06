package resolutedesk.ui;

import java.awt.Color;

/** A popup menu attached to a control. */
public final class Popup {
    public double x, y, w;
    public String title;
    public String[] items, tips, right;
    public Color[] cols;
    public boolean[] enabled;
    public IntAction pick;
    int hover = -1;
}

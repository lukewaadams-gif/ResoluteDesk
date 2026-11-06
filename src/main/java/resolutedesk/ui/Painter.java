package resolutedesk.ui;

import java.awt.Graphics2D;

/** Something that draws itself into a rectangle. */
public interface Painter {
    void paint(Graphics2D g, double x, double y, double w, double h);
}

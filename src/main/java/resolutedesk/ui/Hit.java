package resolutedesk.ui;

/** A clickable or draggable region registered while a frame is drawn. */
public final class Hit {
    public double x, y, w, h;
    public Runnable act;
    public DoubleAction dragX;
    public double dragX0, dragW;
    public String tip, lab;
    public int layer;

    public boolean in(double px, double py) { return px >= x && py >= y && px < x + w && py < y + h; }
}

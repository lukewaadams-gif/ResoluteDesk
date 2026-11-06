package resolutedesk.model;

/** Per-state data. Results are D/R/total votes; demographics are population shares (ACS 2012-16, county-aggregated)
 *  plus hand-assembled religion, union, veteran, gun and LGBTQ estimates (PRRI, BLS, VA, RAND, Gallup). */
public final class StateDef {
    public final String ab, name, region;
    public int ev;                                     // electoral votes (the 2030 what-if map changes them)
    public final double d24, r24, t24, d20, r20, t20, d16, r16, t16, m22;
    public final double white, black, hisp, other, asian, nativeAm, u29, o65, nocol, nocolW, rural, income, cvap;
    public final double wev, wca, jew, lds, arab, cuban, pr, union, vet, gun, lgbt, urban, close;
    final int countSpeed;
    public final double cx, cy;

    public StateDef(String ab, String name, String region, int ev, double[] r, double[] d, double[] h, double close, int spd, double cx, double cy) {
        this.ab = ab; this.name = name; this.region = region; this.ev = ev;
        d24 = r[0]; r24 = r[1]; t24 = r[2]; d20 = r[3]; r20 = r[4]; t20 = r[5]; d16 = r[6]; r16 = r[7]; t16 = r[8]; m22 = r[9];
        white = d[0]; black = d[1]; hisp = d[2]; other = d[3]; u29 = d[4]; o65 = d[5]; nocol = d[6]; nocolW = d[7]; rural = d[8]; income = d[9]; cvap = d[10];
        wev = h[0]; wca = h[1]; jew = h[2]; lds = h[3]; arab = h[4]; nativeAm = h[5]; asian = h[6]; cuban = h[7]; pr = h[8];
        union = h[9]; vet = h[10]; gun = h[11]; lgbt = h[12]; urban = h[13];
        this.close = close; countSpeed = spd; this.cx = cx; this.cy = cy;
    }
}

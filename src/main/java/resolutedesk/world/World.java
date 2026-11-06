package resolutedesk.world;

import static resolutedesk.world.CrisisData.*;
import static resolutedesk.world.WorldData.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** The state of the world in a game: the countries, their leaders and relations, the flashpoints and the wars. */
public final class World implements Serializable {
    private static final long serialVersionUID = 1L;
    public double[] rel = new double[NC];
    public String[] leader = new String[NC], ltitle = new String[NC];
    public int[] tariff = new int[NC], sanction = new int[NC], aid = new int[NC], lastCall = new int[NC], visits = new int[NC], visitAt = new int[NC];
    public int callsThisWeek, callWeek = -1;
    double tariffAdd, oilAdd;
    public double twt = 45, deter = 55, ukr = 55, ruGain = 20, talks = 10, gaza = 40, iran = 70, redsea = 50, proxy = 30, nk = 55, terror = 35, cyber = 45, migr = 50, cartel = 55;
    public int twStage = 1, uaStage = 2, defcon = 5;
    public double nato = 60, pacific = 65, allySpend = 2.0;
    public int[] posture = {1, 1, 1};
    public int[] cool = new int[CR_ID.length];
    public int kia, ops, covert, exposed, crises, deals;
    public int summitCountry = -1, summitWeek = -1, summitRound, summitAsk = -1;
    double summitMood, summitLeverage;
    public List<String> record = new ArrayList<String>();
    public int[] opCool = new int[OP_NAME.length], cvCool = new int[CV_NAME.length];
    public int art5Week = -99, pressWeek = -99, drillWeek = -99;
    // history: the countries as they were, the wars, the turning points already passed
    public String[] name;
    public int[] align;
    public double[] base, power;
    public boolean[] nuke, gone, hfired;
    public boolean modern = true;
    public double tradeK = 1;
    public List<War> wars;
    public int worldYear = -1, pressYear = -1;
}

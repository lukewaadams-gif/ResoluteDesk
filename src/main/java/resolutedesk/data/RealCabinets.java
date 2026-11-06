package resolutedesk.data;

/** The real cabinet of 2025 and other real figures who can be named to posts. */
public final class RealCabinets {
    private RealCabinets() {
    }

    /** The real 2025 slate: post (or staff role as 100+role), name, background, female, skill, loyalty, ideology, trait, roster id. */
    public static final String[] SLATE25 = {
            "0|Marco Rubio|Senator|M|78|72|0.55|0|rubio", "1|Scott Bessent|Investor|M|80|70|0.45|1|", "2|Pete Hegseth|Television host and veteran|M|55|85|0.75|2|",
            "3|Pam Bondi|State attorney general|F|68|88|0.6|2|", "4|Doug Burgum|Former governor|M|74|72|0.45|0|", "5|Brooke Rollins|Think-tank leader|F|70|80|0.6|3|",
            "6|Howard Lutnick|Business executive|M|66|80|0.5|5|", "7|Lori Chavez-DeRemer|House member|F|64|65|0.2|4|", "8|Robert F. Kennedy Jr.|Activist and lawyer|M|58|70|0.3|5|rfk",
            "9|Scott Turner|House member and NFL player|M|60|80|0.6|0|", "10|Sean Duffy|House member|M|62|82|0.55|5|", "11|Chris Wright|Energy executive|M|76|72|0.55|1|",
            "12|Linda McMahon|Business executive|F|62|85|0.5|0|", "13|Doug Collins|House member|M|66|80|0.6|3|", "14|Kristi Noem|Former governor|F|62|82|0.7|5|",
            "15|Lee Zeldin|House member|M|64|84|0.6|5|", "16|Russ Vought|Budget veteran|M|74|90|0.85|1|", "17|Jamieson Greer|Trade lawyer|M|74|78|0.55|1|",
            "18|Elise Stefanik|House member|F|66|82|0.6|5|", "19|Tulsi Gabbard|House member|F|60|70|0.4|5|", "20|John Ratcliffe|House member|M|70|78|0.6|0|",
            "100|Susie Wiles|Campaign veteran|F|84|90|0.45|0|", "101|Mike Waltz|House member|M|64|75|0.6|5|", "102|Karoline Leavitt|Campaign veteran|F|66|90|0.6|5|",
            "103|David Warrington|Campaign lawyer|M|66|85|0.55|2|", "104|Kevin Hassett|Economist|M|74|80|0.5|1|", "105|Vince Haley|Policy adviser|M|66|85|0.7|3|",
            "106|James Braid|Hill veteran|M|70|78|0.5|4|", "107|James Blair|Campaign veteran|M|72|82|0.55|3|", "108|Steven Cheung|Campaign veteran|M|64|90|0.55|5|"};

    /** Who from the roster might take each job, by party: "post:id,id". */
    public static final String[] BIGNAMES_D = {"0:emanuel,murphy", "1:raimondo", "2:kelly", "3:klobuchar", "5:walz", "6:raimondo,cuban", "7:brown", "8:cooper,beshear",
            "10:buttigieg", "12:talarico,beshear", "14:gallego,kelly", "18:booker,khanna", "11:whitmer", "16:emanuel"};

    public static final String[] BIGNAMES_R = {"0:rubio,haley", "1:youngkin", "2:desantis", "3:cruz", "5:kemp", "6:youngkin,ramaswamy", "7:hawley", "8:rfk", "12:sanders",
            "14:desantis,kemp", "16:ramaswamy", "18:haley"};
}

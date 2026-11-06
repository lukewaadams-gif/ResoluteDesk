package resolutedesk.data;

import static resolutedesk.model.Parties.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.model.Era;

/** Every inauguration the game can start from, with the conditions of the day. */
public final class Eras {
    private Eras() {
    }

    public static final List<Era> ERAS = new ArrayList<Era>();

    public static Era era(int year) { for (Era e : ERAS) if (e.year == year) return e; return ERAS.get(ERAS.size() - 1); }

    /** year|month0|day|name|winner|termOf|predecessor|houseD|senateD|approval base|polarization|unemployment|inflation|growth|fed|10y|stocks|oil|gas|debt|deficit|
     *  nominal GDP ($T)|potential|nairu|price level (2025 = 1)|tariff|Fed chair|Fed credibility|slump|House D leader|House R leader|Senate D leader|Senate R leader|issues|labels|blurb */
    public static final String[] ERA_SPEC = {
            "1933|2|4|The Great Depression|fdr|1|Herbert Hoover|313|61|64|0.3|24.9|-5.1|-1.5|2.5|3.3|7|0.67|0.18|35|4.5|0.057|3.0|5.0|0.041|19.8|Eugene Meyer|0.3|-30|Henry Rainey|Bertrand Snell|Joseph Robinson|Charles McNary"
                    + "|0=1.8 1=0.8 2=2.0 3=0.2 4=0.2 5=0.6 6=0.3 7=0.2 8=0.5 9=0.2 10=0.3 11=0.05 12=0.2 13=0.6|9=Energy & conservation 11=Science"
                    + "|March 4, 1933. A quarter of the workforce has no job, thousands of banks have failed and most states have closed the rest. Farm prices have collapsed and families are losing their homes. Democrats hold huge majorities in Congress. The country wants action, and fast.",
            "1937|0|20|The Second New Deal|fdr|2|Franklin D. Roosevelt|334|79|57|0.3|14.3|3.0|5.1|1.0|2.7|17|1.18|0.20|38|2.5|0.092|3.0|5.0|0.045|15.6|Marriner Eccles|0.4|-14|William Bankhead|Bertrand Snell|Joseph Robinson|Charles McNary"
                    + "|0=1.4 1=0.6 2=1.5 3=0.3 4=0.2 5=0.4 6=0.6 7=0.4 8=0.4 9=0.2 10=0.4 11=0.05 12=0.2 13=0.6|9=Energy & conservation 11=Science"
                    + "|January 20, 1937. Re-elected in a landslide with the largest majorities in Congress in a century. The recovery is real but unfinished: one worker in seven is still jobless, and the Supreme Court keeps striking down the New Deal. Abroad, fascism is on the march.",
            "1941|0|20|The Arsenal of Democracy|fdr|3|Franklin D. Roosevelt|267|69|58|0.3|11.0|1.4|8.0|1.0|2.0|10.5|1.14|0.19|42|3.0|0.103|3.5|4.5|0.046|13.6|Marriner Eccles|0.5|-8|Sam Rayburn|Joseph Martin|Alben Barkley|Charles McNary"
                    + "|0=1.0 1=0.4 2=1.0 3=0.2 4=0.2 5=0.3 6=1.6 7=1.4 8=0.4 9=0.2 10=0.4 11=0.05 12=0.2 13=0.3|9=Energy & conservation 11=Science"
                    + "|January 20, 1941. The first third term in history. War has engulfed Europe and China; Britain stands nearly alone and is running out of money to buy American arms. The country is officially neutral, and divided over how far to go to help.",
            "1945|0|20|Victory and After|fdr|4|Franklin D. Roosevelt|242|59|62|0.3|1.9|2.3|4.0|0.375|2.4|13.5|1.22|0.21|100|21|0.228|3.0|4.0|0.056|9.5|Marriner Eccles|0.6|0|Sam Rayburn|Joseph Martin|Alben Barkley|Wallace White"
                    + "|0=0.7 1=0.9 2=0.6 3=0.3 4=0.2 5=0.3 6=1.8 7=2.0 8=0.3 9=0.2 10=0.4 11=0.1 12=0.2 13=0.2|9=Energy & conservation 11=Science & the atom"
                    + "|January 20, 1945. A fourth term, in the last year of the largest war in history. Allied armies are closing on Germany; in the Pacific the island campaigns grind toward Japan. Ahead lie the peace, the United Nations, a secret weapon and twelve million servicemen coming home.",
            "1949|0|20|The Fair Deal|truman|1|Harry S. Truman|263|56|56|0.35|4.3|1.3|1.0|1.0|2.4|15.2|2.54|0.27|78|-0.2|0.272|3.5|4.5|0.074|5.5|Thomas McCabe|0.6|-1|Sam Rayburn|Joseph Martin|Scott Lucas|Kenneth Wherry"
                    + "|0=1.0 1=1.0 2=0.8 3=0.5 4=0.2 5=0.3 6=1.4 7=1.2 8=0.3 9=0.2 10=0.8 11=0.1 12=0.3 13=0.6|9=Energy & conservation 11=Science & the atom"
                    + "|January 20, 1949. Elected in his own right after the upset of the century. The Cold War has begun: Berlin is under blockade and the airlift flies around the clock. Prices have finally cooled, and the Fair Deal waits on a Congress Democrats now control again.",
            "1953|0|20|I Like Ike|eisenhower|1|Harry S. Truman|213|49|60|0.3|2.9|0.8|4.7|2.0|2.8|26|2.68|0.29|58|1.7|0.389|3.5|4.3|0.083|5.0|William McChesney Martin|0.75|0|Sam Rayburn|Joseph Martin|Lyndon Johnson|Robert Taft"
                    + "|0=1.0 1=0.6 2=0.6 3=0.3 4=0.2 5=0.3 6=1.3 7=1.4 8=0.3 9=0.2 10=0.6 11=0.2 12=0.3 13=0.8|9=Energy & conservation 11=Science & the atom"
                    + "|January 20, 1953. The general takes office promising to end the war in Korea, where truce talks have dragged on for eighteen months. Republicans hold the White House and, narrowly, both chambers for the first time in twenty years. The economy is booming.",
            "1957|0|20|The Affluent Society|eisenhower|2|Dwight D. Eisenhower|234|51|62|0.3|4.2|2.9|2.1|3.0|3.4|46|3.09|0.31|47|-0.8|0.474|3.3|4.5|0.088|5.5|William McChesney Martin|0.8|0|Sam Rayburn|Joseph Martin|Lyndon Johnson|William Knowland"
                    + "|0=0.9 1=0.6 2=0.6 3=0.3 4=0.2 5=0.3 6=1.2 7=1.1 8=0.3 9=0.2 10=1.2 11=0.5 12=0.6 13=0.4|9=Energy & conservation 11=Science & space"
                    + "|January 20, 1957. Re-elected in a landslide, though Democrats kept Congress. Prosperity is broad and the suburbs and interstates are spreading. But the South is resisting school desegregation, Soviet tanks have just crushed Hungary, and the space age is about to begin.",
            "1961|0|20|The New Frontier|jfk|1|Dwight D. Eisenhower|262|64|60|0.3|6.6|1.6|0.5|2.0|3.8|58|2.97|0.31|43|0.6|0.54|3.5|5.0|0.093|7.2|William McChesney Martin|0.8|-1|Sam Rayburn|Charles Halleck|Mike Mansfield|Everett Dirksen"
                    + "|0=1.0 1=0.4 2=1.0 3=0.5 4=0.2 5=0.3 6=1.4 7=1.3 8=0.3 9=0.2 10=1.0 11=0.6 12=0.5 13=0.3|9=Energy & conservation 11=Science & space"
                    + "|January 20, 1961. The youngest man ever elected president takes office after one of the closest races in history, promising to get the country moving again. A recession lingers, Cuba has turned to Moscow, the Soviets lead in space, and the civil rights movement is pressing for action.",
            "1965|0|20|The Great Society|lbj|1|Lyndon B. Johnson|295|68|62|0.3|4.9|1.6|6.5|4.0|4.2|87|2.86|0.31|37|0.2|0.72|4.0|4.8|0.098|6.5|William McChesney Martin|0.8|0|John McCormack|Gerald Ford|Mike Mansfield|Everett Dirksen"
                    + "|0=0.7 1=0.3 2=0.6 3=1.0 4=0.4 5=0.6 6=1.1 7=1.0 8=0.3 9=0.3 10=1.5 11=0.5 12=0.8 13=0.3|9=Energy & environment 11=Science & space"
                    + "|January 20, 1965. Elected in a 44-state landslide with the largest majorities since the New Deal. A civil rights act is law; voting rights, Medicare and a war on poverty are next. In South Vietnam the government is losing to the Viet Cong, and 23,000 American advisers are there.",
            "1969|0|20|Law and Order|nixon|1|Lyndon B. Johnson|243|57|54|0.4|3.4|4.4|3.1|6.3|6.0|102|3.09|0.35|29|-0.3|0.98|3.8|5.0|0.115|6.0|William McChesney Martin|0.6|0|John McCormack|Gerald Ford|Mike Mansfield|Everett Dirksen"
                    + "|0=0.7 1=0.8 2=0.4 3=0.4 4=0.2 5=1.2 6=1.2 7=1.6 8=0.3 9=0.5 10=1.0 11=0.5 12=0.5 13=0.4|9=Energy & environment 11=Science & space"
                    + "|January 20, 1969. After a year of assassinations, riots and a war that split the country, the new president promises to bring Americans together and end the war with honor. More than half a million troops are in Vietnam; the Paris peace talks have barely begun. Democrats hold Congress.",
            "1973|0|20|Peace with Honor|nixon|2|Richard Nixon|242|56|55|0.4|4.9|3.6|5.6|6.0|6.5|118|3.89|0.39|26|1.1|1.38|3.4|5.5|0.139|5.5|Arthur Burns|0.45|0|Carl Albert|Gerald Ford|Mike Mansfield|Hugh Scott"
                    + "|0=1.0 1=1.3 2=0.6 3=0.4 4=0.2 5=0.9 6=1.0 7=0.9 8=0.3 9=0.9 10=0.6 11=0.3 12=0.4 13=1.0|9=Energy & environment 11=Science & space"
                    + "|January 20, 1973. Re-elected with 49 states. A peace agreement for Vietnam is days away and the last American troops will soon come home. The economy is running hot under wage and price controls, oil is getting scarce, and questions about a burglary at the Watergate are growing.",
            "1977|0|20|A Crisis of Confidence|carter|1|Gerald Ford|292|62|54|0.4|7.5|5.2|4.6|4.6|7.2|103|14.4|0.62|27|2.6|2.03|3.2|6.2|0.189|4.0|Arthur Burns|0.4|0|Tip O'Neill|John Rhodes|Robert Byrd|Howard Baker"
                    + "|0=1.1 1=1.3 2=1.0 3=0.5 4=0.3 5=0.6 6=0.8 7=0.6 8=0.4 9=1.3 10=0.6 11=0.2 12=0.4 13=0.9|9=Energy & environment 11=Science & technology"
                    + "|January 20, 1977. An outsider from Plains, Georgia promises a government as good as its people, two years after Watergate and the fall of Saigon. Unemployment is 7.5% and inflation is creeping up. The country imports half its oil, and this is the coldest winter in memory.",
            "1981|0|20|The Reagan Revolution|reagan|1|Jimmy Carter|243|47|55|0.45|7.5|11.8|1.5|19.0|12.6|133|35|1.38|25.5|2.5|3.10|3.0|6.3|0.284|3.5|Paul Volcker|0.5|0|Tip O'Neill|Robert Michel|Robert Byrd|Howard Baker"
                    + "|0=1.4 1=1.8 2=0.9 3=0.3 4=0.4 5=0.7 6=1.0 7=1.0 8=0.5 9=0.9 10=0.4 11=0.2 12=0.4 13=0.4|9=Energy & environment 11=Science & technology"
                    + "|January 20, 1981. Inflation is near 12%, the prime rate is over 20%, and the hostages in Iran are coming home today after 444 days. The new president promises tax cuts, a bigger military and a smaller government. Republicans have won the Senate for the first time since 1953.",
            "1985|0|20|Morning in America|reagan|2|Ronald Reagan|253|47|56|0.45|7.3|3.5|4.2|8.4|11.4|168|27|1.20|35|5.0|4.22|3.0|6.2|0.336|3.6|Paul Volcker|0.7|0|Tip O'Neill|Robert Michel|Robert Byrd|Bob Dole"
                    + "|0=1.0 1=0.7 2=0.8 3=0.4 4=0.5 5=0.8 6=1.1 7=1.1 8=0.8 9=0.3 10=0.4 11=0.3 12=0.5 13=0.4|9=Energy & environment 11=Science & technology"
                    + "|January 20, 1985. Re-elected with 49 states. The economy is booming, inflation is tamed, but the deficit has tripled and the trade gap with Japan is a political fight. A new generation may soon lead the Soviet Union; the arms race is at its height.",
            "1989|0|20|A New World Order|bush41|1|Ronald Reagan|260|55|54|0.5|5.4|4.7|3.7|9.1|9.1|286|18|0.96|40|2.8|5.6|3.0|5.8|0.388|3.4|Alan Greenspan|0.8|0|Jim Wright|Robert Michel|George Mitchell|Bob Dole"
                    + "|0=0.9 1=0.6 2=0.6 3=0.6 4=0.4 5=1.1 6=1.1 7=0.9 8=0.6 9=0.6 10=0.4 11=0.2 12=0.6 13=0.7|9=Energy & environment 11=Science & technology"
                    + "|January 20, 1989. A kinder, gentler nation, and a world about to turn over: reform in Moscow is loosening the Eastern bloc's chains. At home the savings and loan industry is collapsing, the deficit won't go away, and a promise of no new taxes hangs over every budget.",
            "1993|0|20|It's the Economy|clinton|1|George H. W. Bush|259|57|52|0.55|7.3|3.3|3.5|3.0|6.6|435|18.5|1.06|47|4.5|6.8|3.0|5.6|0.452|3.2|Alan Greenspan|0.8|0|Tom Foley|Robert Michel|George Mitchell|Bob Dole"
                    + "|0=1.5 1=0.6 2=1.1 3=1.3 4=0.5 5=1.0 6=0.6 7=0.4 8=0.8 9=0.4 10=0.6 11=0.4 12=0.6 13=0.6|11=Technology"
                    + "|January 20, 1993. The first baby-boomer president promises to focus like a laser on an economy that is recovering too slowly to feel. The deficit is huge, health costs are soaring, and the Cold War is over: no one is sure what comes next.",
            "1997|0|20|The Bridge to the 21st Century|clinton|2|Bill Clinton|208|45|56|0.6|5.3|3.0|4.4|5.25|6.4|770|25|1.26|46|1.3|8.4|3.3|5.0|0.502|2.5|Alan Greenspan|0.85|0|Dick Gephardt|Newt Gingrich|Tom Daschle|Trent Lott"
                    + "|0=0.9 1=0.4 2=0.5 3=0.8 4=0.5 5=0.9 6=0.6 7=0.5 8=0.6 9=0.4 10=0.5 11=0.6 12=0.9 13=1.0|11=Technology & the internet"
                    + "|January 20, 1997. Re-elected comfortably, facing a Republican Congress. The economy is the best in a generation and the internet boom is under way; the budget could balance for the first time since 1969. Investigations are circling the White House.",
            "2001|0|20|Compassionate Conservatism|bush43|1|Bill Clinton|213|50|52|0.65|4.2|3.7|1.0|6.0|5.2|1340|26|1.47|32|-1.3|10.6|3.0|4.8|0.553|1.8|Alan Greenspan|0.85|-1|Dick Gephardt|Dennis Hastert|Tom Daschle|Trent Lott"
                    + "|0=1.0 1=0.6 2=0.7 3=0.8 4=0.5 5=0.4 6=0.5 7=0.5 8=0.4 9=0.7 10=0.4 11=0.5 12=1.1 13=0.6|11=Technology & the internet"
                    + "|January 20, 2001. After a 36-day recount and a 5-4 Supreme Court decision, the new president takes office with the Senate split 50-50. The budget is in surplus, but the dot-com bubble has burst and the economy is slowing. Foreign policy barely came up in the campaign.",
            "2005|0|20|Political Capital|bush43|2|George W. Bush|203|45|49|0.75|5.3|3.0|3.5|2.25|4.2|1180|46|1.83|35|3.4|13.0|2.7|4.8|0.61|1.6|Alan Greenspan|0.85|0|Nancy Pelosi|Dennis Hastert|Harry Reid|Bill Frist"
                    + "|0=0.8 1=0.6 2=0.6 3=0.7 4=0.8 5=0.3 6=1.3 7=1.3 8=0.4 9=0.7 10=0.5 11=0.3 12=0.5 13=0.6|11=Technology"
                    + "|January 20, 2005. Re-elected with the first popular-vote majority since 1988, the president says he has political capital to spend: on Social Security and on spreading democracy. 150,000 troops are in Iraq, where the insurgency is growing. House prices are soaring.",
            "2009|0|20|The Great Recession|obama|1|George W. Bush|257|58|58|0.8|7.8|0.0|-2.5|0.125|2.5|850|42|1.84|52|9.8|14.4|2.2|5.0|0.67|1.5|Ben Bernanke|0.85|-6|Nancy Pelosi|John Boehner|Harry Reid|Mitch McConnell"
                    + "|0=2.0 1=0.4 2=1.6 3=1.0 4=0.4 5=0.3 6=0.8 7=0.8 8=0.4 9=0.7 10=0.4 11=0.3 12=0.4 13=0.6|11=Technology"
                    + "|January 20, 2009. The economy is losing 700,000 jobs a month in the worst financial crisis since the Depression; the banks and the automakers are on life support, and two wars are under way. A record crowd fills the Mall to see the first Black president sworn in.",
            "2013|0|20|The Fiscal Cliff|obama|2|Barack Obama|201|55|51|0.85|8.0|1.6|1.8|0.125|1.9|1490|95|3.35|72|4.1|16.8|2.0|5.0|0.728|1.4|Ben Bernanke|0.85|-2|Nancy Pelosi|John Boehner|Harry Reid|Mitch McConnell"
                    + "|0=1.2 1=0.5 2=1.0 3=1.0 4=0.9 5=0.5 6=0.7 7=0.7 8=0.4 9=0.6 10=0.6 11=0.5 12=0.4 13=0.6|11=Technology"
                    + "|January 20, 2013. Re-elected, facing a Republican House. The recovery is slow but steady, the fiscal cliff was dodged at the last minute, and the health law's exchanges open this fall. Troops are coming home from Afghanistan; Syria's civil war is spreading.",
            "2017|0|20|America First|trump|1|Barack Obama|194|48|46|0.95|4.7|2.5|2.2|0.66|2.4|2270|52|2.35|76|3.5|19.5|2.0|4.5|0.766|1.6|Janet Yellen|0.85|0|Nancy Pelosi|Paul Ryan|Chuck Schumer|Mitch McConnell"
                    + "|0=1.0 1=0.5 2=0.9 3=1.1 4=1.3 5=0.5 6=0.7 7=0.8 8=1.0 9=0.5 10=0.6 11=0.4 12=0.4 13=0.8|11=Technology"
                    + "|January 20, 2017. A businessman who has never held office takes over after winning the Electoral College while losing the popular vote. Republicans hold both chambers and plan to repeal the health law and cut taxes. The economy is in its eighth year of expansion.",
            "2021|0|20|The Pandemic|biden|1|Donald Trump|222|50|52|0.95|6.4|1.4|5.9|0.125|1.1|3850|52|2.38|99|12.0|22.0|2.0|4.3|0.847|3.0|Jerome Powell|0.85|-3|Nancy Pelosi|Kevin McCarthy|Chuck Schumer|Mitch McConnell"
                    + "|0=1.3 1=0.6 2=1.1 3=1.6 4=0.9 5=0.6 6=0.6 7=0.5 8=0.4 9=0.7 10=0.9 11=0.4 12=0.6 13=0.9|"
                    + "|January 20, 2021. Two weeks after a mob stormed the Capitol, the new president is sworn in behind fences and troops. The pandemic is killing 3,000 Americans a day, but vaccines are arriving. Democrats hold the House narrowly and the Senate only through the vice president's tie-breaking vote."};

    static {
        for (String spec : ERA_SPEC) {
            String[] t = spec.split("\\|", -1);
            Era e = new Era();
            int i = 0;
            e.year = Integer.parseInt(t[i++]); e.inM = Integer.parseInt(t[i++]); e.inD = Integer.parseInt(t[i++]);
            e.name = t[i++]; e.winnerId = t[i++]; e.termOf = Integer.parseInt(t[i++]); e.predecessor = t[i++];
            e.houseD = Integer.parseInt(t[i++]); e.senateD = Integer.parseInt(t[i++]);
            e.approvalBase = Double.parseDouble(t[i++]); e.polarization = Double.parseDouble(t[i++]);
            e.unemp = Double.parseDouble(t[i++]); e.infl = Double.parseDouble(t[i++]); e.gdp = Double.parseDouble(t[i++]); e.fed = Double.parseDouble(t[i++]);
            e.tenYr = Double.parseDouble(t[i++]); e.stocks = Double.parseDouble(t[i++]); e.oil = Double.parseDouble(t[i++]); e.gas = Double.parseDouble(t[i++]);
            e.debt = Double.parseDouble(t[i++]); e.deficit = Double.parseDouble(t[i++]); e.nominalGdp = Double.parseDouble(t[i++]);
            e.potential = Double.parseDouble(t[i++]); e.nairu = Double.parseDouble(t[i++]); e.priceLevel = Double.parseDouble(t[i++]); e.tariff = Double.parseDouble(t[i++]);
            e.fedChair = t[i++]; e.fedCred = Double.parseDouble(t[i++]); e.slump = Double.parseDouble(t[i++]);
            String hD = t[i++], hR = t[i++], sD = t[i++], sR = t[i++];
            e.speakerD = hD; e.houseLeaderD = hD; e.speakerR = hR; e.houseLeaderR = hR; e.leaderD = sD; e.leaderR = sR;
            e.issues = t[i++]; e.labels = t[i].isEmpty() ? null : t[i]; i++;
            e.blurb = t[i];
            e.ai = e.year >= 2021;
            ERAS.add(e);
        }

        Era e = new Era();
        e.year = 2025; e.name = "The real present"; e.termOf = 2;
        e.blurb = "January 20, 2025. Inflation has cooled but prices are still high, Republicans hold both chambers by a few seats, wars grind on in Ukraine and Gaza, and AI is reshaping the economy.";
        e.predecessor = "Joe Biden"; e.winnerId = "trump";
        e.houseD = 215; e.senateD = 47;
        e.unemp = 4.0; e.infl = 3.0; e.gdp = 2.5; e.fed = 4.33; e.tenYr = 4.6; e.stocks = 5950; e.oil = 76; e.gas = 3.10; e.debt = 98; e.deficit = 6.2; e.nominalGdp = 29.7;
        e.potential = 1.9; e.nairu = 4.2; e.tariff = 2.4;
        e.fedChair = "Jerome Powell";
        e.speakerD = "Hakeem Jeffries"; e.speakerR = "Mike Johnson"; e.leaderD = "Chuck Schumer"; e.leaderR = "John Thune"; e.houseLeaderD = "Hakeem Jeffries"; e.houseLeaderR = "Mike Johnson";
        e.midSeats = "CO:D DE:D GA:D IL:D MA:D MI:D MN:D NH:D NJ:D NM:D OR:D RI:D VA:D AL:R AK:R AR:R ID:R IA:R KS:R KY:R LA:R ME:R MS:R MT:R NE:R NC:R OK:R SC:R SD:R TN:R TX:R WV:R WY:R OH:R FL:R";
        e.issues = "0=1.0 1=1.4 2=0.7 3=0.7 4=1.1 5=0.5 6=0.6 7=0.4 8=0.7 9=0.4 10=0.3 11=0.5 12=0.3 13=0.5";
        ERAS.add(e);

        e = new Era();
        e.year = 2029; e.name = "After Battleground 2028"; e.fictional = true;
        e.blurb = "January 20, 2029, in the world of Battleground 2028. Democrats won the 2026 midterms in a landslide; now a new president takes office after the 2028 election. A projection, not a forecast.";
        e.predecessor = "Donald Trump";
        e.unemp = 4.5; e.infl = 2.7; e.gdp = 1.8; e.fed = 3.4; e.tenYr = 4.3; e.stocks = 6900; e.oil = 72; e.gas = 3.20; e.debt = 104; e.deficit = 6.5; e.nominalGdp = 34.5;
        e.potential = 1.9; e.nairu = 4.3; e.tariff = 11; e.priceLevel = 1.1;
        e.fedChair = "Kevin Warsh";
        e.speakerD = "Hakeem Jeffries"; e.speakerR = "Mike Johnson"; e.leaderD = "Chuck Schumer"; e.leaderR = "John Thune"; e.houseLeaderD = "Hakeem Jeffries"; e.houseLeaderR = "Mike Johnson";
        e.midSeats = "AZ:D CA:D CT:D DE:D HI:D ME:D MD:D MA:D MI:D MN:D NV:D NJ:D NM:D NY:D RI:D VT:D VA:D WA:D WI:D FL:R IN:R MS:R MO:R MT:R NE:R ND:R OH:R PA:R TN:R TX:R UT:R WV:R WY:R";
        e.issues = "0=1.0 1=1.3 2=0.8 3=0.7 4=0.9 5=0.5 6=0.6 7=0.5 8=0.8 9=0.5 10=0.3 11=0.8 12=0.3 13=0.6";
        ERAS.add(e);
    }

    /** Scenarios: a twist on the era's starting conditions. */
    public static final String[] SCEN = {"As it was", "Recession", "A war abroad", "Divided government", "Landslide mandate", "Inflation shock"};

    public static final String[] SCEN_DESC = {
            "The country as it stood on inauguration day.",
            "The economy is shrinking: unemployment is up two points and markets are down a fifth.",
            "American forces are fighting abroad. The country has rallied around you, for now; oil is up and the deficit with it.",
            "The other party controls both the House and the Senate.",
            "You won big: higher approval and a bigger majority in both chambers.",
            "Prices are surging: inflation is three points higher, gas is up a quarter and the Fed is tightening."};

    /** Starting seats for the president's party when the era leaves it to the election. */
    public static int[] seatsFor(Era e, int party) {
        if (e.houseD >= 0) return new int[]{e.houseD, e.senateD};
        return party == DEM ? new int[]{232, 51} : party == REP ? new int[]{206, 48} : new int[]{218, 50};
    }
}

package resolutedesk.data;

import static resolutedesk.data.StateData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Factions.*;
import static resolutedesk.model.Parties.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.model.Cand;
import resolutedesk.model.Look;

/** The real politicians of earlier eras, and who ran in each election. */
public final class HistoricalFigures {
    private HistoricalFigures() {
    }

    //  how each state leaned in each era, the Senate's classes and the Supreme Court year by year.
    //  Real people appear as stylised characters with short factual bios; the game never quotes them.
    public static final List<Cand> HIST = new ArrayList<Cand>();

    /** id|first|last|party|role|home|born|econ|cult|CHA SPE DEB INT DIS EXE FPC XOV EST STA SCA|look|bio */
    public static final String[] HSPEC = {
            "fdr|Franklin D.|Roosevelt|D|Governor of New York|NY|1882|-0.7|-0.2|95 96 80 80 80 82 80 82 80 60 30|M 0 6 2 0.5 0 1 1.05 0.55 2 3 0.72 0.75|Governor of New York and a former assistant secretary of the Navy, elected in 1932 promising a New Deal for the forgotten man. A patrician with a gift for talking to ordinary people.",
            "garner|John Nance|Garner|D|Speaker of the House|TX|1868|-0.1|0.5|55 50 55 70 70 60 40 55 90 55 30|M 0 7 0 0.9 0 0 1.05 0.6 0 1 0.85 0.5|Texas congressman for thirty years and Speaker of the House; a conservative Southern Democrat who knows every deal in the Capitol.",
            "wallace|Henry A.|Wallace|D|Secretary of Agriculture|IA|1888|-0.8|-0.4|55 55 50 88 65 70 60 45 45 70 30|M 0 3 2 0.3 0 0 1.0 0.5 0 2 0.5 0.55|Iowa farm editor and plant geneticist who founded a seed company, then ran the New Deal's farm program; the left wing's favorite.",
            "truman|Harry S.|Truman|D|U.S. Senator, Missouri|MO|1884|-0.5|-0.1|60 60 62 72 78 72 65 60 70 75 30|M 0 6 1 0.7 0 2 1.0 0.5 0 2 0.75 0.6|Missouri senator who led the committee rooting out waste in war production; a plain-spoken First World War artillery captain from Independence.",
            "barkley|Alben|Barkley|D|Senate Majority Leader|KY|1877|-0.5|0|72 80 70 65 60 55 50 60 85 55 30|M 0 6 0 0.9 0 0 1.08 0.6 0 1 0.85 0.75|Kentucky senator since 1927 and the Senate's Democratic leader; a famous storyteller and stump speaker.",
            "stevenson|Adlai|Stevenson|D|Governor of Illinois|IL|1900|-0.4|-0.3|72 88 70 88 70 70 72 60 65 65 25|M 0 6 0 0.6 0 0 1.0 0.5 0 2 0.65 0.6|Governor of Illinois and a lawyer who helped found the United Nations; eloquent and witty, beloved by intellectuals, often called too cerebral for the stump.",
            "sparkman|John|Sparkman|D|U.S. Senator, Alabama|AL|1899|-0.3|0.5|50 50 50 60 65 50 45 50 70 60 30|M 0 6 2 0.6 0 1 1.0 0.5 0 1 0.65 0.5|Alabama senator and a New Deal supporter on housing and small business.",
            "kefauver|Estes|Kefauver|D|U.S. Senator, Tennessee|TN|1903|-0.4|-0.1|68 60 62 70 65 50 50 65 45 70 30|M 0 2 1 0.3 0 1 1.0 0.55 0 1 0.6 0.6|Tennessee senator who became a national figure with televised hearings on organized crime; campaigns in a coonskin cap.",
            "eisenhower|Dwight D.|Eisenhower|R|General of the Army|KS|1890|0.3|0.2|88 70 60 78 82 92 98 92 75 70 20|M 0 5 0 0.5 0 0 1.05 0.55 0 1 0.7 0.88|Supreme Allied Commander in Europe in the Second World War, then president of Columbia University and NATO's first supreme commander. Belonged to no party until 1952.",
            "nixon|Richard|Nixon|R|Vice President|CA|1913|0.3|0.4|55 70 72 85 70 75 85 55 70 75 50|M 0 1 1 0.3 0 0 1.0 0.62 0 2 0.6 0.45|California congressman and senator who made his name pursuing communists in government, then Eisenhower's vice president for eight years. Tireless, tough and shrewd about foreign policy.",
            "jfk|John F.|Kennedy|D|U.S. Senator, Massachusetts|MA|1917|-0.4|-0.3|92 90 85 78 70 60 70 72 60 65 45|M 0 3 2 0 0 0 1.0 0.5 0 2 0.3 0.75|Massachusetts senator and decorated PT-boat commander in the Pacific; elected at 43, the first Catholic president.",
            "lbj|Lyndon B.|Johnson|D|Vice President|TX|1908|-0.6|-0.4|65 60 55 80 70 85 60 60 98 65 45|M 0 6 1 0.6 0 0 1.08 0.65 0 1 0.65 0.55|The most effective Senate majority leader of the century, then Kennedy's vice president; succeeded to the presidency in November 1963 and won a landslide a year later.",
            "humphrey|Hubert|Humphrey|D|U.S. Senator, Minnesota|MN|1911|-0.6|-0.6|70 85 75 78 55 60 60 55 80 75 25|M 0 1 0 0.4 0 0 1.0 0.55 0 1 0.6 0.8|Minnesota senator and the Senate's leading civil rights champion, then Johnson's vice president; known as the Happy Warrior.",
            "hoover|Herbert|Hoover|R|President|CA|1874|0.5|0.3|35 40 45 92 80 85 75 40 70 70 20|M 0 2 1 0.5 0 0 1.12 0.6 0 1 0.7 0.3|Mining engineer and humanitarian who fed Europe after the First World War; commerce secretary, then president from 1929 as the Depression deepened.",
            "landon|Alf|Landon|R|Governor of Kansas|KS|1887|0.2|0|50 45 45 65 70 70 45 60 55 65 20|M 0 3 1 0.4 0 2 1.0 0.5 0 1 0.6 0.6|Kansas oilman and governor, one of the few Republicans to win re-election in 1934; a moderate who backs parts of the New Deal.",
            "knox|Frank|Knox|R|Publisher, Chicago Daily News|IL|1874|0.5|0.2|55 55 50 60 65 70 70 50 55 60 20|M 0 6 0 0.9 0 0 1.05 0.55 0 1 0.8 0.5|Chicago newspaper publisher and a Rough Rider in the Spanish-American War; an internationalist Republican.",
            "willkie|Wendell|Willkie|R|Businessman|IN|1892|0.3|-0.2|82 75 70 75 50 70 60 65 30 70 25|M 0 2 2 0.2 0 0 1.05 0.55 0 1 0.5 0.7|Utility executive and former Democrat from Indiana who fought the New Deal's public power projects; a charismatic political amateur who backs aid to Britain.",
            "mcnary|Charles|McNary|R|Senate Republican Leader|OR|1874|0|0|45 45 50 65 70 55 45 60 85 55 20|M 0 6 0 0.8 0 0 1.0 0.5 0 1 0.85 0.5|Oregon senator and Republican leader, a progressive on farm and public power issues.",
            "dewey|Thomas E.|Dewey|R|Governor of New York|NY|1902|0.2|0|60 65 65 80 80 82 65 60 65 70 20|M 0 1 1 0 4 0 1.0 0.5 0 1 0.45 0.55|Racket-busting Manhattan prosecutor turned governor of New York; efficient and polished, the leader of the party's internationalist wing.",
            "bricker|John|Bricker|R|Governor of Ohio|OH|1893|0.6|0.4|50 50 50 60 65 65 45 45 65 60 20|M 0 7 2 0.9 0 0 1.05 0.55 0 1 0.7 0.5|Ohio governor and a favorite of the party's conservative Old Guard.",
            "warren|Earl|Warren|R|Governor of California|CA|1891|0|-0.2|70 60 55 75 75 80 55 80 50 70 15|M 0 6 1 0.7 0 0 1.1 0.6 0 1 0.75 0.65|Governor of California, so popular he won the Democratic primary too; a progressive Republican.",
            "taft|Robert A.|Taft|R|U.S. Senator, Ohio|OH|1889|0.7|0.3|40 45 60 88 80 60 50 40 85 60 15|M 0 0 0 0.6 0 1 1.05 0.55 0 1 0.75 0.4|Ohio senator, son of a president and the conservative leader known as Mr. Republican; co-author of the Taft-Hartley Act.",
            "lodge|Henry Cabot|Lodge|R|Ambassador to the UN|MA|1902|0.1|-0.1|65 70 65 75 70 60 80 60 70 65 20|M 0 2 2 0.3 0 0 1.0 0.5 0 1 0.55 0.55|Former Massachusetts senator who persuaded Eisenhower to run in 1952, then his ambassador to the United Nations.",
            "goldwater|Barry|Goldwater|R|U.S. Senator, Arizona|AZ|1909|0.85|0.4|70 65 60 65 50 55 55 35 55 70 25|M 0 6 2 0.7 0 2 1.0 0.6 0 1 0.6 0.55|Arizona senator, Air Force reserve general and author of The Conscience of a Conservative; the hero of the party's new right.",
            "agnew|Spiro|Agnew|R|Governor of Maryland|MD|1918|0.4|0.6|50 55 50 55 50 55 30 40 40 65 45|M 0 1 0 0.5 0 0 1.0 0.55 0 1 0.55 0.5|Governor of Maryland and a sharp-tongued law-and-order voice.",
            "muskie|Edmund|Muskie|D|U.S. Senator, Maine|ME|1914|-0.4|-0.3|68 65 65 72 60 65 55 65 65 65 20|M 0 6 2 0.6 0 0 1.05 0.6 0 1 0.6 0.55|Maine senator and former governor; author of the clean air and clean water laws, calm and Lincolnesque.",
            "mcgovern|George|McGovern|D|U.S. Senator, South Dakota|SD|1922|-0.7|-0.6|55 60 60 75 60 50 60 40 45 70 20|M 0 2 2 0.4 0 0 1.0 0.5 0 1 0.5 0.6|South Dakota senator, a decorated B-24 pilot and the Senate's leading voice against the Vietnam War.",
            "shriver|Sargent|Shriver|D|Former Ambassador to France|MD|1915|-0.6|-0.4|70 65 60 70 65 75 70 55 60 70 20|M 0 2 1 0.4 0 0 1.0 0.55 0 1 0.55 0.75|First director of the Peace Corps and of the war on poverty.",
            "ford|Gerald|Ford|R|House Republican Leader|MI|1913|0.4|0.1|55 45 45 70 75 70 70 60 90 75 15|M 0 4 0 0.5 0 0 1.05 0.55 0 1 0.65 0.65|Michigan congressman and House Republican leader for nine years; appointed vice president in 1973 and president in 1974. Known for decency and candor.",
            "carter|Jimmy|Carter|D|Governor of Georgia|GA|1924|-0.2|-0.1|70 55 60 85 80 60 55 60 25 75 10|M 0 4 2 0.2 0 0 1.0 0.5 0 1 0.55 0.85|Navy nuclear engineer, peanut farmer and one-term governor of Georgia who ran as an outsider promising never to lie.",
            "mondale|Walter|Mondale|D|U.S. Senator, Minnesota|MN|1928|-0.55|-0.5|55 55 60 78 70 65 60 50 75 70 15|M 0 2 1 0.4 0 0 1.0 0.55 0 1 0.55 0.55|Minnesota senator and Humphrey's protege; made the vice presidency a working job.",
            "dole|Bob|Dole|R|U.S. Senator, Kansas|KS|1923|0.5|0.3|55 50 60 75 75 60 60 50 90 65 20|M 0 1 2 0.4 0 0 1.0 0.6 0 1 0.6 0.45|Kansas senator gravely wounded in Italy in 1945; a dry wit and a master deal-maker who rose to lead Senate Republicans.",
            "reagan|Ronald|Reagan|R|Former Governor of California|CA|1911|0.75|0.65|95 95 82 65 72 75 60 72 45 70 25|M 0 2 3 0.15 0 0 1.05 0.55 0 1 0.7 0.85|Movie actor, Screen Actors Guild president and two-term governor of California; the conservative movement's great communicator.",
            "bush41|George H. W.|Bush|R|Vice President|TX|1924|0.45|0.35|55 50 55 78 75 75 95 55 85 70 15|M 0 2 1 0.5 0 0 1.0 0.55 0 1 0.65 0.6|Navy pilot shot down in the Pacific, Texas oilman, congressman, UN ambassador, envoy to China and CIA director; Reagan's vice president for eight years.",
            "ferraro|Geraldine|Ferraro|D|U.S. Representative, New York|NY|1935|-0.4|-0.4|65 65 60 65 60 45 30 50 55 70 30|F 0 4 7 0.1 0 0 1.0 0.45 0 1 0.45 0.65 E|Queens congresswoman and former prosecutor; the first woman on a major-party national ticket.",
            "quayle|Dan|Quayle|R|U.S. Senator, Indiana|IN|1947|0.6|0.6|60 45 40 55 55 45 40 45 55 75 25|M 0 4 2 0 0 0 0.98 0.5 0 1 0.3 0.65|Indiana senator who co-wrote the main federal job-training law.",
            "dukakis|Michael|Dukakis|D|Governor of Massachusetts|MA|1933|-0.3|-0.4|50 50 55 82 80 75 45 50 50 70 15|M 1 0 1 0.2 0 0 1.0 0.5 0 1 0.5 0.6|Three-term governor of Massachusetts credited with the state's economic turnaround.",
            "bentsen|Lloyd|Bentsen|D|U.S. Senator, Texas|TX|1921|-0.1|0.1|65 60 75 78 75 65 65 65 85 65 15|M 0 6 1 0.7 0 0 1.0 0.55 0 1 0.65 0.55|Texas senator, B-24 squadron commander and businessman; chairman of the Senate Finance Committee.",
            "clinton|Bill|Clinton|D|Governor of Arkansas|AR|1946|-0.25|-0.3|95 88 85 90 45 70 50 72 60 80 70|M 0 6 2 0.4 0 0 1.05 0.55 0 1 0.45 0.8|Five-term governor of Arkansas and a founder of the centrist Democratic Leadership Council; a gifted, tireless campaigner.",
            "gore|Al|Gore|D|U.S. Senator, Tennessee|TN|1948|-0.35|-0.4|50 55 65 85 75 65 65 50 75 75 15|M 0 1 2 0.2 0 0 1.0 0.55 0 1 0.45 0.5|Tennessee senator and congressman who championed the environment and the early internet; Clinton's vice president for eight years.",
            "jkemp|Jack|Kemp|R|Former Secretary of Housing|NY|1935|0.7|0.2|72 70 65 72 60 60 45 60 60 75 15|M 0 2 2 0.4 0 0 1.0 0.55 0 1 0.55 0.75|Former Buffalo Bills quarterback, congressman and housing secretary; the party's tax-cutting evangelist.",
            "bush43|George W.|Bush|R|Governor of Texas|TX|1946|0.6|0.65|78 60 60 60 75 70 40 65 70 75 30|M 0 6 2 0.5 0 0 1.0 0.55 0 1 0.5 0.7|Two-term governor of Texas and son of the 41st president; ran as a compassionate conservative.",
            "cheney|Dick|Cheney|R|Former Secretary of Defense|WY|1941|0.75|0.6|35 45 70 85 85 85 88 40 90 55 30|M 0 6 0 0.6 0 1 1.05 0.55 0 1 0.6 0.35|Wyoming congressman, White House chief of staff at 34 and secretary of defense during the Gulf War.",
            "mccain|John|McCain|R|U.S. Senator, Arizona|AZ|1936|0.55|0.4|70 60 60 70 55 55 85 70 70 65 25|M 0 7 1 0.9 0 0 1.0 0.55 0 1 0.75 0.6|Arizona senator and Navy pilot who spent five and a half years as a prisoner of war in Hanoi; the Senate's maverick on campaign finance and immigration.",
            "lieberman|Joe|Lieberman|D|U.S. Senator, Connecticut|CT|1942|-0.2|-0.1|55 55 55 70 70 55 65 60 70 65 15|M 0 0 1 0.3 0 1 1.0 0.5 0 1 0.55 0.6|Connecticut senator, a centrist and a hawk; the first Jewish candidate on a major-party ticket.",
            "kerry|John|Kerry|D|U.S. Senator, Massachusetts|MA|1943|-0.45|-0.45|55 65 72 82 70 50 80 45 75 70 20|M 0 6 2 0.5 0 0 0.95 0.65 0 1 0.55 0.5|Massachusetts senator and a decorated Navy officer in Vietnam who later spoke for veterans against the war.",
            "edwards|John|Edwards|D|U.S. Senator, North Carolina|NC|1953|-0.5|-0.3|80 75 75 65 50 40 40 60 40 75 45|M 0 3 2 0 0 0 1.0 0.5 0 1 0.35 0.8|North Carolina senator and trial lawyer who campaigned on the two Americas.",
            "obama|Barack|Obama|D|U.S. Senator, Illinois|IL|1961|-0.5|-0.55|92 96 75 88 85 55 55 65 50 80 15|M 5 0 0 0.15 0 0 0.95 0.5 0 1 0.4 0.75|Illinois senator, constitutional law lecturer and community organizer whose 2004 convention speech made him a national figure.",
            "biden|Joe|Biden|D|U.S. Senator, Delaware|DE|1942|-0.45|-0.45|70 65 60 78 50 70 85 65 95 55 30|M 0 7 0 0.95 0 0 1.0 0.55 0 1 0.85 0.75|Delaware senator for 36 years and chairman of the Judiciary and Foreign Relations committees, then Obama's vice president for eight years.",
            "hclinton|Hillary|Clinton|D|Former Secretary of State|NY|1947|-0.45|-0.5|55 65 80 92 80 75 90 45 85 70 50|F 0 5 6 0.1 0 0 1.0 0.5 0 1 0.6 0.6 E|First lady, senator from New York and secretary of state.",
            "palin|Sarah|Palin|R|Governor of Alaska|AK|1964|0.5|0.85|85 75 50 40 40 50 20 35 25 70 45|F 0 1 7 0 0 1 0.98 0.45 0 1 0.35 0.75 E L|Governor of Alaska, a former small-town mayor and sportscaster.",
            "romney|Mitt|Romney|R|Former Governor of Massachusetts|MA|1947|0.55|0.4|65 65 75 85 85 85 60 55 60 75 15|M 0 0 2 0.4 0 0 1.0 0.6 0 1 0.6 0.65|Private-equity founder who rescued the 2002 Winter Olympics, then governor of Massachusetts, where he signed near-universal health coverage.",
            "ryan|Paul|Ryan|R|Chairman, House Budget Committee|WI|1970|0.75|0.55|65 65 65 82 75 45 45 45 75 80 15|M 0 1 2 0 0 0 0.98 0.55 0 1 0.3 0.65|Wisconsin congressman and the party's budget expert.",
            "pence|Mike|Pence|R|Governor of Indiana|IN|1959|0.6|0.85|55 65 60 60 80 65 40 40 70 75 15|M 0 7 2 0.95 0 0 1.0 0.55 0 1 0.55 0.5|Governor of Indiana, former talk-radio host and congressman; a conservative evangelical.",
            "kaine|Tim|Kaine|D|U.S. Senator, Virginia|VA|1958|-0.3|-0.3|60 55 55 72 70 70 55 55 65 70 10|M 0 2 2 0.4 0 0 1.0 0.55 0 1 0.5 0.65|Virginia senator and former governor and mayor of Richmond."};

    static {
        for (String spec : HSPEC) {
            String[] t = spec.split("\\|");
            Cand c = new Cand();
            c.id = t[0]; c.first = t[1]; c.last = t[2]; c.party = t[3].equals("D") ? DEM : REP; c.role = t[4]; c.homeAb = t[5]; c.born = Integer.parseInt(t[6]);
            c.faction = c.party == DEM ? F_LIB : F_GOV;
            c.plat = FACTIONS[c.faction].pos.clone();
            c.econ = Double.parseDouble(t[7]); c.cult = Double.parseDouble(t[8]);
            String[] v = t[9].split(" ");
            int[] at = {A_CHA, A_SPE, A_DEB, A_INT, A_DIS, A_EXE, A_FPC, A_XOV, A_EST, A_STA, A_SCA};
            for (int i = 0; i < NA; i++) c.a[i] = 50;
            for (int i = 0; i < at.length; i++) c.a[at[i]] = Integer.parseInt(v[i]);
            c.a[A_POD] = 40; c.a[A_RET] = (c.a[A_CHA] + c.a[A_SPE]) / 2; c.a[A_SMD] = 40; c.a[A_BIG] = 60; c.a[A_SELF] = 10; c.a[A_VOL] = 55;
            c.a[A_VIR] = 40; c.a[A_AUT] = (c.a[A_CHA] + c.a[A_DIS]) / 2; c.a[A_HOM] = 70; c.a[A_BAS] = 65;
            c.look = Look.parse(t[10]);
            c.bio = t[11];
            c.home = stateIdx(c.homeAb);
            HIST.add(c);
        }
    }

    /** Who you can play in each era: "year|D: pres>vp ...|R: ...", winner first; then role overrides "id=role;id=role". */
    public static final String[] ERA_ROSTER = {
            "1933|fdr>garner garner|hoover>knox landon>knox knox|",
            "1937|fdr>garner garner wallace|landon>knox knox|",
            "1941|fdr>wallace wallace truman|willkie>mcnary mcnary dewey>bricker|wallace=Vice President-elect;truman=U.S. Senator, Missouri",
            "1945|fdr>truman truman wallace|dewey>bricker bricker warren|truman=Vice President-elect;wallace=Vice President",
            "1949|truman>barkley barkley stevenson|dewey>warren warren taft|truman=President;barkley=Vice President-elect",
            "1953|stevenson>sparkman sparkman kefauver|eisenhower>nixon nixon taft|stevenson=Governor of Illinois;nixon=Vice President-elect;eisenhower=Former Supreme Allied Commander",
            "1957|stevenson>kefauver kefauver lbj|eisenhower>nixon nixon|stevenson=Former Governor of Illinois;eisenhower=President;lbj=Senate Majority Leader",
            "1961|jfk>lbj lbj humphrey|nixon>lodge lodge goldwater|nixon=Vice President;lbj=Vice President-elect",
            "1965|lbj>humphrey humphrey|goldwater>nixon nixon|lbj=President;humphrey=Vice President-elect;nixon=Former Vice President",
            "1969|humphrey>muskie muskie mcgovern|nixon>agnew agnew|humphrey=Vice President;nixon=Former Vice President",
            "1973|mcgovern>shriver shriver muskie|nixon>agnew agnew ford|nixon=President;agnew=Vice President",
            "1977|carter>mondale mondale|ford>dole dole reagan|ford=President;mondale=Vice President-elect",
            "1981|carter>mondale mondale|reagan>bush41 bush41|carter=President;mondale=Vice President;bush41=Vice President-elect",
            "1985|mondale>ferraro ferraro|reagan>bush41 bush41 dole|reagan=President;mondale=Former Vice President;dole=Senate Majority Leader-elect",
            "1989|dukakis>bentsen bentsen|bush41>quayle quayle dole|bush41=Vice President;quayle=Vice President-elect;dole=Senate Republican Leader",
            "1993|clinton>gore gore|bush41>quayle quayle dole|bush41=President;quayle=Vice President;dole=Senate Republican Leader",
            "1997|clinton>gore gore|dole>jkemp jkemp|clinton=President;gore=Vice President;dole=Former Senate Majority Leader",
            "2001|gore>lieberman lieberman|bush43>cheney cheney mccain|gore=Vice President",
            "2005|kerry>edwards edwards|bush43>cheney cheney|bush43=President;cheney=Vice President",
            "2009|obama>biden biden hclinton|mccain>palin palin romney|biden=Vice President-elect;hclinton=U.S. Senator, New York",
            "2013|obama>biden biden|romney>ryan ryan|obama=President;biden=Vice President",
            "2017|hclinton>kaine kaine|trump>pence pence|trump=President-elect",
            "2021|biden>harris harris|trump>pence pence|biden=Former Vice President;trump=President;pence=Vice President;harris=U.S. Senator, California"};

    public static String rosterRow(int year) { for (String r : ERA_ROSTER) if (r.startsWith(year + "|")) return r; return null; }

    /** The real nominees in each presidential election: "year:D>vp,R>vp". */
    public static final String[] NOMINEES = {"1936:fdr>garner,landon>knox", "1940:fdr>wallace,willkie>mcnary", "1944:fdr>truman,dewey>bricker", "1948:truman>barkley,dewey>warren",
            "1952:stevenson>sparkman,eisenhower>nixon", "1956:stevenson>kefauver,eisenhower>nixon", "1960:jfk>lbj,nixon>lodge", "1964:lbj>humphrey,goldwater>",
            "1968:humphrey>muskie,nixon>agnew", "1972:mcgovern>shriver,nixon>agnew", "1976:carter>mondale,ford>dole", "1980:carter>mondale,reagan>bush41",
            "1984:mondale>ferraro,reagan>bush41", "1988:dukakis>bentsen,bush41>quayle", "1992:clinton>gore,bush41>quayle", "1996:clinton>gore,dole>jkemp",
            "2000:gore>lieberman,bush43>cheney", "2004:kerry>edwards,bush43>cheney", "2008:obama>biden,mccain>palin", "2012:obama>biden,romney>ryan",
            "2016:hclinton>kaine,trump>pence", "2020:biden>harris,trump>pence", "2024:harris>walz,trump>vance", "2028:newsom>shapiro,vance>rubio", "2032:ossoff>moore,rubio>youngkin"};

    /** The real nominee of a party in an election year, or null. */
    public static String nominee(int year, int party) {
        for (String s : NOMINEES) {
            if (!s.startsWith(year + ":")) continue;
            String[] t = s.substring(5).split(",");
            String x = t[party == DEM ? 0 : 1];
            return x.split(">")[0];
        }
        return null;
    }
}

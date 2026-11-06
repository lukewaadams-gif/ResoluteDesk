package resolutedesk.legacy;

/** The historians' rankings, the measures they use, and achievements. */
public final class LegacyData {
    private LegacyData() {
    }

    //  When the presidency ends: the historians' ten measures and where you rank among the real presidents,
    //  the record, an epilogue, your presidential library, achievements and the Hall of Fame.
    /** The C-SPAN 2021 Presidential Historians Survey, in rank order; scores are an internal scale used only to place you. */
    public static final String[] RANKED = {"Abraham Lincoln", "George Washington", "Franklin D. Roosevelt", "Theodore Roosevelt", "Dwight D. Eisenhower", "Harry S. Truman", "Thomas Jefferson",
            "John F. Kennedy", "Ronald Reagan", "Barack Obama", "Lyndon B. Johnson", "James Monroe", "Woodrow Wilson", "William McKinley", "John Adams", "James Madison", "John Quincy Adams",
            "James K. Polk", "Bill Clinton", "Ulysses S. Grant", "George H. W. Bush", "Andrew Jackson", "William Howard Taft", "Calvin Coolidge", "Grover Cleveland", "Jimmy Carter",
            "James A. Garfield", "Gerald Ford", "George W. Bush", "Chester A. Arthur", "Richard Nixon", "Benjamin Harrison", "Rutherford B. Hayes", "Martin Van Buren", "Zachary Taylor",
            "Herbert Hoover", "Warren G. Harding", "Millard Fillmore", "John Tyler", "William Henry Harrison", "Donald Trump", "Franklin Pierce", "Andrew Johnson", "James Buchanan"};

    public static final int[] RANK_SCORE = {897, 851, 841, 785, 734, 713, 704, 699, 681, 664, 654, 643, 617, 612, 609, 604, 603, 599, 594, 590, 585, 568, 543, 535, 523, 507, 506, 498, 495,
            472, 463, 448, 443, 435, 430, 396, 380, 370, 360, 350, 312, 305, 265, 227};

    public static final String[] MEASURE = {"Public persuasion", "Crisis leadership", "Economic management", "Moral authority", "International relations", "Administrative skills",
            "Relations with Congress", "Vision and agenda", "Pursued equal justice for all", "Performance within the times"};

    public static final String[][] ACH = {
            {"twoterms", "Eight years", "Serve two full terms."}, {"thirdterm", "Breaking tradition", "Win a third term (possible before 1951)."}, {"fourth", "Four times", "Win a fourth term."},
            {"landslide", "Landslide", "Win re-election with 400 or more electoral votes."}, {"squeaker", "By a whisker", "Win re-election with no more than 15 electoral votes to spare."},
            {"whatif", "What if?", "Win re-election as a president history didn't choose."}, {"lawmaker", "Lawmaker", "Sign 10 laws."}, {"newdeal", "A new deal", "Sign 20 laws."},
            {"civilrights", "Equal justice", "Sign a civil rights law."}, {"midterm", "Defy the midterms", "Gain House seats in a midterm election."}, {"victory", "Victory", "Win a war."},
            {"peace", "Peacemaker", "End a war by agreement."}, {"dove", "No new wars", "Serve a full term without going to war."}, {"recovery", "The recovery", "Cut unemployment by five points."},
            {"fullemp", "Full employment", "Leave office with unemployment under 4%."}, {"slayer", "Inflation slayer", "Cut inflation by five points."}, {"surplus", "In the black", "Leave office with a budget surplus."},
            {"bull", "Bull market", "Leave the stock market at least twice as high as you found it."}, {"beloved", "Beloved", "Leave office with approval of 60% or more."},
            {"comeback", "The comeback kid", "Win re-election after your approval fell below 40%."}, {"survivor", "Survivor", "Be impeached and not removed."}, {"court3", "A court of your own", "Appoint three Supreme Court justices."},
            {"summits", "Statesman", "Reach five agreements with foreign leaders."}, {"crises", "Steady hand", "See ten crises and turning points go your way."}, {"top10", "The greats", "Historians rank you in the top ten."},
            {"lincoln", "Above Lincoln", "Historians rank you first."}, {"hard", "The hard way", "Finish a term on Hard."}, {"stepdown", "Cincinnatus", "Step aside when you could have run again."},
            {"import", "From the campaign trail", "Take office from Battleground 2028."}, {"eras", "A student of history", "Finish presidencies in five different eras."}, {"moon", "Moonshot", "Commit the country to the Moon, and get there."},
            {"tour", "Globetrotter", "Make ten trips abroad."}};
}

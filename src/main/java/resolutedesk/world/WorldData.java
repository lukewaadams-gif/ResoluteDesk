package resolutedesk.world;

/** The countries of the world: leaders, alliances, power and trade. */
public final class WorldData {
    private WorldData() {
    }

    //  2025 leaders are the real ones on January 20, 2025, with the real handovers that followed.
    //  2029 leaders project today's (late 2026) into 2029; where a leader is term-limited by then, the successor is fictional.
    public static final String[] CT_ID = {"CN", "RU", "UA", "GB", "FR", "DE", "IT", "CA", "MX", "JP", "KR", "KP", "TW", "IN", "PK", "IL", "IR", "SA", "TR", "EG",
            "QA", "AE", "JO", "IQ", "SY", "BR", "AR", "VE", "CU", "PA", "PL", "HU", "DK", "FI", "AU", "PH", "ID", "VN", "NG", "ZA"};

    public static final int NC = 40;

    public static final int CC_CN = 0, CC_RU = 1, CC_UA = 2, CC_GB = 3, CC_FR = 4, CC_DE = 5, CC_IT = 6, CC_CA = 7, CC_MX = 8, CC_JP = 9, CC_KR = 10, CC_KP = 11, CC_TW = 12, CC_IN = 13, CC_PK = 14, CC_IL = 15, CC_IR = 16, CC_SA = 17, CC_TR = 18, CC_EG = 19,
            CC_QA = 20, CC_AE = 21, CC_JO = 22, CC_IQ = 23, CC_SY = 24, CC_BR = 25, CC_AR = 26, CC_VE = 27, CC_CU = 28, CC_PA = 29, CC_PL = 30, CC_HU = 31, CC_DK = 32, CC_FI = 33, CC_AU = 34, CC_PH = 35, CC_ID = 36, CC_VN = 37, CC_NG = 38, CC_ZA = 39;

    public static final String[] CT_NAME = {"China", "Russia", "Ukraine", "United Kingdom", "France", "Germany", "Italy", "Canada", "Mexico", "Japan", "South Korea", "North Korea",
            "Taiwan", "India", "Pakistan", "Israel", "Iran", "Saudi Arabia", "Turkey", "Egypt", "Qatar", "United Arab Emirates", "Jordan", "Iraq", "Syria", "Brazil", "Argentina",
            "Venezuela", "Cuba", "Panama", "Poland", "Hungary", "Denmark", "Finland", "Australia", "Philippines", "Indonesia", "Vietnam", "Nigeria", "South Africa"};

    public static final int RG_ASIA = 0, RG_EUROPE = 1, RG_MIDEAST = 2, RG_AMERICAS = 3, RG_AFRICA = 4, RG_SOUTHASIA = 5;

    public static final String[] REGION = {"East Asia and the Pacific", "Europe", "The Middle East", "The Americas", "Africa", "South Asia"};

    public static final int[] CT_REGION = {0, 1, 1, 1, 1, 1, 1, 3, 3, 0, 0, 0, 0, 5, 5, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 1, 1, 1, 1, 0, 0, 0, 0, 4, 4};

    /** Leaders on January 20, 2025: "name|title". */
    public static final String[] LEADER25 = {"Xi Jinping|President", "Vladimir Putin|President", "Volodymyr Zelensky|President", "Keir Starmer|Prime Minister", "Emmanuel Macron|President",
            "Olaf Scholz|Chancellor", "Giorgia Meloni|Prime Minister", "Justin Trudeau|Prime Minister", "Claudia Sheinbaum|President", "Shigeru Ishiba|Prime Minister",
            "Choi Sang-mok|Acting President", "Kim Jong Un|Supreme Leader", "Lai Ching-te|President", "Narendra Modi|Prime Minister", "Shehbaz Sharif|Prime Minister",
            "Benjamin Netanyahu|Prime Minister", "Ali Khamenei|Supreme Leader", "Mohammed bin Salman|Crown Prince", "Recep Tayyip Erdogan|President", "Abdel Fattah el-Sisi|President",
            "Tamim bin Hamad Al Thani|Emir", "Mohamed bin Zayed|President", "Abdullah II|King", "Mohammed Shia al-Sudani|Prime Minister", "Ahmed al-Sharaa|Leader",
            "Luiz Inacio Lula da Silva|President", "Javier Milei|President", "Nicolas Maduro|President", "Miguel Diaz-Canel|President", "Jose Raul Mulino|President",
            "Donald Tusk|Prime Minister", "Viktor Orban|Prime Minister", "Mette Frederiksen|Prime Minister", "Alexander Stubb|President", "Anthony Albanese|Prime Minister",
            "Ferdinand Marcos Jr.|President", "Prabowo Subianto|President", "To Lam|General Secretary", "Bola Tinubu|President", "Cyril Ramaphosa|President"};

    /** Real handovers after inauguration day 2025: "country|week|name|title". */
    public static final String[] CHANGES25 = {"CA|7|Mark Carney|Prime Minister", "DE|15|Friedrich Merz|Chancellor", "KR|19|Lee Jae-myung|President", "JP|39|Sanae Takaichi|Prime Minister",
            "HU|68|Peter Magyar|Prime Minister", "IQ|69|Ali al-Zaidi|Prime Minister", "GB|78|Andy Burnham|Prime Minister"};

    /** Leaders in the 2029 projection. Starred names are fictional successors to leaders who will be term-limited by then. */
    public static final String[] LEADER29 = {"Xi Jinping|President", "Vladimir Putin|President", "Volodymyr Zelensky|President", "Andy Burnham|Prime Minister", "*Claire Morel|President",
            "Friedrich Merz|Chancellor", "Giorgia Meloni|Prime Minister", "Mark Carney|Prime Minister", "Claudia Sheinbaum|President", "Sanae Takaichi|Prime Minister",
            "Lee Jae-myung|President", "Kim Jong Un|Supreme Leader", "Lai Ching-te|President", "Narendra Modi|Prime Minister", "Shehbaz Sharif|Prime Minister",
            "Benjamin Netanyahu|Prime Minister", "Mojtaba Khamenei|Supreme Leader", "Mohammed bin Salman|Crown Prince", "Recep Tayyip Erdogan|President", "Abdel Fattah el-Sisi|President",
            "Tamim bin Hamad Al Thani|Emir", "Mohamed bin Zayed|President", "Abdullah II|King", "Ali al-Zaidi|Prime Minister", "Ahmed al-Sharaa|President",
            "Luiz Inacio Lula da Silva|President", "Javier Milei|President", "Delcy Rodriguez|President", "*Ernesto Valdes|President", "Jose Raul Mulino|President",
            "Donald Tusk|Prime Minister", "Peter Magyar|Prime Minister", "Mette Frederiksen|Prime Minister", "Alexander Stubb|President", "Anthony Albanese|Prime Minister",
            "*Ramon Delgado|President", "Prabowo Subianto|President", "To Lam|General Secretary", "Bola Tinubu|President", "Cyril Ramaphosa|President"};

    /** 0 none, 1 partner, 2 treaty ally, 3 adversary. */
    public static final int AL_NONE = 0, AL_PARTNER = 1, AL_TREATY = 2, AL_ADVERSARY = 3;

    public static final String[] ALIGN = {"Neutral", "Partner", "Treaty ally", "Adversary"};

    public static final int[] CT_ALIGN = {3, 3, 1, 2, 2, 2, 2, 2, 1, 2, 2, 3, 1, 1, 0, 1, 3, 1, 2, 1, 1, 1, 1, 1, 0, 0, 1, 3, 3, 1, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0};

    /** Starting relations with Washington, -100 to 100. */
    public static final double[] CT_REL = {-35, -60, 55, 65, 50, 55, 55, 50, 35, 70, 60, -85, 60, 40, 5, 70, -75, 40, 20, 30, 40, 45, 50, 15, -20, 10, 55, -55, -60, 30, 60, 30, 45, 60, 65, 55, 25, 15, 15, 0};

    /** Weight in the world, 1 to 10. */
    public static final double[] CT_POWER = {10, 8, 4, 7, 7, 7, 5, 6, 5, 7, 5, 4, 4, 7, 4, 5, 5, 5, 5, 4, 3, 3, 2, 2, 2, 5, 3, 2, 1, 1, 4, 2, 3, 3, 5, 3, 4, 3, 3, 3};

    /** Two-way goods trade with the US, $ billions a year (2024), and the US balance (negative: a deficit). */
    public static final double[] CT_TRADE = {582, 3.5, 4, 148, 103, 236, 102, 762, 840, 228, 197, 0, 158, 129, 7, 37, 0.1, 26, 33, 9, 6, 34, 5, 9, 0.1, 92, 16, 6, 0.5, 9, 18, 9, 14, 9, 51, 23, 38, 149, 9, 22};

    public static final double[] CT_BAL = {-295, -2.5, 2, 12, -16, -85, -44, -63, -172, -69, -66, 0, -74, -46, -3, -7, 0, 0.5, -0.5, 3.5, 2.5, 19, -1, -6, 0, 7, 2, -1.5, 0.4, 9, -2, -6, -10, -3, 18, -5, -18, -123, -1.5, -9};

    public static final boolean[] CT_NUKE = {true, true, false, true, true, false, false, false, false, false, false, true, false, true, true, true, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

    /** Map label positions in world units. */
    public static final double[] CT_LX = {772, 668, 575, 492, 508, 525, 531, 298, 220, 854, 832, 826, 828, 726, 684, 593, 640, 620, 594, 583, 638, 647, 598, 617, 600, 344, 332, 315, 284, 278, 545, 548, 519, 554, 860, 835, 816, 791, 523, 566};

    public static final double[] CT_LY = {128, 61, 93, 78, 99, 86, 110, 67, 170, 134, 131, 120, 170, 170, 150, 146, 140, 171, 125, 155, 166, 170, 148, 140, 134, 276, 362, 222, 178, 219, 85, 97, 80, 44, 319, 198, 246, 193, 215, 336};

    /** Stylised flags: "h:" stripes across, "v:" stripes down, "d:" a disc, "x:" a Nordic cross, "s:" a star in the corner, "c:" a centred star;
     *  then optional "|t:" a triangle at the hoist, "|k:" a canton and "|o:" a small centred disc. */
    public static final String[] CT_FLAG = {"s:de2910,ffde00", "h:ffffff,0039a6,d52b1e", "h:0057b7,ffd700", "x:012169,c8102e|k:012169", "v:0055a4,ffffff,ef4135", "h:000000,dd0000,ffce00",
            "v:009246,ffffff,ce2b37", "v:d52b1e,ffffff,ffffff,d52b1e|o:d52b1e", "v:006847,ffffff,ce1126", "d:ffffff,bc002d", "d:ffffff,c60c30|o:003478", "h:024fa2,ed1c27,ed1c27,024fa2|o:ffffff",
            "h:fe0000,fe0000|k:000095", "h:ff9933,ffffff,138808|o:000080", "v:ffffff,01411c,01411c,01411c", "h:ffffff,0038b8,ffffff,ffffff,0038b8,ffffff", "h:239f40,ffffff,da0000",
            "s:006c35,ffffff", "s:e30a17,ffffff", "h:ce1126,ffffff,000000", "v:ffffff,8a1538,8a1538,8a1538", "h:00732f,ffffff,000000|t:ff0000", "h:000000,ffffff,007a3d|t:ce1126",
            "h:ce1126,ffffff,000000", "h:007a3d,ffffff,000000", "d:009c3b,ffdf00|o:002776", "h:74acdf,ffffff,74acdf", "h:ffcc00,00247d,cf142b", "h:002a8f,ffffff,002a8f,ffffff,002a8f|t:cf142b",
            "v:ffffff,da121a|k:ffffff", "h:ffffff,dc143c", "h:ce2939,ffffff,477050", "x:c8102e,ffffff", "x:ffffff,003580", "h:00008b,00008b|k:012169", "h:0038a8,ce1126|t:ffffff",
            "h:ff0000,ffffff", "c:da251d,ffff00", "v:008751,ffffff,008751", "h:e03c31,007749,002395|t:000000"};

    public static int ctIdx(String id) { for (int i = 0; i < NC; i++) if (CT_ID[i].equals(id)) return i; return -1; }

    /** Hours on Air Force One from Washington. */
    public static double flightAbroad(int c) {
        double dx = (CT_LX[c] - 280) * 0.042, dy = (CT_LY[c] - 125) * 0.06;
        return Math.max(1.5, Math.sqrt(dx * dx + dy * dy) * 1.35 + 0.5);
    }
}

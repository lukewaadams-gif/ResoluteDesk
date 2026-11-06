package resolutedesk.executive;

import static resolutedesk.executive.CourtData.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.model.Era;

/** The Supreme Court as it sat in each era. */
public final class CourtHistory {
    private CourtHistory() {
    }

    // ---- the Supreme Court: every justice since 1930, with the years they served ------------------------------
    /** name|born|appointing party|ideology|from|to */
    public static final String[] JUSTICES = {
            "Charles Evans Hughes|1862|R|0.1|1930.1|1941.5", "Willis Van Devanter|1859|R|0.8|1911|1937.4", "James McReynolds|1862|D|0.9|1914.7|1941.08", "Louis Brandeis|1856|D|-0.7|1916.4|1939.1",
            "George Sutherland|1862|R|0.8|1922.7|1938.05", "Pierce Butler|1866|D|0.85|1923.0|1939.9", "Harlan Fiske Stone|1872|R|-0.3|1925.2|1946.3", "Owen Roberts|1875|R|0.2|1930.4|1945.6",
            "Benjamin Cardozo|1870|R|-0.6|1932.2|1938.5", "Hugo Black|1886|D|-0.6|1937.6|1971.7", "Stanley Reed|1884|D|-0.2|1938.08|1957.15", "Felix Frankfurter|1882|D|0.0|1939.08|1962.7",
            "William O. Douglas|1898|D|-0.9|1939.3|1975.9", "Frank Murphy|1890|D|-0.7|1940.1|1949.5", "James Byrnes|1882|D|0.2|1941.5|1942.8", "Robert Jackson|1892|D|-0.1|1941.5|1954.8",
            "Wiley Rutledge|1894|D|-0.7|1943.1|1949.7", "Harold Burton|1888|R|0.3|1945.8|1958.8", "Fred Vinson|1890|D|0.3|1946.5|1953.7", "Tom Clark|1899|D|0.2|1949.6|1967.5",
            "Sherman Minton|1890|D|0.3|1949.8|1956.8", "Earl Warren|1891|R|-0.7|1953.8|1969.5", "John Marshall Harlan|1899|R|0.3|1955.2|1971.7", "William Brennan|1906|R|-0.85|1956.8|1990.6",
            "Charles Whittaker|1901|R|0.3|1957.2|1962.2", "Potter Stewart|1915|R|0.1|1958.8|1981.5", "Byron White|1917|D|0.2|1962.3|1993.5", "Arthur Goldberg|1908|D|-0.8|1962.8|1965.6",
            "Abe Fortas|1910|D|-0.7|1965.8|1969.4", "Thurgood Marshall|1908|D|-0.9|1967.7|1991.6", "Warren Burger|1907|R|0.4|1969.5|1986.7", "Harry Blackmun|1908|R|-0.4|1970.4|1994.6",
            "Lewis Powell|1907|R|0.2|1972.02|1987.5", "William Rehnquist|1924|R|0.8|1972.02|2005.7", "John Paul Stevens|1920|R|-0.5|1975.9|2010.5", "Sandra Day O'Connor|1930|R|0.2|1981.7|2006.1",
            "Antonin Scalia|1936|R|0.85|1986.7|2016.1", "Anthony Kennedy|1936|R|0.3|1988.1|2018.6", "David Souter|1939|R|-0.4|1990.8|2009.5", "Clarence Thomas|1948|R|0.95|1991.8|2100",
            "Ruth Bader Ginsburg|1933|D|-0.8|1993.6|2020.7", "Stephen Breyer|1938|D|-0.6|1994.6|2022.5", "John Roberts|1955|R|0.55|2005.7|2100", "Samuel Alito|1950|R|0.85|2006.08|2100",
            "Sonia Sotomayor|1954|D|-0.85|2009.6|2100", "Elena Kagan|1960|D|-0.7|2010.6|2100", "Neil Gorsuch|1967|R|0.75|2017.3|2100", "Brett Kavanaugh|1965|R|0.6|2018.8|2100",
            "Amy Coney Barrett|1972|R|0.7|2020.8|2100", "Ketanji Brown Jackson|1970|D|-0.8|2022.5|2100"};

    public static final String[] FEMALE_JUSTICES = {"Sandra Day O'Connor", "Ruth Bader Ginsburg", "Sonia Sotomayor", "Elena Kagan", "Amy Coney Barrett", "Ketanji Brown Jackson"};

    /** The Court on inauguration day, as "name|born|D or R|ideology" rows. */
    public static List<String> courtIn(Era e) {
        List<String> l = new ArrayList<String>();
        if (e.year >= 2025) { for (String s : SCOTUS25) l.add(s); return l; }
        double t = e.year + (e.inM * 30.5 + e.inD) / 365.0;
        for (String j : JUSTICES) {
            String[] p = j.split("\\|");
            if (Double.parseDouble(p[4]) <= t && Double.parseDouble(p[5]) > t) l.add(p[0] + "|" + p[1] + "|" + p[2] + "|" + p[3]);
        }
        return l;
    }

    /** Share of the lower federal bench appointed by Democrats: {district, circuit}. */
    public static double[] benchIn(int year) {
        int[] y = {1933, 1941, 1949, 1953, 1961, 1969, 1977, 1981, 1989, 1993, 2001, 2009, 2017, 2021, 2025};
        double[] d = {0.30, 0.62, 0.75, 0.78, 0.55, 0.60, 0.50, 0.58, 0.38, 0.32, 0.50, 0.42, 0.50, 0.44, 0.48};
        int k = 0;
        for (int i = 0; i < y.length; i++) if (year >= y[i]) k = i;
        return new double[]{d[k], d[k] - 0.03};
    }
}

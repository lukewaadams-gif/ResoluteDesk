package resolutedesk.legacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import resolutedesk.io.SaveGame;

/** Where the Hall of Fame lives: a text file next to the saves (in the browser, local storage). */
public final class HallStore {
    public static boolean off;                 // the test harness keeps its games out of the Hall of Fame

    static String read() {
        try {
            File f = new File(SaveGame.dir(), "hall.txt");
            if (!f.exists()) return "";
            BufferedReader r = new BufferedReader(new FileReader(f));
            StringBuilder b = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) b.append(line).append('\n');
            r.close();
            return b.toString();
        } catch (Throwable t) { return ""; }
    }

    static void write(String text) {
        try {
            FileWriter w = new FileWriter(new File(SaveGame.dir(), "hall.txt"));
            w.write(text);
            w.close();
        } catch (Throwable t) { /* best effort */ }
    }
}

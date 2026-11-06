package resolutedesk.ui;

/** Playing interface sounds. */
public final class Sound {
    private Sound() {
    }

    public static Sfx sfx;

    public static void snd(int id) { if (sfx != null) sfx.play(id, 1, 1); }

    public static void snd(int id, double vol, double pitch) { if (sfx != null) sfx.play(id, vol, pitch); }
}

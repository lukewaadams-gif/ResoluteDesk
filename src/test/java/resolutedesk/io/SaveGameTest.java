package resolutedesk.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

public class SaveGameTest {
    /** A saved game, loaded again, plays on exactly as the original does. */
    @Test
    public void aLoadedGameContinuesExactlyAsTheOriginal() throws Exception {
        Game g = Games.start(2024, 2029);
        Games.play(g, 30);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(g);
        out.close();
        Game copy = (Game) new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        SaveGame.upgrade(copy);
        Games.play(g, 20);
        Games.play(copy, 20);
        Check.equal(g.week, copy.week, "week");
        Check.equal(g.approval, copy.approval, "approval");
        Check.equal(g.econ.unemp, copy.econ.unemp, "unemployment");
        Check.equal(g.cong.laws, copy.cong.laws, "laws passed");
    }
}

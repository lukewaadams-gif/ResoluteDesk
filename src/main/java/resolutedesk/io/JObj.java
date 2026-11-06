package resolutedesk.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A minimal reader for Java's serialization format: objects become class names and field maps, so no Battleground classes are needed. */
public final class JObj {
    String cls;
    Map<String, Object> f = new HashMap<String, Object>();
    List<Object> extra = new ArrayList<Object>();
}

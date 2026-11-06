package resolutedesk.io;

/** A class description read from a Java serialization stream. */
public final class JDesc {
    String name;
    int flags;
    char[] types = new char[0];
    String[] fields = new String[0];
    JDesc sup;
}

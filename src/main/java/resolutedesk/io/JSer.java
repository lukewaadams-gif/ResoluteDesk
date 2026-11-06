package resolutedesk.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Reads the Java serialization format without the classes that wrote it, so Battleground 2028 saves can be read. */
public final class JSer {
    static final Object BLOCK = new Object();
    final DataInputStream in;
    final List<Object> handles = new ArrayList<Object>();

    JSer(InputStream s) { in = new DataInputStream(s); }

    Object top() throws IOException {
        if (in.readUnsignedShort() != 0xACED || in.readUnsignedShort() != 5) throw new IOException("not a Java object stream");
        return read();
    }

    Object read() throws IOException { return content(in.readUnsignedByte()); }

    Object content(int tc) throws IOException {
        switch (tc) {
            case 0x70: return null;
            case 0x71: return handles.get(in.readInt() - 0x7E0000);
            case 0x74: { String s = in.readUTF(); handles.add(s); return s; }
            case 0x7C: { long n = in.readLong(); byte[] b = new byte[(int) n]; in.readFully(b); String s = new String(b, "UTF-8"); handles.add(s); return s; }
            case 0x72: case 0x7D: return classDesc(tc);
            case 0x73: return object();
            case 0x75: return array();
            case 0x7E: { JDesc d = desc(); int h = handles.size(); handles.add(null); Object nm = read(); handles.set(h, nm); return nm; }
            case 0x76: { JDesc d = desc(); handles.add(d); return d; }
            case 0x77: { in.skipBytes(in.readUnsignedByte()); return BLOCK; }
            case 0x7A: { in.skipBytes(in.readInt()); return BLOCK; }
            case 0x79: handles.clear(); return read();
            default: throw new IOException("unexpected type code " + tc);
        }
    }

    JDesc desc() throws IOException {
        int tc = in.readUnsignedByte();
        if (tc == 0x70) return null;
        if (tc == 0x71) return (JDesc) handles.get(in.readInt() - 0x7E0000);
        return classDesc(tc);
    }

    JDesc classDesc(int tc) throws IOException {
        JDesc d = new JDesc();
        if (tc == 0x7D) {
            handles.add(d);
            int n = in.readInt();
            for (int i = 0; i < n; i++) in.readUTF();
            annotations();
            d.sup = desc();
            return d;
        }
        d.name = in.readUTF();
        in.readLong();
        handles.add(d);
        d.flags = in.readUnsignedByte();
        int n = in.readShort();
        d.types = new char[n];
        d.fields = new String[n];
        for (int i = 0; i < n; i++) {
            d.types[i] = (char) in.readUnsignedByte();
            d.fields[i] = in.readUTF();
            if (d.types[i] == 'L' || d.types[i] == '[') read();
        }
        annotations();
        d.sup = desc();
        return d;
    }

    void annotations() throws IOException { while (true) { int tc = in.readUnsignedByte(); if (tc == 0x78) return; content(tc); } }

    Object object() throws IOException {
        JDesc d = desc();
        JObj o = new JObj();
        o.cls = d.name;
        handles.add(o);
        List<JDesc> chain = new ArrayList<JDesc>();
        for (JDesc c = d; c != null; c = c.sup) chain.add(0, c);
        for (JDesc c : chain) {
            if ((c.flags & 0x04) != 0) { annotations(); continue; }
            if ((c.flags & 0x02) == 0) continue;
            for (int i = 0; i < c.fields.length; i++) o.f.put(c.fields[i], value(c.types[i]));
            if ((c.flags & 0x01) != 0) while (true) { int tc = in.readUnsignedByte(); if (tc == 0x78) break; Object x = content(tc); if (x != BLOCK) o.extra.add(x); }
        }
        return o;
    }

    Object value(char t) throws IOException {
        switch (t) {
            case 'B': return (int) in.readByte();
            case 'C': return (int) in.readChar();
            case 'D': return in.readDouble();
            case 'F': return (double) in.readFloat();
            case 'I': return in.readInt();
            case 'J': return (double) in.readLong();
            case 'S': return (int) in.readShort();
            case 'Z': return in.readBoolean();
            default: return read();
        }
    }

    Object array() throws IOException {
        JDesc d = desc();
        int n = in.readInt();
        char comp = d.name.charAt(1);
        switch (comp) {
            case 'D': { double[] a = new double[n]; handles.add(a); for (int i = 0; i < n; i++) a[i] = in.readDouble(); return a; }
            case 'I': { int[] a = new int[n]; handles.add(a); for (int i = 0; i < n; i++) a[i] = in.readInt(); return a; }
            case 'Z': { boolean[] a = new boolean[n]; handles.add(a); for (int i = 0; i < n; i++) a[i] = in.readBoolean(); return a; }
            case 'J': { double[] a = new double[n]; handles.add(a); for (int i = 0; i < n; i++) a[i] = in.readLong(); return a; }
            case 'F': { double[] a = new double[n]; handles.add(a); for (int i = 0; i < n; i++) a[i] = in.readFloat(); return a; }
            case 'B': { byte[] a = new byte[n]; handles.add(a); in.readFully(a); return a; }
            case 'S': { int[] a = new int[n]; handles.add(a); for (int i = 0; i < n; i++) a[i] = in.readShort(); return a; }
            case 'C': { int[] a = new int[n]; handles.add(a); for (int i = 0; i < n; i++) a[i] = in.readChar(); return a; }
            default: { Object[] a = new Object[n]; handles.add(a); for (int i = 0; i < n; i++) a[i] = read(); return a; }
        }
    }
}

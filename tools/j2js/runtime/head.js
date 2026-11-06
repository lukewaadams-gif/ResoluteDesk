<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Resolute Desk - A Presidency Simulator</title>
<style>
  html, body { margin: 0; padding: 0; background: #000; overflow: hidden; height: 100%; }
  canvas { display: block; outline: none; touch-action: none; }
  #boot { position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; color: #93a4bf;
          font: 15px "Segoe UI", Helvetica, Arial, sans-serif; background: #050a18; letter-spacing: 0.04em; }
</style>
</head>
<body>
<canvas id="game" tabindex="0"></canvas>
<div id="boot">RESOLUTE DESK &middot; preparing the Oval Office&hellip;</div>
<script>
(function(){
// ===================================================================================
//  Runtime: the parts of Java, AWT and Swing the game uses, implemented for the browser
// ===================================================================================
"use strict";
const HAS_DOM = typeof document !== 'undefined';
const NativePath2D = HAS_DOM ? window.Path2D : null;

// ---- java.util collections as JS arrays -------------------------------------------
(function () {
    const def = (o, n, f) => Object.defineProperty(o, n, { value: f, writable: true, configurable: true, enumerable: false });
    const A = Array.prototype;
    def(A, 'add', function (a, b) { if (arguments.length === 2) { this.splice(a, 0, b); return; } this.push(a); return true; });
    def(A, 'get', function (i) { return this[i]; });
    def(A, 'size', function () { return this.length; });
    def(A, 'isEmpty', function () { return this.length === 0; });
    def(A, 'contains', function (x) { return this.includes(x); });
    def(A, 'clear', function () { this.length = 0; });
    def(A, 'remove', function (x) {
        if (typeof x === 'number') { return this.splice(x, 1)[0]; }
        const i = this.indexOf(x); if (i >= 0) { this.splice(i, 1); return true; } return false;
    });
    def(A, 'addAll', function (c) { for (const x of c) this.push(x); return true; });
    def(A, 'toArray', function () { return this.slice(); });
    const S = String.prototype;
    def(S, 'isEmpty', function () { return this.length === 0; });
    def(S, 'contains', function (x) { return this.includes(x); });
    def(S, 'equals', function (x) { return this.valueOf() === x; });
})();

class HashMap {
    constructor() { this.m = new Map(); }
    get(k) { const v = this.m.get(k); return v === undefined ? null : v; }
    put(k, v) { const o = this.get(k); this.m.set(k, v); return o; }
    containsKey(k) { return this.m.has(k); }
    remove(k) { const o = this.get(k); this.m.delete(k); return o; }
    size() { return this.m.size; }
    isEmpty() { return this.m.size === 0; }
    clear() { this.m.clear(); }
    values() { return Array.from(this.m.values()); }
    keySet() { return Array.from(this.m.keys()); }
}
const LinkedHashMap = HashMap;
class JSet extends Set {
    contains(x) { return this.has(x); }
    includes(x) { return this.has(x); }
}

class StringBuilder {
    constructor(s) { this.s = s === undefined || typeof s === 'number' ? '' : String(s); }
    append(x) { this.s += (x === null ? 'null' : String(x)); return this; }
    get length() { return this.s.length; }
    setLength(n) { this.s = n <= this.s.length ? this.s.substring(0, n) : this.s + '\0'.repeat(n - this.s.length); }
    insert(i, x) { this.s = this.s.substring(0, i) + String(x) + this.s.substring(i); return this; }
    toString() { return this.s; }
}

// ---- java.util.Random (exact 48-bit LCG) ------------------------------------------------
const TWO24 = 16777216, TWO48 = 281474976710656;
class Random {
    constructor(seed) {
        if (seed === undefined) seed = Math.floor(Math.random() * TWO48);
        this.setSeed(seed);
    }
    setSeed(seed) {
        // low 48 bits of the seed, xor 0x5DEECE66D
        let s = seed;
        if (!Number.isFinite(s)) s = 0;
        s = Math.floor(s);
        let lo48 = ((s % TWO48) + TWO48) % TWO48;
        let hi = Math.floor(lo48 / TWO24), lo = lo48 % TWO24;
        // 0x5DEECE66D = hi 0x5DE, lo 0xECE66D
        this.hi = (hi ^ 0x5DE) & 0xFFFFFF;
        this.lo = (lo ^ 0xECE66D) & 0xFFFFFF;
        this.haveG = false; this.nextG = 0;
    }
    next(bits) {
        // seed = seed * 0x5DEECE66D + 0xB  (mod 2^48)
        const mh = 0x5DE, ml = 0xECE66D;
        const lo = this.lo, hi = this.hi;
        let p = lo * ml + 0xB;                          // < 2^48
        let carry = Math.floor(p / TWO24);
        const nlo = p - carry * TWO24;
        let phi = (hi * ml + lo * mh + carry) % TWO24;  // each < 2^48
        this.lo = nlo; this.hi = phi;
        const seed = phi * TWO24 + nlo;
        let r = Math.floor(seed / Math.pow(2, 48 - bits));
        if (bits === 32 && r >= 2147483648) r -= 4294967296;
        return r;
    }
    nextInt(bound) {
        if (bound === undefined) return this.next(32);
        if ((bound & -bound) === bound) return Math.floor(bound * this.next(31) / 2147483648);
        let bits, val;
        do { bits = this.next(31); val = bits % bound; } while (bits - val + (bound - 1) >= 2147483648);
        return val;
    }
    nextDouble() { return (this.next(26) * 134217728 + this.next(27)) / 9007199254740992; }
    nextBoolean() { return this.next(1) !== 0; }
    nextGaussian() {
        if (this.haveG) { this.haveG = false; return this.nextG; }
        let v1, v2, s;
        do { v1 = 2 * this.nextDouble() - 1; v2 = 2 * this.nextDouble() - 1; s = v1 * v1 + v2 * v2; } while (s >= 1 || s === 0);
        const mul = Math.sqrt(-2 * Math.log(s) / s);
        this.nextG = v2 * mul; this.haveG = true;
        return v1 * mul;
    }
}

// ---- small helpers used by the generated code ----------------------------------------
function $arr(n, d) { const a = new Array(n); a.fill(d); return a; }
function $arrN(dims, d) {
    const n = dims[0];
    if (dims.length === 1) return $arr(n, d);
    const a = new Array(n), rest = dims.slice(1);
    for (let i = 0; i < n; i++) a[i] = $arrN(rest, d);
    return a;
}
function $i(x) {
    if (x !== x) return 0;
    if (x === Infinity) return 9223372036854775807;
    if (x === -Infinity) return -9223372036854775808;
    return Math.trunc(x);
}
function $idiv(a, b) { if (b === 0) return 0; return Math.trunc(a / b); }
function $cmp(a, b) { return a < b ? -1 : a > b ? 1 : (a === b ? 0 : (a !== a ? (b !== b ? 0 : 1) : -1)); }
function $natcmp(a, b) { return a < b ? -1 : a > b ? 1 : 0; }
function $eq(a, b) {
    if (a === b) return true;
    if (a === null || a === undefined) return false;
    if (typeof a === 'object' && typeof a.equals === 'function') return a.equals(b);
    return false;
}
function $ds(x) {
    if (x !== x) return 'NaN';
    if (x === Infinity) return 'Infinity';
    if (x === -Infinity) return '-Infinity';
    if (x === 0) return 1 / x < 0 ? '-0.0' : '0.0';
    const ax = Math.abs(x);
    if (ax >= 1e-3 && ax < 1e7) {
        const s = String(x);
        return s.indexOf('.') < 0 && s.indexOf('e') < 0 ? s + '.0' : s;
    }
    let e = x.toExponential();      // 1.5e+7
    let [m, ex] = e.split('e');
    if (m.indexOf('.') < 0) m += '.0';
    return m + 'E' + parseInt(ex, 10);
}
function $split(s, re, limit) {
    if (limit === undefined) limit = 0;
    const rx = new RegExp(re, 'g');
    const out = [];
    let last = 0, m;
    while ((m = rx.exec(s)) !== null) {
        if (m[0].length === 0) { rx.lastIndex++; if (m.index === 0 || m.index >= s.length) continue; }
        if (limit > 0 && out.length === limit - 1) break;
        out.push(s.substring(last, m.index));
        last = m.index + m[0].length;
    }
    out.push(s.substring(last));
    if (limit === 0) while (out.length > 1 && out[out.length - 1] === '') out.pop();
    if (limit === 0 && out.length === 1 && out[0] === '' && s.length > 0) return [];
    return out;
}
function $replace(s, a, b) { return s.split(a).join(b); }
function $replaceAll(s, re, rep) { return s.replace(new RegExp(re, 'g'), rep); }
function $chars(s) { const a = new Array(s.length); for (let i = 0; i < s.length; i++) a[i] = s.charCodeAt(i); return a; }
let $idCounter = 1;
const $idMap = new WeakMap();
function $ihash(o) {
    if (o === null || o === undefined) return 0;
    if (typeof o !== 'object' && typeof o !== 'function') return $hash(o);
    let h = $idMap.get(o);
    if (h === undefined) { h = ($idCounter++ * 2654435761) | 0; $idMap.set(o, h); }
    return h;
}
function $hash(o) {
    if (typeof o === 'string') {
        let h = 0;
        for (let i = 0; i < o.length; i++) h = (Math.imul(31, h) + o.charCodeAt(i)) | 0;
        return h;
    }
    if (typeof o === 'number') return o | 0;
    if (typeof o === 'boolean') return o ? 1231 : 1237;
    return $ihash(o);
}
function $lcChar(c) { return String.fromCharCode(c).toLowerCase().charCodeAt(0); }
function $ucChar(c) { return String.fromCharCode(c).toUpperCase().charCodeAt(0); }
const $t0 = Date.now();
function $nano() { return (typeof performance !== 'undefined' ? performance.now() : Date.now() - $t0) * 1e6 + $t0 * 1e6; }
function $arraycopy(src, sp, dst, dp, n) { for (let i = 0; i < n; i++) dst[dp + i] = src[sp + i]; }
function $shuffle(list, rnd) {
    for (let i = list.length; i > 1; i--) { const j = rnd.nextInt(i); const t = list[i - 1]; list[i - 1] = list[j]; list[j] = t; }
}
function $list(x) { if (typeof x === 'number' || x === undefined) return []; return Array.from(x); }
function $join(sep, ...parts) { if (parts.length === 1 && Array.isArray(parts[0])) return parts[0].join(sep); return parts.join(sep); }

/** java.lang.String.format for the conversions the game uses. */
function $fmt(f, ...args) {
    let ai = 0;
    return f.replace(/%([-+ 0,#]*)(\d+)?(?:\.(\d+))?([dfsxXe%n])/g, (all, flags, width, prec, conv) => {
        if (conv === '%') return '%';
        if (conv === 'n') return '\n';
        let v = args[ai++];
        let s;
        const neg = typeof v === 'number' && (v < 0 || (v === 0 && 1 / v < 0));
        switch (conv) {
            case 'd': s = String(Math.abs(Math.trunc(v))); break;
            case 'f': {
                const p = prec === undefined ? 6 : parseInt(prec, 10);
                if (v !== v) { s = 'NaN'; break; }
                if (!isFinite(v)) { s = 'Infinity'; break; }
                s = Math.abs(v).toFixed(p);
                break;
            }
            case 'e': s = Math.abs(v).toExponential(prec === undefined ? 6 : parseInt(prec, 10)); break;
            case 'x': case 'X': s = (v >>> 0).toString(16); if (conv === 'X') s = s.toUpperCase(); break;
            default: s = v === null ? 'null' : (typeof v === 'number' ? String(v) : String(v));
                if (prec !== undefined) s = s.substring(0, parseInt(prec, 10));
        }
        if (conv === 'd' || conv === 'f' || conv === 'e') {
            if (flags.indexOf(',') >= 0) {
                const parts = s.split('.');
                parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
                s = parts.join('.');
            }
            s = (neg ? '-' : (flags.indexOf('+') >= 0 ? '+' : (flags.indexOf(' ') >= 0 ? ' ' : ''))) + s;
        }
        const w = width ? parseInt(width, 10) : 0;
        if (s.length < w) {
            if (flags.indexOf('-') >= 0) s = s + ' '.repeat(w - s.length);
            else if (flags.indexOf('0') >= 0 && (conv === 'd' || conv === 'f')) {
                const sign = /^[-+ ]/.test(s) ? s[0] : '';
                const rest = sign ? s.substring(1) : s;
                s = sign + '0'.repeat(w - s.length) + rest;
            } else s = ' '.repeat(w - s.length) + s;
        }
        return s;
    });
}

// ---- AWT: colours, fonts, strokes, paints ---------------------------------------------------
class Color {
    constructor(r, g, b, a) {
        this.r = r | 0; this.g = g | 0; this.b = b | 0; this.a = a === undefined ? 255 : (a | 0);
        this._css = null;
    }
    getRed() { return this.r; }
    getGreen() { return this.g; }
    getBlue() { return this.b; }
    getAlpha() { return this.a; }
    equals(o) { return o instanceof Color && o.r === this.r && o.g === this.g && o.b === this.b && o.a === this.a; }
    // brighter() and darker() follow java.awt.Color exactly (a factor of 0.7)
    brighter() {
        let r = this.r, g = this.g, b = this.b; const i = 3;
        if (r === 0 && g === 0 && b === 0) return new Color(i, i, i, this.a);
        if (r > 0 && r < i) r = i; if (g > 0 && g < i) g = i; if (b > 0 && b < i) b = i;
        return new Color(Math.min(Math.trunc(r / 0.7), 255), Math.min(Math.trunc(g / 0.7), 255), Math.min(Math.trunc(b / 0.7), 255), this.a);
    }
    darker() { return new Color(Math.max(Math.trunc(this.r * 0.7), 0), Math.max(Math.trunc(this.g * 0.7), 0), Math.max(Math.trunc(this.b * 0.7), 0), this.a); }
    css() {
        if (this._css === null) this._css = this.a === 255 ? 'rgb(' + this.r + ',' + this.g + ',' + this.b + ')' : 'rgba(' + this.r + ',' + this.g + ',' + this.b + ',' + (this.a / 255).toFixed(4) + ')';
        return this._css;
    }
}
Color.white = new Color(255, 255, 255);
Color.black = new Color(0, 0, 0);
Color.WHITE = Color.white; Color.BLACK = Color.black;

class Font {
    constructor(family, style, size) {
        this.family = family; this.style = style; this.size = size;
        this.css = (style & 2 ? 'italic ' : '') + (style & 1 ? 'bold ' : '') + size + 'px ' + family;
        this._fm = null;
    }
    getSize() { return this.size; }
}
Font.PLAIN = 0; Font.BOLD = 1; Font.ITALIC = 2;

let $measureCtx = null;
function $mctx() {
    if ($measureCtx === null) {
        if (HAS_DOM) $measureCtx = document.createElement('canvas').getContext('2d');
        else $measureCtx = { font: '', measureText: (s) => ({ width: s.length * 7 }) };
    }
    return $measureCtx;
}
class FontMetrics {
    constructor(font) { this.font = font; this.cache = new Map(); }
    stringWidth(s) {
        let w = this.cache.get(s);
        if (w === undefined) {
            const c = $mctx();
            if (c.font !== this.font.css) c.font = this.font.css;
            w = c.measureText(s).width;
            if (this.cache.size > 4000) this.cache.clear();
            this.cache.set(s, w);
        }
        return w;
    }
    charWidth(code) { return this.stringWidth(String.fromCharCode(code)); }
    getAscent() { return Math.round(this.font.size * 0.93); }
    getDescent() { return Math.round(this.font.size * 0.22); }
    getHeight() { return Math.round(this.font.size * 1.17); }
}
function $fm(font) { if (font._fm === null) font._fm = new FontMetrics(font); return font._fm; }

class BasicStroke {
    constructor(w, cap, join) { this.width = w === undefined ? 1 : w; this.cap = cap === undefined ? 2 : cap; this.join = join === undefined ? 0 : join; }
}
BasicStroke.CAP_BUTT = 0; BasicStroke.CAP_ROUND = 1; BasicStroke.CAP_SQUARE = 2;
BasicStroke.JOIN_MITER = 0; BasicStroke.JOIN_ROUND = 1; BasicStroke.JOIN_BEVEL = 2;

class GradientPaint {
    constructor(x1, y1, c1, x2, y2, c2) { this.x1 = x1; this.y1 = y1; this.c1 = c1; this.x2 = x2; this.y2 = y2; this.c2 = c2; this._g = null; this._ctx = null; }
    style(ctx) {
        if (this._ctx !== ctx) {
            const g = ctx.createLinearGradient(this.x1, this.y1, this.x2, this.y2);
            g.addColorStop(0, this.c1.css()); g.addColorStop(1, this.c2.css());
            this._g = g; this._ctx = ctx;
        }
        return this._g;
    }
}
class LinearGradientPaint {
    constructor(x1, y1, x2, y2, fractions, colors) { this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.f = fractions; this.c = colors; this._g = null; this._ctx = null; }
    style(ctx) {
        if (this._ctx !== ctx) {
            const g = ctx.createLinearGradient(this.x1, this.y1, this.x2, this.y2);
            for (let i = 0; i < this.f.length; i++) g.addColorStop(Math.min(1, Math.max(0, this.f[i])), this.c[i].css());
            this._g = g; this._ctx = ctx;
        }
        return this._g;
    }
}
class RadialGradientPaint {
    constructor(center, radius, fractions, colors) { this.cx = center.x; this.cy = center.y; this.r = radius; this.f = fractions; this.c = colors; this._g = null; this._ctx = null; }
    style(ctx) {
        if (this._ctx !== ctx) {
            const g = ctx.createRadialGradient(this.cx, this.cy, 0, this.cx, this.cy, Math.max(0.0001, this.r));
            for (let i = 0; i < this.f.length; i++) g.addColorStop(Math.min(1, Math.max(0, this.f[i])), this.c[i].css());
            this._g = g; this._ctx = ctx;
        }
        return this._g;
    }
}

class AlphaCompositeT { constructor(rule, alpha) { this.rule = rule; this.alpha = alpha; } }
const AlphaComposite = {
    CLEAR: 1, SRC: 2, SRC_OVER: 3, DST_IN: 6,
    getInstance(rule, alpha) { return new AlphaCompositeT(rule, alpha === undefined ? 1 : Math.max(0, Math.min(1, alpha))); },
};
AlphaComposite.SrcOver = new AlphaCompositeT(3, 1);
AlphaComposite.Clear = new AlphaCompositeT(1, 1);
AlphaComposite.DstIn = new AlphaCompositeT(6, 1);

const RenderingHints = new Proxy({}, { get: (t, k) => String(k) });

// ---- geometry ---------------------------------------------------------------------------------------
class Rect2D {
    constructor(x, y, w, h) { this.x = x; this.y = y; this.width = w; this.height = h; }
    getX() { return this.x; } getY() { return this.y; } getWidth() { return this.width; } getHeight() { return this.height; }
    getMinX() { return this.x; } getMinY() { return this.y; } getMaxX() { return this.x + this.width; } getMaxY() { return this.y + this.height; }
    getCenterX() { return this.x + this.width / 2; } getCenterY() { return this.y + this.height / 2; }
    contains(px, py) { return px >= this.x && py >= this.y && px < this.x + this.width && py < this.y + this.height; }
    getBounds2D() { return this; }
    trace(c) { c.moveTo(this.x, this.y); c.lineTo(this.x + this.width, this.y); c.lineTo(this.x + this.width, this.y + this.height); c.lineTo(this.x, this.y + this.height); c.closePath(); }
}
const Rectangle2D = { Double: Rect2D, Float: Rect2D };

class RoundRect2D {
    constructor(x, y, w, h, aw, ah) { this.x = x; this.y = y; this.width = w; this.height = h; this.aw = aw; this.ah = ah; }
    getX() { return this.x; } getY() { return this.y; } getWidth() { return this.width; } getHeight() { return this.height; }
    contains(px, py) { return px >= this.x && py >= this.y && px < this.x + this.width && py < this.y + this.height; }
    trace(c) {
        const x = this.x, y = this.y, w = this.width, h = this.height;
        if (w <= 0 || h <= 0) return;
        const rx = Math.min(Math.abs(this.aw) / 2, w / 2), ry = Math.min(Math.abs(this.ah) / 2, h / 2);
        if (rx <= 0 || ry <= 0) { Rect2D.prototype.trace.call(this, c); return; }
        const k = 0.5522847498;
        c.moveTo(x + rx, y);
        c.lineTo(x + w - rx, y);
        c.bezierCurveTo(x + w - rx + rx * k, y, x + w, y + ry - ry * k, x + w, y + ry);
        c.lineTo(x + w, y + h - ry);
        c.bezierCurveTo(x + w, y + h - ry + ry * k, x + w - rx + rx * k, y + h, x + w - rx, y + h);
        c.lineTo(x + rx, y + h);
        c.bezierCurveTo(x + rx - rx * k, y + h, x, y + h - ry + ry * k, x, y + h - ry);
        c.lineTo(x, y + ry);
        c.bezierCurveTo(x, y + ry - ry * k, x + rx - rx * k, y, x + rx, y);
        c.closePath();
    }
}
const RoundRectangle2D = { Double: RoundRect2D, Float: RoundRect2D };

class Ellipse2DD {
    constructor(x, y, w, h) { this.x = x; this.y = y; this.width = w; this.height = h; }
    getX() { return this.x; } getY() { return this.y; } getWidth() { return this.width; } getHeight() { return this.height; }
    contains(px, py) {
        const rx = this.width / 2, ry = this.height / 2;
        if (rx <= 0 || ry <= 0) return false;
        const dx = (px - this.x - rx) / rx, dy = (py - this.y - ry) / ry;
        return dx * dx + dy * dy < 1;
    }
    trace(c) {
        const rx = this.width / 2, ry = this.height / 2;
        if (rx <= 0 || ry <= 0) return;
        c.moveTo(this.x + this.width, this.y + ry);
        c.ellipse(this.x + rx, this.y + ry, rx, ry, 0, 0, Math.PI * 2, false);
        c.closePath();
    }
}
const Ellipse2D = { Double: Ellipse2DD, Float: Ellipse2DD };

class Line2DD {
    constructor(x1, y1, x2, y2) { this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; }
    trace(c) { c.moveTo(this.x1, this.y1); c.lineTo(this.x2, this.y2); }
    isLine() { return true; }
}
const Line2D = { Double: Line2DD, Float: Line2DD };

class Arc2DD {
    constructor(x, y, w, h, start, extent, type) { this.x = x; this.y = y; this.width = w; this.height = h; this.start = start; this.extent = extent; this.type = type; }
    trace(c) {
        const rx = this.width / 2, ry = this.height / 2;
        if (rx <= 0 || ry <= 0) return;
        const cx = this.x + rx, cy = this.y + ry;
        const a0 = -this.start * Math.PI / 180, a1 = -(this.start + this.extent) * Math.PI / 180;
        c.moveTo(cx + rx * Math.cos(a0), cy + ry * Math.sin(a0));
        c.ellipse(cx, cy, rx, ry, 0, a0, a1, this.extent > 0);
        if (this.type === 2) { c.lineTo(cx, cy); c.closePath(); }
        else if (this.type === 1) c.closePath();
    }
}
const Arc2D = { Double: Arc2DD, Float: Arc2DD, OPEN: 0, CHORD: 1, PIE: 2 };

class Point2DD { constructor(x, y) { this.x = x; this.y = y; } getX() { return this.x; } getY() { return this.y; } }
const Point2D = { Double: Point2DD, Float: Point2DD };

/** Path2D.Double: recorded commands, traced onto a canvas context or cached as a native Path2D. */
class JPath {
    constructor(rule) { this.rule = rule === undefined ? 1 : rule; this.cmd = []; this._native = null; this._bounds = null; }
    moveTo(x, y) { this.cmd.push(0, x, y); this._native = null; this._bounds = null; }
    lineTo(x, y) { this.cmd.push(1, x, y); this._native = null; this._bounds = null; }
    quadTo(a, b, x, y) { this.cmd.push(2, a, b, x, y); this._native = null; this._bounds = null; }
    curveTo(a, b, c, d, x, y) { this.cmd.push(3, a, b, c, d, x, y); this._native = null; this._bounds = null; }
    closePath() { this.cmd.push(4); this._native = null; }
    trace(c) {
        const m = this.cmd;
        for (let i = 0; i < m.length;) {
            switch (m[i]) {
                case 0: c.moveTo(m[i + 1], m[i + 2]); i += 3; break;
                case 1: c.lineTo(m[i + 1], m[i + 2]); i += 3; break;
                case 2: c.quadraticCurveTo(m[i + 1], m[i + 2], m[i + 3], m[i + 4]); i += 5; break;
                case 3: c.bezierCurveTo(m[i + 1], m[i + 2], m[i + 3], m[i + 4], m[i + 5], m[i + 6]); i += 7; break;
                default: c.closePath(); i += 1;
            }
        }
    }
    /** Subpaths traced with a consistent (clockwise) orientation, so that a non-zero fill gives their union. */
    traceUnion(c) {
        const m = this.cmd;
        const subs = [];
        let cur = null;
        for (let i = 0; i < m.length;) {
            const op = m[i];
            const n = op === 0 || op === 1 ? 3 : op === 2 ? 5 : op === 3 ? 7 : 1;
            if (op === 0) { cur = []; subs.push(cur); }
            if (cur === null) { cur = []; subs.push(cur); }
            cur.push(m.slice(i, i + n));
            i += n;
        }
        for (const s of subs) {
            // signed area of the control polygon
            const pts = [];
            for (const seg of s) for (let k = 1; k + 1 < seg.length; k += 2) pts.push(seg[k], seg[k + 1]);
            let area = 0;
            for (let k = 0; k < pts.length; k += 2) {
                const k2 = (k + 2) % pts.length;
                area += pts[k] * pts[k2 + 1] - pts[k2] * pts[k + 1];
            }
            if (area >= 0) {
                for (const seg of s) traceSeg(c, seg);
            } else {
                // reverse the subpath
                const ends = [];
                let px = 0, py = 0;
                for (const seg of s) {
                    if (seg[0] === 4) continue;
                    const x = seg[seg.length - 2], y = seg[seg.length - 1];
                    ends.push({ seg, sx: px, sy: py, ex: x, ey: y });
                    px = x; py = y;
                }
                if (ends.length === 0) continue;
                const last = ends[ends.length - 1];
                c.moveTo(last.ex, last.ey);
                for (let j = ends.length - 1; j >= 1; j--) {
                    const e = ends[j], sg = e.seg;
                    if (sg[0] === 1) c.lineTo(e.sx, e.sy);
                    else if (sg[0] === 2) c.quadraticCurveTo(sg[1], sg[2], e.sx, e.sy);
                    else if (sg[0] === 3) c.bezierCurveTo(sg[3], sg[4], sg[1], sg[2], e.sx, e.sy);
                }
                c.closePath();
            }
        }
    }
    native() {
        if (this._native === null) { const p = new NativePath2D(); this.trace(p); this._native = p; }
        return this._native;
    }
    getBounds2D() {
        if (this._bounds === null) {
            let x0 = Infinity, y0 = Infinity, x1 = -Infinity, y1 = -Infinity;
            const m = this.cmd;
            for (let i = 0; i < m.length;) {
                const op = m[i];
                const n = op === 0 || op === 1 ? 3 : op === 2 ? 5 : op === 3 ? 7 : 1;
                for (let k = i + 1; k + 1 < i + n; k += 2) {
                    x0 = Math.min(x0, m[k]); x1 = Math.max(x1, m[k]); y0 = Math.min(y0, m[k + 1]); y1 = Math.max(y1, m[k + 1]);
                }
                i += n;
            }
            this._bounds = new Rect2D(x0, y0, x1 - x0, y1 - y0);
        }
        return this._bounds;
    }
    contains(x, y) {
        if (!HAS_DOM) return false;
        return $mctx().isPointInPath(this.native(), x, y, this.rule === 0 ? 'evenodd' : 'nonzero');
    }
}
function traceSeg(c, s) {
    switch (s[0]) {
        case 0: c.moveTo(s[1], s[2]); break;
        case 1: c.lineTo(s[1], s[2]); break;
        case 2: c.quadraticCurveTo(s[1], s[2], s[3], s[4]); break;
        case 3: c.bezierCurveTo(s[1], s[2], s[3], s[4], s[5], s[6]); break;
        default: c.closePath();
    }
}
const Path2D = { Double: JPath, Float: JPath, WIND_EVEN_ODD: 0, WIND_NON_ZERO: 1 };

/** java.awt.geom.Area: a union of shapes, optionally intersected with clip regions. */
class Area {
    constructor(s) {
        this.parts = [];
        this.inter = [];        // lists of clip entries
        if (s === undefined || s === null) return;
        if (s instanceof Area) { this.parts = s.parts.slice(); this.inter = s.inter.slice(); }
        else if (s instanceof ClipState) { this.inter.push(s.list); this.fromClip = true; }
        else this.parts.push(s);
    }
    add(a) { for (const p of a.parts) this.parts.push(p); }
    intersect(a) {
        if (a.parts.length === 0 && a.inter.length > 0) { for (const l of a.inter) this.inter.push(l); }
        else if (a.parts.length > 0) this.inter.push([{ shapes: a.parts, t: null, union: true }]);
    }
    trace(c) { for (const p of this.parts) { if (p instanceof JPath) p.traceUnion(c); else p.trace(c); } }
}

class ClipState { constructor(list) { this.list = list; } }

class AffineTransform {
    constructor(m) { this.m = m ? m.slice() : [1, 0, 0, 1, 0, 0]; }
    clone() { return new AffineTransform(this.m); }
    getScaleX() { return this.m[0]; } getShearY() { return this.m[1]; } getShearX() { return this.m[2]; } getScaleY() { return this.m[3]; }
    getTranslateX() { return this.m[4]; } getTranslateY() { return this.m[5]; }
}

// ---- the Graphics2D shim -----------------------------------------------------------------------------
class Graphics2D {
    constructor(ctx, base) {
        this.ctx = ctx;
        this.t = [base, 0, 0, base, 0, 0];
        this.clips = [];
        this.clipDirty = true;
        this.paint = Color.black;
        this.font = new Font('sans-serif', 0, 12);
        this.stroke = new BasicStroke(1);
        this.comp = AlphaComposite.SrcOver;
        ctx.save();
        this.ap = {};
    }
    // --- state
    setColor(c) { this.paint = c; }
    getColor() { return this.paint instanceof Color ? this.paint : Color.black; }
    setPaint(p) { this.paint = p; }
    getPaint() { return this.paint; }
    setFont(f) { this.font = f; }
    getFont() { return this.font; }
    getFontMetrics(f) { return $fm(f || this.font); }
    setStroke(s) { this.stroke = s; }
    getStroke() { return this.stroke; }
    setComposite(c) { this.comp = c; }
    getComposite() { return this.comp; }
    setRenderingHint() { }
    getRenderingHint() { return null; }
    setRenderingHints() { }
    getTransform() { return new AffineTransform(this.t); }
    setTransform(at) { this.t = at.m.slice(); }
    translate(x, y) { const t = this.t; t[4] += t[0] * x + t[2] * y; t[5] += t[1] * x + t[3] * y; }
    scale(sx, sy) { const t = this.t; t[0] *= sx; t[1] *= sx; t[2] *= sy; t[3] *= sy; }
    rotate(th, x, y) {
        if (x !== undefined) { this.translate(x, y); this.rotate(th); this.translate(-x, -y); return; }
        const c = Math.cos(th), s = Math.sin(th), t = this.t;
        const a = t[0], b = t[1], cc = t[2], d = t[3];
        t[0] = a * c + cc * s; t[1] = b * c + d * s;
        t[2] = -a * s + cc * c; t[3] = -b * s + d * c;
    }
    // --- clipping
    getClip() { return this.clips.length === 0 ? null : new ClipState(this.clips.slice()); }
    setClip(a, b, c, d) {
        if (a === null || a === undefined) { this.clips = []; }
        else if (typeof a === 'number') { this.clips = [{ shapes: [new Rect2D(a, b, c, d)], t: this.t.slice(), union: false }]; }
        else if (a instanceof ClipState) { this.clips = a.list.slice(); }
        else { this.clips = []; this.clip(a); return; }
        this.clipDirty = true;
    }
    clip(s) {
        if (s instanceof Area) {
            if (s.parts.length > 0) this.clips.push({ shapes: s.parts, t: this.t.slice(), union: true });
            for (const l of s.inter) for (const e of l) this.clips.push(e.t === null ? { shapes: e.shapes, t: this.t.slice(), union: e.union } : e);
        } else if (s instanceof ClipState) {
            for (const e of s.list) this.clips.push(e);
        } else {
            this.clips.push({ shapes: [s], t: this.t.slice(), union: false });
        }
        this.clipDirty = true;
    }
    _prep() {
        const ctx = this.ctx;
        if (this.clipDirty) {
            ctx.restore();
            ctx.save();
            for (const e of this.clips) {
                const t = e.t;
                ctx.setTransform(t[0], t[1], t[2], t[3], t[4], t[5]);
                ctx.beginPath();
                for (const s of e.shapes) {
                    if (e.union && s instanceof JPath) s.traceUnion(ctx); else s.trace(ctx);
                }
                ctx.clip(e.union ? 'nonzero' : (e.shapes[0] instanceof JPath && e.shapes[0].rule === 0 ? 'evenodd' : 'nonzero'));
            }
            this.clipDirty = false;
            this.ap = {};
        }
        const t = this.t;
        ctx.setTransform(t[0], t[1], t[2], t[3], t[4], t[5]);
        const comp = this.comp;
        if (this.ap.comp !== comp) {
            ctx.globalAlpha = comp.alpha;
            ctx.globalCompositeOperation = comp.rule === 6 ? 'destination-in' : comp.rule === 1 ? 'destination-out' : 'source-over';
            this.ap.comp = comp;
        }
    }
    _style(p) { return p instanceof Color ? p.css() : p.style(this.ctx); }
    _fill() {
        const s = this._style(this.paint);
        if (this.ap.fill !== s) { this.ctx.fillStyle = s; this.ap.fill = s; }
    }
    _strokeStyle() {
        const ctx = this.ctx, st = this.stroke;
        const s = this._style(this.paint);
        if (this.ap.stroke !== s) { ctx.strokeStyle = s; this.ap.stroke = s; }
        if (this.ap.sw !== st) {
            ctx.lineWidth = st.width <= 0 ? 0.0001 : st.width;
            ctx.lineCap = st.cap === 1 ? 'round' : st.cap === 2 ? 'square' : 'butt';
            ctx.lineJoin = st.join === 1 ? 'round' : st.join === 2 ? 'bevel' : 'miter';
            this.ap.sw = st;
        }
    }
    // --- drawing
    fill(s) {
        if (s.isLine) return;
        this._prep();
        this._fill();
        const ctx = this.ctx;
        if (s instanceof JPath) { ctx.fill(s.native(), s.rule === 0 ? 'evenodd' : 'nonzero'); return; }
        if (s instanceof Area) { for (const p of s.parts) { ctx.beginPath(); p.trace(ctx); ctx.fill(p instanceof JPath && p.rule === 0 ? 'evenodd' : 'nonzero'); } return; }
        ctx.beginPath();
        s.trace(ctx);
        ctx.fill();
    }
    draw(s) {
        this._prep();
        this._strokeStyle();
        const ctx = this.ctx;
        if (s instanceof JPath) { ctx.stroke(s.native()); return; }
        ctx.beginPath();
        s.trace(ctx);
        ctx.stroke();
    }
    fillRect(x, y, w, h) {
        this._prep();
        if (this.comp.rule === 1) { this.ctx.clearRect(x, y, w, h); return; }
        this._fill();
        this.ctx.fillRect(x, y, w, h);
    }
    drawRect(x, y, w, h) { this.draw(new Rect2D(x, y, w, h)); }
    drawLine(x1, y1, x2, y2) { this.draw(new Line2DD(x1, y1, x2, y2)); }
    drawString(s, x, y) {
        this._prep();
        this._fill();
        const ctx = this.ctx;
        if (this.ap.font !== this.font) { ctx.font = this.font.css; this.ap.font = this.font; }
        ctx.fillText(s, x, y);
    }
    drawImage(img, x, y, w, h) {
        if (!img) return true;
        this._prep();
        const c = img.canvas;
        if (typeof w === 'number' && typeof h === 'number') this.ctx.drawImage(c, x, y, w, h);
        else this.ctx.drawImage(c, x, y, img.w, img.h);
        return true;
    }
    dispose() { this.ctx.restore(); this.ctx.setTransform(1, 0, 0, 1, 0, 0); }
}

class BufferedImage {
    constructor(w, h, type) {
        this.w = w; this.h = h; this.type = type;
        if (HAS_DOM) {
            this.canvas = document.createElement('canvas');
            this.canvas.width = Math.max(1, w); this.canvas.height = Math.max(1, h);
        } else this.canvas = null;
    }
    getWidth() { return this.w; }
    getHeight() { return this.h; }
    createGraphics() {
        if (!HAS_DOM) return new NullGraphics();
        const ctx = this.canvas.getContext('2d');
        return new Graphics2D(ctx, 1);
    }
}
BufferedImage.TYPE_INT_RGB = 1; BufferedImage.TYPE_INT_ARGB = 2;

/** Headless stand-in (used when running the simulation outside a browser). */
class NullGraphics {
    constructor() { return new Proxy(this, { get: (t, k) => k in t ? t[k] : () => null }); }
    getFontMetrics(f) { return $fm(f || new Font('sans-serif', 0, 12)); }
    getTransform() { return new AffineTransform(); }
    getClip() { return null; }
    getComposite() { return AlphaComposite.SrcOver; }
}

// ---- Swing stand-ins ----------------------------------------------------------------------------------
class Dimension { constructor(w, h) { this.width = w; this.height = h; } }
class Timer { constructor() { } start() { } stop() { } }
const KeyEvent = {
    VK_ENTER: 10, VK_BACK_SPACE: 8, VK_TAB: 9, VK_ESCAPE: 27, VK_SPACE: 32, VK_F1: 112,
    VK_LEFT: 37, VK_UP: 38, VK_RIGHT: 39, VK_DOWN: 40,
    VK_0: 48, VK_1: 49, VK_2: 50, VK_3: 51, VK_4: 52, VK_5: 53, VK_6: 54, VK_7: 55, VK_8: 56, VK_9: 57,
};
class JPanelBase {
    getWidth() { return $host.cssW; }
    getHeight() { return $host.cssH; }
    repaint() { }
    requestFocusInWindow() { if ($host.canvas) $host.canvas.focus(); }
    setPreferredSize() { } setBackground() { } setFocusable() { } setFocusTraversalKeysEnabled() { }
    addMouseListener() { } addMouseMotionListener() { } addMouseWheelListener() { } addKeyListener() { }
}
const $host = { cssW: 1600, cssH: 900, canvas: null };

/** Wraps a DOM mouse event so the game code can read it the Java way. */
class MouseEventW {
    constructor(e, canvas, wheel) {
        const r = canvas.getBoundingClientRect();
        this.x = e.clientX - r.left; this.y = e.clientY - r.top;
        this.button = e.button;
        this.wheel = wheel || 0;
    }
    getX() { return this.x; }
    getY() { return this.y; }
    isRight() { return this.button === 2; }
    getPreciseWheelRotation() { return this.wheel; }
}
class KeyEventW {
    constructor(code, ch) { this.code = code; this.ch = ch; }
    getKeyCode() { return this.code; }
    getKeyChar() { return this.ch; }
}
const SwingUtilities = { isRightMouseButton: (e) => e.isRight(), invokeLater: (f) => f() };
const Double = { NaN: NaN, MAX_VALUE: Number.MAX_VALUE, POSITIVE_INFINITY: Infinity, NEGATIVE_INFINITY: -Infinity };
const Integer = { MAX_VALUE: 2147483647, MIN_VALUE: -2147483648 };

// ---- more helpers used by the generated code (list and character operations) -------------------
function $lrem(l, x) { const i = l.indexOf(x); if (i >= 0) { l.splice(i, 1); return true; } return false; }
function $lset(l, i, v) { const o = l[i]; l[i] = v; return o; }
function $copyOf(a, n) { const r = a.slice(0, n); const d = a.length > 0 && typeof a[0] === 'number' ? 0 : (a.length > 0 && typeof a[0] === 'boolean' ? false : null); while (r.length < n) r.push(d); return r; }
function $arrEq(a, b) { if (a === b) return true; if (a === null || b === null || a.length !== b.length) return false; for (let i = 0; i < a.length; i++) if (a[i] !== b[i]) return false; return true; }
function $swap(l, i, j) { const t = l[i]; l[i] = l[j]; l[j] = t; }
function $removeIf(l, f) { let w = 0, any = false; for (let i = 0; i < l.length; i++) { if (f(l[i])) any = true; else l[w++] = l[i]; } l.length = w; return any; }
function $mget(m, k, d) { return m.containsKey(k) ? m.get(k) : d; }
function $isDigit(c) { return c >= 48 && c <= 57; }
function $isLetter(c) { return /\p{L}/u.test(String.fromCharCode(c)); }
function $isUpper(c) { const s = String.fromCharCode(c); return s !== s.toLowerCase(); }
function $isLower(c) { const s = String.fromCharCode(c); return s !== s.toUpperCase(); }
function $isSpace(c) { return c === 32 || c === 9 || c === 10 || c === 13 || c === 12; }

// ---- classes

// ===================================================================================
//  Browser replacements for the parts of the Java program that touched the OS:
//  calendar, fonts, sound, files, threads and the window.
// ===================================================================================
function dateOf(day) {
    const d = new Date(Date.UTC(CAL_Y, CAL_M, CAL_D + day));
    return [d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate()];
}
function dowOf(day) {
    const d = new Date(Date.UTC(CAL_Y, CAL_M, CAL_D + day));
    return (d.getUTCDay() + 6) % 7;
}
function pickFont(want, fallback) {
    const generic = { SansSerif: 'sans-serif', Serif: 'serif', Monospaced: 'monospace' }[fallback] || 'sans-serif';
    return want.map(w => '"' + w + '"').join(', ') + ', ' + generic;
}

// ---- sound: the same synthesised effects, played through Web Audio ------------------------------
const $audio = {
    ac: null, comp: null, bufs: [], active: 0,
    ctx() {
        if (this.ac === null) {
            const AC = typeof window !== 'undefined' ? (window.AudioContext || window.webkitAudioContext) : null;
            if (!AC) return null;
            try {
                this.ac = new AC();
                this.comp = this.ac.createDynamicsCompressor();
                this.comp.threshold.value = -10; this.comp.knee.value = 12; this.comp.ratio.value = 6;
                this.master = this.ac.createGain();
                this.comp.connect(this.master);
                this.master.connect(this.ac.destination);
            } catch (e) { this.ac = null; return null; }
        }
        return this.ac;
    },
    unlock() { const ac = this.ctx(); if (ac && ac.state !== 'running') ac.resume(); },
};
Sfx.prototype.start = function () { this.line = {}; this.running = true; };
Sfx.prototype.stop = function () { this.running = false; };
Sfx.prototype.run = function () { };
Sfx.prototype.play = function (id, vol, pitch) {
    if (!this.enabled || this.line === null || id < 0 || id >= Sfx.COUNT || this.lib[id] === null) return;
    const ac = $audio.ctx();
    if (!ac || ac.state !== 'running') return;
    if ($audio.active > 16) return;
    let buf = $audio.bufs[id];
    if (!buf) {
        const d = this.lib[id];
        buf = ac.createBuffer(1, d.length, Sfx.RATE);
        const ch = buf.getChannelData(0);
        for (let i = 0; i < d.length; i++) ch[i] = d[i];
        $audio.bufs[id] = buf;
    }
    const src = ac.createBufferSource();
    src.buffer = buf;
    src.playbackRate.value = pitch;
    const gn = ac.createGain();
    gn.gain.value = vol * this.volume * 0.92;
    src.connect(gn);
    gn.connect($audio.comp);
    $audio.active++;
    src.onended = () => { $audio.active--; };
    src.start();
};

// ---- saving: the game object graph as JSON, kept in the browser's IndexedDB ----------------------
const $REG = Object.assign({ Random, HashMap, JSet, Color, StringBuilder }, $CLASSES);
function $ser(root) {
    const ids = new Map();
    function enc(v) {
        if (v === null || v === undefined) return null;
        const ty = typeof v;
        if (ty === 'number') return Number.isFinite(v) ? v : { $n: String(v) };
        if (ty === 'string' || ty === 'boolean') return v;
        if (ty === 'function') return null;
        if (ids.has(v)) return { $r: ids.get(v) };
        const id = ids.size;
        ids.set(v, id);
        if (Array.isArray(v)) return { $a: id, v: v.map(enc) };
        if (v instanceof Set) return { $s: id, v: Array.from(v).map(enc) };
        if (v instanceof Map) return { $m: id, v: Array.from(v.entries()).map(e => [enc(e[0]), enc(e[1])]) };
        const o = { $c: v.constructor.name, $id: id, f: {} };
        for (const k of Object.keys(v)) o.f[k] = enc(v[k]);
        return o;
    }
    return JSON.stringify(enc(root));
}
function $deser(text) {
    const objs = [];
    function dec(v) {
        if (v === null || typeof v !== 'object') return v;
        if ('$n' in v) return Number(v.$n);
        if ('$r' in v) return objs[v.$r];
        if ('$a' in v) { const a = []; objs[v.$a] = a; for (const x of v.v) a.push(dec(x)); return a; }
        if ('$s' in v) { const s = new JSet(); objs[v.$s] = s; for (const x of v.v) s.add(dec(x)); return s; }
        if ('$m' in v) { const m = new Map(); objs[v.$m] = m; for (const e of v.v) m.set(dec(e[0]), dec(e[1])); return m; }
        const C = $REG[v.$c];
        const o = C ? Object.create(C.prototype) : {};
        objs[v.$id] = o;
        for (const k of Object.keys(v.f)) o[k] = dec(v.f[k]);
        const D = $JDEF[v.$c];
        if (D) for (const k in D) if (!(k in o)) o[k] = D[k];
        return o;
    }
    return dec(JSON.parse(text));
}
const $store = {
    mem: {}, db: null, failed: false,
    open(cb) {
        if (this.db || this.failed || typeof indexedDB === 'undefined') { cb(this.db); return; }
        try {
            const r = indexedDB.open('resolutedesk', 1);
            r.onupgradeneeded = () => r.result.createObjectStore('saves');
            r.onsuccess = () => { this.db = r.result; cb(this.db); };
            r.onerror = () => { this.failed = true; cb(null); };
        } catch (e) { this.failed = true; cb(null); }
    },
    put(k, v) {
        this.mem[k] = v;
        this.open(db => {
            if (!db) return;
            try { db.transaction('saves', 'readwrite').objectStore('saves').put(v, k); } catch (e) { }
        });
    },
    get(k, cb) {
        if (k in this.mem) { cb(this.mem[k]); return; }
        this.open(db => {
            if (!db) { cb(null); return; }
            try {
                const r = db.transaction('saves', 'readonly').objectStore('saves').get(k);
                r.onsuccess = () => cb(r.result === undefined ? null : r.result);
                r.onerror = () => cb(null);
            } catch (e) { cb(null); }
        });
    },
    meta: {},
    getMeta(k) { try { const v = localStorage.getItem('rd:' + k); if (v !== null) return v; } catch (e) { } return this.meta[k] === undefined ? null : this.meta[k]; },
    setMeta(k, v) { this.meta[k] = v; try { localStorage.setItem('rd:' + k, v); } catch (e) { } },
};
SaveGame.exists = function (name) { return $store.getMeta(name) !== null; };
SaveGame.describe = function (name) { const v = $store.getMeta(name); return v === null ? '' : v; };
SaveGame.save = function (u, name) {
    const g = u.game;
    if (g === null) return;
    try {
        const data = $ser(g);
        $store.put(name, data);
        $store.setMeta(name, g.pres.last + ' (' + PARTY_SHORT[g.party] + '), ' + monthYear(g.day()));
        if (name === SaveGame.SLOT) u.toast('Game saved.', C_GOOD);
    } catch (t) {
        if (name === SaveGame.SLOT) u.toast('Could not save: ' + t.message, C_BAD);
    }
};
SaveGame.load = function (u, name) {
    $store.get(name, (data) => {
        let g = null;
        try { if (data !== null) g = $deser(data); } catch (t) { g = null; }
        if (g === null) {
            u.info('Could not load', 'The saved game could not be read. It may have been cleared by the browser.');
            return;
        }
        SaveGame.resume(u, g);
    });
};

// ---- the Hall of Fame lives in local storage ------------------------------------------------------
HallStore.read = function () { const v = $store.getMeta('hall'); return v === null ? '' : v; };
HallStore.write = function (t) { $store.setMeta('hall', t); };

// ---- Battleground 2028: its saves are JSON in this browser's IndexedDB (same site) -----------------
function $bgDecode(root) {
    const objs = [];
    function dec(v) {
        if (v === null || typeof v !== 'object') return v;
        if ('$n' in v) return Number(v.$n);
        if ('$r' in v) return objs[v.$r];
        if ('$a' in v) { const a = []; objs[v.$a] = a; for (const x of v.v) a.push(dec(x)); return a; }
        if ('$s' in v) { const a = []; objs[v.$s] = a; for (const x of v.v) a.push(dec(x)); return a; }
        if ('$m' in v) { const m = new Map(); objs[v.$m] = m; for (const e of v.v) m.set(dec(e[0]), dec(e[1])); return m; }
        const o = { $c: v.$c };
        objs[v.$id] = o;
        for (const k of Object.keys(v.f || {})) o[k] = dec(v.f[k]);
        return o;
    }
    return dec(root);
}
function $bgCand(o) {
    if (!o || typeof o !== 'object') return null;
    const c = new Cand();
    c.id = o.id || 'bg'; c.first = o.first || ''; c.last = o.last || ''; c.party = typeof o.party === 'number' ? o.party : DEM;
    c.role = o.role || 'President-elect'; c.homeAb = o.homeAb || null; c.home = c.homeAb ? stateIdx(c.homeAb) : -1;
    c.born = typeof o.born === 'number' ? o.born : 1965; c.bio = o.bio || null; c.pros = o.pros || null; c.cons = o.cons || null;
    c.faction = clampi(typeof o.faction === 'number' ? o.faction : (c.party === DEM ? F_LIB : F_GOV), 0, NF - 1);
    c.econ = typeof o.econ === 'number' ? o.econ : FACTIONS[c.faction].econ; c.cult = typeof o.cult === 'number' ? o.cult : FACTIONS[c.faction].cult;
    c.plat = FACTIONS[c.faction].pos.slice();
    if (Array.isArray(o.a)) for (let i = 0; i < NA && i < o.a.length; i++) c.a[i] = o.a[i] | 0;
    const lk = o.look;
    let l;
    if (lk && typeof lk === 'object') {
        l = new Look();
        for (const k of ['female', 'skin', 'hair', 'style', 'gray', 'facial', 'glasses', 'faceW', 'jaw', 'suit', 'tie', 'age', 'smile', 'earrings', 'lipstick']) if (k in lk) l[k] = lk[k];
    } else l = Creator.randomLook(new Random(c.last.length * 7919));
    c.look = l; c.custom = true; c.fictional = !!o.fictional;
    return c;
}
function $bgPres(g, slot) {
    const camps = Array.isArray(g.camps) ? g.camps : [];
    if (camps.length === 0) return null;
    const player = typeof g.player === 'number' ? g.player : -1;
    const result = g.result || null;
    const evD = g.evD | 0, evR = g.evR | 0, evI = g.evI | 0;
    let win = -1;
    if (result) win = evD >= 270 ? DEM : evR >= 270 ? REP : evI >= 270 ? IND : (typeof g.contingent === 'number' ? g.contingent : -1);
    const p = new BgPres();
    p.slot = slot;
    let camp = null;
    if (win === DEM || win === REP) {
        const idx = win === DEM ? g.dNom : g.rNom;
        for (const c of camps) if (c && c.idx === idx) camp = c;
        if (!camp) for (const c of camps) if (c && c.party === win && c.nominee) camp = c;
    } else if (win < 0 && player >= 0 && player < camps.length) camp = camps[player];
    if (win === IND || win >= 2) { p.independent = true; return p; }
    if (!camp) return null;
    const c = $bgCand(camp.c);
    if (!c) return null;
    const party = typeof camp.party === 'number' ? camp.party : c.party;
    if (party !== DEM && party !== REP) { p.independent = true; return p; }
    c.party = party;
    p.pres = c; p.won = !!result; p.mine = camp.idx === player;
    const vpo = party === DEM ? g.vpD : g.vpR;
    const vp = $bgCand(vpo);
    if (vp) { const known = candById(vp.id); p.vp = known !== null && known !== c ? known : vp; }
    if (result && typeof g.houseSeatsD === 'number' && g.houseSeatsD > 0) p.house = party === DEM ? g.houseSeatsD : 435 - g.houseSeatsD;
    const mid = g.set && typeof g.set.midSenateD === 'number' ? g.set.midSenateD : -1;
    if (result && mid > 0 && Array.isArray(g.senate)) {
        let up = 0, won = 0;
        for (const s of g.senate) { if (s && s.party === DEM) up++; if (s && s.margin > 0) won++; }
        const sd = mid - up + won;
        p.senate = party === DEM ? sd : 100 - sd;
    }
    if (typeof g.unemp === 'number') p.unemp = g.unemp;
    if (typeof g.inflation === 'number') p.infl = g.inflation;
    if (typeof g.gdp === 'number') p.gdp = g.gdp;
    const ev = party === DEM ? evD : evR, oev = party === DEM ? evR : evD;
    p.desc = c.name() + ' (' + PARTY_SHORT[party] + ')' + (p.won ? ', won ' + ev + '-' + oev + ' in 2028' : ', race not finished: takes office as if elected') + (p.mine ? '  (your campaign)' : '');
    return p;
}
BgImport.scan = function (done) {
    BgImport.found.clear();
    BgImport.note = null;
    const end = () => { BgImport.scanned = true; if (done) done(); };
    let r;
    try {
        if (typeof indexedDB === 'undefined') { end(); return; }
        r = indexedDB.open('battleground2028');
    } catch (e) { end(); return; }
    r.onupgradeneeded = () => { try { r.transaction.abort(); } catch (e) { } };
    r.onerror = () => end();
    r.onblocked = () => end();
    r.onsuccess = () => {
        const db = r.result;
        if (!db.objectStoreNames.contains('saves')) { db.close(); end(); return; }
        const keys = ['autosave', 'save1'];
        let left = keys.length;
        const finish = () => { if (--left === 0) { db.close(); end(); } };
        for (const k of keys) {
            let q;
            try { q = db.transaction('saves', 'readonly').objectStore('saves').get(k); } catch (e) { finish(); continue; }
            q.onsuccess = () => {
                try { if (q.result) { const p = $bgPres($bgDecode(JSON.parse(q.result)), k); if (p) BgImport.found.add(p); } }
                catch (e) { BgImport.note = "A Battleground 2028 save couldn't be read."; }
                finish();
            };
            q.onerror = () => finish();
        }
    };
};

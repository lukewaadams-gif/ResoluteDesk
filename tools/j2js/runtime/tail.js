// ===================================================================================
//  Start-up: the window, the frame loop and input
// ===================================================================================
function $boot() {
    const canvas = document.getElementById('game');
    const ctx = canvas.getContext('2d', { alpha: false });
    $host.canvas = canvas;
    Elect.init();
    USMap.load();
    Groups.init();
    const p = new ResoluteDesk();
    APP = p;
    p.frame = {
        dispose() {
            $host.stopped = true;
            if (sfx !== null) sfx.stop();
            try { window.close(); } catch (e) { }
            document.body.innerHTML = '<div style="color:#93a4bf;font:16px sans-serif;display:flex;height:100vh;align-items:center;justify-content:center">Thanks for playing Resolute Desk. Reload the page to play again.</div>';
        }
    };
    sfx = new Sfx();
    sfx.start();
    document.getElementById('boot').remove();

    function resize() {
        const dpr = window.devicePixelRatio || 1;
        const w = window.innerWidth, h = window.innerHeight;
        if (w !== $host.cssW || h !== $host.cssH || dpr !== $host.dpr) {
            $host.cssW = w; $host.cssH = h; $host.dpr = dpr;
            canvas.style.width = w + 'px'; canvas.style.height = h + 'px';
            canvas.width = Math.round(w * dpr); canvas.height = Math.round(h * dpr);
        }
    }
    function frame() {
        if ($host.stopped) return;
        resize();
        try {
            p.actionPerformed(null);
            const g = new Graphics2D(ctx, $host.dpr);
            p.paintComponent(g);
            g.dispose();
        } catch (e) { console.error(e); }
        requestAnimationFrame(frame);
    }

    // ---- input
    const unlock = () => $audio.unlock();
    let down = false;
    canvas.addEventListener('contextmenu', e => e.preventDefault());
    canvas.addEventListener('pointerdown', e => {
        unlock();
        down = true;
        try { canvas.setPointerCapture(e.pointerId); } catch (x) { }
        p.mousePressed(new MouseEventW(e, canvas));
        e.preventDefault();
    });
    canvas.addEventListener('pointerup', e => { down = false; p.mouseReleased(new MouseEventW(e, canvas)); });
    canvas.addEventListener('pointercancel', e => { down = false; p.mouseReleased(new MouseEventW(e, canvas)); });
    canvas.addEventListener('pointermove', e => {
        const w = new MouseEventW(e, canvas);
        if (down) p.mouseDragged(w); else p.mouseMoved(w);
    });
    canvas.addEventListener('pointerleave', e => { if (!down) p.mouseExited(new MouseEventW(e, canvas)); });
    canvas.addEventListener('wheel', e => {
        const k = e.deltaMode === 1 ? 1 / 3 : e.deltaMode === 2 ? 3 : 1 / 100;
        p.mouseWheelMoved(new MouseEventW(e, canvas, e.deltaY * k));
        e.preventDefault();
    }, { passive: false });
    const KEYS = { Enter: 10, Backspace: 8, Tab: 9, Escape: 27, ' ': 32, F1: 112, ArrowLeft: 37, ArrowUp: 38, ArrowRight: 39, ArrowDown: 40, Delete: 127 };
    window.addEventListener('keydown', e => {
        unlock();
        if (e.ctrlKey || e.metaKey || e.altKey) return;
        let code = KEYS[e.key];
        if (code === undefined && e.key.length === 1) {
            const c = e.key.toUpperCase().charCodeAt(0);
            code = (c >= 48 && c <= 57) || (c >= 65 && c <= 90) ? c : c;
        }
        if (code === undefined) return;
        p.keyPressed(new KeyEventW(code, 0));
        if (e.key.length === 1) p.keyTyped(new KeyEventW(code, e.key.charCodeAt(0)));
        if (e.key === ' ' || e.key === 'Tab' || e.key === 'F1' || e.key === 'Backspace' || e.key.startsWith('Arrow')) e.preventDefault();
    });
    window.addEventListener('keyup', e => { if (code0(e) !== undefined) p.keyReleased(new KeyEventW(code0(e), 0)); });
    function code0(e) { return KEYS[e.key] !== undefined ? KEYS[e.key] : (e.key.length === 1 ? e.key.toUpperCase().charCodeAt(0) : undefined); }
    window.__rd = { get app() { return APP; }, Screens, Creator, Oval, Rooms, Paper, Week, Setup, Settings, Economy, Approval, SaveGame, Game, candById, era, Diagnostics, HillUI, Legis, Decisions, Budget, Hill, Midterms, WestWing, Cabinet, Actions, Courts, Trouble, SitRoom, Diplo, Summit, PressRoom, HomeFront, Speech, QA, Social, Polls, History, Reelection, Terms, Legacy, Night, CampaignUI, BgImport, HallStore, Tutorial, Watch, Auto, ERAS, Hist: { leanIn, evFor, courtIn, applyEra } };
    canvas.focus();
    resize();
    requestAnimationFrame(frame);
}
if (typeof document !== 'undefined') {
    window.addEventListener('load', () => setTimeout(() => {
        try { $boot(); } catch (e) {
            console.error(e);
            const b = document.getElementById('boot');
            if (b) b.textContent = 'Could not start the game: ' + e;
        }
    }, 20));
}

})();
</script>
</body>
</html>

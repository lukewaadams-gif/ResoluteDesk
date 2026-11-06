// The West Wing in the browser: staff, nominations, an order, the VP, delegation; play on; save and resume
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => { const u = window.__rd.app; const g = u.game; const x = g ? g.ex : null; let filled = 0; if (x) for (const p of x.post) if (p) filled++;
    return { screen: u.screen, week: g ? g.week : -1, modal: u.modal ? u.modal.title : null, actions: x ? x.actions.size() : 0, filled, cos: x && x.staff[0] ? x.staff[0].name : null, press: x && x.staff[2] ? x.staff[2].name : null, vp: x ? x.vpMission : -1, scandals: x ? x.scandals.size() : 0 }; });
  const answer = async () => { for (let k = 0; k < 5; k++) { const s = await st(); if (!s.modal) break; await ev(() => { const u = window.__rd.app; const m = u.modal; u.modal = null; const a = m.actions ? m.actions[0] : null; if (a) (typeof a === "function" ? a() : a.run()); }); await P.waitForTimeout(120); } };
  await t.click('New presidency'); await P.waitForTimeout(300);
  await P.evaluate(() => { const s = window.__rd.app.setup; s.year = 2025; s.presId = null; s.vpId = null; s.custom = null; s.transition = false; }); await P.waitForTimeout(200);  // the 2025 era
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(400);
  await t.shot(OUT + '/01_cabinet_note.png');
  await answer();
  console.log('start', await st());
  await P.keyboard.press('5'); await P.waitForTimeout(400);
  await t.shot(OUT + '/02_staff.png');
  await ev(() => { window.__rd.WestWing.selStaff = 2; }); await P.waitForTimeout(300);
  await t.shot(OUT + '/03_pick.png');
  const hired = await t.click('Hire');
  await ev(() => { window.__rd.WestWing.tab = 1; window.__rd.WestWing.selPost = 6; }); await P.waitForTimeout(300);
  const wd = await t.click('Withdraw the nomination'); await P.waitForTimeout(200);
  const nom = await t.click('Nominate');
  await t.shot(OUT + '/04_cabinet.png');
  await ev(() => { const W = window.__rd.WestWing; W.tab = 3; W.lib = 0; const g = window.__rd.app.game; for (let i = 0; i < 49; i++) if (window.__rd.Actions.available(g, i) && i >= 5) { W.selAct = i; break; } }); await P.waitForTimeout(300);
  const signed = await t.click('Sign the order');
  await t.shot(OUT + '/05_orders.png');
  await ev(() => { window.__rd.WestWing.tab = 2; }); await P.waitForTimeout(300);
  const sent = await t.click('Send');
  await ev(() => { const g = window.__rd.app.game; g.ex.deleg[0] = true; g.ex.deleg[4] = true; });
  console.log('clicks', { hired, wd, nom, signed, sent }, await st());
  for (let i = 0; i < 30; i++) {
    await answer();
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); });
    await P.keyboard.press(' '); await P.waitForTimeout(220);
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); });
  }
  await answer();
  console.log('after 30 weeks', await st());
  for (let tb = 0; tb < 6; tb++) { await ev((k) => { window.__rd.Rooms.open(window.__rd.app, 4); window.__rd.WestWing.tab = k; }, tb); await P.waitForTimeout(250); await t.shot(OUT + '/06_tab' + tb + '.png'); }
  const before = await st();
  await t.click('Save'); await P.waitForTimeout(500);
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  const after = await st();
  console.log('resumed', after, 'match', before.week === after.week && before.actions === after.actions && before.press === after.press && before.filled === after.filled);
  await P.keyboard.press('5'); await P.waitForTimeout(400); await t.shot(OUT + '/07_resumed.png');
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

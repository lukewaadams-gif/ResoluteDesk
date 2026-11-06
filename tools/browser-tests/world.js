// The Situation Room in the browser: pick a country on the map, call, invite, summit, tariffs, an operation, covert action, a trip abroad; play on; save and resume
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => { const u = window.__rd.app; const g = u.game; const w = g ? g.world : null;
    return { screen: u.screen, week: g ? g.week : -1, modal: u.modal ? u.modal.title : null, sel: window.__rd.SitRoom.sel, relCN: w ? Math.round(w.rel[0]) : 0, relGB: w ? Math.round(w.rel[3]) : 0, tariffCN: w ? w.tariff[0] : -1, ops: w ? w.ops : 0, covert: w ? w.covert : 0, summit: w ? w.summitCountry : -9, deals: w ? w.deals : 0, crises: w ? w.crises : 0, visitsGB: w ? w.visits[3] : 0, tariff: g ? Math.round(g.econ.tariff * 10) / 10 : 0 }; });
  const answer = async () => { for (let k = 0; k < 6; k++) { const s = await st(); if (!s.modal) break; await ev(() => { const u = window.__rd.app; const m = u.modal; u.modal = null; const a = m.actions ? m.actions[0] : null; if (a) (typeof a === "function" ? a() : a.run()); }); await P.waitForTimeout(120); } };
  await t.click('New presidency'); await P.waitForTimeout(300);
  await P.evaluate(() => { const s = window.__rd.app.setup; s.year = 2025; s.presId = null; s.vpId = null; s.custom = null; s.transition = false; }); await P.waitForTimeout(200);  // the 2025 era
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(300);
  await answer();
  await P.keyboard.press('7'); await P.waitForTimeout(500);
  await t.shot(OUT + '/01_world.png');
  // click on the map: roughly the UK, then Russia
  await t.at(60 + 487, 186 + 85); await P.waitForTimeout(300);
  const s1 = await st();
  const called = await t.click('Call ', true);
  const invited = await t.click('Invite to the White House');
  await t.at(60 + 680, 186 + 55); await P.waitForTimeout(300);
  const s2 = await st();
  const summit = await t.click('Propose a summit');
  await t.shot(OUT + '/02_country.png');
  await ev(() => { window.__rd.SitRoom.tab = 3; }); await P.waitForTimeout(300);
  const before = await st();
  await ev(() => { window.__rd.Diplo.setTariff(window.__rd.app.game, 0, 2); }); await P.waitForTimeout(200);
  await t.shot(OUT + '/03_trade.png');
  await ev(() => { window.__rd.SitRoom.tab = 2; }); await P.waitForTimeout(300);
  const ordered = await t.click('Order');
  const signed = await t.click('Sign the finding');
  await t.shot(OUT + '/04_ops.png');
  // a trip abroad from the desk
  await ev(() => { const g = window.__rd.app.game; g.act[1] = 15; g.arg[1] = 9; g.act[2] = 15; g.arg[2] = 9; });
  console.log('picked', s1.sel, s2.sel, { called, invited, summit, ordered, signed }, 'tariff before', before.tariff, await st());
  for (let i = 0; i < 24; i++) {
    await answer();
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); });
    await P.keyboard.press(' '); await P.waitForTimeout(220);
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); });
  }
  await answer();
  console.log('after', await st());
  for (let tb = 0; tb < 5; tb++) { await ev((k) => { window.__rd.Rooms.open(window.__rd.app, 6); window.__rd.SitRoom.tab = k; }, tb); await P.waitForTimeout(250); await t.shot(OUT + '/05_tab' + tb + '.png'); }
  const b2 = await st();
  await ev(() => { window.__rd.SaveGame.save(window.__rd.app, 'slot1'); }); await P.waitForTimeout(500);
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  const a2 = await st();
  console.log('resumed', a2, 'match', b2.week === a2.week && b2.relCN === a2.relCN && b2.tariffCN === a2.tariffCN && b2.ops === a2.ops);
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

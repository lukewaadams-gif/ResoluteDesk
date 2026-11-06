// The Cabinet Room in the browser: choose a policy and a level, read its impacts, send a bill, sign an order, then the outcomes and the map
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  await t.click('New presidency'); await P.waitForTimeout(300);
  await P.evaluate(() => { const s = window.__rd.app.setup; s.year = 2029; s.presId = null; s.vpId = null; s.custom = null; s.tutorial = false; s.transition = false; }); await P.waitForTimeout(200);
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(300);
  for (let i = 0; i < 4 && await ev(() => !!window.__rd.app.modal); i++) await t.closeModal();
  await P.keyboard.press('9'); await P.waitForTimeout(400);
  const room = await ev(() => window.__rd.app.screen);
  await t.at(150, 290 + 20); await P.waitForTimeout(200);          // Jobs and work
  await t.at(500, 205); await P.waitForTimeout(200);               // the minimum wage
  await t.at(870 + 3 * 134 + 64, 245); await P.waitForTimeout(200); // about 55% of the typical wage
  await t.shot(OUT + '/01_minwage.png');
  const bills0 = await ev(() => window.__rd.app.game.cong.bills.size());
  await t.click('Send Congress a bill'); await P.waitForTimeout(300);
  const bills1 = await ev(() => window.__rd.app.game.cong.bills.size());
  console.log('room', room, 'bill sent', bills1 === bills0 + 1);
  // energy: drilling by executive order
  await t.at(150, 190 + 5 * 50 + 20); await P.waitForTimeout(200);
  await t.at(500, 205); await P.waitForTimeout(200);
  const before = await ev(() => { const g = window.__rd.app.game; return g.pol.changes; });
  await t.click('Sign an executive order'); await P.waitForTimeout(300);
  const after = await ev(() => { const g = window.__rd.app.game; return g.pol.changes; });
  console.log('order signed', after === before + 1);
  await t.shot(OUT + '/02_drilling.png');
  await t.click('seg:Outcomes'); await P.waitForTimeout(300); await t.shot(OUT + '/03_outcomes.png');
  await t.click('seg:System map'); await P.waitForTimeout(300);
  await P.mouse.move(...await ev(() => { const u = window.__rd.app; return [u.ox + 190 * u.sc, u.oy + 240 * u.sc]; })); await P.waitForTimeout(300);
  await t.shot(OUT + '/04_map.png');
  for (let i = 0; i < 6; i++) { await P.keyboard.press(' '); await P.waitForTimeout(300); for (let j = 0; j < 4 && await ev(() => !!window.__rd.app.modal); j++) await t.closeModal(); await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); }); }
  const wk = await ev(() => window.__rd.app.game.week);
  console.log('weeks played', wk);
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

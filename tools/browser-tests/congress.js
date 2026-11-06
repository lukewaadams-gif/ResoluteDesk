// Congress in the browser: draft a bill, work the votes, sign or veto, the budget and the midterms, save and resume
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => { const u = window.__rd.app; const g = u.game; return { screen: u.screen, week: g ? g.week : -1, modal: u.modal ? u.modal.title : null, inbox: g ? g.inbox.size() : 0, capital: g ? Math.round(g.capital) : 0, bills: g ? g.cong.bills.size() : 0, laws: g ? g.cong.laws : 0 }; });
  await t.click('New presidency'); await P.waitForTimeout(300);
  await P.evaluate(() => { const s = window.__rd.app.setup; s.year = 2025; s.presId = null; s.vpId = null; s.custom = null; }); await P.waitForTimeout(200);  // the 2025 era
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(300);
  console.log('start', await st());
  await P.keyboard.press('6'); await P.waitForTimeout(400);
  await t.shot(OUT + '/01_hill.png');
  await t.click('Meet (2 capital)'); await P.waitForTimeout(200);
  await ev(() => { window.__rd.HillUI.tab = 1; }); await P.waitForTimeout(300);
  // pick the first agenda bill by clicking its card
  await t.at(664 + 14 + 150, 194 + 18); await P.waitForTimeout(300);
  await t.shot(OUT + '/02_draft.png');
  const sent = await t.click('Send to Congress');
  console.log('sent', sent, await st());
  await P.waitForTimeout(300); await t.shot(OUT + '/03_whip.png');
  await t.click('Call'); await t.click('Go public', true);
  // play weeks, answering every decision with its first option
  for (let i = 0; i < 40; i++) {
    for (let k = 0; k < 4; k++) { const s = await st(); if (!s.modal) break; if (k === 0 && i < 30) await t.shot(OUT + '/04_decision_' + i + '.png'); await ev(() => { const u = window.__rd.app; const m = u.modal; u.modal = null; const a = m.actions ? m.actions[0] : null; if (a) (typeof a === "function" ? a() : a.run()); }); await P.waitForTimeout(150); }
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); });
    await P.keyboard.press(' '); await P.waitForTimeout(250);
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); });
  }
  console.log('after 40 weeks', await st());
  await ev(() => { const u = window.__rd.app; window.__rd.Rooms.open(u, 5); window.__rd.HillUI.tab = 3; });
  await P.waitForTimeout(300); await t.shot(OUT + '/05_budget.png');
  await ev(() => { window.__rd.HillUI.tab = 4; }); await P.waitForTimeout(300); await t.shot(OUT + '/06_elections.png');
  await ev(() => { window.__rd.HillUI.tab = 1; }); await P.waitForTimeout(300); await t.shot(OUT + '/07_bills.png');
  const before = await st();
  await t.click('Save'); await P.waitForTimeout(500);
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  const after = await st();
  console.log('resumed', after, 'match', before.week === after.week && before.bills === after.bills && before.capital === after.capital);
  await P.keyboard.press('6'); await P.waitForTimeout(400);
  await t.shot(OUT + '/08_resumed_hill.png');
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

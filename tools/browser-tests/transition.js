// The transition in the browser: interview, check, float and announce, end weeks, save and resume, then take the oath
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => { const u = window.__rd.app; const g = u.game; return { screen: u.screen, week: g && g.trans ? g.trans.week : -1, hours: g && g.trans ? g.trans.hoursUsed : -1, modal: u.modal ? u.modal.title : null }; });
  await t.click('New presidency'); await P.waitForTimeout(300);
  await P.evaluate(() => { const s = window.__rd.app.setup; s.year = 2029; s.presId = null; s.vpId = null; s.custom = null; s.tutorial = false; }); await P.waitForTimeout(200);
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Begin the transition'); await P.waitForTimeout(600);
  console.log('begin', await st());
  // the chief of staff's shortlist is open first: interview two, check one, name one
  const btns = () => ev(() => window.__rd.app.hits.filter(h => h.act && ['Interview', 'Check', 'Float', 'Senators', 'Announce'].includes(h.lab)).map(h => h.lab));
  console.log('buttons', (await btns()).length);
  await t.click('Interview'); await t.click('Interview'); await t.click('Check');
  console.log('after work', await st());
  await t.click('Announce'); await P.waitForTimeout(200);
  await t.shot(OUT + '/01_transition.png');
  // Defense: float a name and meet senators
  await P.keyboard.press('ArrowDown'); for (let i = 0; i < 12; i++) await P.keyboard.press('ArrowDown');
  await t.click('Float'); await t.click('Senators');
  await t.shot(OUT + '/02_defense.png');
  for (let i = 0; i < 3; i++) { await P.keyboard.press(' '); await P.waitForTimeout(300); }
  const mid = await st();
  console.log('three weeks on', mid);
  // the week autosaves: reload and resume into the transition
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  const back = await st();
  console.log('resumed', back, 'match', back.screen === 13 && back.week === mid.week);
  await t.click('Let the team finish'); await P.waitForTimeout(800);
  const done = await ev(() => { const u = window.__rd.app, g = u.game; let noms = 0, staff = 0; for (const p of g.ex.nominee) if (p) noms++; for (const p of g.ex.staff) if (p) staff++; return { screen: u.screen, modal: u.modal ? u.modal.title : null, noms, staff, approval: Math.round(g.approval), done: g.trans.done }; });
  console.log('inaugurated', done);
  await t.shot(OUT + '/03_oath.png');
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

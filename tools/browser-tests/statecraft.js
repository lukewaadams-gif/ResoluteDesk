// Statecraft and a war of choice in the browser: arm a partner, work an objective, start covert action, ask Congress,
// invade, run the war, set a timetable; then save, resume and check the war and objectives survived
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => {
    const g = window.__rd.app.game, w = g.world; let war = null;
    for (let i = 0; i < w.wars.size(); i++) { const x = w.wars.get(i); if (x.kind >= 0) war = x; }
    let tried = 0; for (let i = 0; i < w.objectives.size(); i++) { const o = w.objectives.get(i); if (o.step > 0 || o.fails > 0 || o.status !== 0) tried++; }
    return { week: g.week, milAidUA: w.milAid[2], covert: w.covert, authorizedVE: w.authorized[27], tried, war: war ? { name: war.name, kind: war.kind, stage: war.stage, troops: Math.round(war.troops), timetable: war.timetable, strategy: war.strategy } : null, toppled: w.toppled[27] };
  });
  await t.click('New presidency'); await P.waitForTimeout(300);
  await ev(() => { const s = window.__rd.app.setup; s.year = 2025; s.presId = null; s.vpId = null; s.custom = null; s.tutorial = false; s.transition = false; }); await P.waitForTimeout(200);
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(300);
  for (let i = 0; i < 4 && await ev(() => !!window.__rd.app.modal); i++) await t.closeModal();
  await P.keyboard.press('7'); await P.waitForTimeout(400);
  const tab = await t.click('seg:Statecraft');
  // Ukraine: cut military aid, then restore it, and work an objective
  await ev(() => { window.__rd.SitRoom.sel = 2; }); await P.waitForTimeout(250);
  await t.at(997, 205); const none = true;                          // military aid: none
  const s0 = await st();
  const major = await t.click('seg:Major');
  const s1 = await st();
  const tried = await t.click('Try it');
  await t.shot(OUT + '/01_ukraine.png');
  // Venezuela: covert action, a vote in Congress, then an invasion
  await ev(() => { window.__rd.SitRoom.sel = 27; }); await P.waitForTimeout(250);
  const cyber = await t.click('Cyber sabotage');
  const asked = await t.click('Ask Congress to authorize');
  await t.shot(OUT + '/02_venezuela.png');
  const invade = await t.click('An invasion');
  const warned = await ev(() => window.__rd.app.modal ? window.__rd.app.modal.title : null);
  await t.shot(OUT + '/03_warning.png');
  const ordered = await t.click('Give the order');
  const s2 = await st();
  console.log('statecraft', { tab, none, major, tried, cyber, asked, invade, ordered }, 'aid', s0.milAidUA, '->', s1.milAidUA, 'warning', warned, 'war', s2.war);
  // run the war for a few months
  await t.click('Send a tenth more');
  await t.click('seg:On the offensive');
  for (let i = 0; i < 12; i++) {
    for (let j = 0; j < 4 && await ev(() => !!window.__rd.app.modal); j++) await t.closeModal();
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); });
    await P.keyboard.press(' '); await P.waitForTimeout(260);
  }
  for (let j = 0; j < 6 && await ev(() => !!window.__rd.app.modal); j++) await t.closeModal();
  await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(5); window.__rd.Rooms.open(u, 6); window.__rd.SitRoom.tab = 6; window.__rd.SitRoom.selWar = 0; });
  await P.waitForTimeout(300);
  const s3 = await st();
  await t.shot(OUT + '/04_war.png');
  const timetable = await t.click('Timetable: a year');
  const s4 = await st();
  await t.shot(OUT + '/05_timetable.png');
  console.log('after 12 weeks', s3, 'timetable set', timetable, s4.war);
  // save and resume
  await ev(() => { window.__rd.SaveGame.save(window.__rd.app, window.__rd.SaveGame.AUTO); }); await P.waitForTimeout(500);   // Continue resumes the autosave
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  const s5 = await st();
  console.log('resumed', s5, 'match', JSON.stringify(s4) === JSON.stringify(s5));
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

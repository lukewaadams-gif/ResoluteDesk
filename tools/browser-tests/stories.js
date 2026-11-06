// The era's news and a storyline in the browser: the paper's sections, a storyline's decision, the developing-stories
// panel and the daily brief; then save, resume and check the storyline carried on
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => {
    const g = window.__rd.app.game, s = g.stories; let line = null;
    for (let i = 0; i < s.lines.size(); i++) { const l = s.lines.get(i); line = { stage: l.stage, status: l.status, heat: Math.round(l.heat), choices: l.choices }; }
    let sections = 0; for (let i = 0; i < g.news.size(); i++) if (g.news.get(i).section !== null) sections++;
    return { week: g.week, ran: s.ran, sections, lines: s.lines.size(), line, modal: window.__rd.app.modal ? window.__rd.app.modal.title : null };
  });
  await t.click('New presidency'); await P.waitForTimeout(300);
  await ev(() => { const s = window.__rd.app.setup; s.year = 1961; s.presId = null; s.vpId = null; s.custom = null; s.tutorial = false; s.transition = false; }); await P.waitForTimeout(200);
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(300);
  for (let i = 0; i < 4 && await ev(() => !!window.__rd.app.modal); i++) await t.closeModal();
  // a few weeks of news
  for (let i = 0; i < 8; i++) {
    for (let j = 0; j < 4 && await ev(() => !!window.__rd.app.modal); j++) await t.closeModal();
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(4); });
    await P.keyboard.press(' '); await P.waitForTimeout(260);
  }
  for (let j = 0; j < 6 && await ev(() => !!window.__rd.app.modal); j++) await t.closeModal();
  const s0 = await st();
  // a strike wave begins, and the president answers it (the translator names defs() defs$ because a field shares its name)
  await ev(() => { const R = window.__rd, g = R.app.game; g.inbox.clear(); const d = (R.Storylines.defs$ || R.Storylines.defs).call(R.Storylines); for (let i = 0; i < d.size(); i++) if (d.get(i).id === 'strike') R.Storylines.start(g, i); R.Decisions.show(R.app, g); });
  await P.waitForTimeout(300);
  const asked = await ev(() => window.__rd.app.modal ? window.__rd.app.modal.title : null);
  await t.shot(OUT + '/01_storyline.png');
  const answered = await t.click('Stand with the strikers');
  const s1 = await st();
  for (let i = 0; i < 3; i++) {
    for (let j = 0; j < 4 && await ev(() => !!window.__rd.app.modal); j++) await t.closeModal();
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(4); });
    await P.keyboard.press(' '); await P.waitForTimeout(260);
  }
  for (let j = 0; j < 6 && await ev(() => !!window.__rd.app.modal); j++) await t.closeModal();
  await ev(() => { window.__rd.app.go(6); }); await P.waitForTimeout(400);
  await t.shot(OUT + '/02_paper.png');
  await ev(() => { const u = window.__rd.app; u.go(4); window.__rd.Rooms.open(u, 6); window.__rd.SitRoom.tab = 1; }); await P.waitForTimeout(300);
  await t.shot(OUT + '/03_brief.png');
  const s2 = await st();
  console.log('news', s0, 'asked', asked, 'answered', answered, s1.line, 'later', s2);
  await ev(() => { window.__rd.SaveGame.save(window.__rd.app, window.__rd.SaveGame.AUTO); }); await P.waitForTimeout(500);
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  const s3 = await st();
  console.log('resumed', s3, 'match', JSON.stringify(s2.line) === JSON.stringify(s3.line) && s2.ran === s3.ran);
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

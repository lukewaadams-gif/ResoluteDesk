// History in the browser: a 1961 presidency with the tour, a war, the re-election campaign and election night,
// save and resume, a second term to the verdict, the library and Hall of Fame, the Battleground import screen and watch mode
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => { const R = window.__rd; const u = R.app; const g = u.game; const re = g ? g.re : null;
    let war = null; if (g && g.world && g.world.wars) for (const x of g.world.wars) if (x.stage === 2) war = { id: x.id, troops: Math.round(x.troops), strat: x.strat };
    let posts = 0; if (g && g.ex) for (const p of g.ex.post) if (p) posts++;
    return { screen: u.screen, week: g ? g.week : -1, year: g ? R.Hist && g.era.year : 0, term: g ? g.term : 0, tut: g ? g.tut : 0, watch: g ? g.watch : false, over: g ? g.over : false,
      modal: u.modal ? u.modal.title : null, night: g ? g.night : false, won: re ? re.won : null, ev: re ? re.evMe + '-' + re.evOpp : null, war, posts, ending: g ? g.ending : 0 }; });
  const pick = async (k) => { await ev((kk) => { const u = window.__rd.app; const m = u.modal; if (!m) return; u.modal = null; const a = m.actions ? m.actions[Math.min(kk, m.actions.length - 1)] : null; if (a) (typeof a === "function" ? a() : a.run()); }, k); await P.waitForTimeout(120); };
  const answer = async () => { for (let k = 0; k < 10; k++) { const s = await st(); if (!s.modal) break; await pick(0); } };
  // play on quickly, the way the hands-off president does, until a condition holds
  const fast = (cond, max) => ev(([c, m]) => { const R = window.__rd; const g = R.app.game; const f = new Function('g', 'R', 'return ' + c);
    for (let w = 0; w < m && !f(g, R); w++) { R.Auto.week(g); R.Week.resolve(g); R.Decisions.autoAll(g); } R.app.modal = null; return g.week; }, [cond, max]);
  const toOval = () => ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(4); });

  // 1. a new presidency in 1961, with the tour
  await t.click('New presidency'); await P.waitForTimeout(300);
  await ev(() => { const s = window.__rd.app.setup; s.year = 1961; s.presId = null; s.vpId = null; s.tutorial = true; }); await P.waitForTimeout(200);
  await t.shot(OUT + '/01_setup.png');
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(414, 218);
  await t.shot(OUT + '/02_pick.png');
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(300);
  await answer();
  const s0 = await st();
  await t.shot(OUT + '/03_tour.png');
  let nexts = 0; for (let i = 0; i < 9; i++) if (await t.click('Next') || await t.click('Done')) nexts++;
  const s1 = await st();
  console.log('start', s0, 'tour clicks', nexts, 'tour over', s1.tut === -1);

  // 2. to the fall of 1964: the campaign, then election night through the UI
  await fast('g.re != null && g.re.opp != null && R.Reelection.weeksTo(g) <= 1', 400);
  await ev(() => { window.__rd.Rooms.open(window.__rd.app, 7); window.__rd.PressRoom.tab = 5; }); await P.waitForTimeout(300);
  await t.shot(OUT + '/04_campaign.png');
  await ev(() => { window.__rd.app.go(4); });
  for (let i = 0; i < 6; i++) { const s = await st(); if (s.screen === 10) break; await answer(); await toOval(); await P.keyboard.press(' '); await P.waitForTimeout(400); }
  const sn = await st();
  await P.waitForTimeout(1500); await t.shot(OUT + '/05_night.png');
  await t.click('Skip to the result'); await P.waitForTimeout(400);
  await t.shot(OUT + '/06_night_result.png');
  const cont = await t.click('Four more years');
  if (!cont) await t.click('Skip to the end of the term');
  console.log('election night', sn.screen === 10, sn.ev, 'won', sn.won);

  // 3. into the second term; the war
  await fast('g.term >= 2 && g.world.wars.some(x => x.stage === 2)', 200);
  await ev(() => { window.__rd.Rooms.open(window.__rd.app, 6); window.__rd.SitRoom.tab = 6; window.__rd.SitRoom.selWar = 0; }); await P.waitForTimeout(300);
  for (let k = 0; k < 4; k++) { const s = await st(); if (!s.war) break; }
  const w0 = (await st()).war;
  const sent = await t.click('Send a tenth more');
  await answer();
  const w1 = (await st()).war;
  await t.shot(OUT + '/07_wars.png');
  console.log('war', w0, '->', w1, 'sent', sent);

  // 4. save and resume
  const b2 = await st();
  await ev(() => { window.__rd.SaveGame.save(window.__rd.app, 'autosave'); }); await P.waitForTimeout(500);
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  const a2 = await st();
  console.log('resumed', a2.week, a2.term, a2.year, 'match', b2.week === a2.week && b2.term === a2.term && b2.year === a2.year && JSON.stringify(b2.war) === JSON.stringify(a2.war) && b2.posts === a2.posts);

  // 5. to the end and the verdict
  await fast('g.over', 300);
  const se = await st();
  await ev(() => { window.__rd.Legacy.begin(window.__rd.app); }); await P.waitForTimeout(500);
  for (let k = 0; k < 5; k++) { await ev((kk) => { window.__rd.Legacy.tab = kk; }, k); await P.waitForTimeout(250); await t.shot(OUT + '/08_legacy_' + k + '.png'); }
  const sl = await st();
  await t.click('Hall of Fame'); await P.waitForTimeout(400);
  const hall = await ev(() => { try { return (localStorage.getItem('rd:hall') || '').split('\n').filter(x => x).length; } catch (e) { return -1; } });
  await t.shot(OUT + '/09_hall.png');
  console.log('ending', se.ending, 'over', se.over, 'legacy screen', sl.screen === 11, 'hall entries', hall);

  // 6. home: the import screen (no Battleground saves in this browser) and watch mode
  await t.click('Back'); await P.waitForTimeout(300);
  await ev(() => { window.__rd.app.go(0); }); await P.waitForTimeout(300);
  await t.click('From Battleground 2028'); await P.waitForTimeout(1200);
  const imp = await ev(() => { const B = window.__rd.BgImport; return { screen: window.__rd.app.screen, scanned: B.scanned, found: B.found.length, note: B.note }; });
  await t.shot(OUT + '/10_import.png');
  console.log('import', imp);
  await t.click('Back'); await P.waitForTimeout(300);
  await t.click('Watch history'); await P.waitForTimeout(500);
  const sw0 = await st();
  await P.waitForTimeout(7000);
  const sw1 = await st();
  await t.shot(OUT + '/11_watch.png');
  await t.click('Watching'); await P.waitForTimeout(300);
  const sw2 = await st();
  console.log('watch', sw0.year, sw0.week, '->', sw1.week, 'watching', sw1.watch, 'stopped', !sw2.watch);
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

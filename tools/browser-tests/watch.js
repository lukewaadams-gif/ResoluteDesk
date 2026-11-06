// Watch mode's plan and the 2029 setup in the browser: a preset, a bill list, a stop for bills to sign, fast
// watching until it stops for a decision; then a 2029 game with the Court, the election and tensions abroad set
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const plan = () => ev(() => { const g = window.__rd.app.game, p = g.plan; return { week: g.week, watch: g.watch, preset: p.preset, bills: p.bills.size(), stopSign: p.stop[0], speed: g.watchSpeed, modal: window.__rd.app.modal ? window.__rd.app.modal.title : null, screen: window.__rd.app.screen }; });
  await t.click('New presidency'); await P.waitForTimeout(300);
  await ev(() => { const s = window.__rd.app.setup; s.year = 2025; s.presId = null; s.vpId = null; s.custom = null; s.tutorial = false; s.transition = false; }); await P.waitForTimeout(200);
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(300);
  for (let i = 0; i < 4 && await ev(() => !!window.__rd.app.modal); i++) await t.closeModal();
  // the plan
  const opened = await t.click('Watch');
  const deal = await t.click('seg:Deal-maker');
  await t.at(840, 233);                                            // the first bill on the agenda
  const stop = await t.click('[  ]  Bills to sign');
  const fast = await t.click('seg:Fast');
  const p0 = await plan();
  await t.shot(OUT + '/01_plan.png');
  const started = await t.click('Start watching');
  // watch until it stops for a bill to sign, or 40 seconds pass
  let p1 = await plan();
  for (let i = 0; i < 80 && !p1.modal; i++) { await P.waitForTimeout(500); p1 = await plan(); }
  await t.shot(OUT + '/02_stopped.png');
  console.log('plan', { opened, deal, stop, fast, started }, p0, 'stopped for', p1);
  await t.closeModal();
  await P.waitForTimeout(1500);
  const p2 = await plan();
  await t.click('Watching...');
  const p3 = await plan();
  console.log('carried on', p2.week > p1.week || p2.modal !== null, 'stopped watching', !p3.watch);
  // a 2029 game, with the world set from the setup screen
  await ev(() => { window.__rd.app.go(0); }); await P.waitForTimeout(300);
  await t.click('New presidency'); await P.waitForTimeout(300);
  await ev(() => { const s = window.__rd.app.setup; s.year = 2029; s.presId = null; s.vpId = null; s.custom = null; s.tutorial = false; s.transition = false; s.cCourtR = 4; s.cTaiwan = 88; s.cUkraine = 15; s.cMideast = 70; s.cMargin = 9; s.cDeficit = 3; }); await P.waitForTimeout(300);
  await t.shot(OUT + '/03_setup_2029.png');
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  const w = await ev(() => { const R = window.__rd, g = R.app.game; let r = 0; for (let i = 0; i < g.ex.court.size(); i++) if (g.ex.court.get(i).party === 1) r++; return { year: g.era.year, courtR: r, court: g.ex.court.size(), twt: Math.round(g.world.twt), ua: g.world.uaStage, iran: Math.round(g.world.iran), deficit: Math.round(g.econ.deficit * 10) / 10 }; });
  console.log('2029', w);
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

// The core of the game in the browser: a new presidency, the week, a trip, the paper, save and resume
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => { const u = window.__rd.app; return [u.screen, u.game ? u.game.week : -1, u.modal ? u.modal.title : null]; });
  await t.shot(OUT + '/01_home.png');
  await t.click('New presidency'); await P.waitForTimeout(300);
  await P.evaluate(() => { const s = window.__rd.app.setup; s.year = 2025; s.presId = null; s.vpId = null; s.custom = null; }); await P.waitForTimeout(200);  // the 2025 era
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.shot(OUT + '/02_pick.png');
  await t.at(48 + 10 + 240 + 100, 166 + 50); // first real candidate card (after create)
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.shot(OUT + '/03_vp.png');
  await t.click('Take the oath'); await P.waitForTimeout(600);
  console.log('after oath', await st());
  await t.shot(OUT + '/04_oath.png');
  await t.click('Get to work'); await P.waitForTimeout(300);
  for (let i = 0; i < 6 && (await st())[2]; i++) await t.closeModal(); // the opening briefings
  await t.shot(OUT + '/05_oval.png');
  await t.at(800, 520); await P.waitForTimeout(300); // the desk
  console.log('desk', await st());
  await t.at(48 + 70, 128 + 38 + 60); await P.waitForTimeout(300); // Monday morning
  await t.shot(OUT + '/06_popup.png');
  await t.click('popup:White House remarks'); await P.waitForTimeout(300);
  await t.click('Immigration', true); await P.waitForTimeout(300);
  await t.click('Road week'); await P.waitForTimeout(200);
  await t.shot(OUT + '/07_desk.png');
  for (let i = 0; i < 6; i++) { await P.keyboard.press(' '); await P.waitForTimeout(400); if (i < 5) { await P.keyboard.press('Escape'); await P.waitForTimeout(150); await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(4); }); } }
  console.log('weeks', await st());
  for (let i = 0; i < 6 && (await st())[2]; i++) await t.closeModal(); // the week's news
  await ev(() => { const u = window.__rd.app; if (u.screen !== 6) u.go(6); }); await P.waitForTimeout(300); // the paper
  await t.shot(OUT + '/08_paper.png');
  await t.click('To the Oval Office'); await P.waitForTimeout(300);
  await P.keyboard.press('2'); await P.waitForTimeout(300); await t.shot(OUT + '/09_country.png');
  await P.keyboard.press('3'); await P.waitForTimeout(300); await t.shot(OUT + '/10_econ.png');
  await P.keyboard.press('4'); await P.waitForTimeout(300); await t.shot(OUT + '/11_residence.png');
  await t.click('Save'); await P.waitForTimeout(500);
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  console.log('resumed', await st());
  await t.shot(OUT + '/12_resumed.png');
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

// The Press Briefing Room in the browser: a press conference, an interview, a post, a poll, an address; the daily brief; play on; save and resume
const { open } = require('./lib');
(async () => {
  const OUT = process.argv[2] || 'browser-test-output'; require('fs').mkdirSync(OUT, { recursive: true });
  const t = await open(); const P = t.page;
  const ev = (f, a) => P.evaluate(f, a);
  const st = () => ev(() => { const u = window.__rd.app; const g = u.game; const h = g ? g.home : null;
    return { screen: u.screen, week: g ? g.week : -1, modal: u.modal ? u.modal.title : null, pressers: h ? h.pressers : 0, interviews: h ? h.interviews : 0, posts: h ? h.posts : 0, speeches: h ? h.speeches : 0, polls: h ? h.polls : 0, disasters: h ? h.disasters.size() : 0, fac: h ? Math.round(h.fac[0]) : 0, brief: h && h.brief ? h.brief.size() : 0 }; });
  const pick = async (k) => { await ev((kk) => { const u = window.__rd.app; const m = u.modal; if (!m) return; u.modal = null; const a = m.actions ? m.actions[Math.min(kk, m.actions.length - 1)] : null; if (a) (typeof a === "function" ? a() : a.run()); }, k); await P.waitForTimeout(150); };
  const answer = async () => { for (let k = 0; k < 8; k++) { const s = await st(); if (!s.modal) break; await pick(0); } };
  await t.click('New presidency'); await P.waitForTimeout(300);
  await P.evaluate(() => { const s = window.__rd.app.setup; s.year = 2025; s.presId = null; s.vpId = null; s.custom = null; s.transition = false; }); await P.waitForTimeout(200);  // the 2025 era
  await t.click('Choose your president'); await P.waitForTimeout(400);
  await t.at(48 + 10 + 240 + 100, 166 + 50);
  await t.click('Next: your running mate'); await P.waitForTimeout(300);
  await t.click('Take the oath'); await P.waitForTimeout(600);
  await t.click('Get to work'); await P.waitForTimeout(300);
  await answer();
  await P.keyboard.press('8'); await P.waitForTimeout(500);
  await t.shot(OUT + '/01_press.png');
  const pc = await t.click('Hold a press conference'); await P.waitForTimeout(300);
  await t.shot(OUT + '/02_question.png');
  for (let i = 0; i < 4; i++) await pick(i % 4);
  await answer();
  const iv = await t.click('Do the interview'); await P.waitForTimeout(300);
  for (let i = 0; i < 4; i++) await pick(1);
  await answer();
  await ev(() => { window.__rd.app.game.funds = 5; window.__rd.PressRoom.tab = 0; }); await P.waitForTimeout(250);
  const poll = await t.click('Run the poll');
  await t.shot(OUT + '/03_poll.png');
  await ev(() => { window.__rd.PressRoom.tab = 2; }); await P.waitForTimeout(250);
  const post = await t.click('Post it');
  await ev(() => { window.__rd.PressRoom.tab = 1; }); await P.waitForTimeout(250);
  const addr = await t.click('Request airtime'); await P.waitForTimeout(300);
  await t.shot(OUT + '/04_speech.png');
  for (let i = 0; i < 6; i++) await pick(i % 3);
  await answer();
  console.log('clicks', { pc, iv, poll, post, addr }, await st());
  for (let i = 0; i < 30; i++) {
    await answer();
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(4); });
    await P.keyboard.press(' '); await P.waitForTimeout(200);
    await ev(() => { const u = window.__rd.app; if (u.screen === 6) u.go(4); });
  }
  await answer();
  console.log('after', await st());
  for (let tb = 0; tb < 5; tb++) { await ev((k) => { window.__rd.Rooms.open(window.__rd.app, 7); window.__rd.PressRoom.tab = k; }, tb); await P.waitForTimeout(250); await t.shot(OUT + '/05_tab' + tb + '.png'); }
  await ev(() => { window.__rd.Rooms.open(window.__rd.app, 6); window.__rd.SitRoom.tab = 1; }); await P.waitForTimeout(250); await t.shot(OUT + '/06_brief.png');
  const b2 = await st();
  await ev(() => { window.__rd.SaveGame.save(window.__rd.app, 'autosave'); }); await P.waitForTimeout(500);
  await P.reload(); await P.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 }); await P.waitForTimeout(800);
  await t.click('Continue'); await P.waitForTimeout(1200);
  const a2 = await st();
  console.log('resumed', a2, 'match', b2.week === a2.week && b2.pressers === a2.pressers && b2.posts === a2.posts && b2.speeches === a2.speeches && b2.disasters === a2.disasters);
  console.log('ERRORS', t.errs.length, t.errs.slice(0, 5).join('\n'));
  await t.b.close();
})();

// Shared helpers for driving the browser version with Playwright.
// The page is index.html at the root of the repository, or the file named by RD_HTML.
const path = require('path');
const { chromium } = require('playwright');

async function open(url) {
  const file = url || process.env.RD_HTML || path.resolve(__dirname, '..', '..', 'index.html');
  const b = await chromium.launch();
  const page = await b.newPage({ viewport: { width: 1600, height: 900 } });
  const errs = [];
  page.on('pageerror', e => errs.push(e.stack || e.message));
  page.on('console', m => { if (m.type() === 'error') errs.push(m.text()); });
  await page.goto('file://' + path.resolve(file));
  await page.waitForFunction(() => !document.getElementById('boot'), null, { timeout: 120000 });
  await page.waitForTimeout(400);
  // the game's own hit regions name its buttons, so tests click by label like a player would
  const find = (lab, any) => page.evaluate(([lab, any]) => {
    const u = window.__rd.app; let f = null;
    for (const h of u.hits) if (h.lab && (any ? h.lab.includes(lab) : h.lab.startsWith(lab)) && h.act && h.layer === u.topLayer) f = h;
    return f ? [u.ox + (f.x + f.w / 2) * u.sc, u.oy + (f.y + f.h / 2) * u.sc] : null;
  }, [lab, !!any]);
  const click = async (lab, any) => {
    await page.waitForTimeout(120);
    const p = await find(lab, any);
    if (!p) { console.log('no button', lab); return false; }
    await page.mouse.click(p[0], p[1]);
    await page.waitForTimeout(220);
    return true;
  };
  const at = async (x, y) => {
    const p = await page.evaluate(([x, y]) => { const u = window.__rd.app; return [u.ox + x * u.sc, u.oy + y * u.sc]; }, [x, y]);
    await page.mouse.click(p[0], p[1]);
    await page.waitForTimeout(220);
  };
  // close the window on top by pressing its first button, as a player would
  const closeModal = async () => {
    const p = await page.evaluate(() => {
      const u = window.__rd.app; if (!u.modal) return null; let f = null;
      for (const h of u.hits) if (h.act && h.layer === 2 && h.lab && !f) f = h;
      return f ? [u.ox + (f.x + f.w / 2) * u.sc, u.oy + (f.y + f.h / 2) * u.sc] : null;
    });
    if (p) await page.mouse.click(p[0], p[1]); else await page.keyboard.press('Escape');
    await page.waitForTimeout(250);
  };
  const shot = (p) => page.screenshot({ path: p });
  return { b, page, errs, find, click, at, shot, closeModal };
}

module.exports = { open };

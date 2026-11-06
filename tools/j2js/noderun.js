// Runs the browser build headless in node: node noderun.js index.html "game 2 1 shapiro 0 ai"
const fs = require('fs');
const html = fs.readFileSync(process.argv[2], 'utf8');
let js = html.substring(html.indexOf('<script>') + 8, html.lastIndexOf('</script>'));
const k = js.lastIndexOf('})();');
js = js.substring(0, k) + '\nDiagnostics.run(' + JSON.stringify(process.argv[3].split(' ')) + ');\n' + js.substring(k);
new Function(js)();

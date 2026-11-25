(function () {
  'use strict';
  function $(s, r) { return (r || document).querySelector(s); }
  function $$(s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); }
  var ls = {
    get: function (k) { try { return localStorage.getItem(k); } catch (e) { return null; } },
    set: function (k, v) { try { localStorage.setItem(k, v); } catch (e) {} }
  };

  // ----- light / dark switch -----
  var tb = $('#theme');
  if (tb) tb.addEventListener('click', function () {
    var d = document.documentElement, cur = d.getAttribute('data-theme');
    var dark = cur ? cur === 'dark' : window.matchMedia('(prefers-color-scheme: dark)').matches;
    var next = dark ? 'light' : 'dark';
    d.setAttribute('data-theme', next); ls.set('theme', next);
  });

  // ----- active menu item -----
  var path = location.pathname;
  $$('[data-nav]').forEach(function (a) {
    var h = a.getAttribute('href');
    var on = (h === '/' || a.hasAttribute('data-exact')) ? path === h : (path === h || path.indexOf(h + '/') === 0);
    if (on) { a.classList.add('on'); a.setAttribute('aria-current', 'page'); }
  });

  // ----- saved posts (this browser only) -----
  function saved() { try { return JSON.parse(ls.get('saved') || '[]'); } catch (e) { return []; } }
  $$('[data-save]').forEach(function (b) {
    var s = b.getAttribute('data-save'), t = b.getAttribute('data-title');
    function paint() {
      var on = saved().some(function (x) { return x.s === s; });
      b.textContent = on ? 'Saved' : 'Save'; b.setAttribute('aria-pressed', on ? 'true' : 'false');
    }
    b.addEventListener('click', function () {
      var a = saved();
      if (a.some(function (x) { return x.s === s; })) a = a.filter(function (x) { return x.s !== s; }); else a.push({ s: s, t: t });
      ls.set('saved', JSON.stringify(a)); paint();
    });
    paint();
  });
  var box = $('#saved-list');
  if (box) {
    var list = saved();
    if (!list.length) box.textContent = 'Nothing saved yet. Use the Save button on any post.';
    list.forEach(function (x) {
      var link = document.createElement('a'); link.href = '/post/' + encodeURIComponent(x.s); link.textContent = x.t;
      var row = document.createElement('div'); row.className = 'row'; row.appendChild(link); box.appendChild(row);
    });
  }

  // ----- copy link, print -----
  $$('[data-copy]').forEach(function (b) {
    b.addEventListener('click', function () {
      var txt = b.getAttribute('data-copy-text') || location.href, old = b.textContent;
      function ok() { b.textContent = 'Copied'; setTimeout(function () { b.textContent = old; }, 1500); }
      if (navigator.clipboard) navigator.clipboard.writeText(txt).then(ok); else { var i = document.createElement('input'); i.value = txt; document.body.appendChild(i); i.select(); document.execCommand('copy'); i.remove(); ok(); }
    });
  });
  $$('[data-print]').forEach(function (b) { b.addEventListener('click', function () { window.print(); }); });

  // ----- trending: top 5 on phones, button reveals the rest -----
  var tm = $('#tm'), tl = $('#h-trend');
  if (tm && tl) tm.addEventListener('click', function () {
    tl.classList.toggle('open'); tm.textContent = tl.classList.contains('open') ? 'Show top 5' : 'Show all';
  });

  // ----- search suggestions -----
  var qi = $('#hq'), sg = $('#sugg'), timer = null;
  if (qi && sg) {
    qi.addEventListener('input', function () {
      clearTimeout(timer); var v = qi.value.trim();
      if (v.length < 2) { sg.hidden = true; return; }
      timer = setTimeout(function () {
        fetch('/search/suggest?q=' + encodeURIComponent(v)).then(function (r) { return r.json(); }).then(function (a) {
          sg.innerHTML = '';
          if (!a.length) { sg.hidden = true; return; }
          a.forEach(function (x) { var l = document.createElement('a'); l.href = x.u; l.textContent = x.t; sg.appendChild(l); });
          sg.hidden = false;
        }).catch(function () { sg.hidden = true; });
      }, 200);
    });
    document.addEventListener('click', function (e) { if (!sg.contains(e.target) && e.target !== qi) sg.hidden = true; });
    qi.addEventListener('keydown', function (e) { if (e.key === 'Escape') sg.hidden = true; });
  }

  // ----- cookie notice, back to top -----
  var ck = $('#cookie');
  if (ck && !ls.get('cookieOk')) { ck.hidden = false; $('#cookie-ok').addEventListener('click', function () { ls.set('cookieOk', '1'); ck.hidden = true; }); }
  var up = $('#totop');
  if (up) {
    window.addEventListener('scroll', function () { up.hidden = window.scrollY < 600; }, { passive: true });
    up.addEventListener('click', function () { window.scrollTo({ top: 0, behavior: 'smooth' }); });
  }

  // ----- admin helpers -----
  $$('form[data-confirm]').forEach(function (f) {
    f.addEventListener('submit', function (e) { if (!confirm(f.getAttribute('data-confirm'))) e.preventDefault(); });
  });
  var checks = $$('.vf'), pub = $('#pub'), note = $('#vnote');
  if (checks.length && pub) {
    var update = function () {
      var ok = checks.every(function (c) { return c.checked; });
      pub.disabled = !ok; if (note) note.textContent = ok ? 'All checks done. Ready to publish.' : 'Tick all checks to enable Publish.';
    };
    checks.forEach(function (c) { c.addEventListener('change', update); }); update();
  }
  // editor toolbar: inserts HTML around the selected text
  var SNIP = {
    h2: ['<h2>', '</h2>'], b: ['<b>', '</b>'], ul: ['<ul>\n<li>', '</li>\n<li></li>\n</ul>'],
    a: ['<a href="https://">', '</a>'], img: ['<img src="/uploads/" alt="', '">'],
    table: ['<table>\n<tr><th>', '</th><th></th></tr>\n<tr><td></td><td></td></tr>\n</table>'],
    note: ['<p><b>Note:</b> ', '</p>']
  };
  $$('[data-ins]').forEach(function (b) {
    b.addEventListener('click', function () {
      var ta = document.getElementById('body'), sn = SNIP[b.getAttribute('data-ins')]; if (!ta || !sn) return;
      var s = ta.selectionStart, e = ta.selectionEnd, sel = ta.value.substring(s, e);
      ta.value = ta.value.substring(0, s) + sn[0] + sel + sn[1] + ta.value.substring(e); ta.focus();
      ta.selectionStart = ta.selectionEnd = s + sn[0].length + sel.length;
    });
  });
})();

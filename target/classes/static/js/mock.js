(function () {
  var f = document.getElementById('quiz'); if (!f) return;
  var left = (parseInt(f.getAttribute('data-minutes'), 10) || 20) * 60, timer = null, done = false;
  var qs = f.querySelectorAll('.q'), clock = document.getElementById('clock');
  function show() { var m = Math.floor(left / 60), s = left % 60; clock.textContent = m + ':' + (s < 10 ? '0' : '') + s; }
  function count() { var n = 0; qs.forEach(function (q) { if (q.querySelector('input:checked')) n++; }); document.getElementById('answered').textContent = n + ' of ' + qs.length + ' answered'; }
  function finish() {
    if (done) return; done = true; clearInterval(timer);
    var right = 0;
    qs.forEach(function (q) {
      var ans = q.getAttribute('data-correct'), sel = q.querySelector('input:checked');
      if (sel && sel.value === ans) right++;
      q.querySelectorAll('.opt').forEach(function (o) {
        var v = o.querySelector('input').value; o.querySelector('input').disabled = true;
        if (v === ans) o.classList.add('right'); else if (sel && sel.value === v) o.classList.add('wrong');
      });
      var ex = q.querySelector('.explain'); if (ex && ex.textContent.trim()) ex.hidden = false;
    });
    document.getElementById('score').textContent = right + ' / ' + qs.length;
    var pct = qs.length ? Math.round(right * 100 / qs.length) : 0;
    document.getElementById('scoreText').textContent = pct + '% correct. ' + (pct >= 70 ? 'Great work!' : pct >= 40 ? 'Good try, practise a little more.' : 'Keep practising, you will improve.');
    document.getElementById('result').hidden = false; document.getElementById('finish').disabled = true;
    document.getElementById('result').scrollIntoView({ behavior: 'smooth' });
  }
  document.getElementById('start').addEventListener('click', function () {
    document.getElementById('intro').hidden = true; document.getElementById('qs').hidden = false; show();
    timer = setInterval(function () { left--; show(); if (left <= 0) finish(); }, 1000);
  });
  document.getElementById('finish').addEventListener('click', function () { if (confirm('Submit the test now?')) finish(); });
  f.addEventListener('change', count);
})();

(function () {
  var $ = function (id) { return document.getElementById(id); };
  $('asOn').valueAsDate = new Date();
  $('go').addEventListener('click', function () {
    var out = $('out');
    if (!$('dob').value || !$('asOn').value) { out.textContent = 'Please choose both dates.'; return; }
    var d = new Date($('dob').value), t = new Date($('asOn').value);
    if (d > t) { out.textContent = 'Date of birth is after the "age as on" date.'; return; }
    var y = t.getFullYear() - d.getFullYear(), m = t.getMonth() - d.getMonth(), days = t.getDate() - d.getDate();
    if (days < 0) { m--; days += new Date(t.getFullYear(), t.getMonth(), 0).getDate(); }
    if (m < 0) { y--; m += 12; }
    var html = '<div class="big">' + y + ' years, ' + m + ' months, ' + days + ' days</div>';
    var min = parseInt($('minAge').value, 10), max = parseInt($('maxAge').value, 10), rel = parseInt($('relax').value, 10) || 0;
    var after = function (lim) { return y > lim || (y === lim && (m > 0 || days > 0)); };
    if (!isNaN(max)) {
      var limit = max + rel;
      html += '<p class="pill ' + (after(limit) ? 'errc' : 'ok') + '" style="margin:8px 0 0">' + (after(limit) ? 'Above the maximum age (' + limit + ')' : 'Within the maximum age (' + limit + ')') + '</p>';
    }
    if (!isNaN(min)) {
      html += '<p class="pill ' + (y < min ? 'errc' : 'ok') + '" style="margin:8px 0 0">' + (y < min ? 'Below the minimum age (' + min + ')' : 'Meets the minimum age (' + min + ')') + '</p>';
    }
    out.innerHTML = html;
  });
})();

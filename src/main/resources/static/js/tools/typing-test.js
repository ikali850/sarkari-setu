(function () {
  var $ = function (id) { return document.getElementById(id); };
  var texts = [
    'Government jobs need patience and regular practice. Read the notification carefully, keep your documents ready and always apply before the last date.',
    'A good habit is to check the official website every day. Dates can change, so keep a printed copy of your application and admit card with you.',
    'Typing speed matters in many exams. Sit straight, keep your eyes on the screen and press each key with the correct finger to build accuracy first.'
  ];
  var text = texts[Math.floor(Math.random() * texts.length)], started = false, left = 60, timer = null;
  var box = $('text'), input = $('typed');
  function paint() {
    var v = input.value, h = '';
    for (var i = 0; i < text.length; i++) {
      var ch = text[i].replace('&', '&amp;').replace('<', '&lt;');
      if (i < v.length) h += '<span class="' + (v[i] === text[i] ? 'good' : 'bad') + '">' + ch + '</span>'; else h += ch;
    }
    box.innerHTML = h;
  }
  function finish() {
    clearInterval(timer); input.disabled = true;
    var v = input.value, correct = 0;
    for (var i = 0; i < v.length && i < text.length; i++) if (v[i] === text[i]) correct++;
    var mins = (60 - left) / 60 || 1 / 60;
    $('result').innerHTML = '<div class="big">' + Math.round(correct / 5 / mins) + ' WPM</div><p>Accuracy: ' + (v.length ? Math.round(correct / v.length * 100) : 0) + '%</p>';
  }
  input.addEventListener('input', function () {
    if (!started) { started = true; timer = setInterval(function () { left--; $('time').textContent = left; if (left <= 0) finish(); }, 1000); }
    paint(); if (input.value.length >= text.length) finish();
  });
  $('again').addEventListener('click', function () { location.reload(); });
  paint();
})();

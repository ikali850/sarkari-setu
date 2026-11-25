(function () {
  var $ = function (id) { return document.getElementById(id); };
  var img = null;
  $('file').addEventListener('change', function () {
    var f = this.files[0]; if (!f) return;
    var r = new FileReader();
    r.onload = function () { img = new Image(); img.onload = function () { $('w').placeholder = img.width; $('h').placeholder = img.height; $('msg').textContent = 'Original: ' + img.width + ' x ' + img.height + ' px, ' + Math.round(f.size / 1024) + ' KB'; }; img.src = r.result; };
    r.readAsDataURL(f);
  });
  $('go').addEventListener('click', function () {
    if (!img) { $('msg').textContent = 'Choose a photo first.'; return; }
    var w = parseInt($('w').value, 10) || img.width, h = parseInt($('h').value, 10) || img.height, maxKb = parseInt($('kb').value, 10) || 50;
    var c = document.createElement('canvas'); c.width = w; c.height = h;
    var ctx = c.getContext('2d'); ctx.fillStyle = '#fff'; ctx.fillRect(0, 0, w, h); ctx.drawImage(img, 0, 0, w, h);
    var q = 0.92;
    (function step() {
      c.toBlob(function (b) {
        if (b.size <= maxKb * 1024 || q <= 0.1) {
          var url = URL.createObjectURL(b);
          $('prev').src = url; $('prev').hidden = false;
          $('dl').href = url; $('dl').hidden = false;
          $('msg').textContent = 'Result: ' + w + ' x ' + h + ' px, ' + Math.round(b.size / 1024) + ' KB' + (b.size > maxKb * 1024 ? ' (could not reach ' + maxKb + ' KB, try smaller dimensions)' : '');
        } else { q -= 0.05; step(); }
      }, 'image/jpeg', q);
    })();
  });
})();

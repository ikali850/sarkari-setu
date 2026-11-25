(function () {
  var $ = function (id) { return document.getElementById(id); };
  $('go1').addEventListener('click', function () {
    var o = parseFloat($('obt').value), t = parseFloat($('tot').value);
    $('out1').textContent = (!(t > 0) || isNaN(o)) ? 'Enter marks obtained and total marks.' : ((o / t) * 100).toFixed(2) + '%';
  });
  $('go2').addEventListener('click', function () {
    var c = parseFloat($('cgpa').value), f = parseFloat($('factor').value);
    $('out2').textContent = (isNaN(c) || isNaN(f)) ? 'Enter CGPA and the multiplier.' : (c * f).toFixed(2) + '%';
  });
})();

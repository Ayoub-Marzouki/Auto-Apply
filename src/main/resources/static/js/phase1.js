// Phase 1: upload with live server-sent progress logs (overlay restored)
(function(){
  function uuid(){return URL.createObjectURL(new Blob()).split('/').pop();}

  document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('form');
    if(!form) return;
    const submitBtn = form.querySelector('button[type="submit"]');
    const hiddenId = document.getElementById('correlationId');
    const correlationId = uuid();
    if(hiddenId) hiddenId.value = correlationId;

    // Build overlay
    const overlay = document.createElement('div');
    overlay.className = 'phase1-overlay-backdrop';
    overlay.style.display = 'none';
    overlay.innerHTML = `
      <div class="phase1-overlay-card">
        <div class="phase1-overlay-header">
          <div class="spinner"></div>
          <div>
            <div class="title">Parsing your CV…</div>
            <div class="sub">Real-time progress below.</div>
          </div>
        </div>
        <ul class="log-list" id="phase1-log-list"></ul>
      </div>`;
    document.body.appendChild(overlay);

    function appendLog(msg, cls){
      const ul = overlay.querySelector('#phase1-log-list');
      const li = document.createElement('li');
      li.className = 'log-step' + (cls? ' '+cls: '');
      li.innerHTML = `<span class="dot"></span><span class="msg">${escapeHtml(msg)}</span>`;
      ul.appendChild(li); ul.scrollTop = ul.scrollHeight;
    }
    function escapeHtml(str){return (str||'').replace(/[&<>]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;'}[c]));}

    form.addEventListener('submit', (evt) => {
      evt.preventDefault();
      if(submitBtn) submitBtn.disabled = true;
      overlay.style.display = 'flex';
      appendLog('Uploading file…','active');

      // Open SSE stream BEFORE submission so we don't miss events
      const es = new EventSource(`/upload-cv/progress/${correlationId}`);
      es.addEventListener('connected', () => appendLog('Connected.','info'));
      es.addEventListener('step', (e) => appendLog(e.data, 'active'));
      es.addEventListener('snippet', (e) => appendLog(e.data, 'snippet'));
      es.addEventListener('info', (e) => appendLog(e.data, 'info'));
      es.addEventListener('done', () => { /* navigation will happen via server redirect */ });

      // Submit via fetch so SSE stays open until server navigates
      const fd = new FormData(form);
      fetch(form.action, {method:'POST', body:fd})
        .then(r => {
          if(r.redirected) window.location.href = r.url;
          else return r.text().then(t => { throw new Error('Upload failed: '+t); });
        })
        .catch(err => {
          appendLog('Error: ' + err.message, 'error');
          if(submitBtn) submitBtn.disabled = false;
          es.close();
        });
    });
  });
})();

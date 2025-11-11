// Phase 2 specific logic can go here (e.g., collecting skill weights)
(function(){
  // Placeholder for future persistence logic

  // Phase 2 page interactions: add/edit/remove skills + languages
  (function(){
    const cvMeta = document.getElementById('cv-meta');
    const cvId = cvMeta ? cvMeta.getAttribute('data-cv-id') : null;
    const profileId = cvMeta ? cvMeta.getAttribute('data-profile-id') : null;

    async function saveSkillsSnapshot(){
      if(!profileId) return;
      const prefs = [
        ...Array.from(document.querySelectorAll('#technical-skills-list .skill-row')),
        ...Array.from(document.querySelectorAll('#tools-list .skill-row')),
        ...Array.from(document.querySelectorAll('#soft-skills-list .skill-row'))
      ].map(row => ({
        name: row.querySelector('span')?.textContent?.trim() || '',
        importance: Number(row.querySelector('.importance-select')?.value || 5),
        mustHave: !!row.querySelector('.must-have')?.checked
      }));
      try {
        await fetch(`/api/v1/wizard/profiles/${profileId}/skills`, { method:'PATCH', headers:{'Content-Type':'application/json'}, body: JSON.stringify(prefs) });
        console.debug('skills saved');
      } catch(e){ console.warn('skills save failed', e); }
    }

    async function saveLanguagesSnapshot(){
      if(!profileId) return;
      const getList = (sel) => Array.from(document.querySelectorAll(sel)).map(r => r.querySelector('span')?.textContent?.trim()).filter(Boolean);
      const payload = { spokenLanguages: getList('#spoken-lang-list .lang-row'), programmingLanguages: getList('#programming-lang-list .lang-row') };
      try {
        await fetch(`/api/v1/wizard/profiles/${profileId}/languages`, { method:'PATCH', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) });
        console.debug('languages saved');
      } catch(e){ console.warn('languages save failed', e); }
    }

    ['technical-skills-list', 'tools-list', 'soft-skills-list'].forEach(listId => {
      const list = document.getElementById(listId);
      if(!list) return;
      list.addEventListener('change', () => { saveSkillsSnapshot(); });
      list.addEventListener('click', (e) => {
        const btn = e.target.closest('.remove-skill');
        if(btn){ setTimeout(saveSkillsSnapshot, 0); }
      });
    });

    document.addEventListener('click', (e) => {
      const langBtn = e.target.closest('.remove-lang, [data-add-lang], .edit-lang');
      if(langBtn){ setTimeout(saveLanguagesSnapshot, 0); }
    });

    ['add-technical-skill', 'add-tool', 'add-soft-skill'].forEach(btnId => {
      document.getElementById(btnId)?.addEventListener('click', () => setTimeout(saveSkillsSnapshot, 0));
    });

    // Skills add/remove/edit for all three lists
    (function(){
      ['technical-skills-list', 'tools-list', 'soft-skills-list'].forEach(listId => {
        const list = document.getElementById(listId);
        if(!list) return;
        
        list.addEventListener('click', (e) => {
          const btn = e.target;
          const row = btn.closest('.skill-row');
          if(!row) return;
          const label = row.querySelector('span');

          if(btn.classList.contains('remove-skill')){
            const name = label?.textContent?.trim();
            row.remove();
            persistSkillChange({ op: 'remove', name });
          }
          if(btn.classList.contains('edit-skill')){
            const curr = label?.textContent?.trim() || '';
            const next = prompt('Edit skill name', curr);
            if(next && next.trim() && next !== curr){
              label.textContent = next.trim();
              persistSkillChange({ op: 'rename', from: curr, to: next.trim() });
            }
          }
        });

        list.addEventListener('change', (e) => {
          const row = e.target.closest('.skill-row');
          if(!row) return;
          const name = row.querySelector('span')?.textContent?.trim();
          if(e.target.classList.contains('must-have')){
            persistSkillChange({ op: 'flag', name, mustHave: e.target.checked });
          }
          if(e.target.classList.contains('importance-select')){
            persistSkillChange({ op: 'weight', name, importance: Number(e.target.value) });
          }
        });
      });

      // Add buttons
      const addConfigs = [
        { btnId: 'add-technical-skill', inputId: 'new-technical-skill', listId: 'technical-skills-list' },
        { btnId: 'add-tool', inputId: 'new-tool', listId: 'tools-list' },
        { btnId: 'add-soft-skill', inputId: 'new-soft-skill', listId: 'soft-skills-list' }
      ];
      
      addConfigs.forEach(cfg => {
        const btn = document.getElementById(cfg.btnId);
        const input = document.getElementById(cfg.inputId);
        const list = document.getElementById(cfg.listId);
        btn?.addEventListener('click', () => {
          const val = (input?.value || '').trim();
          if(!val) return;
          const row = document.createElement('div');
          row.className = 'skill-row';
          row.innerHTML = `
            <span>${escapeHtml(val)}</span>
            <div class="importance">
              <label>Importance</label>
              <select class="input importance-select">
                ${Array.from({length:10},(_,i)=>`<option value="${10-i}" ${10-i===5?'selected':''}>${10-i}</option>`).join('')}
              </select>
              <label style="margin-left:10px"><input type="checkbox" class="must-have" /> Must‑have</label>
              <button type="button" class="btn btn-ghost btn-sm edit-skill">Edit</button>
              <button type="button" class="btn btn-ghost btn-sm remove-skill">Remove</button>
            </div>`;
          list?.insertBefore(row, list.querySelector('div[style]'));
          persistSkillChange({ op: 'add', name: val });
          input.value = '';
        });
      });

      // Languages add/remove/edit
      document.querySelectorAll('[data-add-lang]').forEach(btn => {
        btn.addEventListener('click', () => {
          const type = btn.getAttribute('data-add-lang');
          const input = document.getElementById(type === 'spoken' ? 'new-spoken-lang' : 'new-programming-lang');
          const val = (input?.value || '').trim();
          if(!val) return;
          const list = document.getElementById(type === 'spoken' ? 'spoken-lang-list' : 'programming-lang-list');
          const row = document.createElement('div');
          row.className = 'lang-row';
          row.innerHTML = `<span>${escapeHtml(val)}</span><div style="display:flex;gap:8px"><button type="button" class="btn btn-ghost btn-sm edit-lang" data-type="${type}">Edit</button><button type="button" class="btn btn-ghost btn-sm remove-lang" data-type="${type}">Remove</button></div>`;
          list?.appendChild(row);
          input.value = '';
          persistLanguageChange({ op: 'add', type, value: val });
        });
      });

      document.addEventListener('click', (e) => {
        const removeBtn = e.target.closest('.remove-lang');
        if(removeBtn){
          const type = removeBtn.getAttribute('data-type');
          const row = removeBtn.closest('.lang-row');
          const name = row?.querySelector('span')?.textContent?.trim();
          row?.remove();
          persistLanguageChange({ op: 'remove', type, value: name });
          return;
        }
        const editBtn = e.target.closest('.edit-lang');
        if(editBtn){
          const type = editBtn.getAttribute('data-type');
          const row = editBtn.closest('.lang-row');
          const nameEl = row?.querySelector('span');
          const curr = nameEl?.textContent?.trim() || '';
          const next = prompt('Edit language', curr);
          if(next && next.trim() && next !== curr){
            nameEl.textContent = next.trim();
            persistLanguageChange({ op: 'rename', type, from: curr, to: next.trim() });
          }
        }
      });

      function persistSkillChange(payload){ if(cvId) payload.cvId = cvId; if(profileId) payload.profileId = profileId; console.debug('skill change', payload); }
      function persistLanguageChange(payload){ if(cvId) payload.cvId = cvId; if(profileId) payload.profileId = profileId; console.debug('language change', payload); }
      function escapeHtml(str){ return str.replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }
    })();
  })();
})();

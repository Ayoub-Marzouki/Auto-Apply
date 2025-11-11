// Phase 4: LinkedIn filters persistence
(function(){
  document.addEventListener('DOMContentLoaded',()=>{
    const form = document.getElementById('liFiltersForm');
    if(!form) return;
    const finishBtn = document.getElementById('finishBtn');

    const url = new URL(window.location.href);
    const cvId = url.searchParams.get('cvId');
    const profileIdFromUrl = url.searchParams.get('profileId');

    async function persist(action){
      const fd = new FormData(form);
      const profileId = fd.get('profileId') || profileIdFromUrl;
      if(!profileId){ toast('Missing profile id', true); return; }
      const payload = collectFilters(fd);
      try{
        const res = await fetch(`/api/v1/profiles/${profileId}/filters`, {
          method:'PATCH', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)
        });
        if(!res.ok) throw new Error('Save failed');
        toast(action==='save' ? 'Filters saved' : 'Finished');
        if(action==='finish'){
          const target = cvId ? `/?cvId=${cvId}&profileId=${profileId}` : '/';
          setTimeout(()=>{ window.location.href = target; }, 400);
        }
      }catch(err){ toast('Error: '+err.message, true); }
    }

    form.addEventListener('submit', e => { e.preventDefault(); persist('save'); });
    finishBtn?.addEventListener('click', () => persist('finish'));

    function collectFilters(fd){
      const getList = name => Array.from(document.querySelectorAll(`input[name='${name}']:checked`)).map(i=>i.value);
      return {
        timePostedRange: fd.get('timePostedRange') || null,
        workplaceTypes: getList('workplaceType').join(',') || null,
        experienceLevels: getList('experience').join(',') || null,
        jobTypes: getList('jobType').join(',') || null,
        applyWithLinkedin: fd.get('applyWithLinkedin') ? true : null,
        hasVerifications: fd.get('hasVerifications') ? true : null,
        locationNames: (fd.get('locationNames')||'').trim() || null,
        locationIds: (fd.get('locationIds')||'').trim() || null,
        companyNames: (fd.get('companyNames')||'').trim() || null,
        companyIds: (fd.get('companyIds')||'').trim() || null,
        industryIds: (fd.get('industryIds')||'').trim() || null,
        functionCodes: getList('function').join(',') || null,
        titleIds: (fd.get('titleIds')||'').trim() || null,
        aiAssistFunctions: getList('function').includes('ai-any') || null
      };
    }

    function toast(msg, error){
      let t = document.getElementById('phase4-toast');
      if(!t){ t = document.createElement('div'); t.id='phase4-toast'; t.style.position='fixed'; t.style.bottom='20px'; t.style.right='20px'; t.style.background= error? '#b8423e':'#1d2a3d'; t.style.color='#fff'; t.style.padding='10px 16px'; t.style.borderRadius='8px'; t.style.fontSize='13px'; t.style.boxShadow='0 4px 18px rgba(0,0,0,.35)'; document.body.appendChild(t);} 
      t.textContent = msg; t.style.opacity='1'; setTimeout(()=>{ t.style.transition='opacity .4s'; t.style.opacity='0'; }, 1200);
    }
  });
})();

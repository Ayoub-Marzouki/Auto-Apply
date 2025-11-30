(function(){
  document.addEventListener('DOMContentLoaded', () => {
    const nextBtn = document.querySelector('.actions .btn-primary');
    const textArea = document.getElementById('intentText');
    const overlay = document.getElementById('phase3-overlay');

    if (nextBtn && textArea) {
      nextBtn.addEventListener('click', async (e) => {
        e.preventDefault();
        
        // Extract profileId from the button's href
        const url = new URL(nextBtn.href, window.location.origin);
        const profileId = url.searchParams.get('profileId');
        
        if (!profileId) {
          console.error("Profile ID not found in next link");
          window.location.href = nextBtn.href;
          return;
        }

        // Show loading UI
        if(overlay) overlay.style.display = 'flex';
        nextBtn.style.opacity = '0.7';
        nextBtn.style.pointerEvents = 'none';

        // Save intent
        const intentText = textArea.value;
        
        try {
          const res = await fetch(`/api/v1/profiles/${profileId}/intent`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                intentText: intentText,
                structuredIntentJson: null // Explicitly null to trigger backend AI analysis
            })
          });

          if (!res.ok) {
            console.error("Failed to save intent", await res.text());
            // On error, hide overlay so they can try again
            if(overlay) overlay.style.display = 'none';
            nextBtn.style.opacity = '1';
            nextBtn.style.pointerEvents = 'auto';
            alert("Error saving intent. Please try again.");
            return;
          }
        } catch (err) {
          console.error("Error saving intent", err);
          if(overlay) overlay.style.display = 'none';
          nextBtn.style.opacity = '1';
          nextBtn.style.pointerEvents = 'auto';
          alert("Connection error. Please try again.");
          return;
        }

        // Navigate on success
        window.location.href = nextBtn.href;
      });
    }
  });
})();

package ai.autoapply.business.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ai.autoapply.database.domain.ApplicationDetails;
import ai.autoapply.database.domain.ParsedCV;
import ai.autoapply.database.service.ApplicationDetailsService;

@RestController
@RequestMapping("/api/v1/wizard")
public class PhaseWizardPersistenceController {

    private final ApplicationDetailsService detailsService;

    public PhaseWizardPersistenceController(ApplicationDetailsService detailsService) {
        this.detailsService = detailsService;
    }

    @PatchMapping("/profiles/{id}/skills")
    public ResponseEntity<ApplicationDetails> patchSkills(@PathVariable Long id, @RequestBody java.util.List<ParsedCV.SkillPreference> prefs){
        return detailsService.getById(id)
            .map(p -> { p.setSkillPreferences(prefs); return ResponseEntity.ok(detailsService.update(p)); })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/profiles/{id}/languages")
    public ResponseEntity<ApplicationDetails> patchLangs(@PathVariable Long id, @RequestBody Langs req){
        return detailsService.getById(id)
            .map(p -> { p.setSpokenLanguages(req.spokenLanguages); p.setProgrammingLanguages(req.programmingLanguages); return ResponseEntity.ok(detailsService.update(p)); })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public static record Langs(java.util.List<String> spokenLanguages, java.util.List<String> programmingLanguages) {}
}

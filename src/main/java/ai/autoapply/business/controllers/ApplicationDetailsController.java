package ai.autoapply.business.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ai.autoapply.database.domain.ApplicationDetails;
import ai.autoapply.database.domain.LinkedInFilters;
import ai.autoapply.database.domain.ParsedCV;
import ai.autoapply.database.service.ApplicationDetailsService;
import ai.autoapply.database.service.ParsedCVService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/v1")
public class ApplicationDetailsController {

    private final ApplicationDetailsService detailsService;
    private final ParsedCVService parsedCVService;

    public ApplicationDetailsController(ApplicationDetailsService detailsService, ParsedCVService parsedCVService) {
        this.detailsService = detailsService;
        this.parsedCVService = parsedCVService;
    }

    @PostMapping("/profiles")
    public ResponseEntity<ApplicationDetails> createProfile(@RequestBody CreateProfileRequest req){
        // Create empty profile bound to a parsed CV
        var details = new ApplicationDetails();
        details.setParsedCvId(req.cvId);
        details.setProfileName(req.profileName);
        return ResponseEntity.ok(detailsService.create(details));
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<ApplicationDetails> getProfile(@PathVariable Long id){
        return detailsService.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/profiles/{id}/intent")
    public ResponseEntity<ApplicationDetails> saveIntent(@PathVariable Long id, @RequestBody IntentPayload req){
        var opt = detailsService.getById(id);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        d.setIntentText(req.intentText);
        d.setStructuredIntentJson(req.structuredIntentJson);
        return ResponseEntity.ok(detailsService.update(d));
    }

    @PatchMapping("/profiles/{id}/skills")
    public ResponseEntity<ApplicationDetails> saveSkills(@PathVariable Long id, @RequestBody List<ParsedCV.SkillPreference> prefs){
        var opt = detailsService.getById(id);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        d.setSkillPreferences(prefs);
        return ResponseEntity.ok(detailsService.update(d));
    }

    @PatchMapping("/profiles/{id}/languages")
    public ResponseEntity<ApplicationDetails> saveLanguages(@PathVariable Long id, @RequestBody LangPayload req){
        var opt = detailsService.getById(id);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        d.setSpokenLanguages(req.spokenLanguages);
        d.setProgrammingLanguages(req.programmingLanguages);
        return ResponseEntity.ok(detailsService.update(d));
    }

    @PatchMapping("/profiles/{id}/filters")
    public ResponseEntity<ApplicationDetails> saveFilters(@PathVariable Long id, @RequestBody LinkedInFilters filters){
        var opt = detailsService.getById(id);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        d.setLinkedInFilters(filters);
        return ResponseEntity.ok(detailsService.update(d));
    }

    @GetMapping("/profiles/{id}/bundle")
    public ResponseEntity<ProfileBundle> getBundle(@PathVariable Long id){
        var opt = detailsService.getById(id);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var profile = opt.get();
        var cv = parsedCVService.getParsedCVById(profile.getParsedCvId()).orElse(null);
        return ResponseEntity.ok(new ProfileBundle(cv, profile));
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class CreateProfileRequest { public Long cvId; public String profileName; }
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class IntentPayload { public String intentText; public String structuredIntentJson; }
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class LangPayload { public List<String> spokenLanguages; public List<String> programmingLanguages; }
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ProfileBundle { public ParsedCV cv; public ApplicationDetails profile; }
}

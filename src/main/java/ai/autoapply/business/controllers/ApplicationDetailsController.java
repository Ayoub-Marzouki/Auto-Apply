package ai.autoapply.business.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ai.autoapply.database.domain.ApplicationDetails;
import ai.autoapply.database.domain.LinkedInFilters;
import ai.autoapply.database.domain.ParsedCV;
import ai.autoapply.database.service.ApplicationDetailsService;
import ai.autoapply.database.service.ParsedCVService;
import ai.autoapply.database.repositories.ApplicationDetailsRepository;
import ai.autoapply.business.services.IntentAnalysisService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/v1")
public class ApplicationDetailsController {

    private final ApplicationDetailsService detailsService;
    private final ParsedCVService parsedCVService;
    private final ApplicationDetailsRepository repo;
    private final IntentAnalysisService intentAnalysisService;

    public ApplicationDetailsController(ApplicationDetailsService detailsService, ParsedCVService parsedCVService, ApplicationDetailsRepository repo, IntentAnalysisService intentAnalysisService) {
        this.detailsService = detailsService;
        this.parsedCVService = parsedCVService;
        this.repo = repo;
        this.intentAnalysisService = intentAnalysisService;
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
        
        // If structured intent is not provided, try to analyze it with AI
        if (req.structuredIntentJson == null || req.structuredIntentJson.isEmpty()) {
            String analysis = intentAnalysisService.analyzeIntent(req.intentText);
            d.setStructuredIntentJson(analysis);
        } else {
            d.setStructuredIntentJson(req.structuredIntentJson);
        }
        
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

    // New: bind/unbind and lookup by agent binding id
    @PostMapping("/profiles/{id}/bind")
    public ResponseEntity<ApplicationDetails> bindAgent(@PathVariable Long id, @RequestBody BindRequest req){
        var opt = detailsService.getById(id);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        d.setAgentBindingId(req.bindingId);
        return ResponseEntity.ok(detailsService.update(d));
    }

    @DeleteMapping("/profiles/{id}/bind")
    public ResponseEntity<Void> unbindAgent(@PathVariable Long id){
        var opt = detailsService.getById(id);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        d.setAgentBindingId(null);
        detailsService.update(d);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profiles/by-binding/{bindingId}")
    public ResponseEntity<ProfileBundle> getByBinding(@PathVariable String bindingId){
        var opt = repo.findByAgentBindingId(bindingId);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var profile = opt.get();
        var cv = parsedCVService.getParsedCVById(profile.getParsedCvId()).orElse(null);
        return ResponseEntity.ok(new ProfileBundle(cv, profile));
    }

    @GetMapping("/profiles/recent")
    public ResponseEntity<ProfileBundle> getMostRecent(){
        var opt = repo.findFirstByOrderByUpdatedAtDesc();
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
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class BindRequest { public String bindingId; }
}
package ai.autoapply.presentation.controllers;

import ai.autoapply.database.service.ParsedCVService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;


@Controller
@RequestMapping("/")
public class WebController {
    // Wizard phases:
    // phase1 = CV upload
    // phase2 = Review & edit parsed CV + set skill importance/must-haves
    // phase3 = Define intent / targeting preferences (free text -> structured later)
    // phase4 = LinkedIn filters

    private final ParsedCVService parsedCVService;

    public WebController(ParsedCVService parsedCVService) {
        this.parsedCVService = parsedCVService;
    }

    @GetMapping
    public String index() { return "index"; }

    @GetMapping("phase1") // Phase 1: CV upload
    public String phase1() { return "phase1"; }

    @GetMapping("phase2") // Phase 2: show parsed CV; later persist edits & skill metadata
    public String phase2(@RequestParam("cvId") Long cvId, @RequestParam(value = "profileId", required = false) Long profileId, Model model, RedirectAttributes redirectAttributes) {
        parsedCVService.getParsedCVById(cvId).ifPresent(cv -> model.addAttribute("cv", cv));
        if(profileId != null) model.addAttribute("profileId", profileId);
        // Check for duplicate flag in redirect attributes
        if (redirectAttributes.getFlashAttributes().containsKey("duplicate")) {
            model.addAttribute("duplicate", true);
        }
        return "phase2";
    }

    @GetMapping("phase3") // Phase 3: intent free text (to be LLM-structured next step)
    public String phase3(@RequestParam("cvId") Long cvId, @RequestParam(value = "profileId", required = false) Long profileId, Model model) {
        var cvOpt = parsedCVService.getParsedCVById(cvId);
        model.addAttribute("cvId", cvId);
        if(profileId != null) model.addAttribute("profileId", profileId);
        cvOpt.ifPresent(cv -> {
            String topSkills = "";
            if(cv.getTechnicalSkills() != null && !cv.getTechnicalSkills().isEmpty()) {
                topSkills = String.join(", ", cv.getTechnicalSkills().stream().limit(5).collect(Collectors.toList()));
            }
            String prefill = "I want a \"" + (cv.getExperiences()!=null && !cv.getExperiences().isEmpty() ? "role similar to my background" : "job") + "\" kind of roles, in [City1, City2]. Match at least 70% of my skills, primarily " + topSkills + ". Remote/hybrid is acceptable. Go for multilingual searches.";
            model.addAttribute("prefill", prefill);
        });
        return "phase3";
    }

    @GetMapping("phase4") // Phase 4: scoring weights form (to persist as profile later)
    public String phase4(@RequestParam("cvId") Long cvId, @RequestParam(value = "profileId", required = false) Long profileId, Model model) {
        model.addAttribute("cvId", cvId);
        if(profileId != null) model.addAttribute("profileId", profileId);
        return "phase4";
    }

    @GetMapping("guide")
    public String guide() { return "guide"; }
}

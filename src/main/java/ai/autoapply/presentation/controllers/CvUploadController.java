package ai.autoapply.presentation.controllers;

import ai.autoapply.business.services.CVParsingService;
import ai.autoapply.business.services.ProgressLogService;
import ai.autoapply.database.domain.ApplicationDetails;
import ai.autoapply.database.domain.ParsedCV;
import ai.autoapply.database.service.ApplicationDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping({"/upload-cv"})
public class CvUploadController {

    private final CVParsingService cvParsingService;
    private final ProgressLogService progressLogService;
    private final ApplicationDetailsService applicationDetailsService;

    public CvUploadController(CVParsingService cvParsingService, ProgressLogService progressLogService, ApplicationDetailsService applicationDetailsService) {
        this.cvParsingService = cvParsingService;
        this.progressLogService = progressLogService;
        this.applicationDetailsService = applicationDetailsService;
    }

    @PostMapping
    public String handleFileUpload(@RequestParam("cv") MultipartFile file,
                                   @RequestParam(value = "correlationId", required = false) String correlationId,
                                   RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/phase1";
        }

        try {
            if(correlationId == null || correlationId.isBlank()) correlationId = UUID.randomUUID().toString();
            ParsedCV parsed = cvParsingService.parseAndStoreCV(file, correlationId);

            ApplicationDetails details = new ApplicationDetails();
            details.setParsedCvId(parsed.getId());
            details.setProfileName("Profile " + parsed.getId());
            Long profileId = applicationDetailsService.create(details).getId();

            if(cvParsingService.wasReusedLastCall()){
                redirectAttributes.addFlashAttribute("duplicateCv", true);
            }
            redirectAttributes.addFlashAttribute("successMessage", "CV uploaded and parsed successfully!");
            return "redirect:/phase2?cvId=" + parsed.getId() + "&profileId=" + profileId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error parsing CV: " + e.getMessage());
        }

        return "redirect:/phase1";
    }

    // SSE endpoint for client to subscribe with correlationId
    @GetMapping("/progress/{id}")
    public SseEmitter progress(@PathVariable("id") String id){
        return progressLogService.subscribe(id);
    }
}
package ai.autoapply.presentation.controllers;

import ai.autoapply.business.services.CVParsingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/upload-cv")
public class CvUploadController {

    private final CVParsingService cvParsingService;

    public CvUploadController(CVParsingService cvParsingService) {
        this.cvParsingService = cvParsingService;
    }

    @PostMapping
    public String handleFileUpload(@RequestParam("cv") MultipartFile file,
                                   @RequestParam("track") String track,
                                   RedirectAttributes redirectAttributes) {

        // error handling
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/start";
        }

        try {
            cvParsingService.parseAndStoreCV(file, track);
            redirectAttributes.addFlashAttribute("successMessage", "CV uploaded and parsed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error parsing CV: " + e.getMessage());
        }

        return "redirect:/start";
    }
}
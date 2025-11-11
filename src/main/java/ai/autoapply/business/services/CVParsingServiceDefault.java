package ai.autoapply.business.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ai.autoapply.database.domain.ParsedCV;
import ai.autoapply.database.service.ParsedCVService;

@Service
public class CVParsingServiceDefault implements CVParsingService {
    private static final Logger logger = LoggerFactory.getLogger(CVParsingServiceDefault.class);

    private final ChatClient chatClient;
    private final ParsedCVService parsedCVService;
    private final ProgressLogService progressLogService;
    private final ThreadLocal<Boolean> reusedLastCall = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public CVParsingServiceDefault(Builder chatClientBuilder, ParsedCVService parsedCVService, ProgressLogService progressLogService) {
        this.chatClient = chatClientBuilder.build();
        this.parsedCVService = parsedCVService;
        this.progressLogService = progressLogService;
    }

    @Override
    public ParsedCV parseAndStoreCV(MultipartFile file) { return parseAndStoreCV(file, null); }

    @Override
    public boolean wasReusedLastCall() { return Boolean.TRUE.equals(reusedLastCall.get()); }

    public ParsedCV parseAndStoreCV(MultipartFile file, String correlationId) {
        reusedLastCall.set(false);
        // validate the file
        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            logger.warn("Invalid file uploaded. Not a PDF or empty.");
            if(correlationId != null) progressLogService.send(correlationId, "error", "Invalid file. Please upload a non-empty PDF.");
            throw new IllegalArgumentException("Invalid file. Please upload a non-empty PDF.");
        }

        if(correlationId != null) progressLogService.send(correlationId, "step", "Extracting text from PDF…");
        // AI version of apache pdfbox 
        Resource pdfResource = file.getResource();
        
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
        List<Document> cvDocuments = pdfReader.get();
        String text = cvDocuments.stream().map(Document::getText).collect(Collectors.joining("\n"));
        text = text.replaceAll("[ \\t]+", " ");
        text = text.replaceAll("(?m)^\\s+", "");
        text = text.replaceAll("(\\r?\\n){2,}", "\n");

        // Hash for dedup/versioning
        String cvHash = sha256(text);
        var existingOpt = parsedCVService.findByCvHash(cvHash);
        if(existingOpt.isPresent()){
            reusedLastCall.set(true);
            var existing = existingOpt.get();
            logger.info("Duplicate CV detected by hash; reusing existing with ID: {}", existing.getId());
            if(correlationId != null){
                progressLogService.send(correlationId, "info", "Detected existing CV. Reusing parsed data (ID: " + existing.getId() + ").");
            }
            return existing;
        }

        logger.info("--- Extracted CV Text (using Spring AI Reader) ---");
        System.out.println(text);
        logger.info("--- End of CV Text Snippet ---");
        if(correlationId != null){
            String snippet = text.length() > 800 ? text.substring(0, 800) + "…" : text;
            progressLogService.send(correlationId, "snippet", snippet);
        }

        if(correlationId != null) progressLogService.send(correlationId, "step", "Calling AI to parse CV into structured object…");
        // AI prompt for gemini
        String promptTemplate = """
            You are an expert HR recruitment parser. Analyze the following CV text.
            Extract the key information and return it *only* as a valid JSON object
            that maps to this structure :
            - fullName
            - email
            - phone
            - githubUrl
            - linkedinUrl
            - summary
            - technicalSkills (technical/hard skills like Spring Boot, REST APIs, Docker, Kubernetes, Machine Learning, etc.)
            - tools (tools/platforms like Jira, Slack, GitHub, VS Code, Postman, etc.)
            - softSkills (soft skills like Leadership, Communication, Problem Solving, Teamwork, etc.)
            - spokenLanguages (human languages only: English, French, Arabic, etc.)
            - programmingLanguages (only real programming languages e.g. Java, Python, C++, JavaScript, Go, Rust, etc.)
            - experiences (list of job summaries)
            - projects (list of project summaries)
            - education (list of education entries)

            If a field is not found, make it null or an empty list.

            The CV text is:
            ---
            {cv_text}
            ---
            """;
        Prompt prompt = new PromptTemplate(promptTemplate).create(Map.of("cv_text", text));

        ParsedCV parsedCV = chatClient.prompt(prompt).call().entity(ParsedCV.class);
        parsedCV.setRawCvText(text);
        parsedCV.setCvHash(cvHash);

        // Heuristic filter for programming languages
        if(parsedCV.getProgrammingLanguages() != null){
            var allowed = java.util.Set.of("java","python","javascript","typescript","c","c++","c#","go","rust","php","swift","kotlin","ruby","scala","perl","haskell","dart","elixir","r","bash","powershell");
            var cleaned = parsedCV.getProgrammingLanguages().stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .filter(s -> allowed.contains(s.toLowerCase()))
                .distinct()
                .toList();
            parsedCV.setProgrammingLanguages(cleaned);
        }

        if(correlationId != null) progressLogService.send(correlationId, "step", "Saving your profile…");
        ParsedCV savedCV = parsedCVService.saveParsedCV(parsedCV);

        logger.info("SUCCESS! CV parsed and saved to database with ID: {}", savedCV.getId());
        logger.info("Extracted Name: {}", savedCV.getFullName());
        if(correlationId != null) {
            progressLogService.send(correlationId, "info", "SUCCESS! Saved with ID: " + savedCV.getId());
            if(savedCV.getFullName() != null) progressLogService.send(correlationId, "info", "Extracted Name: " + savedCV.getFullName());
            progressLogService.send(correlationId, "done", savedCV.getId());
            progressLogService.complete(correlationId);
        }
        return savedCV;
    }

    private static String sha256(String s){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(byte b: hash) sb.append(String.format("%02x", b));
            return sb.toString();
        }catch(Exception e){ return null; }
    }
}
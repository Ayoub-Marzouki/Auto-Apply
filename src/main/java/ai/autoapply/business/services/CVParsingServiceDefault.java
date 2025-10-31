package ai.autoapply.business.services;

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

    public CVParsingServiceDefault(Builder chatClientBuilder, ParsedCVService parsedCVService) {
        this.chatClient = chatClientBuilder.build();
        this.parsedCVService = parsedCVService;
    }

    @Override
    public void parseAndStoreCV(MultipartFile file, String track) {
        // validate the file
        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            logger.warn("Invalid file uploaded. Not a PDF or empty.");
            return;
        }

        logger.info("New CV uploaded for track: {}", track);

        // AI version of apache pdfbox 
        Resource pdfResource = file.getResource();
        
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
        List<Document> cvDocuments = pdfReader.get();
        String text = cvDocuments.stream()
                                 .map(Document::getText)
                                 .collect(Collectors.joining("\n"));

        text = text.replaceAll("[ \\t]+", " ");
        text = text.replaceAll("(?m)^\\s+", "");
        text = text.replaceAll("(\\r?\\n){2,}", "\n");

        logger.info("--- Extracted CV Text (using Spring AI Reader) ---");
        System.out.println(text);
        logger.info("--- End of CV Text Snippet ---");

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
            - skills (a list of all skills, both hard and soft)
            - experiences (a list of job summaries)
            - projects (a list of project summaries)
            - education (a list of education entries)
            
            If a field is not found, make it null or an empty list.

            The CV text is:
            ---
            {cv_text}
            ---
            """;

        Prompt prompt = new PromptTemplate(promptTemplate).create(Map.of("cv_text", text));

        logger.info("Calling AI to parse CV into structured object...");

        // Convert from JSON object to our own domain
        ParsedCV parsedCV = chatClient.prompt(prompt)
                                    .call()
                                    .entity(ParsedCV.class);

        parsedCV.setRawCvText(text);
        
        ParsedCV savedCV = parsedCVService.saveParsedCV(parsedCV);

        logger.info("SUCCESS! CV parsed and saved to database with ID: {}", savedCV.getId());
        logger.info("Extracted Name: {}", savedCV.getFullName());
    }
}
package ai.autoapply.business.services;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class CVParsingServiceDefault implements CVParsingService {

    private static final Logger logger = LoggerFactory.getLogger(CVParsingServiceDefault.class);

    @Override
    public void parseAndStoreCV(MultipartFile file, String track) {
        // validate the file
        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            logger.warn("Invalid file uploaded. Not a PDF or empty.");
            return;
        }

        logger.info("New CV uploaded for track: {}", track);

        // PDFBox to extract text
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            logger.info("--- Extracted CV Text ---");
            System.out.println(text);
            logger.info("--- End of CV Text ---");
        } catch (IOException e) {
            logger.error("Failed to parse PDF file", e);
        }
    }
}
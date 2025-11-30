package ai.autoapply.business.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class IntentAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(IntentAnalysisService.class);
    private final ChatClient chatClient;

    public IntentAnalysisService(Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String analyzeIntent(String intentText) {
        if (intentText == null || intentText.isBlank()) {
            return "{}";
        }

        // We construct the string manually to avoid template parsing errors with JSON braces {}
        String messageText = """
            You are a career coach expert in LinkedIn job search optimization.
            Analyze the following candidate's statement of intent:
            ---
            %s
            ---

            Extract the following information into a valid JSON object:
            {
              "targetRole": "A concise, standard job title (e.g., 'Software Engineer', 'Full Stack Developer')",
              "seniority": "One of: 'intern', 'entry', 'mid', 'senior', 'lead'. Infer from context (e.g., 'student' -> 'intern'). Default to 'entry'.",
              "keywords": ["List", "of", "5-10", "optimized", "search", "keywords", "for", "LinkedIn"],
              "locations": ["List", "of", "locations", "mentioned"]
            }

            Return ONLY the JSON object. Do not include markdown formatting (```json).
            """.formatted(intentText);

        try {
            // Pass the raw string directly to Prompt, bypassing PromptTemplate
            String jsonResponse = chatClient.prompt(new Prompt(messageText)).call().content();
            
            if (jsonResponse != null) {
                jsonResponse = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            }
            return jsonResponse;
        } catch (Exception e) {
            logger.error("Failed to analyze intent with AI", e);
            return "{}";
        }
    }
}
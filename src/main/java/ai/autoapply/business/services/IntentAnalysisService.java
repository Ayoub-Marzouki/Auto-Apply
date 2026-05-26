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
            You are a career coach expert in job search optimization for the French market.
            Analyze the following candidate's statement of intent:
            ---
            %s
            ---

            Extract the following information into a valid JSON object:
            {
              "targetRole": "A concise, standard job title in English (e.g., 'Software Engineer')",
              "targetRoleFr": "The same role title in French (e.g., 'Développeur Full Stack')",
              "seniority": "One of: 'intern', 'entry', 'mid', 'senior', 'lead'. Infer from context ('student', 'stage', 'stagiaire' -> 'intern'). Default to 'entry'.",
              "keywords": ["5-10 search keywords mixing English AND French variants. For internships always include: 'stage', 'stagiaire', 'internship', 'intern'. Example: ['Stage Java', 'Java Intern', 'Stage Développeur', 'Software Engineer Intern']"],
              "locations": ["List of locations mentioned, or empty array"],
              "contractType": "One of: 'stage', 'alternance', 'cdi', 'cdd', 'freelance'. Default to 'stage' if context mentions internship/student.",
              "conventionDeStage": true
            }

            IMPORTANT RULES:
            - Always include BOTH French and English variants of role keywords.
            - If the candidate mentions 'stage', 'stagiaire', 'internship', 'intern', or is clearly a student, set seniority to 'intern' and contractType to 'stage'.
            - If 'alternance' or 'apprentissage' is mentioned, set contractType to 'alternance'.
            - Generate keywords that will work on LinkedIn, WTTJ, and Indeed France.

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
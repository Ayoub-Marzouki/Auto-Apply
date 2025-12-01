# Auto-Apply Backend (Java Spring Boot)

The "Brain" of the Auto-Apply system. This application serves as the central database, UI for profile management, and AI orchestration service.

> **System Architecture:** For a complete deep dive into how this Backend interacts with the Node Agent, the AI logic, and the data flow, please refer to **`SYSTEM_OVERVIEW - Version 1.0.md`** located in the **`auto-apply-agent`** repository.

## Features

*   **CV Parsing:** Extracts structured data (Skills, Experience, Contact) from PDF resumes using Spring AI.
*   **Profile Management (Web App):** A 4-Phase Wizard to configure your application strategy:
    *   **Phase 1:** Upload CV.
    *   **Phase 2:** Edit parsed data & set skill weights.
    *   **Phase 3:** Define Intent (Target Role, Location) - Analyzed by AI.
    *   **Phase 4:** Set precise LinkedIn Filters (Remote, Internship, etc.).
*   **AI Integration:** Uses Google Gemini via Vertex AI/Spring AI to analyze intent and structure search queries.
*   **API:** Exposes a comprehensive "Profile Bundle" API for the **Auto-Apply Agent**.

## Prerequisites

*   **Java 21** (JDK)
*   **MySQL Database** (Running on localhost:3306)
*   **Google Gemini API Key** (or Vertex AI setup)

## Configuration

Configure your `src/main/resources/application.properties` (or use environment variables):

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/autoapply?useSSL=false&serverTimezone=GMT&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password

# Gemini AI (Example for simple API key usage, adjust for Vertex AI if needed)
spring.ai.vertex.ai.gemini.project-id=your-project-id
spring.ai.vertex.ai.gemini.location=us-central1
# Or simpler OpenAI/Gemini direct integration if configured
```

## Running the Application

1.  Ensure MySQL is running and the database `autoapply` exists.
2.  Run with Maven Wrapper:

    ```bash
    ./mvnw spring-boot:run
    ```

3.  Access the Web App at: `http://localhost:1000`

## System Architecture

This backend is designed to work with the **Auto-Apply Agent** (Node.js).

1.  **User** configures profile on `localhost:1000` (Java).
2.  **Java Backend** saves CV, Preferences, and AI-generated search keywords.
3.  **Node Agent** connects to `localhost:1000/api/v1/profiles/.../bundle`.
4.  **Node Agent** executes the job search and application process on LinkedIn.

## API Endpoints

*   `GET /api/v1/cv-data`: Raw CV data.
*   `GET /api/v1/profiles/{id}/bundle`: Full configuration bundle for the Agent.
*   `PATCH /api/v1/profiles/{id}/intent`: Triggers AI analysis of user intent.
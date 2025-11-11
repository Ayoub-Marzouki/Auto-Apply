package ai.autoapply.database.domain;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "application_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Association by id to ParsedCV to avoid bidirectional serialization loops.
    private Long parsedCvId;

    // Free-text intent + LLM structured intent JSON (optional)
    @Lob
    private String intentText;

    @Lob
    private String structuredIntentJson;

    // Skill preferences captured in Phase 2 (duplicates names from ParsedCV.skills but with user weights)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_skill_preferences", joinColumns = @JoinColumn(name = "profile_id"))
    private List<ParsedCV.SkillPreference> skillPreferences;

    // User overrides for languages (optional)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_languages_spoken", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "language")
    private List<String> spokenLanguages;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_languages_programming", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "language")
    private List<String> programmingLanguages;

    // LinkedIn filters captured in Phase 4
    @Embedded
    private LinkedInFilters linkedInFilters;

    // Meta
    private String profileName; // optional label (e.g., "Backend Java FR")
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Provision for agent linkage
    private String agentBindingId; // optional token linking this profile to a local agent instance
}

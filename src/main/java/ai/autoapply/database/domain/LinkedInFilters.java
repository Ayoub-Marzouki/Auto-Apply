package ai.autoapply.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkedInFilters {
    // Single-value
    private String timePostedRange; // e.g., r604800

    // Multi-values as comma-separated lists to keep schema simple
    @Column(length = 2000)
    private String workplaceTypes;   // e.g., "1,2"
    @Column(length = 2000)
    private String experienceLevels; // e.g., "1,2,3"
    @Column(length = 2000)
    private String jobTypes;         // e.g., "F,I"

    private Boolean applyWithLinkedin; // Easy Apply
    private Boolean hasVerifications;

    @Column(length = 2000)
    private String locationNames;    // e.g., "Paris,Lyon,Remote"
    @Column(length = 2000)
    private String locationIds;      // e.g., "103693101,106672002"

    @Column(length = 2000)
    private String companyNames;
    @Column(length = 2000)
    private String companyIds;

    @Column(length = 2000)
    private String industryIds;

    @Column(length = 2000)
    private String functionCodes;    // e.g., "it,eng,ai-any,other"

    @Column(length = 2000)
    private String titleIds;

    // Helper to let AI expand selections
    private Boolean aiAssistFunctions; // true if "Let AI decide" is checked
}

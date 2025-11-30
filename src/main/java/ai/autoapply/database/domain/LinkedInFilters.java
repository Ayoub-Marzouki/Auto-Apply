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
    private String timePostedRange; 

    // Multi-values as comma-separated lists
    @Column(columnDefinition = "TEXT")
    private String workplaceTypes;   
    @Column(columnDefinition = "TEXT")
    private String experienceLevels; 
    @Column(columnDefinition = "TEXT")
    private String jobTypes;         

    private Boolean applyWithLinkedin; 
    private Boolean hasVerifications;

    @Column(columnDefinition = "TEXT")
    private String locationNames;
    @Column(columnDefinition = "TEXT")
    private String locationIds;      

    @Column(columnDefinition = "TEXT")
    private String companyNames;
    @Column(columnDefinition = "TEXT")
    private String companyIds;

    @Column(columnDefinition = "TEXT")
    private String industryIds;

    @Column(columnDefinition = "TEXT")
    private String functionCodes;   

    @Column(columnDefinition = "TEXT")
    private String titleIds;

    private Boolean aiAssistFunctions; // true if "Let AI decide" is checked
}

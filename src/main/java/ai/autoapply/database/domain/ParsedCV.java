package ai.autoapply.database.domain;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedCV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName; 
    private String email;
    private String phone;
    private String githubUrl;
    private String linkedinUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_skills", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "skill")
    private List<String> skills; 

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_experiences", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "experience_summary", columnDefinition = "TEXT")
    private List<String> experiences; 

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_projects", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "project_summary", columnDefinition = "TEXT")
    private List<String> projects;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_education", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "education_entry")
    private List<String> education;


    // For potential failure of AI to parse data; we could simply run a second prompt that'll use this column
    @Column(columnDefinition = "TEXT")
    private String rawCvText;
}

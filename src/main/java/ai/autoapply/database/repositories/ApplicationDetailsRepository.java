package ai.autoapply.database.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.autoapply.database.domain.ApplicationDetails;

public interface ApplicationDetailsRepository extends JpaRepository<ApplicationDetails, Long> {
    List<ApplicationDetails> findByParsedCvId(Long parsedCvId);
}

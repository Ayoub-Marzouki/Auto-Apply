package ai.autoapply.database.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.autoapply.database.domain.ApplicationDetails;

public interface ApplicationDetailsRepository extends JpaRepository<ApplicationDetails, Long> {
    List<ApplicationDetails> findByParsedCvId(Long parsedCvId);
    // New methods for agent binding
    Optional<ApplicationDetails> findByAgentBindingId(String agentBindingId);
    // Fetch most recently updated profile (single)
    Optional<ApplicationDetails> findFirstByOrderByUpdatedAtDesc();
}

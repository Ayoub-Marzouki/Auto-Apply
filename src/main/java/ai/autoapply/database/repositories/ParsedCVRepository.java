package ai.autoapply.database.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.autoapply.database.domain.ParsedCV;

public interface ParsedCVRepository extends JpaRepository<ParsedCV, Long> {
    Optional<ParsedCV> findByCvHash(String cvHash);
}

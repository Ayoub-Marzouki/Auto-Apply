package ai.autoapply.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.autoapply.database.domain.ParsedCV;

public interface ParsedCVRepository extends JpaRepository<ParsedCV, Long> {

}

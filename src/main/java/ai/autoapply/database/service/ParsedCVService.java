package ai.autoapply.database.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ai.autoapply.database.domain.ParsedCV;
import ai.autoapply.database.repositories.ParsedCVRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ParsedCVService {
    private final ParsedCVRepository parsedCVRepository;

    public ParsedCVService(ParsedCVRepository parsedCVRepository) {
        this.parsedCVRepository = parsedCVRepository;
    }

    public ParsedCV saveParsedCV(ParsedCV parsedCV) {
        return parsedCVRepository.save(parsedCV);
    }

    public Optional<ParsedCV> getParsedCVById(Long id) {
        return parsedCVRepository.findById(id);
    }

    public List<ParsedCV> getAllParsedCVs() {
        return parsedCVRepository.findAll();
    }

    public void deleteById(Long id) {
        parsedCVRepository.deleteById(id);
    }
}

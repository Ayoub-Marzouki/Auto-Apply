package ai.autoapply.database.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.autoapply.database.domain.ApplicationDetails;
import ai.autoapply.database.repositories.ApplicationDetailsRepository;

@Service
@Transactional
public class ApplicationDetailsService {
    private final ApplicationDetailsRepository repository;

    public ApplicationDetailsService(ApplicationDetailsRepository repository) {
        this.repository = repository;
    }

    public ApplicationDetails create(ApplicationDetails details){
        var now = OffsetDateTime.now();
        details.setCreatedAt(now);
        details.setUpdatedAt(now);
        return repository.save(details);
    }

    public ApplicationDetails update(ApplicationDetails details){
        details.setUpdatedAt(OffsetDateTime.now());
        return repository.save(details);
    }

    public Optional<ApplicationDetails> getById(Long id){ return repository.findById(id); }

    public List<ApplicationDetails> listByParsedCv(Long parsedCvId){ return repository.findByParsedCvId(parsedCvId); }
}

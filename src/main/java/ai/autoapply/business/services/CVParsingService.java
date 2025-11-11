package ai.autoapply.business.services;

import org.springframework.web.multipart.MultipartFile;
import ai.autoapply.database.domain.ParsedCV;

public interface CVParsingService {
    ParsedCV parseAndStoreCV(MultipartFile file);

    // Overload that emits progress under the given correlation id
    default ParsedCV parseAndStoreCV(MultipartFile file, String correlationId) {
        // By default, delegate to the basic method for implementations that don't override
        return parseAndStoreCV(file);
    }

    default boolean wasReusedLastCall() {
        return false;
    }
}

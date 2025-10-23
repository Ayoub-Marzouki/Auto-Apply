package ai.autoapply.business.services;

import org.springframework.web.multipart.MultipartFile;

public interface CVParsingService {
    void parseAndStoreCV(MultipartFile file, String track);
}

package ai.autoapply.business.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ProgressLogService {
    private static final Logger logger = LoggerFactory.getLogger(ProgressLogService.class);
    private static final long TIMEOUT = 0L; // never time out (client will close)

    private final Map<String, List<SseEmitter>> streams = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String id) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        streams.computeIfAbsent(id, k -> new ArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(id, emitter));
        emitter.onTimeout(() -> remove(id, emitter));
        emitter.onError(ex -> remove(id, emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ignored) {}
        return emitter;
    }

    public void send(String id, String event, Object data) {
        List<SseEmitter> emitters = streams.get(id);
        if (emitters == null) return;
        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (IOException ex) {
                remove(id, emitter);
                logger.debug("Removed broken emitter for {}", id);
            }
        }
    }

    public void complete(String id) {
        List<SseEmitter> emitters = streams.remove(id);
        if (emitters == null) return;
        for (SseEmitter emitter : emitters) {
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    private void remove(String id, SseEmitter emitter){
        List<SseEmitter> list = streams.get(id);
        if(list != null){
            list.remove(emitter);
            if(list.isEmpty()) streams.remove(id);
        }
    }
}

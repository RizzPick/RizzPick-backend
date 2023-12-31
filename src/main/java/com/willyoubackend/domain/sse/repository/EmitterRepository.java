package com.willyoubackend.domain.sse.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EmitterRepository {

    // sse 연결 저장하는 맵
    public final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    // 연결된 사용자에게 보낼 데이터 맵
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    public SseEmitter save(String id, SseEmitter sseEmitter) {
        emitters.put(id, sseEmitter);
        sseEmitter.onCompletion(()->this.deleteById(id));
        sseEmitter.onTimeout(()->this.deleteById(id));
        return sseEmitter;
    }

    public void saveEventCache(String id, Object event) {
        eventCache.put(id, event);
    }

    public Map<String, SseEmitter> findAllStartWithById(String id) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(id))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Object> findAllEventCacheStartWithId(String id) {
        return eventCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(id))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void deleteAllStartWithId(String id) {
        Set<String> keysToDelete = emitters.keySet().stream()
                .filter(key -> key.startsWith(id))
                .collect(Collectors.toSet());

        keysToDelete.forEach(emitters::remove);

//        emitters.forEach(
//                (key, emitter) -> {
//                    if (key.startsWith(id)) {
//                        emitters.remove(key);
//                    }
//                }
//        );
    }

    public Optional<SseEmitter> findByUserId(Long userId) {
        String userIdPrefix = userId + "_";
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userIdPrefix))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public void deleteById(String id) {
        emitters.remove(id);
        deleteAllEventCacheStartWithId(id);
    }

    public void deleteAllByUserId(Long userId) {
        String userIdPrefix = userId + "_";
        Set<String> keysToDelete = emitters.keySet().stream()
                .filter(key -> key.startsWith(userIdPrefix))
                .collect(Collectors.toSet());

        keysToDelete.forEach(emitters::remove);
    }

    public void deleteAllEventCacheStartWithId(String id) {
        Set<String> keysToDelete = eventCache.keySet().stream()
                .filter(key -> key.startsWith(id))
                .collect(Collectors.toSet());

        keysToDelete.forEach(eventCache::remove);

//        eventCache.forEach(
//                (key, data) -> {
//                    if (key.startsWith(id)) {
//                        eventCache.remove(key);
//                    }
//                }
//        );
    }
}

package org.ybigta.highlighter.stream.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.ybigta.highlighter.stream.dto.response.ListStreamsResponse;
import org.ybigta.highlighter.topic.TopicConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ConcurrentHashMap<String, Process> workerProcesses = new ConcurrentHashMap<>();

    private static final String VIDEO_PREFIX = "video-";
    private static final String CHAT_PREFIX = "chat-";

    public boolean startStream(String url) {
        final String videoId = parseVideoId(url);
        final String videoKey = VIDEO_PREFIX + videoId;
        final String chatKey = CHAT_PREFIX + videoId;

        if (workerProcesses.containsKey(videoKey) || workerProcesses.containsKey(chatKey)) {
            return false;
        }

        try {
            // Video producer worker
            ProcessBuilder videoBuilder = new ProcessBuilder("streamlink", url, "best", "-O");
            Process videoWorker = videoBuilder.start();
            workerProcesses.put(videoKey, videoWorker);

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(videoWorker.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        kafkaTemplate.send(TopicConfig.VIDEO_TOPIC, videoKey, line.getBytes());
                    }
                } catch (Exception e) {
                    log.error("Error reading buffer: {}", e.getMessage());
                } finally {
                    workerProcesses.remove(videoKey);
                }
            }).start();

            // Chat producer worker
            // TODO: Run Python worker
            ProcessBuilder chatBuilder = new ProcessBuilder("echo", url);
            Process chatWorker = chatBuilder.start();
            workerProcesses.put(chatKey, chatWorker);

            return true;
        } catch (Exception e) {
            log.error("Error creating process: {}", e.getMessage());
            return false;
        }
    }

    public boolean stopStream(String url) {
        final String videoId = parseVideoId(url);
        final String videoKey = VIDEO_PREFIX + videoId;
        final String chatKey = CHAT_PREFIX + videoId;

        boolean isVideoDestroyed = Optional.ofNullable(workerProcesses.remove(videoKey))
                .map(process -> {
                    process.destroy();
                    return true;
                })
                .orElse(false);
        boolean isChatDestroyed = Optional.ofNullable(workerProcesses.remove(chatKey))
                .map(process -> {
                    process.destroy();
                    return true;
                })
                .orElse(false);

        return isVideoDestroyed && isChatDestroyed;
    }

    public ListStreamsResponse getAllStreams() {
        Set<String> uniqueIds = workerProcesses.keySet().stream()
                .map((key) -> key.replaceFirst("^(?:" + VIDEO_PREFIX + "|" + CHAT_PREFIX + ")", ""))
                .collect(Collectors.toSet());

        return new ListStreamsResponse(
                uniqueIds.stream()
                        .map((id) -> {
                            boolean isVideoHealthy = isProcessHealthy(VIDEO_PREFIX + id);
                            boolean isChatHealthy = isProcessHealthy(CHAT_PREFIX + id);
                            return Map.of(id, isVideoHealthy && isChatHealthy);
                        })
                        .toList()
        );
    }

    private static String parseVideoId(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            String[] params = query.split("&");

            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue[0].equals("v") && keyValue.length > 1) {
                    return keyValue[1];
                }
            }
        } catch (URISyntaxException e) {
            log.warn("Invalid URL: {}", url);
        }
        return null;
    }

    private boolean isProcessHealthy(String key) {
        Process process = workerProcesses.get(key);
        return process != null && process.isAlive();
    }
}

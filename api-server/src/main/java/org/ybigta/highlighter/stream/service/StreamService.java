package org.ybigta.highlighter.stream.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ConcurrentHashMap<String, Process> workerProcesses = new ConcurrentHashMap<>();
    private static final String KAFKA_TOPIC = "livestream";

    public boolean startStream(String url) {
        if (workerProcesses.containsKey(url)) {
            return false;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("streamlink", url, "best", "-O");
            Process process = pb.start();

            workerProcesses.put(url, process);

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        kafkaTemplate.send(KAFKA_TOPIC, line.getBytes());
                    }
                } catch (Exception e) {
                    log.error("Error reading buffer: {}", e.getMessage());
                } finally {
                    workerProcesses.remove(url);
                }
            }).start();

            return true;
        } catch (Exception e) {
            log.error("Error creating process: {}", e.getMessage());
            return false;
        }
    }

    public boolean stopStream(String url) {
        Process process = workerProcesses.remove(url);
        if (process != null) {
            process.destroy();
            return true;
        }
        return false;
    }
}

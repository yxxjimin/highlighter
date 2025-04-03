package org.ybigta.highlighter.stream.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ybigta.highlighter.stream.dto.request.StartStreamRequest;
import org.ybigta.highlighter.stream.dto.response.ListStreamsResponse;
import org.ybigta.highlighter.stream.service.StreamService;

@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @PostMapping("/start")
    public ResponseEntity<?> startStream(@RequestBody final StartStreamRequest request) {
        boolean started = streamService.startStream(request.url());
        if (started) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Worker started");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Worker already started");
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopStream(@RequestBody final StartStreamRequest request) {
        boolean stopped = streamService.stopStream(request.url());
        if (stopped) {
            return ResponseEntity.ok("Worker stopped");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such worker");
    }

    @GetMapping("/all")
    public ResponseEntity<ListStreamsResponse> getAllStreams() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(streamService.getAllStreams());
    }

    @GetMapping
    public String ping() {
        return "pong";
    }
}

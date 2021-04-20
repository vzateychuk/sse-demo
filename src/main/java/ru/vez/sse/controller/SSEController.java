package ru.vez.sse.controller;

import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.vez.sse.temperature.Temperature;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
public class SSEController {

    private final Set<SseEmitter> clients = new CopyOnWriteArraySet<>();

    @GetMapping("/temperature-stream") // (2)
    public SseEmitter events(HttpServletRequest request) { // (3)
        SseEmitter emitter = new SseEmitter(); // (4)
        clients.add(emitter); // (5)

        // Remove emitter from clients on error or disconnect
        emitter.onTimeout(() -> clients.remove(emitter)); // (6)
        emitter.onCompletion(() -> clients.remove(emitter)); // (7)
        return emitter; // (8)
    }

    @Async // (9)
    @EventListener // (10)
    public void handleMessage(Temperature temperature) { // (11)
        List<SseEmitter> deadEmitters = new ArrayList<>(); // (12)
        clients.forEach(emitter -> {
            try {
                emitter.send(temperature, MediaType.APPLICATION_JSON); // (13)
            } catch (Exception ignore) {
                deadEmitters.add(emitter); // (14)
            }
        });
        clients.removeAll(deadEmitters); // (15)
    }
}

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

    // (1) For the clients' collection, we may use the CopyOnWriteArraySet class from the java.util.concurrent package
    private final Set<SseEmitter> clients = new CopyOnWriteArraySet<>();

    @GetMapping("/temperature-stream") // (2)
    public SseEmitter events(HttpServletRequest request) { // (3) returns the SseEmitter

        SseEmitter emitter = new SseEmitter(); // (4) when a client requests that URI, we create and return the new SseEmitter instance
        clients.add(emitter); // (5) with its previous registration in the list of the active clients

        // (6) The SseEmitter removes itself from the clients' list when it has finished processing or has reached timeout
        emitter.onTimeout(() -> clients.remove(emitter)); //
        emitter.onCompletion(() -> clients.remove(emitter)); // (7)
        return emitter; // (8)
    }

    @Async // (9) The @Async annotation (9) marks a method as a candidate for the asynchronous execution, so it is invoked in the manually configured thread pool (see @ServerSideEventsDemoApplication)
    @EventListener // (10) It is decorated with the @EventListener annotation in order to receive events from Spring. This framework will invoke the handleMessage() method only when receiving Temperature events, as this type of method's argument is known as temperature
    public void handleMessage(Temperature temperature) { // (11) having a communication channel with clients means that we need to be able to receive events about temperature changes.
        List<SseEmitter> deadEmitters = new ArrayList<>(); // (12)
        clients.forEach(emitter -> {
            try {
                emitter.send(temperature, MediaType.APPLICATION_JSON); // (13) The handleMessage() method receives a new temperature event and asynchronously sends it to all clients in JSON format in parallel for each event
            } catch (Exception ignore) {
                deadEmitters.add(emitter); // (14) when sending to individual emitters, we track all failing ones
            }
        });
        // Such an approach makes it possible to spot clients that are not operational anymore.
        // Unfortunately, SseEmitter does not provide any callback for handling errors, and can be done by
        // handling errors thrown by the send() method only.
        clients.removeAll(deadEmitters); // (15) here remove failing clients from the list of the active clients
    }
}

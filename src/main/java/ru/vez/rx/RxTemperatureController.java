package ru.vez.rx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RxTemperatureController {

    private final RxTemperatureSensor sensor;

    @Autowired
    public RxTemperatureController(RxTemperatureSensor sensor) {
        this.sensor = sensor;
    }

    @GetMapping(value = "/rx/stream")
    public SseEmitter events(HttpServletRequest req) {

        RxSeeEmitter emitter = new RxSeeEmitter(); // (2)

        this.sensor
                .getTemperatureStream() // (3)
                .subscribe( emitter.getSubscriber() ); // (4)
        return emitter; // (5)
    }
}

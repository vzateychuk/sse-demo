package ru.vez.sse.temperature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class Sensor {

    @Autowired
    private final ApplicationEventPublisher publisher; // (1)
    private final Random rnd = new Random(); // (2)
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public Sensor(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct
    public void startProcessing() { // (4)
        this.executor.schedule(this::probe, 1, SECONDS);
    }

    private void probe() { // (5)
        double temperature = 16 + rnd.nextGaussian() * 10;
        Temperature newTemperature =  new Temperature(temperature);
        publisher.publishEvent(newTemperature); // schedule the next read after some random delay (0-5 seconds)
        executor.schedule(this::probe, rnd.nextInt(5_000), MILLISECONDS); //(5.1)
        System.out.println("TemperatureSensor.probe: " + newTemperature);
    }
}

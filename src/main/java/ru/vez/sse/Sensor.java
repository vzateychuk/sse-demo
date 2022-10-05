package ru.vez.sse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.vez.shared.Temperature;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class Sensor {

    @Autowired
    private final ApplicationEventPublisher publisher; // (1) The temperature sensor only depends on the ApplicationEventPublisher class, provided by Spring Framework
    private final Random rnd = new Random(); // (2)
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(); // An event generation process happens in a separate ScheduledExecutorService

    public Sensor(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct
    public void startProcessing() { // method annotated with @PostConstruct, which is called by Spring Framework when the bean is ready and triggers the whole sequence of random temperature values.
        this.executor.schedule(this::probe, 1, SECONDS);
    }

    private void probe() { // (5) An event generation process where each event's generation schedules the next round of an event's generation with a random delay
        double temperature = 16 + rnd.nextGaussian() * 10;
        Temperature newTemperature =  new Temperature(temperature);
        publisher.publishEvent(newTemperature);
        executor.schedule(this::probe, rnd.nextInt(5_000), MILLISECONDS); //(5.1) schedule the next read after some random delay (0-5 seconds)
        System.out.println("Sensor.probe: " + newTemperature);
    }
}

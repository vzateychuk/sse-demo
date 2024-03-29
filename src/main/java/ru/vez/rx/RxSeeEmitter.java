package ru.vez.rx;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.vez.shared.Temperature;
import rx.Subscriber;

import java.io.IOException;

// Имея TemperatureSensor, представляющий поток значений температуры, можем
// подписать каждого нового получателя SseEmitter на поток данных Observable
// и посылать сигналы onNext клиентам SSE. Для обработки ошибок и правильного
// закрытия HTTP-соединения напишем следующее расширение SseEmitter
public class RxSeeEmitter extends SseEmitter {

  static final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;
  private final Subscriber<Temperature> subscriber; // (1)

  public RxSeeEmitter() {
    super(SSE_SESSION_TIMEOUT); // (2)

    this.subscriber =
        new Subscriber<Temperature>() { // (3)
          @Override
          public void onCompleted() {
            System.out.println("subscriber.onCompleted");
          }

          @Override
          public void onError(Throwable e) {
            System.out.println("subscriber.onError");
          }

          @Override
          public void onNext(Temperature temperature) {
              System.out.println("subscriber.onNext: " + temperature);
            try {
              RxSeeEmitter.this.send(temperature); // (4)
            } catch (IOException e) {
              unsubscribe(); // (5)
            }
          }
        };

    this.onCompletion(this.subscriber::unsubscribe); // (8)
    this.onTimeout(this.subscriber::unsubscribe); // (9)
  }

    public Subscriber<Temperature> getSubscriber() { // (10)
        return this.subscriber;
    }
}

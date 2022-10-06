package ru.vez.rx;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.vez.shared.Temperature;
import rx.Subscriber;

import java.io.IOException;
import java.util.List;

// Имея TemperatureSensor, представляющий поток значений температуры, можем
// подписать каждого нового получателя SseEmitter на поток данных Observable
// и посылать сигналы onNext клиентам SSE. Для обработки ошибок и правильного
// закрытия HTTP-соединения напишем следующее расширение SseEmitter
public class RxSeeEmitter extends SseEmitter {

  static final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;
  private final Subscriber<List<Temperature>> subscriber; // (1)

  public RxSeeEmitter() {
    super(SSE_SESSION_TIMEOUT); // (2)

    this.subscriber =
        new Subscriber<List<Temperature>>() { // (3)
          @Override
          public void onCompleted() {
            System.out.println("subscriber.onCompleted");
          }

          @Override
          public void onError(Throwable e) {
            System.out.println("subscriber.onError");
          }

          @Override
          public void onNext(List<Temperature> temps) {
              System.out.println("subscriber.onNext: " + temps.size());
            try {
              RxSeeEmitter.this.send(temps); // (4)
            } catch (IOException e) {
              unsubscribe(); // (5)
            }
          }
        };

    this.onCompletion(this.subscriber::unsubscribe); // (8)
    this.onTimeout(this.subscriber::unsubscribe); // (9)
  }

    public Subscriber<List<Temperature>> getSubscriber() { // (10)
        return this.subscriber;
    }
}

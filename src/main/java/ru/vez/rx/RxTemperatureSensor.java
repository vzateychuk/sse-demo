package ru.vez.rx;

import org.springframework.stereotype.Component;
import ru.vez.shared.Temperature;
import rx.Observable;
import rx.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Component
public class RxTemperatureSensor {

    private final Random rnd = new Random();

    private final List<Temperature> accum;

    // Метод concatMap() принимает функцию f и  преобразует элемент tick в  элементы потока
  // Observable, применяя функцию f к каждому
  // элементу входного потока и объединяя их друг за другом в выходной поток. В нашем случае
  // функция f генерирует замеры температуры после случайной задержки (чтобы повторить поведение
  // предыдущей реализации).
  // Чтобы задействовать датчик, создается новый поток с единственным элементом tick (6).
  // Для имитации случайной задержки использована функция delay(rnd. nextInt(5000), MILLISECONDS) .
  // В итоге после вызова concatMap(tick -> ...) получаем поток данных температуры, который возвращает значения из датчика
  // через случайные промежутки от одной до пяти секунд.
  private final Observable<Temperature> dataStream =
      Observable.range(0, Integer.MAX_VALUE)
          .concatMap(
              tick ->
                  Observable.just(tick) // (6)
                      .delay(rnd.nextInt(5_000), TimeUnit.MILLISECONDS) // (7)
                      .map(val -> this.generateT()))
          .publish()    // (9)
          .refCount();  // (10)

    public RxTemperatureSensor() {
        this.accum = new CopyOnWriteArrayList<>();

    Subscription sub =
        Observable.interval(100, TimeUnit.MILLISECONDS)
            .map(tick -> this.generateT())
            .subscribe(
                t -> {
                  this.accum.add(t);
                  System.out.println("Produced: " + t);
                });
    }

    // На самом деле можно было бы вернуть поток без применения операторов (9) и (10),
  // но в данном случае каждый подписчик (клиент SSE) мог бы инициировать новую подписку и
  // создать новую последовательность операций чтения датчика. То есть замеры температуры не будут использоваться
  // совместно всеми подписчиками, что может вызвать перегрузку оборудования и ухудшение его работы.
  // Для предотвращения этой неприятности используем оператор publish() (9),
  // который рассылает события из одного исходного потока во все потоки, связанные с подписчиками.
  // Оператор publish() возвращает особую разновидность Observable  – ConnectableObservable.
  // Она поддерживает оператор refCount() (10), который создает подписку на общий входящий поток, только когда имеется
  // хотя бы одна исходящая подписка. В отличие от типичной реализации шаблона «Издатель/Подписчик»,
  // этот прием позволяет не обращаться к датчику в отсутствие клиентов.
  public Observable<Temperature> getTemperatureStream() {
        return this.dataStream;
    }

  private Temperature generateT() {
        double val = 16 + rnd.nextGaussian() * 10;
        Temperature temperature =  new Temperature(val);
        // System.out.println("RxSensor.probe: " + temperature);
        return temperature;
    }

    public Observable<List<Temperature>> getTemperatureList() {
        return Observable.range(0, Integer.MAX_VALUE)
                .concatMap(
                        tick ->
                                Observable.just(tick) // (6)
                                        .delay(rnd.nextInt(5_000), TimeUnit.MILLISECONDS) // (7)
                                        .map(val -> this.copyAndClear(accum)))
                .publish()    // (9)
                .refCount();  // (10)
    }

    private List<Temperature> copyAndClear(List<Temperature> temperatures) {
        List<Temperature> result = new ArrayList<>(temperatures);
        temperatures.clear();
        return result;
    }

}

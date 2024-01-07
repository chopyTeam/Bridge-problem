package pl.batormazur.multithreadingtest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import pl.batormazur.multithreadingtest.entity.Car;
import pl.batormazur.multithreadingtest.entity.Source;
import pl.batormazur.multithreadingtest.entity.State;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BridgeService {
    @Getter
    private int maxCarsAmount = 2;
    @Getter
    private Source currentDrivingSource;
    @Getter
    private final Queue<Car> cars = new ConcurrentLinkedQueue<>();
    private final Queue<Car> northQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Car> southQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    public void addToQueue(Car car) {
        cars.add(car);
        if (car.getSource() == Source.NORTH) {
            northQueue.add(car);
        } else {
            southQueue.add(car);
        }
        executorService.execute(()->processQueue(northQueue));
        executorService.execute(()->processQueue(southQueue));
    }
    public void deleteProcessed() {
        var toRemove = cars.stream()
                .filter(c -> c.getState().equals(State.PROCESSED))
                .filter(c -> System.currentTimeMillis() - c.getProcessedTimeStamp() > 10000)
                .toList();
        if (!toRemove.isEmpty())
            cars.removeAll(toRemove);
    }
    public void setMaxCarsAmount(int maxCarsAmount) {
        if(maxCarsAmount > 0){
            this.maxCarsAmount = maxCarsAmount;
        }
    }
    private synchronized void processQueue(Queue<Car> queue) {
        System.out.println(maxCarsAmount);
        for (int i = 0; i < maxCarsAmount && !queue.isEmpty(); i++) {
            var currentCar = queue.remove();
            if (currentCar.getState() == State.PROCESSED) continue;
            crossBridge(currentCar);
            //notifyAll();
        }
    }
    private synchronized void crossBridge(Car currentCar) {
        currentCar.setState(State.PROCESSING);
        currentDrivingSource = currentCar.getSource();
        try {
            Thread.sleep(currentCar.getProcessingTime());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        currentCar.setState(State.PROCESSED);
        currentCar.setProcessedTimeStamp(System.currentTimeMillis());
    }
}
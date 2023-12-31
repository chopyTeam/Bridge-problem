package pl.batormazur.multithreadingtest;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import pl.batormazur.multithreadingtest.entity.Car;
import pl.batormazur.multithreadingtest.entity.CarAddRequest;
import pl.batormazur.multithreadingtest.entity.CarSingleResponse;
import pl.batormazur.multithreadingtest.entity.Source;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
@AllArgsConstructor
public class BridgeController {
    private final BridgeService bridgeService;

    @GetMapping("/cars")
    public ResponseEntity<List<CarSingleResponse>> getCars() {
        return ResponseEntity.ok(bridgeService.getCars().stream().map(CarSingleResponse::map).toList());
    }

    @PostMapping("/add-car")
    public void addCar(@RequestBody CarAddRequest carRequest) {
        var car = new Car(carRequest.getName(), carRequest.getSource(), carRequest.getProcessingTime());
        bridgeService.addToQueue(car);
    }

    @GetMapping("/direction")
    public ResponseEntity<Source> getCurrentDrivingDirection() {
        return ResponseEntity.ok(bridgeService.getCurrentDrivingSource());
    }

    @GetMapping("/max-cars")
    public ResponseEntity<Integer> getMaxCarsAmount() {
        return ResponseEntity.ok(bridgeService.getMaxCarsAmount());
    }

    @PostMapping("/max-cars")
    public void setMaxCars(@RequestBody int maxCarsAmount) {
        bridgeService.setMaxCarsAmount(maxCarsAmount);
    }

    @Scheduled(fixedDelay = 100)
    public void deleteProcessed() {
        bridgeService.deleteProcessed();
        bridgeService.runThreads();
    }
}

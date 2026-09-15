package fr.campus.grog.SG.controller;

import fr.campus.grog.SG.service.HeartbeatSensor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeartbeatController {

    private final HeartbeatSensor heartbeatSensor;

    public HeartbeatController(HeartbeatSensor heartbeatSensor) {
        this.heartbeatSensor = heartbeatSensor;
    }

    @GetMapping("/heartbeat")
    public int getHeartbeat() {
        return this.heartbeatSensor.get();
    }
}

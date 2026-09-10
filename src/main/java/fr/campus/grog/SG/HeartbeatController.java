package fr.campus.grog.SG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeartbeatController {

    @Autowired
    private HeartbeatSensor heartbeatSensor;

    //public HeartbeatController(HeartbeatSensor heartbeatSensor){
    public HeartbeatController(){
        //this.heartbeatSensor = heartbeatSensor;
    }

    @GetMapping("/heartbeat")
    public int getHeartbeat(){
        return heartbeatSensor.get();
    }
}

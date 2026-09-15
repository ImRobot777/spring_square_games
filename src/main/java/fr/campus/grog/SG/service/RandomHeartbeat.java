package fr.campus.grog.SG.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomHeartbeat implements HeartbeatSensor {

    @Override
    public int get() {
        return new Random().nextInt(40, 231);
    }
}

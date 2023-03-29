package com.thehecklers.scfafplanefinder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class PlanefinderController {
    private final PlanefinderService pfService;

    public PlanefinderController(PlanefinderService pfService) {
        this.pfService = pfService;
    }

    @GetMapping("/aircraft")
    public Iterable<AircraftPosition> getCurrentAircraft() throws IOException {
        return pfService.getAircraft();
    }
}

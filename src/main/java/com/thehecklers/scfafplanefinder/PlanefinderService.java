package com.thehecklers.scfafplanefinder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class PlanefinderService {
    private final URL acURL;
    private final ObjectMapper om;
    private final WebClient acClient;
    private final WebClient posClient;

    @SneakyThrows
    public PlanefinderService(WebClient.Builder builder,
                              @Value("${acpositionfeed.url:http://192.168.1.139/ajax/aircraft}") String acposFeedUrl,
                              @Value("${aircraft.url:http://localhost:7071/api/ac}") String acDestUrl,
                              @Value("${position.url:http://localhost:7072/api/pos}") String posDestUrl) {

        acClient = builder.baseUrl(acDestUrl).build();
        posClient = builder.baseUrl(posDestUrl).build();

        acURL = new URL(acposFeedUrl);
        om = new ObjectMapper();
    }

    public Iterable<AircraftPosition> getAircraft() {
        List<AircraftPosition> positions = new ArrayList<>();

        JsonNode aircraftNodes;
        try {
            aircraftNodes = om.readTree(acURL);
            Iterator<Map.Entry<String, JsonNode>> nodes = aircraftNodes.get("aircraft").fields();
            AircraftPosition aircraftPosition;

            while (nodes.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodes.next();
                aircraftPosition = om.treeToValue(entry.getValue(), AircraftPosition.class);
                aircraftPosition.setAdshex(entry.getKey());
                positions.add(aircraftPosition);

                acClient.post()
                        .bodyValue(new Aircraft(aircraftPosition.getAdshex(),
                                aircraftPosition.getReg(),
                                aircraftPosition.getType()))
                        .retrieve()
                        .bodyToMono(Aircraft.class)
                        .subscribe(ac -> System.out.println(" 🛩️ Aircraft response: " + ac));
                posClient.post()
                        .bodyValue(new Position(aircraftPosition.getAdshex(),
                                aircraftPosition.getSquawk(),
                                aircraftPosition.getFlightno(),
                                aircraftPosition.getRoute(),
                                aircraftPosition.getAltitude(),
                                aircraftPosition.getHeading(),
                                aircraftPosition.getSpeed(),
                                aircraftPosition.getVertRate(),
                                aircraftPosition.getSelectedAltitude(),
                                aircraftPosition.getLat(),
                                aircraftPosition.getLon(),
                                aircraftPosition.getBarometer()))
                        .retrieve()
                        .bodyToMono(Position.class)
                        .subscribe(pos -> System.out.println(" ⌖ Position response: " + pos));
            }
        } catch (IOException e) {
            System.out.println("\n>>> IO Exception: " + e.getLocalizedMessage() +
                    ", generating and providing sample data.\n");
            return saveSamplePositions();
        }

        if (positions.size() > 0) {
            positions.forEach(System.out::println);
            return positions;
        } else {
            System.out.println("\n>>> No positions to report, generating and providing sample data.\n");
            return saveSamplePositions();
        }
    }

    private Iterable<AircraftPosition> saveSamplePositions() {
        // Spring Airlines flight 001 en route, flying STL to SFO, at 30000' currently over Kansas City
        var ac1 = new AircraftPosition("SAL001", "N12345", "SAL001", "LJ",
                30000, 280, 440,
                39.2979849, -94.71921);

        // Spring Airlines flight 002 en route, flying SFO to STL, at 40000' currently over Denver
        var ac2 = new AircraftPosition("SAL002", "N54321", "SAL002", "LJ",
                40000, 65, 440,
                39.8560963, -104.6759263);

        // Spring Airlines flight 002 en route, flying SFO to STL, at 40000' currently just past DEN
        var ac3 = new AircraftPosition("SAL002", "N54321", "SAL002", "LJ",
                40000, 65, 440,
                39.8412964, -105.0048267);

        return List.of(ac1, ac2, ac3);
    }
}

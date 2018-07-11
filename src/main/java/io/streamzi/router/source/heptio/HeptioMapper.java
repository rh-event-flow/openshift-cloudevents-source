package io.streamzi.router.source.heptio;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamzi.cloudevents.CloudEvent;
import io.streamzi.cloudevents.CloudEventBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Translate a Heptio event into a CNCF CloudEvent
 */
public class HeptioMapper {

    public static CloudEvent toCloudEvent(String heptioInput) {

        //Represent the input as a Map
        final ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> heptio = new HashMap<>();

        try {
            heptio = mapper.readValue(heptioInput, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (heptio.get("verb").equals("ADDED") || heptio.get("verb").equals("UPDATED")) {

            final Map<String, Object> event = (Map<String, Object>) heptio.get("event");
            final Map<String, Object> metadata = ((Map<String, Object>) (event.get("metadata")));

            final String ts = (String) metadata.get("creationTimestamp");


            final Instant dateInstant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(ts));
            final ZonedDateTime timestamp = ZonedDateTime.ofInstant(dateInstant, ZoneOffset.UTC);

            String eventType;

            //TODO: Is there a spec for the format?
            if (event.get("reason") == null || event.get("reason").equals("")) {
                eventType = "Unknown";
            } else {
                eventType = (String) event.get("reason");
            }

            if (eventType == null || eventType.equals("")) {
                System.err.println(heptioInput);
                return new CloudEventBuilder<String>().build();
            }

            //TODO: work out how source URI should be built from the Heptio payload
            URI source = null;
            try {
                final String selfLink = (String) metadata.get("selfLink");
                source = new URI(selfLink);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            return new CloudEventBuilder<Map<String, Object>>()
                    .data(heptio)
                    .contentType("application/json")
                    .eventTime(timestamp)
                    .eventType(eventType.toLowerCase())
                    .eventTypeVersion("1.0")
                    .eventID((String) metadata.get("uid"))
                    .cloudEventsVersion("0.1")
                    .source(source)
                    .build();

        } else {
            throw new IllegalArgumentException("Received Heptio event with type: " + heptio.get("verb"));
        }

    }
}

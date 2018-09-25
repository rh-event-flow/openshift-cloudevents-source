package io.streamzi.cloudevents.openshift;

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
 * Translate a k8s platform event into a CNCF CloudEvent
 */
public final class CloudEventsMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CloudEventsMapper() {
        // no-op
    }

    public static CloudEvent toCloudEvent(final String rawK8sEvent) {

        //Represent the input as a Map
        Map<String, Object> k8sEventMap = new HashMap<>();

        try {
            k8sEventMap = MAPPER.readValue(rawK8sEvent, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (k8sEventMap.get("verb").equals("ADDED") || k8sEventMap.get("verb").equals("UPDATED")) {

            final Map<String, Object> event = (Map<String, Object>) k8sEventMap.get("event");
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
                System.err.println(rawK8sEvent);
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
                    .data(k8sEventMap)
                    .contentType("application/json")
                    .eventTime(timestamp)
                    .eventType(eventType.toLowerCase())
                    .eventTypeVersion("1.0")
                    .eventID((String) metadata.get("uid"))
                    .cloudEventsVersion("0.1")
                    .source(source)
                    .build();

        } else {
            throw new IllegalArgumentException("Received Heptio event with type: " + k8sEventMap.get("verb"));
        }

    }
}

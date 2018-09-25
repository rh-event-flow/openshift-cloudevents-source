package io.streamzi.cloudevents.openshift;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.streamzi.cloudevents.CloudEvent;
import org.aerogear.kafka.SimpleKafkaProducer;
import org.aerogear.kafka.cdi.annotation.KafkaConfig;
import org.aerogear.kafka.cdi.annotation.Producer;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/heptio")
@KafkaConfig(bootstrapServers = "#{KAFKA_SERVICE_HOST}:#{KAFKA_SERVICE_PORT}")
public class KubernetesEventSource {

    private final static Logger logger = Logger.getLogger(KubernetesEventSource.class.getName());

    @Producer
    private SimpleKafkaProducer<String, String> myproducer;

    private static final String topic = System.getenv("KAFKA_TOPIC");

    private static ObjectMapper mapper = new ObjectMapper();

    static {

        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @POST
    @Produces("application/json")
    public Response transformPlatformEventsToCloudEvents(final String payload) {

        try {

            //We might get events messages per payload if the endpoint has been down
            //https://github.com/heptiolabs/eventrouter/pull/21
            final String[] lines = payload.split("\n");

            for (final String line : lines) {

                //This is a dirty hack to parse the format of the syslog messsage that Heptio sends
                //TODO: replace with Syslog parsing library.
                final String[] eventParts = line.split("- - - ");
                if (eventParts.length != 2) {
                    logger.severe("Unable to parse: " + payload);
                    return Response.serverError().build();
                }

                final CloudEvent ce = CloudEventsMapper.toCloudEvent(eventParts[1]);
                logger.info(ce.getEventType());
                myproducer.send(topic, ce.getEventType(), mapper.writeValueAsString(ce));
            }

            return Response.ok().build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.serverError().build();
        }
    }
}

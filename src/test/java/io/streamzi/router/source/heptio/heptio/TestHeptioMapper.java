package io.streamzi.router.source.heptio.heptio;


import io.streamzi.cloudevents.CloudEvent;
import io.streamzi.router.source.heptio.HeptioMapper;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHeptioMapper {

    @Test
    public void testMapper() throws Exception {

        String added = new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().getResource("added.json").toURI())));
        CloudEvent addedCE = HeptioMapper.toCloudEvent(added);
        assertThat(addedCE.getEventType()).isEqualTo("DeploymentCreated".toLowerCase());
        assertThat(addedCE.getEventID()).isEqualTo("aa8e9201-838c-11e8-a6b6-9aea71cf1083");

        String updated = new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().getResource("updated.json").toURI())));
        CloudEvent updatedCE = HeptioMapper.toCloudEvent(updated);
        assertThat(updatedCE.getEventType()).isEqualTo("BackOff".toLowerCase());
        assertThat(updatedCE.getSource().toString()).isEqualTo("/api/v1/namespaces/myproject/events/heptio-source-http-8-qfx6c.153ff6d5768eaafc");

//        ObjectMapper om = new ObjectMapper();
//        om.registerModule(new Jdk8Module());
//        om.registerModule(new JavaTimeModule());
//        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//        om.enable(SerializationFeature.INDENT_OUTPUT);
//
//        try {
//            om.writeValue(System.out, ce);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}

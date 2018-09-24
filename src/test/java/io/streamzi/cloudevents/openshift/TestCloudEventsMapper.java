package io.streamzi.cloudevents.openshift;


import io.streamzi.cloudevents.CloudEvent;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCloudEventsMapper {

    @Test
    public void testMapper() throws Exception {

        String added = new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().getResource("added.json").toURI())));
        CloudEvent addedCE = CloudEventsMapper.toCloudEvent(added);
        assertThat(addedCE.getEventType()).isEqualTo("DeploymentCreated".toLowerCase());
        assertThat(addedCE.getEventID()).isEqualTo("aa8e9201-838c-11e8-a6b6-9aea71cf1083");

        String updated = new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().getResource("updated.json").toURI())));
        CloudEvent updatedCE = CloudEventsMapper.toCloudEvent(updated);
        assertThat(updatedCE.getEventType()).isEqualTo("BackOff".toLowerCase());
        assertThat(updatedCE.getSource().toString()).isEqualTo("/api/v1/namespaces/myproject/events/heptio-source-http-8-qfx6c.153ff6d5768eaafc");
    }
}

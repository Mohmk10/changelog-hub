package io.github.mohmk10.changeloghub.parser.asyncapi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OperationType.
 */
class OperationTypeTest {

    @Test
    @DisplayName("Should parse publish operation from v2 name")
    void testFromV2NamePublish() {
        OperationType type = OperationType.fromV2Name("publish");
        assertEquals(OperationType.PUBLISH, type);
    }

    @Test
    @DisplayName("Should parse subscribe operation from v2 name")
    void testFromV2NameSubscribe() {
        OperationType type = OperationType.fromV2Name("subscribe");
        assertEquals(OperationType.SUBSCRIBE, type);
    }

    @Test
    @DisplayName("Should return null for unknown v2 name")
    void testFromV2NameUnknown() {
        OperationType type = OperationType.fromV2Name("unknown");
        assertNull(type);
    }

    @Test
    @DisplayName("Should return null for null v2 name")
    void testFromV2NameNull() {
        OperationType type = OperationType.fromV2Name(null);
        assertNull(type);
    }

    @Test
    @DisplayName("Should parse send action from v3 as PUBLISH")
    void testFromV3ActionSend() {
        OperationType type = OperationType.fromV3Action("send");
        assertEquals(OperationType.PUBLISH, type);
    }

    @Test
    @DisplayName("Should parse receive action from v3 as SUBSCRIBE")
    void testFromV3ActionReceive() {
        OperationType type = OperationType.fromV3Action("receive");
        assertEquals(OperationType.SUBSCRIBE, type);
    }

    @Test
    @DisplayName("Should return null for unknown v3 action")
    void testFromV3ActionUnknown() {
        OperationType type = OperationType.fromV3Action("unknown");
        assertNull(type);
    }

    @Test
    @DisplayName("Should return null for null v3 action")
    void testFromV3ActionNull() {
        OperationType type = OperationType.fromV3Action(null);
        assertNull(type);
    }

    @Test
    @DisplayName("Should map PUBLISH to POST HTTP method")
    void testPublishHttpMethod() {
        assertEquals("POST", OperationType.PUBLISH.getHttpMethod());
    }

    @Test
    @DisplayName("Should map SUBSCRIBE to GET HTTP method")
    void testSubscribeHttpMethod() {
        assertEquals("GET", OperationType.SUBSCRIBE.getHttpMethod());
    }

    @Test
    @DisplayName("Should get v2 name for publish")
    void testGetV2NamePublish() {
        assertEquals("publish", OperationType.PUBLISH.getV2Name());
    }

    @Test
    @DisplayName("Should get v2 name for subscribe")
    void testGetV2NameSubscribe() {
        assertEquals("subscribe", OperationType.SUBSCRIBE.getV2Name());
    }

    @Test
    @DisplayName("Should get v3 name for PUBLISH")
    void testGetV3NamePublish() {
        assertEquals("send", OperationType.PUBLISH.getV3Name());
    }

    @Test
    @DisplayName("Should get v3 name for SUBSCRIBE")
    void testGetV3NameSubscribe() {
        assertEquals("receive", OperationType.SUBSCRIBE.getV3Name());
    }

    @Test
    @DisplayName("Should handle case insensitivity in v2 name")
    void testCaseInsensitivityV2() {
        assertEquals(OperationType.PUBLISH, OperationType.fromV2Name("PUBLISH"));
        assertEquals(OperationType.PUBLISH, OperationType.fromV2Name("Publish"));
        assertEquals(OperationType.SUBSCRIBE, OperationType.fromV2Name("SUBSCRIBE"));
    }

    @Test
    @DisplayName("Should handle case insensitivity in v3 action")
    void testCaseInsensitivityV3() {
        assertEquals(OperationType.PUBLISH, OperationType.fromV3Action("SEND"));
        assertEquals(OperationType.PUBLISH, OperationType.fromV3Action("Send"));
        assertEquals(OperationType.SUBSCRIBE, OperationType.fromV3Action("RECEIVE"));
    }

    @Test
    @DisplayName("Should correctly identify producer")
    void testIsProducer() {
        assertTrue(OperationType.PUBLISH.isProducer());
        assertFalse(OperationType.SUBSCRIBE.isProducer());
    }

    @Test
    @DisplayName("Should correctly identify consumer")
    void testIsConsumer() {
        assertFalse(OperationType.PUBLISH.isConsumer());
        assertTrue(OperationType.SUBSCRIBE.isConsumer());
    }
}

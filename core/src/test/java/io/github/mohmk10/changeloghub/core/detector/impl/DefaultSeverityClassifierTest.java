package io.github.mohmk10.changeloghub.core.detector.impl;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSeverityClassifierTest {

    private DefaultSeverityClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new DefaultSeverityClassifier();
    }

    @Test
    void testClassifyEndpointRemoved() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .path("/api/users")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.BREAKING);
    }

    @Test
    void testClassifyParameterRemoved() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.PARAMETER)
                .path("parameter:userId")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.DANGEROUS);
    }

    @Test
    void testClassifyRequiredParameterAdded() {
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.PARAMETER)
                .description("Required parameter 'userId' added")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.BREAKING);
    }

    @Test
    void testClassifyOptionalParameterAdded() {
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.PARAMETER)
                .description("Optional parameter 'filter' added")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.INFO);
    }

    @Test
    void testClassifyDeprecation() {
        Change change = Change.builder()
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.ENDPOINT)
                .path("/api/users")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.WARNING);
    }

    @Test
    void testClassifyParameterTypeChanged() {
        Change change = Change.builder()
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.PARAMETER)
                .path("parameter:userId.type")
                .oldValue("string")
                .newValue("integer")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.BREAKING);
    }

    @Test
    void testClassifyRequiredChangedToTrue() {
        Change change = Change.builder()
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.PARAMETER)
                .path("parameter:filter.required")
                .oldValue(false)
                .newValue(true)
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.BREAKING);
    }

    @Test
    void testClassifyResponseRemoved() {
        Change change = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.RESPONSE)
                .path("response:200")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.DANGEROUS);
    }

    @Test
    void testClassifyRequestBodySchemaChanged() {
        Change change = Change.builder()
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.REQUEST_BODY)
                .path("/api/users.requestBody.schema")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.DANGEROUS);
    }

    @Test
    void testClassifyEndpointAdded() {
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .path("/api/products")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.INFO);
    }

    @Test
    void testClassifyNullChange() {
        Severity severity = classifier.classify(null);

        assertThat(severity).isEqualTo(Severity.INFO);
    }

    @Test
    void testClassifyLocationChanged() {
        Change change = Change.builder()
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.PARAMETER)
                .path("parameter:id.location")
                .build();

        Severity severity = classifier.classify(change);

        assertThat(severity).isEqualTo(Severity.BREAKING);
    }
}

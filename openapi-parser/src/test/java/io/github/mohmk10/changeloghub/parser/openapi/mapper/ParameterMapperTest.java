package io.github.mohmk10.changeloghub.parser.openapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterMapperTest {

    private ParameterMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ParameterMapper();
    }

    @Test
    void testMapPathParameter() {
        io.swagger.v3.oas.models.parameters.Parameter param = new io.swagger.v3.oas.models.parameters.PathParameter();
        param.setName("userId");
        param.setIn("path");
        param.setRequired(true);
        param.setSchema(new StringSchema());

        Parameter result = mapper.map(param);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("userId");
        assertThat(result.getLocation()).isEqualTo(ParameterLocation.PATH);
        assertThat(result.isRequired()).isTrue();
        assertThat(result.getType()).isEqualTo("string");
    }

    @Test
    void testMapQueryParameter() {
        io.swagger.v3.oas.models.parameters.Parameter param = new io.swagger.v3.oas.models.parameters.QueryParameter();
        param.setName("limit");
        param.setIn("query");
        param.setRequired(false);
        param.setSchema(new IntegerSchema());

        Parameter result = mapper.map(param);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("limit");
        assertThat(result.getLocation()).isEqualTo(ParameterLocation.QUERY);
        assertThat(result.isRequired()).isFalse();
        assertThat(result.getType()).isEqualTo("integer");
    }

    @Test
    void testMapHeaderParameter() {
        io.swagger.v3.oas.models.parameters.Parameter param = new io.swagger.v3.oas.models.parameters.HeaderParameter();
        param.setName("X-Api-Key");
        param.setIn("header");
        param.setRequired(true);
        param.setSchema(new StringSchema());

        Parameter result = mapper.map(param);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("X-Api-Key");
        assertThat(result.getLocation()).isEqualTo(ParameterLocation.HEADER);
        assertThat(result.isRequired()).isTrue();
    }

    @Test
    void testMapCookieParameter() {
        io.swagger.v3.oas.models.parameters.Parameter param = new io.swagger.v3.oas.models.parameters.CookieParameter();
        param.setName("session");
        param.setIn("cookie");
        param.setSchema(new StringSchema());

        Parameter result = mapper.map(param);

        assertThat(result).isNotNull();
        assertThat(result.getLocation()).isEqualTo(ParameterLocation.COOKIE);
    }

    @Test
    void testMapRequiredParameter() {
        io.swagger.v3.oas.models.parameters.Parameter param = new io.swagger.v3.oas.models.parameters.PathParameter();
        param.setName("id");
        param.setIn("path");
        param.setRequired(true);
        param.setSchema(new StringSchema());

        Parameter result = mapper.map(param);

        assertThat(result.isRequired()).isTrue();
    }

    @Test
    void testMapWithDefault() {
        io.swagger.v3.oas.models.parameters.Parameter param = new io.swagger.v3.oas.models.parameters.QueryParameter();
        param.setName("page");
        param.setIn("query");
        IntegerSchema schema = new IntegerSchema();
        schema.setDefault(1);
        param.setSchema(schema);

        Parameter result = mapper.map(param);

        assertThat(result).isNotNull();
        assertThat(result.getDefaultValue()).isEqualTo("1");
    }

    @Test
    void testMapWithDescription() {
        io.swagger.v3.oas.models.parameters.Parameter param = new io.swagger.v3.oas.models.parameters.QueryParameter();
        param.setName("filter");
        param.setIn("query");
        param.setDescription("Filter expression");
        param.setSchema(new StringSchema());

        Parameter result = mapper.map(param);

        assertThat(result.getDescription()).isEqualTo("Filter expression");
    }

    @Test
    void testMapNullParameter() {
        Parameter result = mapper.map(null);
        assertThat(result).isNull();
    }

    @Test
    void testMapWithRefSchema() {
        io.swagger.v3.oas.models.parameters.Parameter param = new io.swagger.v3.oas.models.parameters.QueryParameter();
        param.setName("data");
        param.setIn("query");
        Schema<?> schema = new Schema<>();
        schema.set$ref("#/components/schemas/DataModel");
        param.setSchema(schema);

        Parameter result = mapper.map(param);

        assertThat(result.getType()).isEqualTo("DataModel");
    }
}

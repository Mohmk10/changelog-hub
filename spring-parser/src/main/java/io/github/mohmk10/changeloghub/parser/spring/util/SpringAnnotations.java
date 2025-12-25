package io.github.mohmk10.changeloghub.parser.spring.util;

/**
 * Constants for Spring annotation names.
 */
public final class SpringAnnotations {

    private SpringAnnotations() {
        // Utility class
    }

    // Controller annotations
    public static final String REST_CONTROLLER = "RestController";
    public static final String CONTROLLER = "Controller";
    public static final String RESPONSE_BODY = "ResponseBody";

    // Request mapping annotations
    public static final String REQUEST_MAPPING = "RequestMapping";
    public static final String GET_MAPPING = "GetMapping";
    public static final String POST_MAPPING = "PostMapping";
    public static final String PUT_MAPPING = "PutMapping";
    public static final String DELETE_MAPPING = "DeleteMapping";
    public static final String PATCH_MAPPING = "PatchMapping";

    // Parameter annotations
    public static final String REQUEST_PARAM = "RequestParam";
    public static final String PATH_VARIABLE = "PathVariable";
    public static final String REQUEST_BODY = "RequestBody";
    public static final String REQUEST_HEADER = "RequestHeader";
    public static final String COOKIE_VALUE = "CookieValue";

    // Other annotations
    public static final String DEPRECATED = "Deprecated";
    public static final String RESPONSE_STATUS = "ResponseStatus";

    // Annotation attributes
    public static final String VALUE = "value";
    public static final String PATH = "path";
    public static final String METHOD = "method";
    public static final String NAME = "name";
    public static final String REQUIRED = "required";
    public static final String DEFAULT_VALUE = "defaultValue";
    public static final String PRODUCES = "produces";
    public static final String CONSUMES = "consumes";
    public static final String CODE = "code";

    // HTTP Methods (from RequestMethod enum)
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_PATCH = "PATCH";
    public static final String HTTP_HEAD = "HEAD";
    public static final String HTTP_OPTIONS = "OPTIONS";
}

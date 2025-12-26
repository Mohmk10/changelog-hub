package io.github.mohmk10.changeloghub.analytics.exception;

/**
 * Exception thrown when analytics operations fail.
 */
public class AnalyticsException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String context;

    public enum ErrorCode {
        INSUFFICIENT_DATA("Insufficient data for analysis"),
        CALCULATION_ERROR("Error during calculation"),
        INVALID_INPUT("Invalid input data"),
        REPORT_GENERATION_ERROR("Error generating report"),
        AGGREGATION_ERROR("Error aggregating data"),
        TREND_ANALYSIS_ERROR("Error analyzing trends"),
        PATTERN_DETECTION_ERROR("Error detecting patterns");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public AnalyticsException(String message) {
        super(message);
        this.errorCode = ErrorCode.CALCULATION_ERROR;
        this.context = null;
    }

    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.CALCULATION_ERROR;
        this.context = null;
    }

    public AnalyticsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.context = null;
    }

    public AnalyticsException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = null;
    }

    public AnalyticsException(ErrorCode errorCode, String message, String context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getContext() {
        return context;
    }

    public static AnalyticsException insufficientData(String message) {
        return new AnalyticsException(ErrorCode.INSUFFICIENT_DATA, message);
    }

    public static AnalyticsException insufficientData(int required, int actual) {
        return new AnalyticsException(ErrorCode.INSUFFICIENT_DATA,
                String.format("Insufficient data: required %d data points, but only %d available", required, actual));
    }

    public static AnalyticsException calculationError(String message, Throwable cause) {
        return new AnalyticsException(ErrorCode.CALCULATION_ERROR, message, cause);
    }

    public static AnalyticsException invalidInput(String message) {
        return new AnalyticsException(ErrorCode.INVALID_INPUT, message);
    }

    public static AnalyticsException reportGenerationError(String message, Throwable cause) {
        return new AnalyticsException(ErrorCode.REPORT_GENERATION_ERROR, message, cause);
    }
}

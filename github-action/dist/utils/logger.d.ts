/**
 * Log levels for the logger
 */
export declare enum LogLevel {
    DEBUG = 0,
    INFO = 1,
    WARN = 2,
    ERROR = 3
}
/**
 * Logger configuration
 */
export interface LoggerConfig {
    /** Minimum log level to output */
    level: LogLevel;
    /** Include timestamps in log messages */
    timestamps: boolean;
    /** Include logger name prefix */
    includePrefix: boolean;
}
/**
 * A structured logger that integrates with GitHub Actions logging.
 */
export declare class Logger {
    private name;
    private config;
    /**
     * Creates a new logger instance.
     *
     * @param name - Name of the logger (usually component name)
     * @param config - Optional configuration override
     */
    constructor(name: string, config?: Partial<LoggerConfig>);
    /**
     * Logs a debug message.
     * Debug messages are only shown when ACTIONS_STEP_DEBUG is set.
     */
    debug(message: string, ...args: unknown[]): void;
    /**
     * Logs an info message.
     */
    info(message: string, ...args: unknown[]): void;
    /**
     * Logs a warning message.
     */
    warn(message: string, ...args: unknown[]): void;
    /**
     * Logs an error message.
     */
    error(message: string, ...args: unknown[]): void;
    /**
     * Logs an error with full stack trace.
     */
    exception(error: Error, message?: string): void;
    /**
     * Starts a collapsible group in the log output.
     */
    startGroup(name: string): void;
    /**
     * Ends a collapsible group.
     */
    endGroup(): void;
    /**
     * Executes a function within a log group.
     */
    group<T>(name: string, fn: () => Promise<T>): Promise<T>;
    /**
     * Formats a log message with optional prefix and timestamp.
     */
    private format;
    /**
     * Creates a child logger with a sub-name.
     */
    child(name: string): Logger;
}
/**
 * Configures the global logger settings.
 */
export declare function configureLogger(config: Partial<LoggerConfig>): void;
/**
 * Sets the global log level.
 */
export declare function setLogLevel(level: LogLevel): void;
/**
 * Creates a logger for the given component.
 */
export declare function createLogger(name: string): Logger;
/**
 * Default logger instance for quick access.
 */
export declare const logger: Logger;
/**
 * Utility to measure and log execution time.
 */
export declare function timed<T>(log: Logger, operation: string, fn: () => Promise<T>): Promise<T>;
//# sourceMappingURL=logger.d.ts.map
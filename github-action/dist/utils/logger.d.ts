export declare enum LogLevel {
    DEBUG = 0,
    INFO = 1,
    WARN = 2,
    ERROR = 3
}
export interface LoggerConfig {
    level: LogLevel;
    timestamps: boolean;
    includePrefix: boolean;
}
export declare class Logger {
    private name;
    private config;
    constructor(name: string, config?: Partial<LoggerConfig>);
    debug(message: string, ...args: unknown[]): void;
    info(message: string, ...args: unknown[]): void;
    warn(message: string, ...args: unknown[]): void;
    error(message: string, ...args: unknown[]): void;
    exception(error: Error, message?: string): void;
    startGroup(name: string): void;
    endGroup(): void;
    group<T>(name: string, fn: () => Promise<T>): Promise<T>;
    private format;
    child(name: string): Logger;
}
export declare function configureLogger(config: Partial<LoggerConfig>): void;
export declare function setLogLevel(level: LogLevel): void;
export declare function createLogger(name: string): Logger;
export declare const logger: Logger;
export declare function timed<T>(log: Logger, operation: string, fn: () => Promise<T>): Promise<T>;
//# sourceMappingURL=logger.d.ts.map
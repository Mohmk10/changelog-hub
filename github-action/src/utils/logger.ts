import * as core from '@actions/core';

/**
 * Log levels for the logger
 */
export enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3,
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
 * Default logger configuration
 */
const defaultConfig: LoggerConfig = {
  level: LogLevel.INFO,
  timestamps: false,
  includePrefix: true,
};

/**
 * Global logger configuration
 */
let globalConfig: LoggerConfig = { ...defaultConfig };

/**
 * A structured logger that integrates with GitHub Actions logging.
 */
export class Logger {
  private name: string;
  private config: LoggerConfig;

  /**
   * Creates a new logger instance.
   *
   * @param name - Name of the logger (usually component name)
   * @param config - Optional configuration override
   */
  constructor(name: string, config?: Partial<LoggerConfig>) {
    this.name = name;
    this.config = { ...globalConfig, ...config };
  }

  /**
   * Logs a debug message.
   * Debug messages are only shown when ACTIONS_STEP_DEBUG is set.
   */
  debug(message: string, ...args: unknown[]): void {
    if (this.config.level <= LogLevel.DEBUG) {
      const formatted = this.format(message, args);
      core.debug(formatted);
    }
  }

  /**
   * Logs an info message.
   */
  info(message: string, ...args: unknown[]): void {
    if (this.config.level <= LogLevel.INFO) {
      const formatted = this.format(message, args);
      core.info(formatted);
    }
  }

  /**
   * Logs a warning message.
   */
  warn(message: string, ...args: unknown[]): void {
    if (this.config.level <= LogLevel.WARN) {
      const formatted = this.format(message, args);
      core.warning(formatted);
    }
  }

  /**
   * Logs an error message.
   */
  error(message: string, ...args: unknown[]): void {
    if (this.config.level <= LogLevel.ERROR) {
      const formatted = this.format(message, args);
      core.error(formatted);
    }
  }

  /**
   * Logs an error with full stack trace.
   */
  exception(error: Error, message?: string): void {
    const errorMessage = message ? `${message}: ${error.message}` : error.message;
    this.error(errorMessage);
    if (error.stack) {
      core.debug(error.stack);
    }
  }

  /**
   * Starts a collapsible group in the log output.
   */
  startGroup(name: string): void {
    core.startGroup(name);
  }

  /**
   * Ends a collapsible group.
   */
  endGroup(): void {
    core.endGroup();
  }

  /**
   * Executes a function within a log group.
   */
  async group<T>(name: string, fn: () => Promise<T>): Promise<T> {
    return core.group(name, fn);
  }

  /**
   * Formats a log message with optional prefix and timestamp.
   */
  private format(message: string, args: unknown[]): string {
    let formatted = message;

    // Append additional arguments
    if (args.length > 0) {
      const argsStr = args
        .map((arg) => {
          if (typeof arg === 'object') {
            try {
              return JSON.stringify(arg);
            } catch {
              return String(arg);
            }
          }
          return String(arg);
        })
        .join(' ');
      formatted = `${formatted} ${argsStr}`;
    }

    // Add timestamp if configured
    if (this.config.timestamps) {
      const timestamp = new Date().toISOString();
      formatted = `[${timestamp}] ${formatted}`;
    }

    // Add prefix if configured
    if (this.config.includePrefix) {
      formatted = `[${this.name}] ${formatted}`;
    }

    return formatted;
  }

  /**
   * Creates a child logger with a sub-name.
   */
  child(name: string): Logger {
    return new Logger(`${this.name}:${name}`, this.config);
  }
}

/**
 * Configures the global logger settings.
 */
export function configureLogger(config: Partial<LoggerConfig>): void {
  globalConfig = { ...globalConfig, ...config };
}

/**
 * Sets the global log level.
 */
export function setLogLevel(level: LogLevel): void {
  globalConfig.level = level;
}

/**
 * Creates a logger for the given component.
 */
export function createLogger(name: string): Logger {
  return new Logger(name);
}

/**
 * Default logger instance for quick access.
 */
export const logger = new Logger('ChangelogHub');

/**
 * Utility to measure and log execution time.
 */
export async function timed<T>(
  log: Logger,
  operation: string,
  fn: () => Promise<T>
): Promise<T> {
  const start = Date.now();
  log.debug(`Starting: ${operation}`);

  try {
    const result = await fn();
    const duration = Date.now() - start;
    log.debug(`Completed: ${operation} (${duration}ms)`);
    return result;
  } catch (error) {
    const duration = Date.now() - start;
    log.error(`Failed: ${operation} (${duration}ms)`);
    throw error;
  }
}

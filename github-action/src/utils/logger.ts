import * as core from '@actions/core';

export enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3,
}

export interface LoggerConfig {
  
  level: LogLevel;
  
  timestamps: boolean;
  
  includePrefix: boolean;
}

const defaultConfig: LoggerConfig = {
  level: LogLevel.INFO,
  timestamps: false,
  includePrefix: true,
};

let globalConfig: LoggerConfig = { ...defaultConfig };

export class Logger {
  private name: string;
  private config: LoggerConfig;

  constructor(name: string, config?: Partial<LoggerConfig>) {
    this.name = name;
    this.config = { ...globalConfig, ...config };
  }

  debug(message: string, ...args: unknown[]): void {
    if (this.config.level <= LogLevel.DEBUG) {
      const formatted = this.format(message, args);
      core.debug(formatted);
    }
  }

  info(message: string, ...args: unknown[]): void {
    if (this.config.level <= LogLevel.INFO) {
      const formatted = this.format(message, args);
      core.info(formatted);
    }
  }

  warn(message: string, ...args: unknown[]): void {
    if (this.config.level <= LogLevel.WARN) {
      const formatted = this.format(message, args);
      core.warning(formatted);
    }
  }

  error(message: string, ...args: unknown[]): void {
    if (this.config.level <= LogLevel.ERROR) {
      const formatted = this.format(message, args);
      core.error(formatted);
    }
  }

  exception(error: Error, message?: string): void {
    const errorMessage = message ? `${message}: ${error.message}` : error.message;
    this.error(errorMessage);
    if (error.stack) {
      core.debug(error.stack);
    }
  }

  startGroup(name: string): void {
    core.startGroup(name);
  }

  endGroup(): void {
    core.endGroup();
  }

  async group<T>(name: string, fn: () => Promise<T>): Promise<T> {
    return core.group(name, fn);
  }

  private format(message: string, args: unknown[]): string {
    let formatted = message;

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

    if (this.config.timestamps) {
      const timestamp = new Date().toISOString();
      formatted = `[${timestamp}] ${formatted}`;
    }

    if (this.config.includePrefix) {
      formatted = `[${this.name}] ${formatted}`;
    }

    return formatted;
  }

  child(name: string): Logger {
    return new Logger(`${this.name}:${name}`, this.config);
  }
}

export function configureLogger(config: Partial<LoggerConfig>): void {
  globalConfig = { ...globalConfig, ...config };
}

export function setLogLevel(level: LogLevel): void {
  globalConfig.level = level;
}

export function createLogger(name: string): Logger {
  return new Logger(name);
}

export const logger = new Logger('ChangelogHub');

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

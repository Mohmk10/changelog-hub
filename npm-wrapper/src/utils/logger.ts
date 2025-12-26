import chalk from 'chalk';

/**
 * Logger class for CLI output with color support
 */
export class Logger {
  private verbose: boolean;
  private quiet: boolean;

  constructor(verbose = false, quiet = false) {
    this.verbose = verbose;
    this.quiet = quiet;
  }

  /**
   * Log an info message
   */
  info(message: string): void {
    if (!this.quiet) {
      console.log(message);
    }
  }

  /**
   * Log a success message with green checkmark
   */
  success(message: string): void {
    if (!this.quiet) {
      console.log(chalk.green(`✓ ${message}`));
    }
  }

  /**
   * Log a warning message with yellow warning sign
   */
  warn(message: string): void {
    console.log(chalk.yellow(`⚠ ${message}`));
  }

  /**
   * Log an error message with red X
   */
  error(message: string): void {
    console.error(chalk.red(`✗ ${message}`));
  }

  /**
   * Log a debug message (only in verbose mode)
   */
  debug(message: string): void {
    if (this.verbose) {
      console.log(chalk.gray(`[DEBUG] ${message}`));
    }
  }

  /**
   * Log a blank line
   */
  newline(): void {
    if (!this.quiet) {
      console.log('');
    }
  }

  /**
   * Log a header with styling
   */
  header(message: string): void {
    if (!this.quiet) {
      console.log(chalk.bold.blue(`\n${message}`));
      console.log(chalk.blue('─'.repeat(message.length)));
    }
  }

  /**
   * Log a section title
   */
  section(message: string): void {
    if (!this.quiet) {
      console.log(chalk.bold(`\n${message}`));
    }
  }

  /**
   * Log a key-value pair
   */
  keyValue(key: string, value: string | number, color?: 'green' | 'red' | 'yellow' | 'blue'): void {
    if (!this.quiet) {
      const valueStr = String(value);
      let coloredValue = valueStr;

      switch (color) {
        case 'green':
          coloredValue = chalk.green(valueStr);
          break;
        case 'red':
          coloredValue = chalk.red(valueStr);
          break;
        case 'yellow':
          coloredValue = chalk.yellow(valueStr);
          break;
        case 'blue':
          coloredValue = chalk.blue(valueStr);
          break;
      }

      console.log(`  ${chalk.gray(key + ':')} ${coloredValue}`);
    }
  }

  /**
   * Log a bullet point
   */
  bullet(message: string, indent = 2): void {
    if (!this.quiet) {
      const spaces = ' '.repeat(indent);
      console.log(`${spaces}• ${message}`);
    }
  }

  /**
   * Log with custom color
   */
  colored(message: string, color: 'green' | 'red' | 'yellow' | 'blue' | 'gray' | 'cyan' | 'magenta'): void {
    if (!this.quiet) {
      console.log(chalk[color](message));
    }
  }

  /**
   * Log a table row
   */
  tableRow(columns: string[], widths: number[]): void {
    if (!this.quiet) {
      const row = columns.map((col, i) => col.padEnd(widths[i] || 20)).join(' ');
      console.log(`  ${row}`);
    }
  }

  /**
   * Log a horizontal divider
   */
  divider(char = '─', length = 60): void {
    if (!this.quiet) {
      console.log(chalk.gray(char.repeat(length)));
    }
  }
}

/**
 * Create a new logger instance
 */
export function createLogger(verbose = false, quiet = false): Logger {
  return new Logger(verbose, quiet);
}

/**
 * Default logger instance
 */
export const logger = new Logger();

import chalk from 'chalk';

export class Logger {
  private verbose: boolean;
  private quiet: boolean;

  constructor(verbose = false, quiet = false) {
    this.verbose = verbose;
    this.quiet = quiet;
  }

  info(message: string): void {
    if (!this.quiet) {
      console.log(message);
    }
  }

  success(message: string): void {
    if (!this.quiet) {
      console.log(chalk.green(`✓ ${message}`));
    }
  }

  warn(message: string): void {
    console.log(chalk.yellow(`⚠ ${message}`));
  }

  error(message: string): void {
    console.error(chalk.red(`✗ ${message}`));
  }

  debug(message: string): void {
    if (this.verbose) {
      console.log(chalk.gray(`[DEBUG] ${message}`));
    }
  }

  newline(): void {
    if (!this.quiet) {
      console.log('');
    }
  }

  header(message: string): void {
    if (!this.quiet) {
      console.log(chalk.bold.blue(`\n${message}`));
      console.log(chalk.blue('─'.repeat(message.length)));
    }
  }

  section(message: string): void {
    if (!this.quiet) {
      console.log(chalk.bold(`\n${message}`));
    }
  }

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

  bullet(message: string, indent = 2): void {
    if (!this.quiet) {
      const spaces = ' '.repeat(indent);
      console.log(`${spaces}• ${message}`);
    }
  }

  colored(message: string, color: 'green' | 'red' | 'yellow' | 'blue' | 'gray' | 'cyan' | 'magenta'): void {
    if (!this.quiet) {
      console.log(chalk[color](message));
    }
  }

  tableRow(columns: string[], widths: number[]): void {
    if (!this.quiet) {
      const row = columns.map((col, i) => col.padEnd(widths[i] || 20)).join(' ');
      console.log(`  ${row}`);
    }
  }

  divider(char = '─', length = 60): void {
    if (!this.quiet) {
      console.log(chalk.gray(char.repeat(length)));
    }
  }
}

export function createLogger(verbose = false, quiet = false): Logger {
  return new Logger(verbose, quiet);
}

export const logger = new Logger();

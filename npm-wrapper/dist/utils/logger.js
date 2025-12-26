"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.logger = exports.Logger = void 0;
exports.createLogger = createLogger;
const chalk_1 = __importDefault(require("chalk"));
class Logger {
    verbose;
    quiet;
    constructor(verbose = false, quiet = false) {
        this.verbose = verbose;
        this.quiet = quiet;
    }
    info(message) {
        if (!this.quiet) {
            console.log(message);
        }
    }
    success(message) {
        if (!this.quiet) {
            console.log(chalk_1.default.green(`✓ ${message}`));
        }
    }
    warn(message) {
        console.log(chalk_1.default.yellow(`⚠ ${message}`));
    }
    error(message) {
        console.error(chalk_1.default.red(`✗ ${message}`));
    }
    debug(message) {
        if (this.verbose) {
            console.log(chalk_1.default.gray(`[DEBUG] ${message}`));
        }
    }
    newline() {
        if (!this.quiet) {
            console.log('');
        }
    }
    header(message) {
        if (!this.quiet) {
            console.log(chalk_1.default.bold.blue(`\n${message}`));
            console.log(chalk_1.default.blue('─'.repeat(message.length)));
        }
    }
    section(message) {
        if (!this.quiet) {
            console.log(chalk_1.default.bold(`\n${message}`));
        }
    }
    keyValue(key, value, color) {
        if (!this.quiet) {
            const valueStr = String(value);
            let coloredValue = valueStr;
            switch (color) {
                case 'green':
                    coloredValue = chalk_1.default.green(valueStr);
                    break;
                case 'red':
                    coloredValue = chalk_1.default.red(valueStr);
                    break;
                case 'yellow':
                    coloredValue = chalk_1.default.yellow(valueStr);
                    break;
                case 'blue':
                    coloredValue = chalk_1.default.blue(valueStr);
                    break;
            }
            console.log(`  ${chalk_1.default.gray(key + ':')} ${coloredValue}`);
        }
    }
    bullet(message, indent = 2) {
        if (!this.quiet) {
            const spaces = ' '.repeat(indent);
            console.log(`${spaces}• ${message}`);
        }
    }
    colored(message, color) {
        if (!this.quiet) {
            console.log(chalk_1.default[color](message));
        }
    }
    tableRow(columns, widths) {
        if (!this.quiet) {
            const row = columns.map((col, i) => col.padEnd(widths[i] || 20)).join(' ');
            console.log(`  ${row}`);
        }
    }
    divider(char = '─', length = 60) {
        if (!this.quiet) {
            console.log(chalk_1.default.gray(char.repeat(length)));
        }
    }
}
exports.Logger = Logger;
function createLogger(verbose = false, quiet = false) {
    return new Logger(verbose, quiet);
}
exports.logger = new Logger();
//# sourceMappingURL=logger.js.map
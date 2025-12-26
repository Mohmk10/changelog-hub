"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.createValidateCommand = createValidateCommand;
const commander_1 = require("commander");
const path = __importStar(require("path"));
const ora_1 = __importDefault(require("ora"));
const chalk_1 = __importDefault(require("chalk"));
const parser_1 = require("../core/parser");
const file_1 = require("../utils/file");
const logger_1 = require("../utils/logger");
function createValidateCommand() {
    const command = new commander_1.Command('validate');
    command
        .description('Validate API specification files')
        .argument('<specs...>', 'Path(s) to API specification file(s)')
        .option('-v, --verbose', 'Show verbose validation output')
        .option('--strict', 'Enable strict validation mode')
        .option('-f, --format <format>', 'Output format (console, json)', 'console')
        .action(async (specs, options) => {
        const spinner = (0, ora_1.default)('Validating API specifications...').start();
        const results = [];
        let hasErrors = false;
        for (const spec of specs) {
            const specPath = path.resolve(spec);
            const result = validateSpec(specPath, options.strict);
            results.push(result);
            if (!result.valid) {
                hasErrors = true;
            }
        }
        spinner.stop();
        if (options.format === 'json') {
            console.log(JSON.stringify(results, null, 2));
        }
        else {
            outputConsoleResults(results, options.verbose);
        }
        if (hasErrors) {
            process.exit(1);
        }
    });
    return command;
}
function validateSpec(specPath, strict = false) {
    const result = {
        file: specPath,
        valid: true,
        type: 'unknown',
        errors: [],
        warnings: [],
    };
    if (!(0, file_1.fileExists)(specPath)) {
        result.valid = false;
        result.errors.push(`File not found: ${specPath}`);
        return result;
    }
    try {
        const content = (0, file_1.readFile)(specPath);
        result.type = (0, parser_1.detectSpecType)(content, specPath);
        if (result.type === 'unknown') {
            result.valid = false;
            result.errors.push('Unable to detect specification type');
            return result;
        }
        const spec = (0, parser_1.parseSpec)(content, specPath);
        if (!spec.name || spec.name === 'Untitled API') {
            result.warnings.push('API name not specified');
        }
        if (!spec.version || spec.version === '1.0.0') {
            result.warnings.push('API version not specified or using default');
        }
        if (spec.endpoints.length === 0) {
            result.warnings.push('No endpoints defined');
        }
        if (strict) {
            for (const endpoint of spec.endpoints) {
                if (!endpoint.description && !endpoint.summary) {
                    result.warnings.push(`Endpoint ${endpoint.method} ${endpoint.path} has no description`);
                }
                if (endpoint.responses.length === 0) {
                    result.warnings.push(`Endpoint ${endpoint.method} ${endpoint.path} has no response definitions`);
                }
                if (endpoint.deprecated) {
                    result.warnings.push(`Endpoint ${endpoint.method} ${endpoint.path} is deprecated`);
                }
            }
            for (const schema of spec.schemas) {
                if (schema.properties.length === 0 && schema.type === 'object') {
                    result.warnings.push(`Schema ${schema.name} has no properties defined`);
                }
            }
            if (result.warnings.length > 0) {
                result.valid = false;
                result.errors = [...result.errors, ...result.warnings];
                result.warnings = [];
            }
        }
    }
    catch (error) {
        result.valid = false;
        const message = error instanceof Error ? error.message : String(error);
        result.errors.push(`Parse error: ${message}`);
    }
    return result;
}
function outputConsoleResults(results, verbose = false) {
    console.log('');
    console.log(chalk_1.default.bold('API Specification Validation Results'));
    console.log(chalk_1.default.gray('─'.repeat(60)));
    console.log('');
    let totalValid = 0;
    let totalInvalid = 0;
    for (const result of results) {
        const statusIcon = result.valid ? chalk_1.default.green('✓') : chalk_1.default.red('✗');
        const statusText = result.valid ? chalk_1.default.green('VALID') : chalk_1.default.red('INVALID');
        const typeText = chalk_1.default.gray(`[${result.type.toUpperCase()}]`);
        console.log(`${statusIcon} ${path.basename(result.file)} ${typeText} ${statusText}`);
        if (result.valid) {
            totalValid++;
        }
        else {
            totalInvalid++;
        }
        if (result.errors.length > 0) {
            for (const error of result.errors) {
                console.log(chalk_1.default.red(`    ✗ ${error}`));
            }
        }
        if (verbose && result.warnings.length > 0) {
            for (const warning of result.warnings) {
                console.log(chalk_1.default.yellow(`    ⚠ ${warning}`));
            }
        }
        console.log('');
    }
    console.log(chalk_1.default.gray('─'.repeat(60)));
    console.log('');
    console.log(chalk_1.default.bold('Summary'));
    console.log(`  Total:   ${results.length}`);
    console.log(`  Valid:   ${chalk_1.default.green(totalValid.toString())}`);
    console.log(`  Invalid: ${chalk_1.default.red(totalInvalid.toString())}`);
    console.log('');
    if (totalInvalid === 0) {
        logger_1.logger.success('All specifications are valid');
    }
    else {
        logger_1.logger.error(`${totalInvalid} specification(s) failed validation`);
    }
}
//# sourceMappingURL=validate.js.map
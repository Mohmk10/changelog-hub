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
exports.createCompareCommand = createCompareCommand;
const commander_1 = require("commander");
const path = __importStar(require("path"));
const ora_1 = __importDefault(require("ora"));
const detector_1 = require("../core/detector");
const reporter_1 = require("../core/reporter");
const file_1 = require("../utils/file");
const logger_1 = require("../utils/logger");
const config_1 = require("../utils/config");
function createCompareCommand() {
    const command = new commander_1.Command('compare');
    command
        .description('Compare two API specifications and detect breaking changes')
        .argument('<old-spec>', 'Path to the old/base API specification')
        .argument('<new-spec>', 'Path to the new/head API specification')
        .option('-f, --format <format>', 'Output format (console, markdown, json, html)', 'console')
        .option('-o, --output <file>', 'Write output to file')
        .option('--fail-on-breaking', 'Exit with non-zero code if breaking changes detected')
        .option('-v, --verbose', 'Show verbose output')
        .option('--severity <level>', 'Minimum severity to report (INFO, WARNING, DANGEROUS, BREAKING)', 'INFO')
        .option('--no-deprecations', 'Exclude deprecation warnings')
        .option('-c, --config <file>', 'Path to configuration file')
        .action(async (oldSpec, newSpec, options) => {
        const spinner = (0, ora_1.default)('Comparing API specifications...').start();
        try {
            const config = (0, config_1.loadConfig)(options.config);
            const oldSpecPath = path.resolve(oldSpec);
            const newSpecPath = path.resolve(newSpec);
            if (!(0, file_1.fileExists)(oldSpecPath)) {
                spinner.fail(`Old spec file not found: ${oldSpecPath}`);
                process.exit(1);
            }
            if (!(0, file_1.fileExists)(newSpecPath)) {
                spinner.fail(`New spec file not found: ${newSpecPath}`);
                process.exit(1);
            }
            if (options.verbose) {
                spinner.text = `Comparing ${oldSpecPath} → ${newSpecPath}`;
            }
            const result = (0, detector_1.detectBreakingChanges)(oldSpecPath, newSpecPath, {
                severityThreshold: options.severity || config.severityThreshold,
                includeDeprecations: options.deprecations ?? config.includeDeprecations,
            });
            spinner.succeed('Comparison complete');
            const format = (options.format || config.defaultFormat);
            const report = (0, reporter_1.generateReport)(result, format);
            if (options.output) {
                const outputPath = path.resolve(options.output);
                (0, file_1.writeFile)(outputPath, report);
                logger_1.logger.success(`Report saved to ${outputPath}`);
            }
            else {
                console.log(report);
            }
            if (options.verbose) {
                console.log('\n' + (0, reporter_1.generateShortSummary)(result));
            }
            const failOnBreaking = options.failOnBreaking ?? config.failOnBreaking;
            if (failOnBreaking && result.breakingChanges.length > 0) {
                logger_1.logger.error(`Found ${result.breakingChanges.length} breaking change(s)`);
                process.exit(1);
            }
        }
        catch (error) {
            spinner.fail('Comparison failed');
            const message = error instanceof Error ? error.message : String(error);
            logger_1.logger.error(message);
            process.exit(1);
        }
    });
    return command;
}
//# sourceMappingURL=compare.js.map
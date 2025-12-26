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
exports.createAnalyzeCommand = createAnalyzeCommand;
const commander_1 = require("commander");
const path = __importStar(require("path"));
const ora_1 = __importDefault(require("ora"));
const chalk_1 = __importDefault(require("chalk"));
const parser_1 = require("../core/parser");
const file_1 = require("../utils/file");
const logger_1 = require("../utils/logger");
function createAnalyzeCommand() {
    const command = new commander_1.Command('analyze');
    command
        .description('Analyze a single API specification and display its structure')
        .argument('<spec>', 'Path to the API specification file')
        .option('-v, --verbose', 'Show detailed analysis')
        .option('--endpoints', 'List all endpoints')
        .option('--schemas', 'List all schemas')
        .option('--security', 'List security schemes')
        .option('-f, --format <format>', 'Output format (console, json)', 'console')
        .action(async (spec, options) => {
        const spinner = (0, ora_1.default)('Analyzing API specification...').start();
        try {
            const specPath = path.resolve(spec);
            if (!(0, file_1.fileExists)(specPath)) {
                spinner.fail(`Spec file not found: ${specPath}`);
                process.exit(1);
            }
            const content = (0, file_1.readFile)(specPath);
            const parsedSpec = (0, parser_1.parseSpec)(content, specPath);
            spinner.succeed('Analysis complete');
            if (options.format === 'json') {
                outputJson(parsedSpec, options);
            }
            else {
                outputConsole(parsedSpec, options);
            }
        }
        catch (error) {
            spinner.fail('Analysis failed');
            const message = error instanceof Error ? error.message : String(error);
            logger_1.logger.error(message);
            process.exit(1);
        }
    });
    return command;
}
function outputJson(spec, options) {
    const output = {
        name: spec.name,
        version: spec.version,
        type: spec.type,
        summary: {
            endpointsCount: spec.endpoints.length,
            schemasCount: spec.schemas.length,
            securitySchemesCount: spec.security.length,
        },
    };
    if (options.endpoints) {
        output.endpoints = spec.endpoints;
    }
    if (options.schemas) {
        output.schemas = spec.schemas;
    }
    if (options.security) {
        output.security = spec.security;
    }
    if (!options.endpoints && !options.schemas && !options.security) {
        output.endpoints = spec.endpoints;
        output.schemas = spec.schemas;
        output.security = spec.security;
    }
    console.log(JSON.stringify(output, null, 2));
}
function outputConsole(spec, options) {
    console.log('');
    console.log(chalk_1.default.bold.blue('═'.repeat(60)));
    console.log(chalk_1.default.bold.blue('  API SPECIFICATION ANALYSIS'));
    console.log(chalk_1.default.bold.blue('═'.repeat(60)));
    console.log('');
    console.log(chalk_1.default.bold(`  ${spec.name}`));
    console.log(chalk_1.default.gray(`  Version: ${spec.version}`));
    console.log(chalk_1.default.gray(`  Type: ${spec.type.toUpperCase()}`));
    console.log('');
    console.log(chalk_1.default.gray('─'.repeat(60)));
    console.log(chalk_1.default.bold('\n  SUMMARY\n'));
    console.log(`  Endpoints:        ${spec.endpoints.length}`);
    console.log(`  Schemas:          ${spec.schemas.length}`);
    console.log(`  Security Schemes: ${spec.security.length}`);
    console.log('');
    const showEndpoints = options.endpoints || options.verbose;
    const showSchemas = options.schemas || options.verbose;
    const showSecurity = options.security || options.verbose;
    if (showEndpoints && spec.endpoints.length > 0) {
        console.log(chalk_1.default.gray('─'.repeat(60)));
        console.log(chalk_1.default.bold('\n  ENDPOINTS\n'));
        for (const endpoint of spec.endpoints) {
            const methodColor = getMethodColor(endpoint.method);
            console.log(`  ${methodColor(endpoint.method.padEnd(8))} ${endpoint.path}`);
            if (options.verbose) {
                if (endpoint.summary) {
                    console.log(chalk_1.default.gray(`           ${endpoint.summary}`));
                }
                if (endpoint.deprecated) {
                    console.log(chalk_1.default.yellow('           [DEPRECATED]'));
                }
                console.log('');
            }
        }
        console.log('');
    }
    if (showSchemas && spec.schemas.length > 0) {
        console.log(chalk_1.default.gray('─'.repeat(60)));
        console.log(chalk_1.default.bold('\n  SCHEMAS\n'));
        for (const schema of spec.schemas) {
            console.log(`  ${chalk_1.default.cyan(schema.name)}`);
            if (options.verbose && schema.properties.length > 0) {
                for (const prop of schema.properties) {
                    const required = prop.required ? chalk_1.default.red('*') : ' ';
                    console.log(chalk_1.default.gray(`    ${required} ${prop.name}: ${prop.type}`));
                }
                console.log('');
            }
        }
        console.log('');
    }
    if (showSecurity && spec.security.length > 0) {
        console.log(chalk_1.default.gray('─'.repeat(60)));
        console.log(chalk_1.default.bold('\n  SECURITY SCHEMES\n'));
        for (const sec of spec.security) {
            console.log(`  ${chalk_1.default.magenta(sec.name)} (${sec.type})`);
            if (options.verbose && sec.description) {
                console.log(chalk_1.default.gray(`    ${sec.description}`));
            }
        }
        console.log('');
    }
    console.log(chalk_1.default.bold.blue('═'.repeat(60)));
    console.log('');
}
function getMethodColor(method) {
    switch (method.toUpperCase()) {
        case 'GET':
            return chalk_1.default.green;
        case 'POST':
            return chalk_1.default.yellow;
        case 'PUT':
            return chalk_1.default.blue;
        case 'DELETE':
            return chalk_1.default.red;
        case 'PATCH':
            return chalk_1.default.cyan;
        default:
            return chalk_1.default.white;
    }
}
//# sourceMappingURL=analyze.js.map
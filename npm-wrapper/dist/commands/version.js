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
exports.createVersionCommand = createVersionCommand;
exports.getVersion = getVersion;
const commander_1 = require("commander");
const path = __importStar(require("path"));
const fs = __importStar(require("fs"));
const chalk_1 = __importDefault(require("chalk"));
function createVersionCommand() {
    const command = new commander_1.Command('version');
    command
        .description('Display version information')
        .option('--json', 'Output version info as JSON')
        .action((options) => {
        const versionInfo = getVersionInfo();
        if (options.json) {
            console.log(JSON.stringify(versionInfo, null, 2));
        }
        else {
            displayVersionInfo(versionInfo);
        }
    });
    return command;
}
function getVersionInfo() {
    let packageJson = {
        name: '@mohmk10/changelog-hub',
        version: '1.0.0',
        description: 'CLI tool for detecting breaking changes in API specifications',
    };
    try {
        const possiblePaths = [
            path.join(__dirname, '../../package.json'),
            path.join(__dirname, '../../../package.json'),
            path.join(process.cwd(), 'package.json'),
        ];
        for (const packagePath of possiblePaths) {
            if (fs.existsSync(packagePath)) {
                const content = fs.readFileSync(packagePath, 'utf-8');
                packageJson = JSON.parse(content);
                break;
            }
        }
    }
    catch {
    }
    return {
        name: packageJson.name,
        version: packageJson.version,
        description: packageJson.description,
        nodeVersion: process.version,
        platform: process.platform,
        arch: process.arch,
    };
}
function displayVersionInfo(info) {
    console.log('');
    console.log(chalk_1.default.bold.blue('Changelog Hub CLI'));
    console.log('');
    console.log(`  ${chalk_1.default.bold('Package:')}     ${info.name}`);
    console.log(`  ${chalk_1.default.bold('Version:')}     ${chalk_1.default.green(info.version)}`);
    console.log(`  ${chalk_1.default.bold('Description:')} ${info.description}`);
    console.log('');
    console.log(chalk_1.default.gray('  Runtime Information'));
    console.log(`  ${chalk_1.default.bold('Node.js:')}     ${info.nodeVersion}`);
    console.log(`  ${chalk_1.default.bold('Platform:')}    ${info.platform}`);
    console.log(`  ${chalk_1.default.bold('Architecture:')} ${info.arch}`);
    console.log('');
}
function getVersion() {
    const info = getVersionInfo();
    return info.version;
}
//# sourceMappingURL=version.js.map
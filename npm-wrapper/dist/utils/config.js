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
Object.defineProperty(exports, "__esModule", { value: true });
exports.loadConfig = loadConfig;
exports.saveConfig = saveConfig;
exports.createDefaultConfig = createDefaultConfig;
exports.getDefaultConfig = getDefaultConfig;
const fs = __importStar(require("fs"));
const path = __importStar(require("path"));
const yaml = __importStar(require("yaml"));
const defaultConfig = {
    defaultFormat: 'console',
    failOnBreaking: false,
    specPath: 'api/openapi.yaml',
    severityThreshold: 'INFO',
    includeDeprecations: true,
    customRules: [],
};
const configFileNames = [
    '.changelog-hub.yaml',
    '.changelog-hub.yml',
    '.changelog-hub.json',
    'changelog-hub.config.yaml',
    'changelog-hub.config.yml',
    'changelog-hub.config.json',
];
function loadConfig(configPath) {
    let configFile;
    if (configPath) {
        configFile = path.resolve(configPath);
        if (!fs.existsSync(configFile)) {
            throw new Error(`Config file not found: ${configPath}`);
        }
    }
    else {
        const cwd = process.cwd();
        for (const name of configFileNames) {
            const fullPath = path.join(cwd, name);
            if (fs.existsSync(fullPath)) {
                configFile = fullPath;
                break;
            }
        }
    }
    if (!configFile) {
        return defaultConfig;
    }
    const content = fs.readFileSync(configFile, 'utf-8');
    const ext = path.extname(configFile).toLowerCase();
    let userConfig;
    if (ext === '.json') {
        userConfig = JSON.parse(content);
    }
    else {
        userConfig = yaml.parse(content);
    }
    return {
        ...defaultConfig,
        ...userConfig,
    };
}
function saveConfig(config, filePath) {
    const absolutePath = path.resolve(filePath);
    const ext = path.extname(absolutePath).toLowerCase();
    let content;
    if (ext === '.json') {
        content = JSON.stringify(config, null, 2);
    }
    else {
        content = yaml.stringify(config);
    }
    fs.writeFileSync(absolutePath, content, 'utf-8');
}
function createDefaultConfig(format = 'yaml') {
    const fileName = format === 'json' ? '.changelog-hub.json' : '.changelog-hub.yaml';
    const filePath = path.join(process.cwd(), fileName);
    saveConfig(defaultConfig, filePath);
    return filePath;
}
function getDefaultConfig() {
    return { ...defaultConfig };
}
//# sourceMappingURL=config.js.map
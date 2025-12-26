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
exports.readFile = readFile;
exports.writeFile = writeFile;
exports.fileExists = fileExists;
exports.isDirectory = isDirectory;
exports.getExtension = getExtension;
exports.getBasename = getBasename;
exports.listFiles = listFiles;
exports.createTempFile = createTempFile;
exports.deleteFile = deleteFile;
const fs = __importStar(require("fs"));
const path = __importStar(require("path"));
function readFile(filePath) {
    const absolutePath = path.resolve(filePath);
    return fs.readFileSync(absolutePath, 'utf-8');
}
function writeFile(filePath, content) {
    const absolutePath = path.resolve(filePath);
    const dir = path.dirname(absolutePath);
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
    fs.writeFileSync(absolutePath, content, 'utf-8');
}
function fileExists(filePath) {
    const absolutePath = path.resolve(filePath);
    return fs.existsSync(absolutePath);
}
function isDirectory(filePath) {
    const absolutePath = path.resolve(filePath);
    try {
        return fs.statSync(absolutePath).isDirectory();
    }
    catch {
        return false;
    }
}
function getExtension(filePath) {
    const ext = path.extname(filePath);
    return ext.startsWith('.') ? ext.substring(1).toLowerCase() : ext.toLowerCase();
}
function getBasename(filePath) {
    return path.basename(filePath, path.extname(filePath));
}
function listFiles(dirPath, pattern) {
    const absolutePath = path.resolve(dirPath);
    const files = fs.readdirSync(absolutePath);
    const result = files
        .map(file => path.join(absolutePath, file))
        .filter(file => fs.statSync(file).isFile());
    if (pattern) {
        return result.filter(file => pattern.test(path.basename(file)));
    }
    return result;
}
function createTempFile(content, extension = 'tmp') {
    const tempDir = process.env.TMPDIR || '/tmp';
    const filename = `changelog-hub-${Date.now()}-${Math.random().toString(36).substring(7)}.${extension}`;
    const filePath = path.join(tempDir, filename);
    writeFile(filePath, content);
    return filePath;
}
function deleteFile(filePath) {
    const absolutePath = path.resolve(filePath);
    if (fs.existsSync(absolutePath)) {
        fs.unlinkSync(absolutePath);
    }
}
//# sourceMappingURL=file.js.map
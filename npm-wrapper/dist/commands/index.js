"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getVersion = exports.createVersionCommand = exports.createValidateCommand = exports.createAnalyzeCommand = exports.createCompareCommand = void 0;
var compare_1 = require("./compare");
Object.defineProperty(exports, "createCompareCommand", { enumerable: true, get: function () { return compare_1.createCompareCommand; } });
var analyze_1 = require("./analyze");
Object.defineProperty(exports, "createAnalyzeCommand", { enumerable: true, get: function () { return analyze_1.createAnalyzeCommand; } });
var validate_1 = require("./validate");
Object.defineProperty(exports, "createValidateCommand", { enumerable: true, get: function () { return validate_1.createValidateCommand; } });
var version_1 = require("./version");
Object.defineProperty(exports, "createVersionCommand", { enumerable: true, get: function () { return version_1.createVersionCommand; } });
Object.defineProperty(exports, "getVersion", { enumerable: true, get: function () { return version_1.getVersion; } });
//# sourceMappingURL=index.js.map
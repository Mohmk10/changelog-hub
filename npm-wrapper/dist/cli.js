#!/usr/bin/env node
"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const commander_1 = require("commander");
const chalk_1 = __importDefault(require("chalk"));
const commands_1 = require("./commands");
const program = new commander_1.Command();
program
    .name('changelog-hub')
    .description('API breaking change detector with automatic changelog generation')
    .version((0, commands_1.getVersion)(), '-V, --version', 'Display version number')
    .helpOption('-h, --help', 'Display help information');
program.addCommand((0, commands_1.createCompareCommand)());
program.addCommand((0, commands_1.createAnalyzeCommand)());
program.addCommand((0, commands_1.createValidateCommand)());
program.addCommand((0, commands_1.createVersionCommand)());
program.exitOverride((err) => {
    if (err.code === 'commander.helpDisplayed' || err.code === 'commander.version') {
        throw err;
    }
    throw err;
});
async function main() {
    try {
        if (process.argv.length <= 2) {
            program.outputHelp();
            return;
        }
        await program.parseAsync(process.argv);
    }
    catch (error) {
        const err = error;
        if (err.code === 'commander.helpDisplayed' ||
            err.code === 'commander.version' ||
            err.message === '(outputHelp)') {
            return;
        }
        if (error instanceof Error) {
            console.error(chalk_1.default.red(`Error: ${error.message}`));
        }
        process.exit(1);
    }
}
main();
//# sourceMappingURL=cli.js.map
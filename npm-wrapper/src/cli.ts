#!/usr/bin/env node
import { Command } from 'commander';
import chalk from 'chalk';
import {
  createCompareCommand,
  createAnalyzeCommand,
  createValidateCommand,
  createVersionCommand,
  getVersion,
} from './commands';

const program = new Command();

program
  .name('changelog-hub')
  .description('API breaking change detector with automatic changelog generation')
  .version(getVersion(), '-V, --version', 'Display version number')
  .helpOption('-h, --help', 'Display help information');

// Register commands
program.addCommand(createCompareCommand());
program.addCommand(createAnalyzeCommand());
program.addCommand(createValidateCommand());
program.addCommand(createVersionCommand());

// Global error handling
program.exitOverride((err) => {
  // Suppress help and version display exit codes
  if (err.code === 'commander.helpDisplayed' || err.code === 'commander.version') {
    throw err; // Re-throw to be caught below with proper handling
  }
  throw err;
});

async function main(): Promise<void> {
  try {
    // Show help if no command provided
    if (process.argv.length <= 2) {
      program.outputHelp();
      return;
    }
    await program.parseAsync(process.argv);
  } catch (error: unknown) {
    // Handle commander errors properly
    const err = error as { code?: string; message?: string };

    // Don't show error for help/version exits
    if (err.code === 'commander.helpDisplayed' ||
        err.code === 'commander.version' ||
        err.message === '(outputHelp)') {
      return;
    }

    if (error instanceof Error) {
      console.error(chalk.red(`Error: ${error.message}`));
    }
    process.exit(1);
  }
}

main();

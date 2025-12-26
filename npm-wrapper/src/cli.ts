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

program.addCommand(createCompareCommand());
program.addCommand(createAnalyzeCommand());
program.addCommand(createValidateCommand());
program.addCommand(createVersionCommand());

program.exitOverride((err) => {
  
  if (err.code === 'commander.helpDisplayed' || err.code === 'commander.version') {
    throw err; 
  }
  throw err;
});

async function main(): Promise<void> {
  try {
    
    if (process.argv.length <= 2) {
      program.outputHelp();
      return;
    }
    await program.parseAsync(process.argv);
  } catch (error: unknown) {
    
    const err = error as { code?: string; message?: string };

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

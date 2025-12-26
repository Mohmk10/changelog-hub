import { Command } from 'commander';
import * as path from 'path';
import ora from 'ora';
import { detectBreakingChanges } from '../core/detector';
import { generateReport, generateShortSummary } from '../core/reporter';
import { writeFile, fileExists } from '../utils/file';
import { logger } from '../utils/logger';
import { loadConfig } from '../utils/config';
import { OutputFormat } from '../types';

export function createCompareCommand(): Command {
  const command = new Command('compare');

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
    .action(async (oldSpec: string, newSpec: string, options) => {
      const spinner = ora('Comparing API specifications...').start();

      try {
        
        const config = loadConfig(options.config);

        const oldSpecPath = path.resolve(oldSpec);
        const newSpecPath = path.resolve(newSpec);

        if (!fileExists(oldSpecPath)) {
          spinner.fail(`Old spec file not found: ${oldSpecPath}`);
          process.exit(1);
        }
        if (!fileExists(newSpecPath)) {
          spinner.fail(`New spec file not found: ${newSpecPath}`);
          process.exit(1);
        }

        if (options.verbose) {
          spinner.text = `Comparing ${oldSpecPath} → ${newSpecPath}`;
        }

        const result = detectBreakingChanges(oldSpecPath, newSpecPath, {
          severityThreshold: options.severity || config.severityThreshold,
          includeDeprecations: options.deprecations ?? config.includeDeprecations,
        });

        spinner.succeed('Comparison complete');

        const format = (options.format || config.defaultFormat) as OutputFormat;
        const report = generateReport(result, format);

        if (options.output) {
          const outputPath = path.resolve(options.output);
          writeFile(outputPath, report);
          logger.success(`Report saved to ${outputPath}`);
        } else {
          console.log(report);
        }

        if (options.verbose) {
          console.log('\n' + generateShortSummary(result));
        }

        const failOnBreaking = options.failOnBreaking ?? config.failOnBreaking;
        if (failOnBreaking && result.breakingChanges.length > 0) {
          logger.error(`Found ${result.breakingChanges.length} breaking change(s)`);
          process.exit(1);
        }
      } catch (error) {
        spinner.fail('Comparison failed');
        const message = error instanceof Error ? error.message : String(error);
        logger.error(message);
        process.exit(1);
      }
    });

  return command;
}

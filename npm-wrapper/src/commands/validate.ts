import { Command } from 'commander';
import * as path from 'path';
import ora from 'ora';
import chalk from 'chalk';
import { parseSpec, detectSpecType } from '../core/parser';
import { readFile, fileExists } from '../utils/file';
import { logger } from '../utils/logger';

interface ValidationResult {
  file: string;
  valid: boolean;
  type: string;
  errors: string[];
  warnings: string[];
}

export function createValidateCommand(): Command {
  const command = new Command('validate');

  command
    .description('Validate API specification files')
    .argument('<specs...>', 'Path(s) to API specification file(s)')
    .option('-v, --verbose', 'Show verbose validation output')
    .option('--strict', 'Enable strict validation mode')
    .option('-f, --format <format>', 'Output format (console, json)', 'console')
    .action(async (specs: string[], options) => {
      const spinner = ora('Validating API specifications...').start();

      const results: ValidationResult[] = [];
      let hasErrors = false;

      for (const spec of specs) {
        const specPath = path.resolve(spec);
        const result = validateSpec(specPath, options.strict);
        results.push(result);

        if (!result.valid) {
          hasErrors = true;
        }
      }

      spinner.stop();

      if (options.format === 'json') {
        console.log(JSON.stringify(results, null, 2));
      } else {
        outputConsoleResults(results, options.verbose);
      }

      if (hasErrors) {
        process.exit(1);
      }
    });

  return command;
}

function validateSpec(specPath: string, strict: boolean = false): ValidationResult {
  const result: ValidationResult = {
    file: specPath,
    valid: true,
    type: 'unknown',
    errors: [],
    warnings: [],
  };

  if (!fileExists(specPath)) {
    result.valid = false;
    result.errors.push(`File not found: ${specPath}`);
    return result;
  }

  try {
    
    const content = readFile(specPath);

    result.type = detectSpecType(content, specPath);

    if (result.type === 'unknown') {
      result.valid = false;
      result.errors.push('Unable to detect specification type');
      return result;
    }

    const spec = parseSpec(content, specPath);

    if (!spec.name || spec.name === 'Untitled API') {
      result.warnings.push('API name not specified');
    }

    if (!spec.version || spec.version === '1.0.0') {
      result.warnings.push('API version not specified or using default');
    }

    if (spec.endpoints.length === 0) {
      result.warnings.push('No endpoints defined');
    }

    if (strict) {
      
      for (const endpoint of spec.endpoints) {
        if (!endpoint.description && !endpoint.summary) {
          result.warnings.push(`Endpoint ${endpoint.method} ${endpoint.path} has no description`);
        }

        if (endpoint.responses.length === 0) {
          result.warnings.push(`Endpoint ${endpoint.method} ${endpoint.path} has no response definitions`);
        }

        if (endpoint.deprecated) {
          result.warnings.push(`Endpoint ${endpoint.method} ${endpoint.path} is deprecated`);
        }
      }

      for (const schema of spec.schemas) {
        if (schema.properties.length === 0 && schema.type === 'object') {
          result.warnings.push(`Schema ${schema.name} has no properties defined`);
        }
      }

      if (result.warnings.length > 0) {
        result.valid = false;
        result.errors = [...result.errors, ...result.warnings];
        result.warnings = [];
      }
    }
  } catch (error) {
    result.valid = false;
    const message = error instanceof Error ? error.message : String(error);
    result.errors.push(`Parse error: ${message}`);
  }

  return result;
}

function outputConsoleResults(results: ValidationResult[], verbose: boolean = false): void {
  console.log('');
  console.log(chalk.bold('API Specification Validation Results'));
  console.log(chalk.gray('─'.repeat(60)));
  console.log('');

  let totalValid = 0;
  let totalInvalid = 0;

  for (const result of results) {
    const statusIcon = result.valid ? chalk.green('✓') : chalk.red('✗');
    const statusText = result.valid ? chalk.green('VALID') : chalk.red('INVALID');
    const typeText = chalk.gray(`[${result.type.toUpperCase()}]`);

    console.log(`${statusIcon} ${path.basename(result.file)} ${typeText} ${statusText}`);

    if (result.valid) {
      totalValid++;
    } else {
      totalInvalid++;
    }

    if (result.errors.length > 0) {
      for (const error of result.errors) {
        console.log(chalk.red(`    ✗ ${error}`));
      }
    }

    if (verbose && result.warnings.length > 0) {
      for (const warning of result.warnings) {
        console.log(chalk.yellow(`    ⚠ ${warning}`));
      }
    }

    console.log('');
  }

  console.log(chalk.gray('─'.repeat(60)));
  console.log('');
  console.log(chalk.bold('Summary'));
  console.log(`  Total:   ${results.length}`);
  console.log(`  Valid:   ${chalk.green(totalValid.toString())}`);
  console.log(`  Invalid: ${chalk.red(totalInvalid.toString())}`);
  console.log('');

  if (totalInvalid === 0) {
    logger.success('All specifications are valid');
  } else {
    logger.error(`${totalInvalid} specification(s) failed validation`);
  }
}

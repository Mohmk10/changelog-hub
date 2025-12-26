import { Command } from 'commander';
import * as path from 'path';
import ora from 'ora';
import chalk from 'chalk';
import { parseSpec } from '../core/parser';
import { readFile, fileExists } from '../utils/file';
import { logger } from '../utils/logger';
import { ApiSpec } from '../types';

export function createAnalyzeCommand(): Command {
  const command = new Command('analyze');

  command
    .description('Analyze a single API specification and display its structure')
    .argument('<spec>', 'Path to the API specification file')
    .option('-v, --verbose', 'Show detailed analysis')
    .option('--endpoints', 'List all endpoints')
    .option('--schemas', 'List all schemas')
    .option('--security', 'List security schemes')
    .option('-f, --format <format>', 'Output format (console, json)', 'console')
    .action(async (spec: string, options) => {
      const spinner = ora('Analyzing API specification...').start();

      try {
        
        const specPath = path.resolve(spec);

        if (!fileExists(specPath)) {
          spinner.fail(`Spec file not found: ${specPath}`);
          process.exit(1);
        }

        const content = readFile(specPath);
        const parsedSpec = parseSpec(content, specPath);

        spinner.succeed('Analysis complete');

        if (options.format === 'json') {
          outputJson(parsedSpec, options);
        } else {
          outputConsole(parsedSpec, options);
        }
      } catch (error) {
        spinner.fail('Analysis failed');
        const message = error instanceof Error ? error.message : String(error);
        logger.error(message);
        process.exit(1);
      }
    });

  return command;
}

function outputJson(spec: ApiSpec, options: { endpoints?: boolean; schemas?: boolean; security?: boolean }): void {
  const output: Record<string, unknown> = {
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

function outputConsole(spec: ApiSpec, options: { verbose?: boolean; endpoints?: boolean; schemas?: boolean; security?: boolean }): void {
  console.log('');
  console.log(chalk.bold.blue('═'.repeat(60)));
  console.log(chalk.bold.blue('  API SPECIFICATION ANALYSIS'));
  console.log(chalk.bold.blue('═'.repeat(60)));
  console.log('');

  console.log(chalk.bold(`  ${spec.name}`));
  console.log(chalk.gray(`  Version: ${spec.version}`));
  console.log(chalk.gray(`  Type: ${spec.type.toUpperCase()}`));
  console.log('');

  console.log(chalk.gray('─'.repeat(60)));
  console.log(chalk.bold('\n  SUMMARY\n'));
  console.log(`  Endpoints:        ${spec.endpoints.length}`);
  console.log(`  Schemas:          ${spec.schemas.length}`);
  console.log(`  Security Schemes: ${spec.security.length}`);
  console.log('');

  const showEndpoints = options.endpoints || options.verbose;
  const showSchemas = options.schemas || options.verbose;
  const showSecurity = options.security || options.verbose;

  if (showEndpoints && spec.endpoints.length > 0) {
    console.log(chalk.gray('─'.repeat(60)));
    console.log(chalk.bold('\n  ENDPOINTS\n'));

    for (const endpoint of spec.endpoints) {
      const methodColor = getMethodColor(endpoint.method);
      console.log(`  ${methodColor(endpoint.method.padEnd(8))} ${endpoint.path}`);

      if (options.verbose) {
        if (endpoint.summary) {
          console.log(chalk.gray(`           ${endpoint.summary}`));
        }
        if (endpoint.deprecated) {
          console.log(chalk.yellow('           [DEPRECATED]'));
        }
        console.log('');
      }
    }
    console.log('');
  }

  if (showSchemas && spec.schemas.length > 0) {
    console.log(chalk.gray('─'.repeat(60)));
    console.log(chalk.bold('\n  SCHEMAS\n'));

    for (const schema of spec.schemas) {
      console.log(`  ${chalk.cyan(schema.name)}`);

      if (options.verbose && schema.properties.length > 0) {
        for (const prop of schema.properties) {
          const required = prop.required ? chalk.red('*') : ' ';
          console.log(chalk.gray(`    ${required} ${prop.name}: ${prop.type}`));
        }
        console.log('');
      }
    }
    console.log('');
  }

  if (showSecurity && spec.security.length > 0) {
    console.log(chalk.gray('─'.repeat(60)));
    console.log(chalk.bold('\n  SECURITY SCHEMES\n'));

    for (const sec of spec.security) {
      console.log(`  ${chalk.magenta(sec.name)} (${sec.type})`);
      if (options.verbose && sec.description) {
        console.log(chalk.gray(`    ${sec.description}`));
      }
    }
    console.log('');
  }

  console.log(chalk.bold.blue('═'.repeat(60)));
  console.log('');
}

function getMethodColor(method: string): (text: string) => string {
  switch (method.toUpperCase()) {
    case 'GET':
      return chalk.green;
    case 'POST':
      return chalk.yellow;
    case 'PUT':
      return chalk.blue;
    case 'DELETE':
      return chalk.red;
    case 'PATCH':
      return chalk.cyan;
    default:
      return chalk.white;
  }
}

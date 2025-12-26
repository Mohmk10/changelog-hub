import { Command } from 'commander';
import * as path from 'path';
import * as fs from 'fs';
import chalk from 'chalk';

export function createVersionCommand(): Command {
  const command = new Command('version');

  command
    .description('Display version information')
    .option('--json', 'Output version info as JSON')
    .action((options) => {
      const versionInfo = getVersionInfo();

      if (options.json) {
        console.log(JSON.stringify(versionInfo, null, 2));
      } else {
        displayVersionInfo(versionInfo);
      }
    });

  return command;
}

interface VersionInfo {
  name: string;
  version: string;
  description: string;
  nodeVersion: string;
  platform: string;
  arch: string;
}

function getVersionInfo(): VersionInfo {
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
  } catch {
    
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

function displayVersionInfo(info: VersionInfo): void {
  console.log('');
  console.log(chalk.bold.blue('Changelog Hub CLI'));
  console.log('');
  console.log(`  ${chalk.bold('Package:')}     ${info.name}`);
  console.log(`  ${chalk.bold('Version:')}     ${chalk.green(info.version)}`);
  console.log(`  ${chalk.bold('Description:')} ${info.description}`);
  console.log('');
  console.log(chalk.gray('  Runtime Information'));
  console.log(`  ${chalk.bold('Node.js:')}     ${info.nodeVersion}`);
  console.log(`  ${chalk.bold('Platform:')}    ${info.platform}`);
  console.log(`  ${chalk.bold('Architecture:')} ${info.arch}`);
  console.log('');
}

export function getVersion(): string {
  const info = getVersionInfo();
  return info.version;
}

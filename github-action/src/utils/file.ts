import * as fs from 'fs';
import * as path from 'path';
import * as io from '@actions/io';
import { Logger } from './logger';

const logger = new Logger('FileUtils');

export async function readFile(filePath: string): Promise<string> {
  const absolutePath = resolveAbsolutePath(filePath);
  logger.debug(`Reading file: ${absolutePath}`);

  try {
    const content = await fs.promises.readFile(absolutePath, 'utf-8');
    logger.debug(`Read ${content.length} bytes from ${absolutePath}`);
    return content;
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    throw new Error(`Failed to read file '${filePath}': ${message}`);
  }
}

export async function writeFile(filePath: string, content: string): Promise<void> {
  const absolutePath = resolveAbsolutePath(filePath);
  logger.debug(`Writing file: ${absolutePath}`);

  try {
    
    const dir = path.dirname(absolutePath);
    await io.mkdirP(dir);

    await fs.promises.writeFile(absolutePath, content, 'utf-8');
    logger.debug(`Wrote ${content.length} bytes to ${absolutePath}`);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    throw new Error(`Failed to write file '${filePath}': ${message}`);
  }
}

export async function fileExists(filePath: string): Promise<boolean> {
  const absolutePath = resolveAbsolutePath(filePath);

  try {
    const stats = await fs.promises.stat(absolutePath);
    return stats.isFile();
  } catch {
    return false;
  }
}

export async function directoryExists(dirPath: string): Promise<boolean> {
  const absolutePath = resolveAbsolutePath(dirPath);

  try {
    const stats = await fs.promises.stat(absolutePath);
    return stats.isDirectory();
  } catch {
    return false;
  }
}

export async function listFiles(dirPath: string, recursive = false): Promise<string[]> {
  const absolutePath = resolveAbsolutePath(dirPath);
  const files: string[] = [];

  async function traverse(currentPath: string): Promise<void> {
    const entries = await fs.promises.readdir(currentPath, { withFileTypes: true });

    for (const entry of entries) {
      const entryPath = path.join(currentPath, entry.name);

      if (entry.isFile()) {
        files.push(entryPath);
      } else if (entry.isDirectory() && recursive) {
        await traverse(entryPath);
      }
    }
  }

  await traverse(absolutePath);
  return files;
}

export async function findFiles(pattern: string, basePath?: string): Promise<string[]> {
  const searchPath = basePath || process.cwd();
  const files = await listFiles(searchPath, true);

  const regexPattern = pattern
    .replace(/\./g, '\\.')
    .replace(/\*\*/g, '{{GLOBSTAR}}')
    .replace(/\*/g, '[^/]*')
    .replace(/{{GLOBSTAR}}/g, '.*');

  const regex = new RegExp(`^${regexPattern}$`);

  return files.filter((file) => {
    const relativePath = path.relative(searchPath, file);
    return regex.test(relativePath);
  });
}

export async function copyFile(source: string, destination: string): Promise<void> {
  const srcPath = resolveAbsolutePath(source);
  const destPath = resolveAbsolutePath(destination);

  const destDir = path.dirname(destPath);
  await io.mkdirP(destDir);

  await io.cp(srcPath, destPath);
  logger.debug(`Copied ${srcPath} to ${destPath}`);
}

export async function deleteFile(filePath: string): Promise<void> {
  const absolutePath = resolveAbsolutePath(filePath);

  try {
    await fs.promises.unlink(absolutePath);
    logger.debug(`Deleted ${absolutePath}`);
  } catch (error) {
    
    if ((error as NodeJS.ErrnoException).code !== 'ENOENT') {
      throw error;
    }
  }
}

export async function createTempFile(content: string, extension = '.tmp'): Promise<string> {
  const tempDir = process.env.RUNNER_TEMP || '/tmp';
  const filename = `changelog-hub-${Date.now()}-${Math.random().toString(36).substring(7)}${extension}`;
  const filePath = path.join(tempDir, filename);

  await writeFile(filePath, content);
  return filePath;
}

export function getExtension(filePath: string): string {
  const ext = path.extname(filePath);
  return ext.startsWith('.') ? ext.substring(1) : ext;
}

export function getBasename(filePath: string): string {
  return path.basename(filePath, path.extname(filePath));
}

export function resolveAbsolutePath(filePath: string): string {
  if (path.isAbsolute(filePath)) {
    return filePath;
  }

  const basePath = process.env.GITHUB_WORKSPACE || process.cwd();
  return path.resolve(basePath, filePath);
}

export function getRelativePath(absolutePath: string): string {
  const basePath = process.env.GITHUB_WORKSPACE || process.cwd();
  return path.relative(basePath, absolutePath);
}

export async function readJsonFile<T = unknown>(filePath: string): Promise<T> {
  const content = await readFile(filePath);
  try {
    return JSON.parse(content) as T;
  } catch (error) {
    throw new Error(`Failed to parse JSON file '${filePath}': ${error}`);
  }
}

export async function writeJsonFile(
  filePath: string,
  data: unknown,
  indent = 2
): Promise<void> {
  const content = JSON.stringify(data, null, indent);
  await writeFile(filePath, content);
}

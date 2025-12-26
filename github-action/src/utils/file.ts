import * as fs from 'fs';
import * as path from 'path';
import * as io from '@actions/io';
import { Logger } from './logger';

const logger = new Logger('FileUtils');

/**
 * Reads a file and returns its contents as a string.
 *
 * @param filePath - Path to the file to read
 * @returns File contents
 * @throws Error if file cannot be read
 */
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

/**
 * Writes content to a file.
 *
 * @param filePath - Path to the file to write
 * @param content - Content to write
 */
export async function writeFile(filePath: string, content: string): Promise<void> {
  const absolutePath = resolveAbsolutePath(filePath);
  logger.debug(`Writing file: ${absolutePath}`);

  try {
    // Ensure directory exists
    const dir = path.dirname(absolutePath);
    await io.mkdirP(dir);

    await fs.promises.writeFile(absolutePath, content, 'utf-8');
    logger.debug(`Wrote ${content.length} bytes to ${absolutePath}`);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    throw new Error(`Failed to write file '${filePath}': ${message}`);
  }
}

/**
 * Checks if a file exists.
 *
 * @param filePath - Path to check
 * @returns true if file exists
 */
export async function fileExists(filePath: string): Promise<boolean> {
  const absolutePath = resolveAbsolutePath(filePath);

  try {
    const stats = await fs.promises.stat(absolutePath);
    return stats.isFile();
  } catch {
    return false;
  }
}

/**
 * Checks if a directory exists.
 *
 * @param dirPath - Path to check
 * @returns true if directory exists
 */
export async function directoryExists(dirPath: string): Promise<boolean> {
  const absolutePath = resolveAbsolutePath(dirPath);

  try {
    const stats = await fs.promises.stat(absolutePath);
    return stats.isDirectory();
  } catch {
    return false;
  }
}

/**
 * Lists files in a directory.
 *
 * @param dirPath - Directory path
 * @param recursive - Whether to list recursively
 * @returns Array of file paths
 */
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

/**
 * Finds files matching a glob pattern.
 *
 * @param pattern - Glob pattern (simple implementation)
 * @param basePath - Base path for searching
 * @returns Array of matching file paths
 */
export async function findFiles(pattern: string, basePath?: string): Promise<string[]> {
  const searchPath = basePath || process.cwd();
  const files = await listFiles(searchPath, true);

  // Convert glob pattern to regex
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

/**
 * Copies a file.
 *
 * @param source - Source file path
 * @param destination - Destination file path
 */
export async function copyFile(source: string, destination: string): Promise<void> {
  const srcPath = resolveAbsolutePath(source);
  const destPath = resolveAbsolutePath(destination);

  // Ensure destination directory exists
  const destDir = path.dirname(destPath);
  await io.mkdirP(destDir);

  await io.cp(srcPath, destPath);
  logger.debug(`Copied ${srcPath} to ${destPath}`);
}

/**
 * Deletes a file.
 *
 * @param filePath - Path to the file to delete
 */
export async function deleteFile(filePath: string): Promise<void> {
  const absolutePath = resolveAbsolutePath(filePath);

  try {
    await fs.promises.unlink(absolutePath);
    logger.debug(`Deleted ${absolutePath}`);
  } catch (error) {
    // Ignore if file doesn't exist
    if ((error as NodeJS.ErrnoException).code !== 'ENOENT') {
      throw error;
    }
  }
}

/**
 * Creates a temporary file with the given content.
 *
 * @param content - Content to write
 * @param extension - File extension (default: .tmp)
 * @returns Path to the temporary file
 */
export async function createTempFile(content: string, extension = '.tmp'): Promise<string> {
  const tempDir = process.env.RUNNER_TEMP || '/tmp';
  const filename = `changelog-hub-${Date.now()}-${Math.random().toString(36).substring(7)}${extension}`;
  const filePath = path.join(tempDir, filename);

  await writeFile(filePath, content);
  return filePath;
}

/**
 * Gets the file extension.
 *
 * @param filePath - File path
 * @returns File extension without the dot
 */
export function getExtension(filePath: string): string {
  const ext = path.extname(filePath);
  return ext.startsWith('.') ? ext.substring(1) : ext;
}

/**
 * Gets the file name without extension.
 *
 * @param filePath - File path
 * @returns File name without extension
 */
export function getBasename(filePath: string): string {
  return path.basename(filePath, path.extname(filePath));
}

/**
 * Resolves a path to an absolute path.
 *
 * @param filePath - Path to resolve
 * @returns Absolute path
 */
export function resolveAbsolutePath(filePath: string): string {
  if (path.isAbsolute(filePath)) {
    return filePath;
  }

  // Use GITHUB_WORKSPACE if available, otherwise current directory
  const basePath = process.env.GITHUB_WORKSPACE || process.cwd();
  return path.resolve(basePath, filePath);
}

/**
 * Gets the relative path from the workspace.
 *
 * @param absolutePath - Absolute path
 * @returns Relative path from workspace
 */
export function getRelativePath(absolutePath: string): string {
  const basePath = process.env.GITHUB_WORKSPACE || process.cwd();
  return path.relative(basePath, absolutePath);
}

/**
 * Reads a JSON file.
 *
 * @param filePath - Path to the JSON file
 * @returns Parsed JSON content
 */
export async function readJsonFile<T = unknown>(filePath: string): Promise<T> {
  const content = await readFile(filePath);
  try {
    return JSON.parse(content) as T;
  } catch (error) {
    throw new Error(`Failed to parse JSON file '${filePath}': ${error}`);
  }
}

/**
 * Writes a JSON file with pretty formatting.
 *
 * @param filePath - Path to the JSON file
 * @param data - Data to write
 * @param indent - Indentation spaces (default: 2)
 */
export async function writeJsonFile(
  filePath: string,
  data: unknown,
  indent = 2
): Promise<void> {
  const content = JSON.stringify(data, null, indent);
  await writeFile(filePath, content);
}

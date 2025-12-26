import * as fs from 'fs';
import * as path from 'path';

/**
 * Read a file synchronously and return its contents
 * @param filePath - Path to the file
 * @returns File contents as string
 */
export function readFile(filePath: string): string {
  const absolutePath = path.resolve(filePath);
  return fs.readFileSync(absolutePath, 'utf-8');
}

/**
 * Write content to a file synchronously
 * @param filePath - Path to the file
 * @param content - Content to write
 */
export function writeFile(filePath: string, content: string): void {
  const absolutePath = path.resolve(filePath);
  const dir = path.dirname(absolutePath);

  // Create directory if it doesn't exist
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  fs.writeFileSync(absolutePath, content, 'utf-8');
}

/**
 * Check if a file exists
 * @param filePath - Path to check
 * @returns true if file exists
 */
export function fileExists(filePath: string): boolean {
  const absolutePath = path.resolve(filePath);
  return fs.existsSync(absolutePath);
}

/**
 * Check if a path is a directory
 * @param filePath - Path to check
 * @returns true if path is a directory
 */
export function isDirectory(filePath: string): boolean {
  const absolutePath = path.resolve(filePath);
  try {
    return fs.statSync(absolutePath).isDirectory();
  } catch {
    return false;
  }
}

/**
 * Get the file extension (without dot)
 * @param filePath - File path
 * @returns File extension
 */
export function getExtension(filePath: string): string {
  const ext = path.extname(filePath);
  return ext.startsWith('.') ? ext.substring(1).toLowerCase() : ext.toLowerCase();
}

/**
 * Get the file name without extension
 * @param filePath - File path
 * @returns File name without extension
 */
export function getBasename(filePath: string): string {
  return path.basename(filePath, path.extname(filePath));
}

/**
 * List files in a directory
 * @param dirPath - Directory path
 * @param pattern - Optional regex pattern to filter files
 * @returns Array of file paths
 */
export function listFiles(dirPath: string, pattern?: RegExp): string[] {
  const absolutePath = path.resolve(dirPath);
  const files = fs.readdirSync(absolutePath);

  const result = files
    .map(file => path.join(absolutePath, file))
    .filter(file => fs.statSync(file).isFile());

  if (pattern) {
    return result.filter(file => pattern.test(path.basename(file)));
  }

  return result;
}

/**
 * Create a temporary file with the given content
 * @param content - Content to write
 * @param extension - File extension
 * @returns Path to the temporary file
 */
export function createTempFile(content: string, extension = 'tmp'): string {
  const tempDir = process.env.TMPDIR || '/tmp';
  const filename = `changelog-hub-${Date.now()}-${Math.random().toString(36).substring(7)}.${extension}`;
  const filePath = path.join(tempDir, filename);
  writeFile(filePath, content);
  return filePath;
}

/**
 * Delete a file
 * @param filePath - Path to the file to delete
 */
export function deleteFile(filePath: string): void {
  const absolutePath = path.resolve(filePath);
  if (fs.existsSync(absolutePath)) {
    fs.unlinkSync(absolutePath);
  }
}

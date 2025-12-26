import * as fs from 'fs';
import * as path from 'path';

export function readFile(filePath: string): string {
  const absolutePath = path.resolve(filePath);
  return fs.readFileSync(absolutePath, 'utf-8');
}

export function writeFile(filePath: string, content: string): void {
  const absolutePath = path.resolve(filePath);
  const dir = path.dirname(absolutePath);

  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  fs.writeFileSync(absolutePath, content, 'utf-8');
}

export function fileExists(filePath: string): boolean {
  const absolutePath = path.resolve(filePath);
  return fs.existsSync(absolutePath);
}

export function isDirectory(filePath: string): boolean {
  const absolutePath = path.resolve(filePath);
  try {
    return fs.statSync(absolutePath).isDirectory();
  } catch {
    return false;
  }
}

export function getExtension(filePath: string): string {
  const ext = path.extname(filePath);
  return ext.startsWith('.') ? ext.substring(1).toLowerCase() : ext.toLowerCase();
}

export function getBasename(filePath: string): string {
  return path.basename(filePath, path.extname(filePath));
}

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

export function createTempFile(content: string, extension = 'tmp'): string {
  const tempDir = process.env.TMPDIR || '/tmp';
  const filename = `changelog-hub-${Date.now()}-${Math.random().toString(36).substring(7)}.${extension}`;
  const filePath = path.join(tempDir, filename);
  writeFile(filePath, content);
  return filePath;
}

export function deleteFile(filePath: string): void {
  const absolutePath = path.resolve(filePath);
  if (fs.existsSync(absolutePath)) {
    fs.unlinkSync(absolutePath);
  }
}

/**
 * Reads a file and returns its contents as a string.
 *
 * @param filePath - Path to the file to read
 * @returns File contents
 * @throws Error if file cannot be read
 */
export declare function readFile(filePath: string): Promise<string>;
/**
 * Writes content to a file.
 *
 * @param filePath - Path to the file to write
 * @param content - Content to write
 */
export declare function writeFile(filePath: string, content: string): Promise<void>;
/**
 * Checks if a file exists.
 *
 * @param filePath - Path to check
 * @returns true if file exists
 */
export declare function fileExists(filePath: string): Promise<boolean>;
/**
 * Checks if a directory exists.
 *
 * @param dirPath - Path to check
 * @returns true if directory exists
 */
export declare function directoryExists(dirPath: string): Promise<boolean>;
/**
 * Lists files in a directory.
 *
 * @param dirPath - Directory path
 * @param recursive - Whether to list recursively
 * @returns Array of file paths
 */
export declare function listFiles(dirPath: string, recursive?: boolean): Promise<string[]>;
/**
 * Finds files matching a glob pattern.
 *
 * @param pattern - Glob pattern (simple implementation)
 * @param basePath - Base path for searching
 * @returns Array of matching file paths
 */
export declare function findFiles(pattern: string, basePath?: string): Promise<string[]>;
/**
 * Copies a file.
 *
 * @param source - Source file path
 * @param destination - Destination file path
 */
export declare function copyFile(source: string, destination: string): Promise<void>;
/**
 * Deletes a file.
 *
 * @param filePath - Path to the file to delete
 */
export declare function deleteFile(filePath: string): Promise<void>;
/**
 * Creates a temporary file with the given content.
 *
 * @param content - Content to write
 * @param extension - File extension (default: .tmp)
 * @returns Path to the temporary file
 */
export declare function createTempFile(content: string, extension?: string): Promise<string>;
/**
 * Gets the file extension.
 *
 * @param filePath - File path
 * @returns File extension without the dot
 */
export declare function getExtension(filePath: string): string;
/**
 * Gets the file name without extension.
 *
 * @param filePath - File path
 * @returns File name without extension
 */
export declare function getBasename(filePath: string): string;
/**
 * Resolves a path to an absolute path.
 *
 * @param filePath - Path to resolve
 * @returns Absolute path
 */
export declare function resolveAbsolutePath(filePath: string): string;
/**
 * Gets the relative path from the workspace.
 *
 * @param absolutePath - Absolute path
 * @returns Relative path from workspace
 */
export declare function getRelativePath(absolutePath: string): string;
/**
 * Reads a JSON file.
 *
 * @param filePath - Path to the JSON file
 * @returns Parsed JSON content
 */
export declare function readJsonFile<T = unknown>(filePath: string): Promise<T>;
/**
 * Writes a JSON file with pretty formatting.
 *
 * @param filePath - Path to the JSON file
 * @param data - Data to write
 * @param indent - Indentation spaces (default: 2)
 */
export declare function writeJsonFile(filePath: string, data: unknown, indent?: number): Promise<void>;
//# sourceMappingURL=file.d.ts.map
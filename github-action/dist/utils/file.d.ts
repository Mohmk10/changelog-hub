export declare function readFile(filePath: string): Promise<string>;
export declare function writeFile(filePath: string, content: string): Promise<void>;
export declare function fileExists(filePath: string): Promise<boolean>;
export declare function directoryExists(dirPath: string): Promise<boolean>;
export declare function listFiles(dirPath: string, recursive?: boolean): Promise<string[]>;
export declare function findFiles(pattern: string, basePath?: string): Promise<string[]>;
export declare function copyFile(source: string, destination: string): Promise<void>;
export declare function deleteFile(filePath: string): Promise<void>;
export declare function createTempFile(content: string, extension?: string): Promise<string>;
export declare function getExtension(filePath: string): string;
export declare function getBasename(filePath: string): string;
export declare function resolveAbsolutePath(filePath: string): string;
export declare function getRelativePath(absolutePath: string): string;
export declare function readJsonFile<T = unknown>(filePath: string): Promise<T>;
export declare function writeJsonFile(filePath: string, data: unknown, indent?: number): Promise<void>;
//# sourceMappingURL=file.d.ts.map
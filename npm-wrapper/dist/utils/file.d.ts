export declare function readFile(filePath: string): string;
export declare function writeFile(filePath: string, content: string): void;
export declare function fileExists(filePath: string): boolean;
export declare function isDirectory(filePath: string): boolean;
export declare function getExtension(filePath: string): string;
export declare function getBasename(filePath: string): string;
export declare function listFiles(dirPath: string, pattern?: RegExp): string[];
export declare function createTempFile(content: string, extension?: string): string;
export declare function deleteFile(filePath: string): void;
//# sourceMappingURL=file.d.ts.map
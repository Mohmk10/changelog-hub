export declare class Logger {
    private verbose;
    private quiet;
    constructor(verbose?: boolean, quiet?: boolean);
    info(message: string): void;
    success(message: string): void;
    warn(message: string): void;
    error(message: string): void;
    debug(message: string): void;
    newline(): void;
    header(message: string): void;
    section(message: string): void;
    keyValue(key: string, value: string | number, color?: 'green' | 'red' | 'yellow' | 'blue'): void;
    bullet(message: string, indent?: number): void;
    colored(message: string, color: 'green' | 'red' | 'yellow' | 'blue' | 'gray' | 'cyan' | 'magenta'): void;
    tableRow(columns: string[], widths: number[]): void;
    divider(char?: string, length?: number): void;
}
export declare function createLogger(verbose?: boolean, quiet?: boolean): Logger;
export declare const logger: Logger;
//# sourceMappingURL=logger.d.ts.map
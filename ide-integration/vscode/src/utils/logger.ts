import * as vscode from 'vscode';

let outputChannel: vscode.OutputChannel;

/**
 * Initialize the output channel
 */
function getOutputChannel(): vscode.OutputChannel {
  if (!outputChannel) {
    outputChannel = vscode.window.createOutputChannel('Changelog Hub');
  }
  return outputChannel;
}

/**
 * Logger utility for the extension
 */
export class Logger {
  private static formatMessage(level: string, message: string): string {
    const timestamp = new Date().toISOString();
    return `[${timestamp}] [${level}] ${message}`;
  }

  static info(message: string): void {
    getOutputChannel().appendLine(this.formatMessage('INFO', message));
  }

  static warn(message: string): void {
    getOutputChannel().appendLine(this.formatMessage('WARN', message));
  }

  static error(message: string): void {
    getOutputChannel().appendLine(this.formatMessage('ERROR', message));
  }

  static debug(message: string): void {
    getOutputChannel().appendLine(this.formatMessage('DEBUG', message));
  }

  static show(): void {
    getOutputChannel().show();
  }

  static clear(): void {
    getOutputChannel().clear();
  }
}

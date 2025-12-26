import * as core from '@actions/core';
import { run } from './action';

/**
 * Main entry point for the GitHub Action.
 * This function is called when the action is executed.
 */
async function main(): Promise<void> {
  try {
    core.info('Starting Changelog Hub Action...');
    await run();
    core.info('Changelog Hub Action completed successfully');
  } catch (error) {
    if (error instanceof Error) {
      core.setFailed(`Action failed: ${error.message}`);
      if (error.stack) {
        core.debug(error.stack);
      }
    } else {
      core.setFailed('Action failed with an unknown error');
    }
  }
}

main();

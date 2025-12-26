import * as assert from 'assert';
import * as vscode from 'vscode';

suite('Extension Test Suite', () => {
  vscode.window.showInformationMessage('Start all tests.');

  test('Extension should be present', () => {
    const extension = vscode.extensions.getExtension('mohmk10.changelog-hub');
    assert.ok(extension, 'Extension should be present');
  });

  test('Extension should activate', async () => {
    const extension = vscode.extensions.getExtension('mohmk10.changelog-hub');
    if (extension) {
      await extension.activate();
      assert.ok(extension.isActive, 'Extension should be active');
    }
  });

  test('Commands should be registered', async () => {
    const commands = await vscode.commands.getCommands(true);

    assert.ok(
      commands.includes('changelogHub.compare'),
      'Compare command should be registered'
    );
    assert.ok(
      commands.includes('changelogHub.compareWithGit'),
      'Compare with Git command should be registered'
    );
    assert.ok(
      commands.includes('changelogHub.analyze'),
      'Analyze command should be registered'
    );
    assert.ok(
      commands.includes('changelogHub.validate'),
      'Validate command should be registered'
    );
    assert.ok(
      commands.includes('changelogHub.generateChangelog'),
      'Generate changelog command should be registered'
    );
  });

  test('Configuration should have defaults', () => {
    const config = vscode.workspace.getConfiguration('changelogHub');

    assert.strictEqual(
      config.get('defaultFormat'),
      'markdown',
      'Default format should be markdown'
    );
    assert.strictEqual(
      config.get('autoDetectSpecs'),
      true,
      'Auto detect specs should be true'
    );
    assert.strictEqual(
      config.get('showInlineWarnings'),
      true,
      'Show inline warnings should be true'
    );
    assert.strictEqual(
      config.get('baseRef'),
      'main',
      'Base ref should be main'
    );
  });
});

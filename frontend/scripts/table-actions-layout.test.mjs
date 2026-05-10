import assert from 'node:assert/strict';
import { readdir, readFile } from 'node:fs/promises';

const srcRoot = new URL('../src/', import.meta.url);
const cssSource = await readFile(new URL('../src/shared/styles/global.css', import.meta.url), 'utf8');

async function collectTsxFiles(dirUrl) {
  const entries = await readdir(dirUrl, { withFileTypes: true });
  const files = await Promise.all(entries.map((entry) => {
    const childUrl = new URL(`${entry.name}${entry.isDirectory() ? '/' : ''}`, dirUrl);
    return entry.isDirectory() ? collectTsxFiles(childUrl) : entry.name.endsWith('.tsx') ? [childUrl] : [];
  }));
  return files.flat();
}

const offenders = [];
for (const fileUrl of await collectTsxFiles(srcRoot)) {
  const source = await readFile(fileUrl, 'utf8');
  const matches = source.matchAll(/<td[^>]*className=["']([^"']*)["'][^>]*>/g);
  for (const match of matches) {
    if (match[1].split(/\s+/).includes('actions')) {
      offenders.push(`${fileUrl.pathname.replace(srcRoot.pathname, 'src/')}: ${match[0]}`);
    }
  }
}

assert.deepEqual(offenders, []);
assert.match(cssSource, /\.table-actions-cell \{[^}]*min-width: 172px;/);
assert.match(cssSource, /\.table-actions \{[^}]*min-height: 38px;/);
assert.match(cssSource, /\.table-actions-placeholder \{[^}]*color: var\(--text-muted\);/);

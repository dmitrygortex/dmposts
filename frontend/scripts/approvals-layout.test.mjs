import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';

const pageSource = await readFile(new URL('../src/pages/approvals/ApprovalsPage.tsx', import.meta.url), 'utf8');
const cssSource = await readFile(new URL('../src/shared/styles/global.css', import.meta.url), 'utf8');

assert.match(pageSource, /className="approvals-table"/);
assert.doesNotMatch(pageSource, /<td className="actions">/);
assert.match(pageSource, /className="approval-actions-cell"/);
assert.match(pageSource, /className="actions approval-actions"/);

assert.match(cssSource, /\.approvals-table th,\s*\n\.approvals-table td \{[^}]*vertical-align: middle;/);
assert.match(cssSource, /\.approval-actions-cell \{[^}]*vertical-align: middle;/);

import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';

const pageSource = await readFile(new URL('../src/pages/content-units/ContentUnitDetailsPage.tsx', import.meta.url), 'utf8');
const cssSource = await readFile(new URL('../src/shared/styles/global.css', import.meta.url), 'utf8');
const tasksSource = pageSource.slice(
  pageSource.indexOf('function TasksTab'),
  pageSource.indexOf('function MediaTab')
);

assert.match(tasksSource, /className="content-unit-tasks-table"/);
assert.doesNotMatch(tasksSource, /<td className="actions">/);
assert.match(tasksSource, /className="content-unit-task-actions-cell"/);
assert.match(tasksSource, /className="actions content-unit-task-actions"/);
assert.match(tasksSource, /nextStatuses\.length === 0/);

assert.match(cssSource, /\.content-unit-tasks-table th,\s*\n\.content-unit-tasks-table td \{[^}]*vertical-align: middle;/);
assert.match(cssSource, /\.content-unit-task-actions-cell \{[^}]*min-width: 172px;/);
assert.match(cssSource, /\.content-unit-task-actions \{[^}]*min-height: 38px;/);

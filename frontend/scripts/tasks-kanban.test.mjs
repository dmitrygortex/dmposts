import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import ts from 'typescript';

const sourcePath = new URL('../src/pages/tasks/tasksKanban.ts', import.meta.url);
const source = await readFile(sourcePath, 'utf8');
const transpiled = ts.transpileModule(source, {
  compilerOptions: {
    module: ts.ModuleKind.ES2022,
    target: ts.ScriptTarget.ES2022
  }
}).outputText;

const moduleUrl = `data:text/javascript;base64,${Buffer.from(transpiled).toString('base64')}`;
const kanban = await import(moduleUrl);

const baseTask = {
  id: 1,
  contentUnitId: 10,
  contentUnitTitle: 'Пост про акцию',
  title: 'Подготовить баннер',
  type: 'DESIGN',
  status: 'TODO',
  priority: 'HIGH',
  assignee: { id: 3, email: 'executor@example.com', fullName: 'Дизайнер', role: 'EXECUTOR', isActive: true },
  overdue: false
};

const tasks = [
  baseTask,
  { ...baseTask, id: 2, title: 'Написать текст', status: 'IN_PROGRESS', priority: 'MEDIUM' },
  { ...baseTask, id: 3, title: 'Проверить макет', status: 'ON_REVIEW', overdue: true },
  { ...baseTask, id: 4, title: 'Опубликовать отчет', status: 'DONE' },
  { ...baseTask, id: 5, title: 'Старый вариант', status: 'CANCELED' }
];

const grouped = kanban.groupTasksByStatus(tasks);
assert.equal(grouped.TODO.length, 1);
assert.equal(grouped.IN_PROGRESS.length, 1);
assert.equal(grouped.ON_REVIEW.length, 1);
assert.equal(grouped.DONE.length, 1);
assert.equal(grouped.CANCELED.length, 1);

assert.deepEqual(kanban.getTaskKanbanColumns().map((column) => column.status), [
  'TODO',
  'IN_PROGRESS',
  'ON_REVIEW',
  'DONE',
  'CANCELED'
]);

assert.deepEqual(kanban.getAvailableTaskTransitions(baseTask, false), ['IN_PROGRESS']);
assert.deepEqual(kanban.getAvailableTaskTransitions(tasks[1], false), ['ON_REVIEW']);
assert.deepEqual(kanban.getAvailableTaskTransitions(tasks[1], true), ['ON_REVIEW', 'CANCELED']);
assert.deepEqual(kanban.getAvailableTaskTransitions(tasks[2], true), ['DONE', 'IN_PROGRESS']);
assert.deepEqual(kanban.getAvailableTaskTransitions(tasks[3], true), []);

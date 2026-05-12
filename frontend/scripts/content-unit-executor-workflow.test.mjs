import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';

const pageSource = await readFile(new URL('../src/pages/content-units/ContentUnitDetailsPage.tsx', import.meta.url), 'utf8');
const apiSource = await readFile(new URL('../src/services/api.ts', import.meta.url), 'utf8');

assert.match(apiSource, /updateBaseText:\s*\(id: number, baseText: string\)/);
assert.match(pageSource, /const isExecutor = user\?\.role === 'EXECUTOR'/);
assert.match(pageSource, /const canManageContent = user\?\.role === 'OWNER' \|\| user\?\.role === 'CONTENT_MANAGER'/);
assert.match(pageSource, /function CopywritingWorkspace/);
assert.match(pageSource, /contentApi\.updateBaseText\(content\.id, baseText\)/);
assert.match(pageSource, /Текст подготовлен и отправлен на проверку/);

const loadRequiredStart = pageSource.indexOf('const loadRequired = async () =>');
const loadManagerStart = pageSource.indexOf('const loadManagerSections = async');
assert.notEqual(loadRequiredStart, -1);
assert.notEqual(loadManagerStart, -1);
const requiredLoadSource = pageSource.slice(loadRequiredStart, loadManagerStart);

assert.match(requiredLoadSource, /const contentData = await contentApi\.get\(contentId\)/);
assert.doesNotMatch(requiredLoadSource, /Promise\.all\(\[/);
assert.match(requiredLoadSource, /taskApi\.list\(\{ contentUnitId: contentId, size: 100 \}\)/);
assert.match(requiredLoadSource, /tasksResult\.status === 'fulfilled'/);
assert.doesNotMatch(requiredLoadSource, /approvalApi\.list/);
assert.doesNotMatch(requiredLoadSource, /publicationApi\.list/);
assert.doesNotMatch(requiredLoadSource, /attemptsByContent/);
assert.doesNotMatch(requiredLoadSource, /mediaApi\.list/);
assert.match(pageSource, /const tabs: \[Tab, string\]\[] = canManageContent/);
assert.match(pageSource, /canManageContent && tab === 'crossposting'/);
assert.match(pageSource, /task\.type === 'COPYWRITING'/);

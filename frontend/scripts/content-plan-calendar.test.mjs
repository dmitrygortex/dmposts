import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import ts from 'typescript';

const sourcePath = new URL('../src/pages/content-plan/contentPlanCalendar.ts', import.meta.url);
const source = await readFile(sourcePath, 'utf8');
const transpiled = ts.transpileModule(source, {
  compilerOptions: {
    module: ts.ModuleKind.ES2022,
    target: ts.ScriptTarget.ES2022
  }
}).outputText;

const moduleUrl = `data:text/javascript;base64,${Buffer.from(transpiled).toString('base64')}`;
const calendar = await import(moduleUrl);

const days = calendar.buildCalendarMonth(2026, 4, '2026-05-01');
assert.equal(days.length, 42);
assert.equal(days[0].key, '2026-04-27');
assert.equal(days[4].key, '2026-05-01');
assert.equal(days[4].isToday, true);
assert.equal(days[4].isCurrentMonth, true);
assert.equal(days.at(-1).key, '2026-06-07');

assert.deepEqual(calendar.getVisibleRange(days), {
  from: '2026-04-27T00:00:00',
  to: '2026-06-07T23:59:59'
});

assert.equal(calendar.getMoscowDateKey('2026-05-01T18:30:00'), '2026-05-01');
assert.equal(calendar.getMoscowTimeLabel('2026-05-01T18:30:00'), '18:30');

const variants = [
  {
    id: 1,
    contentUnitId: 10,
    contentUnitTitle: 'Telegram announcement',
    platform: 'TELEGRAM',
    adaptedText: 'Short Telegram text',
    scheduledAt: '2026-05-01T18:30:00',
    status: 'SCHEDULED'
  },
  {
    id: 2,
    contentUnitId: 10,
    contentUnitTitle: 'TenChat manual post',
    platform: 'TENCHAT',
    scheduledAt: '2026-05-01T19:00:00',
    status: 'MANUAL_REQUIRED'
  },
  {
    id: 3,
    contentUnitId: 11,
    contentUnitTitle: 'No schedule',
    platform: 'VK',
    status: 'READY'
  }
];

const grouped = calendar.groupVariantsByDate(variants);
assert.equal(grouped['2026-05-01'].length, 2);
assert.equal(grouped['2026-05-02'], undefined);

assert.equal(calendar.getVariantTargetPath(variants[0]), '/content-units/10');
assert.equal(calendar.getVariantTargetPath(variants[1]), '/manual-publication/2');
assert.equal(calendar.getVariantPreview(variants[0], 12), 'Short Teleg…');
assert.equal(calendar.getVariantPreview(variants[1], 16), 'TenChat manual…');

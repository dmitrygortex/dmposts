import type { PublicationVariant } from '../../shared/types/domain';

export const MOSCOW_TIME_ZONE = 'Europe/Moscow';

export interface CalendarMonthIdentity {
  year: number;
  month: number;
}

export interface CalendarDay {
  key: string;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  ariaLabel: string;
}

const moscowDateFormatter = new Intl.DateTimeFormat('ru-RU', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  timeZone: MOSCOW_TIME_ZONE
});

const moscowMonthFormatter = new Intl.DateTimeFormat('ru-RU', {
  month: 'long',
  year: 'numeric',
  timeZone: MOSCOW_TIME_ZONE
});

const moscowTimeFormatter = new Intl.DateTimeFormat('ru-RU', {
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
  timeZone: MOSCOW_TIME_ZONE
});

const moscowDateTimeFormatter = new Intl.DateTimeFormat('ru-RU', {
  dateStyle: 'short',
  timeStyle: 'short',
  timeZone: MOSCOW_TIME_ZONE
});

function pad2(value: number) {
  return String(value).padStart(2, '0');
}

function toDateKey(date: Date) {
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
}

function getMoscowKeyFromDate(date: Date) {
  const parts = moscowDateFormatter.formatToParts(date);
  const day = parts.find((part) => part.type === 'day')?.value ?? '01';
  const month = parts.find((part) => part.type === 'month')?.value ?? '01';
  const year = parts.find((part) => part.type === 'year')?.value ?? '1970';
  return `${year}-${month}-${day}`;
}

function parseDateKey(key: string) {
  const [year, month, day] = key.split('-').map(Number);
  return new Date(year, month - 1, day, 12);
}

function hasExplicitTimeZone(value: string) {
  return /(?:z|[+-]\d{2}:\d{2})$/i.test(value);
}

function capitalize(value: string) {
  return value.length > 0 ? value[0].toUpperCase() + value.slice(1) : value;
}

export function getMoscowMonthIdentity(now = new Date()): CalendarMonthIdentity {
  const key = getMoscowKeyFromDate(now);
  const [year, month] = key.split('-').map(Number);
  return { year, month: month - 1 };
}

export function getMoscowTodayKey(now = new Date()) {
  return getMoscowKeyFromDate(now);
}

export function shiftMonth(identity: CalendarMonthIdentity, delta: number): CalendarMonthIdentity {
  const date = new Date(identity.year, identity.month + delta, 1, 12);
  return { year: date.getFullYear(), month: date.getMonth() };
}

export function formatMonthTitle(year: number, month: number) {
  return capitalize(moscowMonthFormatter.format(new Date(Date.UTC(year, month, 15, 12))));
}

export function buildCalendarMonth(year: number, month: number, todayKey = getMoscowTodayKey()): CalendarDay[] {
  const firstDay = new Date(year, month, 1, 12);
  const mondayFirstOffset = (firstDay.getDay() + 6) % 7;
  const start = new Date(year, month, 1 - mondayFirstOffset, 12);

  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start);
    date.setDate(start.getDate() + index);
    const key = toDateKey(date);
    const dayNumber = date.getDate();

    return {
      key,
      dayNumber,
      isCurrentMonth: date.getMonth() === month,
      isToday: key === todayKey,
      ariaLabel: new Intl.DateTimeFormat('ru-RU', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      }).format(date)
    };
  });
}

export function getVisibleRange(days: CalendarDay[]) {
  const first = days[0]?.key;
  const last = days[days.length - 1]?.key;
  return {
    from: `${first}T00:00:00`,
    to: `${last}T23:59:59`
  };
}

export function getMoscowDateKey(value?: string) {
  if (!value) return '';
  if (/^\d{4}-\d{2}-\d{2}T/.test(value) && !hasExplicitTimeZone(value)) {
    return value.slice(0, 10);
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : getMoscowKeyFromDate(date);
}

export function getMoscowTimeLabel(value?: string) {
  if (!value) return '—';
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(value) && !hasExplicitTimeZone(value)) {
    return value.slice(11, 16);
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : moscowTimeFormatter.format(date);
}

export function formatMoscowDateTime(value?: string) {
  if (!value) return '—';
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(value) && !hasExplicitTimeZone(value)) {
    const date = parseDateKey(value.slice(0, 10));
    return `${moscowDateFormatter.format(date)}, ${value.slice(11, 16)}`;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : moscowDateTimeFormatter.format(date);
}

export function groupVariantsByDate(variants: PublicationVariant[]) {
  return variants.reduce<Record<string, PublicationVariant[]>>((groups, variant) => {
    const key = getMoscowDateKey(variant.scheduledAt);
    if (!key) return groups;
    groups[key] = [...(groups[key] ?? []), variant].sort(compareVariantsBySchedule);
    return groups;
  }, {});
}

export function compareVariantsBySchedule(left: PublicationVariant, right: PublicationVariant) {
  const leftSchedule = left.scheduledAt ?? '';
  const rightSchedule = right.scheduledAt ?? '';
  if (leftSchedule !== rightSchedule) return leftSchedule.localeCompare(rightSchedule);
  return `${left.platform}-${left.id}`.localeCompare(`${right.platform}-${right.id}`);
}

export function getVariantTargetPath(variant: PublicationVariant) {
  return variant.status === 'MANUAL_REQUIRED'
    ? `/manual-publication/${variant.id}`
    : `/content-units/${variant.contentUnitId}`;
}

export function getVariantPreview(variant: PublicationVariant, maxLength = 58) {
  const source = (variant.adaptedText?.trim() || variant.contentUnitTitle).replace(/\s+/g, ' ');
  return source.length <= maxLength ? source : `${source.slice(0, Math.max(0, maxLength - 1)).trimEnd()}…`;
}

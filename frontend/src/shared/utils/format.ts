export function formatDateTime(value?: string) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('ru-RU', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(new Date(value));
}

export function toInputDateTime(value?: string) {
  if (!value) return '';
  return value.slice(0, 16);
}

export function optionalNumber(value: string) {
  return value ? Number(value) : undefined;
}

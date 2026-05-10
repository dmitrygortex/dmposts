import { useEffect, useMemo, useState } from 'react';
import { CalendarDays, ChevronLeft, ChevronRight, List, RotateCcw } from 'lucide-react';
import { Link } from 'react-router-dom';
import { publicationApi } from '../../services/api';
import { Platform, platforms, PublicationVariant, PublicationVariantStatus } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { ErrorState, Field, LoadingState } from '../../components/forms/FormControls';
import {
  buildCalendarMonth,
  compareVariantsBySchedule,
  formatMonthTitle,
  formatMoscowDateTime,
  getMoscowMonthIdentity,
  getMoscowTimeLabel,
  getVariantPreview,
  getVariantTargetPath,
  getVisibleRange,
  groupVariantsByDate,
  shiftMonth
} from './contentPlanCalendar';

type ViewMode = 'calendar' | 'list';

const statusFilterOptions: PublicationVariantStatus[] = [
  'SCHEDULED',
  'MANUAL_REQUIRED',
  'PUBLISHED',
  'MANUAL_COMPLETED'
];

const weekdays = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
const visibleEventsPerDay = 3;

export function ContentPlanPage() {
  const [variants, setVariants] = useState<PublicationVariant[]>([]);
  const [totalItems, setTotalItems] = useState(0);
  const [month, setMonth] = useState(() => getMoscowMonthIdentity());
  const [platformFilter, setPlatformFilter] = useState<Platform | ''>('');
  const [statusFilter, setStatusFilter] = useState<PublicationVariantStatus | ''>('');
  const [viewMode, setViewMode] = useState<ViewMode>('calendar');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const days = useMemo(() => buildCalendarMonth(month.year, month.month), [month]);
  const range = useMemo(() => getVisibleRange(days), [days]);
  const variantsByDate = useMemo(() => groupVariantsByDate(variants), [variants]);
  const monthTitle = useMemo(() => formatMonthTitle(month.year, month.month), [month]);

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    setError('');
    publicationApi.list({
      from: range.from,
      to: range.to,
      platform: platformFilter || undefined,
      status: statusFilter || undefined,
      size: 500
    })
      .then((page) => {
        if (!mounted) return;
        setVariants([...page.items].sort(compareVariantsBySchedule));
        setTotalItems(page.totalItems);
      })
      .catch((e) => {
        if (mounted) setError(e.message);
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [platformFilter, range.from, range.to, statusFilter]);

  const goToToday = () => setMonth(getMoscowMonthIdentity());
  const shiftVisibleMonth = (delta: number) => setMonth((current) => shiftMonth(current, delta));

  const publicationCountLabel = totalItems === variants.length
    ? `${variants.length} публикаций`
    : `${variants.length} из ${totalItems} публикаций`;

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Контент-план</h1>
          <p>Месячный календарь публикаций по publication variants. Время отображается в Europe/Moscow.</p>
        </div>
      </div>
      <ErrorState message={error} />
      <section className="panel content-plan-panel">
        <div className="content-plan-toolbar">
          <div className="content-plan-title">
            <CalendarDays size={22} aria-hidden="true" />
            <div>
              <h2>{monthTitle}</h2>
              <span>{publicationCountLabel} в видимом диапазоне</span>
            </div>
          </div>
          <div className="actions">
            <button type="button" onClick={() => shiftVisibleMonth(-1)} aria-label="Предыдущий месяц">
              <ChevronLeft size={16} aria-hidden="true" />
              Назад
            </button>
            <button type="button" onClick={goToToday}>
              <RotateCcw size={16} aria-hidden="true" />
              Сегодня
            </button>
            <button type="button" onClick={() => shiftVisibleMonth(1)} aria-label="Следующий месяц">
              Вперёд
              <ChevronRight size={16} aria-hidden="true" />
            </button>
          </div>
        </div>

        <div className="content-plan-controls">
          <Field label="Платформа">
            <select value={platformFilter} onChange={(event) => setPlatformFilter(event.target.value as Platform | '')}>
              <option value="">Все платформы</option>
              {platforms.map((platform) => <option key={platform} value={platform}>{platform}</option>)}
            </select>
          </Field>
          <Field label="Статус">
            <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as PublicationVariantStatus | '')}>
              <option value="">Все статусы</option>
              {statusFilterOptions.map((status) => <option key={status} value={status}>{status}</option>)}
            </select>
          </Field>
          <div className="content-plan-view-switch" aria-label="Режим отображения">
            <button type="button" className={viewMode === 'calendar' ? 'primary' : ''} aria-pressed={viewMode === 'calendar'} onClick={() => setViewMode('calendar')}>
              <CalendarDays size={16} aria-hidden="true" />
              Календарь
            </button>
            <button type="button" className={viewMode === 'list' ? 'primary' : ''} aria-pressed={viewMode === 'list'} onClick={() => setViewMode('list')}>
              <List size={16} aria-hidden="true" />
              Список
            </button>
          </div>
        </div>

        {loading ? <LoadingState /> : viewMode === 'calendar' ? (
          <div className="content-calendar-scroll">
            <div className="content-calendar-weekdays" aria-hidden="true">
              {weekdays.map((weekday) => <span key={weekday}>{weekday}</span>)}
            </div>
            <div className="content-calendar-grid" role="grid" aria-label={`Контент-план: ${monthTitle}`}>
              {days.map((day) => {
                const dayVariants = variantsByDate[day.key] ?? [];
                const visibleVariants = dayVariants.slice(0, visibleEventsPerDay);
                const hiddenCount = dayVariants.length - visibleVariants.length;

                return (
                  <div
                    key={day.key}
                    className={`content-calendar-day ${day.isCurrentMonth ? '' : 'other-month'} ${day.isToday ? 'today' : ''}`}
                    role="gridcell"
                    aria-label={day.ariaLabel}
                  >
                    <div className="content-calendar-day-head">
                      <span className="content-calendar-day-number">{day.dayNumber}</span>
                      {day.isToday && <span className="content-calendar-today">Сегодня</span>}
                    </div>
                    <div className="content-calendar-events">
                      {visibleVariants.map((variant) => (
                        <Link
                          key={variant.id}
                          className={`content-calendar-event event-${variant.status.toLowerCase().replaceAll('_', '-')}`}
                          to={getVariantTargetPath(variant)}
                          title={`${variant.platform}: ${variant.contentUnitTitle}`}
                        >
                          <span className="content-calendar-event-meta">
                            <time dateTime={variant.scheduledAt}>{getMoscowTimeLabel(variant.scheduledAt)}</time>
                            <span>{variant.platform}</span>
                          </span>
                          <strong>{getVariantPreview(variant)}</strong>
                          <Badge value={variant.status} />
                        </Link>
                      ))}
                      {hiddenCount > 0 && <span className="content-calendar-more">+{hiddenCount}</span>}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ) : (
          <PublicationList variants={variants} />
        )}
      </section>
    </>
  );
}

function PublicationList({ variants }: { variants: PublicationVariant[] }) {
  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Дата и время</th><th>Платформа</th><th>Материал</th><th>Статус</th><th>Действие</th></tr></thead>
        <tbody>{variants.map((variant) => (
          <tr key={variant.id}>
            <td>{formatMoscowDateTime(variant.scheduledAt)}</td>
            <td>{variant.platform}</td>
            <td>
              <strong>{variant.contentUnitTitle}</strong>
              {variant.adaptedText && <><br /><small>{getVariantPreview(variant, 96)}</small></>}
            </td>
            <td><Badge value={variant.status} /></td>
            <td><Link className="button" to={getVariantTargetPath(variant)}>Открыть</Link></td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}

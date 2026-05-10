import { useEffect, useState } from 'react';
import { analyticsApi, notificationApi } from '../../services/api';
import { AnalyticsSummary, NotificationItem } from '../../shared/types/domain';
import { ErrorState, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime } from '../../shared/utils/format';

export function DashboardPage() {
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([analyticsApi.summary(), notificationApi.list()])
      .then(([summaryData, notificationsData]) => {
        setSummary(summaryData);
        setNotifications(notificationsData.items);
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Ошибка загрузки dashboard'));
  }, []);

  if (error) return <ErrorState message={error} />;
  if (!summary) return <LoadingState />;

  const cards = [
    ['Контент в работе', summary.content.inProgress ?? 0],
    ['На согласовании', summary.content.onReview ?? 0],
    ['Запланировано', summary.publications.scheduled ?? 0],
    ['Опубликовано', summary.publications.published ?? 0],
    ['Manual completed', summary.publications.manualCompleted ?? 0],
    ['Manual required', summary.publications.manualRequired ?? 0],
    ['Просроченные задачи', summary.tasks.overdue ?? 0]
  ];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p>Операционная сводка по производству, согласованию и публикациям.</p>
        </div>
      </div>
      <div className="grid three">
        {cards.map(([label, value]) => (
          <section className="panel" key={label}>
            <h2>{label}</h2>
            <strong style={{ fontSize: 34 }}>{value}</strong>
          </section>
        ))}
      </div>
      <section className="panel">
        <h2>Уведомления</h2>
        {notifications.length === 0 ? (
          <p className="state">Новых уведомлений нет.</p>
        ) : (
          <div className="grid">
            {notifications.map((item) => (
              <div className="state" key={item.id}>
                <strong>{item.message}</strong>
                <span>{formatDateTime(item.createdAt)}</span>
              </div>
            ))}
          </div>
        )}
      </section>
    </>
  );
}

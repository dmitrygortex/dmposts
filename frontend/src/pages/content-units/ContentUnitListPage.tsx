import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { contentApi } from '../../services/api';
import { ContentUnit } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { EmptyState, ErrorState, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime } from '../../shared/utils/format';

export function ContentUnitListPage() {
  const [items, setItems] = useState<ContentUnit[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    contentApi.list({ size: 100 })
      .then((page) => setItems(page.items))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Контент-единицы</h1>
          <p>CRM-список центральных материалов с задачами, согласованием и публикационными версиями.</p>
        </div>
        <Link className="button primary" to="/content-units/new"><Plus size={16} />Создать</Link>
      </div>
      <ErrorState message={error} />
      {loading ? <LoadingState /> : items.length === 0 ? <EmptyState title="Контента пока нет" text="Создайте первую content unit для demo flow." /> : (
        <section className="panel table-wrap">
          <table>
            <thead><tr><th>Название</th><th>Статус</th><th>Тип</th><th>Ответственный</th><th>План</th><th>Задачи</th><th>Версии</th></tr></thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td><Link to={`/content-units/${item.id}`}><strong>{item.title}</strong></Link></td>
                  <td><Badge value={item.status} /></td>
                  <td>{item.contentType}</td>
                  <td>{item.responsibleUser?.fullName ?? '—'}</td>
                  <td>{formatDateTime(item.plannedPublishAt)}</td>
                  <td>{item.tasksCount}</td>
                  <td>{item.variantsCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </>
  );
}

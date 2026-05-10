import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { publicationApi } from '../../services/api';
import { PublicationVariant } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { EmptyState, ErrorState, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime } from '../../shared/utils/format';

export function ManualPublicationListPage() {
  const [variants, setVariants] = useState<PublicationVariant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    publicationApi.list({ status: 'MANUAL_REQUIRED', size: 100 })
      .then((page) => setVariants(page.items))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Ручная публикация</h1>
          <p>Очередь платформ, где автоматическая публикация недоступна или завершилась ошибкой.</p>
        </div>
      </div>
      <ErrorState message={error} />
      {loading ? <LoadingState /> : (
        variants.length === 0
          ? <section className="panel"><EmptyState title="Очередь ручной публикации пуста" /></section>
          : (
            <section className="panel table-wrap">
              <table>
                <thead><tr><th>Материал</th><th>Платформа</th><th>Статус</th><th>План</th><th>Ошибка</th><th></th></tr></thead>
                <tbody>{variants.map((variant) => (
                  <tr key={variant.id}>
                    <td>{variant.contentUnitTitle}</td>
                    <td>{variant.platform}</td>
                    <td><Badge value={variant.status} /></td>
                    <td>{formatDateTime(variant.scheduledAt)}</td>
                    <td>{variant.errorMessage}</td>
                    <td><Link className="button primary" to={`/manual-publication/${variant.id}`}>Открыть</Link></td>
                  </tr>
                ))}</tbody>
              </table>
            </section>
          )
      )}
    </>
  );
}

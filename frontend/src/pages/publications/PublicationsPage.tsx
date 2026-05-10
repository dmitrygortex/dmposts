import { FormEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ExternalLink, Play, RefreshCcw, Save } from 'lucide-react';
import { publicationApi } from '../../services/api';
import { PublicationVariant, PublicationVariantStatus } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { ErrorState, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime } from '../../shared/utils/format';

const publishableStatuses: PublicationVariantStatus[] = ['READY', 'SCHEDULED'];
const finalStatuses: PublicationVariantStatus[] = ['PUBLISHED', 'MANUAL_COMPLETED'];

export function PublicationsPage() {
  const [variants, setVariants] = useState<PublicationVariant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [linkDrafts, setLinkDrafts] = useState<Record<number, string>>({});
  const [savingLinkId, setSavingLinkId] = useState<number | null>(null);

  const load = () => {
    setLoading(true);
    setError('');
    publicationApi.list({ size: 100 }).then((page) => setVariants(page.items)).catch((e) => setError(e.message)).finally(() => setLoading(false));
  };
  useEffect(load, []);

  const saveExternalPostUrl = async (event: FormEvent, variant: PublicationVariant) => {
    event.preventDefault();
    const externalPostUrl = (linkDrafts[variant.id] ?? variant.externalPostUrl ?? '').trim();
    if (!externalPostUrl) {
      setError('External post URL is required');
      return;
    }
    setError('');
    setSavingLinkId(variant.id);
    try {
      const updated = await publicationApi.update(variant.id, { externalPostUrl });
      setVariants((current) => current.map((item) => item.id === updated.id ? updated : item));
      setLinkDrafts((current) => ({ ...current, [variant.id]: updated.externalPostUrl ?? '' }));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'External post URL save failed');
    } finally {
      setSavingLinkId(null);
    }
  };

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Публикации</h1>
          <p>Операционный список всех publication variants по платформам.</p>
        </div>
      </div>
      <ErrorState message={error} />
      {loading ? <LoadingState /> : (
        <section className="panel table-wrap">
          <table>
            <thead><tr><th>Материал</th><th>Платформа</th><th>Статус</th><th>План</th><th>Ошибка</th><th>Действия</th></tr></thead>
            <tbody>{variants.map((variant) => (
              <tr key={variant.id}>
                <td>{variant.contentUnitTitle}</td>
                <td>{variant.platform}</td>
                <td><Badge value={variant.status} /></td>
                <td>{formatDateTime(variant.scheduledAt)}</td>
                <td>{variant.errorMessage ?? '—'}</td>
                <td className="table-actions-cell">
                  <div className="actions table-actions">
                    {publishableStatuses.includes(variant.status) && (
                      <button onClick={() => publicationApi.publishNow(variant.id).then(load).catch((e) => setError(e.message))}><Play size={16} />Publish</button>
                    )}
                    {variant.status === 'MANUAL_REQUIRED' && <button onClick={() => publicationApi.retry(variant.id).then(load).catch((e) => setError(e.message))}><RefreshCcw size={16} />Retry</button>}
                    {variant.status === 'MANUAL_REQUIRED' && <Link className="button" to={`/manual-publication/${variant.id}`}>Manual</Link>}
                    {finalStatuses.includes(variant.status) && variant.externalPostUrl && (
                      <a className="button" href={variant.externalPostUrl} target="_blank" rel="noreferrer"><ExternalLink size={16} />Открыть публикацию</a>
                    )}
                    {finalStatuses.includes(variant.status) && !variant.externalPostUrl && (
                      <form className="inline-link-form" onSubmit={(event) => saveExternalPostUrl(event, variant)}>
                        <input
                          value={linkDrafts[variant.id] ?? ''}
                          onChange={(event) => setLinkDrafts((current) => ({ ...current, [variant.id]: event.target.value }))}
                          placeholder="https://vk.com/wall-123_456"
                          aria-label={`External post URL for ${variant.platform}`}
                          required
                        />
                        <button type="submit" disabled={savingLinkId === variant.id}>
                          <Save size={16} />{savingLinkId === variant.id ? 'Saving...' : 'Save link'}
                        </button>
                      </form>
                    )}
                    {!publishableStatuses.includes(variant.status) && variant.status !== 'MANUAL_REQUIRED' && !finalStatuses.includes(variant.status) && <span className="table-actions-placeholder">—</span>}
                  </div>
                </td>
              </tr>
            ))}</tbody>
          </table>
        </section>
      )}
    </>
  );
}

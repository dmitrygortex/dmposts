import { useEffect, useState } from 'react';
import { Download } from 'lucide-react';
import { mediaApi } from '../../services/api';
import { MediaFile } from '../../shared/types/domain';
import { ErrorState, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime } from '../../shared/utils/format';

export function MediaPage() {
  const [media, setMedia] = useState<MediaFile[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    mediaApi.list().then(setMedia).catch((e) => setError(e.message)).finally(() => setLoading(false));
  }, []);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Медиафайлы</h1>
          <p>Файлы хранятся локально, metadata находится в PostgreSQL.</p>
        </div>
      </div>
      <ErrorState message={error} />
      {loading ? <LoadingState /> : (
        <section className="panel table-wrap">
          <table>
            <thead><tr><th>Файл</th><th>Content</th><th>Task</th><th>MIME</th><th>Размер</th><th>Загружен</th><th></th></tr></thead>
            <tbody>{media.map((file) => (
              <tr key={file.id}>
                <td>{file.originalName}</td>
                <td>{file.contentUnitId}</td>
                <td>{file.taskId ?? '—'}</td>
                <td>{file.mimeType}</td>
                <td>{Math.round(file.size / 1024)} KB</td>
                <td>{formatDateTime(file.uploadedAt)}</td>
                <td><a className="button" href={file.downloadUrl} target="_blank" rel="noreferrer"><Download size={16} />Download</a></td>
              </tr>
            ))}</tbody>
          </table>
        </section>
      )}
    </>
  );
}

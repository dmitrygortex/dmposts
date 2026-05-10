import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Copy, Download, ExternalLink } from 'lucide-react';
import { mediaApi, publicationApi } from '../../services/api';
import { ManualPublicationDetails, MediaFile } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { ErrorState, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime } from '../../shared/utils/format';

export function ManualPublicationDetailsPage() {
  const { variantId } = useParams();
  const navigate = useNavigate();
  const [details, setDetails] = useState<ManualPublicationDetails | null>(null);
  const [externalPostUrl, setExternalPostUrl] = useState('');
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState('');
  const [downloadError, setDownloadError] = useState('');
  const [downloadingFileId, setDownloadingFileId] = useState<number | null>(null);

  useEffect(() => {
    publicationApi.manualDetails(Number(variantId))
      .then((data) => {
        setDetails(data);
        setExternalPostUrl(data.variant.externalPostUrl ?? '');
      })
      .catch((e) => setError(e.message));
  }, [variantId]);

  const copy = async () => {
    if (!details?.variant.adaptedText) return;
    await navigator.clipboard.writeText(details.variant.adaptedText);
    setCopied(true);
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await publicationApi.manualComplete(Number(variantId), externalPostUrl);
      navigate('/manual-publication');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Manual complete failed');
    }
  };

  const downloadMedia = async (file: MediaFile) => {
    setDownloadError('');
    setDownloadingFileId(file.id);
    try {
      const blob = await mediaApi.download(file.id);
      const objectUrl = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = objectUrl;
      link.download = file.originalName;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(objectUrl);
    } catch (e) {
      setDownloadError(e instanceof Error ? e.message : 'Download failed');
    } finally {
      setDownloadingFileId(null);
    }
  };

  if (error && !details) return <ErrorState message={error} />;
  if (!details) return <LoadingState />;

  return (
    <>
      <div className="page-header">
        <div>
          <h1>{details.variant.platform}: manual publication</h1>
          <p>{details.contentUnit.title}</p>
        </div>
        <Badge value={details.variant.status} />
      </div>
      <ErrorState message={error} />
      <section className="panel grid two">
        <div className="grid">
          <div className="state">Scheduled: {formatDateTime(details.variant.scheduledAt)}</div>
          {details.variant.errorMessage && <ErrorState message={details.variant.errorMessage} />}
          <div className="copy-box">{details.variant.adaptedText}</div>
          <div className="actions">
            <button onClick={copy}><Copy size={16} />{copied ? 'Скопировано' : 'Copy text'}</button>
            {details.platformUrl && <a className="button" href={details.platformUrl} target="_blank" rel="noreferrer"><ExternalLink size={16} />Open platform</a>}
          </div>
        </div>
        <div className="grid">
          <div className="state"><strong>Instruction</strong><br />{details.variant.manualInstruction}</div>
          <div className="state">
            <strong>Media</strong>
            <div className="media-list">
              {details.mediaFiles.length === 0 ? (
                <span>Media files are not attached.</span>
              ) : details.mediaFiles.map((file) => (
                <div className="media-item" key={file.id}>
                  <span>{file.originalName}</span>
                  <button type="button" className="primary" onClick={() => downloadMedia(file)} disabled={downloadingFileId === file.id}>
                    <Download size={16} />{downloadingFileId === file.id ? 'Downloading...' : 'Download'}
                  </button>
                </div>
              ))}
            </div>
          </div>
          <ErrorState message={downloadError} />
          <form className="grid" onSubmit={submit}>
            <label className="field"><span>External post URL</span><input value={externalPostUrl} onChange={(e) => setExternalPostUrl(e.target.value)} placeholder="https://vk.com/wall-123_456" required /></label>
            <button className="primary" type="submit">Отметить как опубликовано вручную</button>
          </form>
        </div>
      </section>
      <Link className="button" to="/manual-publication">Назад к очереди</Link>
    </>
  );
}

import { useEffect, useState } from 'react';
import { useAuth } from '../../app/providers/AuthProvider';
import { approvalApi } from '../../services/api';
import { Approval } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { ErrorState, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime } from '../../shared/utils/format';

export function ApprovalsPage() {
  const { user } = useAuth();
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [rejectComment, setRejectComment] = useState('Нужно исправить текст.');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const canDecideApproval = user?.role === 'OWNER';

  const load = () => approvalApi.list()
    .then(setApprovals)
    .catch((e) => setError(e.message))
    .finally(() => setLoading(false));

  useEffect(() => { void load(); }, []);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Согласование</h1>
          <p>OWNER утверждает или отклоняет материалы. Reject требует комментарий.</p>
        </div>
      </div>
      <ErrorState message={error} />
      {loading ? <LoadingState /> : (
        <section className="panel table-wrap">
          <div className="actions" style={{ marginBottom: 12 }}>
            <input value={rejectComment} onChange={(e) => setRejectComment(e.target.value)} aria-label="Reject comment" />
          </div>
          <table className="approvals-table">
            <thead><tr><th>Материал</th><th>Reviewer</th><th>Статус</th><th>Создано</th><th>Действия</th></tr></thead>
            <tbody>{approvals.map((approval) => (
              <tr key={approval.id}>
                <td>{approval.contentUnit.title}</td>
                <td>{approval.reviewer.fullName}</td>
                <td><Badge value={approval.status} /></td>
                <td>{formatDateTime(approval.createdAt)}</td>
                <td className="approval-actions-cell">
                  <div className="actions approval-actions">
                    {approval.status === 'PENDING' && canDecideApproval && <button onClick={() => approvalApi.approve(approval.id, 'Согласовано').then(load).catch((e) => setError(e.message))}>Approve</button>}
                    {approval.status === 'PENDING' && canDecideApproval && <button className="danger" onClick={() => approvalApi.reject(approval.id, rejectComment).then(load).catch((e) => setError(e.message))}>Reject</button>}
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

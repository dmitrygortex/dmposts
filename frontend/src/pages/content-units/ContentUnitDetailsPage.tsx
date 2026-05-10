import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Copy, Download, ExternalLink, Play, RefreshCcw, Save, Send, Upload } from 'lucide-react';
import { useAuth } from '../../app/providers/AuthProvider';
import { approvalApi, contentApi, mediaApi, publicationApi, taskApi, userApi } from '../../services/api';
import {
  Approval,
  ContentUnit,
  MediaFile,
  Platform,
  platforms,
  PublicationAttempt,
  PublicationVariant,
  Task,
  TaskPriority,
  taskPriorities,
  TaskStatus,
  TaskType,
  taskTypes,
  User
} from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { EmptyState, ErrorState, Field, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime, optionalNumber, toInputDateTime } from '../../shared/utils/format';

type Tab = 'overview' | 'tasks' | 'media' | 'approvals' | 'crossposting' | 'history';

export function ContentUnitDetailsPage() {
  const { id } = useParams();
  const contentId = Number(id);
  const [tab, setTab] = useState<Tab>('overview');
  const [content, setContent] = useState<ContentUnit | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [media, setMedia] = useState<MediaFile[]>([]);
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [variants, setVariants] = useState<PublicationVariant[]>([]);
  const [attempts, setAttempts] = useState<PublicationAttempt[]>([]);
  const [error, setError] = useState('');

  const load = async () => {
    setError('');
    try {
      const [contentData, usersData, tasksData, mediaData, approvalsData, variantsData, attemptsData] = await Promise.all([
        contentApi.get(contentId),
        userApi.list().catch(() => []),
        taskApi.list({ contentUnitId: contentId, size: 100 }),
        mediaApi.list({ contentUnitId: contentId }).catch(() => []),
        approvalApi.list({ contentUnitId: contentId }),
        publicationApi.list({ contentUnitId: contentId, size: 100 }),
        publicationApi.attemptsByContent(contentId).catch(() => [])
      ]);
      setContent(contentData);
      setUsers(usersData);
      setTasks(tasksData.items);
      setMedia(mediaData);
      setApprovals(approvalsData);
      setVariants(variantsData.items);
      setAttempts(attemptsData);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Карточка не загружена');
    }
  };

  useEffect(() => {
    void load();
  }, [contentId]);

  const ownerOptions = users.filter((user) => user.role === 'OWNER');
  const executorOptions = users.filter((user) => user.role !== 'OWNER' || user.isActive);

  if (error) return <ErrorState message={error} />;
  if (!content) return <LoadingState />;

  const tabs: [Tab, string][] = [
    ['overview', 'Overview'],
    ['tasks', 'Tasks'],
    ['media', 'Media'],
    ['approvals', 'Approvals'],
    ['crossposting', 'Crossposting'],
    ['history', 'History']
  ];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>{content.title}</h1>
          <p>{content.description || 'Карточка content unit со связанными задачами, файлами, согласованием и публикациями.'}</p>
        </div>
        <div className="actions"><Badge value={content.status} /><Badge value={content.contentType} /></div>
      </div>
      <div className="tabs">
        {tabs.map(([key, label]) => <button key={key} className={`tab ${tab === key ? 'active' : ''}`} onClick={() => setTab(key)}>{label}</button>)}
      </div>
      {tab === 'overview' && <OverviewTab content={content} users={users} onSaved={load} />}
      {tab === 'tasks' && <TasksTab contentId={contentId} tasks={tasks} users={executorOptions} onChanged={load} />}
      {tab === 'media' && <MediaTab contentId={contentId} media={media} tasks={tasks} onChanged={load} />}
      {tab === 'approvals' && <ApprovalsTab contentId={contentId} approvals={approvals} reviewers={ownerOptions} onChanged={load} />}
      {tab === 'crossposting' && <CrosspostingTab content={content} variants={variants} onChanged={load} />}
      {tab === 'history' && <HistoryTab attempts={attempts} />}
    </>
  );
}

function OverviewTab({ content, users, onSaved }: { content: ContentUnit; users: User[]; onSaved: () => void }) {
  const [form, setForm] = useState({
    title: content.title,
    description: content.description ?? '',
    baseText: content.baseText ?? '',
    contentType: content.contentType,
    responsibleUserId: String(content.responsibleUser?.id ?? ''),
    plannedPublishAt: toInputDateTime(content.plannedPublishAt)
  });
  const [error, setError] = useState('');

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await contentApi.update(content.id, {
        ...form,
        responsibleUserId: optionalNumber(form.responsibleUserId),
        plannedPublishAt: form.plannedPublishAt || undefined
      });
      onSaved();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Изменения не сохранены');
    }
  };

  return (
    <section className="panel">
      <form className="grid two" onSubmit={submit}>
        <ErrorState message={error} />
        <Field label="Название"><input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></Field>
        <Field label="Ответственный">
          <select value={form.responsibleUserId} onChange={(e) => setForm({ ...form, responsibleUserId: e.target.value })}>
            <option value="">Не выбран</option>
            {users.map((user) => <option key={user.id} value={user.id}>{user.fullName}</option>)}
          </select>
        </Field>
        <Field label="План публикации"><input type="datetime-local" value={form.plannedPublishAt} onChange={(e) => setForm({ ...form, plannedPublishAt: e.target.value })} /></Field>
        <div className="state">Создал: {content.createdBy?.fullName ?? '—'} · обновлено {formatDateTime(content.updatedAt)}</div>
        <Field label="Описание"><textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
        <Field label="Базовый текст"><textarea value={form.baseText} onChange={(e) => setForm({ ...form, baseText: e.target.value })} /></Field>
        <button className="primary" type="submit"><Save size={16} />Сохранить</button>
      </form>
    </section>
  );
}

function TasksTab({ contentId, tasks, users, onChanged }: { contentId: number; tasks: Task[]; users: User[]; onChanged: () => void }) {
  const [form, setForm] = useState({ title: 'Подготовить баннер для акции', description: 'Сделать изображение для VK и Telegram', type: 'DESIGN' as TaskType, priority: 'HIGH' as TaskPriority, assigneeId: '', deadline: '' });
  const [error, setError] = useState('');

  const create = async (event: FormEvent) => {
    event.preventDefault();
    if (!form.assigneeId) {
      setError('Выберите исполнителя');
      return;
    }
    try {
      await taskApi.create({ ...form, contentUnitId: contentId, assigneeId: Number(form.assigneeId), deadline: form.deadline || undefined });
      onChanged();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Задача не создана');
    }
  };

  return (
    <>
      <section className="panel">
        <h2>Новая задача</h2>
        <form className="grid three" onSubmit={create}>
          <ErrorState message={error} />
          <Field label="Название"><input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></Field>
          <Field label="Тип"><select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value as TaskType })}>{taskTypes.map((type) => <option key={type}>{type}</option>)}</select></Field>
          <Field label="Приоритет"><select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value as TaskPriority })}>{taskPriorities.map((priority) => <option key={priority}>{priority}</option>)}</select></Field>
          <Field label="Исполнитель"><select value={form.assigneeId} onChange={(e) => setForm({ ...form, assigneeId: e.target.value })}><option value="">Выберите</option>{users.map((user) => <option key={user.id} value={user.id}>{user.fullName} ({user.role})</option>)}</select></Field>
          <Field label="Deadline"><input type="datetime-local" value={form.deadline} onChange={(e) => setForm({ ...form, deadline: e.target.value })} /></Field>
          <Field label="Описание"><textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
          <button className="primary" type="submit">Создать задачу</button>
        </form>
      </section>
      <section className="panel table-wrap">
        <table className="content-unit-tasks-table">
          <thead><tr><th>Задача</th><th>Статус</th><th>Приоритет</th><th>Исполнитель</th><th>Deadline</th><th>Действия</th></tr></thead>
          <tbody>{tasks.map((task) => <TaskRow key={task.id} task={task} onChanged={onChanged} />)}</tbody>
        </table>
      </section>
    </>
  );
}

function TaskRow({ task, onChanged }: { task: Task; onChanged: () => void }) {
  const nextStatuses: TaskStatus[] = task.status === 'TODO' ? ['IN_PROGRESS'] : task.status === 'IN_PROGRESS' ? ['ON_REVIEW', 'CANCELED'] : task.status === 'ON_REVIEW' ? ['DONE', 'IN_PROGRESS'] : [];
  return (
    <tr>
      <td><strong>{task.title}</strong><br /><small>{task.description}</small></td>
      <td><Badge value={task.status} /></td>
      <td><Badge value={task.priority} /></td>
      <td>{task.assignee.fullName}</td>
      <td>{formatDateTime(task.deadline)}</td>
      <td className="content-unit-task-actions-cell">
        <div className="actions content-unit-task-actions">
          {nextStatuses.length === 0 ? (
            <span className="content-unit-task-actions-placeholder">—</span>
          ) : nextStatuses.map((status) => (
            <button key={status} onClick={() => taskApi.changeStatus(task.id, status, status === 'ON_REVIEW' ? 'Готово, файл прикреплён.' : 'Обновлено').then(onChanged)}>
              {status}
            </button>
          ))}
        </div>
      </td>
    </tr>
  );
}

function MediaTab({ contentId, media, tasks, onChanged }: { contentId: number; media: MediaFile[]; tasks: Task[]; onChanged: () => void }) {
  const [taskId, setTaskId] = useState('');
  const [error, setError] = useState('');

  const upload = async (file?: File) => {
    if (!file) return;
    try {
      await mediaApi.upload(file, contentId, optionalNumber(taskId));
      onChanged();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Файл не загружен');
    }
  };

  return (
    <section className="panel">
      <h2>Медиафайлы</h2>
      <div className="actions">
        <select value={taskId} onChange={(e) => setTaskId(e.target.value)}>
          <option value="">Content unit</option>
          {tasks.map((task) => <option key={task.id} value={task.id}>{task.title}</option>)}
        </select>
        <label className="button">
          <Upload size={16} />Загрузить
          <input type="file" hidden onChange={(e) => upload(e.target.files?.[0])} />
        </label>
      </div>
      <ErrorState message={error} />
      <MediaTable media={media} />
    </section>
  );
}

function ApprovalsTab({ contentId, approvals, reviewers, onChanged }: { contentId: number; approvals: Approval[]; reviewers: User[]; onChanged: () => void }) {
  const { user } = useAuth();
  const [reviewerId, setReviewerId] = useState('');
  const [comment, setComment] = useState('Материал готов, прошу согласовать.');
  const [rejectComment, setRejectComment] = useState('Нужно исправить текст.');
  const [error, setError] = useState('');
  const canDecideApproval = user?.role === 'OWNER';

  const submit = async () => {
    if (!reviewerId) {
      setError('Выберите OWNER для согласования');
      return;
    }
    try {
      await approvalApi.submit({ contentUnitId: contentId, reviewerId: Number(reviewerId), comment });
      onChanged();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Не отправлено');
    }
  };

  return (
    <>
      <section className="panel grid three">
        <ErrorState message={error} />
        <Field label="Reviewer"><select value={reviewerId} onChange={(e) => setReviewerId(e.target.value)}><option value="">OWNER</option>{reviewers.map((user) => <option key={user.id} value={user.id}>{user.fullName}</option>)}</select></Field>
        <Field label="Комментарий"><input value={comment} onChange={(e) => setComment(e.target.value)} /></Field>
        <button className="primary" onClick={submit}><Send size={16} />Отправить</button>
      </section>
      <section className="panel table-wrap">
        <Field label="Reject comment"><input value={rejectComment} onChange={(e) => setRejectComment(e.target.value)} /></Field>
        <table>
          <thead><tr><th>ID</th><th>Reviewer</th><th>Статус</th><th>Комментарий</th><th>Действия</th></tr></thead>
          <tbody>{approvals.map((approval) => (
            <tr key={approval.id}>
              <td>{approval.id}</td>
              <td>{approval.reviewer.fullName}</td>
              <td><Badge value={approval.status} /></td>
              <td>{approval.comment}</td>
              <td className="table-actions-cell">
                <div className="actions table-actions">
                  {approval.status === 'PENDING' && canDecideApproval ? (
                    <>
                      <button onClick={() => approvalApi.approve(approval.id, 'Согласовано').then(onChanged)}>Approve</button>
                      <button className="danger" onClick={() => approvalApi.reject(approval.id, rejectComment).then(onChanged)}>Reject</button>
                    </>
                  ) : (
                    <span className="table-actions-placeholder">—</span>
                  )}
                </div>
              </td>
            </tr>
          ))}</tbody>
        </table>
      </section>
    </>
  );
}

function CrosspostingTab({ content, variants, onChanged }: { content: ContentUnit; variants: PublicationVariant[]; onChanged: () => void }) {
  const [selectedPlatforms, setSelectedPlatforms] = useState<Platform[]>(platforms);
  const existing = useMemo(() => new Set(variants.map((variant) => variant.platform)), [variants]);

  const createMissing = async () => {
    await publicationApi.bulkCreate(content.id, selectedPlatforms);
    onChanged();
  };

  return (
    <>
      <section className="panel">
        <h2>Publication variants</h2>
        <div className="actions">
          {platforms.map((platform) => (
            <label key={platform} className="actions">
              <input type="checkbox" checked={selectedPlatforms.includes(platform)} onChange={(e) => setSelectedPlatforms(e.target.checked ? [...selectedPlatforms, platform] : selectedPlatforms.filter((item) => item !== platform))} />
              {platform}
            </label>
          ))}
          <button className="primary" onClick={createMissing}>Создать версии</button>
        </div>
      </section>
      <div className="grid two">
        {platforms.map((platform) => {
          const variant = variants.find((item) => item.platform === platform);
          return variant
            ? <PublicationVariantCard key={variant.id} variant={variant} onChanged={onChanged} />
            : <section className="panel" key={platform}><h2>{platform}</h2><EmptyState title={existing.has(platform) ? 'Версия есть' : 'Версия не создана'} /></section>;
        })}
      </div>
    </>
  );
}

function PublicationVariantCard({ variant, onChanged }: { variant: PublicationVariant; onChanged: () => void }) {
  const [text, setText] = useState(variant.adaptedText ?? '');
  const [scheduledAt, setScheduledAt] = useState(toInputDateTime(variant.scheduledAt));
  const save = () => publicationApi.update(variant.id, { adaptedText: text, scheduledAt: scheduledAt || undefined }).then(onChanged);

  return (
    <section className="panel">
      <div className="page-header">
        <div><h2>{variant.platform}</h2><Badge value={variant.status} /></div>
        {variant.status === 'MANUAL_REQUIRED' && <Link className="button" to={`/manual-publication/${variant.id}`}><ExternalLink size={16} />Manual</Link>}
      </div>
      <div className="grid">
        {variant.errorMessage && <ErrorState message={variant.errorMessage} />}
        <Field label="Adapted text"><textarea value={text} onChange={(e) => setText(e.target.value)} /></Field>
        <Field label="Scheduled at"><input type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} /></Field>
        <div className="actions">
          <button onClick={save}><Save size={16} />Save</button>
          <button onClick={() => publicationApi.schedule(variant.id, scheduledAt).then(onChanged)}>Schedule</button>
          <button className="primary" onClick={() => publicationApi.publishNow(variant.id).then(onChanged)}><Play size={16} />Publish now</button>
          {variant.status === 'MANUAL_REQUIRED' && <button onClick={() => publicationApi.retry(variant.id).then(onChanged)}><RefreshCcw size={16} />Retry</button>}
          <button onClick={() => publicationApi.switchToManual(variant.id, 'Переведено вручную из интерфейса').then(onChanged)}>Switch manual</button>
        </div>
      </div>
    </section>
  );
}

function HistoryTab({ attempts }: { attempts: PublicationAttempt[] }) {
  return (
    <section className="panel table-wrap">
      <table>
        <thead><tr><th>Variant</th><th>Attempt</th><th>Status</th><th>Error</th><th>Created</th></tr></thead>
        <tbody>{attempts.map((attempt) => (
          <tr key={attempt.id}>
            <td>{attempt.publicationVariantId}</td>
            <td>{attempt.attemptNumber}</td>
            <td><Badge value={attempt.status} /></td>
            <td>{attempt.errorMessage ?? '—'}</td>
            <td>{formatDateTime(attempt.createdAt)}</td>
          </tr>
        ))}</tbody>
      </table>
    </section>
  );
}

function MediaTable({ media }: { media: MediaFile[] }) {
  if (media.length === 0) return <EmptyState title="Файлов пока нет" />;
  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Файл</th><th>MIME</th><th>Размер</th><th>Загружен</th><th>Действия</th></tr></thead>
        <tbody>{media.map((file) => (
          <tr key={file.id}>
            <td>{file.originalName}</td>
            <td>{file.mimeType}</td>
            <td>{Math.round(file.size / 1024)} KB</td>
            <td>{formatDateTime(file.uploadedAt)}</td>
            <td><a className="button" href={file.downloadUrl} target="_blank" rel="noreferrer"><Download size={16} />Download</a></td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}

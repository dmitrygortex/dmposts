import { useEffect, useMemo, useState } from 'react';
import { Columns3, List, Upload } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';
import { mediaApi, taskApi } from '../../services/api';
import { Task, TaskStatus } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { ErrorState, LoadingState } from '../../components/forms/FormControls';
import { formatDateTime } from '../../shared/utils/format';
import { getAvailableTaskTransitions, getTaskKanbanColumns, groupTasksByStatus } from './tasksKanban';

type ViewMode = 'list' | 'kanban';

export function TasksPage() {
  const { user } = useAuth();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const canReviewTasks = user?.role === 'OWNER' || user?.role === 'CONTENT_MANAGER';
  const columns = useMemo(() => getTaskKanbanColumns(), []);
  const tasksByStatus = useMemo(() => groupTasksByStatus(tasks), [tasks]);

  const load = () => {
    setLoading(true);
    setError('');
    taskApi.list(user?.role === 'EXECUTOR' ? { mine: true, size: 100 } : { size: 100 })
      .then((page) => setTasks(page.items))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(load, [user?.role]);

  const change = (task: Task, status: TaskStatus) => {
    taskApi.changeStatus(task.id, status, getStatusComment(task, status)).then(load).catch((e) => setError(e.message));
  };

  const upload = (task: Task, file?: File) => {
    if (!file) return;
    mediaApi.upload(file, task.contentUnitId, task.id).then(load).catch((e) => setError(e.message));
  };

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Задачи</h1>
          <p>EXECUTOR видит свои задачи, переводит их в работу, прикрепляет файлы и отправляет на проверку.</p>
        </div>
      </div>
      <ErrorState message={error} />
      <section className="panel tasks-panel">
        <div className="tasks-toolbar">
          <div>
            <h2>Рабочий список</h2>
            <span>{tasks.length} задач загружено</span>
          </div>
          <div className="tasks-view-switch" aria-label="Режим отображения задач">
            <button type="button" className={viewMode === 'list' ? 'primary' : ''} aria-pressed={viewMode === 'list'} onClick={() => setViewMode('list')}>
              <List size={16} aria-hidden="true" />
              Список
            </button>
            <button type="button" className={viewMode === 'kanban' ? 'primary' : ''} aria-pressed={viewMode === 'kanban'} onClick={() => setViewMode('kanban')}>
              <Columns3 size={16} aria-hidden="true" />
              Канбан
            </button>
          </div>
        </div>

        {loading ? <LoadingState /> : tasks.length === 0 ? (
          <div className="state state-empty">
            <strong>Задач нет</strong>
            <span>Когда задачи появятся, они будут доступны в списке и на Kanban-доске.</span>
          </div>
        ) : viewMode === 'list' ? (
          <TaskList tasks={tasks} canReviewTasks={canReviewTasks} onChange={change} onUpload={upload} />
        ) : (
          <div className="tasks-kanban-scroll">
            <div className="tasks-kanban-board" aria-label="Kanban-доска задач">
              {columns.map((column) => {
                const columnTasks = tasksByStatus[column.status] ?? [];
                return (
                  <section key={column.status} className={`tasks-kanban-column status-${column.status.toLowerCase().replaceAll('_', '-')}`} aria-label={column.title}>
                    <div className="tasks-kanban-column-head">
                      <h3>{column.title}</h3>
                      <span>{columnTasks.length}</span>
                    </div>
                    <div className="tasks-kanban-cards">
                      {columnTasks.length === 0 ? (
                        <div className="tasks-kanban-empty">Нет задач</div>
                      ) : columnTasks.map((task) => (
                        <TaskCard key={task.id} task={task} canReviewTasks={canReviewTasks} onChange={change} onUpload={upload} />
                      ))}
                    </div>
                  </section>
                );
              })}
            </div>
          </div>
        )}
      </section>
    </>
  );
}

function TaskList({ tasks, canReviewTasks, onChange, onUpload }: {
  tasks: Task[];
  canReviewTasks: boolean;
  onChange: (task: Task, status: TaskStatus) => void;
  onUpload: (task: Task, file?: File) => void;
}) {
  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Задача</th><th>Content unit</th><th>Исполнитель</th><th>Статус</th><th>Приоритет</th><th>Deadline</th><th>Действия</th></tr></thead>
        <tbody>{tasks.map((task) => (
          <tr key={task.id}>
            <td>
              <strong>{task.title}</strong>
              {task.description && <><br /><small>{task.description}</small></>}
            </td>
            <td><Link className="text-link" to={`/content-units/${task.contentUnitId}`}>{task.contentUnitTitle}</Link></td>
            <td>{task.assignee.fullName}</td>
            <td><Badge value={task.status} /></td>
            <td><Badge value={task.priority} /></td>
            <td>
              <span className={task.overdue ? 'task-overdue-text' : ''}>{formatDateTime(task.deadline)}</span>
              {task.overdue && <><br /><span className="task-overdue-label">Просрочено</span></>}
            </td>
            <td><TaskActions task={task} canReviewTasks={canReviewTasks} onChange={onChange} onUpload={onUpload} /></td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}

function TaskCard({ task, canReviewTasks, onChange, onUpload }: {
  task: Task;
  canReviewTasks: boolean;
  onChange: (task: Task, status: TaskStatus) => void;
  onUpload: (task: Task, file?: File) => void;
}) {
  return (
    <article className={`task-kanban-card priority-${task.priority.toLowerCase()} ${task.overdue ? 'is-overdue' : ''}`}>
      <div className="task-kanban-card-head">
        <h4>{task.title}</h4>
        <Badge value={task.status} />
      </div>
      {task.description && <p>{task.description}</p>}
      <dl className="task-kanban-meta">
        <div>
          <dt>Материал</dt>
          <dd><Link className="text-link" to={`/content-units/${task.contentUnitId}`}>{task.contentUnitTitle}</Link></dd>
        </div>
        <div>
          <dt>Исполнитель</dt>
          <dd>{task.assignee.fullName}</dd>
        </div>
        <div>
          <dt>Приоритет</dt>
          <dd><Badge value={task.priority} /></dd>
        </div>
        <div>
          <dt>Deadline</dt>
          <dd>
            <span className={task.overdue ? 'task-overdue-text' : ''}>{formatDateTime(task.deadline)}</span>
            {task.overdue && <span className="task-overdue-label">Просрочено</span>}
          </dd>
        </div>
      </dl>
      <TaskActions task={task} canReviewTasks={canReviewTasks} onChange={onChange} onUpload={onUpload} compact />
    </article>
  );
}

function TaskActions({ task, canReviewTasks, onChange, onUpload, compact = false }: {
  task: Task;
  canReviewTasks: boolean;
  onChange: (task: Task, status: TaskStatus) => void;
  onUpload: (task: Task, file?: File) => void;
  compact?: boolean;
}) {
  const transitions = getAvailableTaskTransitions(task, canReviewTasks);
  return (
    <div className={`actions ${compact ? 'task-card-actions' : ''}`}>
      {transitions.map((status) => (
        <button key={status} type="button" onClick={() => onChange(task, status)}>
          {getTransitionLabel(task, status)}
        </button>
      ))}
      <label className="button">
        <Upload size={16} aria-hidden="true" />
        Файл
        <input hidden type="file" onChange={(event) => onUpload(task, event.target.files?.[0])} />
      </label>
    </div>
  );
}

function getTransitionLabel(task: Task, status: TaskStatus) {
  if (status === 'IN_PROGRESS' && task.status === 'ON_REVIEW') return 'Вернуть';
  if (status === 'IN_PROGRESS') return 'В работу';
  if (status === 'ON_REVIEW') return 'На проверку';
  if (status === 'DONE') return 'Принять';
  if (status === 'CANCELED') return 'Отменить';
  return status;
}

function getStatusComment(task: Task, status: TaskStatus) {
  if (status === 'ON_REVIEW') return 'Готово, файл прикреплён.';
  if (status === 'DONE') return 'Задача принята.';
  if (status === 'IN_PROGRESS' && task.status === 'ON_REVIEW') return 'Нужно доработать.';
  if (status === 'CANCELED') return 'Задача отменена.';
  return 'Статус обновлён.';
}

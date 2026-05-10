import type { Task, TaskStatus } from '../../shared/types/domain';

export interface TaskKanbanColumn {
  status: TaskStatus;
  title: string;
}

const taskKanbanColumns: TaskKanbanColumn[] = [
  { status: 'TODO', title: 'TODO' },
  { status: 'IN_PROGRESS', title: 'IN PROGRESS' },
  { status: 'ON_REVIEW', title: 'ON REVIEW' },
  { status: 'DONE', title: 'DONE' },
  { status: 'CANCELED', title: 'CANCELED' }
];

export function getTaskKanbanColumns() {
  return taskKanbanColumns;
}

export function groupTasksByStatus(tasks: Task[]) {
  return taskKanbanColumns.reduce<Record<TaskStatus, Task[]>>((groups, column) => {
    groups[column.status] = tasks.filter((task) => task.status === column.status);
    return groups;
  }, {} as Record<TaskStatus, Task[]>);
}

export function getAvailableTaskTransitions(task: Task, canReviewTasks: boolean): TaskStatus[] {
  if (task.status === 'TODO') {
    return canReviewTasks ? ['IN_PROGRESS', 'CANCELED'] : ['IN_PROGRESS'];
  }

  if (task.status === 'IN_PROGRESS') {
    return canReviewTasks ? ['ON_REVIEW', 'CANCELED'] : ['ON_REVIEW'];
  }

  if (task.status === 'ON_REVIEW' && canReviewTasks) {
    return ['DONE', 'IN_PROGRESS'];
  }

  return [];
}

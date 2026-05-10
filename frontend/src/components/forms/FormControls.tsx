import { ReactNode } from 'react';

export function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  );
}

export function ErrorState({ message }: { message?: string }) {
  if (!message) return null;
  return <div className="state state-error">{message}</div>;
}

export function EmptyState({ title, text }: { title: string; text?: string }) {
  return (
    <div className="state state-empty">
      <strong>{title}</strong>
      {text && <span>{text}</span>}
    </div>
  );
}

export function LoadingState() {
  return <div className="state">Загрузка...</div>;
}

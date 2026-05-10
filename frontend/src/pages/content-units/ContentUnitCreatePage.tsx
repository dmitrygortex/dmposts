import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { contentApi, userApi } from '../../services/api';
import { ContentType, contentTypes, User } from '../../shared/types/domain';
import { ErrorState, Field } from '../../components/forms/FormControls';
import { optionalNumber } from '../../shared/utils/format';

export function ContentUnitCreatePage() {
  const navigate = useNavigate();
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    title: 'Пост про весеннюю акцию на детейлинг',
    description: 'Материал для продвижения весенней акции',
    baseText: 'Весенняя акция на детейлинг автомобиля. Только до конца недели скидка 20%.',
    contentType: 'POST' as ContentType,
    responsibleUserId: '',
    plannedPublishAt: ''
  });

  useEffect(() => {
    userApi.list().then(setUsers).catch(() => setUsers([]));
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      const created = await contentApi.create({
        ...form,
        responsibleUserId: optionalNumber(form.responsibleUserId),
        plannedPublishAt: form.plannedPublishAt || undefined
      });
      navigate(`/content-units/${created.id}`);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Контент не создан');
    }
  };

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Новая content unit</h1>
          <p>Материал создаётся один раз, а платформенные тексты будут подготовлены позже в Crossposting.</p>
        </div>
      </div>
      <section className="panel">
        <form className="grid two" onSubmit={submit}>
          <ErrorState message={error} />
          <Field label="Название"><input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></Field>
          <Field label="Тип">
            <select value={form.contentType} onChange={(e) => setForm({ ...form, contentType: e.target.value as ContentType })}>{contentTypes.map((type) => <option key={type}>{type}</option>)}</select>
          </Field>
          <Field label="Ответственный">
            <select value={form.responsibleUserId} onChange={(e) => setForm({ ...form, responsibleUserId: e.target.value })}>
              <option value="">Не выбран</option>
              {users.map((user) => <option key={user.id} value={user.id}>{user.fullName} ({user.role})</option>)}
            </select>
          </Field>
          <Field label="План публикации"><input type="datetime-local" value={form.plannedPublishAt} onChange={(e) => setForm({ ...form, plannedPublishAt: e.target.value })} /></Field>
          <Field label="Описание"><textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
          <Field label="Базовый текст"><textarea value={form.baseText} onChange={(e) => setForm({ ...form, baseText: e.target.value })} /></Field>
          <button className="primary" type="submit">Создать content unit</button>
        </form>
      </section>
    </>
  );
}

import { FormEvent, useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import { userApi } from '../../services/api';
import { Role, roles, User } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { ErrorState, Field, LoadingState } from '../../components/forms/FormControls';

export function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ email: 'manager@example.com', password: 'password123', fullName: 'Контент Менеджер', role: 'CONTENT_MANAGER' as Role });

  const load = () => {
    setLoading(true);
    userApi.list().then(setUsers).catch((e) => setError(e.message)).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await userApi.create(form);
      setForm({ email: '', password: 'password123', fullName: '', role: 'EXECUTOR' });
      load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Пользователь не создан');
    }
  };

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Пользователи</h1>
          <p>OWNER управляет ролями и активностью пользователей без отдельной permissions-системы.</p>
        </div>
      </div>
      <section className="panel">
        <h2>Создать пользователя</h2>
        <form className="grid four" onSubmit={submit}>
          <Field label="Email"><input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required /></Field>
          <Field label="ФИО"><input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required /></Field>
          <Field label="Пароль"><input value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required /></Field>
          <Field label="Роль">
            <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value as Role })}>
              {roles.map((role) => <option key={role}>{role}</option>)}
            </select>
          </Field>
          <button className="primary" type="submit"><Plus size={16} />Создать</button>
        </form>
      </section>
      <ErrorState message={error} />
      {loading ? <LoadingState /> : (
        <section className="panel table-wrap">
          <table>
            <thead><tr><th>Email</th><th>ФИО</th><th>Роль</th><th>Статус</th><th>Действия</th></tr></thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.email}</td>
                  <td>{user.fullName}</td>
                  <td><Badge value={user.role} /></td>
                  <td>{user.isActive ? 'Активен' : 'Отключён'}</td>
                  <td className="actions">
                    <select value={user.role} onChange={(e) => userApi.updateRole(user.id, e.target.value as Role).then(load).catch((err) => setError(err.message))}>
                      {roles.map((role) => <option key={role}>{role}</option>)}
                    </select>
                    {user.isActive
                      ? <button className="danger" onClick={() => userApi.deactivate(user.id).then(load).catch((err) => setError(err.message))}>Отключить</button>
                      : <button onClick={() => userApi.activate(user.id).then(load).catch((err) => setError(err.message))}>Активировать</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </>
  );
}

import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';
import { ErrorState, Field } from '../../components/forms/FormControls';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('owner@example.com');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Не удалось войти');
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card grid" onSubmit={submit}>
        <div>
          <h1>Вход</h1>
          <p>Авторизация для команды производства и публикации контента.</p>
        </div>
        <ErrorState message={error} />
        <Field label="Email">
          <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
        </Field>
        <Field label="Пароль">
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
        </Field>
        <button className="primary" type="submit">Войти</button>
        <Link to="/register">Первый запуск системы</Link>
      </form>
    </div>
  );
}

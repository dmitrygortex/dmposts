import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';
import { authApi } from '../../services/api';
import { ErrorState, Field, LoadingState } from '../../components/forms/FormControls';

export function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [available, setAvailable] = useState<boolean | null>(null);
  const [email, setEmail] = useState('owner@example.com');
  const [password, setPassword] = useState('password123');
  const [fullName, setFullName] = useState('Дмитрий Голиков');
  const [error, setError] = useState('');

  useEffect(() => {
    authApi.setupStatus().then((status) => setAvailable(status.registrationAvailable)).catch(() => setAvailable(false));
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await register(email, password, fullName);
      navigate('/dashboard');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Регистрация не выполнена');
    }
  };

  if (available === null) {
    return <div className="auth-page"><div className="auth-card"><LoadingState /></div></div>;
  }

  return (
    <div className="auth-page">
      <form className="auth-card grid" onSubmit={submit}>
        <div>
          <h1>Первый OWNER</h1>
          <p>Публичная регистрация доступна только до создания первого пользователя.</p>
        </div>
        {!available && <ErrorState message="Регистрация закрыта. Войдите под существующим пользователем." />}
        <ErrorState message={error} />
        <Field label="Email">
          <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required disabled={!available} />
        </Field>
        <Field label="Пароль">
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required disabled={!available} />
        </Field>
        <Field label="ФИО">
          <input value={fullName} onChange={(event) => setFullName(event.target.value)} required disabled={!available} />
        </Field>
        <button className="primary" type="submit" disabled={!available}>Зарегистрировать OWNER</button>
        <Link to="/login">Перейти ко входу</Link>
      </form>
    </div>
  );
}

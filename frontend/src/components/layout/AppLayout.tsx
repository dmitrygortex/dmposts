import {
  Bell,
  CalendarDays,
  CheckSquare,
  ClipboardCheck,
  FileText,
  LayoutDashboard,
  LogOut,
  Megaphone,
  Settings,
  Upload,
  Users
} from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';

const managerLinks = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/content-plan', label: 'Контент-план', icon: CalendarDays },
  { to: '/content-units', label: 'Контент-единицы', icon: FileText },
  { to: '/tasks', label: 'Задачи', icon: CheckSquare },
  { to: '/approvals', label: 'Согласование', icon: ClipboardCheck },
  { to: '/publications', label: 'Публикации', icon: Megaphone },
  { to: '/manual-publication', label: 'Ручная публикация', icon: Upload },
  { to: '/media', label: 'Медиафайлы', icon: Upload }
];

const ownerOnlyLinks = [
  { to: '/users', label: 'Пользователи', icon: Users },
  { to: '/settings/platforms', label: 'Настройки платформ', icon: Settings }
];

export function AppLayout() {
  const { user, logout } = useAuth();
  const links = user?.role === 'EXECUTOR'
    ? managerLinks.filter((link) => link.to === '/dashboard' || link.to === '/tasks')
    : user?.role === 'OWNER'
      ? [...managerLinks, ...ownerOnlyLinks]
      : managerLinks;

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">CC</span>
          <div>
            <strong>Content CRM</strong>
            <small>Social MVP</small>
          </div>
        </div>
        <nav className="nav-list" aria-label="Основная навигация">
          {links.map((link) => {
            const Icon = link.icon;
            return (
              <NavLink key={link.to} to={link.to} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                <Icon size={18} aria-hidden="true" />
                <span>{link.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </aside>
      <div className="workspace">
        <header className="topbar">
          <div>
            <strong>{user?.fullName}</strong>
            <span>{user?.role}</span>
          </div>
          <div className="topbar-actions">
            <Bell size={19} aria-label="Уведомления" />
            <button className="icon-text" onClick={logout} type="button">
              <LogOut size={17} aria-hidden="true" />
              Выйти
            </button>
          </div>
        </header>
        <main className="main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

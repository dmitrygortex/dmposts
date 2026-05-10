import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../providers/AuthProvider';
import { AppLayout } from '../../components/layout/AppLayout';
import { LoginPage } from '../../pages/auth/LoginPage';
import { RegisterPage } from '../../pages/auth/RegisterPage';
import { DashboardPage } from '../../pages/dashboard/DashboardPage';
import { UsersPage } from '../../pages/users/UsersPage';
import { PlatformSettingsPage } from '../../pages/settings/PlatformSettingsPage';
import { ContentUnitListPage } from '../../pages/content-units/ContentUnitListPage';
import { ContentUnitCreatePage } from '../../pages/content-units/ContentUnitCreatePage';
import { ContentUnitDetailsPage } from '../../pages/content-units/ContentUnitDetailsPage';
import { TasksPage } from '../../pages/tasks/TasksPage';
import { ApprovalsPage } from '../../pages/approvals/ApprovalsPage';
import { PublicationsPage } from '../../pages/publications/PublicationsPage';
import { ManualPublicationListPage } from '../../pages/manual-publication/ManualPublicationListPage';
import { ManualPublicationDetailsPage } from '../../pages/manual-publication/ManualPublicationDetailsPage';
import { MediaPage } from '../../pages/media/MediaPage';
import { ContentPlanPage } from '../../pages/content-plan/ContentPlanPage';

function Protected({ children }: { children: JSX.Element }) {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return <div className="centered">Загрузка...</div>;
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

function OwnerOnly({ children }: { children: JSX.Element }) {
  const { user } = useAuth();
  return user?.role === 'OWNER' ? children : <Navigate to="/dashboard" replace />;
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        element={(
          <Protected>
            <AppLayout />
          </Protected>
        )}
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/content-plan" element={<ContentPlanPage />} />
        <Route path="/content-units" element={<ContentUnitListPage />} />
        <Route path="/content-units/new" element={<ContentUnitCreatePage />} />
        <Route path="/content-units/:id" element={<ContentUnitDetailsPage />} />
        <Route path="/tasks" element={<TasksPage />} />
        <Route path="/approvals" element={<ApprovalsPage />} />
        <Route path="/publications" element={<PublicationsPage />} />
        <Route path="/manual-publication" element={<ManualPublicationListPage />} />
        <Route path="/manual-publication/:variantId" element={<ManualPublicationDetailsPage />} />
        <Route path="/media" element={<MediaPage />} />
        <Route path="/users" element={<OwnerOnly><UsersPage /></OwnerOnly>} />
        <Route path="/settings/platforms" element={<OwnerOnly><PlatformSettingsPage /></OwnerOnly>} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

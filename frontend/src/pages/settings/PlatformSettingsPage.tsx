import { useEffect, useState } from 'react';
import { Save } from 'lucide-react';
import { platformSettingsApi } from '../../services/api';
import { PlatformMode, PlatformSetting, platformModes } from '../../shared/types/domain';
import { Badge } from '../../components/status/Badge';
import { ErrorState, Field, LoadingState } from '../../components/forms/FormControls';

const manualEditorUrls: Partial<Record<string, string>> = {
  TENCHAT: 'https://tenchat.ru/editor',
  SETKA: 'https://setka.ru/posts/regular/new'
};

export function PlatformSettingsPage() {
  const [settings, setSettings] = useState<PlatformSetting[]>([]);
  const [tokens, setTokens] = useState<Record<string, string>>({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    platformSettingsApi.list().then(setSettings).catch((e) => setError(e.message)).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const save = async (setting: PlatformSetting) => {
    setError('');
    try {
      const payload: { enabled: boolean; mode: PlatformMode; accessToken?: string; communityId?: string; apiVersion?: string; manualUrl?: string; instanceUrl?: string } = {
        enabled: setting.enabled,
        mode: setting.mode
      };
      const token = tokens[setting.platform]?.trim();
      if (token) {
        payload.accessToken = token;
      }
      if (setting.platform === 'VK') {
        payload.communityId = setting.communityId ?? '';
        payload.apiVersion = setting.apiVersion ?? '5.199';
      }
      if (setting.platform === 'MAX') {
        payload.manualUrl = setting.manualUrl ?? '';
      }
      if (setting.platform === 'MASTODON') {
        payload.instanceUrl = setting.instanceUrl ?? '';
      }
      await platformSettingsApi.update(setting.platform, payload);
      setTokens({ ...tokens, [setting.platform]: '' });
      load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Настройки не сохранены');
    }
  };

  if (loading) return <LoadingState />;

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Настройки платформ</h1>
          <p>Telegram работает как mock success, VK и Mastodon публикуют текстовые посты при корректных настройках, TenChat, Setka и MAX остаются ручными каналами.</p>
        </div>
      </div>
      <ErrorState message={error} />
      <div className="grid two">
        {settings.map((setting, index) => (
          <section className="panel" key={setting.platform}>
            <div className="page-header">
              <div>
                <h2>{setting.platform}</h2>
                <Badge value={setting.mode} />
              </div>
              <label className="actions">
                <input
                  type="checkbox"
                  checked={setting.enabled}
                  onChange={(event) => setSettings(settings.map((item, i) => i === index ? { ...item, enabled: event.target.checked } : item))}
                />
                enabled
              </label>
            </div>
            <div className="grid">
              <Field label="Mode">
                <select value={setting.mode} onChange={(event) => setSettings(settings.map((item, i) => i === index ? { ...item, mode: event.target.value as PlatformMode } : item))}>
                  {platformModes.map((mode) => <option key={mode}>{mode}</option>)}
                </select>
              </Field>
              {!['TENCHAT', 'SETKA', 'MAX'].includes(setting.platform) && (
                <>
                  <div className="state">Token: {setting.tokenConfigured ? 'configured' : 'not configured'}</div>
                  <Field label={setting.platform === 'VK' ? 'VK community access token' : setting.platform === 'MASTODON' ? 'Mastodon access token' : 'Access token'}>
                    <input value={tokens[setting.platform] ?? ''} onChange={(event) => setTokens({ ...tokens, [setting.platform]: event.target.value })} placeholder={setting.tokenConfigured ? 'Оставьте пустым, чтобы не менять' : setting.platform === 'VK' ? 'community token' : 'token'} />
                  </Field>
                </>
              )}
              {setting.platform === 'VK' && (
                <>
                  <div className="state">Текстовые VK-посты публикуются автоматически с community token. Посты с изображениями переводятся в ручную публикацию.</div>
                  <Field label="VK community ID">
                    <input
                      value={setting.communityId ?? ''}
                      onChange={(event) => setSettings(settings.map((item, i) => i === index ? { ...item, communityId: event.target.value } : item))}
                      placeholder="238241783 или https://vk.com/club238241783"
                    />
                  </Field>
                  <Field label="VK API version">
                    <input
                      value={setting.apiVersion ?? '5.199'}
                      onChange={(event) => setSettings(settings.map((item, i) => i === index ? { ...item, apiVersion: event.target.value } : item))}
                      placeholder="5.199"
                    />
                  </Field>
                </>
              )}
              {manualEditorUrls[setting.platform] && (
                <div className="state">Manual platform. Open platform ведёт на {manualEditorUrls[setting.platform]}.</div>
              )}
              {setting.platform === 'MAX' && (
                <>
                  <div className="state">Manual platform. Open platform ведёт на ссылку канала/чата, а если она не задана — на https://web.max.ru/.</div>
                  <Field label="MAX channel/chat URL">
                    <input
                      type="url"
                      value={setting.manualUrl ?? ''}
                      onChange={(event) => setSettings(settings.map((item, i) => i === index ? { ...item, manualUrl: event.target.value } : item))}
                      placeholder="https://web.max.ru/-74213461897922"
                    />
                  </Field>
                </>
              )}
              {setting.platform === 'MASTODON' && (
                <>
                  <div className="state">Text-only posts use Mastodon API. Posts with media go to manual fallback in this MVP.</div>
                  <Field label="Mastodon instance URL">
                    <input
                      type="url"
                      value={setting.instanceUrl ?? ''}
                      onChange={(event) => setSettings(settings.map((item, i) => i === index ? { ...item, instanceUrl: event.target.value } : item))}
                      placeholder="https://mastodon.social"
                    />
                  </Field>
                </>
              )}
              <button className="primary" onClick={() => save(setting)}><Save size={16} />Сохранить</button>
            </div>
          </section>
        ))}
      </div>
    </>
  );
}

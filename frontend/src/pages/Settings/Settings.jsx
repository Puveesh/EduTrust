import React, { useState, useEffect } from 'react';
import { Settings as SettingsIcon, Shield, Database, Key, Lock, Server, Cpu, CheckCircle2, User, Save } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { userService } from '../../services/userService';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';

export const Settings = () => {
  const { user, login } = useAuth();
  const { showToast } = useToast();

  const [profile, setProfile] = useState(null);
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const data = await userService.getProfile();
      setProfile(data);
      setFullName(data.fullName || '');
    } catch (e) {
      console.error('Error loading profile', e);
    }
  };

  const handleUpdateName = async (e) => {
    e.preventDefault();
    if (!fullName.trim()) {
      showToast('Display name cannot be empty', 'warning');
      return;
    }

    setIsSaving(true);
    try {
      const updated = await userService.updateProfile({ fullName: fullName.trim() });
      setProfile(updated);
      setFullName(updated.fullName);
      showToast('Profile full name updated successfully in database', 'success');
      // Update local storage user
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        try {
          const parsed = JSON.parse(storedUser);
          parsed.fullName = updated.fullName;
          localStorage.setItem('user', JSON.stringify(parsed));
        } catch (ignored) {}
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to update profile name', 'error');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-4xl">
      <div>
        <h2 className="text-xl font-bold text-slate-900">Account Profile & System Specifications</h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Manage your personal display name, inspect institutional tenant boundary, and view cryptographic protocols
        </p>
      </div>

      {/* Account Profile Card (Section 8) */}
      <Card
        title="College Registrar Profile"
        subtitle="Only display name is editable; administrative and institutional bounds are strictly read-only"
      >
        <form onSubmit={handleUpdateName} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            {/* Section 8.1: Editable Field */}
            <div className="p-3.5 bg-blue-50/50 rounded-xl border border-blue-200">
              <label className="text-blue-900 block text-[11px] font-bold uppercase mb-1">
                Display Full Name <span className="text-blue-600 font-normal lowercase">(Editable)</span>
              </label>
              <div className="flex gap-2 mt-1">
                <Input
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="Your Full Name"
                  required
                  className="bg-white"
                />
                <Button type="submit" isLoading={isSaving} icon={Save} size="sm">
                  Save
                </Button>
              </div>
              <span className="text-[10px] text-slate-500 mt-1 block">
                This name appears on institutional records and audit logs.
              </span>
            </div>

            {/* Section 8.2: Read-Only Fields */}
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/80">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                Username <span className="text-slate-400 font-normal">(Read-Only)</span>
              </span>
              <span className="text-sm font-mono font-semibold text-slate-900 mt-1 block">
                {profile?.username || user?.username}
              </span>
            </div>

            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/80">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                Email Address <span className="text-slate-400 font-normal">(Read-Only)</span>
              </span>
              <span className="text-sm font-semibold text-slate-800 mt-1 block">
                {profile?.email || 'N/A'}
              </span>
            </div>

            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/80">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                Assigned Role <span className="text-slate-400 font-normal">(Read-Only)</span>
              </span>
              <div className="mt-1 flex items-center gap-2">
                <span className="text-sm font-bold text-blue-700">{profile?.role || user?.role}</span>
                <Badge variant="info">Fixed Authority</Badge>
              </div>
            </div>

            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/80">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                Associated Institution <span className="text-slate-400 font-normal">(Read-Only)</span>
              </span>
              <span className="text-sm font-bold text-slate-900 mt-1 block">
                {profile?.institutionName || user?.institutionName || 'Central AICTE Administration'}
              </span>
            </div>

            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/80">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                Institution Code & ID <span className="text-slate-400 font-normal">(Tenant Boundary)</span>
              </span>
              <span className="text-sm font-mono font-semibold text-slate-800 mt-1 block">
                {profile?.institutionCode || 'N/A'} (ID: {profile?.institutionId || user?.institutionId || 'N/A'})
              </span>
            </div>
          </div>
        </form>
      </Card>

      {/* Cryptographic & Platform Security Specs */}
      <Card title="Security & Cryptographic Specifications" subtitle="Enterprise architectural parameters implemented in prototype">
        <div className="space-y-3 text-xs">
          <div className="flex items-center justify-between p-3 rounded-xl border border-slate-200">
            <div className="flex items-center gap-3">
              <Key className="w-5 h-5 text-blue-600 shrink-0" />
              <div>
                <p className="font-semibold text-slate-900">Digital Signature Algorithm</p>
                <p className="text-slate-500 text-[11px]">RSA-2048 with SHA-256 for academic credentials</p>
              </div>
            </div>
            <Badge variant="info">SHA256withRSA</Badge>
          </div>

          <div className="flex items-center justify-between p-3 rounded-xl border border-slate-200">
            <div className="flex items-center gap-3">
              <Lock className="w-5 h-5 text-emerald-600 shrink-0" />
              <div>
                <p className="font-semibold text-slate-900">Session Authentication</p>
                <p className="text-slate-500 text-[11px]">Stateless HMAC-SHA512 JWT (24-hour expiration)</p>
              </div>
            </div>
            <Badge variant="success">JJWT 0.12.6</Badge>
          </div>

          <div className="flex items-center justify-between p-3 rounded-xl border border-slate-200">
            <div className="flex items-center gap-3">
              <Database className="w-5 h-5 text-indigo-600 shrink-0" />
              <div>
                <p className="font-semibold text-slate-900">Primary Relational Database</p>
                <p className="text-slate-500 text-[11px]">MySQL 8.0 with InnoDB and Flyway V1 schema migrations</p>
              </div>
            </div>
            <Badge variant="purple">sentinel_ledger</Badge>
          </div>

          <div className="flex items-center justify-between p-3 rounded-xl border border-slate-200">
            <div className="flex items-center gap-3">
              <Server className="w-5 h-5 text-slate-700 shrink-0" />
              <div>
                <p className="font-semibold text-slate-900">API Documentation Standard</p>
                <p className="text-slate-500 text-[11px]">OpenAPI 3.0 / Swagger UI live at /swagger-ui.html</p>
              </div>
            </div>
            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="text-blue-600 hover:underline font-semibold"
            >
              Open Swagger UI ↗
            </a>
          </div>
        </div>
      </Card>
    </div>
  );
};

import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { LogOut, Bell, User as UserIcon, Shield, ChevronDown } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Topbar = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const getPageTitle = () => {
    const path = location.pathname;
    if (path === '/') return 'Overview & Aggregate Analytics';
    if (path.startsWith('/students')) return 'Institutional Student Records';
    if (path.startsWith('/imports')) return 'APAAR Data Import & Validation';
    if (path.startsWith('/reviews')) return 'Review & Conflict Resolution Center';
    if (path.startsWith('/institutions')) return 'AICTE Institutions Directory';
    if (path.startsWith('/compliance')) return 'Approved Intake & Quota Compliance';
    if (path.startsWith('/verification')) return 'Cryptographic Credential Verification';
    if (path.startsWith('/audit-logs')) return 'Immutable Audit Log Trail';
    if (path.startsWith('/settings')) return 'System Settings & Security Config';
    return 'Dashboard';
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200/80 px-8 flex items-center justify-between sticky top-0 z-30 shadow-2xs">
      {/* Title & Breadcrumbs */}
      <div>
        <div className="flex items-center space-x-2 text-xs text-slate-400">
          <span>EduTrust</span>
          <span>/</span>
          <span className="text-slate-600 font-medium capitalize">
            {location.pathname.substring(1) || 'Dashboard'}
          </span>
        </div>
        <h1 className="text-base font-semibold text-slate-900 tracking-tight mt-0.5">
          {getPageTitle()}
        </h1>
      </div>

      {/* Right Controls */}
      <div className="flex items-center space-x-4">
        {/* Synthetic Demo Banner Pill */}
        <div className="hidden md:flex items-center px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-[11px] font-medium text-slate-600 gap-1.5">
          <span className="w-1.5 h-1.5 rounded-full bg-blue-600 animate-pulse"></span>
          <span>SIH 2024 Demo Prototype</span>
        </div>

        {/* User Menu */}
        <div className="relative">
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="flex items-center space-x-3 p-1.5 rounded-xl hover:bg-slate-50 transition-colors focus:outline-none"
          >
            <div className="w-8 h-8 rounded-full bg-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 font-semibold text-xs">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="hidden sm:block text-left text-xs">
              <p className="font-semibold text-slate-800 leading-tight">{user?.fullName || 'User'}</p>
              <p className="text-[10px] text-slate-500">{user?.role?.replace('_', ' ')}</p>
            </div>
            <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
          </button>

          {dropdownOpen && (
            <div
              className="absolute right-0 mt-2 w-56 rounded-xl bg-white shadow-xl border border-slate-100 py-1.5 z-50 text-xs animate-scale-up"
              onClick={() => setDropdownOpen(false)}
            >
              <div className="px-4 py-2 border-b border-slate-100">
                <p className="font-semibold text-slate-900">{user?.fullName}</p>
                <p className="text-slate-500 truncate text-[11px]">{user?.email}</p>
                <p className="text-[10px] font-medium text-blue-600 mt-1">Role: {user?.role}</p>
              </div>

              <button
                onClick={() => navigate('/settings')}
                className="w-full flex items-center px-4 py-2 text-slate-700 hover:bg-slate-50 text-left"
              >
                <Shield className="w-3.5 h-3.5 mr-2.5 text-slate-400" />
                Security Credentials
              </button>

              <button
                onClick={handleLogout}
                className="w-full flex items-center px-4 py-2 text-rose-600 hover:bg-rose-50 text-left font-medium"
              >
                <LogOut className="w-3.5 h-3.5 mr-2.5 text-rose-500" />
                Sign Out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

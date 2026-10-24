import React, { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  UploadCloud,
  ShieldAlert,
  Building2,
  Scale,
  Award,
  History,
  Settings,
  ShieldCheck,
  ChevronRight
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { reviewService } from '../../services/reviewService';

export const Sidebar = () => {
  const { user, hasRole } = useAuth();
  const [openReviewsCount, setOpenReviewsCount] = useState(0);

  useEffect(() => {
    const fetchPendingReviews = async () => {
      try {
        const res = await reviewService.getCases({ status: 'OPEN', size: 1 });
        if (res && res.totalElements !== undefined) {
          setOpenReviewsCount(res.totalElements);
        }
      } catch (e) {
        // Silent catch for background notification indicator
      }
    };
    fetchPendingReviews();
    const interval = setInterval(fetchPendingReviews, 30000);
    return () => clearInterval(interval);
  }, []);

  const navItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Student Records', path: '/students', icon: Users, roles: ['AICTE_ADMIN', 'COLLEGE_REGISTRAR'] },
    { name: 'Data Imports', path: '/imports', icon: UploadCloud, roles: ['AICTE_ADMIN', 'COLLEGE_REGISTRAR'] },
    {
      name: 'Review Center',
      path: '/reviews',
      icon: ShieldAlert,
      roles: ['AICTE_ADMIN', 'COLLEGE_REGISTRAR'],
      badge: openReviewsCount > 0 ? openReviewsCount : null
    },
    { name: 'Institutions', path: '/institutions', icon: Building2, roles: ['AICTE_ADMIN'] },
    { name: 'Intake Compliance', path: '/compliance', icon: Scale, roles: ['AICTE_ADMIN', 'COLLEGE_REGISTRAR'] },
    { name: 'Credential Verification', path: '/verification', icon: Award },
    { name: 'Audit Logs', path: '/audit-logs', icon: History, roles: ['AICTE_ADMIN', 'COLLEGE_REGISTRAR'] },
    { name: 'Settings', path: '/settings', icon: Settings }
  ];

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col shrink-0 min-h-screen text-slate-300">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-6 border-b border-slate-800/80 gap-3">
        <div className="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center text-white shadow-md shadow-blue-500/20">
          <ShieldCheck className="w-5 h-5" />
        </div>
        <div>
          <div className="flex items-center gap-1.5">
            <span className="font-bold text-white tracking-tight text-base">Project EduTrust</span>
            <span className="text-[10px] font-semibold bg-blue-500/20 text-blue-400 px-1.5 py-0.5 rounded">SIH</span>
          </div>
          <p className="text-[10px] text-slate-400 font-medium">APAAR Trust Ledger</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <div className="px-3 pb-2 text-[10px] font-semibold tracking-wider text-slate-400 uppercase">
          Core Workflows
        </div>
        {navItems.map((item) => {
          if (item.roles && !item.roles.includes(user?.role)) {
            return null;
          }
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center justify-between px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all group ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                }`
              }
            >
              <div className="flex items-center gap-3">
                <item.icon className="w-4 h-4 shrink-0 transition-transform group-hover:scale-105" />
                <span>{item.name}</span>
              </div>
              {item.badge ? (
                <span className="px-2 py-0.5 text-[10px] font-bold rounded-full bg-amber-500 text-slate-950 animate-pulse">
                  {item.badge}
                </span>
              ) : null}
            </NavLink>
          );
        })}
      </nav>

      {/* Institution Scope Badge */}
      <div className="p-4 border-t border-slate-800/80 bg-slate-950/40">
        <div className="flex items-center gap-2 mb-1.5">
          <div className="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></div>
          <p className="text-[10px] uppercase tracking-wider font-bold text-slate-400">Security Context</p>
        </div>
        <p className="text-xs font-semibold text-white truncate">
          {user?.role === 'AICTE_ADMIN' ? 'National AICTE Grid' : (user?.institutionName || 'Authorized Verifier')}
        </p>
        <p className="text-[10px] text-slate-400">
          Role: <span className="text-blue-400 font-medium">{user?.role}</span>
        </p>
      </div>
    </aside>
  );
};

import React from 'react';

export const Badge = ({ children, variant = 'default', size = 'md', className = '' }) => {
  const getVariantStyles = () => {
    switch (variant) {
      case 'success':
      case 'VERIFIED':
      case 'VALID':
      case 'APPROVED':
      case 'RESOLVED':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'warning':
      case 'PENDING_REVIEW':
      case 'UNDER_REVIEW':
      case 'NEEDS_CORRECTION':
      case 'DUPLICATE_CANDIDATE':
      case 'PROVISIONAL':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'danger':
      case 'FLAGGED':
      case 'REJECTED':
      case 'INVALID':
      case 'FAILED':
      case 'TAMPERED':
      case 'SUSPENDED':
      case 'CONFIRMED_DUPLICATE':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'info':
      case 'READY_TO_COMMIT':
      case 'UPLOADED':
      case 'ADMITTED':
      case 'ENROLLED':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'purple':
      case 'COMMITTED':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  const getSizeStyles = () => {
    return size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs font-semibold';
  };

  return (
    <span
      className={`inline-flex items-center rounded-full border ${getVariantStyles()} ${getSizeStyles()} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-70"></span>
      {children}
    </span>
  );
};

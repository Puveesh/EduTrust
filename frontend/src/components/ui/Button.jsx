import React from 'react';
import { Loader2 } from 'lucide-react';

export const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled = false,
  icon: Icon,
  className = '',
  onClick,
  type = 'button',
  ...props
}) => {
  const getVariantStyles = () => {
    switch (variant) {
      case 'primary':
        return 'bg-blue-600 hover:bg-blue-700 text-white shadow-sm border border-transparent focus:ring-blue-500';
      case 'secondary':
        return 'bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 shadow-sm focus:ring-slate-400';
      case 'success':
        return 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm focus:ring-emerald-500';
      case 'danger':
        return 'bg-rose-600 hover:bg-rose-700 text-white shadow-sm focus:ring-rose-500';
      case 'ghost':
        return 'hover:bg-slate-100 text-slate-600 border-transparent';
      default:
        return 'bg-blue-600 text-white';
    }
  };

  const getSizeStyles = () => {
    switch (size) {
      case 'sm':
        return 'px-3 py-1.5 text-xs font-medium';
      case 'lg':
        return 'px-5 py-2.5 text-base font-medium';
      default:
        return 'px-4 py-2 text-sm font-medium';
    }
  };

  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      onClick={onClick}
      className={`inline-flex items-center justify-center rounded-lg transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:opacity-50 disabled:cursor-not-allowed ${getVariantStyles()} ${getSizeStyles()} ${className}`}
      {...props}
    >
      {isLoading ? (
        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
      ) : Icon ? (
        <Icon className="w-4 h-4 mr-2" />
      ) : null}
      {children}
    </button>
  );
};

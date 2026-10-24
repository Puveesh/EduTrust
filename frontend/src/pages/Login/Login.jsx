import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldCheck, Lock, User, ArrowRight, ShieldAlert, CheckCircle2, UserPlus } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';

export const Login = () => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Admin@123');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await login(username, password);
      showToast('Successfully authenticated to Project EduTrust platform', 'success');
      navigate('/');
    } catch (err) {
      showToast(err.response?.data?.message || 'Invalid username or password', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const fillDemoAccount = (u, p) => {
    setUsername(u);
    setPassword(p);
  };

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-center items-center p-4">
      {/* Background Glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="w-full max-w-md relative z-10">
        {/* Branding */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-blue-600 text-white shadow-xl shadow-blue-500/25 mb-4">
            <ShieldCheck className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Project EduTrust</h1>
          <p className="text-xs text-slate-400 mt-1 max-w-xs mx-auto">
            APAAR ID-Based Student Data Management & Verification Platform for AICTE Institutions
          </p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8 border border-slate-100">
          <div className="mb-6">
            <h2 className="text-lg font-bold text-slate-900">Institutional Access</h2>
            <p className="text-xs text-slate-500 mt-0.5">Please sign in with your authorized security credentials</p>
          </div>

          <form onSubmit={handleLogin} className="space-y-4">
            <Input
              label="Username / Security ID"
              icon={User}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter username"
              required
            />

            <Input
              label="Password"
              icon={Lock}
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />

            <Button
              type="submit"
              className="w-full"
              isLoading={isLoading}
              size="lg"
            >
              Sign In to Platform <ArrowRight className="w-4 h-4 ml-1.5" />
            </Button>
          </form>

          {/* ======================================================================== */}
          {/* TEMPORARY REGISTRATION LINK (Comment out to make invisible/non-usable)     */}
          <div className="mt-4 pt-3 border-t border-slate-100 text-center">
            <Link
              to="/register"
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-blue-600 hover:text-blue-700 hover:underline"
            >
              <UserPlus className="w-3.5 h-3.5" />
              Need a new account? Register as Any Role
            </Link>
          </div>
          {/* ======================================================================== */}

          {/* Quick Demo Credentials Seed Buttons */}
          <div className="mt-4 pt-4 border-t border-slate-100">
            <p className="text-[11px] font-semibold text-slate-500 mb-2 uppercase tracking-wider">
              Quick Demo Logins (Click to autofill)
            </p>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => fillDemoAccount('admin', 'Admin@123')}
                className="p-2 rounded-lg border border-slate-200 text-left hover:bg-slate-50 transition-colors"
              >
                <p className="font-semibold text-slate-800">AICTE Admin</p>
                <p className="text-[10px] text-slate-400">admin / Admin@123</p>
              </button>

              <button
                type="button"
                onClick={() => fillDemoAccount('registrar_delhi', 'Registrar@123')}
                className="p-2 rounded-lg border border-slate-200 text-left hover:bg-slate-50 transition-colors"
              >
                <p className="font-semibold text-slate-800">Delhi Registrar</p>
                <p className="text-[10px] text-slate-400">registrar_delhi</p>
              </button>

              <button
                type="button"
                onClick={() => fillDemoAccount('registrar_maha', 'Registrar@123')}
                className="p-2 rounded-lg border border-slate-200 text-left hover:bg-slate-50 transition-colors"
              >
                <p className="font-semibold text-slate-800">Maharashtra Registrar</p>
                <p className="text-[10px] text-slate-400">registrar_maha</p>
              </button>

              <button
                type="button"
                onClick={() => fillDemoAccount('verifier_corp', 'Verifier@123')}
                className="p-2 rounded-lg border border-slate-200 text-left hover:bg-slate-50 transition-colors"
              >
                <p className="font-semibold text-slate-800">Verifier</p>
                <p className="text-[10px] text-slate-400">verifier_corp</p>
              </button>
            </div>
          </div>
        </div>

        {/* Disclaimer */}
        <p className="text-[11px] text-slate-400 text-center mt-6">
          Smart India Hackathon (SIH) Prototype • Synthetic Evaluation Data Only
        </p>
      </div>
    </div>
  );
};

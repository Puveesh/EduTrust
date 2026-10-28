import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  ShieldCheck,
  Building2,
  CheckCircle2,
  Lock,
  User,
  Mail,
  ArrowRight,
  ArrowLeft,
  Sparkles,
  HelpCircle,
  AlertCircle
} from 'lucide-react';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';

export const Register = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  const { showToast } = useToast();

  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    role: 'COLLEGE_REGISTRAR',
    institutionId: ''
  });

  const [institutions, setInstitutions] = useState([]);
  const [isLoadingInstitutions, setIsLoadingInstitutions] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Fetch institutions for College Registrar selection
  useEffect(() => {
    const fetchInstitutions = async () => {
      setIsLoadingInstitutions(true);
      try {
        const data = await authService.getRegistrationInstitutions();
        setInstitutions(data);
        if (data.length > 0 && !formData.institutionId) {
          setFormData((prev) => ({ ...prev, institutionId: data[0].id }));
        }
      } catch (err) {
        console.error('Failed to load institutions for registration', err);
      } finally {
        setIsLoadingInstitutions(false);
      }
    };

    fetchInstitutions();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
    if (errorMsg) setErrorMsg('');
  };

  const handleRoleSelect = (roleKey) => {
    setFormData((prev) => ({
      ...prev,
      role: roleKey,
      institutionId: roleKey === 'COLLEGE_REGISTRAR' && institutions.length > 0 ? (prev.institutionId || institutions[0].id) : ''
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    if (formData.role === 'COLLEGE_REGISTRAR' && !formData.institutionId) {
      setErrorMsg('Please select an institution for the College Registrar role.');
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        fullName: formData.fullName.trim(),
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password,
        role: formData.role,
        institutionId: formData.role === 'COLLEGE_REGISTRAR' ? Number(formData.institutionId) : null
      };

      await register(payload);
      showToast(`Account registered successfully as ${formData.role}!`, 'success');
      navigate('/');
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Registration failed. Please verify the entered details.';
      setErrorMsg(msg);
      showToast(msg, 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const roles = [
    {
      key: 'COLLEGE_REGISTRAR',
      label: 'College Registrar',
      icon: Building2,
      desc: 'Scoped to a specific institution for student records, CSV imports & local reviews'
    },
    {
      key: 'AICTE_ADMIN',
      label: 'AICTE Admin',
      icon: ShieldCheck,
      desc: 'Full national authority across all institutions, global reviews & intake policies'
    },
    {
      key: 'VERIFIER',
      label: 'Verifier',
      icon: CheckCircle2,
      desc: 'External verification wing with minimal-disclosure academic credential checks'
    }
  ];

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-center items-center p-4 py-12">
      {/* Background Ambient Glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="w-full max-w-xl relative z-10">
        {/* Header Branding */}
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-blue-600 text-white shadow-lg shadow-blue-500/25 mb-3">
            <Sparkles className="w-6 h-6" />
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Register New User Account</h1>
          <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
            Create an authorized user identity under any platform role (AICTE Admin, College Registrar, or Verifier)
          </p>
        </div>

        {/* Registration Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-6 sm:p-8 border border-slate-100">
          {errorMsg && (
            <div className="mb-5 p-3 rounded-lg bg-red-50 border border-red-200 flex items-start gap-2.5 text-xs text-red-700">
              <AlertCircle className="w-4 h-4 text-red-500 mt-0.5 flex-shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* 1. Role Selection */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                Select Account Role
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
                {roles.map((r) => {
                  const Icon = r.icon;
                  const isSelected = formData.role === r.key;
                  return (
                    <button
                      key={r.key}
                      type="button"
                      onClick={() => handleRoleSelect(r.key)}
                      className={`p-3 rounded-xl border text-left transition-all flex flex-col justify-between ${
                        isSelected
                          ? 'border-blue-600 bg-blue-50/60 ring-2 ring-blue-500/20'
                          : 'border-slate-200 hover:border-slate-300 hover:bg-slate-50/50'
                      }`}
                    >
                      <div>
                        <div className="flex items-center justify-between mb-1.5">
                          <Icon
                            className={`w-4 h-4 ${
                              isSelected ? 'text-blue-600' : 'text-slate-500'
                            }`}
                          />
                          {isSelected && (
                            <span className="w-2 h-2 rounded-full bg-blue-600"></span>
                          )}
                        </div>
                        <p className={`text-xs font-bold ${isSelected ? 'text-blue-900' : 'text-slate-800'}`}>
                          {r.label}
                        </p>
                      </div>
                      <p className="text-[10px] text-slate-500 mt-1 line-clamp-2 leading-relaxed">
                        {r.desc}
                      </p>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* 2. Institution Dropdown (Visible only if COLLEGE_REGISTRAR) */}
            {formData.role === 'COLLEGE_REGISTRAR' && (
              <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-xl space-y-1.5 animate-fade-in">
                <label className="block text-xs font-semibold text-slate-800 flex items-center justify-between">
                  <span>Assigned Institution *</span>
                  <span className="text-[10px] font-normal text-slate-500">
                    Registrar will be strictly scoped to this college
                  </span>
                </label>
                {isLoadingInstitutions ? (
                  <p className="text-xs text-slate-400">Loading approved institutions...</p>
                ) : institutions.length === 0 ? (
                  <p className="text-xs text-amber-600">
                    No approved institutions found. Please create an institution first or choose another role.
                  </p>
                ) : (
                  <select
                    name="institutionId"
                    value={formData.institutionId}
                    onChange={handleChange}
                    required
                    className="w-full text-xs rounded-lg border border-slate-300 bg-white p-2.5 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent font-medium"
                  >
                    {institutions.map((inst) => (
                      <option key={inst.id} value={inst.id}>
                        {inst.name} ({inst.code}) - {inst.state}
                      </option>
                    ))}
                  </select>
                )}
              </div>
            )}

            {/* 3. Personal & Security Credentials */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <Input
                label="Full Name *"
                icon={User}
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                placeholder="e.g. Dr. Priya Sharma"
                required
              />

              <Input
                label="Username / Login ID *"
                icon={User}
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="e.g. registrar_tech"
                required
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <Input
                label="Email Address *"
                icon={Mail}
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="official@institution.ac.in"
                required
              />

              <Input
                label="Password *"
                icon={Lock}
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="••••••••"
                required
              />
            </div>

            {/* Submit Button */}
            <Button
              type="submit"
              className="w-full mt-2"
              isLoading={isSubmitting}
              size="lg"
            >
              Complete Registration & Sign In <ArrowRight className="w-4 h-4 ml-1.5" />
            </Button>
          </form>

          {/* Footer Back to Login */}
          <div className="mt-5 pt-4 border-t border-slate-100 flex items-center justify-between text-xs">
            <Link
              to="/login"
              className="inline-flex items-center text-slate-500 hover:text-slate-800 font-medium transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5 mr-1" /> Back to Login
            </Link>
            <span className="text-[11px] text-slate-400">
              Immediate auto-login enabled
            </span>
          </div>
        </div>

        {/* Helper Note */}
        <p className="text-[11px] text-slate-500 text-center mt-4">
          Tip: You can comment out this registration component in <code className="text-slate-400 font-mono text-[10px]">AppRoutes.jsx</code> or <code className="text-slate-400 font-mono text-[10px]">Login.jsx</code> to disable it anytime.
        </p>
      </div>
    </div>
  );
};

export default Register;

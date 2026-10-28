import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Users,
  Building2,
  Copy,
  AlertTriangle,
  Clock,
  UploadCloud,
  ArrowRight,
  ShieldAlert,
  History,
  CheckCircle2
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import { dashboardService } from '../../services/dashboardService';
import { useAuth } from '../../context/AuthContext';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { LoadingSpinner } from '../../components/feedback/LoadingSpinner';

const COLORS = ['#2563eb', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4'];

export const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const isRegistrar = user?.role === 'COLLEGE_REGISTRAR';
  const [academicYear, setAcademicYear] = useState('2024-2025');

  useEffect(() => {
    loadDashboard(academicYear);
  }, [academicYear]);

  const loadDashboard = async (year) => {
    setIsLoading(true);
    setHasError(false);
    try {
      const summary = await dashboardService.getSummary(isRegistrar ? { academicYear: year } : {});
      setData(summary);
    } catch (e) {
      console.error('Error loading dashboard summary', e);
      setHasError(true);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner message="Aggregating platform metrics from database..." />;
  }

  if (hasError || !data) {
    return (
      <div className="p-8 text-center bg-white rounded-2xl border border-slate-200 space-y-4 max-w-lg mx-auto mt-12">
        <div className="w-12 h-12 bg-rose-50 text-rose-600 rounded-2xl flex items-center justify-center mx-auto">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <h3 className="text-base font-bold text-slate-900">Dashboard Metrics Unavailable</h3>
        <p className="text-xs text-slate-500">
          Unable to retrieve real-time platform metrics. Please verify that your session is active and the backend ledger service is running.
        </p>
        <div className="flex justify-center gap-3">
          <Button variant="secondary" size="sm" onClick={() => navigate('/login')}>
            Re-authenticate (Login)
          </Button>
          <Button size="sm" onClick={() => loadDashboard(academicYear)}>
            Retry Loading
          </Button>
        </div>
      </div>
    );
  }

  // Format Recharts data
  const institutionChartData = Object.entries(data.studentsByInstitution || {}).map(([name, count]) => ({
    name: name.length > 20 ? name.substring(0, 18) + '...' : name,
    students: count
  }));

  const programChartData = Object.entries(data.studentsByProgram || {}).map(([name, count]) => ({
    name: name.length > 20 ? name.substring(0, 18) + '...' : name,
    students: count
  }));

  // Status chart data
  const verifiedCount = data.studentsByStatus?.['VERIFIED'] || 0;
  const pendingCount = data.studentsByStatus?.['PENDING_REVIEW'] || 0;
  const totalStatusStudents = (data.totalStudents || 1);

  const statusChartData = isRegistrar
    ? [
        { name: 'Verified', value: verifiedCount, color: '#10b981' },
        { name: 'Pending Review', value: pendingCount, color: '#f59e0b' }
      ]
    : Object.entries(data.studentsByStatus || {}).map(([status, count], i) => ({
        name: status,
        value: count,
        color: COLORS[i % COLORS.length]
      }));

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-slate-900 to-slate-800 rounded-2xl p-6 text-white shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-500/20 text-blue-300">
              Institutional Security Ledger
            </span>
            <span className="text-xs text-slate-400">• Synthetic AICTE Grid</span>
          </div>
          <h2 className="text-xl font-bold tracking-tight">
            Welcome back, {user?.fullName}
          </h2>
          <p className="text-xs text-slate-300 mt-1 max-w-xl">
            {user?.role === 'AICTE_ADMIN'
              ? 'Monitoring cross-institutional student records, approved intake limits, and cryptographic verification status.'
              : `Managing student enrollments and verification integrity for ${user?.institutionName || 'your institution'}.`}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => navigate('/verification')}
          >
            Verify Credential
          </Button>
          <Button
            size="sm"
            onClick={() => navigate('/students')}
          >
            View Student Records
          </Button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className={`grid grid-cols-1 sm:grid-cols-2 ${isRegistrar ? 'lg:grid-cols-3' : 'lg:grid-cols-4'} gap-4`}>
        <Card className="hover:border-slate-300 transition-colors">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500">
                {isRegistrar ? 'Total Enrolled (Our Institution)' : 'Total Enrolled Students'}
              </p>
              <h3 className="text-2xl font-bold text-slate-900 mt-1">{data.totalStudents}</h3>
              <p className="text-[11px] text-slate-400 mt-0.5">Synthetic APAAR Records</p>
            </div>
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <Users className="w-5 h-5" />
            </div>
          </div>
        </Card>

        {/* Section 2.1: Remove Approved Institutions for Registrar */}
        {!isRegistrar && (
          <Card className="hover:border-slate-300 transition-colors">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold text-slate-500">Approved Institutions</p>
                <h3 className="text-2xl font-bold text-slate-900 mt-1">{data.totalInstitutions}</h3>
                <p className="text-[11px] text-slate-400 mt-0.5">Active AICTE Profiles</p>
              </div>
              <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
                <Building2 className="w-5 h-5" />
              </div>
            </div>
          </Card>
        )}

        {/* Section 2.2: Duplicate Card Navigation */}
        <Card
          className="hover:border-amber-300 transition-all border-amber-200/80 bg-amber-50/20 cursor-pointer hover:shadow-md"
          onClick={() => navigate('/reviews?type=DUPLICATE_CANDIDATE&status=OPEN')}
        >
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-1.5">
                <p className="text-xs font-semibold text-amber-800">Duplicate Candidates</p>
                <ArrowRight className="w-3.5 h-3.5 text-amber-600" />
              </div>
              <h3 className="text-2xl font-bold text-amber-900 mt-1">{data.openDuplicateCases}</h3>
              <p className="text-[11px] text-amber-700/80 mt-0.5">Click to view duplicate review queue</p>
            </div>
            <div className="w-10 h-10 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center">
              <Copy className="w-5 h-5" />
            </div>
          </div>
        </Card>

        {/* Section 2.3: Intake Alerts Card Navigation */}
        <Card
          className="hover:border-rose-300 transition-all border-rose-200/80 bg-rose-50/20 cursor-pointer hover:shadow-md"
          onClick={() => navigate('/reviews?type=INTAKE_VIOLATION&status=OPEN')}
        >
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-1.5">
                <p className="text-xs font-semibold text-rose-800">Intake Alerts</p>
                <ArrowRight className="w-3.5 h-3.5 text-rose-600" />
              </div>
              <h3 className="text-2xl font-bold text-rose-600 mt-1">{data.intakeViolationsCount}</h3>
              <p className="text-[11px] text-rose-700/80 mt-0.5">Click to view excess capacity flags</p>
            </div>
            <div className="w-10 h-10 rounded-xl bg-rose-100 text-rose-600 flex items-center justify-center">
              <AlertTriangle className="w-5 h-5" />
            </div>
          </div>
        </Card>
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Section 2.4: Replace Institution-Based Student Overview for Registrar */}
        {isRegistrar ? (
          <Card
            className="lg:col-span-2"
            title="Department / Program Intake Overview"
            subtitle={`Intake quota compliance for ${user?.institutionName || 'Our Institution'}`}
            action={
              <div className="flex items-center gap-2">
                <label className="text-xs text-slate-500 font-medium">Academic Year:</label>
                <select
                  value={academicYear}
                  onChange={(e) => setAcademicYear(e.target.value)}
                  className="text-xs border border-slate-300 rounded-md px-2 py-1 bg-white text-slate-800 focus:outline-none focus:ring-1 focus:ring-blue-500"
                >
                  <option value="2024-2025">2024-2025</option>
                  <option value="2023-2024">2023-2024</option>
                  <option value="2022-2023">2022-2023</option>
                </select>
              </div>
            }
          >
            <div className="overflow-x-auto">
              {data.programIntakeOverview && data.programIntakeOverview.length > 0 ? (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                    <tr>
                      <th className="py-2.5 px-3">Program / Department</th>
                      <th className="py-2.5 px-3 text-right">Approved Intake</th>
                      <th className="py-2.5 px-3 text-right">Total Admitted</th>
                      <th className="py-2.5 px-3 text-right">Remaining Seats</th>
                      <th className="py-2.5 px-3 text-right">Utilization</th>
                      <th className="py-2.5 px-3 text-center">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {data.programIntakeOverview.map((item, idx) => (
                      <tr key={idx} className="hover:bg-slate-50/70 transition-colors">
                        <td className="py-2.5 px-3 font-semibold text-slate-900">{item.programName}</td>
                        <td className="py-2.5 px-3 text-right font-medium text-slate-700">{item.approvedIntake}</td>
                        <td className="py-2.5 px-3 text-right font-medium text-slate-900">{item.admittedCount}</td>
                        <td className={`py-2.5 px-3 text-right font-semibold ${item.remainingSeats < 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                          {item.remainingSeats}
                        </td>
                        <td className="py-2.5 px-3 text-right">
                          <span className={`font-medium ${item.utilizationPercentage > 100 ? 'text-rose-600 font-bold' : 'text-slate-800'}`}>
                            {item.utilizationPercentage}%
                          </span>
                        </td>
                        <td className="py-2.5 px-3 text-center">
                          <Badge variant={item.status === 'WITHIN_LIMIT' ? 'success' : item.status === 'WARNING' ? 'warning' : 'danger'}>
                            {item.status === 'WITHIN_LIMIT' ? 'Within Limit' : item.status === 'WARNING' ? 'Near Capacity' : 'Intake Alert'}
                          </Badge>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <div className="py-12 text-center text-xs text-slate-500">
                  No program intake quotas configured for academic year {academicYear}.
                </div>
              )}
            </div>
          </Card>
        ) : (
          <Card
            className="lg:col-span-2"
            title="Student Records by Institution"
            subtitle="Real-time distribution across active AICTE institutes"
          >
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={institutionChartData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 11 }} angle={-15} textAnchor="end" interval={0} />
                  <YAxis tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Bar dataKey="students" fill="#2563eb" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </Card>
        )}

        {/* Section 2.5: Verification Status Distribution */}
        <Card
          title="Student Status Distribution"
          subtitle={isRegistrar ? "Verified vs Pending Review records (Our Institution)" : "Proportion of verified vs pending cases"}
        >
          <div className="h-56 w-full flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={statusChartData}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={75}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {statusChartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="flex flex-col gap-2 mt-2 text-xs border-t border-slate-100 pt-3">
            {isRegistrar ? (
              <>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
                    <span className="text-slate-700 font-medium">Verified Students:</span>
                  </div>
                  <span className="font-bold text-slate-900">
                    {verifiedCount} ({totalStatusStudents > 0 ? ((verifiedCount / totalStatusStudents) * 100).toFixed(1) : 0}%)
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded-full bg-amber-500"></span>
                    <span className="text-slate-700 font-medium">Pending Review:</span>
                  </div>
                  <span className="font-bold text-slate-900">
                    {pendingCount} ({totalStatusStudents > 0 ? ((pendingCount / totalStatusStudents) * 100).toFixed(1) : 0}%)
                  </span>
                </div>
              </>
            ) : (
              <div className="flex flex-wrap justify-center gap-3">
                {statusChartData.map((entry, idx) => (
                  <div key={idx} className="flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: entry.color }}></span>
                    <span className="text-slate-600 font-medium">{entry.name}: {entry.value}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>
      </div>

      {/* Recent Reviews and Audit Event Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Open Review Cases */}
        <Card
          title="Active Review Center Cases"
          subtitle="Pending cases requiring verification decisions"
          action={
            <Button variant="ghost" size="sm" onClick={() => navigate('/reviews')}>
              Review All <ArrowRight className="w-3.5 h-3.5 ml-1" />
            </Button>
          }
        >
          <div className="space-y-3">
            {data.recentReviewCases && data.recentReviewCases.length > 0 ? (
              data.recentReviewCases.map((rc) => (
                <div
                  key={rc.id}
                  className="p-3 rounded-lg border border-slate-100 bg-slate-50/60 hover:bg-slate-100/80 transition-colors flex items-center justify-between cursor-pointer"
                  onClick={() => navigate('/reviews')}
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-slate-800">{rc.caseNumber}</span>
                      <Badge variant={rc.caseType}>{rc.caseType.replace('_', ' ')}</Badge>
                      <Badge variant={rc.status}>{rc.status}</Badge>
                    </div>
                    <p className="text-xs text-slate-600 font-medium">
                      Student: <span className="text-slate-900 font-semibold">{rc.targetStudentName || 'N/A'}</span> • {rc.institutionName}
                    </p>
                  </div>
                  <Button variant="secondary" size="sm">Inspect</Button>
                </div>
              ))
            ) : (
              <p className="text-xs text-slate-500 py-6 text-center">No open review cases at this time.</p>
            )}
          </div>
        </Card>

        {/* Recent Audit Logs */}
        <Card
          title="Recent Audit Ledger Trail"
          subtitle="Chronological system and credential events"
          action={
            <Button variant="ghost" size="sm" onClick={() => navigate('/audit-logs')}>
              Full Log <ArrowRight className="w-3.5 h-3.5 ml-1" />
            </Button>
          }
        >
          <div className="space-y-2.5">
            {data.recentAuditLogs && data.recentAuditLogs.slice(0, 5).map((log) => (
              <div
                key={log.id}
                className="p-2.5 rounded-lg border border-slate-100 flex items-center justify-between text-xs"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-800">{log.action}</span>
                    <span className="text-[10px] text-slate-400 font-mono">[{log.correlationId}]</span>
                  </div>
                  <p className="text-[11px] text-slate-500 mt-0.5">
                    User: <span className="font-medium text-slate-700">{log.username}</span> • {log.reason}
                  </p>
                </div>
                <div className="text-right shrink-0">
                  <Badge variant={log.outcome === 'SUCCESS' ? 'success' : 'danger'} size="sm">
                    {log.outcome}
                  </Badge>
                  <p className="text-[10px] text-slate-400 mt-1">{log.timestamp?.substring(11, 16) || ''}</p>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Scale,
  AlertTriangle,
  CheckCircle2,
  TrendingUp,
  Building2,
  Mail,
  Phone,
  MapPin,
  Eye,
  Info,
  ShieldAlert
} from 'lucide-react';
import { complianceService } from '../../services/complianceService';
import { institutionService } from '../../services/institutionService';
import { useAuth } from '../../context/AuthContext';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import { LoadingSpinner } from '../../components/feedback/LoadingSpinner';

export const Compliance = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const isRegistrar = user?.role === 'COLLEGE_REGISTRAR';

  const [complianceList, setComplianceList] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [currentInstitution, setCurrentInstitution] = useState(null);
  const [selectedInstId, setSelectedInstId] = useState('');
  const [selectedYear, setSelectedYear] = useState('2024-2025');
  const [isLoading, setIsLoading] = useState(true);

  // Flag inspection modal
  const [inspectedFlag, setInspectedFlag] = useState(null);

  useEffect(() => {
    loadInstitutions();
  }, []);

  useEffect(() => {
    loadCompliance();
  }, [selectedInstId, selectedYear]);

  const loadInstitutions = async () => {
    try {
      const data = await institutionService.getAllInstitutions();
      setInstitutions(data);
      if (isRegistrar && user?.institutionId) {
        const myInst = data.find((i) => i.id === user.institutionId);
        if (myInst) setCurrentInstitution(myInst);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const loadCompliance = async () => {
    setIsLoading(true);
    try {
      const data = await complianceService.getIntakeCompliance({
        institutionId: selectedInstId || undefined,
        academicYear: selectedYear
      });
      setComplianceList(data);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  const exceededPrograms = complianceList.filter((item) => item.isExceeded);

  // Total configured intake derivation
  const totalApprovedIntake = complianceList.reduce((acc, curr) => acc + (curr.approvedIntake || 0), 0);
  const totalAdmittedStudents = complianceList.reduce((acc, curr) => acc + (curr.currentAdmitted || curr.admittedCount || 0), 0);

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900">Approved Intake & Quota Compliance</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Configurable, rules-based monitoring of AICTE-approved enrollment capacity per program
          </p>
        </div>
      </div>

      {/* Section 6.1: Institution Information Card for College Registrar */}
      {isRegistrar && currentInstitution && (
        <Card className="bg-gradient-to-r from-blue-900 to-slate-900 text-white p-6 shadow-sm">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-blue-500/30 text-blue-300 uppercase tracking-wider">
                  Registered Institution Profile
                </span>
                <span className="text-xs text-slate-400 font-mono">[{currentInstitution.code}]</span>
              </div>
              <h3 className="text-lg font-bold tracking-tight text-white">{currentInstitution.name}</h3>
              <div className="flex flex-wrap gap-4 text-xs text-slate-300 pt-1">
                <div className="flex items-center gap-1.5">
                  <MapPin className="w-3.5 h-3.5 text-blue-400" />
                  <span>{currentInstitution.city}, {currentInstitution.state}</span>
                </div>
                {currentInstitution.contactEmail && (
                  <div className="flex items-center gap-1.5">
                    <Mail className="w-3.5 h-3.5 text-blue-400" />
                    <span>{currentInstitution.contactEmail}</span>
                  </div>
                )}
                {currentInstitution.contactPhone && (
                  <div className="flex items-center gap-1.5">
                    <Phone className="w-3.5 h-3.5 text-blue-400" />
                    <span>{currentInstitution.contactPhone}</span>
                  </div>
                )}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 shrink-0">
              <div className="bg-white/10 backdrop-blur-xs p-3 rounded-xl border border-white/10 text-center">
                <span className="text-[10px] font-semibold text-slate-300 uppercase block">Total Configured Intake</span>
                <span className="text-xl font-bold text-white mt-0.5 block">
                  {currentInstitution.configuredIntake || totalApprovedIntake} seats
                </span>
                <span className="text-[10px] text-slate-400">Sum of Program Quotas</span>
              </div>
              <div className="bg-white/10 backdrop-blur-xs p-3 rounded-xl border border-white/10 text-center">
                <span className="text-[10px] font-semibold text-slate-300 uppercase block">Total Admitted</span>
                <span className="text-xl font-bold text-emerald-400 mt-0.5 block">
                  {totalAdmittedStudents} students
                </span>
                <span className="text-[10px] text-slate-400">AY {selectedYear}</span>
              </div>
            </div>
          </div>
        </Card>
      )}

      {/* Filter Row with Academic Year Dropdown (Section 6.3) */}
      <Card className="p-4">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex flex-wrap items-center gap-4">
            {!isRegistrar && user?.role === 'AICTE_ADMIN' && (
              <div className="min-w-[220px]">
                <label className="block text-[11px] font-semibold text-slate-500 mb-1">Filter Institution:</label>
                <select
                  value={selectedInstId}
                  onChange={(e) => setSelectedInstId(e.target.value)}
                  className="w-full h-9 rounded-lg border border-slate-200 bg-white px-3 text-xs text-slate-800"
                >
                  <option value="">All Institutions</option>
                  {institutions.map((i) => (
                    <option key={i.id} value={i.id}>{i.name}</option>
                  ))}
                </select>
              </div>
            )}

            <div>
              <label className="block text-[11px] font-semibold text-slate-500 mb-1">Academic Year:</label>
              <select
                value={selectedYear}
                onChange={(e) => setSelectedYear(e.target.value)}
                className="h-9 rounded-lg border border-slate-200 bg-white px-3 text-xs text-slate-800 font-medium"
              >
                <option value="2024-2025">2024-2025</option>
                <option value="2023-2024">2023-2024</option>
                <option value="2022-2023">2022-2023</option>
                <option value="2021-2022">2021-2022</option>
              </select>
            </div>
          </div>

          <div className="text-xs text-slate-500 flex items-center gap-1.5">
            <Info className="w-4 h-4 text-slate-400" />
            <span>Official Rule: Intake counts officially admitted eligible students; uncommitted/withdrawn records excluded.</span>
          </div>
        </div>
      </Card>

      {/* Excess Intake Alert Banner */}
      {exceededPrograms.length > 0 && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-900 flex items-start justify-between gap-3">
          <div className="flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
            <div>
              <h4 className="font-bold text-sm">Excess Intake Detected ({exceededPrograms.length} Programs)</h4>
              <p className="mt-0.5 text-rose-700">
                The following programs have admitted more students than the configured AICTE approved intake limit for {selectedYear}.
                Under policy, detected anomalies are treated as reviewable alerts rather than automatic penalties.
              </p>
            </div>
          </div>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => navigate('/reviews?type=INTAKE_VIOLATION&status=OPEN')}
          >
            Open Intake Reviews
          </Button>
        </div>
      )}

      {/* Grid of Programs (Section 6.2) */}
      {isLoading ? (
        <LoadingSpinner message="Calculating intake compliance statistics..." />
      ) : complianceList.length === 0 ? (
        <Card className="p-12 text-center text-slate-500 text-xs">
          <p className="font-semibold text-sm text-slate-700">Quota Data Unavailable for this Academic Year</p>
          <p className="mt-1">No program intake limits are configured for academic year {selectedYear}.</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {complianceList.map((item, index) => {
            const isExceeded = item.isExceeded;
            const percentage = Math.min(item.utilizationPercentage, 100);
            const remaining = item.approvedIntake - item.currentAdmitted;
            const excess = Math.max(0, item.currentAdmitted - item.approvedIntake);

            return (
              <Card
                key={index}
                className={`transition-all hover:shadow-sm ${
                  isExceeded ? 'border-rose-300 bg-rose-50/10' : 'border-slate-200'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
                      {item.institutionName}
                    </span>
                    <h4 className="text-sm font-bold text-slate-900 mt-1">{item.programName}</h4>
                  </div>
                  {isExceeded ? (
                    <Badge variant="danger">Intake Alert</Badge>
                  ) : item.utilizationPercentage >= 90 ? (
                    <Badge variant="warning">Near Limit</Badge>
                  ) : (
                    <Badge variant="success">Within Limit</Badge>
                  )}
                </div>

                {/* Metrics Breakdown */}
                <div className="grid grid-cols-3 gap-2 mt-4 p-2.5 bg-slate-50 rounded-lg text-center text-xs">
                  <div>
                    <span className="text-[10px] text-slate-500 block">Approved</span>
                    <span className="font-bold text-slate-800">{item.approvedIntake}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-500 block">Admitted</span>
                    <span className="font-bold text-slate-900">{item.currentAdmitted}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-500 block">
                      {isExceeded ? 'Excess' : 'Available'}
                    </span>
                    <span className={`font-bold ${isExceeded ? 'text-rose-600' : 'text-emerald-600'}`}>
                      {isExceeded ? `+${excess}` : remaining}
                    </span>
                  </div>
                </div>

                {/* Utilization Progress Meter */}
                <div className="mt-4 space-y-1.5">
                  <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                    <div
                      className={`h-2.5 rounded-full transition-all duration-500 ${
                        isExceeded ? 'bg-rose-500' : percentage > 85 ? 'bg-amber-500' : 'bg-blue-600'
                      }`}
                      style={{ width: `${percentage}%` }}
                    />
                  </div>

                  <div className="flex justify-between text-[11px] text-slate-400 pt-1">
                    <span>Intake Utilization:</span>
                    <span className={`font-bold ${isExceeded ? 'text-rose-600' : 'text-slate-700'}`}>
                      {item.utilizationPercentage}%
                    </span>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100 text-[10px] text-slate-500 flex justify-between items-center">
                  <span>AY: {item.academicYear}</span>
                  {isExceeded && (
                    <button
                      onClick={() => setInspectedFlag(item)}
                      className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-700 font-semibold text-xs"
                    >
                      <Eye className="w-3.5 h-3.5" /> Inspect Flag
                    </button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Section 6.4: Intake Flag Inspection Modal */}
      {inspectedFlag && (
        <Modal
          isOpen={!!inspectedFlag}
          onClose={() => setInspectedFlag(null)}
          title={`Intake Alert: ${inspectedFlag.programName}`}
          subtitle={`Capacity anomaly detected for academic year ${inspectedFlag.academicYear}`}
        >
          <div className="space-y-4 text-xs">
            <div className="p-3 bg-rose-50 rounded-xl border border-rose-200 text-rose-900 flex items-start gap-2.5">
              <ShieldAlert className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
              <div>
                <p className="font-bold text-sm">Excess Seat Utilization Detected</p>
                <p className="mt-0.5 text-rose-800 leading-relaxed">
                  Admitted students ({inspectedFlag.currentAdmitted}) exceed the configured AICTE limit of ({inspectedFlag.approvedIntake}) by <strong>{inspectedFlag.currentAdmitted - inspectedFlag.approvedIntake}</strong> seats.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 p-3 bg-slate-50 rounded-xl border border-slate-200">
              <div>
                <span className="text-[11px] text-slate-500 block">Department / Program</span>
                <span className="font-bold text-slate-900">{inspectedFlag.programName}</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-500 block">Academic Year</span>
                <span className="font-bold text-slate-900">{inspectedFlag.academicYear}</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-500 block">Approved Intake</span>
                <span className="font-bold text-slate-900">{inspectedFlag.approvedIntake} seats</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-500 block">Total Admitted Students</span>
                <span className="font-bold text-slate-900">{inspectedFlag.currentAdmitted} students</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-500 block">Excess Seats</span>
                <span className="font-bold text-rose-600">+{inspectedFlag.currentAdmitted - inspectedFlag.approvedIntake} seats</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-500 block">Compliance Status</span>
                <Badge variant="danger">INTAKE ALERT</Badge>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
              <Button variant="secondary" onClick={() => setInspectedFlag(null)}>
                Close
              </Button>
              <Button
                variant="primary"
                onClick={() => {
                  setInspectedFlag(null);
                  navigate('/reviews?type=INTAKE_VIOLATION&status=OPEN');
                }}
              >
                View in Review Center
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

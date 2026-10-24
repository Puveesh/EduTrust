import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  ShieldAlert,
  Search,
  Filter,
  CheckCircle2,
  XCircle,
  Eye,
  Clock,
  User,
  AlertTriangle,
  ArrowRight,
  HelpCircle,
  Copy,
  ExternalLink,
  History,
  Trash2
} from 'lucide-react';
import { reviewService } from '../../services/reviewService';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { DataTable } from '../../components/tables/DataTable';

export const Reviews = () => {
  const { user } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [cases, setCases] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters initialized from URL query params (for Dashboard navigation)
  const [selectedType, setSelectedType] = useState(searchParams.get('type') || '');
  const [selectedStatus, setSelectedStatus] = useState(searchParams.get('status') || '');

  // Resolution & Update Modal State
  const [selectedCase, setSelectedCase] = useState(null);
  const [resolutionStatus, setResolutionStatus] = useState('RESOLVED');
  const [resolutionReason, setResolutionReason] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isEditingDecision, setIsEditingDecision] = useState(false);

  // Dismissal Modal State
  const [isDismissModalOpen, setIsDismissModalOpen] = useState(false);
  const [dismissReason, setDismissReason] = useState('');
  const [isDismissing, setIsDismissing] = useState(false);

  useEffect(() => {
    const urlType = searchParams.get('type');
    const urlStatus = searchParams.get('status');
    if (urlType !== null) setSelectedType(urlType);
    if (urlStatus !== null) setSelectedStatus(urlStatus);
  }, [searchParams]);

  useEffect(() => {
    loadCases();
  }, [page, selectedType, selectedStatus]);

  const loadCases = async () => {
    setIsLoading(true);
    try {
      const res = await reviewService.getCases({
        page,
        size: 10,
        caseType: selectedType || undefined,
        status: selectedStatus || undefined
      });
      setCases(res.content || []);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements || 0);
    } catch (e) {
      showToast('Failed to load review cases', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenResolve = (reviewCase) => {
    setSelectedCase(reviewCase);
    setIsEditingDecision(false);
    setResolutionStatus(
      reviewCase.status !== 'OPEN' && reviewCase.status !== 'UNDER_REVIEW'
        ? reviewCase.status
        : (reviewCase.caseType === 'DUPLICATE_CANDIDATE' ? 'CONFIRMED_DUPLICATE' : 'RESOLVED')
    );
    setResolutionReason(reviewCase.resolutionReason || '');
  };

  const handleResolveSubmit = async (e) => {
    e.preventDefault();
    if (!resolutionReason.trim()) {
      showToast('Mandatory resolution reason is required', 'warning');
      return;
    }

    setIsSubmitting(true);
    try {
      await reviewService.resolveCase(selectedCase.id, {
        status: resolutionStatus,
        resolutionReason: resolutionReason.trim()
      });
      showToast(`Case ${selectedCase.caseNumber} decision saved as ${resolutionStatus}`, 'success');
      setSelectedCase(null);
      setIsEditingDecision(false);
      loadCases();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to record decision', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDismissSubmit = async (e) => {
    e.preventDefault();
    if (!dismissReason.trim()) {
      showToast('Mandatory dismissal reason is required', 'warning');
      return;
    }

    setIsDismissing(true);
    try {
      await reviewService.dismissCase(selectedCase.id, {
        reason: dismissReason.trim()
      });
      showToast(`Case ${selectedCase.caseNumber} dismissed / marked false positive`, 'info');
      setIsDismissModalOpen(false);
      setSelectedCase(null);
      setDismissReason('');
      loadCases();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to dismiss review case', 'error');
    } finally {
      setIsDismissing(false);
    }
  };

  const parseEvidence = (evidenceJson) => {
    try {
      return JSON.parse(evidenceJson);
    } catch (e) {
      return { note: evidenceJson };
    }
  };

  const columns = [
    {
      header: 'Case ID',
      accessor: (row) => (
        <div>
          <span className="font-mono font-bold text-slate-800 text-xs">{row.caseNumber}</span>
          <p className="text-[10px] text-slate-400 mt-0.5">{new Date(row.createdAt).toLocaleDateString()}</p>
        </div>
      )
    },
    {
      header: 'Case Type',
      accessor: (row) => <Badge variant={row.caseType}>{row.caseType.replace('_', ' ')}</Badge>
    },
    {
      header: 'Target Student / Institution',
      accessor: (row) => (
        <div>
          <p className="font-semibold text-slate-900">{row.targetStudentName || 'Unknown Student'}</p>
          <p className="text-[10px] text-slate-500">{row.institutionName}</p>
        </div>
      )
    },
    {
      header: 'Severity',
      accessor: (row) => (
        <span
          className={`inline-flex px-2 py-0.5 rounded text-[10px] font-bold ${
            row.severity === 'HIGH' || row.severity === 'CRITICAL'
              ? 'bg-rose-100 text-rose-800'
              : row.severity === 'MEDIUM'
              ? 'bg-amber-100 text-amber-800'
              : 'bg-slate-100 text-slate-700'
          }`}
        >
          {row.severity}
        </span>
      )
    },
    {
      header: 'Current Status',
      accessor: (row) => <Badge variant={row.status}>{row.status.replace('_', ' ')}</Badge>
    },
    {
      header: 'Actions',
      accessor: (row) => (
        <Button
          variant={row.status === 'OPEN' || row.status === 'UNDER_REVIEW' ? 'primary' : 'secondary'}
          size="sm"
          onClick={() => handleOpenResolve(row)}
        >
          {row.status === 'OPEN' || row.status === 'UNDER_REVIEW' ? 'Review & Decide' : 'View Decision'}
        </Button>
      )
    }
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold text-slate-900">Review & Conflict Center</h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Investigate cross-institutional duplicate candidate flags, intake limit exceptions, and data corrections
        </p>
      </div>

      {/* Filter Tabs */}
      <Card className="p-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex rounded-lg border border-slate-200 p-0.5 bg-slate-50 text-xs">
            <button
              onClick={() => { setSelectedType(''); setPage(0); }}
              className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
                selectedType === '' ? 'bg-white shadow-2xs text-slate-900 font-semibold' : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              All Types
            </button>
            <button
              onClick={() => { setSelectedType('DUPLICATE_CANDIDATE'); setPage(0); }}
              className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
                selectedType === 'DUPLICATE_CANDIDATE' ? 'bg-white shadow-2xs text-slate-900 font-semibold' : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Duplicate Candidates
            </button>
            <button
              onClick={() => { setSelectedType('INTAKE_VIOLATION'); setPage(0); }}
              className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
                selectedType === 'INTAKE_VIOLATION' ? 'bg-white shadow-2xs text-slate-900 font-semibold' : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Intake Violations
            </button>
            <button
              onClick={() => { setSelectedType('INVALID_DATA'); setPage(0); }}
              className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
                selectedType === 'INVALID_DATA' ? 'bg-white shadow-2xs text-slate-900 font-semibold' : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Invalid Data
            </button>
          </div>

          <div className="ml-auto flex items-center gap-2">
            <label className="text-xs font-semibold text-slate-500">Status:</label>
            <select
              value={selectedStatus}
              onChange={(e) => { setSelectedStatus(e.target.value); setPage(0); }}
              className="h-8 rounded-lg border border-slate-200 bg-white px-2.5 text-xs text-slate-800"
            >
              <option value="">All Statuses</option>
              <option value="OPEN">Open</option>
              <option value="UNDER_REVIEW">Under Review</option>
              <option value="CONFIRMED_DUPLICATE">Confirmed Duplicate</option>
              <option value="NOT_DUPLICATE">Not Duplicate (Legitimate)</option>
              <option value="RESOLVED">Resolved</option>
            </select>
          </div>
        </div>
      </Card>

      {/* Case Table */}
      <Card>
        <DataTable
          columns={columns}
          data={cases}
          isLoading={isLoading}
          page={page}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={(p) => setPage(p)}
          emptyTitle="No Review Cases"
          emptyDescription="There are currently no cases matching your selected criteria."
        />
      </Card>

      {/* Case Investigation & Decision Modal */}
      {selectedCase && (
        <Modal
          isOpen={!!selectedCase}
          onClose={() => setSelectedCase(null)}
          title={`Review Case: ${selectedCase.caseNumber}`}
          subtitle={`Investigation details for ${selectedCase.caseType.replace('_', ' ')}`}
          maxWidth="max-w-xl"
        >
          <div className="space-y-4">
            {/* Case Info Pill */}
            <div className="flex items-center justify-between p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs">
              <div>
                <span className="text-slate-500">Institution: </span>
                <strong className="text-slate-800">{selectedCase.institutionName}</strong>
              </div>
              <div className="flex items-center gap-2">
                <Badge variant={selectedCase.severity}>{selectedCase.severity}</Badge>
                <Badge variant={selectedCase.status}>{selectedCase.status}</Badge>
              </div>
            </div>

            {/* Evidence Display Box */}
            <div className="p-4 rounded-xl bg-blue-50/60 border border-blue-200 text-xs">
              <h4 className="font-bold text-blue-900 mb-2 flex items-center gap-1.5">
                <ShieldAlert className="w-4 h-4 text-blue-600" /> Authorized Evidence & Findings
              </h4>

              {(() => {
                const evidence = parseEvidence(selectedCase.evidenceJson);
                return (
                  <div className="space-y-2 text-slate-700">
                    {evidence.note && <p className="font-medium text-slate-800">{evidence.note}</p>}

                    {evidence.matchedStudentName && (
                      <div className="bg-white p-2.5 rounded-lg border border-blue-100 text-[11px] space-y-1">
                        <p><strong className="text-slate-900">Matched Student:</strong> {evidence.matchedStudentName}</p>
                        <p><strong className="text-slate-900">Cross Institution:</strong> {evidence.matchedInstitutionName}</p>
                        <p><strong className="text-slate-900">Shared Reference:</strong> <span className="font-mono text-blue-700">{evidence.syntheticApaarId}</span></p>
                      </div>
                    )}

                    {evidence.approvedIntake && (
                      <div className="bg-white p-2.5 rounded-lg border border-blue-100 text-[11px] space-y-1">
                        <p><strong className="text-slate-900">Program:</strong> {evidence.program}</p>
                        <p><strong className="text-slate-900">Approved Capacity:</strong> {evidence.approvedIntake} seats</p>
                        <p><strong className="text-slate-900">Admitted Students:</strong> {evidence.admittedCount} students</p>
                        <p className="text-rose-600 font-bold">Excess Utilization: +{evidence.excessIntake} seats over approved limit</p>
                      </div>
                    )}
                  </div>
                );
              })()}
            </div>

            {/* Associated Student Action Link */}
            {selectedCase.targetStudentName && (
              <div className="flex items-center justify-between p-2.5 bg-slate-50 rounded-lg border border-slate-200">
                <span className="text-slate-600">Target Student Record:</span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => navigate(`/students?query=${encodeURIComponent(selectedCase.targetStudentName)}`)}
                  icon={ExternalLink}
                >
                  Inspect / Correct Student
                </Button>
              </div>
            )}

            {/* Decision History Timeline (Section 5.4) */}
            {selectedCase.decisionHistoryJson && (() => {
              try {
                const history = JSON.parse(selectedCase.decisionHistoryJson);
                if (Array.isArray(history) && history.length > 0) {
                  return (
                    <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
                      <h5 className="font-bold text-slate-800 flex items-center gap-1.5 text-xs">
                        <History className="w-3.5 h-3.5 text-blue-600" /> Decision & Adjudication History
                      </h5>
                      <div className="space-y-2 divide-y divide-slate-100">
                        {history.map((h, i) => (
                          <div key={i} className="pt-1.5 text-[11px] space-y-0.5">
                            <div className="flex items-center justify-between">
                              <span className="font-semibold text-slate-900">
                                {h.previousStatus ? `${h.previousStatus} → ${h.newStatus}` : h.newStatus}
                              </span>
                              <span className="text-slate-400 font-mono text-[10px]">{h.timestamp}</span>
                            </div>
                            <p className="text-slate-600 italic">"{h.newDecision || h.reason}"</p>
                            <p className="text-[10px] text-slate-500 font-medium">Recorded by: {h.amendedBy || 'Authorized Registrar'}</p>
                          </div>
                        ))}
                      </div>
                    </div>
                  );
                }
              } catch (e) {
                return null;
              }
              return null;
            })()}

            {/* If already resolved, show resolution summary with option to Amend or Dismiss */}
            {selectedCase.resolvedBy && !isEditingDecision ? (
              <div className="p-3.5 bg-emerald-50 rounded-xl border border-emerald-200 text-xs text-emerald-900 space-y-2">
                <div className="flex items-center justify-between">
                  <p className="font-bold">Current Adjudication Decision</p>
                  <Badge variant={selectedCase.status}>{selectedCase.status}</Badge>
                </div>
                <p className="mt-1">
                  Recorded by: <strong>{selectedCase.resolvedBy}</strong> on {selectedCase.resolvedAt ? new Date(selectedCase.resolvedAt).toLocaleDateString() : 'Active Record'}
                </p>
                <p className="text-slate-700 italic bg-white/70 p-2 rounded border border-emerald-100">
                  "{selectedCase.resolutionReason}"
                </p>
                <div className="flex justify-between items-center pt-2 border-t border-emerald-200/60">
                  {selectedCase.status !== 'DISMISSED' && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setIsDismissModalOpen(true)}
                      className="text-rose-600 hover:text-rose-700 hover:bg-rose-50"
                      icon={Trash2}
                    >
                      Dismiss Case / False Positive
                    </Button>
                  )}
                  <div className="flex gap-2 ml-auto">
                    <Button variant="secondary" size="sm" onClick={() => setSelectedCase(null)}>
                      Close
                    </Button>
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => setIsEditingDecision(true)}
                    >
                      Amend Decision
                    </Button>
                  </div>
                </div>
              </div>
            ) : (
              /* Resolution & Amendment Form */
              <form onSubmit={handleResolveSubmit} className="space-y-4 pt-2">
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label className="block text-xs font-semibold text-slate-700">
                      Select Adjudication Decision <span className="text-rose-500">*</span>
                    </label>
                    {isEditingDecision && (
                      <span className="text-[11px] text-amber-700 font-semibold bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
                        Amending Existing Decision
                      </span>
                    )}
                  </div>
                  <select
                    value={resolutionStatus}
                    onChange={(e) => setResolutionStatus(e.target.value)}
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs text-slate-900 font-medium"
                  >
                    {selectedCase.caseType === 'DUPLICATE_CANDIDATE' ? (
                      <>
                        <option value="UNDER_REVIEW">Mark Under Active Investigation</option>
                        <option value="CONFIRMED_DUPLICATE">Confirm Duplicate (Block Duplicate Admission)</option>
                        <option value="NOT_DUPLICATE">Not Duplicate (Legitimate Multi-Program / Transfer)</option>
                        <option value="RESOLVED">Resolved / Closed</option>
                      </>
                    ) : (
                      <>
                        <option value="UNDER_REVIEW">Mark Under Review</option>
                        <option value="RESOLVED">Approve / Resolve Exception</option>
                      </>
                    )}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Mandatory Resolution Reason / Justification <span className="text-rose-500">*</span>
                  </label>
                  <textarea
                    value={resolutionReason}
                    onChange={(e) => setResolutionReason(e.target.value)}
                    placeholder="Document evidence verification, cross-institution registrar confirmation, or corrective rationale..."
                    rows={3}
                    required
                    className="w-full rounded-lg border border-slate-200 p-2.5 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-100"
                  />
                </div>

                <div className="flex justify-between items-center pt-3 border-t border-slate-100">
                  {selectedCase.status !== 'DISMISSED' && (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => setIsDismissModalOpen(true)}
                      className="text-rose-600 hover:text-rose-700 hover:bg-rose-50"
                      icon={Trash2}
                    >
                      Dismiss Case
                    </Button>
                  )}
                  <div className="flex space-x-2 ml-auto">
                    <Button variant="secondary" onClick={() => { setSelectedCase(null); setIsEditingDecision(false); }}>
                      Cancel
                    </Button>
                    <Button type="submit" isLoading={isSubmitting}>
                      {isEditingDecision ? 'Update Decision & Log History' : 'Record Decision & Audit'}
                    </Button>
                  </div>
                </div>
              </form>
            )}
          </div>
        </Modal>
      )}

      {/* Section 5.3: Soft Dismissal Modal */}
      <Modal
        isOpen={isDismissModalOpen}
        onClose={() => setIsDismissModalOpen(false)}
        title={`Dismiss Review Case: ${selectedCase?.caseNumber}`}
        subtitle="Safely dismiss or mark false-positive without erasing historical audit records"
      >
        <form onSubmit={handleDismissSubmit} className="space-y-4 text-xs">
          <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl text-amber-900">
            <p className="font-semibold">Review Case Dismissal Policy</p>
            <p className="mt-1 text-amber-800">
              This case will be soft-dismissed as <strong>DISMISSED</strong>. Original case telemetry, target student association, and full history remain permanently in the audit trail.
            </p>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Mandatory Reason for Dismissal / False-Positive Marking <span className="text-rose-500">*</span>
            </label>
            <textarea
              value={dismissReason}
              onChange={(e) => setDismissReason(e.target.value)}
              placeholder="Explain why this review case was flagged erroneously or resolved as false-positive..."
              rows={3}
              required
              className="w-full rounded-lg border border-slate-200 p-2.5 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-100"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsDismissModalOpen(false)} disabled={isDismissing}>
              Cancel
            </Button>
            <Button variant="danger" type="submit" isLoading={isDismissing}>
              Confirm Dismissal
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

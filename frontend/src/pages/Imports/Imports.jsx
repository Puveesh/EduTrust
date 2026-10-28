import React, { useState, useEffect } from 'react';
import {
  UploadCloud,
  Download,
  CheckCircle2,
  AlertTriangle,
  AlertCircle,
  FileText,
  Filter,
  Check,
  X,
  Edit2,
  ArrowRight,
  ShieldAlert,
  Clock,
  RotateCcw
} from 'lucide-react';
import { importService } from '../../services/importService';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { LoadingSpinner } from '../../components/feedback/LoadingSpinner';

export const Imports = () => {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [batches, setBatches] = useState([]);
  const [currentBatch, setCurrentBatch] = useState(null);
  const [batchRows, setBatchRows] = useState([]);
  const [rowStatusFilter, setRowStatusFilter] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [isCommitting, setIsCommitting] = useState(false);

  // Edit Row State
  const [editingRow, setEditingRow] = useState(null);
  const [editFormData, setEditFormData] = useState({
    syntheticApaarId: '',
    studentName: '',
    program: '',
    academicYear: '',
    admissionYear: 2024,
    admissionCategory: 'GENERAL'
  });

  // Commit Summary Modal State
  const [commitSummary, setCommitSummary] = useState(null);
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
  const [isCancelling, setIsCancelling] = useState(false);

  useEffect(() => {
    loadBatches();
  }, []);

  const loadBatches = async () => {
    try {
      const res = await importService.getBatches({ size: 10 });
      setBatches(res.content || []);
    } catch (e) {
      console.error(e);
    }
  };

  const handleDownloadTemplate = async () => {
    try {
      await importService.downloadTemplate();
      showToast('Sample CSV template downloaded successfully', 'success');
    } catch (e) {
      showToast('Failed to download template', 'error');
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.name.endsWith('.csv')) {
      showToast('Please upload a valid CSV file (.csv)', 'error');
      return;
    }

    setIsUploading(true);
    try {
      const res = await importService.uploadAndPreview(file, user?.institutionId);
      setCurrentBatch(res.batch);
      setBatchRows(res.rows || []);
      showToast(`CSV preview generated: ${res.batch.totalRows} rows staged for validation`, 'success');
      loadBatches();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to upload and parse CSV', 'error');
    } finally {
      setIsUploading(false);
    }
  };

  const handleSelectBatch = async (batch) => {
    setCurrentBatch(batch);
    setIsLoading(true);
    try {
      const res = await importService.getBatchRows(batch.id, {
        status: rowStatusFilter || undefined,
        size: 50
      });
      setBatchRows(res.content || []);
    } catch (e) {
      showToast('Failed to load batch rows', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleExclude = async (row) => {
    const newStatus = row.status === 'EXCLUDED' ? 'VALID' : 'EXCLUDED';
    try {
      await importService.updateBatchRow(currentBatch.id, row.id, { status: newStatus });
      showToast(`Row #${row.rowNumber} ${newStatus === 'EXCLUDED' ? 'excluded from' : 'included in'} import`, 'info');
      // Refresh
      const updatedRows = await importService.getBatchRows(currentBatch.id, { size: 50 });
      setBatchRows(updatedRows.content || []);
      const updatedBatch = await importService.getBatchById(currentBatch.id);
      setCurrentBatch(updatedBatch);
      loadBatches();
    } catch (e) {
      showToast('Error updating row exclusion', 'error');
    }
  };

  const handleOpenEditRow = (row) => {
    setEditingRow(row);
    setEditFormData({
      syntheticApaarId: row.syntheticApaarId || '',
      studentName: row.studentName || '',
      program: row.program || 'Computer Science and Engineering',
      academicYear: row.academicYear || '2024-2025',
      admissionYear: row.admissionYear || 2024,
      admissionCategory: row.admissionCategory || 'GENERAL'
    });
  };

  const handleSaveRowEdit = async (e) => {
    e.preventDefault();
    try {
      await importService.updateBatchRow(currentBatch.id, editingRow.id, editFormData);
      showToast(`Row #${editingRow.rowNumber} corrected and re-validated`, 'success');
      setEditingRow(null);
      // Refresh
      const updatedRows = await importService.getBatchRows(currentBatch.id, { size: 50 });
      setBatchRows(updatedRows.content || []);
      const updatedBatch = await importService.getBatchById(currentBatch.id);
      setCurrentBatch(updatedBatch);
      loadBatches();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to update row', 'error');
    }
  };

  const handleCommitBatch = async () => {
    if (!currentBatch) return;
    setIsCommitting(true);
    try {
      const summary = await importService.commitBatch(currentBatch.id);
      setCommitSummary(summary);
      showToast('Import batch committed successfully!', 'success');
      const updatedBatch = await importService.getBatchById(currentBatch.id);
      setCurrentBatch(updatedBatch);
      loadBatches();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to commit batch', 'error');
    } finally {
      setIsCommitting(false);
    }
  };

  const handleCancelBatch = async () => {
    if (!currentBatch) return;
    setIsCancelling(true);
    try {
      const cancelledBatch = await importService.cancelBatch(currentBatch.id);
      setCurrentBatch(cancelledBatch);
      showToast(`Batch #${currentBatch.id} cancelled. Staged rows discarded.`, 'info');
      setIsCancelModalOpen(false);
      loadBatches();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to cancel batch', 'error');
    } finally {
      setIsCancelling(false);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900">Institutional Data Import Wizard</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Stage, validate, inspect duplicate candidates, correct anomalies, and commit student enrollment datasets
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <Button variant="secondary" onClick={handleDownloadTemplate} icon={Download}>
            Download CSV Template
          </Button>
        </div>
      </div>

      {/* Upload Box */}
      <Card className="border-dashed border-2 border-slate-300 hover:border-blue-400 bg-slate-50/40 text-center p-8 transition-colors">
        <div className="max-w-md mx-auto">
          <div className="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center mx-auto mb-3 shadow-xs">
            <UploadCloud className="w-6 h-6" />
          </div>
          <h3 className="text-sm font-semibold text-slate-900">Upload Institution Enrollment CSV</h3>
          <p className="text-xs text-slate-500 mt-1 mb-4">
            Upload CSV containing synthetic student data. Staged records will be re-validated and evaluated for cross-institution duplicates before permanent database entry.
          </p>
          <label className="inline-flex">
            <Button
              type="button"
              isLoading={isUploading}
              className="cursor-pointer"
              onClick={() => document.getElementById('csvFileInput').click()}
            >
              Select CSV File
            </Button>
            <input
              id="csvFileInput"
              type="file"
              accept=".csv"
              onChange={handleFileUpload}
              className="hidden"
            />
          </label>
        </div>
      </Card>

      {/* Active Staging & Preview Area */}
      {currentBatch && (
        <Card
          title={`Batch Staging Review: ${currentBatch.fileName}`}
          subtitle={`Uploaded by ${currentBatch.uploadedBy} on ${new Date(currentBatch.createdAt).toLocaleString()}`}
          action={
            <div className="flex items-center gap-2">
              <Badge variant={currentBatch.status}>{currentBatch.status}</Badge>

              {/* Section 4.1: Cancel/Remove Batch Action for uncommitted batches */}
              {currentBatch.status !== 'COMMITTED' && currentBatch.status !== 'CANCELLED' && (
                <>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => setIsCancelModalOpen(true)}
                    title="Cancel and discard this staged batch"
                  >
                    Cancel Batch
                  </Button>
                  <Button
                    variant="success"
                    size="sm"
                    isLoading={isCommitting}
                    disabled={currentBatch.invalidRows > 0}
                    onClick={handleCommitBatch}
                    icon={CheckCircle2}
                  >
                    Commit Batch to Registry
                  </Button>
                </>
              )}
            </div>
          }
        >
          {/* Batch Status Metrics */}
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 mb-6 p-4 rounded-xl bg-slate-50 border border-slate-200/80 text-center">
            <div>
              <p className="text-[11px] font-semibold text-slate-500 uppercase">Total Rows</p>
              <p className="text-lg font-bold text-slate-800 mt-0.5">{currentBatch.totalRows}</p>
            </div>
            <div>
              <p className="text-[11px] font-semibold text-emerald-600 uppercase">Valid Rows</p>
              <p className="text-lg font-bold text-emerald-700 mt-0.5">{currentBatch.validRows}</p>
            </div>
            <div>
              <p className="text-[11px] font-semibold text-rose-600 uppercase">Invalid Rows</p>
              <p className="text-lg font-bold text-rose-700 mt-0.5">{currentBatch.invalidRows}</p>
            </div>
            <div>
              <p className="text-[11px] font-semibold text-amber-600 uppercase">Duplicate Candidates</p>
              <p className="text-lg font-bold text-amber-700 mt-0.5">{currentBatch.duplicateCandidates}</p>
            </div>
            <div>
              <p className="text-[11px] font-semibold text-slate-500 uppercase">Excluded</p>
              <p className="text-lg font-bold text-slate-600 mt-0.5">{currentBatch.excludedRows}</p>
            </div>
          </div>

          {currentBatch.invalidRows > 0 && (
            <div className="mb-4 p-3 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-800 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Attention: Invalid Rows Detected</p>
                <p className="mt-0.5 text-rose-700">
                  You must correct invalid rows or toggle them to 'Excluded' before committing this batch to the permanent student database.
                </p>
              </div>
            </div>
          )}

          {/* Staged Rows Table */}
          <div className="overflow-x-auto rounded-lg border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200 text-left text-xs">
              <thead className="bg-slate-50 font-semibold text-slate-700">
                <tr>
                  <th className="px-3.5 py-3">Row #</th>
                  <th className="px-3.5 py-3">Synthetic APAAR</th>
                  <th className="px-3.5 py-3">Student Name</th>
                  <th className="px-3.5 py-3">Program</th>
                  <th className="px-3.5 py-3">Academic Year</th>
                  <th className="px-3.5 py-3">Category</th>
                  <th className="px-3.5 py-3">Status & Notes</th>
                  <th className="px-3.5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white text-slate-600">
                {batchRows.map((row) => (
                  <tr
                    key={row.id}
                    className={`hover:bg-slate-50/80 transition-colors ${
                      row.status === 'EXCLUDED' ? 'opacity-50 bg-slate-50' : ''
                    }`}
                  >
                    <td className="px-3.5 py-2.5 font-mono font-medium text-slate-400">#{row.rowNumber}</td>
                    <td className="px-3.5 py-2.5 font-mono font-semibold text-slate-800">{row.syntheticApaarId}</td>
                    <td className="px-3.5 py-2.5 font-medium text-slate-900">{row.studentName}</td>
                    <td className="px-3.5 py-2.5">{row.program}</td>
                    <td className="px-3.5 py-2.5">{row.academicYear}</td>
                    <td className="px-3.5 py-2.5">{row.admissionCategory}</td>
                    <td className="px-3.5 py-2.5">
                      <Badge variant={row.status}>{row.status}</Badge>
                      {row.duplicateExplanation && (
                        <p className="text-[10px] text-amber-700 mt-1 max-w-xs">{row.duplicateExplanation}</p>
                      )}
                      {row.validationErrorsJson && (
                        <p className="text-[10px] text-rose-600 mt-1 max-w-xs font-mono">{row.validationErrorsJson}</p>
                      )}
                    </td>
                    <td className="px-3.5 py-2.5 text-right whitespace-nowrap">
                      {currentBatch.status !== 'COMMITTED' && (
                        <div className="flex items-center justify-end space-x-1.5">
                          <button
                            onClick={() => handleOpenEditRow(row)}
                            className="p-1 text-slate-500 hover:text-blue-600 hover:bg-slate-100 rounded"
                            title="Edit row fields"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleToggleExclude(row)}
                            className={`p-1 rounded ${
                              row.status === 'EXCLUDED'
                                ? 'text-emerald-600 hover:bg-emerald-50'
                                : 'text-slate-400 hover:text-rose-600 hover:bg-rose-50'
                            }`}
                            title={row.status === 'EXCLUDED' ? 'Include row' : 'Exclude row'}
                          >
                            {row.status === 'EXCLUDED' ? <RotateCcw className="w-3.5 h-3.5" /> : <X className="w-3.5 h-3.5" />}
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* Batch History Card */}
      <Card title="Previous Import Batches" subtitle="Audit log of staged and committed enrollment uploads">
        <div className="overflow-x-auto rounded-lg border border-slate-200">
          <table className="min-w-full divide-y divide-slate-200 text-left text-xs">
            <thead className="bg-slate-50 font-semibold text-slate-700">
              <tr>
                <th className="px-4 py-3">Batch ID</th>
                <th className="px-4 py-3">File Name</th>
                <th className="px-4 py-3">Institution</th>
                <th className="px-4 py-3">Total Rows</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Created Date</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 bg-white text-slate-600">
              {batches.map((b) => (
                <tr key={b.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="px-4 py-3 font-mono font-semibold text-slate-800">BATCH-{b.id}</td>
                  <td className="px-4 py-3 font-medium text-slate-900">{b.fileName}</td>
                  <td className="px-4 py-3">{b.institutionName}</td>
                  <td className="px-4 py-3">{b.totalRows}</td>
                  <td className="px-4 py-3">
                    <Badge variant={b.status}>{b.status}</Badge>
                  </td>
                  <td className="px-4 py-3 text-slate-400">{new Date(b.createdAt).toLocaleString()}</td>
                  <td className="px-4 py-3 text-right">
                    <Button variant="secondary" size="sm" onClick={() => handleSelectBatch(b)}>
                      Inspect
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Edit Row Modal */}
      <Modal
        isOpen={!!editingRow}
        onClose={() => setEditingRow(null)}
        title={`Correct Row #${editingRow?.rowNumber}`}
        subtitle="Edit cell values before committing"
      >
        <form onSubmit={handleSaveRowEdit} className="space-y-4">
          <Input
            label="Synthetic APAAR Reference"
            value={editFormData.syntheticApaarId}
            onChange={(e) => setEditFormData({ ...editFormData, syntheticApaarId: e.target.value })}
            required
          />
          <Input
            label="Student Full Name"
            value={editFormData.studentName}
            onChange={(e) => setEditFormData({ ...editFormData, studentName: e.target.value })}
            required
          />
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Program</label>
              <select
                value={editFormData.program}
                onChange={(e) => setEditFormData({ ...editFormData, program: e.target.value })}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs"
              >
                <option value="Computer Science and Engineering">Computer Science</option>
                <option value="Information Technology">Information Technology</option>
                <option value="Electronics and Communication">Electronics & Comm</option>
                <option value="Mechanical Engineering">Mechanical Engineering</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Admission Category</label>
              <select
                value={editFormData.admissionCategory}
                onChange={(e) => setEditFormData({ ...editFormData, admissionCategory: e.target.value })}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs"
              >
                <option value="GENERAL">General</option>
                <option value="OBC">OBC</option>
                <option value="SC">SC</option>
                <option value="ST">ST</option>
                <option value="EWS">EWS</option>
                <option value="MANAGEMENT">Management Quota</option>
              </select>
            </div>
          </div>
          <div className="flex justify-end space-x-2 pt-4 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setEditingRow(null)}>
              Cancel
            </Button>
            <Button type="submit">
              Save & Revalidate Row
            </Button>
          </div>
        </form>
      </Modal>

      {/* Commit Success Summary Modal */}
      <Modal
        isOpen={!!commitSummary}
        onClose={() => setCommitSummary(null)}
        title="Batch Import Successful"
        subtitle="Staged records have been transactionally committed"
      >
        <div className="space-y-4">
          <div className="p-4 bg-emerald-50 rounded-xl border border-emerald-200 flex items-center gap-3">
            <CheckCircle2 className="w-8 h-8 text-emerald-600 shrink-0" />
            <div>
              <h4 className="font-bold text-emerald-900 text-sm">Commit Successful</h4>
              <p className="text-xs text-emerald-700 mt-0.5">{commitSummary?.message}</p>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <p className="text-[11px] font-semibold text-slate-500 uppercase">Committed</p>
              <p className="text-xl font-bold text-emerald-700 mt-1">{commitSummary?.committedRecordsCount}</p>
            </div>
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <p className="text-[11px] font-semibold text-slate-500 uppercase">Excluded</p>
              <p className="text-xl font-bold text-slate-600 mt-1">{commitSummary?.excludedRecordsCount}</p>
            </div>
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <p className="text-[11px] font-semibold text-slate-500 uppercase">Flagged Reviews</p>
              <p className="text-xl font-bold text-amber-700 mt-1">{commitSummary?.flaggedReviewCasesCount}</p>
            </div>
          </div>

          {commitSummary?.flaggedCaseNumbers && commitSummary.flaggedCaseNumbers.length > 0 && (
            <div className="p-3 bg-amber-50 rounded-lg border border-amber-200 text-xs text-amber-900">
              <p className="font-semibold mb-1">Generated Review Cases:</p>
              <div className="flex flex-wrap gap-1.5">
                {commitSummary.flaggedCaseNumbers.map((cNum) => (
                  <Badge key={cNum} variant="warning">{cNum}</Badge>
                ))}
              </div>
            </div>
          )}

          <div className="flex justify-end pt-3">
            <Button onClick={() => setCommitSummary(null)}>
              Done
            </Button>
          </div>
        </div>
      </Modal>

      {/* Section 4.1: Cancel Batch Confirmation Modal */}
      <Modal
        isOpen={isCancelModalOpen}
        onClose={() => setIsCancelModalOpen(false)}
        title="Cancel Uncommitted Import Batch"
        subtitle={`Confirm cancellation for batch BATCH-${currentBatch?.id} (${currentBatch?.fileName})`}
      >
        <div className="space-y-4 text-xs">
          <div className="p-3.5 bg-rose-50 rounded-xl border border-rose-200 text-rose-900 flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-sm">Destructive Action Notice</p>
              <p className="mt-1 leading-relaxed text-rose-800">
                You are about to cancel this staged upload containing <strong>{currentBatch?.totalRows}</strong> rows.
                Once cancelled, this batch will transition to a permanent <strong>CANCELLED</strong> state and will no longer be eligible for commit to the student registry. All staged row validations will be safely discarded.
              </p>
            </div>
          </div>

          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 space-y-1.5 text-slate-600">
            <div className="flex justify-between">
              <span>Batch ID:</span>
              <span className="font-mono font-bold text-slate-800">BATCH-{currentBatch?.id}</span>
            </div>
            <div className="flex justify-between">
              <span>File Name:</span>
              <span className="font-medium text-slate-800">{currentBatch?.fileName}</span>
            </div>
            <div className="flex justify-between">
              <span>Current Status:</span>
              <Badge variant={currentBatch?.status}>{currentBatch?.status}</Badge>
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsCancelModalOpen(false)} disabled={isCancelling}>
              Keep Batch
            </Button>
            <Button
              variant="danger"
              isLoading={isCancelling}
              onClick={handleCancelBatch}
            >
              Confirm Cancellation
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

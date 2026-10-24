import React, { useEffect, useState } from 'react';
import {
  Users,
  Search,
  Filter,
  Plus,
  Edit2,
  History,
  Award,
  Download,
  AlertCircle,
  Building2,
  CheckCircle2,
  Eye,
  ShieldCheck,
  ShieldAlert,
  Info
} from 'lucide-react';
import { studentService } from '../../services/studentService';
import { institutionService } from '../../services/institutionService';
import { credentialService } from '../../services/credentialService';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { DataTable } from '../../components/tables/DataTable';
import { StudentHistoryModal } from './StudentHistoryModal';

export const Students = () => {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [students, setStudents] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Pagination & Filtering state
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedInstId, setSelectedInstId] = useState('');
  const [selectedProgram, setSelectedProgram] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedCredentialFilter, setSelectedCredentialFilter] = useState('');

  // Modals
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isHistoryModalOpen, setIsHistoryModalOpen] = useState(false);
  const [isIssueModalOpen, setIsIssueModalOpen] = useState(false);
  const [isInspectModalOpen, setIsInspectModalOpen] = useState(false);
  const [selectedStudent, setSelectedStudent] = useState(null);

  // Form State
  const [formData, setFormData] = useState({
    institutionId: '',
    syntheticApaarId: '',
    enrollmentNumber: '',
    studentName: '',
    program: 'Computer Science and Engineering',
    academicYear: '2024-2025',
    admissionYear: 2024,
    admissionCategory: 'GENERAL',
    admissionStatus: 'ADMITTED',
    gender: 'MALE',
    updateReason: ''
  });

  const [issueData, setIssueData] = useState({
    qualification: 'Bachelor of Technology in Engineering',
    completionYear: 2024,
    cgpaOrGrade: '8.8 CGPA'
  });

  useEffect(() => {
    loadInstitutions();
  }, []);

  useEffect(() => {
    loadStudents();
  }, [page, searchQuery, selectedInstId, selectedProgram, selectedCategory, selectedStatus, selectedCredentialFilter]);

  const loadInstitutions = async () => {
    try {
      const data = await institutionService.getAllInstitutions();
      setInstitutions(data);
    } catch (e) {
      console.error(e);
    }
  };

  const loadStudents = async () => {
    setIsLoading(true);
    try {
      const credParam = selectedCredentialFilter === '' ? undefined : (selectedCredentialFilter === 'true');
      const res = await studentService.getStudents({
        page,
        size: 10,
        query: searchQuery || undefined,
        institutionId: selectedInstId || undefined,
        program: selectedProgram || undefined,
        category: selectedCategory || undefined,
        status: selectedStatus || undefined,
        credentialIssued: credParam
      });
      setStudents(res.content || []);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements || 0);
    } catch (e) {
      showToast('Failed to load student records', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenAdd = () => {
    const defaultInst = user?.role === 'COLLEGE_REGISTRAR' ? user.institutionId : (institutions[0]?.id || '');
    setFormData({
      institutionId: defaultInst,
      syntheticApaarId: `APAAR-SYN-2024-${Math.floor(1000 + Math.random() * 9000)}`,
      enrollmentNumber: `ENR-${Math.floor(1000 + Math.random() * 9000)}`,
      studentName: '',
      program: 'Computer Science and Engineering',
      academicYear: '2024-2025',
      admissionYear: 2024,
      admissionCategory: 'GENERAL',
      admissionStatus: 'ADMITTED',
      gender: 'MALE',
      updateReason: ''
    });
    setIsAddModalOpen(true);
  };

  const handleCreateStudent = async (e) => {
    e.preventDefault();
    try {
      await studentService.createStudent(formData);
      showToast('Student record successfully registered in registry', 'success');
      setIsAddModalOpen(false);
      loadStudents();
    } catch (err) {
      showToast(err.response?.data?.message || 'Error creating student record', 'error');
    }
  };

  const handleOpenEdit = (student) => {
    setSelectedStudent(student);
    setFormData({
      studentName: student.studentName,
      program: student.program,
      academicYear: student.academicYear,
      admissionYear: student.admissionYear,
      admissionCategory: student.admissionCategory,
      admissionStatus: student.admissionStatus,
      gender: student.gender,
      updateReason: ''
    });
    setIsEditModalOpen(true);
  };

  const handleUpdateStudent = async (e) => {
    e.preventDefault();
    if (!formData.updateReason.trim()) {
      showToast('Mandatory update reason is required for audit trail', 'warning');
      return;
    }
    try {
      await studentService.updateStudent(selectedStudent.id, formData);
      showToast('Student record updated and logged to audit trail', 'success');
      setIsEditModalOpen(false);
      loadStudents();
    } catch (err) {
      showToast(err.response?.data?.message || 'Error updating student record', 'error');
    }
  };

  const handleOpenInspect = (student) => {
    setSelectedStudent(student);
    setIsInspectModalOpen(true);
  };

  const handleOpenIssue = (student) => {
    // Section 3.1: Frontend issuance eligibility restriction
    if (student.verificationStatus !== 'VERIFIED') {
      showToast('Credential Issuance Blocked: Student verification status must be VERIFIED. (Currently ' + student.verificationStatus + ')', 'error');
      return;
    }
    if (student.admissionStatus !== 'ADMITTED') {
      showToast('Credential Issuance Blocked: Student must be in officially ADMITTED status.', 'error');
      return;
    }
    if (student.credentialIssued) {
      showToast('Credential Issuance Blocked: An active signed credential already exists for this student.', 'warning');
      return;
    }

    setSelectedStudent(student);
    setIssueData({
      qualification: `Bachelor of Technology in ${student.program}`,
      completionYear: student.admissionYear ? student.admissionYear + 4 : 2024,
      cgpaOrGrade: '8.8 CGPA'
    });
    setIsIssueModalOpen(true);
  };

  const handleIssueCredential = async (e) => {
    e.preventDefault();
    try {
      await credentialService.issueCredential({
        studentId: selectedStudent.id,
        ...issueData
      });
      showToast('Cryptographically signed digital credential issued successfully', 'success');
      setIsIssueModalOpen(false);
      loadStudents();
    } catch (err) {
      showToast(err.response?.data?.message || 'Error issuing credential', 'error');
    }
  };

  const columns = [
    {
      header: 'Synthetic APAAR ID',
      accessor: (row) => (
        <div>
          <span className="font-mono font-semibold text-blue-700 bg-blue-50 px-2 py-0.5 rounded text-[11px]">
            {row.syntheticApaarId}
          </span>
          <p className="text-[10px] text-slate-400 mt-0.5 font-mono">{row.enrollmentNumber}</p>
        </div>
      )
    },
    {
      header: 'Student Name',
      accessor: (row) => (
        <div>
          <p className="font-semibold text-slate-900">{row.studentName}</p>
          <p className="text-[10px] text-slate-500">{row.gender} • Category: {row.admissionCategory}</p>
        </div>
      )
    },
    {
      header: 'Institution',
      accessor: (row) => (
        <span className="text-slate-700 font-medium truncate max-w-[180px] block" title={row.institutionName}>
          {row.institutionName}
        </span>
      )
    },
    {
      header: 'Program & Cohort',
      accessor: (row) => (
        <div>
          <p className="text-slate-800 font-medium">{row.program}</p>
          <p className="text-[10px] text-slate-500">AY: {row.academicYear} (Adm: {row.admissionYear})</p>
        </div>
      )
    },
    {
      header: 'Verification Status',
      accessor: (row) => <Badge variant={row.verificationStatus}>{row.verificationStatus}</Badge>
    },
    {
      header: 'Credential Status',
      accessor: (row) => (
        <div className="flex items-center gap-1.5" title={row.credentialIssued ? `Credential Issued: ${row.credentialId || ''} (${row.credentialIssueDate || ''})` : 'Credential not issued'}>
          {row.credentialIssued ? (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 fill-emerald-100" />
              <span>Issued</span>
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-slate-100 text-slate-500 border border-slate-200">
              <Award className="w-3.5 h-3.5 text-slate-400" />
              <span>Not Issued</span>
            </span>
          )}
        </div>
      )
    },
    {
      header: 'Actions',
      accessor: (row) => {
        const canIssue = row.verificationStatus === 'VERIFIED' && row.admissionStatus === 'ADMITTED' && !row.credentialIssued;
        return (
          <div className="flex items-center space-x-1.5">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => handleOpenInspect(row)}
              title="Inspect Student Record"
            >
              <Eye className="w-3.5 h-3.5 text-blue-600" />
            </Button>

            <Button
              variant="ghost"
              size="sm"
              onClick={() => handleOpenEdit(row)}
              title="Edit Student Record"
            >
              <Edit2 className="w-3.5 h-3.5 text-slate-600" />
            </Button>

            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setSelectedStudent(row);
                setIsHistoryModalOpen(true);
              }}
              title="View Change History"
            >
              <History className="w-3.5 h-3.5 text-slate-600" />
            </Button>

            <Button
              variant="ghost"
              size="sm"
              onClick={() => handleOpenIssue(row)}
              disabled={!canIssue}
              className={!canIssue ? "opacity-30 cursor-not-allowed" : ""}
              title={
                !canIssue
                  ? (row.credentialIssued
                      ? "Credential already issued"
                      : "Credential issuance restricted to VERIFIED students")
                  : "Issue Digitally Signed Credential"
              }
            >
              <Award className={`w-3.5 h-3.5 ${canIssue ? "text-blue-600" : "text-slate-400"}`} />
            </Button>
          </div>
        );
      }
    }
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top action header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900">Student Registry</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Institutional student records managed via synthetic APAAR references
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <Button onClick={handleOpenAdd} icon={Plus}>
            Add Student Record
          </Button>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <Card className="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-6 gap-3">
          <div className="lg:col-span-2">
            <Input
              placeholder="Search by student name, synthetic APAAR ID, enrollment..."
              icon={Search}
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPage(0);
              }}
            />
          </div>

          {user?.role === 'AICTE_ADMIN' && (
            <div>
              <select
                value={selectedInstId}
                onChange={(e) => {
                  setSelectedInstId(e.target.value);
                  setPage(0);
                }}
                className="w-full h-9 rounded-lg border border-slate-200 bg-white px-3 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-100"
              >
                <option value="">All Institutions</option>
                {institutions.map((inst) => (
                  <option key={inst.id} value={inst.id}>
                    {inst.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div>
            <select
              value={selectedProgram}
              onChange={(e) => {
                setSelectedProgram(e.target.value);
                setPage(0);
              }}
              className="w-full h-9 rounded-lg border border-slate-200 bg-white px-3 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-100"
            >
              <option value="">All Programs</option>
              <option value="Computer Science and Engineering">Computer Science</option>
              <option value="Information Technology">Information Technology</option>
              <option value="Electronics and Communication">Electronics & Comm</option>
              <option value="Mechanical Engineering">Mechanical Engineering</option>
            </select>
          </div>

          <div>
            <select
              value={selectedStatus}
              onChange={(e) => {
                setSelectedStatus(e.target.value);
                setPage(0);
              }}
              className="w-full h-9 rounded-lg border border-slate-200 bg-white px-3 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-100"
            >
              <option value="">All Verification Statuses</option>
              <option value="VERIFIED">Verified</option>
              <option value="PENDING_REVIEW">Pending Review</option>
              <option value="FLAGGED">Flagged</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>

          {/* Section 3.4: Credential Issuance Filters */}
          <div>
            <select
              value={selectedCredentialFilter}
              onChange={(e) => {
                setSelectedCredentialFilter(e.target.value);
                setPage(0);
              }}
              className="w-full h-9 rounded-lg border border-slate-200 bg-white px-3 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-100 font-medium"
            >
              <option value="">All Credential Statuses</option>
              <option value="true">Credential Issued</option>
              <option value="false">Credential Not Issued</option>
            </select>
          </div>
        </div>
      </Card>

      {/* Student Data Table */}
      <Card>
        <DataTable
          columns={columns}
          data={students}
          isLoading={isLoading}
          page={page}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={(newPage) => setPage(newPage)}
          emptyTitle="No Student Records Found"
          emptyDescription="No student records match your query or filter criteria. Try resetting filters or register a student."
        />
      </Card>

      {/* History Modal */}
      <StudentHistoryModal
        isOpen={isHistoryModalOpen}
        onClose={() => setIsHistoryModalOpen(false)}
        student={selectedStudent}
      />

      {/* Add Student Modal */}
      <Modal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        title="Register New Student Record"
        subtitle="Create an authorized synthetic student record in the AICTE EduTrust ledger"
      >
        <form onSubmit={handleCreateStudent} className="space-y-4">
          {user?.role === 'AICTE_ADMIN' && (
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Target Institution</label>
              <select
                value={formData.institutionId}
                onChange={(e) => setFormData({ ...formData, institutionId: e.target.value })}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs"
                required
              >
                {institutions.map((i) => (
                  <option key={i.id} value={i.id}>{i.name} ({i.code})</option>
                ))}
              </select>
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Synthetic APAAR Reference"
              value={formData.syntheticApaarId}
              onChange={(e) => setFormData({ ...formData, syntheticApaarId: e.target.value })}
              required
            />
            <Input
              label="Enrollment / Roll Number"
              value={formData.enrollmentNumber}
              onChange={(e) => setFormData({ ...formData, enrollmentNumber: e.target.value })}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Student Full Name"
              value={formData.studentName}
              onChange={(e) => setFormData({ ...formData, studentName: e.target.value })}
              placeholder="e.g. Ananya Sen"
              required
            />
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Gender</label>
              <select
                value={formData.gender}
                onChange={(e) => setFormData({ ...formData, gender: e.target.value })}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs"
              >
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Academic Program</label>
              <select
                value={formData.program}
                onChange={(e) => setFormData({ ...formData, program: e.target.value })}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs"
              >
                <option value="Computer Science and Engineering">Computer Science & Engg</option>
                <option value="Information Technology">Information Technology</option>
                <option value="Electronics and Communication">Electronics & Comm</option>
                <option value="Mechanical Engineering">Mechanical Engineering</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Admission Category</label>
              <select
                value={formData.admissionCategory}
                onChange={(e) => setFormData({ ...formData, admissionCategory: e.target.value })}
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
            <Button variant="secondary" onClick={() => setIsAddModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit">
              Register Student
            </Button>
          </div>
        </form>
      </Modal>

      {/* Edit Student Modal */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title="Edit Student Record"
        subtitle={`Editing ${selectedStudent?.studentName} (${selectedStudent?.syntheticApaarId})`}
      >
        <form onSubmit={handleUpdateStudent} className="space-y-4">
          <Input
            label="Student Full Name"
            value={formData.studentName}
            onChange={(e) => setFormData({ ...formData, studentName: e.target.value })}
            required
          />

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Program</label>
              <select
                value={formData.program}
                onChange={(e) => setFormData({ ...formData, program: e.target.value })}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs"
              >
                <option value="Computer Science and Engineering">Computer Science & Engg</option>
                <option value="Information Technology">Information Technology</option>
                <option value="Electronics and Communication">Electronics & Comm</option>
                <option value="Mechanical Engineering">Mechanical Engineering</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Admission Category</label>
              <select
                value={formData.admissionCategory}
                onChange={(e) => setFormData({ ...formData, admissionCategory: e.target.value })}
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

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Mandatory Reason for Update <span className="text-rose-500">*</span>
            </label>
            <textarea
              value={formData.updateReason}
              onChange={(e) => setFormData({ ...formData, updateReason: e.target.value })}
              placeholder="Provide justification for correction or data modification (logged to immutable audit trail)..."
              rows={3}
              required
              className="w-full rounded-lg border border-slate-200 p-2.5 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-100"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-4 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit">
              Save & Audit Changes
            </Button>
          </div>
        </form>
      </Modal>

      {/* Issue Credential Modal */}
      <Modal
        isOpen={isIssueModalOpen}
        onClose={() => setIsIssueModalOpen(false)}
        title="Issue Digitally Signed Academic Credential"
        subtitle={`Generate tamper-resistant cryptographic credential for ${selectedStudent?.studentName}`}
      >
        <form onSubmit={handleIssueCredential} className="space-y-4">
          <div className="p-3 bg-blue-50/70 rounded-xl border border-blue-200 text-xs text-blue-900 flex items-start gap-2">
            <CheckCircle2 className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
            <p>
              This action will sign the canonical academic claims using the server's RSA-2048 private key.
              Verifiers can authenticate this claim online or offline using public key cryptography.
            </p>
          </div>

          <Input
            label="Degree / Qualification Awarded"
            value={issueData.qualification}
            onChange={(e) => setIssueData({ ...issueData, qualification: e.target.value })}
            required
          />

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Completion Year"
              type="number"
              value={issueData.completionYear}
              onChange={(e) => setIssueData({ ...issueData, completionYear: parseInt(e.target.value) })}
              required
            />
            <Input
              label="CGPA / Grade"
              value={issueData.cgpaOrGrade}
              onChange={(e) => setIssueData({ ...issueData, cgpaOrGrade: e.target.value })}
              required
            />
          </div>

          <div className="flex justify-end space-x-2 pt-4 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsIssueModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" icon={Award}>
              Sign & Issue Credential
            </Button>
          </div>
        </form>
      </Modal>

      {/* Section 3.2: Detailed Student Inspection Modal */}
      <Modal
        isOpen={isInspectModalOpen}
        onClose={() => setIsInspectModalOpen(false)}
        title="Student Record Inspection"
        subtitle={`Authoritative record details for ${selectedStudent?.studentName || 'Student'}`}
      >
        {selectedStudent && (
          <div className="space-y-4 text-xs">
            {/* Top Identity Card */}
            <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between">
              <div>
                <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Synthetic APAAR ID</span>
                <p className="font-mono text-sm font-bold text-blue-700">{selectedStudent.syntheticApaarId}</p>
                <p className="text-[11px] text-slate-500 font-medium mt-0.5">Enrollment: {selectedStudent.enrollmentNumber}</p>
              </div>
              <div className="text-right">
                <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Verification Status</span>
                <div className="mt-1">
                  <Badge variant={selectedStudent.verificationStatus}>{selectedStudent.verificationStatus}</Badge>
                </div>
              </div>
            </div>

            {/* Academic Information */}
            <div className="grid grid-cols-2 gap-3 p-3 bg-white rounded-xl border border-slate-200">
              <div>
                <span className="text-[11px] text-slate-400 block">Student Full Name</span>
                <span className="font-semibold text-slate-900 text-sm">{selectedStudent.studentName}</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-400 block">Institution</span>
                <span className="font-medium text-slate-800">{selectedStudent.institutionName}</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-400 block">Department / Program</span>
                <span className="font-medium text-slate-800">{selectedStudent.program}</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-400 block">Academic Year / Adm. Year</span>
                <span className="font-medium text-slate-800">{selectedStudent.academicYear} (Adm: {selectedStudent.admissionYear})</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-400 block">Admission Category</span>
                <span className="font-medium text-slate-800">{selectedStudent.admissionCategory}</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-400 block">Admission Status</span>
                <Badge variant={selectedStudent.admissionStatus === 'ADMITTED' ? 'success' : 'neutral'}>
                  {selectedStudent.admissionStatus}
                </Badge>
              </div>
              <div>
                <span className="text-[11px] text-slate-400 block">Gender</span>
                <span className="font-medium text-slate-800">{selectedStudent.gender || 'Not specified'}</span>
              </div>
              <div>
                <span className="text-[11px] text-slate-400 block">Date of Birth</span>
                <span className="font-medium text-slate-800">{selectedStudent.dateOfBirth || 'N/A'}</span>
              </div>
            </div>

            {/* Credential Status Box */}
            <div className={`p-3.5 rounded-xl border ${selectedStudent.credentialIssued ? 'bg-emerald-50/60 border-emerald-200' : 'bg-slate-50 border-slate-200'}`}>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  {selectedStudent.credentialIssued ? (
                    <ShieldCheck className="w-5 h-5 text-emerald-600" />
                  ) : (
                    <Award className="w-5 h-5 text-slate-400" />
                  )}
                  <div>
                    <span className="font-semibold text-slate-900 block">
                      {selectedStudent.credentialIssued ? 'Active Signed Credential Issued' : 'No Credential Issued'}
                    </span>
                    <span className="text-[11px] text-slate-500">
                      {selectedStudent.credentialIssued
                        ? `Credential ID: ${selectedStudent.credentialId || 'AICTE-CRED'} • Issued: ${selectedStudent.credentialIssueDate || 'Authoritative Record'}`
                        : 'Eligible for issuance once status is VERIFIED and officially ADMITTED.'}
                    </span>
                  </div>
                </div>
                <div>
                  <Badge variant={selectedStudent.credentialIssued ? 'success' : 'neutral'}>
                    {selectedStudent.credentialIssued ? 'ISSUED' : 'NOT ISSUED'}
                  </Badge>
                </div>
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-100">
              <Button variant="secondary" onClick={() => setIsInspectModalOpen(false)}>
                Close Inspector
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

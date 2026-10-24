import React, { useState, useEffect } from 'react';
import { Building2, Plus, Edit2, Users, MapPin, Mail, Phone, CheckCircle2 } from 'lucide-react';
import { institutionService } from '../../services/institutionService';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { LoadingSpinner } from '../../components/feedback/LoadingSpinner';

export const Institutions = () => {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [institutions, setInstitutions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingInst, setEditingInst] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    code: '',
    state: '',
    city: '',
    approvalStatus: 'APPROVED',
    configuredIntake: 120,
    contactEmail: '',
    contactPhone: ''
  });

  useEffect(() => {
    loadInstitutions();
  }, []);

  const loadInstitutions = async () => {
    setIsLoading(true);
    try {
      const data = await institutionService.getAllInstitutions();
      setInstitutions(data);
    } catch (e) {
      showToast('Failed to load institutions', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingInst(null);
    setFormData({
      name: '',
      code: `AICTE-${Math.floor(100 + Math.random() * 900)}`,
      state: '',
      city: '',
      approvalStatus: 'APPROVED',
      configuredIntake: 120,
      contactEmail: '',
      contactPhone: ''
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (inst) => {
    setEditingInst(inst);
    setFormData({
      name: inst.name,
      code: inst.code,
      state: inst.state,
      city: inst.city,
      approvalStatus: inst.approvalStatus,
      configuredIntake: inst.configuredIntake,
      contactEmail: inst.contactEmail || '',
      contactPhone: inst.contactPhone || ''
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingInst) {
        await institutionService.updateInstitution(editingInst.id, formData);
        showToast('Institution settings updated successfully', 'success');
      } else {
        await institutionService.createInstitution(formData);
        showToast('New AICTE institution registered successfully', 'success');
      }
      setIsModalOpen(false);
      loadInstitutions();
    } catch (err) {
      showToast(err.response?.data?.message || 'Error saving institution', 'error');
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900">AICTE Institutions Directory</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Registered participating institutions, approved intake limits, and regulatory approval statuses
          </p>
        </div>
        {user?.role === 'AICTE_ADMIN' && (
          <Button onClick={handleOpenAdd} icon={Plus}>
            Register Institution
          </Button>
        )}
      </div>

      {/* Grid of Institution Cards */}
      {isLoading ? (
        <LoadingSpinner message="Loading institution profiles..." />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {institutions.map((inst) => (
            <Card key={inst.id} className="hover:shadow-sm transition-shadow flex flex-col justify-between">
              <div>
                <div className="flex items-start justify-between">
                  <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold text-sm">
                    {inst.code.substring(6, 9) || 'INS'}
                  </div>
                  <Badge variant={inst.approvalStatus}>{inst.approvalStatus}</Badge>
                </div>

                <h3 className="text-base font-bold text-slate-900 mt-3">{inst.name}</h3>
                <p className="text-xs font-mono font-semibold text-blue-700 mt-0.5">{inst.code}</p>

                <div className="mt-4 space-y-1.5 text-xs text-slate-600">
                  <p className="flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                    {inst.city}, {inst.state}
                  </p>
                  {inst.contactEmail && (
                    <p className="flex items-center gap-1.5">
                      <Mail className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      {inst.contactEmail}
                    </p>
                  )}
                  {inst.contactPhone && (
                    <p className="flex items-center gap-1.5">
                      <Phone className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      {inst.contactPhone}
                    </p>
                  )}
                </div>
              </div>

              <div className="mt-6 pt-3 border-t border-slate-100 flex items-center justify-between">
                <div>
                  <p className="text-[10px] text-slate-400 uppercase font-semibold">Configured Intake</p>
                  <p className="text-sm font-bold text-slate-800">{inst.configuredIntake} seats</p>
                </div>

                {user?.role === 'AICTE_ADMIN' && (
                  <Button variant="secondary" size="sm" onClick={() => handleOpenEdit(inst)} icon={Edit2}>
                    Configure
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Add / Edit Institution Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingInst ? 'Configure Institution & Intake' : 'Register AICTE Institution'}
        subtitle="Institution profile, campus location, and approved capacity"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Institution Full Name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="e.g. Indian Institute of Information Technology"
            required
          />

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Institution Code"
              value={formData.code}
              onChange={(e) => setFormData({ ...formData, code: e.target.value })}
              required
              disabled={!!editingInst}
            />
            <Input
              label="Configured Intake Limit"
              type="number"
              value={formData.configuredIntake}
              onChange={(e) => setFormData({ ...formData, configuredIntake: parseInt(e.target.value) })}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="State"
              value={formData.state}
              onChange={(e) => setFormData({ ...formData, state: e.target.value })}
              required
            />
            <Input
              label="City"
              value={formData.city}
              onChange={(e) => setFormData({ ...formData, city: e.target.value })}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Contact Email"
              type="email"
              value={formData.contactEmail}
              onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
            />
            <Input
              label="Contact Phone"
              value={formData.contactPhone}
              onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Regulatory Approval Status</label>
            <select
              value={formData.approvalStatus}
              onChange={(e) => setFormData({ ...formData, approvalStatus: e.target.value })}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs"
            >
              <option value="APPROVED">Approved</option>
              <option value="PROVISIONAL">Provisional</option>
              <option value="SUSPENDED">Suspended</option>
            </select>
          </div>

          <div className="flex justify-end space-x-2 pt-4 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit">
              {editingInst ? 'Save Changes' : 'Register Institution'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

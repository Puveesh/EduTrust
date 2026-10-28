import React, { useEffect, useState } from 'react';
import { History, Clock, User, ArrowRight } from 'lucide-react';
import { studentService } from '../../services/studentService';
import { Modal } from '../../components/ui/Modal';
import { LoadingSpinner } from '../../components/feedback/LoadingSpinner';

export const StudentHistoryModal = ({ isOpen, onClose, student }) => {
  const [history, setHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (isOpen && student) {
      loadHistory();
    }
  }, [isOpen, student]);

  const loadHistory = async () => {
    setIsLoading(true);
    try {
      const data = await studentService.getStudentHistory(student.id);
      setHistory(data);
    } catch (err) {
      console.error('Failed to load student history', err);
    } finally {
      setIsLoading(false);
    }
  };

  const renderDiff = (oldJson, newJson) => {
    try {
      const oldObj = oldJson ? JSON.parse(oldJson) : {};
      const newObj = newJson ? JSON.parse(newJson) : {};
      const changedKeys = Object.keys(newObj).filter((k) => oldObj[k] !== newObj[k] && k !== 'updatedAt');

      if (changedKeys.length === 0) {
        return <p className="text-xs text-slate-500">Record updated without field modifications.</p>;
      }

      return (
        <div className="space-y-1.5 mt-2 bg-slate-50 p-3 rounded-lg border border-slate-200 text-xs">
          {changedKeys.map((k) => (
            <div key={k} className="grid grid-cols-3 gap-2">
              <span className="font-semibold text-slate-700 capitalize">{k.replace(/([A-Z])/g, ' $1')}:</span>
              <span className="text-rose-600 line-through truncate">{String(oldObj[k] ?? 'empty')}</span>
              <span className="text-emerald-600 font-medium truncate flex items-center gap-1">
                <ArrowRight className="w-3 h-3 shrink-0" /> {String(newObj[k] ?? 'empty')}
              </span>
            </div>
          ))}
        </div>
      );
    } catch (e) {
      return <pre className="text-[10px] text-slate-600">{newJson}</pre>;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Student Modification History"
      subtitle={`Audit trail for ${student?.studentName} (${student?.syntheticApaarId})`}
      maxWidth="max-w-2xl"
    >
      {isLoading ? (
        <LoadingSpinner message="Fetching historical change records..." />
      ) : history.length === 0 ? (
        <div className="py-8 text-center text-xs text-slate-500">
          <History className="w-8 h-8 mx-auto text-slate-400 mb-2" />
          No manual modifications recorded for this student. Record remains at initial intake state.
        </div>
      ) : (
        <div className="space-y-4">
          {history.map((item, idx) => (
            <div key={item.id || idx} className="p-4 rounded-xl border border-slate-200 bg-white shadow-2xs">
              <div className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-slate-900 px-2 py-0.5 rounded bg-blue-50 text-blue-700">
                    {item.changeType}
                  </span>
                  <span className="text-slate-500 flex items-center gap-1">
                    <User className="w-3 h-3" /> Modified by: <strong className="text-slate-700">{item.modifiedBy}</strong>
                  </span>
                </div>
                <span className="text-[11px] text-slate-400 flex items-center gap-1">
                  <Clock className="w-3 h-3" /> {new Date(item.timestamp).toLocaleString()}
                </span>
              </div>
              {renderDiff(item.oldValueJson, item.newValueJson)}
            </div>
          ))}
        </div>
      )}
    </Modal>
  );
};

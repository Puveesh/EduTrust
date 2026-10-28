import React, { useState, useEffect } from 'react';
import { History, Search, Filter, Shield, Clock, User, Terminal } from 'lucide-react';
import { auditService } from '../../services/auditService';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Input } from '../../components/ui/Input';
import { DataTable } from '../../components/tables/DataTable';

export const AuditLogs = () => {
  const [logs, setLogs] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [actionFilter, setActionFilter] = useState('');
  const [userFilter, setUserFilter] = useState('');

  useEffect(() => {
    loadLogs();
  }, [page, actionFilter, userFilter]);

  const loadLogs = async () => {
    setIsLoading(true);
    try {
      const res = await auditService.getAuditLogs({
        page,
        size: 15,
        action: actionFilter || undefined,
        username: userFilter || undefined
      });
      setLogs(res.content || []);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements || 0);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  const columns = [
    {
      header: 'Timestamp',
      accessor: (row) => (
        <div>
          <p className="font-semibold text-slate-800">{new Date(row.timestamp).toLocaleTimeString()}</p>
          <p className="text-[10px] text-slate-400">{new Date(row.timestamp).toLocaleDateString()}</p>
        </div>
      )
    },
    {
      header: 'Action / Event',
      accessor: (row) => (
        <div>
          <span className="font-mono font-bold text-slate-900 bg-slate-100 px-2 py-0.5 rounded text-[11px]">
            {row.action}
          </span>
          <p className="text-[10px] text-slate-400 mt-0.5">Entity: {row.entityType} ({row.entityId || 'N/A'})</p>
        </div>
      )
    },
    {
      header: 'User & Origin',
      accessor: (row) => (
        <div>
          <p className="font-semibold text-slate-900">{row.username}</p>
          <p className="text-[10px] text-slate-400 font-mono">IP: {row.ipAddress || '127.0.0.1'}</p>
        </div>
      )
    },
    {
      header: 'Outcome',
      accessor: (row) => (
        <Badge variant={row.outcome === 'SUCCESS' ? 'success' : 'danger'}>
          {row.outcome}
        </Badge>
      )
    },
    {
      header: 'Reason / Details',
      accessor: (row) => (
        <p className="text-slate-600 max-w-sm truncate" title={row.reason}>
          {row.reason}
        </p>
      )
    },
    {
      header: 'Correlation ID',
      accessor: (row) => (
        <span className="font-mono text-[11px] text-slate-500 bg-slate-50 border border-slate-200 px-1.5 py-0.5 rounded">
          {row.correlationId || 'N/A'}
        </span>
      )
    }
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold text-slate-900">System Audit Trail</h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Tamper-evident chronological log of all security events, data imports, reviews, and credential operations
        </p>
      </div>

      {/* Filter Row */}
      <Card className="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          <Input
            placeholder="Filter by Username..."
            icon={User}
            value={userFilter}
            onChange={(e) => {
              setUserFilter(e.target.value);
              setPage(0);
            }}
          />

          <select
            value={actionFilter}
            onChange={(e) => {
              setActionFilter(e.target.value);
              setPage(0);
            }}
            className="w-full h-9 rounded-lg border border-slate-200 bg-white px-3 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-100"
          >
            <option value="">All Actions</option>
            <option value="USER_LOGIN">USER_LOGIN</option>
            <option value="STUDENT_CREATED">STUDENT_CREATED</option>
            <option value="STUDENT_UPDATED">STUDENT_UPDATED</option>
            <option value="IMPORT_PREVIEWED">IMPORT_PREVIEWED</option>
            <option value="IMPORT_COMMITTED">IMPORT_COMMITTED</option>
            <option value="REVIEW_CASE_RESOLVED">REVIEW_CASE_RESOLVED</option>
            <option value="CREDENTIAL_ISSUED">CREDENTIAL_ISSUED</option>
            <option value="CREDENTIAL_VERIFIED_SUCCESS">CREDENTIAL_VERIFIED_SUCCESS</option>
            <option value="CREDENTIAL_VERIFY_TAMPER_DETECTED">CREDENTIAL_VERIFY_TAMPER_DETECTED</option>
          </select>
        </div>
      </Card>

      {/* Audit Table */}
      <Card>
        <DataTable
          columns={columns}
          data={logs}
          isLoading={isLoading}
          page={page}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={(p) => setPage(p)}
          emptyTitle="No Audit Events Recorded"
          emptyDescription="No events match your current filter criteria."
        />
      </Card>
    </div>
  );
};

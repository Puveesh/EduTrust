import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { LoadingSpinner } from '../feedback/LoadingSpinner';
import { EmptyState } from '../feedback/EmptyState';

export const DataTable = ({
  columns,
  data = [],
  isLoading = false,
  page = 0,
  totalPages = 1,
  totalElements = 0,
  onPageChange,
  emptyTitle = 'No data available',
  emptyDescription = 'No records match the current view.'
}) => {
  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (!data || data.length === 0) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />;
  }

  return (
    <div className="w-full">
      <div className="overflow-x-auto overflow-y-auto max-h-[560px] rounded-lg border border-slate-200">
        <table className="min-w-full divide-y divide-slate-200 text-left text-xs">
          <thead className="bg-slate-50 font-semibold text-slate-700 sticky top-0 z-10 shadow-sm">
            <tr>
              {columns.map((col, index) => (
                <th
                  key={index}
                  scope="col"
                  className={`px-4 py-3.5 ${col.className || ''}`}
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 bg-white text-slate-600">
            {data.map((row, rowIndex) => (
              <tr
                key={row.id || rowIndex}
                className="hover:bg-slate-50/80 transition-colors"
              >
                {columns.map((col, colIndex) => (
                  <td
                    key={colIndex}
                    className={`px-4 py-3 whitespace-nowrap ${col.cellClassName || ''}`}
                  >
                    {col.accessor ? (typeof col.accessor === 'function' ? col.accessor(row) : row[col.accessor]) : null}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between px-2 py-3 border-t border-slate-100 mt-2 text-xs text-slate-500">
          <div>
            Showing page <span className="font-semibold text-slate-700">{page + 1}</span> of{' '}
            <span className="font-semibold text-slate-700">{totalPages}</span> ({totalElements} total records)
          </div>
          <div className="flex items-center space-x-1.5">
            <button
              onClick={() => onPageChange(page - 1)}
              disabled={page === 0}
              className="p-1 rounded-md border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={() => onPageChange(page + 1)}
              disabled={page >= totalPages - 1}
              className="p-1 rounded-md border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

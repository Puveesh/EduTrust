import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from '../components/layout/Layout';
import { ProtectedRoute } from './ProtectedRoute';

// Pages
import { Login } from '../pages/Login/Login';
import { Register } from '../pages/Register/Register';
import { Dashboard } from '../pages/Dashboard/Dashboard';
import { Students } from '../pages/Students/Students';
import { Imports } from '../pages/Imports/Imports';
import { Reviews } from '../pages/Reviews/Reviews';
import { Institutions } from '../pages/Institutions/Institutions';
import { Compliance } from '../pages/Compliance/Compliance';
import { Verification } from '../pages/Verification/Verification';
import { AuditLogs } from '../pages/AuditLogs/AuditLogs';
import { Settings } from '../pages/Settings/Settings';

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Route */}
      <Route path="/login" element={<Login />} />

      {/* ======================================================================== */}
      {/* TEMPORARY USER REGISTRATION (Comment out this route to make non-usable) */}
      <Route path="/register" element={<Register />} />
      {/* ======================================================================== */}

      {/* Protected Routes inside Main Dashboard Layout */}
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/verification" element={<Verification />} />
          <Route path="/settings" element={<Settings />} />

          {/* AICTE Admin and College Registrar Routes */}
          <Route element={<ProtectedRoute allowedRoles={['AICTE_ADMIN', 'COLLEGE_REGISTRAR']} />}>
            <Route path="/students" element={<Students />} />
            <Route path="/imports" element={<Imports />} />
            <Route path="/reviews" element={<Reviews />} />
            <Route path="/institutions" element={<Institutions />} />
            <Route path="/compliance" element={<Compliance />} />
            <Route path="/audit-logs" element={<AuditLogs />} />
          </Route>
        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

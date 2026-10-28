import api from './api';

export const complianceService = {
  async getIntakeCompliance(params = {}) {
    const response = await api.get('/compliance/intake', { params });
    return response.data;
  }
};

export const institutionService = {
  async getAllInstitutions() {
    const response = await api.get('/institutions');
    return response.data;
  },

  async getInstitutionById(id) {
    const response = await api.get(`/institutions/${id}`);
    return response.data;
  },

  async createInstitution(data) {
    const response = await api.post('/institutions', data);
    return response.data;
  },

  async updateInstitution(id, data) {
    const response = await api.put(`/institutions/${id}`, data);
    return response.data;
  }
};

export const credentialService = {
  async issueCredential(data) {
    const response = await api.post('/credentials/issue', data);
    return response.data;
  },

  async verifyCredential(data) {
    const response = await api.post('/credentials/verify', data);
    return response.data;
  },

  async getCredentials(params = {}) {
    const response = await api.get('/credentials', { params });
    return response.data;
  }
};

export const auditService = {
  async getAuditLogs(params = {}) {
    const response = await api.get('/audit/logs', { params });
    return response.data;
  }
};

export const dashboardService = {
  async getSummary(params = {}) {
    const response = await api.get('/dashboard/summary', { params });
    return response.data;
  }
};

import api from './api';

export const complianceService = {
  async getIntakeCompliance(params = {}) {
    const response = await api.get('/compliance/intake', { params });
    return response.data;
  }
};

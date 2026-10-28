import api from './api';

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

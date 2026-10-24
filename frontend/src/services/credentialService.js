import api from './api';

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

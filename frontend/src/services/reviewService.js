import api from './api';

export const reviewService = {
  async getCases(params = {}) {
    const response = await api.get('/reviews', { params });
    return response.data;
  },

  async getCaseById(id) {
    const response = await api.get(`/reviews/${id}`);
    return response.data;
  },

  async resolveCase(id, resolutionData) {
    const response = await api.post(`/reviews/${id}/resolve`, resolutionData);
    return response.data;
  },

  async dismissCase(id, dismissalData) {
    const response = await api.post(`/reviews/${id}/dismiss`, dismissalData);
    return response.data;
  }
};

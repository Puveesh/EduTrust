import api from './api';

export const authService = {
  async login(username, password) {
    const response = await api.post('/auth/login', { username, password });
    if (response.data && response.data.token) {
      localStorage.setItem('edutrust_token', response.data.token);
      localStorage.setItem('edutrust_user', JSON.stringify(response.data));
    }
    return response.data;
  },

  async register(userData) {
    const response = await api.post('/auth/register', userData);
    if (response.data && response.data.token) {
      localStorage.setItem('edutrust_token', response.data.token);
      localStorage.setItem('edutrust_user', JSON.stringify(response.data));
    }
    return response.data;
  },

  async getRegistrationInstitutions() {
    const response = await api.get('/auth/institutions');
    return response.data;
  },

  logout() {
    localStorage.removeItem('edutrust_token');
    localStorage.removeItem('edutrust_user');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('edutrust_user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return !!localStorage.getItem('edutrust_token');
  }
};

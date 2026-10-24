import api from './api';

export const importService = {
  async downloadTemplate() {
    const response = await api.get('/imports/template', { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'student_import_template.csv');
    document.body.appendChild(link);
    link.click();
    link.parentNode.removeChild(link);
  },

  async uploadAndPreview(file, institutionId) {
    const formData = new FormData();
    formData.append('file', file);
    if (institutionId) {
      formData.append('institutionId', institutionId);
    }
    const response = await api.post('/imports/preview', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
  },

  async getBatches(params = {}) {
    const response = await api.get('/imports', { params });
    return response.data;
  },

  async getBatchById(batchId) {
    const response = await api.get(`/imports/${batchId}`);
    return response.data;
  },

  async getBatchRows(batchId, params = {}) {
    const response = await api.get(`/imports/${batchId}/rows`, { params });
    return response.data;
  },

  async updateBatchRow(batchId, rowId, rowData) {
    const response = await api.patch(`/imports/${batchId}/rows/${rowId}`, rowData);
    return response.data;
  },

  async commitBatch(batchId) {
    const response = await api.post(`/imports/${batchId}/commit`);
    return response.data;
  },

  async cancelBatch(batchId) {
    const response = await api.post(`/imports/${batchId}/cancel`);
    return response.data;
  }
};

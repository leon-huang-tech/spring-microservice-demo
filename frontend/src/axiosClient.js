import axios from 'axios';
import { API_BASE_URL } from './config';

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
});

// Request interceptor: attach token automatically
axiosClient.interceptors.request.use((config) => {
  const t = localStorage.getItem('token');
  if (t) {
    config.headers.Authorization = `Bearer ${t}`;
  }
  return config;
});

// Response interceptor: redirect to login on 401
axiosClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default axiosClient;

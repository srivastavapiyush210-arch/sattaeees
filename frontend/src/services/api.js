const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const getHeaders = (isJson = true) => {
  const headers = {};
  if (isJson) headers['Content-Type'] = 'application/json';
  const token = localStorage.getItem('token');
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
};

export const workerService = {
  getAllWorkers: async () => {
    const res = await fetch(`${API_URL}/workers`, { headers: getHeaders(false) });
    if (!res.ok) throw new Error('Failed to fetch workers');
    return res.json();
  },
  searchWorkers: async (skill, city) => {
    const query = new URLSearchParams();
    if (skill) query.append('skill', skill);
    if (city) {
        query.append('city', city);
        const res = await fetch(`${API_URL}/workers/search-by-city?${query.toString()}`, { headers: getHeaders(false) });
        if (!res.ok) throw new Error('Failed to search workers by city');
        return res.json();
    } else {
        const res = await fetch(`${API_URL}/workers/search?${query.toString()}`, { headers: getHeaders(false) });
        if (!res.ok) throw new Error('Failed to search workers');
        return res.json();
    }
  },
  createWorker: async (workerData) => {
    const res = await fetch(`${API_URL}/workers`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(workerData)
    });
    if (!res.ok) throw new Error('Failed to create worker');
    return res.json();
  },
  loginWorker: async (credentials) => {
    const res = await fetch(`${API_URL}/workers/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    if (!res.ok) throw new Error('Invalid credentials');
    return res.json();
  }
};

export const customerService = {
  createCustomer: async (customerData) => {
    const res = await fetch(`${API_URL}/customers`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(customerData)
    });
    if (!res.ok) throw new Error('Failed to create customer');
    return res.json();
  },
  loginCustomer: async (credentials) => {
    const res = await fetch(`${API_URL}/customers/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    if (!res.ok) throw new Error('Invalid credentials');
    return res.json();
  },
  getAllCustomers: async () => {
    const res = await fetch(`${API_URL}/customers`, { headers: getHeaders(false) });
    if (!res.ok) throw new Error('Failed to fetch customers');
    return res.json();
  }
};

export const jobRequestService = {
  createJobRequest: async (jobData) => {
    const res = await fetch(`${API_URL}/job-requests`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(jobData)
    });
    if (!res.ok) throw new Error('Failed to create job request');
    return res.json();
  },
  getAllJobRequests: async () => {
    const res = await fetch(`${API_URL}/job-requests`, { headers: getHeaders(false) });
    if (!res.ok) throw new Error('Failed to fetch jobs');
    return res.json();
  },
  getJobsForWorker: async (workerId) => {
    const res = await fetch(`${API_URL}/job-requests/worker/${workerId}`, { headers: getHeaders(false) });
    if (!res.ok) throw new Error('Failed to fetch worker jobs');
    return res.json();
  },
  updateJobStatus: async (jobId, status) => {
    const res = await fetch(`${API_URL}/job-requests/${jobId}/status?status=${status}`, {
      method: 'PUT',
      headers: getHeaders(false)
    });
    if (!res.ok) throw new Error('Failed to update job status');
    return res.json();
  }
};

export const reviewService = {
  createReview: async (customerId, workerId, rating, comment) => {
    const res = await fetch(`${API_URL}/reviews?customerId=${customerId}&workerId=${workerId}&rating=${rating}&comment=${encodeURIComponent(comment || '')}`, {
      method: 'POST',
      headers: getHeaders(false)
    });
    if (!res.ok) throw new Error('Failed to submit review');
    return res.json();
  }
};

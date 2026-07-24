// SSE streaming request wrapper for AI Chat
// behavior: attach token and handle 401

import { API_BASE_URL } from '../config';

/**
 * Initiate streaming chat request and read AI response chunk by chunk.
 *
 * @param {Object} params
 * @param {string} params.message
 * @param {string} params.sessionId - Session ID for server-side context memory
 * @param {(chunkText: string) => void} params.onChunk - Callback triggered upon receiving each text chunk
 * @returns {Promise<void>}
 * @throws 
 */
export async function streamChat({ message, sessionId, onChunk }) {
  const token = localStorage.getItem('token');
  const url = `${API_BASE_URL}/api/ai/chat/stream?message=${encodeURIComponent(
    message
  )}&sessionId=${sessionId}`;
 
  const response = await fetch(url, {
    headers: { Authorization: `Bearer ${token}` },
  });
 
  if (response.status === 401) {
    localStorage.removeItem('token');
    window.location.href = '/login';
    return;
  }
 
  if (!response.ok) {
    throw new Error(`Chat stream failed with status ${response.status}`);
  }
 
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
 
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
 
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() || ''; // Retain the last line, which may be incomplete
 
    for (const line of lines) {
      const trimmed = line.trim();
      if (trimmed.startsWith('data:')) {
        const chunkText = trimmed.startsWith('data: ')
          ? trimmed.slice(6) // FOR "data: " 
          : trimmed.slice(5); // FOR "data:" 
        if (chunkText) {
          onChunk(chunkText);
        }
      }
    }
  }
}
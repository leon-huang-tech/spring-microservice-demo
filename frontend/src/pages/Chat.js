import { useState, useRef, useEffect } from 'react';
import { streamChat } from '../api/chatStream';
import { layout, colors, text } from '../styles/common';

function Chat() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const sessionId = useRef('session_' + Date.now());
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userMessage = input;
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', content: userMessage }]);
    setMessages((prev) => [...prev, { role: 'ai', content: '' }]);
    setLoading(true);

    let aiMessage = '';

    try {
      await streamChat({
        message: userMessage,
        sessionId: sessionId.current,
        onChunk: (chunkText) => {
          aiMessage += chunkText;
          setMessages((prev) => {
            const updated = [...prev];
            updated[updated.length - 1] = { role: 'ai', content: aiMessage };
            return updated;
          });
        },
      });
    } catch (e) {
      setMessages((prev) => {
        const updated = [...prev];
        updated[updated.length - 1] = {
          role: 'ai',
          content: 'Error: Failed to get response.',
        };
        return updated;
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={layout.title}>AI Assistant</h2>

      <div style={styles.chatBox}>
        {messages.length === 0 && (
          <p style={text.placeholder}>Ask me about your orders or account...</p>
        )}
        {messages.map((msg, index) => (
          <div
            key={index}
            style={{
              ...styles.message,
              alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
              backgroundColor: msg.role === 'user' ? colors.primary : '#f0f0f0',
              color: msg.role === 'user' ? 'white' : 'black',
            }}
          >
            {msg.content}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      <div style={styles.inputArea}>
        <input
          style={styles.input}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && !loading && sendMessage()}
          placeholder="Type a message..."
          disabled={loading}
        />
        <button
          style={{ ...styles.sendButton, opacity: loading ? 0.6 : 1 }}
          onClick={sendMessage}
          disabled={loading}
        >
          {loading ? '...' : 'Send'}
        </button>
      </div>
    </div>
  );
}

// Page-specific chat bubble layout; common styles are imported from common.js
const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    // Subtract approximate Navbar and page padding height to make the chat box fill the remaining viewport
    height: 'calc(100vh - 140px)',
  },
  chatBox: {
    flex: 1,
    overflowY: 'auto',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    padding: '16px',
    backgroundColor: colors.bgSubtle,
    borderRadius: '8px',
    marginBottom: '16px',
  },
  message: {
    padding: '10px 14px',
    borderRadius: '12px',
    maxWidth: '70%',
    wordBreak: 'break-word',
  },
  inputArea: { display: 'flex', gap: '8px' },
  input: {
    flex: 1,
    padding: '10px',
    borderRadius: '4px',
    border: `1px solid ${colors.border}`,
    fontSize: '14px',
  },
  sendButton: {
    padding: '10px 20px',
    backgroundColor: colors.primary,
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
};

export default Chat;

import { createContext, useContext, useRef, useState } from 'react';

// Holds AI Chat state (messages + sessionId) above the route tree, so it
// survives navigating away from and back to the Chat page. If this lived
// inside Chat.js as local state, React Router would unmount/remount the
// component on every route change and wipe it out.
const ChatContext = createContext(null);

export function ChatProvider({ children }) {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  // Created once when the app loads (ChatProvider mounts once, above
  // <Routes>), not once per Chat page visit.
  const sessionIdRef = useRef('session_' + Date.now());

  const value = {
    messages, setMessages,
    loading, setLoading,
    sessionId: sessionIdRef.current,
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
}

export function useChatContext() {
  const ctx = useContext(ChatContext);
  if (!ctx) {
    throw new Error('useChatContext must be used within a ChatProvider');
  }
  return ctx;
}

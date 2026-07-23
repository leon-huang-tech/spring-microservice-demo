import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Users from './pages/Users';
import Orders from './pages/Orders';
import Chat from './pages/Chat';
import KnowledgeBase from './pages/KnowledgeBase';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        {/*
          * All routes below require authentication (ProtectedRoute) and share the main navbar shell (Layout).
          * To add a new page, simply insert a new <Route path="..." element={<NewPage />} /> line here.
          */}
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route path="/users" element={<Users />} />
            <Route path="/orders" element={<Orders />} />
            <Route path="/chat" element={<Chat />} />
            <Route path="/knowledge" element={<KnowledgeBase />} />
          </Route>
        </Route>

        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

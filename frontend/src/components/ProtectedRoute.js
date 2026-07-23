import { Navigate, Outlet } from 'react-router-dom';

/**
 * Protected route: redirect to /login if no token present.
 */
function ProtectedRoute() {
  const token = localStorage.getItem('token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}

export default ProtectedRoute;

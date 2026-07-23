import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import { layout } from '../styles/common';

/**
 * Mounted on the App.js route tree; all pages requiring a navbar share this wrapper.
 * Adding a new page does not require importing Navbar or wrapping <div style={layout.page}> manually;
 * simply add the new route under this Layout in App.js.
 */
function Layout() {
  return (
    <div style={layout.page}>
      <Navbar />
      <Outlet />
    </div>
  );
}

export default Layout;

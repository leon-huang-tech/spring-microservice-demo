import { pagination } from '../styles/common';

/**
 * Generic Pagination Component（server-side pagination）。
 * @param {number} currentPage - Current page (0-based)
 * @param {number} totalPages
 * @param {(page: number) => void} onPageChange - Page change callback
 */
function Pagination({ currentPage, totalPages, onPageChange }) {
  return (
    <div style={pagination.wrapper}>
      <button
        style={pagination.pageBtn}
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        Previous
      </button>
      <span style={{ margin: '0 16px' }}>
        Page {currentPage + 1} of {Math.max(totalPages, 1)}
      </span>
      <button
        style={pagination.pageBtn}
        disabled={currentPage >= totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Next
      </button>
    </div>
  );
}

export default Pagination;

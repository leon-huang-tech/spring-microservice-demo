import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import axiosClient from '../axiosClient';
import Pagination from '../components/Pagination';
import { layout, table, text } from '../styles/common';

const PAGE_SIZE = 10;

const fetchUsers = async (page) => {
  const res = await axiosClient.get(
    `/api/users/paged?page=${page}&size=${PAGE_SIZE}`
  );
  return res.data.data; // { content, totalPages, ... }
};

function Users() {
  const [currentPage, setCurrentPage] = useState(0);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['users', currentPage],
    queryFn: () => fetchUsers(currentPage),
    keepPreviousData: true,
    staleTime: 30_000,
  });

  const users = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <h2 style={layout.title}>User List</h2>

      {isError && <p style={text.error}>Failed to fetch users. Please try again.</p>}

      {isLoading ? (
        <p>Loading...</p>
      ) : (
        <>
          <table style={table.table}>
            <thead>
              <tr>
                <th style={table.th}>ID</th>
                <th style={table.th}>Name</th>
                <th style={table.th}>Email</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td style={table.td}>{user.id}</td>
                  <td style={table.td}>{user.name}</td>
                  <td style={table.td}>{user.email}</td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td style={table.td} colSpan={3}>
                    No users found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        </>
      )}
    </>
  );
}

export default Users;

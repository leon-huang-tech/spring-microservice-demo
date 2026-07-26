import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useReactTable, getCoreRowModel, flexRender } from '@tanstack/react-table';
import axiosClient from '../axiosClient';
import Pagination from '../components/Pagination';
import { layout, form, table, button, text, statusColor } from '../styles/common';

const STATUS_OPTIONS = ['PENDING', 'PROCESSING', 'COMPLETED'];
const PAGE_SIZE = 10;

const fetchUsersForDropdown = async () => {
  const res = await axiosClient.get('/api/users');
  return res.data; 
};

// ---------- API Request Functions (Decoupled from UI) ----------
const fetchOrders = async (page) => {
  const res = await axiosClient.get(
    `/api/orders/paged?page=${page}&size=${PAGE_SIZE}`
  );
  return res.data.data; // { content, totalPages, ... }
};

const createOrder = (payload) => axiosClient.post('/api/orders', payload);
const updateOrder = ({ id, payload }) =>
  axiosClient.put(`/api/orders/${id}`, payload);
const deleteOrderReq = (id) => axiosClient.delete(`/api/orders/${id}`);

function Orders() {
  const queryClient = useQueryClient();

  const [currentPage, setCurrentPage] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [editOrder, setEditOrder] = useState(null);
  const [orderForm, setOrderForm] = useState({
    userId: 1,
    product: '',
    amount: '',
    status: 'PENDING',
  });
  const [error, setError] = useState('');

  // ---------- Data Fetching & Caching (TanStack Query) ----------
  const { data, isLoading, isError } = useQuery({
    queryKey: ['orders', currentPage],
    queryFn: () => fetchOrders(currentPage),
    keepPreviousData: true, // Keep previous data on pagination to prevent UI flickering
    staleTime: 30_000, // Cache data and prevent duplicate requests within 30 seconds
  });

  const { data: userOptions = [] } = useQuery({
    queryKey: ['users-dropdown'],
    queryFn: fetchUsersForDropdown,
    staleTime: 5 * 60_000,
  });

  const orders = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  // ---------- Mutation ----------
  const invalidateOrders = () =>
    queryClient.invalidateQueries({ queryKey: ['orders'] });

  const createMutation = useMutation({
    mutationFn: createOrder,
    onSuccess: () => {
      resetForm();
      invalidateOrders();
    },
    onError: (err) => setError('Failed to save order: ' + err.message),
  });

  const updateMutation = useMutation({
    mutationFn: updateOrder,
    onSuccess: () => {
      resetForm();
      invalidateOrders();
    },
    onError: (err) => setError('Failed to save order: ' + err.message),
  });

  const deleteMutation = useMutation({
    mutationFn: deleteOrderReq,
    onSuccess: () => invalidateOrders(),
    onError: (err) => setError('Failed to delete: ' + err.message),
  });

  const resetForm = () => {
    setOrderForm({ userId: 1, product: '', amount: '', status: 'PENDING' });
    setEditOrder(null);
    setShowForm(false);
  };

  const handleSubmit = () => {
    if (!orderForm.product || !orderForm.amount) {
      setError('Product and amount are required.');
      return;
    }
    const payload = {
      userId: Number(orderForm.userId),
      product: orderForm.product,
      amount: Number(orderForm.amount),
      status: orderForm.status,
    };

    if (editOrder) {
      updateMutation.mutate({ id: editOrder.id, payload });
    } else {
      createMutation.mutate(payload);
    }
  };

  const handleEdit = (order) => {
    setEditOrder(order);
    setOrderForm({
      userId: order.userId,
      product: order.product,
      amount: order.amount,
      status: order.status,
    });
    setShowForm(true);
  };

  const handleDelete = (id) => {
    if (!window.confirm('Delete this order?')) return;
    deleteMutation.mutate(id);
  };

  // ---------- Table Column Definitions (TanStack Table) ----------
  const columns = useMemo(
    () => [
      { header: 'ID', accessorKey: 'id' },
      { header: 'User ID', accessorKey: 'userId' },
      { header: 'Product', accessorKey: 'product' },
      {
        header: 'Amount',
        accessorKey: 'amount',
        cell: (info) => `$${info.getValue()}`,
      },
      {
        header: 'Status',
        accessorKey: 'status',
        cell: (info) => (
          <span
            style={{ ...table.badge, backgroundColor: statusColor(info.getValue()) }}
          >
            {info.getValue()}
          </span>
        ),
      },
      {
        header: 'Actions',
        id: 'actions',
        cell: ({ row }) => (
          <>
            <button
              style={{ ...button.base, ...button.primary, ...button.small }}
              onClick={() => handleEdit(row.original)}
            >
              Edit
            </button>
            <button
              style={{ ...button.base, ...button.danger, ...button.small }}
              onClick={() => handleDelete(row.original.id)}
            >
              Delete
            </button>
          </>
        ),
      },
    ],
    []
  );

  const reactTable = useReactTable({
    data: orders,
    columns,
    getCoreRowModel: getCoreRowModel(),
  });

  return (
    <>
      <div style={layout.header}>
        <h2 style={layout.title}>Order List</h2>
        <button
          style={{ ...button.base, ...button.success }}
          onClick={() => {
            resetForm();
            setShowForm(true);
          }}
        >
          + New Order
        </button>
      </div>

      {(error || isError) && (
        <p style={text.error}>{error || 'Failed to fetch orders.'}</p>
      )}

      {showForm && (
        <div style={form.card}>
          <h3>{editOrder ? 'Edit Order' : 'New Order'}</h3>

          <label style={form.label}>User</label>
          <select
            style={form.input}
            value={orderForm.userId}
            onChange={(e) => setOrderForm({ ...orderForm, userId: e.target.value })}
          >
            {userOptions.map((u) => (
              <option key={u.id} value={u.id}>
                {u.name}
              </option>
            ))}
          </select>

          <label style={form.label}>Product</label>
          <input
            style={form.input}
            value={orderForm.product}
            onChange={(e) => setOrderForm({ ...orderForm, product: e.target.value })}
            placeholder="Product name"
          />

          <label style={form.label}>Amount ($)</label>
          <input
            style={form.input}
            type="number"
            value={orderForm.amount}
            onChange={(e) => setOrderForm({ ...orderForm, amount: e.target.value })}
            placeholder="0.00"
          />

          <label style={form.label}>Status</label>
          <select
            style={form.input}
            value={orderForm.status}
            onChange={(e) => setOrderForm({ ...orderForm, status: e.target.value })}
          >
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>

          <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
            <button
              style={{ ...button.base, ...button.success }}
              onClick={handleSubmit}
              disabled={createMutation.isPending || updateMutation.isPending}
            >
              {editOrder ? 'Update' : 'Create'}
            </button>
            <button
              style={{ ...button.base, backgroundColor: '#999' }}
              onClick={resetForm}
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {isLoading ? (
        <p>Loading...</p>
      ) : (
        <>
          <table style={table.table}>
            <thead>
              {reactTable.getHeaderGroups().map((headerGroup) => (
                <tr key={headerGroup.id}>
                  {headerGroup.headers.map((header) => (
                    <th style={table.th} key={header.id}>
                      {flexRender(header.column.columnDef.header, header.getContext())}
                    </th>
                  ))}
                </tr>
              ))}
            </thead>
            <tbody>
              {reactTable.getRowModel().rows.map((row) => (
                <tr key={row.id}>
                  {row.getVisibleCells().map((cell) => (
                    <td style={table.td} key={cell.id}>
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </td>
                  ))}
                </tr>
              ))}
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

export default Orders;

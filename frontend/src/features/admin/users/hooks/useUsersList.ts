import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  getListQueryKey,
  useList,
} from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import type { ListParams } from '../../../../api/model/listParams';
import type { UserAdminSummaryResponse } from '../../../../api/model/userAdminSummaryResponse';
import { BACKEND_ROLES, getRoleLabel, isBackendRoleCode } from '../../../../lib/auth/roleLabels';

export interface UserRowViewModel {
  userId: string;
  email: string;
  roleLabel: string;
  status: string;
  canDeactivate: boolean;
}

const USER_STATUS_OPTIONS = [
  { value: '', label: 'Todos los estados' },
  { value: 'INACTIVE', label: 'Inactivo' },
  { value: 'ACTIVE', label: 'Activo' },
  { value: 'DEACTIVATED', label: 'Revocado' },
] as const;

function buildListParams(roleFilter: string, statusFilter: string): ListParams | undefined {
  const params: ListParams = {};
  if (roleFilter) {
    params.role = roleFilter;
  }
  if (statusFilter) {
    params.status = statusFilter;
  }
  return Object.keys(params).length > 0 ? params : undefined;
}

function toRow(user: UserAdminSummaryResponse): UserRowViewModel | null {
  if (!user.userId || !user.email || !user.role || !user.status) {
    return null;
  }

  return {
    userId: user.userId,
    email: user.email,
    roleLabel: isBackendRoleCode(user.role) ? getRoleLabel(user.role) : user.role,
    status: user.status,
    canDeactivate: user.status !== 'DEACTIVATED',
  };
}

export function useUsersList() {
  const queryClient = useQueryClient();
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const listParams = buildListParams(roleFilter, statusFilter);
  const query = useList(listParams);

  const users = (query.data?.data ?? [])
    .map(toRow)
    .filter((row: UserRowViewModel | null): row is UserRowViewModel => row !== null);

  const roleOptions = [
    { value: '', label: 'Todos los roles' },
    ...BACKEND_ROLES.map((role) => ({ value: role, label: getRoleLabel(role) })),
  ];

  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: getListQueryKey() });
  };

  return {
    users,
    isLoading: query.isLoading,
    isError: query.isError,
    error: query.error,
    roleFilter,
    statusFilter,
    roleOptions,
    statusOptions: USER_STATUS_OPTIONS,
    setRoleFilter,
    setStatusFilter,
    refresh,
  };
}

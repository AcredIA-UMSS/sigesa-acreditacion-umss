import { useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useRegister } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import { getListQueryKey } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import { useList1 } from '../../../../api/endpoints/program-catalog-controller/program-catalog-controller';
import type { RegisterUserResponse, ProgramSummaryResponse } from '../../../../api/model';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';
import {
  ASSIGNABLE_ROLES,
  ROLE_LABELS,
  ROLE_REQUIRES_PROGRAM,
  type BackendRoleCode,
} from '../../../../lib/auth/roleLabels';
import type { AddUserFormViewModel } from '../components/AddUserModalUI';
import {
  normalizePhoneDigits,
  validateUserForm,
  type UserFormErrors,
} from '../lib/userFormValidation';

const EMPTY_FORM: AddUserFormViewModel = {
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  role: '',
  programId: '',
  password: '',
  confirmPassword: '',
};

export interface SaveSuccessState {
  fullName: string;
  email: string;
  password: string;
}

export function useRegisterUserForm() {
  const queryClient = useQueryClient();
  const programsQuery = useList1();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form, setForm] = useState<AddUserFormViewModel>(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState<UserFormErrors>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState<SaveSuccessState | null>(null);
  const [pendingPassword, setPendingPassword] = useState('');

  const { mutate, isPending, reset } = useRegister({
    mutation: {
      onSuccess: async (response) => {
        const payload = response.data as RegisterUserResponse;
        const fullName = `${form.firstName.trim()} ${form.lastName.trim()}`.trim();
        setSaveSuccess({
          fullName,
          email: form.email.trim().toLowerCase(),
          password: pendingPassword,
        });
        setSubmitError(null);
        setIsModalOpen(false);
        setForm(EMPTY_FORM);
        setFieldErrors({});
        setPendingPassword('');
        await queryClient.invalidateQueries({ queryKey: getListQueryKey(undefined) });
        void payload;
      },
      onError: (error) => {
        setSubmitError(getApiErrorMessage(error));
      },
    },
  });

  const requiresProgram = form.role !== '' ? ROLE_REQUIRES_PROGRAM[form.role] : false;
  const programOptions = (programsQuery.data?.data ?? [])
    .filter((program: ProgramSummaryResponse) => program.id && program.name)
    .map((program: ProgramSummaryResponse) => ({
      value: program.id as string,
      label: program.code ? `${program.code} — ${program.name}` : (program.name as string),
    }));

  const openModal = () => {
    setForm(EMPTY_FORM);
    setFieldErrors({});
    setSubmitError(null);
    setIsModalOpen(true);
    reset();
  };

  const closeModal = () => {
    if (isPending) return;
    setIsModalOpen(false);
    setSubmitError(null);
    setFieldErrors({});
  };

  const closeSuccessDialog = () => {
    setSaveSuccess(null);
  };

  const handleSubmit = () => {
    setSubmitError(null);

    const errors = validateUserForm({
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      phoneNumber: form.phoneNumber,
      role: form.role,
      programId: form.programId,
      password: form.password,
      confirmPassword: form.confirmPassword,
      requiresProgram,
    });

    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    setPendingPassword(form.password);

    if (!form.role) {
      return;
    }

    mutate({
      data: {
        email: form.email.trim().toLowerCase(),
        role: form.role,
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        phoneNumber: normalizePhoneDigits(form.phoneNumber),
        password: form.password,
        ...(requiresProgram ? { programId: form.programId } : {}),
      },
    });
  };

  const setFirstName = (firstName: string) => setForm((c) => ({ ...c, firstName }));
  const setLastName = (lastName: string) => setForm((c) => ({ ...c, lastName }));
  const setEmail = (email: string) => setForm((c) => ({ ...c, email }));
  const setPhoneNumber = (phoneNumber: string) =>
    setForm((c) => ({ ...c, phoneNumber: normalizePhoneDigits(phoneNumber) }));
  const setRole = (role: BackendRoleCode | '') =>
    setForm((c) => ({ ...c, role, programId: '' }));
  const setProgramId = (programId: string) => setForm((c) => ({ ...c, programId }));
  const setPassword = (password: string) => setForm((c) => ({ ...c, password }));
  const setConfirmPassword = (confirmPassword: string) => setForm((c) => ({ ...c, confirmPassword }));

  const roleOptions = ASSIGNABLE_ROLES.map((role: BackendRoleCode) => ({
    value: role,
    label: ROLE_LABELS[role],
  }));

  return {
    isModalOpen,
    form,
    fieldErrors,
    submitError,
    saveSuccess,
    isPending,
    requiresProgram,
    roleOptions,
    programOptions,
    isProgramsLoading: programsQuery.isLoading,
    openModal,
    closeModal,
    closeSuccessDialog,
    handleSubmit,
    setFirstName,
    setLastName,
    setEmail,
    setPhoneNumber,
    setRole,
    setProgramId,
    setPassword,
    setConfirmPassword,
  };
}

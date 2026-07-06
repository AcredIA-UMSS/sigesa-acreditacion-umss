// frontend/src/features/procesos/CreateProcessPage.tsx
import { Sidebar } from '../../components/layout/Sidebar';
import { CreateProcessUI } from './components/CreateProcessUI';
// import { useCreateProcessMutation } from '../../api/endpoints/procesos'; // <--- Hook de Orval

export const CreateProcessPage = () => {
  // Lógica de React Query (Orval) iría aquí:
  // const { mutate: createProcess, isPending } = useCreateProcessMutation();

  // Handlers para el formulario...

  return (
    <div className="flex h-screen bg-body">
      <Sidebar />
      <CreateProcessUI />
    </div>
  );
};
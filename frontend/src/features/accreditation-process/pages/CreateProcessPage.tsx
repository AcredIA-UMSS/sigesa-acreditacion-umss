import { Link } from 'react-router-dom';
import { Sidebar } from '../../../components/layout/Sidebar';
import { CreateProcessView } from '../components/CreateProcessView';

export function CreateProcessPage() {
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="processes" />
      <main className="flex-1 overflow-y-auto px-8 py-8">
        <nav className="mb-6 text-body-md text-gray-500">
          <Link to="/procesos" className="text-primary-600 hover:text-primary-800">
            Procesos
          </Link>
          <span className="mx-2">/</span>
          <span>Nuevo proceso</span>
        </nav>
        <CreateProcessView />
      </main>
    </div>
  );
}

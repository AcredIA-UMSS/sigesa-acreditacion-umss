import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { CreateProcessPage } from './features/procesos/CreateProcessPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Redirección por defecto */}
        <Route path="/" element={<Navigate to="/procesos/nuevo" replace />} />
        
        {/* Tu nueva ruta */}
        <Route path="/procesos/nuevo" element={<CreateProcessPage />} />
        
        {/* Aquí irán creciendo tus demás rutas (ej. /reportes, /historial) */}
      </Routes>
    </BrowserRouter>
  );
}

export default App;

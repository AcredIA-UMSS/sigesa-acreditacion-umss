import { CheckCircle2, Copy, X } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../../components/ui/Button';

interface UserSaveSuccessDialogProps {
  isOpen: boolean;
  fullName: string;
  email: string;
  password: string;
  onClose: () => void;
}

export function UserSaveSuccessDialog({
  isOpen,
  fullName,
  email,
  password,
  onClose,
}: UserSaveSuccessDialogProps) {
  const [copied, setCopied] = useState(false);

  if (!isOpen) {
    return null;
  }

  const handleCopy = async () => {
    const text = `Usuario: ${email}\nContraseña: ${password}`;
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-primary-900/40" aria-hidden="true" />
      <div className="relative z-10 w-full max-w-md rounded-2xl border border-success/20 bg-body p-8 shadow-2xl">
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 rounded-lg p-1 text-gray-500 hover:bg-gray-100"
          aria-label="Cerrar confirmación"
        >
          <X size={20} />
        </button>

        <div className="flex flex-col items-center text-center">
          <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-success/10 text-success">
            <CheckCircle2 size={32} />
          </div>
          <h3 className="text-heading-md font-bold text-primary-800">Usuario guardado correctamente</h3>
          <p className="mt-2 text-body-md text-gray-600">
            {fullName} fue registrado con estado inicial <strong>INACTIVE</strong>.
          </p>
        </div>

        <div className="mt-6 space-y-3 rounded-xl border border-gray-200 bg-gray-50 p-4 text-left">
          <p className="text-label-md font-semibold uppercase tracking-wide text-gray-600">
            Credenciales para compartir
          </p>
          <div>
            <span className="text-label-md text-gray-500">Correo</span>
            <p className="text-body-md font-medium text-gray-900">{email}</p>
          </div>
          <div>
            <span className="text-label-md text-gray-500">Contraseña</span>
            <p className="font-mono text-body-md font-semibold text-primary-800">{password}</p>
          </div>
          <p className="text-label-md text-gray-500">
            La contraseña no se puede recuperar después. Guárdela ahora para entregarla al usuario.
          </p>
          <Button type="button" variant="secondary" className="w-full" onClick={handleCopy}>
            <Copy size={16} />
            {copied ? 'Copiado' : 'Copiar credenciales'}
          </Button>
        </div>

        <Button type="button" className="mt-6 w-full" onClick={onClose}>
          Aceptar
        </Button>
      </div>
    </div>
  );
}

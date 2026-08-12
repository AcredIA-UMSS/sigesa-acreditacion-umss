import { GraduationCap } from 'lucide-react';
import { Navigate, useLocation } from 'react-router-dom';
import { LoginFormUI } from '../components/LoginFormUI';
import { useLoginForm } from '../hooks/useLoginForm';
import { getPostLoginPath } from '../../../lib/auth/getPostLoginPath';
import { useAuth } from '../../../lib/auth/useAuth';

interface LoginLocationState {
  from?: string;
}

export function LoginPage() {
  const { isAuthenticated, session } = useAuth();
  const location = useLocation();
  const loginForm = useLoginForm();

  if (isAuthenticated && session) {
    const from = (location.state as LoginLocationState | null)?.from;
    const destination =
      from && from !== '/login' ? from : getPostLoginPath(session.role);
    return <Navigate to={destination} replace />;
  }

  return (
    <div className="flex min-h-screen">
      <section className="relative hidden w-[42%] overflow-hidden bg-primary-900 text-body lg:flex lg:flex-col lg:justify-between">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute -left-10 top-20 h-72 w-72 rounded-full border border-primary-300" />
          <div className="absolute bottom-10 right-10 h-96 w-96 rounded-full border border-primary-400" />
          <GraduationCap className="absolute bottom-24 right-24 h-48 w-48 text-primary-700" strokeWidth={1} />
        </div>

        <div className="relative z-10 p-10">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-body/10">
              <GraduationCap className="text-body" size={28} />
            </div>
            <span className="text-heading-md font-bold">SIGESA</span>
          </div>
        </div>

        <div className="relative z-10 px-10 pb-16">
          <h2 className="text-display-lg leading-tight">Accede a SIGESA</h2>
          <p className="mt-4 max-w-md text-body-lg text-primary-100">
            Gestiona procesos académicos y administrativos con la plataforma institucional de alto
            rendimiento.
          </p>
          <div className="mt-8 flex gap-2">
            <span className="h-1 w-8 rounded-full bg-secondary" />
            <span className="h-1 w-8 rounded-full bg-primary-600" />
            <span className="h-1 w-8 rounded-full bg-primary-600" />
          </div>
        </div>
      </section>

      <section className="flex-1">
        <LoginFormUI
          email={loginForm.form.email}
          password={loginForm.form.password}
          emailError={loginForm.fieldErrors.email}
          passwordError={loginForm.fieldErrors.password}
          submitError={loginForm.submitError}
          isSubmitting={loginForm.isPending}
          onEmailChange={loginForm.setEmail}
          onPasswordChange={loginForm.setPassword}
          onSubmit={loginForm.handleSubmit}
        />
      </section>
    </div>
  );
}

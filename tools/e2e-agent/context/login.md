# Sección: Login UC-001 (`/login`)

## Pantalla

- Ruta: `/login`
- Heading: «Bienvenido»
- Campo correo: label «Correo Institucional» (dominio `@umss.edu.bo`)
- Campo contraseña: textbox accesible como `getByRole('textbox', { name: 'Contraseña' })`
- Botón: «Iniciar sesión»
- Errores API: `role="alert"` (no assert sobre texto exacto)
- Tras login JD exitoso: redirección a `/admin/users`, heading «Gestión de usuarios»

## Casos típicos

- Camino feliz JD → `/admin/users`
- Credenciales inválidas → permanece en `/login` + alert
- Formulario vacío → validación, no navega
- Email no institucional → error de validación cliente
- Toggle mostrar/ocultar contraseña (borde)

## Referencia manual

Patrón a mano: `frontend/tests/tradicional/login.spec.ts`

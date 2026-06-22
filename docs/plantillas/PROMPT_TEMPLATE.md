---
id: PR-IMPL-NNN
feature_asociado: DD-UC-NNN
fecha: "<dd/mm/aaaa>"
autor: "<tu-nombre>"
---

# Prompt de Implementación `PR-IMPL-NNN`

## 1. Propósito y Objetivo
<Descripción concisa de qué código debe generar este prompt>

## 2. Rol y Persona
Actúa como un **Desarrollador Backend Senior / Frontend Senior** experto en SIGESA (Spring Boot, Java 21, Arquitectura Hexagonal / React). Escribe código limpio y listo para producción.

## 3. Límites y Restricciones (In-Scope / Out-of-Scope)
- **Obligatorio:** Respetar la regla de cero divergencia silenciosa. Usar DTOs en Controladores.
- **Prohibido:** Modificar código fuera del alcance. Romper la arquitectura hexagonal.

## 4. Contexto de Entrada
- **Design Doc:** Leer `docs/design/DD-UC-NNN.md`
- **Archivos a modificar:** `<lista de archivos src/...>`

## 5. Salida Esperada
Proporciona los archivos generados o modificados con sus respectivas explicaciones breves, listos para integrarse mediante un PR.
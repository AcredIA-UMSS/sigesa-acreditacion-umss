# Instrucciones de prueba — resumen de procesos

Estrategia Minimal, postura test-after, alcance express. Una prueba comprobable por requisito, con al menos un camino feliz por componente nuevo. La suite existente debe seguir en verde. Este cambio no añade umbral de cobertura.

## Runner

JUnit 5 y Mockito, a través de `spring-boot-starter-test` en `backend/pom.xml`. No hace falta un runner nuevo.

Antes de la primera prueba nueva, comprobar el runner con una clase que ya existe:

```bash
mvn -f backend/pom.xml -Dtest=ListProcessesServiceTest test
```

## Comando de este cambio

Solo estas clases. No usar `mvn test` a secas.

```bash
mvn -f backend/pom.xml -Dtest=GetProcessStatsServiceTest,ProcessQueryStatsJpaTest,ProcessStatsControllerWebMvcTest test
```

## Qué cubrir

| Clase | Componente | Casos |
| --- | --- | --- |
| `GetProcessStatsServiceTest` | Caso de uso | `JD` con conteos y `frozenCount`; `CC` solo sus carreras; alcance vacío en ceros y sin llamar al puerto; claves `ACTIVE`/`CLOSED` y los tres modos siempre presentes |
| `ProcessQueryStatsJpaTest` | Adaptador JPA (`@DataJpaTest`, como `UserProgramAssignmentRepositoryTest`) | Una fila no archivada en `AUDIT_READONLY` se lee; una `ARCHIVED` no |
| `ProcessStatsControllerWebMvcTest` | WebMvc con la cadena de `ProcessFreezeControllerWebMvcTest` | 200 con el JSON; 401 sin token, cuerpo `UNAUTHORIZED`; 403 con un rol distinto de `JD`, `TD`, `CC`, `EE` y `CAE_MEMBER`, cuerpo `ACCESS_DENIED` |

El caso de uso se prueba con el puerto mockeado. No hace falta levantar PostgreSQL. El `@DataJpaTest` usa el H2 del perfil de prueba, igual que el repositorio de asignaciones.

No hay pruebas de frontend.

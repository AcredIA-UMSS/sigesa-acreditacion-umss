Auditoría 12 factores — ACREDIA
Módulo 8 · Actividad de auditoría (parte 1 de 2) · Entrega: domingo 18 de octubre de 2026, 23:59. Este archivo es a la vez las instrucciones para el asistente de IA y la ficha que se entrega . El asistente lo rellena en su lugar; el grupo lo revisa, responde las preguntas y lo sube a su repositorio.

Cómo se usa (para el grupo)
Copie este archivo a su repositorio como docs/auditoria/12factor.md.
Creen la etiqueta o rama release/8.0.0sobre su rama principal actual (es el estado de partida del módulo) y trabajen sobre ese compromiso.
Abran su asistente con acceso al repositorio (Claude Code, Codex, Cursor u otro) y escríbanle:
Sigue las instrucciones de la sección 0 de docs/auditoria/12factor.mdy rellena ese archivo.

Respondan las preguntas que el asistente les haga al final de cada oleada. Pueden contestar en el chat o escribir la respuesta directamente en la sección 4.
Revise el resultado, corran ustedes la autoverificación (sección 6) y hagan los commits cada integrante con su propia cuenta .
Si su asistente no tiene acceso al repositorio (un chat sin herramientas): pídanle que les indiquen qué comandos correr, córranlos ustedes y péguenle la salida. El resto del proceso es igual.

Cómo se califica la parte de preguntas: cada pregunta debe terminar respondida o cerrada como NO VERIFICABLEcon su motivo. Si quedan más de 3 preguntas sin responder en este archivo, baja la nota de la actividad. No saber un dato no es un error; dejar sin respuesta lo que el grupo sí podría averiguar, sí.

0. Instrucciones para el asistente de IA (no editar esta sección)
rol. Eres el asistente de auditoría de un grupo de maestría que desarrolla un backend. Vas a revisar su repositorio contra seis prácticas 12-factor ya rellenar este mismo archivo . No produce otros archivos.

Reglas que no puedes romper.

Solo lectura del producto. No modificas código, configuración ni dependencias del repositorio. El único archivo que editas es este.
Nada de inventos. Cada estado distinto de N/Alleva evidencia comprobable: ruta/archivo:líneacon el fragmento relevante, o el comando exacto que ejecutaste y las primeras líneas de su salida. Si no puedes comprobar algo, no lo supones: lo preguntas o lo marcas NO VERIFICABLEcon su motivo.
Sin copias secretas. Si encuentras una contraseña, token o clave, reportas la ruta y la línea, nunca el valor . No abras ni muestres archivos .envni de claves en el chat.
Todas las comprobaciones aparecen , una vez cada una (F12-01 a F12-06), aunque el resultado sea negativo. No agregas ni quitas comprobaciones.
Escribes en español y en frases claras.
Estados permitidos. OK , PARCIAL, FALLA, N/A(no aplica, con razón) y NO VERIFICABLE(no se pudo comprobar, con razón). Mientras trabajas puedes usar PENDIENTE, pero al terminar no puede quedar ninguno .

Cómo preguntar sin bloquear. Cuando te falte un dato que no está en el código (por ejemplo, cómo se despliega en producción):

Marca la comprobación como PENDIENTE, agrega la pregunta a la sección 4 con un número (P1, P2…) y la comprobación a la que afecta, y continúa con la siguiente . Nunca te detengas a esperar.
Al terminar cada oleada , muestra al grupo todas las preguntas nuevas juntas, cada una con su número. Si tu herramienta permite preguntas con opciones, úsalas; si no, escríbelas en el chat.
Si el grupo responde, anota la respuesta en la sección 4 y actualiza la comprobación. Si no responde o no lo sabe, en la oleada 3 cierra la comprobación como NO VERIFICABLEy la pregunta como "sin respuesta".
Pregunta solo lo necesario: antes de preguntar, búscalo en el repositorio (README, docker-compose, Dockerfile, manifiestos, configuración, scripts).
Oleadas. Trabaja en este orden y actualiza la sección 2 al terminar cada una:

Oleada 0 · Reconocimiento. Rellena la sección 1 (commit, stack, carpetas de código, cómo se ejecuta) y escribe en la sección 2 un plan breve: qué carpetas revisarás y qué comandos usarás para cada comprobación. No preguntes nada todavía, salvo que no encuentres el código fuente.
Oleada 1 · Evidencia automática. Ejecuta los comandos de cada comprobación (adáptalos a las carpetas reales del proyecto) y anota la evidencia. No decidas estados aún.
Oleada 2 · Juicio. Con la evidencia, decida el estado de cada comprobación y escriba la observación. Lo que no se pueda decidir sin un dato externo, pregúntalo como se indica arriba. Al terminar, muestra las preguntas.
Oleada 3 · Cierre. Integra las respuestas recibidas; cierra lo que quede sin respuesta como NO VERIFICABLE; escribe los hallazgos priorizados (sección 5); marca la autoverificación (sección 6) y rellena el resumen (sección 7). Termina diciendo al grupo cuantas preguntas quedaron sin respuesta.
Si el proyecto tiene varios servicios (por ejemplo, un backend en NestJS y otro en Python), evalúa cada comprobación sobre todos y anota la evidencia de cada uno; el estado es el del servicio peor evaluado.

1. Datos del repositorio (oleada 0)
Grupo: AcredIA-UMSS (organización GitHub del remoto origin).
Repositorio: git@github.com:AcredIA-UMSS/sigesa-acreditacion-umss.git
Confirmación auditada ( release/8.0.0): no existe la etiqueta release/8.0.0( git tag -l 'release/8.0.0'vacía). Se auditó HEADde la rama auditoria/boris: ace8fd141f0d229b0cc33940ea52439f15cd866d( ace8fd1 Merge pull request #48 from AcredIA-UMSS/features/actualizaciones).
Fecha de la auditoría: 2026-10-15
Stack detectado: monorepo — backend Java 21 + Spring Boot 4.1.0 ( backend/pom.xml) + Spring Web MVC + JPA/Hibernate + Flyway (perfil prod) + PostgreSQL 16 (Compose) / H2 (perfil por defecto y test); interfaz React 19 + Vite + pnpm; asistente LLM vía Open WebUI / Ollama ( docker-compose.yml); Nodo MCP ( mcp/sigesa-evidence); nginx como frente estático. No hay Redis ni cola AMQP en el código de producto.
Carpetas de código fuente: backend/src/main/java/ , backend/src/main/resources/, frontend/src/,mcp/sigesa-evidence/src/
Cómo se ejecuta (verificado): documentado en README.md(JDK 21, Node 20+/22, docker composecon backend :8080y frontend :3000, o ./mvnw+ Vite :5173). En esta auditoría no se arrancó el stack; el juicio se basa en código y manifiestos.
Integrantes y qué revisó cada uno: Boris Anthony Angulo Urquieta (F12-01…F12-06, con asistencia de auditoría automática).
2. Plan y avance por oleadas
Oleada	Qué se hizo	Estado
0 · Reconocimiento	Identificado monorepo, commit ace8fd1, manifiestos Compose/Docker, YAML Spring, README. Plan: application*.yamlDockerfiles docker-compose.yml, almacenamiento, registro, JWT.	Hecha
1 · Evidencia	git grep/ lectura de YAML, Dockerfiles, adaptadores de blob, JWT, .gitignore, git ls-filesde .env.	Hecha
2 · Juicio	Estados F12-01…06; preguntas P1–P4.	Hecha
3 · Cierre	Preguntas sin respuesta del grupo; hallazgos; resumen.	Hecha
3. Comprobaciones
F12-01 · Configuración fuera del código (factor III)
Qué se busca: todo lo que cambia entre entornos (URL, puertos, credenciales, banderas) se lee del entorno al arrancar, y la aplicación valida al inicio que esté presente. Mismo código en dev, pruebas y producción; solo cambia la configuración.
Señales de falla: localhost , IPs o puertos escritos en el código; URL de servicios fijos; condiciones del tipo if (env === 'production')repartidas por el código.
Comandos sugeridos:
git grep -nE "localhost|127\.0\.0\.1|:5432|:3306|:6379|:27017|https?://" -- backend/src frontend/src
git grep -nE "process\.env|@Value|@ConfigurationProperties|SIGESA_|SPRING_DATASOURCE" -- backend/src frontend/src
Evidencia:
Perfiles Spring y variables: backend/src/main/resources/application.yaml:8-12(H2 embebido por defecto), :35-48( ${SIGESA_JWT_SECRET:…}, ${SIGESA_ASSISTANT_BASE_URL:http://localhost:3001/api}).
Dev: backend/src/main/resources/application-dev.yaml:3-5( SPRING_DATASOURCE_URLcon fallback jdbc:postgresql://db:5432/sigesay usuario/contraseña por defecto).
Prod: backend/src/main/resources/application-prod.yaml:6-8,21( DATABASE_URLcon opción de reserva localhost:5432; sigesa.jwt.secret: ${SIGESA_JWT_SECRET} sin opción predeterminada).
Valores predeterminados en Java: backend/src/main/java/com/umss/sigesa/config/AssistantProperties.java:10( baseUrl = "http://localhost:3000/api").
MCP: mcp/sigesa-evidence/src/index.js:10-11( SIGESA_API_URL ?? 'http://localhost:8080').
Interfaz: frontend/src/lib/api/customFetch.ts:53( VITE_API_URL); mensaje de error con host fijo frontend/src/lib/api/customFetch.ts:30,79.
Componer: docker-compose.yml:61-93inyecta env al backend; un solo código base, no hay ramas de “prod” con código distinto.
git grepde hikari|maximum-pool-sizeen YAML: sin coincidencias .
Estado: PARCIAL
Observación: Hay un único código base y la mayoría de URL/credenciales se leen del entorno (factor I + III a medias). Fallos: (1) el perfil por defecto arranca H2 en memoria sin exigir env; (2) JWT y datasource de dev tienen respaldos embebidos; (3) prod aún defaulta DATABASE_URLa localhost; (4) no hay fail-fast global de configuración (salvo que falla SIGESA_JWT_SECRETen perfil prod). Deuda aceptable en laboratorio; riesgo real si alguien se despliega sin SPRING_PROFILES_ACTIVE=prod. Ticket: eliminar defaults de secretos/URLs de servicios en YAML no-test; arranque que falla si faltan DATABASE_URL, SIGESA_JWT_SECRET, SIGESA_ASSISTANT_BASE_URLen prod.
Auditó: Asistente de auditoría (Cursor)
F12-02 · Secretos fuera del repositorio y de su historial (factor III)
Qué se busca: ninguna contraseña, token o clave en el código, en archivos versionados ni en el historial de git. El .envreal está en .gitignore; solo se versiona un .env.examplesin valores reales.
Señales de falla: .env versionado; asignaciones del tipo password = "..."o apiKey: '...'; claves en docker-compose.yml, en un Dockerfile( ENVo ARG) o en archivos de configuración.
Comandos sugeridos (no muestran valores):
git ls-files | grep -E '(^|/)\.env$' ; git check-ignore -v .env
Evidencia (sin valores):
Comando: git ls-files | grep -E '(^|/)\.env$'→ rastreados .env.example, frontend/.env, frontend/.env.exampley ejemplos de herramientas. No is the .envroot.
Comando: git check-ignore -v .env→ .gitignore:38:.env	.env(el .envde raíz se ignora).
frontend/.env sí está versionado (5 líneas; claves de flags Vite, no se copian valores aquí).
JWT con secreto por defecto en backend/src/main/resources/application.yaml:37.
JWT con fallback en Compose: docker-compose.yml:80( SIGESA_JWT_SECRET=${SIGESA_JWT_SECRET:-<literal versionado>}).
Contraseña de Postgres por defecto: docker-compose.yml:12,82y application-dev.yaml:5.
Contraseñas de cuentas demo en código: backend/src/main/java/com/umss/sigesa/config/AuthDataLoader.java:34-39( @Profile("!prod")) y documentadas en README.md(tabla de credenciales de desarrollo).
Dockerfiles: no hay COPY .envni ENVcon PASS/SECRET ( backend/Dockerfile, frontend/Dockerfile).
Estado: FALLA
Observación: El .envraíz está bien ignorado y hay .env.example. Aun así el repositorio contiene (1) secreto JWT por defecto utilizable en dev y Compose, (2) contraseña de BD por defecto, (3) contraseñas de seed en Java/README, (4) frontend/.envversionado. Matiz: frontend/.env versionado y los fallbacks en application.yaml/ application-dev.yamlson deuda técnica aceptable exclusivamente para perfiles locales ( dev/ default de laboratorio) . En producción o puesta en escena el arranque debe fallar si SIGESA_JWT_SECRETo SPRING_DATASOURCE_PASSWORDno están definidos (el perfil prodya exige JWT sin default en application-prod.yaml:21; la contraseña de BD y DATABASE_URLaún tienen huecos). Problema técnico: Separar perfiles application-dev.ymly application-prod.ymlexigir variables sin fallback para JWT_SECRETySPRING_DATASOURCE_PASSWORD . Complemento: quitar el literal de Compose y sacar frontend/.envdel índice ( git rm --cached).
Auditó: Asistente de auditoría (Cursor)
F12-03 · Procesos sin estado (factor VI)
Qué se busca: nada importante vive en la memoria del proceso ni en su disco local entre peticiones: sesiones, archivos subidos y cachés compartidos van a servicios de apoyo (base de datos, Redis, almacenamiento de objetos). Así se pueden levantar varias instancias.
Señales de error: archivos subidos guardados en una carpeta local; sesiones en memoria; un Mapo diccionario global usado como caché compartido; trabajos programados que asumen una sola instancia.
Comandos sugeridos:
git grep -nE "Files.write|FileOutputStream|ConcurrentHashMap" -- backend/src/main/java
Evidencia:
JWT sin servidor de sesiones: backend/src/main/java/com/umss/sigesa/adapter/out/auth/JwtTokenAdapter.java:26-30,34-48(token firmado, sin estado).
Blobs de evidencia en disco local: backend/src/main/java/com/umss/sigesa/adapter/out/evidence/LocalFileEvidenceBlobStorageAdapter.java:17-29( sigesa.evidence.storage-path, predeterminado ./data/evidences).
Reportes en disco: backend/src/main/resources/application.yaml:39-42; LocalFileReportArtifactStorageAdapter+ FileOutputStreamen OpenPdfReportStreamingAdapter.java:32y ApachePoiReportStreamingAdapter.java:32.
Candado de carga en memoria de proceso: backend/src/main/java/com/umss/sigesa/adapter/out/evidence/InMemoryEvidenceUploadLockAdapter.java:10-16( ConcurrentHashMap).
Jobs de reporte: metadatos en BD ( ReportExportJobService.java:40-43) pero el archivo resultante es una ruta local ( markCompleted(file.getAbsolutePath())en :66).
Compose del servicio backend( docker-compose.yml:61-96): no monta volumen para ./data/evidencesni ./data/reports.
No hay express-session/ MemoryStore(no es Node en la API).
Estado: FALLA
Observación: La autenticación sí es stateless (JWT) y el estado de negocio está en PostgreSQL. Matiz (FALLA crítica para escalado): guardar PDFs/evidencias en el disco del contenedor y manejar el lock con ConcurrentHashMaprompe el Factor VI e impide escalar horizontalmente a más de 1 réplica . Con (N=1) (Compose mononodo, P1) es tolerable en demo; un docker compose up --scaleo una segunda JVM perdería blobs y no coordinaría las cargas. Issue técnico (candidato a feature individual del sprint): Migrar almacenamiento de evidencias a MinIO/S3 (blob Storage intercambiable) y sustituir lock en memoria por lock distribuido (Redis/DB lock) o atomicidad en base de datos . No persistir file.getAbsolutePath()como contrato de descarga.
Remediación 8.1.0 (no cambia el veredicto): GitHub #51 // PR-IMPL-041rama feat/51-s3-evidence-lock. Evidencias y reportes → Compatible con S3 (Componga: pgsty/silo; imágenes oficiales MinIO Hub/Quay no públicas); bloquear → evidence_upload_lock; descarga GET /document-assets/{id}/content.
Auditó: Asistente de auditoría (Cursor)
F12-04 · Registros a la salida estándar (factor XI)
Qué se busca: la aplicación escribe sus eventos en la salida estándar (idealmente en formato estructurado, por ejemplo JSON); recogerlos y guardarlos es tarea de la plataforma.
Señales de error: el registrador escribe en archivos propios ( app.log, rotación de archivos); console.logsueltos con datos sensibles.
Comandos sugeridos:
git grep -nE "transports\.File|DailyRotateFile|logback.*FILE|logging.file" -- .
git grep -nE "LoggerFactory" -- backend/src/main/java
Evidencia:
No hay logback-spring.xmlni logging.fileen el repositorio de producto ( git grep logbacken yaml/xml: sin coincidencias).
Spring Boot usa Logback por defecto en la consola (stdout del contenedor java -jar).
Uso de SLF4J: p. ej. backend/src/main/java/com/umss/sigesa/adapter/out/auth/NoOpAuditLogAdapter.java:5-6,14; Slf4jAssistantToolAuditAdapter.java:5-12.
Frontend: customFetchno escribe secretos; no hay registrador de archivo.
Bitácora de dominio UC-017 sigue en NoOpAuditLogAdapter(no es rotación de fichero; es ausencia de fregadero).
Estado: PARCIAL
Observación: Cumple el espíritu de “logs a stdout” (no hay app.logpropio). No hay formato JSON estructurado ni evaluación de request-id documentada. Aceptable en laboratorio. Ticket: logging.pattern JSON (codificador logstash) y MDC con requestId; no loguear Autorización ni JWT.
Auditó: Asistente de auditoría (Cursor)
F12-05 · Servicios de apoyo intercambiables (factor IV)
Qué se busca: base de datos, colas, caché, almacenamiento y servicio de IA se conectan por una URL o variables de configuración, de modo que cambiar uno (por ejemplo, la base local por una gestionada) no exige tocar el código.
Señales de falla: el cliente de un servicio se construye con host, puerto o credenciales fijas; el código depende de un proveedor concreto sin una capa que lo pasillo.
Comandos sugeridos:
git grep -nE "DATABASE_URL|SPRING_DATASOURCE|S3_|OPENAI|ASSISTANT_BASE_URL" -- backend/src docker-compose.yml
Evidencia:
BD por URL/env: application-dev.yaml:3, application-prod.yaml:6-8, Compose SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/sigesa( docker-compose.yml:81).
LLM por URL: application.yaml:47 SIGESA_ASSISTANT_BASE_URL; cliente OpenWebUiChatAdapter.java:59( normalizeBaseUrl(properties.getBaseUrl()) + "/v1/chat/completions"). Puerto hexagonal ChatCompletionPort.
Almacenamiento: puerto EvidenceBlobStoragePortpero única implementación local ( LocalFileEvidenceBlobStorageAdapter.java:13). No hay cliente S3/MinIO en el código de producto.
No hay Redis/AMQP ( git grepde REDIS_URL|AMQPen backend: no aplica).
Auth local (tabla app_user) detrás de AuthPort(diseño hexagonal; LDAP no está cableado).
Estado: PARCIAL
Observación: BD e IA sí se apuntan por URL. El blob store no es intercambiable en la práctica (solo disco). Ayuda hexagonal, pero falta adaptador de objeto. Ticket: implementación MinIO/S3 de EvidenceBlobStoragePortseleccionable por SIGESA_EVIDENCE_STORAGE=s3+ S3_ENDPOINT.
Auditó: Asistente de auditoría (Cursor)
F12-06 · Construcción inmutable y apagado limpio (factores V y IX)
Qué se busca: se construye una vez (imagen o artefacto) y se ejecuta igual en todos los entornos, sin secretos dentro de la imagen; y el proceso, al recibir SIGTERM, deja de aceptar peticiones, termina lo que tiene en curso y cierra conexiones.
Señales de error: COPY .env o ENVcon secretos en el Dockerfile; CMDen forma de texto ( CMD npm start) en lugar de forma exec; ningún manejo de cierre (en NestJS falta app.enableShutdownHooks(); en Spring alguien puso server.shutdown: immediate; en FastAPI no se cierran recursos en el lifespan).
Comandos sugeridos:
git ls-files | grep -iE "dockerfile|compose"
git grep -nE "COPY \.env|^ENV .*(PASS|SECRET|KEY|TOKEN)|^CMD [^\[]" -- '*Dockerfile*'
git grep -nE "SIGTERM|enableShutdownHooks|server.shutdown" -- .
Evidencia:
backend/Dockerfile:16-20: imagen JRE, COPYsolo app.jar, ENTRYPOINT ["java", "-jar", "app.jar"](forma ejecutiva).
frontend/Dockerfile:27-31: nginx, CMD ["nginx", "-g", "daemon off;"](exec). Args Vite de flags, no secretos de JWT/BD.
git grep SIGTERM|enableShutdownHooks|server.shutdownen el repo de producto: sin coincidencias .
No hay server.shutdown: gracefulen application.yaml/ application-prod.yaml. Spring Boot por defecto usa desactivado inmediatamente el conector Tomcat salvo que se configura graceful.
Archivo de bloqueo frontal: frontend/pnpm-lock.yaml+ pnpm install --frozen-lockfile( frontend/Dockerfile:11). Backend: pom.xml(Maven no trae lockfile; versiones del BOM Spring Boot 4.1.0). MCP: .gitignoreignora mcp/**/pnpm-lock.yaml(dependencias no pinneadas en git).
Redactar: ​​un contenedor por servicio, restart: always; No hay manifiestos Kubernetes de actualización continua.
Estado: PARCIAL
Observación: La imagen backend es razonable (multi-stage, exec form, sin .envcopiado). Falta apagado limpio explícito: un SIGTERMde Compose/K8s puede cortar cargas y llamadas LLM de 120 s. Matiz (tolerable en dev; ticket de config, no de código): en Spring Boot el apagado limpio se resuelve habilitando server.shutdown=gracefuly spring.lifecycle.timeout-per-shutdown-phase=20sen YAML, sin tocar código Java . El tiempo de espera de 20 s no cubre un LLM de 120 s: para Compose conviene además stop_grace_period≥ timeout LLM. MCP npm start→ node src/index.js(aceptable); Frontend Docker en forma ejecutiva.
Auditó: Asistente de auditoría (Cursor)
4. Preguntas al grupo
N.º	Comprobación	Pregunta	Respuesta del grupo	Estado (respondida / sin respuesta)
P1	F12-01 / F12-06	¿El despliegue real (si existe) usa SPRING_PROFILES_ACTIVE=prody cuántas réplicas del backend?	(N = 1) réplica. Despliegue actual: Docker Compose mononodo ( container_name: sigesa-backend). Redactar estados unidos SPRING_PROFILES_ACTIVE=dev, no prod.	respondida
P2	F12-02	¿El literal de SIGESA_JWT_SECRETen Compose se rotó alguna vez fuera del git, o es el mismo de todos los clones?	El fallback versionado es el mismo para todos los clones; no se rotó fuera de git. Aceptable solo en dev; prod debe fallar al arrancar sin env.	respondida
P3	F12-03	¿Hay volumen o cubeta de evidencias en algún entorno no descrito en este repositorio?	No. Solo disco local del contenedor ( ./data/evidences, ./data/reports) sin volumen Compose ni MinIO/S3.	respondida
P4	Datos	Nombres de integrantes del grupo para la autoverificación de compromisos individuales.	Boris Anthony Angulo Urquieta	respondida
5. Hallazgos priorizados
Nota: «ticket» / «emisión técnica» en esta ficha no es un módulo de SIGESA . Es el nombre que usa la plantilla de la materia para una tarea de corrección (qué habría que cambiar después). En el código del producto no existe un sistema de tickets.

#	Comprobación	Severidad (alta / media / baja)	Qué falla	Corrección propuesta	Responsable	¿Antes de contenedorizar (S07)?
1	F12-02	alta	JWT y contraseña de BD predeterminada en YAML/Compose; demostración de contraseñas esAuthDataLoader	Problema: perfiles devvs prod; sin fallback para JWT_SECRETy SPRING_DATASOURCE_PASSWORDen prod/staging	Boris Anthony Angulo Urquieta	Si
2	F12-03	alta	Evidencias/PDF en disco del contenedor; bloquear en memoria	Asunto: MinIO/S3 + bloqueo distribuido (candidato a característica individual)	Boris Anthony Angulo Urquieta	Sí, antes de (N>1)
3	F12-01	medios de comunicación	Fallbacks localhosty H2 por defecto	Fail-fast de config en prod	Boris Anthony Angulo Urquieta	Si
4	F12-06	medios de comunicación	Pecadoserver.shutdown=graceful	Solo YAML: server.shutdown=graceful+spring.lifecycle.timeout-per-shutdown-phase=20s	Boris Anthony Angulo Urquieta	Si
5	F12-04	Baja	Registros no estructurados	JSON y salida estándar	Boris Anthony Angulo Urquieta	No
6	F12-05	medios de comunicación	Tienda Blob no intercambiable	Cubierto por el número de F12-03 (MinIO/S3)	Boris Anthony Angulo Urquieta	Recomendado
6. Autoverificación (la marca el asistente; el grupo la confirma)
Las seis comprobaciones F12-01 a F12-06 están en la sección 3, una vez cada una.
Ninguna quedó en PENDIENTEni sin estado; todos los estados son de los cinco permitidos.
Todo estado distinto de N/Ay NO VERIFICABLEtiene evidencia con ruta y línea, o con comando y salida.
Cada NO VERIFICABLEy cada N/Aexplica su motivo. (ninguno en F12-01…06)
Todas las preguntas de la sección 4 están respondidas o marcadas "sin respuesta".
Los hallazgos de severidad alta tienen responsabilidad.
No hay ningún secreto copiado en este archivo.
No se modificó el código del producto (solo este informe y el de concurrencia).
Cada integrante aparece como auditor de al menos una comprobación (commit individual en la rama de entrega).
7. Resumen
DE ACUERDO	PARCIAL	FALLA	N / A	NO VERIFICABLE	Preguntas sin respuesta
0	3	2	0	0	0
Conclusión en dos líneas: El API es un único código base con JWT stateless y BD/LLM configurables por env, pero no es 12-factor en secretos ni en blobs . Los fallbacks de secretos son tolerables solo en dev; el disco local + ConcurrentHashMapes FALLA crítica si (N>1). Tickets inmediatos: fail-fast de JWT_SECRET/ SPRING_DATASOURCE_PASSWORDen prod, MinIO/S3 + lock distribuido, y server.shutdown=gracefulen YAML.

## Contexto
El proyecto requiere sincronización perfecta entre los contratos del backend (Spring Boot) y el frontend (React 19). La IA y los desarrolladores perdían tiempo escribiendo interfaces y llamadas HTTP manuales, propensas a errores de tipado.

## Decisión
1. Adoptar arquitectura de Monorepositorio (`/backend` y `/frontend`).
2. Utilizar **Orval** en el frontend para autogenerar hooks de **React Query** y tipos de **TypeScript** a partir del OpenAPI (`/v3/api-docs`) del backend.
3. Prohibir estrictamente el uso manual de `fetch` o `axios` para consumo de APIs internas.

## Consecuencias
- **Positivas:** Tipado estricto end-to-end, eliminación de código repetitivo, la IA (Cursor) tiene contexto completo del proyecto.
- **Negativas:** El backend debe estar corriendo localmente antes de poder generar el código del frontend.
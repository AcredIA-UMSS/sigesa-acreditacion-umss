# Contract Test Agent — SIGESA (Lic. Marlene)

Contratos JSON en la carpeta de equipo `com.sigesa.app.contracts` (no `unit/` ni `integration/`).

```
backend/src/test/
├── java/com/sigesa/app/
│   ├── unit/          # Aylen
│   ├── integration/   # Alex
│   └── contracts/     # Lic. Marlene
│       ├── assistant/
│       ├── evidence/
│       ├── auth/
│       └── JsonContracts.java
└── resources/
    └── application-test.yaml    # perfil test (H2); el lab lo nombra .yml
```

Package: `com.sigesa.app.contracts.{assistant,evidence,auth}`.

```bash
cd tools/contract-test-agent
./setup.sh
./run.sh correr
```

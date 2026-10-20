# Reverse Engineering Timestamp

## Run Record

- Date: 2026-10-20
- Commit: 521585cbd1748757624ef5e05ad649d6cc895d81

## Scope of Analysis

```yaml
scope_version: 1
kind: partial
intent: process-stats
fingerprint: d2295af9c2e73e774301f9c8e3193353d38c4378
analyzed:
  paths:
    - backend/pom.xml
    - backend/src/main/java/com/umss/sigesa/adapter/in/security/JwtAuthenticationFilter.java
    - backend/src/main/java/com/umss/sigesa/adapter/in/security/JwtProgramScopeSupport.java
    - backend/src/main/java/com/umss/sigesa/adapter/in/security/RestAccessDeniedHandler.java
    - backend/src/main/java/com/umss/sigesa/adapter/in/security/RestAuthenticationEntryPoint.java
    - backend/src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java
    - backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java
    - backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessFreezeController.java
    - backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessResponsibleController.java
    - backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/ProcessSummaryResponseDto.java
    - backend/src/main/java/com/umss/sigesa/adapter/out/auth/JwtTokenAdapter.java
    - backend/src/main/java/com/umss/sigesa/adapter/out/persistance/ProcessQueryJpaAdapter.java
    - backend/src/main/java/com/umss/sigesa/adapter/out/persistance/entity/AccreditationProcessJpaEntity.java
    - backend/src/main/java/com/umss/sigesa/adapter/out/persistance/mapper/ProcessPersistenceMapper.java
    - backend/src/main/java/com/umss/sigesa/adapter/out/persistance/repository/SpringDataAccreditationProcessRepository.java
    - backend/src/main/java/com/umss/sigesa/application/model/process/ProcessQueryContext.java
    - backend/src/main/java/com/umss/sigesa/application/port/out/ProcessQueryPort.java
    - backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessDetailService.java
    - backend/src/main/java/com/umss/sigesa/application/service/process/ListProcessesService.java
    - backend/src/main/java/com/umss/sigesa/application/service/process/ProcessAccessPolicy.java
    - backend/src/main/java/com/umss/sigesa/config/ProcessModuleConfig.java
    - backend/src/main/java/com/umss/sigesa/domain/model/AccreditationProcess.java
    - backend/src/main/java/com/umss/sigesa/domain/model/OperationalMode.java
    - backend/src/main/java/com/umss/sigesa/domain/model/ProcessStatus.java
    - backend/src/main/java/com/umss/sigesa/domain/model/Role.java
    - backend/src/main/resources/application-dev.yaml
    - backend/src/main/resources/application-prod.yaml
    - backend/src/main/resources/application.yaml
    - backend/src/main/resources/db/migration/V17__methodological_workflow_stages.sql
    - backend/src/test/java/com/umss/sigesa/application/service/process/ListProcessesServiceTest.java
  components:
    - ProcessController
    - ListProcessesService
    - GetProcessDetailService
    - ProcessAccessPolicy
    - ProcessQueryJpaAdapter
    - JwtAuthenticationFilter
    - SecurityConfig
    - AccreditationProcess
shallow:
  paths:
    - frontend/
    - docs/
    - tools/
    - backend/src/main/java/com/umss/sigesa/adapter/in/web/
    - backend/src/main/resources/db/migration/
    - backend/src/test/java/com/umss/sigesa/
    - backend/src/main/java/com/umss/sigesa/application/service/workflow/
    - backend/src/main/java/com/umss/sigesa/config/
```

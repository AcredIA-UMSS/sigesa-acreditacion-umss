# AGENTS.md

Purpose
-------
Document how Copilot agents (cloud and local) should behave in this repository: which specialized agents to use, repository-specific rules, security constraints, and operational guidance for maintainers.

Location
--------
Place this file at the repository root so Copilot CLI and other tooling can discover repository-specific agent guidance.

Recommended Agents
------------------
- code-review: high-signal reviews of PRs (bugs, security, logic). Do NOT comment on trivial style/formatting.
- task: run build/test/lint tasks and report failures succinctly.
- explore: use for broad codebase research or cross-cutting analysis.
- research: for external web/GitHub searches and citation-backed findings.

Repository-specific rules
-------------------------
- Never expose secrets or credentials in agent output. Agents must redact or refuse to include secrets.
- Agents must not modify files outside the repository root or any configured allowed paths.
- Agents must never push commits directly to protected branches (main, release/*) without a PR and human review.
- For code-review agents: focus on logic/security/bugs; do not raise style nitpicks.
- Use DTOs for API controllers; do not expose JPA entities directly in responses (project convention).

Copilot / Cloud-agent integration
--------------------------------
- If preinstall steps are required, provide a `.github/workflows/copilot-setup-steps.yml` with a `copilot-setup-steps` job. Allowed job keys: `steps`, `permissions`, `runs-on`, `services`, `snapshot`, `timeout-minutes` (max 59).
- To enable Git LFS, set `lfs: true` on `actions/checkout` in the setup steps.
- Use runner labels in `runs-on` to select larger or self-hosted runners when needed.
- If using self-hosted runners, ensure network/firewall rules allow required Copilot endpoints and disable Copilot integrated firewall if required (see GitHub docs).

Security & secrets
------------------
- Add any required secrets or env vars to the repository's `copilot` environment (Settings → Environments → copilot) as secrets or variables.
- Agents must not print or commit secret values.
- Mark sensitive directories (e.g., `secrets/`, `deploy/keys`) in this doc so human reviewers and agents avoid accidental exposure.

Agent configuration template (example)
--------------------------------------
You may include repository-level directives here or in `.github/copilot-instructions.md`.

Example: Code-review agent intent
```
agent: code-review
scope:
  - "src/**/*.java"
  - "src/**/*.kt"
rules:
  - "Report only bugs, security issues, and correctness problems."
  - "Do not comment on formatting or trivial style." 
  - "If a code change modifies database access or transactions, highlight potential performance or consistency issues."
preferences:
  model: gpt-5-mini
  max_results: 20
```

Checklist — add or verify
-------------------------
- [ ] Default branch name recorded (e.g., `main`).
- [ ] `copilot-setup-steps.yml` exists if agents need preinstalled deps.
- [ ] `.github/copilot-instructions.md` present if fine-grained instructions are needed.
- [ ] Sensitive paths listed and protected from agent output.
- [ ] Self-hosted runner/network rules documented if used.
- [ ] Contact/owner for agent decisions (team or person) documented.

Maintenance and updates
-----------------------
Keep AGENTS.md in sync with repo conventions and CI changes. When changing runner or network requirements, update the Copilot setup steps and this file.

Contact
-------
Repository owners / maintainers: add names or team contact here.

Notes specific to this project
------------------------------
- Backend: Java 21, Spring Boot. Prefer agents that understand Java and Spring conventions.
- Persistence: Spring Data JPA; enforce rule to never return JPA Entities directly from controllers — use DTOs.
- Build/test: use existing Maven/Gradle steps in CI; prefer `task` agent to run `mvn -q test` and summarize failures.

If you want, add more specific per-agent instruction files under `.cursor/` or request a tailored AGENTS.md with stricter rules + `./agents.md`.

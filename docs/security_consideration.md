# Security Considerations — Job Application Tracker

## Threat Model Scope
Single-user personal utility app with a deployed backend. Not handling payment info, not multi-tenant. Threat model is intentionally lightweight — this is not a production SaaS.

## Realistic Risks (v1)
1. **Unauthenticated public backend** — if deployed with no auth, anyone who finds the URL could read/write your application data.
   - *Mitigation*: minimum viable — a static API key header (`X-API-Key`) checked on every route via Ktor plugin. Store default key via `local.properties`/BuildConfig or configure at runtime via `ServerConfigDialog` (persisted securely in app-private `SharedPreferences`).
2. **Data exposure via logs** — Ktor/Android logging could leak application data (company names, notes) into logs on a shared device or CI.
   - *Mitigation*: no `Log.d`/`println` of full request/response bodies in production builds; strip verbose debug logs in release config.
3. **Database exposure & transit security** — backend connects to serverless Neon PostgreSQL.
   - *Mitigation*: connections require TLS (`sslmode=require`), connection string is injected via environment variable `DATABASE_URL` (never committed to Git), and HikariCP handles secure pooled connections.
4. **Man-in-the-middle on the API call** — Android app talking to backend over plain HTTP would expose data in transit.
   - *Mitigation*: HTTPS/TLS only. Render & Railway provide TLS by default — don't disable it or fall back to HTTP.
5. **Secrets in repo** — API keys, backend URLs with embedded credentials.
   - *Mitigation*: `.gitignore` for `local.properties`, `.env` files, and `data/*.db`. Never commit real credentials to GitHub.

## Explicitly Deferred (not v1 concerns)
- OAuth/user accounts — irrelevant until there's more than one user.
- Rate limiting — irrelevant at single-user traffic levels.
- Input sanitization beyond basic validation — Exposed SQL ORM parameterizes queries by default, preventing SQL injection.

## If This Repo Goes Public (portfolio use)
- Scrub any real deployed backend URL + API key from README/commit history before making the repo public, or rotate the key after making it public.
- Don't commit real personal application data (actual company names/notes) as seed/test data — use fake placeholder data in any public-facing demo.

# Security Considerations — Job Application Tracker

## Threat Model Scope
Single-user personal utility app with a deployed backend. Not handling payment info, not multi-tenant. Threat model is intentionally lightweight — this is not a production SaaS.

## Realistic Risks (v1)
1. **Unauthenticated public backend** — if deployed with no auth, anyone who finds the URL could read/write your application data.
   - *Mitigation*: minimum viable — a static API key header (`X-API-Key`) checked on every route. Not real auth, but stops casual/accidental access. Store the key in Android via `local.properties`/BuildConfig, never hardcoded in a committed file.
2. **Data exposure via logs** — Ktor/Android logging could leak application data (company names, notes) into logs on a shared device or CI.
   - *Mitigation*: no `Log.d`/`println` of full request/response bodies in production builds; strip logs in release config.
3. **SQLite file exposure** — if backend is compromised or misconfigured, the whole DB is one file.
   - *Mitigation*: acceptable risk for v1 given low sensitivity of data (job application notes, not financial/health data). Revisit if this ever gets more sensitive content.
4. **Man-in-the-middle on the API call** — Android app talking to backend over plain HTTP would expose data in transit.
   - *Mitigation*: HTTPS only. Free hosts (Railway/Render) provide TLS by default — don't disable it or fall back to HTTP for "convenience."
5. **Secrets in repo** — API keys, backend URLs with embedded credentials.
   - *Mitigation*: `.gitignore` for `local.properties`, any `.env` files. Never commit real deployed URLs+keys together in a public repo if the repo is meant to be portfolio-visible.

## Explicitly Deferred (not v1 concerns)
- OAuth/user accounts — irrelevant until there's more than one user.
- Rate limiting — irrelevant at single-user traffic levels.
- Input sanitization beyond basic validation — no rendering of user input as HTML/SQL anywhere unsafe (Exposed parameterizes queries by default, so SQL injection isn't a realistic risk here).

## If This Repo Goes Public (portfolio use)
- Scrub any real deployed backend URL + API key from README/commit history before making the repo public, or rotate the key after making it public.
- Don't commit real personal application data (actual company names/notes) as seed/test data — use fake placeholder data in any public-facing demo.

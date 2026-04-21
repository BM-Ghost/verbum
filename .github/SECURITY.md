# Security Policy

Maintained by [Bwire](https://meshackbwire.vercel.app/).

## Supported Versions

Only the latest production release on the Play Store is actively supported. Older releases do not receive patches.

| Version | Supported |
|---|---|
| Latest release on `main` | ✅ |
| Previous releases | ❌ |

---

## Reporting a Vulnerability

**Please do not open a public GitHub Issue for security vulnerabilities.**

If you find something, report it privately so it can be fixed before anyone takes advantage of it.

**How to report:**

1. Go to the [Security tab](../../security) of this repository
2. Click **"Report a vulnerability"** — this opens a private advisory only visible to you and the maintainer
3. Fill in as much detail as you can (see checklist below)

Alternatively, reach out directly through [meshackbwire.vercel.app](https://meshackbwire.vercel.app/).

### What to Include

- **Description** — what is the vulnerability and where is it
- **Impact** — what could an attacker realistically do with it
- **Steps to reproduce** — how to trigger the issue reliably
- **Affected versions** — which app version(s) are impacted
- **Suggested fix** (optional, but always appreciated)

---

## Response Commitment

I check reports personally. Here's what to expect:

| Timeline | What happens |
|---|---|
| Within **72 hours** | I'll acknowledge the report |
| Within **7 days** | I'll share an initial severity assessment |
| Within **30 days** | Patch shipped for critical/high severity issues |
| After the patch | Credit offered to you if you'd like it |

For critical vulnerabilities (CVSS ≥ 9.0), I'll fast-track the fix.

---

## Scope

### In Scope

- Android app binary (`com.verbum.app`)
- Authentication and session management
- Local data storage and encryption (Room database)
- Network communication (API endpoints used by the app)
- Third-party SDK integrations bundled in the app
- CI/CD pipeline configuration that could compromise build integrity

### Out of Scope

- Vulnerabilities in third-party services (Firebase, Google Play, etc.) — report to those services directly
- Social engineering attacks
- Denial-of-service attacks
- Issues in dependencies that are already publicly disclosed and have a pending update

---

## Disclosure Policy

I follow **coordinated disclosure**:

1. You report privately
2. I validate and build a fix
3. Fix ships to production
4. We agree on a public disclosure date (usually 14–30 days after the patch)
5. A public advisory goes out with your name on it, if you want

Please give reasonable time to fix before going public. That's all I ask.

---

## Security Controls in This Repository

For reference, here's what's already in place:

- **Least-privilege permissions** — every workflow and job declares only the permissions it actually needs
- **No credentials in source** — all secrets live in GitHub Settings, injected at runtime
- **Keystore handling** — decoded at build time, deleted immediately after; never written to the runner's persistent storage
- **CodeQL SAST** — weekly static analysis covering Java/Kotlin security patterns
- **TruffleHog** — weekly scan across full commit history for accidentally committed credentials
- **Dependency Review** — every PR is checked; HIGH and CRITICAL severity dependencies block merges
- **Dependabot** — weekly dependency updates, auto-PRed to `dev`
- **CODEOWNERS** — all infrastructure files require maintainer approval before any change merges
- **Branch protection** — required status checks and code review on every protected branch
- **Manual approval gates** — UAT and production deployments need explicit sign-off before they run

# Verbum — CI/CD Pipeline Setup Guide

> **Audience:** Repository owner (`@BM-Ghost`) only. This document covers secrets,
> environments, and branch protection configuration required to activate the
> GitHub Actions pipelines defined in `.github/workflows/`.

---

## Pipeline Overview

| Workflow file | Trigger | Environment | Manual approval | Destination |
|---|---|---|---|---|
| `01-pr-validation.yml` | PR → dev / sit / uat / main | — | No | Status checks on PR |
| `02-ci-dev.yml` | Push → `dev` | — | No | Debug APK artifact |
| `03-deploy-sit.yml` | Push → `sit` / manual | `sit` | No | Firebase App Distribution (SIT) |
| `04-deploy-uat.yml` | Push → `uat` / manual | `uat` | **Yes** | Firebase App Distribution (UAT) |
| `05-deploy-production.yml` | Push → `main` / manual | `production` | **Yes** | Google Play Store + GitHub Release |
| `06-security-scan.yml` | Weekly / Push → main\|uat / manual | — | No | GitHub Security tab |

---

## Step 1 — Required GitHub Secrets

Go to **Settings → Secrets and variables → Actions → New repository secret** and add all of the following.

### Signing Secrets

| Secret name | Value |
|---|---|
| `KEYSTORE_BASE64` | `base64 -i your-release-keystore.jks` output (see §1.1) |
| `KEYSTORE_PASSWORD` | Keystore store password |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Key password |

#### §1.1 — How to base64-encode your keystore (macOS / Linux)

```bash
base64 -i path/to/verbum-release.jks | pbcopy
# Output is now in your clipboard — paste it as the secret value
```

> Keep the original `.jks` file in a **password-manager vault** (not in the repository).

### Firebase Secrets

| Secret name | Value |
|---|---|
| `FIREBASE_APP_ID_SIT` | Firebase Console → Project settings → Your apps → App ID (SIT flavour) |
| `FIREBASE_APP_ID_UAT` | Firebase Console → Project settings → Your apps → App ID (UAT flavour) |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Contents of the JSON key file from IAM service account with **Firebase App Distribution Admin** role |

#### How to create the Firebase service account JSON

1. [Firebase Console](https://console.firebase.google.com) → Project settings → Service accounts
2. Click **Generate new private key** → download the `.json` file
3. Paste the **entire JSON content** (not base64) as the secret value

### Google Play Secrets

| Secret name | Value |
|---|---|
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | Contents of the JSON key from a Google Play service account with **Release manager** role |

#### How to create the Play Store service account

1. [Google Play Console](https://play.google.com/console) → Setup → API access
2. Link to a Google Cloud project, create a service account with **Release manager** permissions
3. Generate a JSON key and paste the full content as the secret value

---

## Step 2 — GitHub Environments

Go to **Settings → Environments** and create three environments:

### `sit` environment

- **No required reviewers** (auto-deploy on merge to `sit`)
- Deployment branches: `sit` only

### `uat` environment

- **Required reviewers:** `@BM-Ghost`
- Deployment branches: `uat` only
- Optional: add a wait timer (e.g. 5 minutes) to allow cancellation

### `production` environment

- **Required reviewers:** `@BM-Ghost`
- Deployment branches: `main` only
- Recommended: add a wait timer of 5–10 minutes

---

## Step 3 — Branch Protection Rules

Go to **Settings → Branches → Add rule** for each protected branch.

### `dev`

| Setting | Value |
|---|---|
| Require status checks before merging | ✅ |
| Required status checks | `Branch Naming Convention`, `Engineering Guidelines`, `Android Lint`, `Unit Tests`, `Build Verification`, `Dependency Review`, `✅ PR Gate` |
| Require branches to be up to date | ✅ |
| Require pull request reviews | 1 approving review |
| Dismiss stale reviews | ✅ |
| Require review from Code Owners | ✅ |
| Do not allow bypassing the above settings | ✅ |

> **Note on Dependency Review:** This check requires either (a) a **public repository**, or (b) a private repository with **GitHub Advanced Security** enabled. If the repository is private and Advanced Security is not available on your plan, omit `Dependency Review` from the required checks list — the check will still run and report, but will not block merges.

### `sit`

Same as `dev`, plus:

| Setting | Value |
|---|---|
| Restrict who can push to matching branches | `@BM-Ghost` only |

### `uat`

Same as `sit`.

### `main`

Same as `uat`, plus:

| Setting | Value |
|---|---|
| Require linear history | ✅ |
| Require signed commits | ✅ (recommended) |

---

## Step 4 — `app/build.gradle.kts` Signing Config

Add the following signing configuration to `app/build.gradle.kts` so the CI
workflows can inject credentials via environment variables at build time:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile   = file(System.getenv("KEYSTORE_FILE_PATH") ?: "placeholder.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias    = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            signingConfig   = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

> The `KEYSTORE_FILE_PATH` environment variable is set by the workflow steps
> (`verbum-sit.keystore`, `verbum-release.keystore`, or `verbum-production.keystore`).
> The keystore file is decoded from the `KEYSTORE_BASE64` secret and deleted after the build.

---

## Step 5 — Firebase App Distribution — Tester Groups

In the Firebase Console for each project variant:

1. **App Distribution → Testers & Groups → Add group**
2. Create a group named `sit-testers` (SIT project) and `uat-testers` (UAT project)
3. Add tester email addresses to each group

---

## Step 6 — Dependabot (auto-activated)

The `.github/dependabot.yml` file is already configured. Dependabot will
automatically open weekly PRs against `dev` for:

- Gradle dependency updates (grouped by ecosystem: AndroidX, Compose, Hilt)
- GitHub Actions pinned-version updates

No additional setup required.

---

## Step 7 — GitHub Actions SHA Pinning (Supply-Chain Hardening)

The workflow files currently reference actions by floating version tags (e.g.
`actions/checkout@v4`). While convenient, these tags can be moved by the
action publisher, creating a supply-chain risk.

To fully harden the pipeline, pin each action to its exact commit SHA:

```yaml
# Instead of:
- uses: actions/checkout@v4

# Use the SHA for that tag (example — verify the current SHA on GitHub):
- uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683  # v4.2.2
```

To find the SHA for any action:
1. Go to the action's GitHub repository (e.g. `https://github.com/actions/checkout`)
2. Click the tag you want (e.g. `v4`)
3. Copy the full commit SHA from the URL or commit details

Dependabot (already configured) will open PRs to keep these SHAs up to date
when new versions are released — so pinning does not mean manual maintenance.

**Priority:** Pin the most security-sensitive steps first:
1. `actions/checkout` (code is checked out here)
2. `gradle/actions/setup-gradle` (cache write access)
3. `github/codeql-action/*` (security analysis)
4. `r0adkll/upload-google-play` (Play Store deployment)

---

## Security Visibility Rationale

The `.github/` directory — including workflow files — is intentionally **visible
to all contributors**. This is standard professional practice:

- **Transparency** builds trust with contributors
- Workflow logic contains no credentials (all secrets are injected via GitHub Secrets)
- `CODEOWNERS` ensures all `.github/` changes require owner review before merging
- Branch protection prevents unauthorised changes from reaching protected branches
- Secrets live in **GitHub Settings**, never in source files

---

## Quick Reference — Branching Strategy

```
feature/your-feature
        │
        ▼ PR (01-pr-validation)
       dev ──────────────────── CI build + artifact (02-ci-dev)
        │
        ▼ merge
       sit ──────────────────── Auto-deploy to Firebase SIT testers (03-deploy-sit)
        │
        ▼ merge
       uat ──────────────────── [Manual approval] → Firebase UAT testers (04-deploy-uat)
        │
        ▼ merge
      main ──────────────────── [Manual approval] → Play Store (05-deploy-production)
                                 + GitHub Release tag
```

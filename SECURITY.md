# Security

## Why any of this exists

Let's be honest up front: **none of this is necessary here.** This is a
dice game for five friends at a festival. Nobody is attacking it, and the
CRA does not apply to a private app that is not placed on the market.

The real reason is that I wanted to learn how SBOMs, CVE scanning and
supply-chain hardening actually work - and doing that on a real project I
care about beats reading about it. A small app is a good place to make the
mistakes: when the first SBOM turned out to describe the build machine
instead of the APK and produced 75 CVE findings for libraries that never
ship, that was a lesson, not an incident.

So treat what follows as a deliberately over-engineered exercise. It is
still all true - it just protects rather less than the effort suggests.

## What is actually at stake

The app has **no internet permission**, no accounts, no server and no
telemetry - everything stays on the phone, and history only leaves it when
someone deliberately exports it. That removes most of what would normally
be a security concern here.

Two things are still worth taking seriously, and both are about the build
rather than the app itself.

## What we do

- **The signing key is the crown jewel.** Whoever gets the release keystore
  out of a workflow run could sign APKs the group would trust. Actions are
  therefore pinned to commit SHAs (a tag can be moved), only the release job
  may write, and the Gradle wrapper is checksum-validated before anything
  runs it.
- **We know what we ship.** Every build produces a CycloneDX **SBOM**
  (attached to releases, and as the `sbom` artifact on every run), gets
  scanned for known CVEs, and reports its full dependency tree to GitHub so
  vulnerability alerts cover indirect dependencies too. Findings show up
  under [Security → Code scanning](../../security/code-scanning).
- **Dependencies are kept moving.** Dependabot proposes grouped updates
  weekly, including the Gradle wrapper, and every proposal is built and
  tested before it can be merged.

## Reporting something

Found a problem? Open a
[GitHub issue](../../issues/new) - or, if you would rather not discuss it in
public, message Marcel directly in the group chat. It's five people; there
is no formal process and no need for one.

Expect a reply within a few days. Given the app's scope, the realistic
response to most findings is a dependency bump and a new tag.

## Supported versions

Only the [latest release](../../releases/latest) is maintained. Older APKs
keep working - it's an offline app - but fixes only ever go into a new
version.

## Scope

Out of scope, on purpose: the game rules (they are the rules of the house),
the fact that history can be exported and shared as a plain JSON file (that
is the feature), and anything requiring physical access to an unlocked
phone.

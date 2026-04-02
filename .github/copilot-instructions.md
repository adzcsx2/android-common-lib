# Copilot Project Instructions

This repository is a greenfield project.

## Code Lifecycle Policy

- Do not introduce deprecation annotations or deprecation comments for internal code cleanup.
- Do not keep compatibility shims for old implementations unless explicitly requested.
- When replacing an internal implementation, delete the old code directly and keep only the latest version.
- Prefer minimal, direct removal over staged migration for unused/obsolete code paths.

## Review Policy For Cleanup Changes

- If a feature or abstraction is replaced, remove the old path in the same change when safe.
- Avoid leaving parallel APIs that serve the same purpose.
- Keep public API surface small and current.

## Exception Rule

- If the user explicitly asks to keep backward compatibility, follow the user request.

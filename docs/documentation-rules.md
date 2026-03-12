# Project Documentation Rules

## README.md Preservation Rule

**CRITICAL**: When updating README.md, always preserve all existing content and only add new sections where appropriate.

### What This Means

1. **NEVER** delete or modify existing README sections unless explicitly requested
2. **ONLY** add new content at appropriate locations (typically navigation sections, new feature announcements)
3. **PRESERVE** all existing formatting, structure, and content
4. **RESPECT** the existing documentation style and language

### When to Update README.md

Update README.md only for:

- Adding documentation navigation sections
- Announcing new features in appropriate sections
- Adding installation/usage examples for new features
- Updating version numbers in dependency blocks
- Adding links to new documentation

### When NOT to Modify README.md Content

Do NOT modify:

- Existing feature descriptions
- Project structure descriptions
- Existing code examples
- Module descriptions
- Version information tables
- Existing installation instructions

### Example: Correct Update

**BEFORE:**
```markdown
# Project Name

Description here.

## Features
- Feature 1
- Feature 2
```

**AFTER (adding navigation):**
```markdown
# Project Name

Description here.

## Documentation
- [Guide 1](/docs/guide1.md)
- [Guide 2](/docs/guide2.md)

## Features
- Feature 1
- Feature 2
```

### Example: INCORRECT Update

**DO NOT DO THIS:**
```markdown
# Project Name

**Completely rewritten description** (This is WRONG!)

## Modified Features (This is WRONG!)
- Modified feature 1
```

## Documentation Location Rules

### Where to Create Documentation

1. **API Documentation**: `/docs/api/` directory
   - One file per module
   - Format: `{module-name}.md`

2. **Guides**: `/docs/guides/` directory
   - Getting started guides
   - Usage guides
   - Best practices

3. **Architecture**: `/docs/ARCHITECTURE.md`
   - Overall architecture documentation
   - Design decisions
   - Module relationships

4. **Changelog**: `/docs/CHANGELOG.md`
   - Version history
   - Breaking changes
   - New features

5. **Release Notes**: `/docs/releases/` directory
   - Per-version release notes
   - `{version}.md` format

6. **Migration Guides**: `/docs/releases/migration-guide.md`
   - Migration instructions between versions

### What NOT to Create

Do NOT create documentation files in:
- Project root (except README.md)
- Module directories
- Source directories

## Style Guidelines

### Markdown Format

- Use Markdown for all documentation
- Follow existing formatting conventions
- Use proper heading levels (# ## ###)
- Include code examples with syntax highlighting
- Add tables for structured data

### Language

- Use Chinese for user-facing content (matching existing README)
- Use English for technical content (API docs, architecture)
- Maintain consistency within each document

### Code Examples

- Provide complete, runnable examples
- Use Kotlin for Android examples
- Include necessary imports
- Add explanatory comments

## Review Process

Before finalizing documentation changes:

1. Verify README.md original content is preserved
2. Check all links are valid
3. Ensure code examples are accurate
4. Confirm documentation is in correct location
5. Test any commands or code provided

## Configuration

These rules are configured in `.claude/README.md` and automatically loaded by Claude Code when working on this project.

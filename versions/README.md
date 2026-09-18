# Versions

This directory contains version-specific documentation for the SmokingTracker Android application.

Each application version has its own directory containing the documentation associated with that release.

## Structure

```text
versions/
|__ vX.Y.Z/
    |__ notes.md
    |__ changelog.md
|__ vX.Y.Z/
    |__ notes.md
    |__ changelog.md
|__ vX.Y.Z/
    |__ notes.md
    |__ changelog.md
```

Each version directory follows the `vX.Y.Z` format and contains the following files:

* `notes.md` - detailed release notes for the GitHub release
* `changelog.md` - user-facing changelog for the application

## `notes.md`

The `notes.md` file contains the release notes used for the corresponding GitHub release.

It can include detailed information about:

- New features
- Improvements
- Bug fixes
- Technical changes
- Internal changes
- Testing and stability improvements
- Important implementation details
- Etc.

The release workflow uses this file when creating the GitHub release.

## `changelog.md`

The `changelog.md` file contains the user-facing changes introduced in the corresponding application version.

It should focus on changes that are relevant to users rather than internal implementation details.

It can include:

- New features
- Improved functionality
- Bug fixes
- Performance improvements
- Localization and translation updates
- UI and UX improvements

The changelog is intended to be displayed in the application as **What's New** information after an update.

## Difference Between `notes.md` and `changelog.md`

The two files describe the same version but have different purposes.

| File           | Purpose                        | Audience                    |
| -------------- | ------------------------------ | --------------------------- |
| `notes.md`     | Detailed release documentation | Developers and GitHub users |
| `changelog.md` | User-facing summary of changes | Application users           |

`notes.md` may contain technical and implementation details that are not appropriate for the in-app changelog.

`changelog.md` should remain concise and focused on changes that users can understand and benefit from.

## Guidelines

-  Create a new directory for every released version
-  Use the `vX.Y.Z` version format for directory names
-  Keep `notes.md` and `changelog.md` specific to their version
-  Keep GitHub release notes and the in-app changelog separate
-  Do not place unrelated documentation in a version directory
-  Update both files when preparing a new release
-  Keep the changelog focused on user-visible changes
- Include technical details primarily in `notes.md`
- Do not modify previous version documentation unless correcting an error
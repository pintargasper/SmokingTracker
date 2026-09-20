# Release

This directory contains version-specific documentation for the SmokingTracker Android application.

Each application version has its own release notes and user-facing changelog.

## Structure

```text
release/
|__ notes/
    |__ vX.Y.Z.md
|__ changelogs/
    |__ <language>
        |__ vX.Y.Z.txt
```

Each release uses the vX.Y.Z version format for both files.

## `notes.md`

The `notes/vX.Y.Z.md` file contains the release notes used for the corresponding GitHub release.

It can include detailed information about:

- New features
- Improvements
- Bug fixes
- Technical changes
- Internal changes
- Testing and stability improvements
- Important implementation details
- Etc.

The release workflow uses this file when creating the corresponding GitHub release

## `vX.Y.Z.txt`

The `changelogs/vX.Y.Z.txt` file contains the user-facing changes introduced in the corresponding application version.

It should focus on changes that are relevant to users rather than internal implementation details.

It can include:

- New features
- Improved functionality
- Bug fixes
- Performance improvements
- Localization and translation updates
- UI and UX improvements

The changelog is intended to be displayed in the application as **What's New** information after an update.

## Difference Between `notes.md` and `vX.Y.Z.txt`

The two files describe the same version but have different purposes.

| File         | Purpose                        | Audience                    |
|--------------|--------------------------------|-----------------------------|
| `notes.md`   | Detailed release documentation | Developers and GitHub users |
| `vX.Y.Z.txt` | User-facing summary of changes | Application users           |

`vX.Y.Z.md` may contain technical and implementation details that are not appropriate for the in-app changelog.

`vX.Y.Z.txt` should remain concise and focused on changes that users can understand and benefit from.

## Guidelines

- Create a new release notes file and changelog for every released version
- Use the `vX.Y.Z` version format for file names
- Keep `vX.Y.Z.md` and `vX.Y.Z.txt` specific to their version
- Keep GitHub release notes and the in-app changelog separate
- Do not place unrelated documentation in the release/ directory
- Update both files when preparing a new release
- Keep the changelog focused on user-visible changes
- Include technical details primarily in `vX.Y.Z.md`
- Do not modify previous release documentation unless correcting an error

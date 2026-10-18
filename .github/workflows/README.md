# Workflows

This directory contains GitHub Actions workflows used to automate building, testing, releasing, and maintaining the SmokingTracker Android application.

## `build.yml`

GitHub Actions workflow for building and testing the SmokingTracker Android application.

- Runs on pushes to any branch
- Runs on pull requests targeting any branch
- Runs automatically every Monday at 08:00 UTC
- Can be started manually using GitHub Actions
- Builds the Debug APK
- Runs local unit tests
- Generates dummy data for instrumented testing
- Tests the application on Android API levels 26 and 34
- Uses an Android Emulator with KVM hardware acceleration
- Installs the generated Debug APK on the emulator
- Imports generated dummy data into the application
- Runs instrumented Android tests

### Usage

The workflow runs automatically when changes are pushed or when a pull request is opened or updated, according to the configured triggers.

It can also be started manually from the GitHub Actions interface using **Run workflow**.

The workflow uses the following Android API levels for instrumented tests.

```text
API 26
API 34
```

### Guidelines

- Keep the build and test process compatible with the configured GitHub Actions environment
- Update the Android API level matrix when support requirements change
- Keep unit tests independent of the Android emulator whenever possible
- Use `dummy_data.py` to generate test data instead of creating it manually
- Ensure changes to test data generation remain compatible with instrumented tests

## `fastlane.yml`

GitHub Actions workflow for generating and updating Fastlane screenshots.

- Runs manually using GitHub Actions
- Generates dummy data for consistent screenshot content
- Builds the Debug APK
- Creates an Android Emulator configured for screenshot generation
- Configures the emulator to the expected screenshot resolution and density
- Imports generated dummy data into the application
- Generates screenshots using Fastlane Screengrab
- Crops screenshots using `image_cropper.py`
- Removes generated HTML files that are not required in the repository
- Creates a pull request containing the updated screenshots

### Usage

Start the workflow manually from the GitHub Actions interface using **Run workflow**.

The workflow generates screenshots using an Android Emulator with the following configuration

```text
API level: 34
Device profile: Pixel 6
Resolution: 1080x2400
Density: 400
```

Generated screenshots are processed for all available Fastlane languages.

The workflow creates a pull request containing the updated screenshots instead of committing the changes directly to `master`.

### Guidelines

- Use the workflow whenever updated application screenshots are required
- Keep screenshot generation deterministic by using generated dummy data
- Do not manually modify generated screenshots when they can be regenerated using the workflow
- Keep screenshot dimensions and density consistent with the configured emulator
- Review the generated pull request before merging screenshot updates

## `sync_bots.yml`

GitHub Actions workflow for automatically updating and approving pull requests created by supported GitHub bots

- Runs on pushes to the `master` branch
- Detects open pull requests created by Dependabot and GitHub Actions bots
- Updates bot pull requests with the latest changes from the base branch
- Automatically approves detected bot pull requests
- Continues processing if updating or approving an individual pull request fails

### Usage

The workflow runs automatically after changes are pushed to the `master` branch. No manual action is required.

The workflow currently handles pull requests created by

```text
dependabot
github-actions
github-actions[bot]
```

### Guidelines

- Review changes made by automated dependency and workflow updates when necessary
- Keep the list of supported bot accounts synchronized with the repository configuration
- Do not grant additional permissions unless they are required by the workflow
- Keep automated pull request handling limited to trusted bot accounts

## `release.yml`

Main GitHub Actions workflow for validating, building, and publishing a new application release. It orchestrates the reusable workflows `build.yml`, `fastlane.yml`, `prepare.yml`, and `publish.yml`.

- Runs manually using GitHub Actions
- Requires the release version as a workflow input
- Validates the version format (`vX.Y.Z`)
- Verifies that the release version matches the application's `versionName`
- Verifies that the current `versionCode` is greater than the previous release's `versionCode`
- Checks that the version-specific release notes and application changelog exist
- Runs the build and test workflow
- Runs the Fastlane screenshot workflow
- Merges the generated screenshot pull request using a GitHub App token
- Waits for the `review` environment's configured protection rules before executing the preparation job
- Determines the commit SHA to be released
- Creates a draft GitHub release using the version-specific release notes
- Runs the publishing workflow against the prepared release commit
- Builds the signed Release APK and AAB
- Verifies that the generated APK and AAB exist
- Uploads the signed APK to the draft GitHub release
- Validates the Google Play changelog for the current `versionCode`
- Prepares Fastlane metadata for Google Play
- Applies Google Play locale mappings and metadata fallbacks
- Processes and normalizes Fastlane screenshots
- Uploads the application to Google Play using Fastlane
- Publishes the GitHub release after a successful Google Play upload
- Removes signing credentials from the GitHub Actions runner after the publishing job completes

### Reusable workflows

The release process is divided into separate workflows:

| Workflow       | Responsibility                                                                                            |
|----------------|-----------------------------------------------------------------------------------------------------------|
| `build.yml`    | Builds the application and runs tests                                                                     |
| `fastlane.yml` | Generates screenshots and creates a screenshot pull request                                               |
| `prepare.yml`  | Merges the screenshot pull request, determines the release commit, and creates a draft GitHub release     |
| `publish.yml`  | Builds signed artifacts, uploads the APK and application to Google Play, and publishes the GitHub release |

### Usage

Start `release.yml` manually from the GitHub Actions interface using **Run workflow**.

The workflow requires the release version in the following format

```text
vX.Y.Z
```

For each release, the corresponding version directory must contain:

```text
release/
|__ notes/
    |__ vX.Y.Z.md
|__ changelogs/
        |__ <en-US>/
            |__vX.Y.Z.txt
```

- `release/notes/vX.Y.Z.md` contains the GitHub release notes
- `release/changelogs/en-US/vX.Y.Z.txt` contains the user-facing application changelog

The workflow also requires a Fastlane changelog matching the application's `versionCode`:

```text
fastlane/
|__ metadata/
    |__ android/
        |__ en-US/
            |__ changelogs/
                |__ <versionCode>.txt
```

The release process runs in the following order

```text
1. Validate release version and required files
2. Build and test the application
3. Generate screenshots and create a screenshot pull request
4. Execute the preparation job after the review environment's protection rules are satisfied
5. Merge the screenshot pull request
6. Determine the release commit and create a draft GitHub release
7. Build and verify the signed APK and AAB
8. Upload the APK to the draft GitHub release
9. Prepare Google Play metadata and upload the application
10. Publish the GitHub release
11. Remove signing credentials from the runner
```

### Guidelines

- Update `versionName` and `versionCode` before creating a release
- Use the `vX.Y.Z` format for the workflow release input
- Create `release/notes/vX.Y.Z.md` for every release
- Create the corresponding application changelog under `release/changelogs/<language>/`
- Keep the GitHub release notes and application changelog appropriate for their respective purposes
- Add the Fastlane changelog corresponding to the application's `versionCode`
- Do not reuse a previous version's release notes or changelog
- Review the generated screenshot pull request before allowing the preparation job to proceed
- Do not publish a release manually before the workflow has completed successfully
- Ensure the Google Play upload succeeds before publishing the GitHub release
- Keep workflow permissions limited to the operations each job performs
- Ensure signing credentials are removed from the runner after the publishing job completes
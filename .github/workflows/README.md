# Workflows

This directory contains GitHub Actions workflows used to automate building, testing, and maintaining the SmokingTracker Android application

## `build.yml`

GitHub Actions workflow for building and testing the SmokingTracker Android application

* Runs on pushes to the `master` branch
* Runs on pull requests targeting the `master` branch
* Runs automatically every Monday at 08:00 UTC
* Can be started manually using GitHub Actions
* Builds the Debug APK
* Runs local unit tests
* Generates dummy data for instrumented testing
* Tests the application on Android API levels 26 and 34
* Uses Android Emulator with KVM hardware acceleration
* Installs the generated Debug APK on the emulator
* Imports generated dummy data into the application
* Runs instrumented Android tests

### Usage

The workflow runs automatically when changes are pushed to `master` or when a pull request targets `master`.

It can also be started manually from the GitHub Actions interface using **Run workflow**.

The workflow uses the following Android API levels for instrumented tests

```text
API 26
API 34
```

### Guidelines

* Keep the build and test process compatible with the configured GitHub Actions environment
* Update the Android API level matrix when support requirements change
* Keep unit tests independent of the Android emulator whenever possible
* Use `dummy_data.py` for generating test data instead of manually creating test data
* Ensure changes to test data generation remain compatible with instrumented tests

## `fastlane.yml`

GitHub Actions workflow for automatically generating and updating Fastlane screenshots

* Runs manually using GitHub Actions
* Generates dummy data for consistent screenshot content
* Builds the Debug APK
* Creates an Android Emulator configured for screenshot generation
* Configures the emulator to the expected screenshot resolution and density
* Imports generated dummy data into the application
* Generates screenshots using Fastlane Screengrab
* Crops screenshots using `image_cropper.py`
* Removes generated HTML files that are not required in the repository
* Creates a pull request containing the updated Fastlane screenshots

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

* Use the workflow whenever updated application screenshots are required
* Keep screenshot generation deterministic by using the generated dummy data
* Do not manually modify generated screenshots when they can be regenerated using the workflow
* Keep screenshot dimensions and density consistent with the configured emulator
* Review the generated pull request before merging screenshot updates

## `sync_bots.yml`

GitHub Actions workflow for automatically updating and approving pull requests created by supported GitHub bots

* Runs on pushes to the `master` branch
* Detects open pull requests created by Dependabot and GitHub Actions bots
* Updates bot pull requests with the latest changes from the base branch
* Automatically approves detected bot pull requests
* Continues processing if updating or approving an individual pull request fails

### Usage

The workflow runs automatically after changes are pushed to the `master` branch.

No manual action is required.

The workflow currently handles pull requests created by:

```text
dependabot
github-actions
github-actions[bot]
```

### Guidelines

* Review changes made by automated dependency and workflow updates when necessary
* Keep the list of supported bot accounts synchronized with the repository configuration
* Do not grant additional permissions unless they are required by the workflow
* Keep automated pull request handling limited to trusted bot accounts

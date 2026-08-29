# Scripts

This directory contains scripts used during the development and maintenance of the SmokingTracker Android application

## `image_cropper.py`

Python script for cropping and preparing Fastlane screenshots

- Finds screenshot images in the Fastlane Android metadata directory
- Supports JPEG, PNG, and WebP images
- Validates the expected screenshot dimensions
- Crops the top and bottom of screenshots
- Preserves image format when saving
- Processes screenshots for all available languages
- Prints processing statistics and errors

### Usage

Run the script from the project root

```bash
python tools/scripts/image_cropper.py
```

The script expects screenshots to be stored in the Fastlane Android metadata directory

```text
fastlane/
|__ metadata/
    |__ android/
        |__ <language>/
            |__ images/
                |__ phoneScreenshots/
```

### Guidelines

- Keep screenshots in the appropriate Fastlane language directories
- Do not manually modify screenshots when they can be processed by the script
- Update EXPECTED_SIZE, TARGET_SIZE, and CROP together when changing screenshot dimensions

## `dummy_data.py`

Python script for generating dummy data for development and testing of the SmokingTracker Android application

- Generates smoking history for the configured number of days
- Generates smoke-free days and random smoking times
- Generates borrowed cigarette entries
- Calculates achievement progress based on generated history
- Generates cigarette price history
- Generates sample notes with different moods
- Generates application settings
- Generates notification settings
- Exports all generated data to an Excel workbook

### Usage

Run the script from the project root

```bash
python tools/scripts/dummy_data.py
```

The generated Excel file is saved to

```text
tools/
|__ scripts/
    |__ dummy_data/
        |__ dummy_data.xlsx
```

### Guidelines

- Use the generated Excel file only for development and testing
- Adjust the configuration constants in the script when different test data is needed
- Keep the generated data representative of realistic application usage
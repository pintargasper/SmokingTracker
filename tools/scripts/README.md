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
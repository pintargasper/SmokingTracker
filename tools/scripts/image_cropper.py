from dataclasses import dataclass
from PIL import Image
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[2]
FASTLANE_ANDROID_FOLDER = (PROJECT_ROOT / "fastlane" / "metadata" / "android")
SUPPORTED_FORMATS = {".jpg", ".jpeg", ".png", ".webp"}

EXPECTED_SIZE = (1080, 2400)
TARGET_SIZE = (1080, 2250)
CROP = (100, 50)


@dataclass
class Statistics:
    total: int = 0
    processed: int = 0
    skipped: int = 0
    failed: int = 0

    def summary(self) -> None:
        print("\n".join((
            "",
            "=" * 60,
            "SUMMARY",
            "=" * 60,
            f"Total images:       {self.total}",
            f"Processed images:   {self.processed}",
            f"Skipped images:     {self.skipped}",
            f"Failed images:      {self.failed}",
            "",
            f"Target size:        {TARGET_SIZE[0]} x {TARGET_SIZE[1]}",
            f"Fastlane folder:    {FASTLANE_ANDROID_FOLDER}",
            "=" * 60
        )))


def validate_configuration() -> bool:
    if CROP[0] + CROP[1] >= EXPECTED_SIZE[1]:
        print("ERROR: Crop values are invalid")
        return False

    expected_size = (EXPECTED_SIZE[0], EXPECTED_SIZE[1] - sum(CROP))

    if expected_size != TARGET_SIZE:
        print("\n".join((
            "ERROR: TARGET_SIZE does not match the crop settings",
            f"  Calculated size: {expected_size[0]} x {expected_size[1]}",
            f"  TARGET_SIZE:     {TARGET_SIZE[0]} x {TARGET_SIZE[1]}"
        )))
        return False
    return True


def find_language_folders() -> list[Path]:
    return sorted(
        folder
        for folder in FASTLANE_ANDROID_FOLDER.iterdir()
        if folder.is_dir()
    )


def find_screenshots(folder: Path) -> list[Path]:
    return sorted(
        file
        for file in folder.iterdir()
        if (file.is_file() and file.suffix.lower() in SUPPORTED_FORMATS)
    )


def save_image(image: Image.Image, image_path: Path, image_format: str | None) -> None:
    save_options = {
        "JPEG": {"format": "JPEG", "quality": 100, "subsampling": 0},
        "WEBP": {"format": "WEBP", "lossless": True},
        "PNG": {"format": "PNG"}
    }

    try:
        options = save_options[image_format]
    except KeyError:
        raise ValueError(f"Unsupported image format: {image_format}")

    image.save(image_path, **options)


def crop_image(image_path: Path) -> str:
    try:
        with Image.open(image_path) as image:
            width, height = image.size

            if image.size != EXPECTED_SIZE:
                return "skipped"

            cropped = image.crop((0, CROP[0], width, height - CROP[1]))

            if cropped.size != TARGET_SIZE:
                return "failed"

            save_image(cropped, image_path, image.format)
            return "processed"

    except (OSError, ValueError):
        return "failed"


def process_language(language_folder: Path, statistics: Statistics) -> None:
    screenshots_folder = language_folder / "images" / "phoneScreenshots"

    if not screenshots_folder.exists():
        return

    images = find_screenshots(screenshots_folder)

    print("\n".join((
        "-" * 60,
        f"  Language: {language_folder.name}",
        f"  Folder:  {screenshots_folder}",
        f"  Images found: {len(images)}"
    )))

    statistics.total += len(images)

    counters = {
        "processed": "processed",
        "skipped": "skipped",
        "failed": "failed"
    }

    for image_path in images:
        result = crop_image(image_path)

        if result in counters:
            attribute = counters[result]
            setattr(statistics, attribute, getattr(statistics, attribute) + 1)


def main() -> None:
    print("\n".join((
        "=" * 60,
        "IMAGE CROPPER",
        "=" * 60,
        ""
    )))

    if not validate_configuration():
        return

    if not FASTLANE_ANDROID_FOLDER.exists():
        print("\n".join((
            "ERROR: Fastlane folder does not exist:",
            f"  {FASTLANE_ANDROID_FOLDER}"
        )))
        return

    language_folders = find_language_folders()

    print("\n".join((
        f"Project:  {PROJECT_ROOT}",
        f"Fastlane: {FASTLANE_ANDROID_FOLDER}",
        f"Languages found: {len(language_folders)}"
    )))

    statistics = Statistics()

    for language_folder in language_folders:
        process_language(language_folder, statistics)

    statistics.summary()


if __name__ == "__main__":
    main()

from dataclasses import dataclass
from datetime import datetime, timedelta
from pathlib import Path
from random import randint, choice, choices, gauss
from json import load
from openpyxl import Workbook
from openpyxl.utils import get_column_letter

PROJECT_ROOT = Path(__file__).resolve().parents[2]
ROOT = Path(__file__).resolve().parent

ACHIEVEMENTS_FILE = (PROJECT_ROOT / "app" / "src" / "main" / "res" / "raw" / "achievements.json")

OUTPUT_FOLDER = ROOT / "dummy_data"
OUTPUT_FOLDER.mkdir(exist_ok=True)
OUTPUT_FILE = OUTPUT_FOLDER / "dummy_data.xlsx"

DAYS = 360
AVERAGE_CIGARETTES_PER_DAY = (8, 25)
SMOKE_FREE_PROBABILITY = 2
LENT_PROBABILITY = 2
MONTHLY_MIN_GAP = 4
DAILY_VARIATION = 2.5
SECONDS_IN_DAY = 60 * 60 * 24
DATE_FORMAT = "%Y-%m-%d %H:%M:%S"


@dataclass
class Statistics:
    history: int = 0
    achievements: int = 0
    costs: int = 0
    notes: int = 0

    def summary(self) -> None:
        print("\n".join((
            "",
            "=" * 60,
            "SUMMARY",
            "=" * 60,
            f"History records:    {self.history}",
            f"Achievements:       {self.achievements}",
            f"Cost records:       {self.costs}",
            f"Notes:              {self.notes}",
            "",
            f"Output file:        {OUTPUT_FILE}",
            "=" * 60,
            "Dummy data generation completed successfully.",
            "=" * 60
        )))


def generate_monthly_averages(start_date: datetime, end_date: datetime) -> dict[tuple[int, int], int]:
    minimum, maximum = AVERAGE_CIGARETTES_PER_DAY

    averages: dict[tuple[int, int], int] = {}
    previous_average: int | None = None

    current = start_date.replace(day=1)
    while current <= end_date:
        candidates = list(range(minimum, maximum + 1))

        if previous_average is not None:
            candidates = [
                value
                for value in candidates
                if abs(value - previous_average) >= MONTHLY_MIN_GAP
            ]

        average = choice(candidates)
        averages[(current.year, current.month)] = average
        previous_average = average

        if current.month == 12:
            current = current.replace(year=current.year + 1, month=1)
        else:
            current = current.replace(month=current.month + 1)
    return averages


def generate_cigarette_count(monthly_average: int) -> int:
    minimum, maximum = AVERAGE_CIGARETTES_PER_DAY
    count = round(gauss(monthly_average, DAILY_VARIATION))
    return max(minimum, min(maximum, count))


def generate_history_day(date: datetime, monthly_average: int, end_time: datetime | None = None) -> list[dict]:
    start_time = date.replace(hour=7, minute=randint(0, 30), second=randint(0, 59), microsecond=0)
    end_time = end_time or date.replace(hour=23, minute=59, second=59, microsecond=0)

    if randint(1, 100) <= SMOKE_FREE_PROBABILITY:
        return []

    cigarettes = generate_cigarette_count(monthly_average=monthly_average)

    history = []
    current_time = start_time

    interval_ranges = [(10, 25), (25, 45), (45, 70), (70, 110), (110, 180)]
    interval_weights = [35, 30, 20, 10, 5]

    while len(history) < cigarettes and current_time <= end_time:
        history.append({
            "Lent": int(randint(1, 100) <= LENT_PROBABILITY),
            "CreatedAt": current_time.strftime(DATE_FORMAT),
        })

        remaining = cigarettes - len(history)
        if remaining == 0:
            break

        remaining_minutes = int((end_time - current_time).total_seconds() / 60)
        if remaining_minutes <= 0:
            break

        minimum_interval = 10
        maximum_interval = max(minimum_interval, remaining_minutes // remaining)

        interval_min, interval_max = choices(interval_ranges, weights=interval_weights, k=1)[0]
        interval_min = min(interval_min, maximum_interval)
        interval_max = min(interval_max, maximum_interval)

        if interval_min > interval_max:
            interval_min = interval_max

        interval = randint(interval_min, interval_max)
        current_time += timedelta(minutes=interval, seconds=randint(0, 59))
    return history


def get_duration(value: int, unit: str) -> timedelta:
    return {
        "DAYS": timedelta(days=value),
        "WEEKS": timedelta(weeks=value),
        "MONTHS": timedelta(days=value * 30),
        "YEARS": timedelta(days=value * 365)
    }[unit]


def update_last_achieved(achievement: dict, achieved_at: datetime) -> None:
    last_achieved = achievement["LastAchieved"]
    if last_achieved is None or achieved_at > datetime.strptime(last_achieved, DATE_FORMAT):
        achievement["LastAchieved"] = achieved_at.strftime(DATE_FORMAT)


def get_last_completed_sunday(date: datetime) -> datetime:
    days_since_sunday = (date.weekday() + 1) % 7
    if days_since_sunday == 0:
        days_since_sunday = 7
    return date - timedelta(days=days_since_sunday)


def get_dataset_end_time() -> datetime:
    now = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
    return get_last_completed_sunday(now).replace(hour=19, minute=30)


def generate_history() -> list[dict]:
    history = []

    end_date = get_dataset_end_time()
    start_date = end_date.replace(hour=0) - timedelta(days=DAYS - 1)
    monthly_averages = generate_monthly_averages(start_date=start_date, end_date=end_date)

    for day in range(DAYS):
        date = start_date + timedelta(days=day)
        monthly_average = monthly_averages[(date.year, date.month)]
        end_time = end_date if date.date() == end_date.date() else None
        history.extend(generate_history_day(date=date, monthly_average=monthly_average, end_time=end_time))
    return history


def generate_achievements(history: list[dict]) -> list[dict]:
    with ACHIEVEMENTS_FILE.open("r", encoding="utf-8") as file:
        data = load(file)

    achievements = [
        {
            "Value": item["value"],
            "Times": 0,
            "LastAchieved": None,
            "Reset": True,
            "Notify": True,
            "Category": category,
            "Unit": item.get("unit", "CIGARETTES"),
            "Id": index,
        }
        for index, (category, item) in enumerate(((category, item) for category, items in data.items() for item in items), start=1)
    ]
    timestamps = sorted(datetime.strptime(entry["CreatedAt"], DATE_FORMAT) for entry in history)

    if not timestamps:
        return achievements

    end_time = get_dataset_end_time()
    average_cigarettes_per_day = len(timestamps) / max((end_time - timestamps[0]).total_seconds() / SECONDS_IN_DAY, 1)

    periods = list(zip(timestamps, timestamps[1:]))
    periods.append((timestamps[-1], end_time))

    for achievement in achievements:
        threshold = achievement["Value"]

        match achievement["Category"]:
            case "SMOKE_FREE_TIME":
                duration = get_duration(threshold, achievement["Unit"])
                for start, end in periods:
                    if end - start >= duration:
                        achievement["Times"] += 1
                        update_last_achieved(achievement, start + duration)
                achievement["Reset"] = (end_time - timestamps[-1] < duration)

            case "CIGARETTES_AVOIDED":
                for start, end in periods:
                    smoke_free_days = (end - start).total_seconds() / SECONDS_IN_DAY
                    cigarettes_avoided = (smoke_free_days * average_cigarettes_per_day)

                    if cigarettes_avoided >= threshold:
                        achievement["Times"] += 1
                        update_last_achieved(achievement, start + timedelta(days=threshold / average_cigarettes_per_day))

                current_cigarettes_avoided = ((end_time - timestamps[-1]).total_seconds() / SECONDS_IN_DAY * average_cigarettes_per_day)
                achievement["Reset"] = (current_cigarettes_avoided < threshold)
    return achievements


def generate_costs() -> list[dict]:
    end_date = get_dataset_end_time()
    start_date = end_date.replace(hour=0) - timedelta(days=DAYS - 1)

    prices = [0.18, 0.20, 0.21, 0.22, 0.23, 0.24, 0.26, 0.28, 0.30]

    costs = []
    current_start = start_date

    while current_start < end_date:
        remaining_seconds = int((end_date - current_start).total_seconds())

        if remaining_seconds <= 30 * SECONDS_IN_DAY:
            current_end = end_date
        else:
            interval_seconds = randint(30 * SECONDS_IN_DAY, min(120 * SECONDS_IN_DAY, remaining_seconds))
            current_end = current_start + timedelta(seconds=interval_seconds)

        costs.append({
            "Price": choice(prices),
            "StartDate": current_start.strftime(DATE_FORMAT),
            "EndDate": current_end.strftime(DATE_FORMAT),
        })
        current_start = current_end + timedelta(seconds=1)
    return costs


def generate_notes() -> list[dict]:
    templates = [
        {
            "Title": "Daily smoking habits",
            "Content": (
                "Today was a fairly regular day. I noticed that I tend to smoke "
                "more during the afternoon, especially when I am working or feeling stressed."
            ),
            "Mood": 3,
        },
        {
            "Title": "A better day",
            "Content": (
                "I smoked less than usual today. The longest break was in the afternoon, "
                "and it was easier than expected to avoid smoking."
            ),
            "Mood": 4,
        },
        {
            "Title": "Weekend",
            "Content": (
                "Smoking was more frequent today because I spent more time with friends. "
                "Most cigarettes were smoked in the evening."
            ),
            "Mood": 5,
        },
        {
            "Title": "Trying to reduce",
            "Content": (
                "I am trying to gradually reduce my daily consumption. "
                "The morning was easy, but I had more cravings later in the day."
            ),
            "Mood": 2,
        },
        {
            "Title": "Good progress",
            "Content": (
                "Today went well. I managed to keep longer intervals between cigarettes "
                "and felt that I had more control over my smoking."
            ),
            "Mood": 4,
        },
    ]

    notes = []
    end_time = get_dataset_end_time()
    for template in templates:
        created_at = end_time - timedelta(
            days=randint(0, DAYS - 1),
            hours=randint(0, 23),
            minutes=randint(0, 59),
            seconds=randint(0, 59),
        )

        updated_at = min(created_at + timedelta(minutes=randint(1, 60)), end_time)
        notes.append({
            **template,
            "CreatedAt": created_at.strftime(DATE_FORMAT),
            "UpdatedAt": updated_at.strftime(DATE_FORMAT),
        })
    return sorted(notes, key=lambda item: item["CreatedAt"])


def generate_settings() -> dict:
    currencies = ["€", "$", "£"]
    custom_currencies = ["CHF", "kr", "Kč", "zł", "¥"]

    return {
        "Theme": 1,
        "Language": "en",
        "Frequency": randint(0, 2),
        "Currency": choice(currencies),
        "CustomCurrency": choice(custom_currencies) if randint(0, 2) == 0 else "",
    }


def generate_notifications() -> dict:
    values = ["TRUE", "FALSE"]
    return {
        "System": choice(values),
        "Achievements": choice(values),
        "Progress": choice(values),
    }


def export_to_excel(data: dict[str, list[dict]]) -> None:
    workbook = Workbook()
    workbook.remove(workbook.active)

    for sheet_name, rows in data.items():
        worksheet = workbook.create_sheet(sheet_name)

        if not rows:
            continue

        headers = list(rows[0].keys())
        worksheet.append(headers)

        for row in rows:
            worksheet.append([row.get(header) for header in headers])

        worksheet.freeze_panes = "A2"
        worksheet.auto_filter.ref = worksheet.dimensions

        for column_index, header in enumerate(headers, start=1):
            column_letter = get_column_letter(column_index)

            max_length = max(
                len(str(worksheet.cell(row=row, column=column_index).value or ""))
                for row in range(1, worksheet.max_row + 1)
            )
            worksheet.column_dimensions[column_letter].width = min(max_length + 2, 50)
    workbook.save(OUTPUT_FILE)


def main() -> None:
    print("\n".join((
        "=" * 60,
        "DUMMY DATA GENERATOR",
        "=" * 60,
        "",
        f"Project:  {PROJECT_ROOT}",
        f"Output:   {OUTPUT_FILE}"
    )))

    history = generate_history()
    achievements = generate_achievements(history=history)
    costs = generate_costs()
    notes = generate_notes()
    settings = generate_settings()
    notifications = generate_notifications()

    export_to_excel({
        "History": history,
        "Achievements": achievements,
        "Costs": costs,
        "Notes": notes,
        "Settings": [settings],
        "Notifications": [notifications],
    })

    statistics = Statistics(history=len(history), achievements=len(achievements), costs=len(costs), notes=len(notes))
    statistics.summary()


if __name__ == "__main__":
    main()

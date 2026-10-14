# dailyz: Daily Work Time Tracker & Smart Notes

dailyz is an intentional daily work companion featuring a persistent notification workday timer, a 3-hour daily goal with layered overtime progress visualization, permanent automatic purging of history older than 3 months, configurable workday reminders with one-tap start actions, weekly productivity charts, and a smart notes tab with automatic CSV number totaling and embedded to-do checklists.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following revisions have been incorporated based on your latest instructions:

- **App Name**: Renamed to **dailyz** (synced across platform metadata, launcher icon, and strings).
- **3-Hour Daily Target & Stacked Overtime Progress**:
  - The default daily work target is set to **3 hours** (180 minutes).
  - Progress up to 3 hours fills the primary target bar/ring (0% to 100%).
  - **Over-work Indicator**: When work exceeds 3 hours, the completed base bar remains filled (celebrating target completion) while a distinct secondary progress layer (vibrant amber/electric-cyan overtime bar) starts progressing directly over/alongside the completed bar, clearly communicating bonus/overtime hours (e.g., `3h 45m / 3h target • +45m overtime`).
- **Permanent 3-Month Local Data Deletion**:
  - Any work history older than 90 calendar days is permanently purged from the local Room database (`DELETE FROM work_sessions WHERE endTime < :cutoffTimestamp`).
  - Pruning runs automatically on app launch, after every stopped session, and can also be triggered manually in settings.
- **Smart Work Reminder**: Push notification at the user-scheduled workday start time with a quick-action "Start Work" button right in the notification tray.
- **Smart Notes & CSV Auto-Sum**: Note titles, optional to-do list items with strike-through completion, and comma/newline separated number calculations yielding live Sum, Average, and Count.

---

### 1. Overview & Core Concept

- **Core Utility**: Keep daily work hours on track without distractions. Start your day with a tap or from a reminder notification, monitor progress via a persistent ongoing notification, and automatically log sessions.
- **Visual Feedback for Effort**: The 3-hour goal provides an achievable daily benchmark. Reaching 3 hours triggers a goal-reached badge, while any extra work is tracked via an overtime overlay progress bar.
- **Strict Privacy & Clean Footprint**: 100% on-device Room database with zero cloud sync required and automatic permanent pruning of records older than 3 months.
- **Work Notes & Calculations**: Keep project scratchpads with integrated itemized costs, billable hours, or counts summed automatically.

---

### 2. User Experience & Visual Design

#### Screen 1: Work Tracker (`dailyz Timer & Trends`)
- **Top Bar**: App branding **dailyz** with current date, theme mode toggle (System/Light/Dark), and reminder status pill.
- **Hero Daily Progress Ring / Dual-Bar**:
  - Circular or stadium dual-track progress:
    - Inner/Base track: Fills up to 3h (in vibrant Cerulean / Emerald).
    - Overtime overlay track: Once past 3h, an energetic secondary color fills over the base track, displaying "+Xh Ym overtime".
  - Center readout: Live timer formatted as `HH:MM:SS`, current status (`Working`, `Paused`, `Target Reached!`, `Idle`), and session count today.
- **Timer Action Controls**:
  - Large tactile **Start / Pause / Resume / Stop** buttons with ripple feedback and distinct tonal color accents.
- **Daily Reminder Setup Card**:
  - Configurable reminder toggle, time picker (e.g., 09:00 AM), and status indicator.
- **Weekly Productivity Bar Chart**:
  - 7-day bar chart (Monday to Sunday) showing actual logged hours per day.
  - A subtle 3-hour reference line showing which days met or exceeded the daily goal.
  - Tapping any bar reveals detailed session breakdowns for that day.
- **Session History List**:
  - Grouped by day, displaying start/end times and total duration.
  - Notice badge: *"History older than 3 months is automatically removed permanently."*

#### Screen 2: Smart Notes (`Notes, CSV & To-Dos`)
- **Note List / Grid**: Shows title, snippet, to-do progress indicator (e.g., `✓ 3/4 tasks`), and CSV total badge (e.g., `Σ 1,240.50`).
- **Floating Action Button**: "+ New Note".
- **Note Editor Modal / Screen**:
  - **Title Field**: Crisp, prominent header input.
  - **Body Text**: Freeform thoughts, descriptions, meeting minutes.
  - **To-Do Section (Optional)**:
    - Checkbox list with task reordering, quick add field, and tap-to-complete strike-through animation.
  - **CSV Numbers Input**:
    - Dedicated clean numeric input box accepting commas, spaces, or newlines (e.g. `45, 120.5, 30, 85`).
  - **Sticky Bottom Calculation Bar**:
    - Live pills: **Total Sum: 280.50** | **Average: 70.13** | **Count: 4**

---

### 3. Key Technical Decisions & Architecture

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│  ┌──────────────────────────┐┌───────────────────────┐ │
│  │    DailyTrackerScreen    ││    SmartNotesScreen   │ │
│  │ (Timer, 3h Overtime Ring,││ (Notes, ToDos, CSV    │ │
│  │   Weekly Trends Chart)   ││   Auto-Sum Engine)    │ │
│  └─────────────▲────────────┘└───────────▲───────────┘ │
│                │                         │             │
│  ┌─────────────┴─────────────────────────┴───────────┐ │
│  │                   ViewModels                      │ │
│  │    DailyzTimerViewModel │      NotesViewModel     │ │
│  └─────────────▲─────────────────────────▲───────────┘ │
└────────────────┼─────────────────────────┼─────────────┘
                 │                         │
┌────────────────┼─────────────────────────┼─────────────┐
│  Background & System Services            │             │
│  ┌───────────────────────┐ ┌─────────────┴───────────┐ │
│  │   WorkTimerService    │ │   WorkReminderReceiver  │ │
│  │ (Foreground Service + │ │ (Scheduled AlarmManager │ │
│  │ Ongoing Notification) │ │ + "Start Work" Action)  │ │
│  └───────────────────────┘ └─────────────────────────┘ │
└────────────────┬───────────────────────────────────────┘
                 │
┌────────────────┴───────────────────────────────────────┐
│              Data Layer (Room + Prefs)                 │
│  ┌───────────────────────────────────────────────────┐ │
│  │             DailyzDatabase (SQLite)               │ │
│  │  - WorkSessionDao (Automated 90-day hard pruning) │ │
│  │  - NoteDao & NoteTodoDao                          │ │
│  │  - AppPreferences (Reminder time, target: 3h)     │ │
│  └───────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

#### Database Architecture & Automatic 3-Month Pruning
- **`WorkSession` Table**:
  - `id`: Long Primary Key
  - `startTime`: Long
  - `endTime`: Long
  - `durationMillis`: Long
  - `dateKey`: String (`YYYY-MM-DD`)
- **Permanent Pruning Mechanism**:
  ```kotlin
  @Query("DELETE FROM work_sessions WHERE endTime < :cutoffEpochMillis")
  suspend fun deleteSessionsOlderThan(cutoffEpochMillis: Long): Int
  ```
  Where `cutoffEpochMillis = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000L)`. Executed on startup and immediately upon saving any completed work session.

#### 3-Hour Target & Overtime Math
- `targetMillis = 3 * 3600 * 1000L`
- `baseProgress = (todayTotalMillis.toFloat() / targetMillis).coerceIn(0f, 1f)`
- `overtimeMillis = (todayTotalMillis - targetMillis).coerceAtLeast(0L)`
- `overtimeProgress = if (todayTotalMillis > targetMillis) ((overtimeMillis.toFloat() / targetMillis) % 1f) else 0f`
- Visualized using stacked Canvas arcs / progress bars where the base ring turns to completion green (`#10B981`) and the overtime ring draws in radiant amber (`#F59E0B`) or cyan over the top.

#### Persistent Notification & Reminder Service
- `WorkTimerService`: Android Foreground Service with channel `dailyz_active_work` updating elapsed time, running chronometer, and action buttons (`Pause`/`Stop`).
- `WorkReminderReceiver`: Alarm-driven receiver configured via `AlarmManager`. At the scheduled start time, checks if `WorkTimerService` is currently active. If not, posts a high-priority heads-up notification with action button `"Start Work"` (`PendingIntent` directly starting the service).

---

### Verification & Testing Plan
- **Verification of 3-hour Target & Overtime Progress**:
  - Tested with 0m, 1h 30m (50% base), 3h (100% base), and 4h 15m (100% base + 1h 15m overtime layer).
- **Verification of 3-Month Retention**:
  - Verify DAO unit test injecting dummy sessions at 30 days, 89 days, 91 days, and 120 days ago, verifying permanent deletion of only the >90 days records.
- **CSV Math Parsing**:
  - Unit tests verifying mixed separators (`10, 20.5\n30, 4.5`), empty lines, trailing commas, and negative numbers.
- **Build Verification**:
  - Clean compilation via `compile_applet`.

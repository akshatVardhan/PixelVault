# PixelVault — Progress

## Stack
- **Android**: Kotlin, Jetpack Compose, Room, Retrofit, WorkManager, Coil, Hilt, Navigation Compose, DataStore
- **Backend**: Python, FastAPI, Uvicorn, aiosqlite, Pydantic, PyTorch, Pillow, python-multipart, python-dotenv
- **ML**: YOLOv8 (scene), InsightFace/ArcFace (faces), CLIP (semantic search), EfficientNet (food)
- **GPU**: RTX 3070 8GB, CUDA
- **DB**: SQLite (metadata), Room (local cache)
- **Transport**: HTTPS over LAN, token auth, incremental sync w/ hash dedup

## Project Structure
```
pixelvault/
├── android/
│   ├── app/
│   │   ├── ui/          (gallery, detail, search, people, sync)
│   │   ├── sync/        (SyncWorker, SyncScheduler, SyncStatusRepo)
│   │   ├── data/         (remote/Retrofit, local/Room + DAOs)
│   │   ├── di/           (Hilt modules: NetworkModule, etc.)
│   │   └── search/       (CLIP query UI)
├── backend/
│   ├── api/              (FastAPI routes: sync, faces, search, notifications)
│   ├── ml/               (base.py, loader.py, queue.py, processor.py)
│   │   ├── face/         (detector.py, cluster.py)
│   │   ├── scene/        (yolo.py)
│   │   ├── clip/         (encoder.py)
│   │   └── food/         (classifier.py)
│   ├── sync/             (sync engine, dedup)
│   ├── db/               (schema.py, connection.py)
│   └── dashboard/        (HTML dashboard via Jinja2)
├── README.md
├── claude.md
├── prompt.md
└── progress.md           (← this file)
```

## Prompts Status
- [x] **1** — Backend scaffold: `backend/` dirs, `requirements.txt`, `main.py` (health check), `config.py`, `.env.example`, `db/schema.py` (photos, tags, faces tables), `db/connection.py`
- [x] **2** — Auth middleware + sync endpoints: `api/auth.py` (Bearer token), `POST /sync/upload`, `GET /sync/status`, `/api/v1` prefix
- [x] **3** — ML pipeline: `ml/base.py` (abstract MLModel), `ml/loader.py` (model registry), `ml/queue.py` (async job queue), `ml/scene/yolo.py` (YOLOv8 wrapper), `ml/food/classifier.py` (EfficientNet stub), `ml/processor.py` (orchestrator)
- [x] **4** — Face recognition: `ml/face/detector.py` (InsightFace ArcFace), `ml/face/cluster.py` (cosine sim clustering, threshold 0.6), `api/routes/faces.py` (GET clusters, GET cluster photos, PUT name + POST cluster)
- [x] **5** — CLIP search: `ml/clip/encoder.py` (openai/clip-vit-base-patch32), `db/schema.py` +clip_embedding BLOB, `api/routes/search.py` (GET /search?q=, GET /search/tags)
- [x] **6** — Android scaffold: Kotlin minSdk 26, Hilt DI, Retrofit (base URL from DataStore), Room (PhotoEntity, TagEntity, PhotoDao, TagDao), WorkManager (SyncWorker, SyncScheduler, SyncStatusRepo), Navigation Compose (Screen, NavGraph), SettingsDataStore
- [x] **7** — Android sync: SyncWorker (MediaStore → hash → upload), SyncScheduler (12h periodic, WiFi req), SyncStatusRepo, SyncStatusBar, READ_MEDIA_IMAGES perm
- [x] **8** — Gallery UI: GalleryScreen (date-grouped grid), GalleryViewModel, PhotoDetailScreen (tags chips, faces), PeopleScreen (cluster grid), PersonPhotosScreen, Navigation graph
- [x] **9** — Search UI: SearchScreen (debounced 500ms, shimmer), SearchApiService, SearchViewModel, bottom nav search icon
- [x] **10** — Dashboard + notifications: backend/dashboard/ (Jinja2 HTML: stats, chart, logs), GET /notifications/on-this-day, Android NotificationWorker (daily)
- [x] **11** — Foundation: TFLite/ML Kit deps, Room schema v2 (FaceEntity, ClusterEntity, new PhotoEntity columns), DB migration, SettingsDataStore additions, aaptOptions for .tflite
- [ ] **12** — On-device ML pipeline: ModelLoader, DelegateSelector, SceneDetector (YOLOv8n), FoodClassifier (EfficientNet-Lite0), FaceDetector (ML Kit), FaceEmbedder (MobileFaceNet), MLPipelineService orchestrator, bundle pre-converted TFLite models in assets

## Key Decisions
- ML runs on PC (RTX 3070), not on-device
- Sync over local Wi-Fi every 12h (configurable)
- HTTPS + token auth on LAN
- Incremental sync w/ hash-based dedup
- Face cluster threshold: cosine sim 0.6
- CLIP model: openai/clip-vit-base-patch32
- YOLO model: yolov8n.pt (nano)

## Code Style
- **Python**: type hints, docstrings on public fns, FastAPI DI, Pydantic models, aiosqlite
- **Kotlin**: coroutines, ViewModel + StateFlow, no hardcoded strings (res files), Compose previews

## Bugs & Fixes
| # | Date | Prompt | Issue | Fix | Status |
|---|------|--------|-------|-----|--------|
| 1 | 2026-06-24 | 4 | `FaceDetector.ctx_id` always `0` when device set, even for `cpu` | Changed condition from `device or "cuda" in device` to `device and "cuda" in device` | Fixed |
| 2 | 2026-06-24 | 5 | `_store_result` / `_store_clip_result` used `await get_db()` instead of `async with get_db() as db:` | Changed all DB writes to use `async with` | Fixed |
| 3 | 2026-06-24 | 6 | AndroidManifest.xml missing `xmlns:tools` namespace | Added `xmlns:tools="http://schemas.android.com/tools"` | Fixed |
| 4 | 2026-06-24 | 6 | AndroidManifest.xml referenced missing `@mipmap/ic_launcher` | Removed `android:icon` and `android:roundIcon` attributes | Fixed |
| 5 | 2026-06-24 | 7 | SyncStatusRepo exposed `MutableStateFlow` as `StateFlow` without `.asStateFlow()` | Added `.asStateFlow()` cast | Fixed |
| 6 | 2026-06-24 | 7 | `Long.toRequestBody()` doesn't exist in OkHttp | Converted `size` from `Long` to `String` before calling `.toRequestBody()` | Fixed |
| 7 | 2026-06-24 | 7 | `createdAt` was raw `String` instead of `RequestBody` | Wrapped with `.toRequestBody("text/plain".toMediaTypeOrNull())` | Fixed |
| 8 | 2026-06-24 | 7 | SyncWorker didn't save uploaded photos to local Room DB after upload | Added `photoDao.insertAll()` call on successful upload | Fixed |
| 9 | 2026-06-24 | 6 | Default base URL in `SettingsDataStore.kt` was `192.168.1.100:8000` | Changed default to `http://10.0.2.2:8000` (emulator alias for host) | Fixed |
| 10 | 2026-06-24 | 7 | `queryMediaStore()` returned empty on emulator (unknown cause) | Replaced with direct filesystem scan of `/sdcard/Pictures/`, `/sdcard/DCIM/`, `/sdcard/Download/` | Fixed |
| 11 | 2026-06-24 | 6 | `@HiltViewModel` class was `object` instead of `class` | Changed from `object GalleryViewModel` to `class GalleryViewModel` | Fixed |
| 12 | 2026-06-24 | 8 | GalleryScreen missing runtime permission request for `READ_MEDIA_IMAGES` (API 33+) | Added `rememberLauncherForActivityResult` with `RequestPermission` contract, launched on first composition | Fixed |
| 13 | 2026-06-24 | 8 | `GalleryPhotoItem.kt` used `File(photo.path)` instead of `Uri.parse(photo.path)` for Coil | Changed `File(photo.path)` → `Uri.parse(photo.path)` | Fixed |
| 14 | 2026-06-24 | 6 | `NetworkModule.kt` `@BaseUrl` was hardcoded to `http://localhost:8000` (emulator's own localhost, not host) | Read from `SettingsDataStore` via `runBlocking { settings.baseUrl.first() }` | Fixed |
| 15 | 2026-06-24 | 2 | Empty `created_at` string caused 422 from backend FastAPI | Changed `SyncWorker` to send `SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Date())` as fallback | Fixed |
| 16 | 2026-06-24 | 2 | Backend `sync.py` returned 422 on invalid date format instead of falling back | Changed `datetime.fromisoformat("")` to fall back to `datetime.now()` | Fixed |
| 17 | 2026-06-24 | 7 | Duplicate responses (`"duplicate"`) not saving to local Room DB — condition only checked `"uploaded"` | Changed condition to `body?.status == "uploaded" || body?.status == "duplicate"` | Fixed |
| 18 | 2026-06-24 | 7 | Dozens of stale WorkManager periodic jobs piling up, causing unpredictable behavior | Bypassed WorkManager for manual sync — `GalleryViewModel.triggerSync()` runs sync directly on `Dispatchers.IO` via `viewModelScope.launch`; SyncWorker rewritten to match same approach | Fixed |

## Rules (from claude.md)
- **First action on session start: read this entire file** before doing anything else (opencode.json sets `instructions: ["progress.md"]` so this loads automatically)
- **Then read `.opencode/workflow.md`** for the branching strategy and promotion rules
- One prompt at a time, lowest incomplete first, do not jump ahead
- Always produce working/runnable code — no pseudocode or placeholders (use `# TODO:` only if needed + reason)
- After each prompt, commit and push to GitHub: `feat(scope): desc`
- No gold-plating — build what prompt asks, note optional enhancements at end
- After completing each prompt, mark it `[x]` in this file's Prompts Status section, save, commit, and push

## Agents (Role-Based Pipeline)

**Pipeline**: `Architect → Coder → Tester → [Debugger ↔ Tester loop] → Tester merges to staging → Reviewer creates PR → You approve`

| Agent | Color | Model | Job | Branch | Handoff |
|-------|-------|-------|-----|--------|---------|
| **Architect** | pink | sonnet | Designs architecture, produces structured plan with DoD | — (no branches) | → Coder |
| **Coder** | green | opus | Implements code per plan | `feature/<name>` off `staging` | → Tester |
| **Tester** | red | sonnet | Writes & runs tests; merges to staging on pass | merges **directly** to `staging` | → Debugger or merge to staging |
| **Debugger** | yellow | sonnet | Diagnoses & fixes bugs; loops back to Tester | `bugfix/<name>` off `staging` (or `main`) | → Tester |
| **Reviewer** | purple | sonnet | Final quality gate; creates PR staging→main | reviews `staging` | → creates PR, assigns you |

### Agent Files (opencode — auto-discovered from `.opencode/agents/`)
| File | Role | Permission |
|------|------|------------|
| `.opencode/agents/architect.md` | Plans, designs. Never writes code. | `edit: deny` |
| `.opencode/agents/coder.md` | Implements code per plan. | full |
| `.opencode/agents/tester.md` | Writes & runs tests. Merges to staging on pass. | full |
| `.opencode/agents/debugger.md` | Fixes bugs, logs in Bugs & Fixes table. | full |
| `.opencode/agents/reviewer.md` | Final quality gate. Creates PR staging→main. | `edit: deny` |

### Claude Counterparts (reference — at `~\..claude\agents\`)
Same 5 agents exist there for Claude sessions. These opencode agents are independent equivalents.

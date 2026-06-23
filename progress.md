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
- [ ] **2** — Auth middleware + sync endpoints: `api/auth.py` (Bearer token), `POST /sync/upload`, `GET /sync/status`, `/api/v1` prefix
- [ ] **3** — ML pipeline: `ml/base.py` (abstract MLModel), `ml/loader.py` (model registry), `ml/queue.py` (async job queue), `ml/scene/yolo.py` (YOLOv8 wrapper), `ml/food/classifier.py` (EfficientNet stub), `ml/processor.py` (orchestrator)
- [ ] **4** — Face recognition: `ml/face/detector.py` (InsightFace ArcFace), `ml/face/cluster.py` (cosine sim clustering, threshold 0.6), `api/routes/faces.py` (GET clusters, GET cluster photos, PUT name)
- [ ] **5** — CLIP search: `ml/clip/encoder.py` (openai/clip-vit-base-patch32), `db/schema.py` +clip_embedding BLOB, `api/routes/search.py` (GET /search?q=, GET /search/tags)
- [ ] **6** — Android scaffold: Kotlin minSdk 26, Hilt DI, Retrofit (base URL from DataStore), Room (PhotoEntity, TagEntity), WorkManager, Navigation Compose, SettingsDataStore
- [ ] **7** — Android sync: SyncWorker (MediaStore → hash → upload), SyncScheduler (12h periodic, WiFi req), SyncStatusRepo, SyncStatusBar, READ_MEDIA_IMAGES perm
- [ ] **8** — Gallery UI: GalleryScreen (date-grouped grid), GalleryViewModel, PhotoDetailScreen (tags chips, faces), PeopleScreen (cluster grid), PersonPhotosScreen, Navigation graph
- [ ] **9** — Search UI: SearchScreen (debounced 500ms, shimmer), SearchApiService, SearchViewModel, bottom nav search icon
- [ ] **10** — Dashboard + notifications: backend/dashboard/ (Jinja2 HTML: stats, chart, logs), GET /notifications/on-this-day, Android NotificationWorker (daily)

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

## Rules (from claude.md)
- **First action on session start: read this entire file** before doing anything else (opencode.json sets `instructions: ["progress.md"]` so this loads automatically)
- One prompt at a time, lowest incomplete first, do not jump ahead
- Always produce working/runnable code — no pseudocode or placeholders (use `# TODO:` only if needed + reason)
- After each prompt, suggest commit msg: `feat(scope): desc`
- No gold-plating — build what prompt asks, note optional enhancements at end
- After completing each prompt, mark it `[x]` in this file's Prompts Status section and save

## Agents (Role-Based Pipeline)

**Workflow**: `Architect → Coder → Tester → [Debugger ↔ Tester loop] → Reviewer → Commit`

| Agent | Color | Model | Job | Handoff |
|-------|-------|-------|-----|---------|
| **Architect** | pink | sonnet | Designs architecture, produces structured plan with DoD | → Coder |
| **Coder** | green | opus | Implements code per plan | → Tester |
| **Tester** | red | sonnet | Writes & runs tests; if fail → Debugger | → Debugger or Reviewer |
| **Debugger** | yellow | sonnet | Diagnoses & fixes bugs; loops back to Tester | → Tester |
| **Reviewer** | purple | sonnet | Final quality gate: correctness, security, style, spec compliance | → Commit |

### Agent Files (opencode — auto-discovered from `.opencode/agents/`)
| File | Role | Permission |
|------|------|------------|
| `.opencode/agents/architect.md` | Plans, designs. Never writes code. | `edit: deny` |
| `.opencode/agents/coder.md` | Implements code per plan. | full |
| `.opencode/agents/tester.md` | Writes & runs tests. Hands to debugger on failure. | full |
| `.opencode/agents/debugger.md` | Fixes bugs, logs in Bugs & Fixes table. | full |
| `.opencode/agents/reviewer.md` | Final quality gate. Approves/rejects. | `edit: deny` |

### Claude Counterparts (reference — at `~\..claude\agents\`)
Same 5 agents exist there for Claude sessions. These opencode agents are independent equivalents.

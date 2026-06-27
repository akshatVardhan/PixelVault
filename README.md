# PixelVault

> Self-hosted photo gallery with on-device ML-powered search, face clustering, and scene recognition — no server required.

---

## Architecture

```
Phone (Android) — Primary Device           PC (RTX 3070) — Optional Backup
┌──────────────────────────────────┐       ┌───────────────────────────────┐
│  Kotlin / Jetpack Compose        │       │  FastAPI Backend              │
│  ├─ Gallery, Search, People UI   │       │  ├─ Sync Engine              │
│  ├─ On-Device ML Pipeline        │◄─────►│  ├─ Remote Sync (optional)   │
│  │  ├─ YOLOv8n INT8 (scene)     │  LAN  │  ├─ SQLite (meta)            │
│  │  ├─ EfficientNet-Lite0 (food) │       │  └─ Dashboard (WIP)          │
│  │  ├─ ML Kit (face detect)      │       └───────────────────────────────┘
│  │  └─ MobileFaceNet (embed)    │
│  ├─ Room DB (metadata, tags)     │
│  └─ WorkManager (processing)     │
└──────────────────────────────────┘
```

## Stack

| Layer | Technology |
|-------|-----------|
| **Android** | Kotlin, Jetpack Compose, Room, Retrofit, WorkManager, Coil, Hilt, Navigation Compose, DataStore |
| **On-Device ML** | TFLite (YOLOv8n INT8, EfficientNet-Lite0 INT8, MobileFaceNet), ML Kit (face detection), NNAPI/GPU/XNNPACK delegates |
| **UI** | shadcn-inspired theme (violet/slate palette, custom typography, 8dp base radius) |
| **Backend** (optional) | Python, FastAPI, Uvicorn, aiosqlite, Pydantic |
| **Storage** | Room/SQLite (metadata), filesystem (photos) |
| **Transport** | HTTPS over LAN, Bearer token auth, incremental sync with hash dedup |

## Project Structure

```
pixelvault/
├── android/                                 # Android app (primary client)
│   └── app/src/main/java/com/pixelvault/app/
│       ├── data/
│       │   ├── local/                       # Room DB + DataStore
│       │   │   ├── AppDatabase.kt
│       │   │   ├── PhotoEntity.kt
│       │   │   ├── FaceEntity.kt
│       │   │   ├── ClusterEntity.kt
│       │   │   ├── TagEntity.kt
│       │   │   ├── PhotoDao.kt
│       │   │   ├── TagDao.kt
│       │   │   └── SettingsDataStore.kt
│       │   ├── remote/                      # Retrofit API (optional)
│       │   │   └── ApiService.kt
│       │   └── model/                       # Domain models
│       │       └── Result.kt
│       ├── di/                              # Hilt modules
│       │   ├── DatabaseModule.kt
│       │   └── NetworkModule.kt
│       ├── ml/                              # On-device ML pipeline
│       │   ├── ModelLoader.kt               # TFLite model loading
│       │   ├── DelegateSelector.kt          # NNAPI → GPU → CPU fallback
│       │   ├── SceneDetector.kt             # YOLOv8n runner
│       │   ├── FoodClassifier.kt            # EfficientNet-Lite0 runner
│       │   ├── FaceDetector.kt              # ML Kit wrapper
│       │   ├── FaceEmbedder.kt              # MobileFaceNet runner
│       │   ├── FaceClusterer.kt             # Greedy cosine clustering
│       │   └── MLPipelineService.kt         # Orchestrator
│       ├── sync/                            # Background processing
│       │   ├── PhotoProcessingWorker.kt     # Main processing worker
│       │   ├── RemoteSyncWorker.kt          # PC sync (optional)
│       │   ├── ProcessingScheduler.kt       # Battery/idle scheduling
│       │   ├── ProcessingStatusRepo.kt
│       │   ├── NotificationWorker.kt
│       │   └── SyncScheduler.kt
│       ├── ui/
│       │   ├── theme/                       # shadcn-inspired theme system
│       │   │   ├── Color.kt
│       │   │   ├── ShadcnColors.kt
│       │   │   ├── Theme.kt
│       │   │   ├── Type.kt
│       │   │   ├── Shape.kt
│       │   │   └── ThemeMode.kt
│       │   ├── components/                  # Shared composables
│       │   │   ├── PersonClusterCard.kt
│       │   │   ├── ShimmerGrid.kt
│       │   │   └── EmptyState.kt
│       │   ├── navigation/
│       │   │   ├── NavGraph.kt
│       │   │   └── Screen.kt
│       │   ├── gallery/                     # Photo grid & detail
│       │   │   ├── GalleryScreen.kt
│       │   │   ├── GalleryViewModel.kt
│       │   │   ├── GalleryPhotoItem.kt
│       │   │   ├── PhotoDetailScreen.kt
│       │   │   └── PhotoDetailViewModel.kt
│       │   ├── search/                      # Local tag/scene/people search
│       │   │   ├── SearchScreen.kt
│       │   │   └── SearchViewModel.kt
│       │   ├── people/                      # Face cluster browsing
│       │   │   ├── PeopleScreen.kt
│       │   │   ├── PeopleViewModel.kt
│       │   │   ├── PersonPhotosScreen.kt
│       │   │   └── PersonPhotosViewModel.kt
│       │   └── settings/                    # Settings screen
│       │       ├── SettingsScreen.kt
│       │       └── SettingsViewModel.kt
│       ├── MainActivity.kt
│       └── PixelVaultApp.kt
├── backend/                                 # FastAPI server (optional backup)
│   ├── api/
│   │   ├── auth.py
│   │   └── routes/
│   │       └── sync.py
│   ├── db/
│   │   ├── schema.py
│   │   └── connection.py
│   ├── ml/
│   │   ├── base.py
│   │   ├── loader.py
│   │   ├── queue.py
│   │   ├── processor.py
│   │   ├── scene/yolo.py
│   │   └── food/classifier.py
│   ├── sync/
│   ├── dashboard/
│   ├── config.py
│   └── main.py
├── data/                     # Runtime data (photos, db, .gitignored)
├── .env.example
├── requirements.txt
└── progress.md
```

## Getting Started

### Prerequisites

- **Android phone** with Android 8+ (API 26+)
- **Android Studio** Hedgehog or later
- **Python 3.11+** (for optional backend — skip if using on-device only)
- **CUDA-capable GPU** (for optional backend only)

### Android App (Primary)

```bash
git clone https://github.com/akshatVardhan/PixelVault.git
cd PixelVault/android
./gradlew installDebug
```

### Backend Setup (Optional — only if you want PC sync)

```bash
cd PixelVault
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
# Edit .env with your settings
uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```

Enable remote sync in the app's Settings → Advanced → "Enable remote sync".

### API Endpoints (Backend)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/health` | No | Health check |
| `POST` | `/api/v1/sync/upload` | Bearer | Upload a photo with hash dedup |
| `GET` | `/api/v1/sync/status` | Bearer | Sync status & photo count |

### Environment Variables (Backend)

| Variable | Default | Description |
|----------|---------|-------------|
| `AUTH_TOKEN` | `dev-token` | Bearer token for API auth |
| `STORAGE_PATH` | `./data/photos` | Photo storage directory |
| `DB_PATH` | `./data/pixelvault.db` | SQLite database path |
| `PORT` | `8000` | Server port |
| `ML_DEVICE` | auto (CUDA/CPU) | Torch device override |
| `ML_NUM_WORKERS` | `1` | Async ML worker count |

## Current Progress

All 6 prompts are feature-complete. See [progress.md](progress.md) for detailed status.

| Prompt | Branch | Description |
|--------|--------|-------------|
| P11 | `feature/p11-foundation` | Room schema v2, TFLite deps, Settings keys |
| P12 | `feature/p12-ml-pipeline` | On-device ML: YOLOv8, EfficientNet, ML Kit, MobileFaceNet |
| P13 | `feature/p13-worker-refactor` | PhotoProcessingWorker, scheduler, status repo |
| P14 | `feature/p14-viewmodel-search-clustering` | ViewModel→Room switch, face clustering, on-device search |
| P15 | `feature/p15-shadcn-theme` | shadcn theme system (violet/slate), shared components |
| P16 | `feature/p16-ui-screens` | Screen redesign, Settings screen, bottom nav polish |

## Key Decisions

- **ML runs on-device** (NNAPI/GPU/CPU) — no PC required; PC is optional backup via LAN sync
- **TFLite models** bundled in assets (YOLOv8n INT8, EfficientNet-Lite0 INT8, MobileFaceNet)
- **ML Kit** for face detection (not recognition — no NDK pain)
- **shadcn-inspired UI** (violet/slate palette, 8dp base radius, custom typography)
- **Local-first**: photos scanned → hashed → Room → ML pipeline — no network needed
- **Face cluster threshold**: cosine similarity 0.6
- **Dynamic color** off by default to keep shadcn palette consistent

## License

MIT

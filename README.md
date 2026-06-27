# PixelVault

> Self-hosted photo gallery with on-device ML-powered search, face clustering, and scene recognition — no server required.

---

## Architecture

```
┌─────────────────────────────────────┐
│  Kotlin / Jetpack Compose           │
│  ├─ Gallery, Search, People UI      │
│  ├─ On-Device ML Pipeline           │
│  │  ├─ YOLOv8n INT8 (scene)        │
│  │  ├─ EfficientNet-Lite0 (food)    │
│  │  ├─ ML Kit (face detect)         │
│  │  └─ MobileFaceNet (embed)       │
│  ├─ Room DB (metadata, tags)        │
│  └─ WorkManager (processing)        │
└─────────────────────────────────────┘
```

## Stack

| Layer | Technology |
|-------|-----------|
| **Android** | Kotlin, Jetpack Compose, Room, Retrofit, WorkManager, Coil, Hilt, Navigation Compose, DataStore |
| **On-Device ML** | TFLite (YOLOv8n INT8, EfficientNet-Lite0 INT8, MobileFaceNet), ML Kit (face detection), NNAPI/GPU/XNNPACK delegates |
| **UI** | shadcn-inspired theme (violet/slate palette, custom typography, 8dp base radius) |
| **Storage** | Room/SQLite (metadata), filesystem (photos) |

## Project Structure

```
pixelvault/
├── android/                                 # Android app
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
├── data/                     # Runtime data (photos, db, .gitignored)
└── progress.md
```

## Getting Started

### Prerequisites

- **Android phone** with Android 8+ (API 26+)
- **Android Studio** Hedgehog or later

### Android App

```bash
git clone https://github.com/akshatVardhan/PixelVault.git
cd PixelVault/android
./gradlew installDebug
```

## Key Decisions

- **ML runs on-device** (NNAPI/GPU/CPU) — no PC or server required
- **TFLite models** bundled in assets (YOLOv8n INT8, EfficientNet-Lite0 INT8, MobileFaceNet)
- **ML Kit** for face detection (not recognition — no NDK pain)
- **shadcn-inspired UI** (violet/slate palette, 8dp base radius, custom typography)
- **Local-first**: photos scanned → hashed → Room → ML pipeline — no network needed
- **Face cluster threshold**: cosine similarity 0.6
- **Dynamic color** off by default to keep shadcn palette consistent

## License

MIT

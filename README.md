# PixelVault

> Self-hosted photo gallery with local ML-powered search, face clustering, and scene recognition — all over your home Wi-Fi.

---

## Architecture

```
Phone (Android)                     PC (RTX 3070)
┌─────────────────┐     LAN HTTPS    ┌──────────────────────┐
│  Kotlin/Jetpack  │ ◄─────────────► │  FastAPI Backend      │
│  Compose         │   token auth    │  ├─ Sync Engine       │
│  ├─ Gallery UI   │                 │  ├─ ML Pipeline       │
│  ├─ Search UI    │                 │  │  ├─ YOLOv8 (scene) │
│  ├─ People View  │                 │  │  ├─ EfficientNet   │
│  └─ Sync Worker  │                 │  │  │   (food)        │
└─────────────────┘                 │  │  ├─ Face Recog     │
                                     │  │  └─ CLIP (search) │
                                     │  ├─ SQLite (meta)    │
                                     │  └─ Dashboard        │
                                     └──────────────────────┘
```

## Stack

| Layer | Technology |
|-------|-----------|
| **Android** | Kotlin, Jetpack Compose, Room, Retrofit, WorkManager, Coil, Hilt, Navigation Compose, DataStore |
| **Backend** | Python, FastAPI, Uvicorn, aiosqlite, Pydantic |
| **ML** | YOLOv8 (scene), EfficientNet-V2-S (food), *InsightFace/ArcFace (faces — WIP)*, *CLIP (semantic search — WIP)* |
| **GPU** | RTX 3070 8GB, CUDA |
| **Storage** | SQLite (metadata), filesystem (photos) |
| **Transport** | HTTPS over LAN, Bearer token auth, incremental sync with hash dedup |

## Project Structure

```
pixelvault/
├── backend/
│   ├── api/
│   │   ├── auth.py            # Bearer token auth
│   │   └── routes/
│   │       └── sync.py        # Upload & sync status endpoints
│   ├── db/
│   │   ├── schema.py          # SQLite table definitions
│   │   └── connection.py      # Async connection helpers
│   ├── ml/
│   │   ├── base.py            # Abstract MLModel base class
│   │   ├── loader.py          # Model registry (lazy loading)
│   │   ├── queue.py           # Async job queue with worker pool
│   │   ├── processor.py       # Orchestrator (queue → model → DB)
│   │   ├── scene/
│   │   │   └── yolo.py        # YOLOv8 scene detection
│   │   └── food/
│   │       └── classifier.py  # EfficientNet food classifier
│   ├── sync/                  # Sync engine (WIP)
│   ├── dashboard/             # Web dashboard (WIP)
│   ├── config.py              # Settings via pydantic-settings
│   └── main.py                # FastAPI app entry point
├── android/                   # Android app (not yet scaffolded)
├── data/                      # Runtime data (photos, db, .gitignored)
├── .env.example
├── requirements.txt
└── progress.md
```

## Getting Started

### Prerequisites

- Python 3.11+
- CUDA-capable GPU recommended (falls back to CPU)
- Android Studio (for the Android app — coming soon)

### Backend Setup

```bash
# Clone the repo
git clone https://github.com/akshatVardhan/PixelVault.git
cd PixelVault

# Create virtual environment
python -m venv .venv
source .venv/bin/activate   # or .venv\Scripts\Activate.ps1 on Windows

# Install dependencies
pip install -r requirements.txt

# Configure
cp .env.example .env
# Edit .env with your settings (at minimum set AUTH_TOKEN)

# Run the API
uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```

### API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/health` | No | Health check |
| `POST` | `/api/v1/sync/upload` | Bearer | Upload a photo with hash dedup |
| `GET` | `/api/v1/sync/status` | Bearer | Sync status & photo count |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `AUTH_TOKEN` | `dev-token` | Bearer token for API auth |
| `STORAGE_PATH` | `./data/photos` | Photo storage directory |
| `DB_PATH` | `./data/pixelvault.db` | SQLite database path |
| `PORT` | `8000` | Server port |
| `ML_DEVICE` | auto (CUDA/CPU) | Torch device override |
| `ML_NUM_WORKERS` | `1` | Async ML worker count |

## Current Progress

See [progress.md](progress.md) for detailed status. Prompt 3 (ML pipeline) is complete. Next up: face recognition (Prompt 4).

## Key Decisions

- **ML runs on PC** (RTX 3070), not on-device — keeps the phone thin
- **Sync over local Wi-Fi** every 12h (configurable)
- **HTTPS + token auth** on LAN
- **Incremental sync** with SHA-256 hash dedup
- **Face cluster threshold**: cosine similarity 0.6
- **CLIP model**: `openai/clip-vit-base-patch32`

## License

MIT

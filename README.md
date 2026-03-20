# 🏥 AI-Powered Medical Prescription Analyzer

A multi-phase, multi-agent AI system built with **Java Spring Boot 3.3.x** and **Spring AI** that reads doctor prescriptions (handwritten or PDF), extracts structured data, routes to the right specialist, generates medical advice, and finds cheaper medicine alternatives.

---

## 🎯 What It Does

```
Doctor's Prescription (Image / PDF)
            ↓
   Google Vision OCR (reads handwriting)
            ↓
   Ollama llama3.2 (extracts structured data)
            ↓
   Groq llama-3.3-70b (recommends specialist)
            ↓
   Specialist AI Agent (generates medical advice)
            ↓
   Medicine Substitution Engine (finds cheaper alternatives)
```

---

## ✅ Phases Built

| Phase   | Feature                                        | Status      |
| ------- | ---------------------------------------------- | ----------- |
| Phase 1 | Prescription OCR + AI Data Extraction          | ✅ Complete |
| Phase 2 | Specialist Routing with Groq + Ollama fallback | ✅ Complete |
| Phase 3 | Specialist AI Doctor Agents                    | ✅ Complete |
| Phase 4 | Medicine Substitution Engine (108k+ medicines) | ✅ Complete |
| Phase 5 | Polypharmacy Risk Engine                       | 🔜 Planned  |
| Phase 6 | PDF Report Generation                          | 🔜 Planned  |
| Phase 7 | Patient History                                | 🔜 Planned  |

---

## 🛠️ Tech Stack

| Layer             | Technology                     |
| ----------------- | ------------------------------ |
| Backend           | Spring Boot 3.3.x / Java 21    |
| AI Orchestration  | Spring AI                      |
| OCR               | Google Cloud Vision API        |
| Local LLM         | Ollama (llama3.2)              |
| Cloud LLM         | Groq (llama-3.3-70b-versatile) |
| Database          | PostgreSQL 16 via Docker       |
| Migrations        | Flyway                         |
| PDF Processing    | Apache PDFBox 3.x              |
| Code Quality      | SonarQube Community            |
| Secret Management | spring-dotenv                  |

---

## 📋 Prerequisites

Before running this project make sure you have:

- Java 21
- Maven
- Docker
- Ollama installed → [https://ollama.com](https://ollama.com)
- Google Cloud account with Vision API enabled
- Groq account with API key → [https://console.groq.com](https://console.groq.com)

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/prescription-analyzer.git
cd prescription-analyzer
```

### 2. Pull the Ollama model

```bash
ollama pull llama3.2
```

### 3. Start PostgreSQL via Docker

```bash
docker run --name prescription-db \
  -e POSTGRES_USER=rxadmin \
  -e POSTGRES_PASSWORD=rxpassword \
  -e POSTGRES_DB=prescriptiondb \
  -p 5432:5432 \
  -d postgres:16
```

### 4. Set up environment variables

Create a `.env` file in the project root:

```
GROQ_API_KEY=your-groq-api-key-here
DB_URL=jdbc:postgresql://localhost:5432/prescriptiondb
DB_USERNAME=rxadmin
DB_PASSWORD=rxpassword
```

### 5. Add Google Vision credentials

- Create a Service Account in Google Cloud Console
- Download the JSON key file
- Rename it to `google-vision-credentials.json`
- Place it in `src/main/resources/`

### 6. Download the Indian Medicine Dataset

Download `indian_medicines.csv` from:

```
https://github.com/junioralive/Indian-Medicine-Dataset
```

Place it in `src/main/resources/`

### 7. Run the application

```bash
./mvnw spring-boot:run
```

On first startup the app will automatically:

- Run Flyway migrations to create the medicines table
- Load 108,000+ medicines from the CSV into PostgreSQL

---

## 🏗️ Project Structure

```
prescription-analyzer/
├── src/main/java/com/rxanalyzer/
│   ├── agent/
│   │   └── SpecialistAgent.java
│   ├── config/
│   │   └── MedicineDataLoader.java
│   ├── controller/
│   │   ├── PrescriptionController.java
│   │   ├── SpecialistAdviceController.java
│   │   └── MedicineSubstitutionController.java
│   ├── model/
│   │   ├── MedicineEntry.java
│   │   ├── PatientInfo.java
│   │   ├── PrescriptionResponse.java
│   │   ├── SpecialistAdviceRequest.java
│   │   ├── SpecialistAdviceResponse.java
│   │   ├── SpecialistRecommendation.java
│   │   ├── Medicine.java
│   │   ├── MedicineAlternative.java
│   │   ├── SubstitutionResult.java
│   │   └── SubstitutionResponse.java
│   ├── repository/
│   │   └── MedicineRepository.java
│   └── service/
│       ├── OcrService.java
│       ├── AiExtractionService.java
│       ├── PrescriptionService.java
│       ├── GroqService.java
│       ├── OllamaSpecialistService.java
│       ├── CompositionIdentifierService.java
│       └── MedicineSubstitutionService.java
├── src/main/resources/
│   ├── application.yml
│   ├── google-vision-credentials.json  ← gitignored
│   ├── indian_medicines.csv            ← download separately
│   └── db/migration/
│       └── V1__create_medicines_table.sql
├── .env                                ← gitignored
├── .gitignore
└── pom.xml
```

---

## 🔒 Security

All sensitive credentials are managed via environment variables and never committed to Git:

- `.env` — Groq API key, database credentials
- `google-vision-credentials.json` — Google Cloud service account key

Both files are listed in `.gitignore`.

---

## 💰 Cost Analysis

| Service             | Free Tier           | Cost for 500 prescriptions/month |
| ------------------- | ------------------- | -------------------------------- |
| Google Cloud Vision | 1,000 calls/month   | $0                               |
| Groq API            | 14,400 requests/day | $0                               |
| Ollama              | Unlimited (local)   | $0                               |
| PostgreSQL          | Self-hosted         | $0                               |
| **Total**           |                     | **$0**                           |

---

## 📊 SonarQube Results

| Metric            | Grade | Issues     |
| ----------------- | ----- | ---------- |
| Security          | A     | 0          |
| Reliability       | A     | 0          |
| Maintainability   | A     | 14 (minor) |
| Duplications      | A     | 0%         |
| Security Hotspots | A     | 0          |

---

## 🤝 Contributing

Pull requests are welcome. Open for any Suggestions or Collaboration. For major changes please open an issue first to discuss what you would like to change.

---

## 📄 License

MIT License — feel free to use this project for learning, research, or building your own health tech products.

---

## 🙏 Acknowledgements

- [Indian Medicine Dataset](https://github.com/junioralive/Indian-Medicine-Dataset) — 108,000+ Indian medicines dataset
- [Spring AI](https://spring.io/projects/spring-ai) — AI orchestration framework
- [Ollama](https://ollama.com) — Local LLM inference
- [Groq](https://groq.com) — High-speed LLM inference API
- [Google Cloud Vision](https://cloud.google.com/vision) — OCR engine

---

_Built with Java 21 · Spring Boot 3.3.x · Spring AI · Google Cloud Vision · Ollama · Groq · PostgreSQL_

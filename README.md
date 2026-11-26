# ReliefBridge – AI-Powered Disaster Management and Humanitarian Assistance Platform

ReliefBridge is an enterprise-grade disaster management and humanitarian relief platform built for coordinating disaster response, transparent donation collection, verifiable fund utilization, and AI-powered volunteer matching.

---

## Architecture Overview

1. **Backend**: Spring Boot 4.1.2 / Java 21, Spring Security with JWT authentication, Spring Data JPA / Hibernate, PostgreSQL 18.6, modular architecture (`auth`, `disaster`, `campaign`, `donation`, `notification`, `volunteer`). Runs on **Port 8081**.
2. **AI Service**: Python FastAPI service utilizing BGE cross-encoder reranker models for intelligent volunteer-to-disaster requirement matching. Runs on **Port 5000**.
3. **Frontend**: Modern responsive Single Page Application built with React 19, Vite, Tailwind CSS v4, Lucide React, and Recharts. Runs on **Port 5173**.

---

## Role-Based Access Control

- **ADMIN**: Platform overview, NGO approval & rejection, disaster creation, donation audit, fund utilization verification, reports.
  - **Email**: `admin@reliefbridge.com`
  - **Password**: `Admin@12345678`
- **NGO** (Approved): Campaign creation & management, campaign donation tracking, fund utilization submission with expense breakdown, volunteer requirement posting & AI-driven volunteer matching.
  - **Email**: `ngo.test@example.com`
  - **Password**: `Admin@12345678`
- **CITIZEN**: Public browsing, making online donations with Razorpay order & signature verification, instant receipt generation and PDF printing, volunteer profile registration, volunteer assignment tracking, in-app notifications.
  - **Email**: `amit.test@example.com`
  - **Password**: `Admin@12345678`

---

## Registration Requirements (Backend Validation Rules)

When registering new accounts at `/register`:
1. **Password**: Must be at least **12 characters** (enforced by backend `@Size(min=12, max=72)`).
2. **Citizen Registration**:
   - Option to participate as **Donor** and/or **Volunteer**.
   - If **Volunteer** is selected, `skills` is **mandatory** (e.g. `First Aid, Rescue, Medical`).
3. **NGO Registration**:
   - `Organisation Name` (`orgName`) and `Registration Number` (`regNo`) are **mandatory**.
   - Registered NGOs start in `approved: false` status until approved by the Admin in `/admin`.

---

## Quick Start & Running Instructions

### 1. Database Setup
PostgreSQL running on `127.0.0.1:5432` with database `reliefbridge_db`.

### 2. Start Backend (Spring Boot)
```bash
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1
mvn spring-boot:run
```
Backend runs on `http://localhost:8081`.

### 3. Start AI Volunteer Matching Service (Python)
```bash
cd reliefbridge-ai
venv\Scripts\activate
uvicorn app.main:app --host 127.0.0.1 --port 5000
```
Runs on `http://127.0.0.1:5000`.

### 4. Start Frontend (React + Vite)
```bash
cd frontend
npm run dev
```
Access at `http://localhost:5173`.

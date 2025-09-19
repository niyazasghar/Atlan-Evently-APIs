Evently - Event Booking Management System
A modern, scalable event booking backend built with Spring Boot, featuring real-time waitlist management, QR code integration, and comprehensive analytics.

🚀 Features
Core Functionality
Event Management: Create, update, and browse events with capacity management

Smart Booking System: Idempotent booking creation with concurrency protection

Intelligent Waitlist: Automatic promotion when seats become available

QR Code Integration: Quick waitlist joining via scannable QR codes

Comprehensive Analytics: User and admin analytics with daily statistics

Advanced Features
JWT Authentication: Stateless, scalable authentication system

Idempotent APIs: Safe retry mechanism using Idempotency-Key headers

Concurrency Control: Pessimistic locking prevents overbooking

Caching Layer: Caffeine cache for optimized performance

Real-time Position Tracking: Live waitlist position and ETA calculation

🛠️ Tech Stack
Backend: Spring Boot 3.x, Spring Security 6, Spring Data JPA

Database: PostgreSQL with native SQL analytics

Caching: Caffeine Cache

Authentication: JWT with Bearer tokens

QR Generation: ZXing library

Documentation: OpenAPI/Swagger

Build Tool: Maven

📋 Prerequisites
Java 17 or higher

PostgreSQL 12+

Maven 3.6+

🚦 Quick Start
1. Clone the Repository
bash
git clone https://github.com/yourusername/evently.git
cd evently

📚 API Overview
Authentication
POST /api/v1/auth/register - Register new user

POST /api/v1/auth/login - User login (returns JWT)

Events
GET /api/v1/events - List upcoming events

POST /api/v1/events - Create event (Admin)

PUT /api/v1/events/{id} - Update event (Admin)

Bookings
POST /api/v1/bookings - Create booking (requires Idempotency-Key)

DELETE /api/v1/bookings/{id}/cancel - Cancel booking (idempotent)

GET /api/v1/bookings/user/{userId} - List user bookings

Waitlist
POST /api/v1/waitlist?eventId={id} - Join waitlist (current user)

GET /api/v1/waitlist/me - View my waitlist entries

QR Waitlist
GET /api/v1/waitlist/qr/image?eventId={id} - Generate QR code (PNG)

POST /api/v1/waitlist/qr/join?eventId={id} - Quick join via QR

GET /api/v1/waitlist/qr/status?eventId={id} - Check position and ETA

Analytics
GET /api/v1/analytics/summary - Public analytics summary

GET /api/v1/admin/analytics/totals - Admin totals (requires admin role)

GET /api/v1/admin/analytics/daily-stats?from=YYYY-MM-DD&to=YYYY-MM-DD - Daily statistics

🏗️ Architecture
text
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client Layer  │────│  Security Layer  │────│ Controller Layer│
│  Web/Mobile/k6  │    │  JwtAuthFilter   │    │ Event,Booking,  │
└─────────────────┘    └──────────────────┘    │ Waitlist, QR    │
                                               └─────────────────┘
                                                        │
                              ┌─────────────────────────┼─────────────────────────┐
                              │                         │                         │
                    ┌─────────────────┐                            ┌─────────────────┐
                    │ Service Layer   │                            │ Data Access     │
                    │ Event, Booking, │                            │ JPA Repos,      │
                    │ Waitlist, QR,   │                            │ Caffeine Cache  │
                    │ Analytics       │                            └─────────────────┘
                    └─────────────────┘                                        │
                                                                    ┌─────────────────┐
                                                                    │   PostgreSQL    │
                                                                    └─────────────────┘
🔧 Key Design Decisions
Concurrency Control
Pessimistic Locking: Row-level locks on events prevent overbooking

Optimistic Retries: Small retry loops handle concurrent access gracefully

Idempotency
Idempotency-Key Header: Makes POST /bookings safe for retries

Idempotent DELETE: Single cancel endpoint for consistent behavior

Data Consistency
Transactional Operations: Booking creation and waitlist promotion are atomic

Current User Context: Waitlist operations use authenticated user automatically

Performance Optimization
DTO Pattern: Prevents lazy loading issues during JSON serialization

Date Series Analytics: Complete date ranges with zero-fill for clean charts

Caffeine Caching: Hot-path optimization for frequent reads

💡 

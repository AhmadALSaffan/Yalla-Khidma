<div align="center">
  <img src="docs/media/yalla-khedma-demo-hd.gif" alt="Yalla Khedma demo" width="100%">
</div>

<div align="center">
  <h1>يلّا خِدمة · Yalla Khedma</h1>
  <h3>An Arabic-first, bilingual services marketplace built with Kotlin Multiplatform (Android + iOS)</h3>

  <img src="https://img.shields.io/badge/Kotlin-2.3-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Compose-Multiplatform-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform" />
  <img src="https://img.shields.io/badge/Firebase-Auth%20%C2%B7%20Firestore-FFCA28?style=flat-square&logo=firebase&logoColor=black" alt="Firebase" />
  <img src="https://img.shields.io/badge/Images-Openinary-FF6B35?style=flat-square" alt="Openinary" />
  <img src="https://img.shields.io/badge/Languages-Arabic%20%C2%B7%20English-121418?style=flat-square" alt="AR / EN" />
  <img src="https://img.shields.io/badge/Architecture-Clean%20%C2%B7%20MVI-FF8B9B?style=flat-square" alt="Clean Architecture" />
  <img src="https://img.shields.io/badge/Status-In%20Development-orange?style=flat-square" alt="Status" />
</div>

---

## Overview

**Yalla Khedma** (يلّا خِدمة — *"your service, one tap away"*) connects clients with verified service providers (plumbers, electricians, tutors, designers, cleaners…) across the Arab region. It is written entirely in **Kotlin Multiplatform** with **Compose Multiplatform**, so one codebase ships to Android and iOS.

Arabic is the primary language with a full RTL layout, and the whole app switches to English (LTR, Inter font) from a single toggle — without losing your place.

Clients use the app for free; providers pay a monthly subscription to be listed. Clients propose a price, the provider accepts, and the client pays from a saved card — with optional discount coupons.

> Brand: orange `#FF6B35` on white · clean · energetic
> Tagline: **خِدمَتُك بضغطة زِر** — *Your service, one tap away*

---

## Screenshots

### Onboarding & sign-in

| Motion onboarding | | | Welcome (Google-first) |
|---|---|---|---|
| <img src="docs/screenshots/onboarding-1.png" width="200"> | <img src="docs/screenshots/onboarding-2.png" width="200"> | <img src="docs/screenshots/onboarding-3.png" width="200"> | <img src="docs/screenshots/welcome.png" width="200"> |

| Log in | Account type | Sign up | Forgot password |
|---|---|---|---|
| <img src="docs/screenshots/login.png" width="200"> | <img src="docs/screenshots/account-type.png" width="200"> | <img src="docs/screenshots/signup.png" width="200"> | <img src="docs/screenshots/forgot-password.png" width="200"> |

### Client

| Home | Explore · services | Explore · pros | Categories |
|---|---|---|---|
| <img src="docs/screenshots/client-home.png" width="200"> | <img src="docs/screenshots/explore-services.png" width="200"> | <img src="docs/screenshots/explore-pros.png" width="200"> | <img src="docs/screenshots/categories.png" width="200"> |

| Provider details | Service details | My bookings | Profile |
|---|---|---|---|
| <img src="docs/screenshots/provider-details.png" width="200"> | <img src="docs/screenshots/service-details.png" width="200"> | <img src="docs/screenshots/my-bookings.png" width="200"> | <img src="docs/screenshots/client-profile.png" width="200"> |

### Provider

| Dashboard | Requests | My services | Add service |
|---|---|---|---|
| <img src="docs/screenshots/provider-dashboard.png" width="200"> | <img src="docs/screenshots/provider-requests.png" width="200"> | <img src="docs/screenshots/provider-services.png" width="200"> | <img src="docs/screenshots/add-service.png" width="200"> |

| Coupons | Profile | Confirm it's you |
|---|---|---|
| <img src="docs/screenshots/coupons.png" width="200"> | <img src="docs/screenshots/provider-profile.png" width="200"> | <img src="docs/screenshots/confirm-identity.png" width="200"> |

### Arabic (RTL)

| تعريف متحرك | | يلّا نبدأ | لوحة التحكم |
|---|---|---|---|
| <img src="docs/screenshots/ar-onboarding.png" width="200"> | <img src="docs/screenshots/ar-onboarding-2.png" width="200"> | <img src="docs/screenshots/ar-welcome.png" width="200"> | <img src="docs/screenshots/ar-dashboard.png" width="200"> |

> A 1280×720 MP4 of the demo is at [`docs/media/yalla-khedma-demo.mp4`](docs/media/yalla-khedma-demo.mp4); a 400×225 GIF is at [`docs/media/yalla-khedma-demo.gif`](docs/media/yalla-khedma-demo.gif).

---

## Features

### Onboarding & authentication
- **Story-style motion onboarding** — three coded animation "clips" synced to a segmented progress bar; auto-advances, swipeable, skippable
- **Google-first sign-in** — one tap for new and returning users; email/password is the secondary path
- **Account type after Google** — a brand-new Google account picks client or provider before anything else
- **Email verification by link** — Firebase's free verification email; the screen detects the click automatically
- **Forgot password** — enumeration-safe reset link
- **ID verification** for providers with a live waiting screen

### For clients
- **Home** — greeting, location chip, search entry, promo banner, categories from Firestore, top-rated pros, services near you
- **Explore** — search plus category filter over services and pros, with a Services / Pros toggle
- **GPS distance sorting** — Haversine distance with a permission-aware location picker
- **Provider & service pages** — stats, specialties, services offered, call / WhatsApp, sticky "Book" bar
- **Booking with price negotiation**, saved cards (AES-256-GCM, brand auto-detect) and coupons
- **My bookings** — status filters and a "Pay now" shortcut for accepted bookings

### For providers
- **Dashboard** — new requests, this month's earnings from paid bookings, rating, total bookings, and a live last-7-days bookings chart
- **Requests** — status filters with counts; accept / decline inline
- **My services** + **Add service** — photo upload, category chips, price, duration, GPS location
- **Coupons** — ticket-style list and a live preview while creating a code
- **Profile** — identity + stats card; editing is protected by re-authentication (password or Google)

### Cross-cutting
- **Arabic + English** — in-app switch that flips RTL/LTR and the font (IBM Plex Sans Arabic / Inter) and keeps the back stack
- **Design system** — one set of tokens, components and icons (`Yk*` components, `YkIcons`)
- **Locked pill bottom bar** — tap or press-and-slide to scrub between tabs, with haptics and a springy highlight
- **Safe-area aware** — top padding follows the status bar or camera cutout, whichever is taller
- **Real-time** — Firestore snapshot listeners for bookings, providers, services, coupons and payment methods

---

## Progress Checklist

### ✅ Done

#### Foundation
- [x] KMP + Compose Multiplatform (`shared` + `androidApp` + `iosApp`)
- [x] Firebase Auth + Firestore via the GitLive Kotlin SDK — works on the free Spark plan
- [x] Koin DI, Voyager navigation, Coil 3 images
- [x] Design system: tokens, IBM Plex Sans Arabic / Inter fonts, shared components, icon set
- [x] Arabic / English with in-app switch (persisted)
- [x] Safe-area top padding for notches and punch-holes

#### Authentication
- [x] Motion onboarding (first launch only)
- [x] Google-first Welcome screen, email login, sign-up, forgot password
- [x] Account-type step for new Google users
- [x] Email verification by link (no Cloud Functions needed)
- [x] Provider ID verification + waiting screen

#### Client
- [x] Client tab shell (Home · Explore · Bookings · Profile)
- [x] Home, Explore, Categories, Provider details, Service details
- [x] Booking with price negotiation, payment methods, coupons
- [x] My bookings with filters, profile and edit profile

#### Provider
- [x] Provider tab shell (Dashboard · Requests · My services · Profile)
- [x] Live dashboard metrics + weekly chart
- [x] Requests with filters, accept / decline
- [x] My services, Add service, Coupons, Add coupon
- [x] Re-authentication before editing the profile

#### Storage
- [x] Image uploads to Openinary (profile photos, service images, ID documents)

### 🚧 In progress
- [ ] Redesign the remaining legacy screens: booking request / details / payment, payment methods, provider setup, provider edit profile, ID verification
- [ ] Move banner and category images from Firebase Storage to Openinary
- [ ] iOS build fixes (`Clock.System`, `ByteArray` → `NSData`, Koin `GlobalContext`) and in-app Google button on iOS

### 📋 Backlog
- [ ] Chats list + conversation (text, image, voice)
- [ ] Push notifications (FCM) + notifications inbox
- [ ] Reviews and ratings after a paid booking
- [ ] Subscription plans (Basic $9.99 / Pro $24.99 / VIP $49.99) + checkout
- [ ] Real payment processor (Stripe / Moyasar / HyperPay) — `markPaid` is currently a database flag
- [ ] Phone-number sign-in
- [ ] Play Store closed testing · TestFlight

---

## Tech Stack

| Layer | Technology | Notes |
|---|---|---|
| Language | Kotlin 2.3 | |
| Platforms | Android + iOS | shared `commonMain` |
| UI | Compose Multiplatform | RTL / LTR follows the language |
| Architecture | Clean Architecture + MVI | Data → Domain → Presentation |
| Navigation | Voyager | Screen + ScreenModel, custom tab host |
| DI | Koin | `expect`/`actual` `platformModule` |
| Backend | Firebase Auth + Firestore | GitLive Firebase Kotlin SDK |
| Images | Openinary Cloud | uploads via Ktor multipart |
| Networking | Ktor Client | OkHttp (Android) · Darwin (iOS) |
| Strings | Compose Multiplatform resources | `values/` (Arabic) · `values-en/` |
| Async | Coroutines + Flow | `StateFlow` per screen |
| Local storage | Multiplatform Settings | language, onboarding flag |
| Crypto | AES-256-GCM | encrypts card PANs on-device |
| Fonts | IBM Plex Sans Arabic · Inter | bundled |

---

## Architecture

```text
Presentation  (Screen + ScreenModel · Voyager)
      ↓
Domain        (Repository interfaces + Models)
      ↓
Data          (Repository impls → Firebase Auth / Firestore · Openinary)
```

### Project Structure

```text
yalla-khedma/
├── shared/                                  ← KMP module
│   └── src/
│       ├── commonMain/
│       │   ├── composeResources/
│       │   │   ├── values/strings.xml      Arabic (default)
│       │   │   ├── values-en/strings.xml   English
│       │   │   ├── font/                   IBM Plex Sans Arabic · Inter
│       │   │   └── drawable/
│       │   └── kotlin/com/yallakhedma/app/
│       │       ├── data/
│       │       │   ├── datasource/         Firebase Auth, Firestore, image storage
│       │       │   ├── remote/             OpeninaryClient (image uploads)
│       │       │   ├── local/              AppPreferences
│       │       │   └── repository/         *RepositoryImpl
│       │       ├── domain/                 models · repository interfaces · DataResult
│       │       ├── presentation/
│       │       │   ├── components/         Yk* design-system components, YkIcons, YkBottomBar
│       │       │   ├── i18n/               AppLanguage, LanguageManager, UiText, locale override
│       │       │   ├── theme/              Yk tokens · typography · shapes · AppTheme
│       │       │   ├── routing/            AuthRouting (single post-auth decision point)
│       │       │   └── screens/
│       │       │       ├── onboarding/     motion scenes + story pager
│       │       │       ├── auth/           welcome · login · signup · forgot · verify · usertype
│       │       │       ├── main/           MainTabsHost · ClientMainScreen · ProviderMainScreen
│       │       │       ├── home/ explore/ categories/ providers/ services/
│       │       │       ├── bookings/ payment/ coupons/ profile/ dashboard/
│       │       │       └── common/         shared marketplace + request cards
│       │       └── App.kt
│       ├── androidMain/                    Android actuals (locale, crypto, Google sign-in)
│       └── iosMain/                        iOS actuals
├── androidApp/                              ← Android entry
├── iosApp/                                  ← iOS entry (+ AuthBridge.swift)
├── docs/
│   ├── screenshots/                        README screenshots
│   └── media/                              demo GIF + MP4
└── gradle/libs.versions.toml
```

---

## Navigation

```text
SplashScreen
  ├─► OnboardingScreen            (first launch) ──► WelcomeScreen
  ├─► WelcomeScreen               (signed out)
  │     ├─► Google ─────────────► UserTypeSelection (new account) ──► …
  │     └─► LoginScreen ──► SignUp / ForgotPassword
  ├─► EmailVerificationScreen     (email not verified yet)
  ├─► IdVerification / Waiting    (provider, not approved)
  ├─► ClientMainScreen            Home · Explore · Bookings · Profile
  └─► ProviderMainScreen          Dashboard · Requests · My services · Profile

ProviderProfile ──► ConfirmIdentityScreen ──► ProviderEditProfileScreen
```

---

## Getting Started

### Prerequisites
- Android Studio (latest stable), JDK 17+
- Xcode 15+ for iOS
- A **Firebase project** (the free Spark plan is enough) with **Authentication** (Email/Password + Google) and **Firestore**
- An **Openinary** account for image uploads — [app.openinary.dev](https://app.openinary.dev)

### 1 — Clone
```bash
git clone https://github.com/AhmadALSaffan/Yalla-Khidma.git
cd Yalla-Khidma
```

### 2 — Firebase
1. Create a project with package `com.yallakhedma.app`
2. Put `google-services.json` in `androidApp/` and `GoogleService-Info.plist` in `iosApp/`
3. Enable **Email/Password** and **Google** sign-in, and **Firestore**
4. Paste the rules from [`firestore.rules`](./firestore.rules) into Firestore → Rules (seed data: [`firestore-test-data.md`](./firestore-test-data.md))

### 3 — Openinary key
In Openinary go to **Settings → API keys**, create a key, and add it to `local.properties` in the project root (this file is git-ignored):

```properties
openinary.apiKey=oik_your_key
openinary.baseUrl=https://cdn.openinary.dev
```

The build turns these into a generated `OpeninaryKeys` object, so the key never lands in the repository.

> Openinary Cloud delivery URLs are public. Uploaded files get random names, but anyone with a link can open it.

### 4 — Build & run

```bash
./gradlew :androidApp:assembleDebug
# or press ▶ Run in Android Studio
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run.

---

## Firebase Data Model

```text
users/{userId}
  email · phone · name · userType · userTypeChosen · emailVerified ·
  photoUrl · idVerificationStatus · idDocumentType · idDocumentUrl ·
  providerProfileCompleted
  payment_methods/{methodId}   brand · last4 · encryptedPan · iv · …

providers/{providerId}          (doc id == userId)
  name · profession · photoUrl · city · bio · rating · reviewsCount ·
  bookingsCount · completedJobs · yearsExperience · verified · services[]

services/{serviceId}
  title · description · categoryTag · priceFrom · durationText · imageUrl ·
  providerId · providerName · rating · latitude · longitude

requests/{requestId}
  serviceId · providerId · clientId · status · proposedPrice · currency ·
  paid · paidAt · paidAmount · couponCode · paymentMethodBrand · …

coupons/{couponId}
  providerId · code · discountPercent · discountAmount · active · createdAt

categories/{categoryId}
  nameAr · nameEn · imagePath · order
```

---

## Design System

| Token | Value |
|---|---|
| Brand orange | `#FF6B35` |
| Orange deep | `#C2410C` |
| Orange soft / tint | `#FFF4EE` / `#FFE3D5` |
| Ink (text) | `#121418` · `#4B5262` · `#8B91A0` |
| Canvas / fill | `#FFFFFF` / `#F6F7F9` |
| Night (dark CTA, bottom bar) | `#15161B` |
| Success · Warning · Danger | `#10B981` · `#F59E0B` · `#E5484D` |
| Radii | 8 chips · 12 small · 16 buttons & fields · 20 cards · 28 hero panels |
| Fonts | IBM Plex Sans Arabic (Arabic) · Inter (English) |

Tokens live in `presentation/theme/Color.kt` (`Yk`) and `Shapes.kt` (`YkRadius`); components in `presentation/components/`.

---

## Animation Highlights

| Where | What |
|---|---|
| Onboarding | Three timeline-driven scenes (typing search, verified badge, tracker + check + confetti) synced to story bars |
| Bottom bar | Spring highlight, press-and-slide scrubbing, directional tab transitions |
| Splash / Welcome | Spring logo entrance, floating category chips, breathing rings |
| Lists | Staggered entrance + `animateItem` placement, shimmer skeletons |
| Buttons & cards | Press-scale, loading crossfade |
| OTP-free verification | Animated mail badge, auto-detect on click |

---

## Contributing

Feedback, suggestions and pull requests are welcome.

1. Fork the repo
2. Create a branch: `git checkout -b feature/your-feature`
3. Commit: `git commit -m "feat: add your feature"`
4. Push and open a Pull Request

---

<div align="center">
  Built with ❤️ by <a href="https://github.com/AhmadALSaffan">Ahmad AL Saffan</a>
</div>

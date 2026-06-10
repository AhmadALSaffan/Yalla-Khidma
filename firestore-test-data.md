# Firestore Test Data — Home Screen

Copy each field's value (Arabic + URLs) directly from this file into the Firebase Console.

---

## 🔒 1. Firestore Security Rules

Go to **Firebase Console → Firestore Database → Rules** and paste:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{userId} {
      function isOwner() {
        return request.auth != null && request.auth.uid == userId;
      }
      // The client may set its own ID-verification status to NotSubmitted,
      // Pending, or Rejected — but NEVER to "Approved". Approval happens only
      // via the admin console / a Cloud Function (both bypass these rules).
      // On update we also allow keeping an already-Approved value, so the app
      // can still write other fields (e.g. providerProfileCompleted) afterwards.
      function notSelfApprovingOnCreate() {
        return request.resource.data.idVerificationStatus != 'Approved';
      }
      function notSelfApprovingOnUpdate() {
        return request.resource.data.idVerificationStatus != 'Approved'
          || resource.data.idVerificationStatus == 'Approved';
      }

      allow read: if isOwner();
      allow create: if isOwner() && notSelfApprovingOnCreate();
      allow update: if isOwner() && notSelfApprovingOnUpdate();
      allow delete: if false;

      // Saved payment cards live under the user. Only the owner can touch them.
      match /payment_methods/{methodId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }

    match /providers/{providerId} {
      allow read: if request.auth != null;
      // A user may create their own provider doc at signup (doc id == uid).
      allow create: if request.auth != null && request.auth.uid == providerId;
      // The owner may edit their whole profile. Anyone else may ONLY bump the
      // booking counter (a client booking a service) — they cannot touch name,
      // rating, verified, photo, etc. Hardening the counter itself (so clients
      // can't inflate it) belongs in a Cloud Function — noted for launch.
      allow update: if request.auth != null && (
        request.auth.uid == providerId ||
        request.resource.data.diff(resource.data).affectedKeys().hasOnly(['bookingsCount'])
      );
      allow delete: if false;
    }
    match /services/{serviceId} {
      allow read: if request.auth != null;
      // Providers create their own service listings from the app.
      allow create: if request.auth != null;
      allow update, delete: if false;
    }
    match /categories/{categoryId} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    // Booking requests: clients create, providers read/accept/reject, client pays.
    match /requests/{requestId} {
      allow read: if request.auth != null;
      // Only the client themselves may open a request (clientId == their uid).
      allow create: if request.auth != null
        && request.resource.data.clientId == request.auth.uid;
      // Only the two parties on the request may change it (accept/reject/pay).
      allow update: if request.auth != null && (
        resource.data.clientId == request.auth.uid ||
        resource.data.providerId == request.auth.uid
      );
      allow delete: if false;
    }

    // Discount coupons: a provider owns their codes; any signed-in user may
    // read them (needed to validate a code at checkout).
    match /coupons/{couponId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
        && request.resource.data.providerId == request.auth.uid;
      allow update, delete: if request.auth != null
        && resource.data.providerId == request.auth.uid;
    }

    // OTP codes — written/read ONLY by Cloud Functions (Admin SDK bypasses
    // rules). No client may ever touch them; the hashed code must stay secret.
    match /otps/{otpId} {
      allow read, write: if false;
    }
  }
}
```

> The canonical, deployable copy of these rules lives in **`firestore.rules`** at
> the repo root (`firebase deploy --only firestore:rules`). This block is kept in
> sync for reference.

Hit **Publish**.

---

## 🗂️ Collection `categories` (All Categories page)

Console: **Firestore Database → Start collection → ID: `categories` → Auto-ID for each document.**
The "المزيد" tile / "عرض الكل" link on the home screen opens a page that lists
**all** of these — add or delete docs here and the page updates live.

`imagePath` is a Storage path (upload the matching PNG to `categories/` in Storage,
or reuse the ones already there). `order` controls sort position (ascending).

### Category 1
| Field       | Type   | Value |
|-------------|--------|-------|
| `nameAr`    | string | `سباكة` |
| `nameEn`    | string | `Plumbing` |
| `imagePath` | string | `categories/plumbing.png` |
| `order`     | number | `1` |

### Category 2
| Field       | Type   | Value |
|-------------|--------|-------|
| `nameAr`    | string | `كهرباء` |
| `nameEn`    | string | `Electrical` |
| `imagePath` | string | `categories/electrical.png` |
| `order`     | number | `2` |

### Category 3
| Field       | Type   | Value |
|-------------|--------|-------|
| `nameAr`    | string | `تدريس` |
| `nameEn`    | string | `Tutoring` |
| `imagePath` | string | `categories/tutoring.png` |
| `order`     | number | `3` |

### Category 4
| Field       | Type   | Value |
|-------------|--------|-------|
| `nameAr`    | string | `تصميم` |
| `nameEn`    | string | `Design` |
| `imagePath` | string | `categories/design.png` |
| `order`     | number | `4` |

### Category 5
| Field       | Type   | Value |
|-------------|--------|-------|
| `nameAr`    | string | `تنظيف` |
| `nameEn`    | string | `Cleaning` |
| `imagePath` | string | `categories/cleaning.png` |
| `order`     | number | `5` |

### Category 6 (example of adding more)
| Field       | Type   | Value |
|-------------|--------|-------|
| `nameAr`    | string | `نجارة` |
| `nameEn`    | string | `Carpentry` |
| `imagePath` | string | `categories/more.png` |
| `order`     | number | `6` |

**Arabic names (quick copy):**
```
سباكة
كهرباء
تدريس
تصميم
تنظيف
نجارة
```

---

## 👷 2. Collection `providers`

Console: **Firestore Database → Start collection → ID: `providers` → Auto-ID for each document.**

> **New richer fields** (all optional — old docs still work without them). They
> power the redesigned provider details page: `bio` (string), `city` (string),
> `yearsExperience` (number), `completedJobs` (number), `verified` (boolean),
> `phone` (string), `services` (array of strings), `bookingsCount` (number, auto-incremented on booking).

### Provider 1 (full example with the new fields)

| Field             | Type      | Value |
|-------------------|-----------|-------|
| `name`            | string    | `أحمد محمد` |
| `profession`      | string    | `سباك محترف` |
| `photoUrl`        | string    | `https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300` |
| `rating`          | number    | `4.9` |
| `reviewsCount`    | number    | `124` |
| `isFeatured`      | boolean   | `true` |
| `category`        | string    | `plumbing` |
| `bio`             | string    | `سباك محترف بخبرة واسعة في إصلاح التسربات وتمديد المواسير. خدمة سريعة ومضمونة.` |
| `city`            | string    | `الرياض` |
| `yearsExperience` | number    | `8` |
| `completedJobs`   | number    | `120` |
| `verified`        | boolean   | `true` |
| `phone`           | string    | `+966500000000` |
| `services`        | array (string) | `إصلاح تسربات` , `تمديد مواسير` , `صيانة سخانات` |
| `bookingsCount`   | number    | `0` |

> To add `services`: in the console pick type **array**, then add each item as a **string** element.

### Provider 2

| Field          | Type    | Value |
|----------------|---------|-------|
| `name`         | string  | `سارة خالد` |
| `profession`   | string  | `مصممة جرافيك` |
| `photoUrl`     | string  | `https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300` |
| `rating`       | number  | `4.8` |
| `reviewsCount` | number  | `87` |
| `isFeatured`   | boolean | `true` |
| `category`     | string  | `design` |

### Provider 3

| Field          | Type    | Value |
|----------------|---------|-------|
| `name`         | string  | `عمر عبدالله` |
| `profession`   | string  | `فني كهرباء` |
| `photoUrl`     | string  | `https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300` |
| `rating`       | number  | `5.0` |
| `reviewsCount` | number  | `56` |
| `isFeatured`   | boolean | `true` |
| `category`     | string  | `electrical` |

---

## 🛠️ 3. Collection `services`

Console: **Firestore Database → Start collection → ID: `services` → Auto-ID for each document.**

> **New optional fields** for the richer service details page:
> `providerName` (string — shown in the "مقدم الخدمة" card), `durationText`
> (string, e.g. `ساعة - ساعتين`), `rating` (number, e.g. `4.7`).

### Service 1

| Field          | Type   | Value |
|----------------|--------|-------|
| `title`        | string | `إصلاح تسربات المياه` |
| `categoryTag`  | string | `سباكة` |
| `description`  | string | `خدمة سريعة وموثوقة لإصلاح جميع أنواع التسربات المنزلية.` |
| `priceFrom`    | string | `تبدأ من 50 ر.س` |
| `distance`     | string | `📍 2.5 كم` |
| `imageUrl`     | string | `https://images.unsplash.com/photo-1585704032915-c3400ca199e7?w=400` |
| `providerId`   | string | _(paste Provider 1's auto-ID — required for the booking counter to work)_ |
| `providerName` | string | `أحمد محمد` |
| `durationText` | string | `ساعة - ساعتين` |
| `rating`       | number | `4.7` |

### Service 2

| Field         | Type   | Value |
|---------------|--------|-------|
| `title`       | string | `تصميم شعار احترافي` |
| `categoryTag` | string | `تصميم` |
| `description` | string | `تصميم شعارات مميزة تعكس هوية علامتك التجارية.` |
| `priceFrom`   | string | `تبدأ من 150 ر.س` |
| `distance`    | string | `🌐 عن بعد` |
| `imageUrl`    | string | `https://images.unsplash.com/photo-1561070791-2526d30994b8?w=400` |
| `providerId`  | string | _(paste Provider 2's auto-ID, or leave empty)_ |

### Service 3

| Field         | Type   | Value |
|---------------|--------|-------|
| `title`       | string | `صيانة لوحة كهرباء` |
| `categoryTag` | string | `كهرباء` |
| `description` | string | `فحص شامل وإصلاح أعطال اللوحات الكهربائية السكنية والتجارية.` |
| `priceFrom`   | string | `تبدأ من 200 ر.س` |
| `distance`    | string | `📍 4 كم` |
| `imageUrl`    | string | `https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=400` |
| `providerId`  | string | _(paste Provider 3's auto-ID, or leave empty)_ |

---

## 🧪 Quick copy section (Arabic strings only)

If you'd rather copy the Arabic values one block at a time without table chrome:

**Provider names**
```
أحمد محمد
سارة خالد
عمر عبدالله
```

**Provider professions**
```
سباك محترف
مصممة جرافيك
فني كهرباء
```

**Service titles**
```
إصلاح تسربات المياه
تصميم شعار احترافي
صيانة لوحة كهرباء
```

**Service category tags**
```
سباكة
تصميم
كهرباء
```

**Service descriptions**
```
خدمة سريعة وموثوقة لإصلاح جميع أنواع التسربات المنزلية.
تصميم شعارات مميزة تعكس هوية علامتك التجارية.
فحص شامل وإصلاح أعطال اللوحات الكهربائية السكنية والتجارية.
```

**Service prices**
```
تبدأ من 50 ر.س
تبدأ من 150 ر.س
تبدأ من 200 ر.س
```

**Service distances**
```
📍 2.5 كم
🌐 عن بعد
📍 4 كم
```

---

## ✅ Expected behavior after upload

- Open the app → home screen → "مزودو خدمة مميزون" rail shows the 3 providers.
- Below it, "خدمات بالقرب منك" shows the 3 services.
- Photos load from Unsplash via Coil.
- Toggle a provider's `isFeatured` to `false` in the console → it disappears from the rail in real time (Firestore live updates).
- Delete all docs in either collection → the section shows the "ما عندنا..." empty-state text.

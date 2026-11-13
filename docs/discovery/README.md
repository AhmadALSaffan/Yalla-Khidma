# Natural-language service discovery

A customer types a request in their own words, for example *«عندي مي عم تنزل من تحت المجلى وبدي حدا اليوم»* ("water is leaking from under my sink and I need someone today"). The app returns ranked, relevant services, each with a short reason built from the listing's own data. From there the customer continues through the existing service → provider → booking flow.

## How it works

```text
request ──► QueryParser ──► Retriever ──► hard filters ──► rank ──► grounded reasons
            time intent      candidates    service area      score       quoted words
            concepts         + evidence    availability      rating      schedule
                                           (structured)      distance    distance
```

All of it lives in `shared/src/commonMain/kotlin/com/yallakhedma/app/domain/discovery/`, as pure Kotlin with no Firebase or UI dependencies.

| Step | File | What it does |
|---|---|---|
| Normalize | `ArabicText.kt` | Removes diacritics and tatweel, unifies the letter variants (ا / ي / ه), converts Arabic-Indic digits, applies a light prefix/suffix stemmer, and drops dialect + English stopwords |
| Understand | `QueryParser.kt` | Extracts the time intent (`Now` / `Today` / `Tomorrow`) from dialect words (هلق، اليوم، النهارده، بكرا، باكر، today, urgent…) and removes them from the retrieval text |
| Retrieve | `Retrievers.kt` | `KeywordRetriever`: BM25 baseline. `SemanticRetriever`: concept coverage (`ConceptLexicon.kt`) + a category prior + character-trigram cosine |
| Filter | `DiscoveryEngine.applyFilters` | **Hard** filters on structured data only (details below) |
| Rank + explain | `DiscoveryEngine.search` | Sorts by relevance, then rating, then distance. Builds a `MatchReason` from data |

### Hard filters

1. **Service area.** The listing needs coordinates, and the customer must be within `serviceRadiusKm` of them. A listing with coordinates but no radius gets a 10 km default, and the UI says that default was assumed. A listing without coordinates is excluded.
2. **Availability**, applied only when the request has a time. "Today" and "tomorrow" are resolved in the **listing's own time zone** (`ServiceAvailability.timeZone`), so 00:30 Saturday in Amman counts as Saturday even though it is still Friday in UTC. "Today" also requires working time left today. A listing with **unknown** availability never passes a timed request. When the request has no time, unknown listings stay in the results and are labeled *"Availability not listed"*.

Filtered-out matches are not hidden. The screen lists the strongest ones under *"Matches we filtered out"* with the reason (for example *"Outside its service area: 20.5 km away, covers 8 km"*).

### Grounded explanations

Reasons are **structured data rendered from string resources**. No text is generated freely, so a reason cannot invent a price, schedule or qualification:

- **Matches:** words quoted verbatim from the listing's title, description or category. A test checks that every quoted term appears in the listing.
- **Availability:** the listing's own `startMinute`/`endMinute` values, or "not listed".
- **Area:** the computed distance and whether the radius came from the listing or the default.

The price shown on the card is the listing's own `priceFrom` field. The demo providers are not marked verified and have no years of experience, so no qualification is claimed.

## Is this RAG?

It is retrieval-augmented, with one limit to keep in mind. Retrieval (concept-expanded semantic search over the listings) decides what is shown, and every explanation is generated **only** from the retrieved records. The generation step is template-based, not an LLM, and there is no embedding model. This was a deliberate choice:

- The Firebase project is on the Spark plan, so Cloud Functions (the repository's backend convention for anything that needs secrets, see `functions/`) cannot be deployed.
- No embedding or LLM API key is available. Shipping one inside the app would break "keep credentials server-side".
- Template generation from retrieved fields makes it impossible to invent prices, schedules or qualifications. That property is what the feature needs most.

**Upgrade path:** once the project is on Blaze, add a `discoverServices` callable in `functions/src/` that embeds the request with an embedding model whose key is stored via `defineSecret(...)`, the same pattern as `SMTP_PASSWORD`. Implement `Retriever` on the client by calling it. Filters, ranking and reasons stay unchanged, because the engine already takes the retriever as a parameter.

## Data model additions

`Service` gained three optional fields, which are safe for existing documents:

```text
services/{serviceId}
  serviceRadiusKm : number        0 / missing = not provided (10 km default, labeled)
  availability    : map | null    null = unknown
    timeZone      : "Asia/Amman"
    days          : [1..7]        ISO weekday, 1 = Monday
    startMinute   : 480           minutes after local midnight
    endMinute     : 1320          0..1440 = around the clock
    bookedDates   : ["2026-11-13"]
  isDemo          : bool
```

The service page now shows an **Hours** row, which reads *"Not listed"* when `availability` is missing.

## Demo data

`data/demo/DemoDiscoveryCatalog.kt` holds **30 fictional listings around Amman**: 12 plumbing, 9 electrical and 9 cleaning. They come with a demo customer location (Jabal Amman) and the `Asia/Amman` time zone. Every name, price and schedule is made up. These listings are flagged `isDemo`, shown with a "Demo" tag and a warning banner, and kept out of the normal Home/Explore lists.

The demo includes strong semantic matches that the filters deliberately exclude for the user-story request:

| Listing | Why it is a strong match | Why it is excluded |
|---|---|---|
| `demo-p09` «تسريب المجلى؟ نصلّحه بنفس اليوم» | Its title is literally "sink leak, same-day fix" | Zarqa, 20.5 km away, covers 8 km → **outside service area** |
| `demo-p10` «صيانة تسريبات المطبخ والحمام» | Sink and kitchen leaks, 1 km away | **Fully booked today** |
| `demo-p03` «كشف تسربات المياه بالأجهزة» | Leak detection | **Closed on Fridays** (the eval runs on a Friday) |

Opening a demo result works through the normal flow: service page → provider page → booking form. On submit, the booking screen shows *"This is a fictional demo listing, so no real request is sent"*, and nothing is written to Firestore. Chat with a demo provider is disabled. `DemoAwareProviderRepository` / `DemoAwareServiceRepository` resolve `demo-` ids locally and pass everything else to Firestore unchanged.

## Run the demo

**In the app** (Android):

1. `./gradlew :androidApp:assembleDebug` and install. Sign in as a client.
2. On **Home**, or at the top of **Explore**, tap **"اوصف مشكلتك بكلامك" / "Describe it in your own words"**.
3. With **Demo data** selected (the default), tap the first example or type your own request.
4. Switch to **Live listings** to search real Firestore services with your GPS location. Live listings have no availability yet, so a timed request ("today") honestly filters them out, and they appear under "filtered out → Availability unknown".

**Tests + eval** (JVM, no device needed):

```bash
./gradlew :shared:testAndroidHostTest --tests "*discovery*"
# report: shared/build/reports/discovery/eval.md
```

## Evaluation: keyword vs semantic

The eval uses 20 labeled requests: 10 Levantine, 1 Gulf, 1 Egyptian, 2 MSA and 6 English. Both retrievers run through **identical** hard filters and ranking. The clock is fixed at Friday 2026-11-13, 10:00 Amman time. Labels list the listings that would actually solve each request. Each one is scored against the labeled listings that also pass the filters. Full per-query table: [`eval-results.md`](./eval-results.md).

| Retriever | Hit@1 | MRR | Recall@5 | nDCG@5 | Zero-result queries | Filtered listings returned |
|---|---|---|---|---|---|---|
| Keyword (BM25) | 0.65 | 0.68 | 0.64 | 0.62 | 6/20 | 0 |
| Semantic (concepts + trigrams) | 0.95 | 0.98 | 0.96 | 0.94 | 0/20 | 0 |

| Subset | Retriever | Hit@1 | MRR | Recall@5 | nDCG@5 |
|---|---|---|---|---|---|
| Arabic (14) | Keyword | 0.93 | 0.96 | 0.92 | 0.88 |
| Arabic (14) | Semantic | 0.93 | 0.96 | 0.94 | 0.93 |
| English (6) | Keyword | 0.00 | 0.00 | 0.00 | 0.00 |
| English (6) | Semantic | 1.00 | 1.00 | 1.00 | 0.98 |

### Reading these numbers

- **Most of the headline gain is cross-language.** The listings are in Arabic, so keyword search returns nothing for all 6 English requests. On the 14 Arabic requests the two retrievers tie on Hit@1 and MRR. Semantic search is only slightly better on Recall@5 and nDCG@5, because it pushes off-topic keyword hits down: for the user-story query, keyword put a *kitchen-cleaning* listing 2nd because it mentions «المجلى», while semantic put the 24-hour emergency plumber 2nd.
- **Semantic search fails on q08** («الكهربا قاطعة… والقاطع بيفصل»). It ranks the AC/heater power-line listing first, because that listing mentions «قاطع خاص» (a dedicated breaker). Keyword gets this one right.
- **Negation is not handled.** In q04 «المي ما عم تنزل» means "the water won't drain", but the leak concept still fires. The drain listing ranks first anyway.
- **The labels and the lexicon were written by the same person in the same session.** The lexicon was not tuned after the first eval run, but this is still not a held-out benchmark, and the numbers are optimistic. A fair test needs requests collected from real users and labeled by someone else.
- 20 requests over 30 listings is a small sample, so differences of ±0.05 are noise.

## Known gaps

- Live Firestore services have no `availability` or `serviceRadiusKm` yet. Providers need a way to enter them (Add/Edit service).
- The concept lexicon is hand-written and covers only plumbing, electrical and cleaning. Other trades fall back to trigram and category matching. An embedding model behind a Cloud Function (see the upgrade path above) would remove this ceiling.
- Live mode loads up to 300 services to the client and ranks them on the device. That works for an MVP catalog but not at scale. Retrieval should move server-side.
- The booking form's currency label is the app's existing `ر.س`, while demo prices are in `د.أ`.
- iOS was not built or run (the iOS target already has known build issues, see the main README).

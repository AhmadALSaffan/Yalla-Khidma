# Discovery eval — keyword vs semantic

30 demo listings · 20 labeled requests · identical hard filters · clock fixed at 2026-11-13T07:00:00Z (Fri 10:00 Asia/Amman) · client at Jabal Amman.

| Retriever | Hit@1 | MRR | Recall@5 | nDCG@5 | Zero-result queries | Filtered listings returned |
|---|---|---|---|---|---|---|
| keyword (BM25) | 0.65 | 0.68 | 0.64 | 0.62 | 6/20 | 0 |
| semantic (concepts + trigrams) | 0.95 | 0.98 | 0.96 | 0.94 | 0/20 | 0 |

## By request language

| Subset | Retriever | Hit@1 | MRR | Recall@5 | nDCG@5 | Zero-result |
|---|---|---|---|---|---|---|
| Arabic (14) | keyword (BM25) | 0.93 | 0.96 | 0.92 | 0.88 | 0/14 |
| Arabic (14) | semantic (concepts + trigrams) | 0.93 | 0.96 | 0.94 | 0.93 | 0/14 |
| English (6) | keyword (BM25) | 0.00 | 0.00 | 0.00 | 0.00 | 6/6 |
| English (6) | semantic (concepts + trigrams) | 1.00 | 1.00 | 1.00 | 0.98 | 0/6 |

## Per query

| # | Dialect | Request | Eligible relevant | Keyword top 3 | Semantic top 3 | RR kw → sem |
|---|---|---|---|---|---|---|
| q01 | Levantine | عندي مي عم تنزل من تحت المجلى وبدي حدا اليوم | p01, p02 | p01, c03, p04 | p01, p02, p04 | 1.00 → 1.00 |
| q02 | Levantine | الحنفية بتنقط طول الليل | p01, p02, p12 | p12, p01, c06 | p12, p01, p03 | 1.00 → 1.00 |
| q03 | English | my kitchen sink is leaking, need a plumber today | p01, p02 | — | p01, p12, p04 | 0.00 → 1.00 |
| q04 | Levantine | المجاري مسدودة والمي ما عم تنزل بالمغسلة | p02, p04 | p04, p10, p02 | p04, p02, p01 | 1.00 → 1.00 |
| q05 | Levantine | السيفون خربان وبيضل يعبي | p07 | p07 | p07, p04 | 1.00 → 1.00 |
| q06 | Gulf | ابغى سباك يركب سخان ماء بكرة | p05 | p05, e05, p02 | p05, p11, p08 | 1.00 → 1.00 |
| q07 | Levantine | ضغط المي ضعيف بالطابق الثالث | p08 | p08, p01, p10 | p08, p11, p03 | 1.00 → 1.00 |
| q08 | Levantine | الكهربا قاطعة عن نص البيت والقاطع بيفصل | e01, e02, e04 | e02, e01, e05 | e05, e01, e02 | 1.00 → 0.50 |
| q09 | Levantine | الفيشة بتطلع شرار وفي ريحة حريق | e01, e06 | e06, e01 | e06, e01 | 1.00 → 1.00 |
| q10 | Egyptian | عايز كهربائي يركب نجفة النهارده | e01 | e06, e01, p04 | e01, e06, e05 | 0.50 → 1.00 |
| q11 | English | electrician to install ceiling lights | e01, e03 | — | e03, e01, e04 | 0.00 → 1.00 |
| q12 | English | need a dedicated line for my AC | e01, e05 | — | e05, e01, e09 | 0.00 → 1.00 |
| q13 | English | wiring for a new apartment | e01, e09 | — | e01, e09, e06 | 0.00 → 1.00 |
| q14 | Levantine | بدي حدا يعزّل البيت قبل ما نسكن | c01, c07 | c07, p01, c01 | c07, c01, c02 | 1.00 → 1.00 |
| q15 | MSA | تنظيف كنب وسجاد | c02, c09 | c02, c09, c08 | c02, c09, c08 | 1.00 → 1.00 |
| q16 | English | the sofa has coffee stains, can someone clean it today | c02 | — | c02, c08, c07 | 0.00 → 1.00 |
| q17 | Levantine | المطبخ كله دهون ووسخ | c01, c03 | c03, p10, p12 | c03, c04, c02 | 1.00 → 1.00 |
| q18 | English | deep cleaning after renovation, lots of dust | c01, c04, c07 | — | c04, c07, c01 | 0.00 → 1.00 |
| q19 | Levantine | صار عندنا فيضان مي بالبيت والسجاد غرقان | c08, p02 | c08, c02, c07 | c08, c02, c09 | 1.00 → 1.00 |
| q20 | MSA | الحمام فيه عفن وكلس على الحنفيات | c06 | c06, p12, p01 | c06, p12, p01 | 1.00 → 1.00 |

package com.yallakhedma.app.data.demo

import com.yallakhedma.app.domain.discovery.GeoPoint
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.model.ServiceAvailability
import kotlinx.datetime.LocalDate

/**
 * DEMO FIXTURES — 30 fictional listings around Amman for the natural-language
 * discovery MVP. Every name, price and schedule here is made up. Live Firestore
 * listings do not carry availability yet, which is why these exist; they are
 * always flagged `isDemo = true` and labeled as demo data in the UI.
 *
 * Two strong matches for "water leaking under the sink today" are deliberately
 * filtered out: `demo-p09` is outside its service area and `demo-p10` is fully
 * booked today.
 */
object DemoDiscoveryCatalog {

    const val ID_PREFIX = "demo-"
    const val TIME_ZONE = "Asia/Amman"

    /** The demo customer's location: Jabal Amman. */
    val clientLocation = GeoPoint(31.9515, 35.9239)

    fun isDemoId(id: String): Boolean = id.startsWith(ID_PREFIX)

    private enum class Area(val ar: String, val en: String, val lat: Double, val lng: Double) {
        JabalAmman("جبل عمّان", "Jabal Amman", 31.9515, 35.9239),
        Weibdeh("اللويبدة", "Weibdeh", 31.9580, 35.9230),
        Abdali("العبدلي", "Abdali", 31.9632, 35.9106),
        JabalHussein("جبل الحسين", "Jabal Al-Hussein", 31.9681, 35.9150),
        Shmeisani("الشميساني", "Shmeisani", 31.9692, 35.9000),
        Abdoun("عبدون", "Abdoun", 31.9447, 35.8870),
        Sweifieh("الصويفية", "Sweifieh", 31.9566, 35.8648),
        Marka("ماركا", "Marka", 31.9787, 35.9886),
        TlaaAlAli("تلاع العلي", "Tlaa Al-Ali", 31.9998, 35.8606),
        Khalda("خلدا", "Khalda", 31.9926, 35.8424),
        MarjAlHamam("مرج الحمام", "Marj Al-Hamam", 31.8920, 35.8410),
        Zarqa("الزرقاء", "Zarqa", 32.0728, 36.0880),
    }

    private val ALL_WEEK = listOf(1, 2, 3, 4, 5, 6, 7)
    private val SAT_TO_THU = listOf(1, 2, 3, 4, 6, 7) // closed Friday
    private val SUN_TO_THU = listOf(1, 2, 3, 4, 7)    // closed Friday + Saturday

    private class Fixture(
        val code: String,
        val category: String,
        val title: String,
        val description: String,
        val provider: String,
        val area: Area,
        val radiusKm: Double,
        val days: List<Int>,
        val from: Int,
        val until: Int,
        val price: String,
        val rating: Double,
        val bookedToday: Boolean = false,
    )

    private const val PLUMBING = "سباكة"
    private const val ELECTRICAL = "كهرباء"
    private const val CLEANING = "تنظيف"

    private fun h(hour: Int) = hour * 60

    private val fixtures = listOf(
        // ── Plumbing (12) ─────────────────────────────────────────────────
        Fixture("p01", PLUMBING, "تصليح تسريب المجلى والحنفيات",
            "نصلّح تسريب المي من تحت المجلى، ونبدّل الحنفيات والخلاطات ووصلات المي المهترئة. نفحص مصدر التسريب قبل ما نبدأ.",
            "أبو سامر للسباكة", Area.Abdoun, 12.0, ALL_WEEK, h(8), h(22), "من 15 د.أ", 4.8),
        Fixture("p02", PLUMBING, "سباك طوارئ ٢٤ ساعة",
            "خدمة طوارئ لتسريبات المي، كسر المواسير، وانسداد المجاري على مدار الساعة.",
            "سباكة النجدة", Area.Sweifieh, 15.0, ALL_WEEK, 0, h(24), "من 25 د.أ", 4.6),
        Fixture("p03", PLUMBING, "كشف تسربات المياه بالأجهزة",
            "كشف تسرب المياه داخل الجدران والأرضيات بدون تكسير، باستخدام أجهزة حرارية وصوتية.",
            "مركز الدقة لكشف التسرب", Area.Khalda, 20.0, SAT_TO_THU, h(9), h(18), "من 35 د.أ", 4.9),
        Fixture("p04", PLUMBING, "تسليك مجاري وبالوعات",
            "تسليك انسداد المجلى والبالوعات والتواليت بماكينة كهربائية، مع تنظيف خطوط الصرف.",
            "تسليك الأردن", Area.Marka, 15.0, ALL_WEEK, h(7), h(23), "من 20 د.أ", 4.5),
        Fixture("p05", PLUMBING, "تركيب وصيانة سخانات المياه",
            "تركيب سخانات الغاز والكهرباء والشمسية، وتصليح السخان اللي ما بيسخّن أو بيسرّب.",
            "حرارة للسخانات", Area.TlaaAlAli, 15.0, SAT_TO_THU, h(8), h(20), "من 20 د.أ", 4.7),
        Fixture("p06", PLUMBING, "تمديدات صحية للمطابخ والحمامات",
            "تمديد مواسير مي جديدة، وتركيب مجالي ومغاسل وكراسي حمام للبيوت الجديدة والترميم.",
            "البنّا للتمديدات الصحية", Area.JabalHussein, 10.0, SUN_TO_THU, h(8), h(17), "من 40 د.أ", 4.4),
        Fixture("p07", PLUMBING, "تصليح سيفون وتواليت",
            "تصليح السيفون اللي بيضل يعبّي، تبديل قطع النياجرا، وتصليح تسريب قاعدة التواليت.",
            "أبو خليل الصحي", Area.Shmeisani, 12.0, ALL_WEEK, h(9), h(21), "من 12 د.أ", 4.6),
        Fixture("p08", PLUMBING, "مضخات وضغط المياه",
            "تركيب وصيانة مضخات (مواتير) المي ورفع ضغط المي الضعيف للطوابق العليا.",
            "ضغط برو", Area.MarjAlHamam, 20.0, SAT_TO_THU, h(8), h(18), "من 30 د.أ", 4.3),
        Fixture("p09", PLUMBING, "تسريب المجلى؟ نصلّحه بنفس اليوم",
            "متخصصين بتسريب المي من تحت المجلى والخلاطات، تصليح بنفس اليوم.",
            "سباكة الزرقاء السريعة", Area.Zarqa, 8.0, ALL_WEEK, h(8), h(22), "من 15 د.أ", 4.9),
        Fixture("p10", PLUMBING, "صيانة تسريبات المطبخ والحمام",
            "تصليح تسريب المجلى والمغسلة ووصلات المي المرنة تحت الأحواض.",
            "ورشة الجبل", Area.JabalAmman, 10.0, ALL_WEEK, h(8), h(20), "من 18 د.أ", 4.8, bookedToday = true),
        Fixture("p11", PLUMBING, "تنظيف وعزل خزانات المياه",
            "تنظيف وتعقيم خزانات المي على السطح، وعزل الخزان لمنع التسريب.",
            "خزانات نظيفة", Area.Sweifieh, 15.0, SAT_TO_THU, h(8), h(16), "من 25 د.أ", 4.2),
        Fixture("p12", PLUMBING, "تبديل خلاطات وحنفيات",
            "توريد وتركيب خلاطات مطبخ وحمام، وتبديل الحنفية اللي بتنقّط.",
            "خلاطات العبدلي", Area.Abdali, 10.0, ALL_WEEK, h(10), h(20), "من 10 د.أ", 4.5),

        // ── Electrical (9) ────────────────────────────────────────────────
        Fixture("e01", ELECTRICAL, "كهربائي منازل — أعطال وتمديدات",
            "تصليح أعطال الكهربا بالبيت: فيش ما بتشتغل، ضو بيرجف، وقواطع بتفصل. تمديد خطوط جديدة.",
            "أبو راشد الكهربائي", Area.Weibdeh, 12.0, ALL_WEEK, h(8), h(22), "من 15 د.أ", 4.7),
        Fixture("e02", ELECTRICAL, "طوارئ كهرباء ٢٤ ساعة",
            "انقطاع الكهربا عن البيت أو جزء منه؟ نوصلك بسرعة لفحص الطبلون والقواطع.",
            "نور للطوارئ", Area.TlaaAlAli, 20.0, ALL_WEEK, 0, h(24), "من 25 د.أ", 4.5),
        Fixture("e03", ELECTRICAL, "تركيب إنارة وثريات",
            "تركيب ثريات وسبوتات ولمبات LED وإنارة مخفية للأسقف.",
            "إضاءات عمّان", Area.Sweifieh, 15.0, SAT_TO_THU, h(10), h(19), "من 10 د.أ", 4.6),
        Fixture("e04", ELECTRICAL, "فحص وتبديل الطبلون والقواطع",
            "تبديل لوحة الكهرباء القديمة، وتركيب قواطع حماية من التسريب الأرضي وموازنة الأحمال.",
            "لوحات الأمان", Area.Khalda, 20.0, SUN_TO_THU, h(8), h(16), "من 45 د.أ", 4.8),
        Fixture("e05", ELECTRICAL, "توصيل كهرباء للسخانات والمكيفات",
            "تمديد خط كهرباء مستقل للسخان الكهربائي والمكيف مع قاطع خاص.",
            "فولت للتمديدات", Area.Marka, 15.0, ALL_WEEK, h(9), h(21), "من 20 د.أ", 4.4),
        Fixture("e06", ELECTRICAL, "كشف الماس الكهربائي والشرار",
            "فحص الأسلاك اللي بتطلع شرار أو ريحة حريق، وتحديد مكان الماس الكهربائي.",
            "أمان الكهرباء", Area.Shmeisani, 12.0, ALL_WEEK, h(8), h(23), "من 20 د.أ", 4.7),
        Fixture("e07", ELECTRICAL, "تركيب كاميرات وأنتركم",
            "تركيب كاميرات مراقبة وأنظمة أنتركم وجرس الباب مع التمديدات.",
            "عين الأمان", Area.Abdoun, 15.0, SAT_TO_THU, h(9), h(18), "من 30 د.أ", 4.3),
        Fixture("e08", ELECTRICAL, "كهربائي طوارئ — الزرقاء",
            "نحل مشاكل انقطاع الكهربا والقواطع اللي بتفصل خلال ساعة.",
            "كهرباء الزرقاء", Area.Zarqa, 10.0, ALL_WEEK, 0, h(24), "من 20 د.أ", 4.6),
        Fixture("e09", ELECTRICAL, "تمديدات كهرباء للشقق الجديدة",
            "تأسيس كهرباء كامل للشقق والمحلات حسب المخطط.",
            "التأسيس الحديث", Area.MarjAlHamam, 25.0, SUN_TO_THU, h(7), h(15), "من 300 د.أ", 4.5),

        // ── Cleaning (9) ──────────────────────────────────────────────────
        Fixture("c01", CLEANING, "تنظيف شامل للبيوت",
            "تنظيف عميق للبيت كامل: مطبخ، حمامات، أرضيات وشبابيك. فريق من عاملتين.",
            "بيت يلمع", Area.Abdoun, 15.0, SAT_TO_THU, h(8), h(18), "من 35 د.أ", 4.6),
        Fixture("c02", CLEANING, "تنظيف كنب وسجاد بالبخار",
            "تنظيف الكنب والسجاد والموكيت والفرشات بالبخار وإزالة البقع.",
            "بخار كلين", Area.Sweifieh, 20.0, ALL_WEEK, h(9), h(20), "من 20 د.أ", 4.8),
        Fixture("c03", CLEANING, "تنظيف مطابخ ودهون",
            "إزالة الدهون عن الخزائن والفرن والشفاط، وتنظيف المجلى والبلاط.",
            "مطبخ لامع", Area.JabalHussein, 12.0, ALL_WEEK, h(8), h(20), "من 25 د.أ", 4.5),
        Fixture("c04", CLEANING, "تنظيف بعد الترميم والدهان",
            "إزالة غبرة البناء وبقع الدهان والإسمنت بعد التشطيب.",
            "ما بعد الورشة", Area.Khalda, 20.0, SUN_TO_THU, h(8), h(17), "من 60 د.أ", 4.4),
        Fixture("c05", CLEANING, "تنظيف واجهات وشبابيك",
            "تنظيف الزجاج والواجهات والشبابيك العالية بأدوات أمان.",
            "زجاج صافي", Area.Shmeisani, 15.0, SAT_TO_THU, h(8), h(16), "من 30 د.أ", 4.3),
        Fixture("c06", CLEANING, "تنظيف حمامات وإزالة الكلس والعفن",
            "إزالة الكلس عن الحنفيات والبلاط، وتنظيف العفن من زوايا الحمام.",
            "حمام جديد", Area.Marka, 15.0, ALL_WEEK, h(9), h(19), "من 15 د.أ", 4.5),
        Fixture("c07", CLEANING, "تعزيل البيت قبل وبعد السكن",
            "تعزيل كامل للشقة قبل الانتقال أو بعد الإخلاء، مع تنظيف الخزائن من جوّا.",
            "تعزيلة", Area.TlaaAlAli, 15.0, ALL_WEEK, h(8), h(20), "من 45 د.أ", 4.7),
        Fixture("c08", CLEANING, "تنظيف بعد تسريب المياه وتجفيف",
            "شفط المي وتجفيف السجاد والأرضيات بعد تسريب أو فيضان، ومنع الرطوبة والعفن.",
            "جاف وسريع", Area.Abdali, 12.0, ALL_WEEK, h(8), h(22), "من 30 د.أ", 4.6),
        Fixture("c09", CLEANING, "تنظيف كنب بنفس اليوم",
            "تنظيف كنب ومفروشات بالبخار مع تجفيف سريع.",
            "كنبتي", Area.JabalAmman, 10.0, SUN_TO_THU, h(9), h(18), "من 18 د.أ", 4.7),
    )

    /** The 30 demo listings. [today] (in Amman) marks `demo-p10` as fully booked. */
    fun services(today: LocalDate): List<Service> = fixtures.map { f ->
        Service(
            id = ID_PREFIX + f.code,
            title = f.title,
            categoryTag = f.category,
            description = f.description,
            priceFrom = f.price,
            providerId = providerId(f.code),
            providerName = f.provider,
            rating = f.rating,
            latitude = f.area.lat,
            longitude = f.area.lng,
            serviceRadiusKm = f.radiusKm,
            availability = ServiceAvailability(
                timeZone = TIME_ZONE,
                days = f.days,
                startMinute = f.from,
                endMinute = f.until,
                bookedDates = if (f.bookedToday) listOf(today.toString()) else emptyList(),
            ),
            isDemo = true,
        )
    }

    /** Demo provider profiles. No qualifications are claimed (not verified, experience unknown). */
    val providers: List<Provider> by lazy {
        fixtures.map { f ->
            Provider(
                id = providerId(f.code),
                name = f.provider,
                profession = f.category,
                rating = f.rating,
                category = f.category,
                city = f.area.ar,
                bio = DEMO_BIO,
                verified = false,
                services = emptyList(),
            )
        }
    }

    fun provider(id: String): Provider? = providers.firstOrNull { it.id == id }

    private fun providerId(code: String) = "${ID_PREFIX}prov-$code"

    private const val DEMO_BIO = "مزوّد تجريبي خيالي لعرض البحث الذكي — ليس مزوّد خدمة حقيقي. / Fictional demo provider — not a real business."
}

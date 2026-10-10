package com.futo.themestudio

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import java.util.zip.ZipFile
import kotlin.math.*

/** Offline FUTO advanced-theme authoring companion. Catalogue entries are generated deterministically. */
class MainActivity : Activity() {
    private val dark = Color.rgb(9, 11, 19)
    private val panel = Color.rgb(20, 25, 39)
    private val shapeFamilies = listOf("كلاسيكي", "هندسي", "نيون", "ثلاثي الأبعاد", "إبداعي")
    private val shapeBaseNames = listOf("مربع ناعم", "مستطيل", "كبسولة", "دائري", "بيضاوي", "زوايا مقصوصة", "حواف مشطوفة", "سداسي", "مثمن", "معين", "مثلث", "خماسي", "إطار مزدوج", "زجاجي", "كريستالي", "سهم", "درع", "نجمة", "موجة", "حواف تقنية")
    // 5 families × 100 numbered variants. Variants alter geometry, radius, bevel and decorative treatment.
    private val shapeNames = List(500) { i -> "${shapeFamilies[i / 100]} • ${shapeBaseNames[(i % 100) / 5]} ${i % 5 + 1}" }
    private var shapeFamilyIndex = 0
    private var shapePage = 0
    private val shapesPerPage = 20
    private val fontNames = listOf(
        "Noto Kufi Arabic Regular", "Noto Kufi Arabic Medium", "Noto Kufi Arabic Bold", "Noto Kufi Arabic Light", "Noto Kufi Arabic Black", "Noto Kufi Arabic ExtraBold", "Noto Kufi Arabic ExtraLight", "Noto Kufi Arabic SemiBold", "Noto Kufi Arabic Thin",
        "FreeSans Regular", "FreeSans Bold", "FreeSans Italic", "FreeSans Bold Italic", "FreeSerif Regular", "FreeSerif Bold", "FreeSerif Italic", "FreeSerif Bold Italic", "DejaVu Sans", "DejaVu Sans Bold", "DejaVu Sans Italic"
    )
    private val fontFiles = listOf(
        "NotoKufiArabic-Regular.ttf", "NotoKufiArabic-Medium.ttf", "NotoKufiArabic-Bold.ttf", "NotoKufiArabic-Light.ttf", "NotoKufiArabic-Black.ttf", "NotoKufiArabic-ExtraBold.ttf", "NotoKufiArabic-ExtraLight.ttf", "NotoKufiArabic-SemiBold.ttf", "NotoKufiArabic-Thin.ttf",
        "FreeSans.ttf", "FreeSansBold.ttf", "FreeSansOblique.ttf", "FreeSansBoldOblique.ttf", "FreeSerif.ttf", "FreeSerifBold.ttf", "FreeSerifItalic.ttf", "FreeSerifBoldItalic.ttf", "DejaVuSans.ttf", "DejaVuSans-Bold.ttf", "DejaVuSans-Oblique.ttf"
    )
    private var paletteIndex = 0
    private var themeIndex = 1
    private var shapeIndex = 0
    private var fontIndex = 0
    private var corner = 22f
    private var themeName = "Neon Aurora"
    private var accent = 0xFF25D9FF.toInt()
    private var keyText = Color.WHITE
    private var arabicText = Color.WHITE
    private var latinText = Color.WHITE
    private var bgImageUri: Uri? = null
    private var importedFontFile: File? = null
    private var fontChanged = false
    private val keyImageUris = mutableMapOf<String, Uri?>("normal" to null, "functional" to null, "action" to null, "pressed" to null)
    // Keep the complete imported archive so round-trip export never drops custom rules/assets.
    private var importedThemeEntries: LinkedHashMap<String, ByteArray>? = null
    private var importedThemeOriginalText: String? = null
    private val changedImageRoles = mutableSetOf<String>()
    private val iconImageUris = mutableMapOf<String, Uri?>()
    private val changedIconSelectors = mutableSetOf<String>()
    private val standardIconSelectors = listOf("icon delete_key", "icon action_emoji", "icon shift_key_shifted", "icon shift_key", "icon enter_key")
    private var backgroundImageChanged = false
    // For imported themes, regenerate existing border files only after the user edits the visual design.
    private var regenerateImportedBorders = false
    private lateinit var root: LinearLayout
    private lateinit var pageHost: LinearLayout
    private lateinit var pageScroll: ScrollView
    private val editorPages = linkedMapOf<String, LinearLayout>()
    private val navItemViews = linkedMapOf<String, TextView>()
    private var activePage = "الرئيسية"
    private lateinit var preview: LinearLayout
    private lateinit var nameInput: EditText
    private lateinit var colorLabel: TextView
    private lateinit var paletteSearch: EditText
    private lateinit var themeSearch: EditText
    private lateinit var themeLabel: TextView
    private lateinit var shapeSpinner: Spinner
    private lateinit var shapeFamilySpinner: Spinner
    private lateinit var shapePageLabel: TextView
    private lateinit var shapeSearch: EditText
    private lateinit var shapeGallery: LinearLayout
    private lateinit var fontSpinner: Spinner
    private lateinit var gradientSwitch: Switch
    private lateinit var backgroundGradientSwitch: Switch
    private lateinit var darkSwitch: Switch
    private lateinit var keyAlpha: SeekBar
    private lateinit var keySize: SeekBar
    private lateinit var textSizeSeek: SeekBar
    private lateinit var backgroundOpacity: SeekBar
    private lateinit var radiusSeek: SeekBar
    private lateinit var colorPreview: View
    private var customBgColor = 0xFF10172A.toInt()
    private var customKeyColor = 0xFF1B2942.toInt()
    private var currentPickerTarget = "accent"
    private var pendingExportFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Render a tiny, safe screen first. Build the full editor on the next UI-loop turn
        // so any startup failure can be caught and shown instead of looking like a silent exit.
        try {
            window.statusBarColor = dark
            window.navigationBarColor = dark
            val boot = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(24), dp(24), dp(24), dp(24))
                setBackgroundColor(dark)
            }
            boot.addView(txt("NEON FUTO STUDIO", 24f, 0xFF8BEAFF.toInt()))
            boot.addView(txt("جارٍ تجهيز الاستوديو…", 15f, Color.WHITE))
            setContentView(boot)
            window.decorView.post {
                try {
                    buildUi()
                } catch (failure: Throwable) {
                    android.util.Log.e("NeonFutoThemeStudio", "Startup failed while building UI", failure)
                    try {
                        showStartupError(failure)
                    } catch (diagnosticFailure: Throwable) {
                        android.util.Log.e("NeonFutoThemeStudio", "Could not render crash details", diagnosticFailure)
                        val fallback = TextView(this).apply {
                            text = "تعذّر تشغيل الاستوديو. افتح سجلّ الأخطاء وابحث عن NeonFutoThemeStudio / FATAL EXCEPTION."
                            textSize = 16f
                            setTextColor(Color.WHITE)
                            setPadding(dp(20), dp(24), dp(20), dp(24))
                            setBackgroundColor(dark)
                        }
                        try { setContentView(fallback) } catch (_: Throwable) { }
                    }
                }
            }
        } catch (failure: Throwable) {
            android.util.Log.e("NeonFutoThemeStudio", "Fatal startup setup failure", failure)
            try {
                showStartupError(failure)
            } catch (_: Throwable) {
                // Last-resort UI is intentionally simple to avoid repeating the failing path.
                try {
                    setContentView(TextView(this).apply {
                        text = "تعذّر بدء التطبيق. يلزم استخراج سجلّ الانهيار (Logcat)."
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        setPadding(24, 24, 24, 24)
                        setBackgroundColor(Color.BLACK)
                    })
                } catch (_: Throwable) { }
            }
        }
    }

    /** Show diagnostic details instead of silently closing if a device-specific startup error occurs. */
    private fun showStartupError(error: Throwable) {
        val report = android.util.Log.getStackTraceString(error)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(24))
            setBackgroundColor(dark)
        }
        page.addView(txt("تعذّر تشغيل واجهة الاستوديو", 21f, 0xFFFF8A8A.toInt()))
        page.addView(txt("ظهر خطأ أثناء تهيئة الواجهة. انسخ التقرير وأرسله للمطور لإصلاح السبب بدقة.", 14f))
        val details = TextView(this).apply {
            text = report
            textSize = 11f
            setTextColor(0xFFE5EAF5.toInt())
            setTextIsSelectable(true)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = bg(panel, 12f)
        }
        val scroll = ScrollView(this).apply { addView(details) }
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(14); bottomMargin = dp(14) })
        page.addView(button("نسخ تقرير الخطأ") {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("NeonFutoThemeStudio crash", report))
            toast("تم نسخ التقرير؛ أرسله للمطور")
        })
        setContentView(page)
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun txt(s: String, size: Float = 14f, color: Int = Color.WHITE) = TextView(this).apply { text=s; textSize=size; setTextColor(color); gravity=Gravity.CENTER_VERTICAL }
    private fun bg(c: Int, r: Float = 18f, stroke: Int = 0x334E7CFF) = GradientDrawable().apply { setColor(c); cornerRadius=dp(r.toInt()).toFloat(); setStroke(dp(1), stroke) }
    private fun section(title: String) { root.addView(txt(title, 17f, 0xFF8BEAFF.toInt()), LinearLayout.LayoutParams(-1, -2).apply { topMargin=dp(22); bottomMargin=dp(8) }) }
    private fun addView(v: View, h: Int = -2) { root.addView(v, LinearLayout.LayoutParams(-1, h).apply { topMargin=dp(5) }) }
    private fun button(label: String, action: () -> Unit) = Button(this).apply { text=label; setOnClickListener { action() } }
    private fun generatePalette(i: Int): IntArray {
        // 6,000 reproducible palettes from hue/saturation/lightness, no downloaded data.
        val hue = ((i * 137.508) % 360.0).toFloat()
        val sat = (0.48f + ((i * 17) % 48) / 100f).coerceAtMost(0.96f)
        val bgL = if (darkMode()) 0.055f + ((i * 7) % 9) / 100f else 0.84f + ((i * 3) % 12) / 100f
        val bgColor = Color.HSVToColor(floatArrayOf(hue, sat * 0.36f, bgL.coerceIn(0.04f,0.97f)))
        val keyColor = Color.HSVToColor(floatArrayOf((hue+12f)%360f, sat*0.55f, if(darkMode()) 0.13f + ((i*5)%15)/100f else 0.96f))
        val accentColor = Color.HSVToColor(floatArrayOf(hue, sat, 0.88f))
        val textColor = if (darkMode()) 0xFFF4F7FF.toInt() else 0xFF171923.toInt()
        return intArrayOf(bgColor,keyColor,accentColor,textColor,Color.HSVToColor(floatArrayOf((hue+45f)%360f,sat,0.78f)))
    }
    private fun darkMode() = !::darkSwitch.isInitialized || darkSwitch.isChecked
    private fun currentPalette() = if (paletteIndex in 0 until 6000) generatePalette(paletteIndex) else generatePalette(0)

    private fun buildUi() {
        val shell = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(dark) }
        pageScroll = ScrollView(this).apply { setBackgroundColor(dark); isFillViewport = true }
        root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(16),dp(18),dp(16),dp(30)) }
        pageScroll.addView(root)
        shell.addView(pageScroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER; setPadding(dp(4),dp(6),dp(4),dp(6)); background=bg(0xFF101625.toInt(),18f,0x5537DFFF) }
        val navItems = listOf("الرئيسية" to "⌂", "الألوان" to "◉", "الأشكال" to "▦", "الخطوط" to "Aa", "الأصول" to "▧", "التصدير" to "⇧")
        navItems.forEach { (label, icon) ->
            val item = TextView(this).apply {
                text="$icon\n$label"; gravity=Gravity.CENTER; textSize=10f
                setTextColor(if(label==activePage) 0xFF67E8FF.toInt() else 0xFF9AA8C4.toInt())
                setPadding(dp(3),dp(5),dp(3),dp(5))
                background = if (label == activePage) bg(0xFF20314A.toInt(), 12f, 0xFF37DFFF.toInt()) else bg(0x00101625, 12f, 0x00101625)
                setOnClickListener { showEditorPage(label) }
            }
            navItemViews[label] = item
            nav.addView(item, LinearLayout.LayoutParams(0, dp(54), 1f))
        }
        shell.addView(nav, LinearLayout.LayoutParams(-1, -2))
        setContentView(shell)
        root.addView(txt("NEON FUTO STUDIO",25f))
        root.addView(txt("استوديو ثيمات محلي • معاينة مباشرة • تصدير ZIP",13f,0xFF9CAAC5.toInt()))
        val banner=TextView(this).apply { text="محرر إضافي مستقل — لا يستبدل واجهة الكيبورد\n6,000 لون • 5,000 وصفة ثيم • أشكال قابلة للتعديل"; textSize=16f; gravity=Gravity.CENTER; setTextColor(Color.WHITE); setPadding(dp(14),dp(18),dp(14),dp(18)); background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(0xFF1A2854.toInt(),0xFF151824.toInt())).apply { cornerRadius=dp(22).toFloat(); setStroke(dp(1),0xFF2E8DBB.toInt()) } }
        addView(banner)
        section("معاينة حية — العربية والإنجليزية")
        preview=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(9),dp(10),dp(9),dp(10)); background=bg(panel) }
        addView(preview); refreshPreview()
        section("اسم الثيم وبيانات التصدير")
        nameInput=EditText(this).apply { setText(themeName); setTextColor(Color.WHITE); hint="اسم الثيم الذي سيظهر في FUTO"; setHintTextColor(0xFF8894AC.toInt()); background=bg(panel); setPadding(dp(12),0,dp(12),0); setSingleLine(true) }
        addView(nameInput,dp(50))
        section("مكتبة الألوان — 6,000 لون")
        colorLabel=txt("اللون ${paletteIndex+1} / 6000 • مولّد محلي",13f,0xFFB7C6E8.toInt()); addView(colorLabel)
        val paletteControls=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        paletteSearch=EditText(this).apply { hint="رقم اللون 1–6000"; inputType=2; setTextColor(Color.WHITE); setHintTextColor(0xFF8A96AE.toInt()); background=bg(panel); setPadding(dp(8),0,dp(8),0) }
        paletteControls.addView(paletteSearch,LinearLayout.LayoutParams(0,dp(48),1f))
        paletteControls.addView(button("اذهب",::goPalette),LinearLayout.LayoutParams(dp(88),dp(48)))
        addView(paletteControls)
        val paletteNav=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        paletteNav.addView(button("◀ السابق") { paletteIndex=(paletteIndex+5999)%6000; applyPalette() },LinearLayout.LayoutParams(0,dp(48),1f))
        paletteNav.addView(button("التالي ▶") { paletteIndex=(paletteIndex+1)%6000; applyPalette() },LinearLayout.LayoutParams(0,dp(48),1f))
        addView(paletteNav)
        addView(txt("اختيار سريع — المس أي عينة لتطبيق لوحة ألوان كاملة",12f,0xFF9CAAC5.toInt()))
        val swatchScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled=false }
        val swatchRow = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        (0 until 24).forEach { slot ->
            val paletteId = (paletteIndex + slot * 37) % 6000
            val swatch = View(this).apply {
                background = GradientDrawable().apply { shape=GradientDrawable.RECTANGLE; cornerRadius=dp(12).toFloat(); setColor(generatePalette(paletteId)[2]); setStroke(dp(2), if(slot==0) Color.WHITE else 0x5537DFFF) }
                contentDescription = "لوحة اللون ${paletteId+1}"
                setOnClickListener { paletteIndex=paletteId; applyPalette(); toast("تم تطبيق لوحة اللون ${paletteId+1}") }
            }
            val params = LinearLayout.LayoutParams(dp(42),dp(42)); params.marginEnd=dp(8); swatchRow.addView(swatch,params)
        }
        swatchScroll.addView(swatchRow); addView(swatchScroll,dp(54))
        colorPreview=View(this).apply { background=bg(accent,12f) }; addView(colorPreview,dp(38))
        addView(button("اختيار لون مخصص (Hue / Saturation)") { showColorPicker("accent") })
        addView(button("لون خلفية مخصص") { showColorPicker("background") })
        addView(button("لون الأزرار مخصص") { showColorPicker("key") })
        addView(button("لون النص العربي") { showColorPicker("arabic") })
        addView(button("لون النص الإنجليزي") { showColorPicker("latin") })

        section("مكتبة الثيمات — 5,000 وصفة قابلة للتعديل")
        themeLabel=txt("الوصفة ${themeIndex} / 5000",13f,0xFFB7C6E8.toInt()); addView(themeLabel)
        themeSearch=EditText(this).apply { hint="رقم الوصفة 1–5000"; inputType=2; setTextColor(Color.WHITE); setHintTextColor(0xFF8A96AE.toInt()); background=bg(panel); setPadding(dp(8),0,dp(8),0) }
        addView(themeSearch,dp(48))
        addView(button("تحميل الوصفة") { val n=themeSearch.text.toString().toIntOrNull(); if(n!=null && n in 1..5000) { themeIndex=n; applyThemeRecipe() } else toast("أدخل رقمًا بين 1 و5000") })
        addView(button("وصفة عشوائية") { themeIndex=(1..5000).random(); applyThemeRecipe() })
        val themeNav=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        themeNav.addView(button("◀ السابقة") { themeIndex=if(themeIndex<=1)5000 else themeIndex-1; applyThemeRecipe() },LinearLayout.LayoutParams(0,dp(48),1f))
        themeNav.addView(button("التالية ▶") { themeIndex=if(themeIndex>=5000)1 else themeIndex+1; applyThemeRecipe() },LinearLayout.LayoutParams(0,dp(48),1f))
        addView(themeNav)

        section("مكتبة الأشكال — 500 شكل في 5 فئات")
        addView(txt("100 كلاسيكي + 100 هندسي + 100 نيون + 100 ثلاثي الأبعاد + 100 إبداعي",12f,0xFFB7C6E8.toInt()))
        shapeFamilySpinner=Spinner(this).apply {
            adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,shapeFamilies)
            onItemSelectedListener=object:AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0:android.widget.AdapterView<*>?){}
                override fun onItemSelected(p0:android.widget.AdapterView<*>?,v:View?,pos:Int,id:Long){
                    if (pos != shapeFamilyIndex && importedThemeEntries != null) regenerateImportedBorders = true
                    shapeFamilyIndex=pos; shapePage=0; updateShapePage()
                }
            }
        }
        addView(shapeFamilySpinner,dp(48))
        shapePageLabel=txt("الصفحة 1 / 5",13f,0xFFB7C6E8.toInt()); addView(shapePageLabel)
        shapeSpinner=Spinner(this).apply {
            adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,shapeNames.take(shapesPerPage))
            onItemSelectedListener=object:AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0:android.widget.AdapterView<*>?){}
                override fun onItemSelected(p0:android.widget.AdapterView<*>?,v:View?,pos:Int,id:Long){
                    val selectedShape = (shapeFamilyIndex*100+shapePage*shapesPerPage+pos).coerceIn(0,499)
                    if (selectedShape != shapeIndex && importedThemeEntries != null) regenerateImportedBorders = true
                    shapeIndex=selectedShape
                    corner=when(shapeIndex%5){0->22f;1->50f;2->5f;3->10f;else->16f}
                    if(::radiusSeek.isInitialized) radiusSeek.progress=corner.toInt()
                    if(::preview.isInitialized) refreshPreview()
                }
            }
        }
        addView(shapeSpinner,dp(48))
        val shapeNav=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        shapeNav.addView(button("◀ الأشكال السابقة") { shapePage=if(shapePage<=0)4 else shapePage-1; updateShapePage() },LinearLayout.LayoutParams(0,dp(48),1f))
        shapeNav.addView(button("الأشكال التالية ▶") { shapePage=if(shapePage>=4)0 else shapePage+1; updateShapePage() },LinearLayout.LayoutParams(0,dp(48),1f))
        addView(shapeNav)
        shapeGallery=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(4),dp(4),dp(4),dp(4)); background=bg(panel) }
        addView(shapeGallery)
        updateShapePage()
        shapeSearch=EditText(this).apply { hint="رقم الشكل 1–500"; inputType=2; setTextColor(Color.WHITE); setHintTextColor(0xFF8A96AE.toInt()); background=bg(panel); setPadding(dp(8),0,dp(8),0) }
        addView(shapeSearch,dp(48))
        addView(button("الانتقال إلى رقم الشكل") { val n=shapeSearch.text.toString().toIntOrNull(); if(n!=null && n in 1..500){if(importedThemeEntries!=null && shapeIndex!=n-1)regenerateImportedBorders=true;shapeIndex=n-1;shapeFamilyIndex=(n-1)/100;shapePage=((n-1)%100)/shapesPerPage;if(::shapeFamilySpinner.isInitialized)shapeFamilySpinner.setSelection(shapeFamilyIndex,false);updateShapePage();shapeSpinner.setSelection((n-1)%shapesPerPage)}else toast("أدخل رقمًا بين 1 و500") })
        radiusSeek=SeekBar(this).apply { max=50; progress=22; setOnSeekBarChangeListener(seekListener { if (importedThemeEntries != null && corner != it.toFloat()) regenerateImportedBorders=true; corner=it.toFloat(); refreshPreview() }) }; addView(txt("استدارة الزوايا",13f,0xFFB7C6E8.toInt())); addView(radiusSeek)
        keySize=SeekBar(this).apply { max=24; progress=10; setOnSeekBarChangeListener(seekListener { refreshPreview() }) }; addView(txt("حجم الأزرار",13f,0xFFB7C6E8.toInt())); addView(keySize)
        keyAlpha=SeekBar(this).apply { max=100; progress=100; setOnSeekBarChangeListener(seekListener { refreshPreview() }) }; addView(txt("شفافية الأزرار",13f,0xFFB7C6E8.toInt())); addView(keyAlpha)
        textSizeSeek=SeekBar(this).apply { max=12; progress=5; setOnSeekBarChangeListener(seekListener { refreshPreview() }) }; addView(txt("حجم النص",13f,0xFFB7C6E8.toInt())); addView(textSizeSeek)

        section("الخطوط — 20 اختيارًا من خطوط النظام")
        fontSpinner=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,fontNames); onItemSelectedListener=object:AdapterView.OnItemSelectedListener { override fun onNothingSelected(p0:android.widget.AdapterView<*>?){}; override fun onItemSelected(p0:android.widget.AdapterView<*>?,v:View?,pos:Int,id:Long){ if (pos != fontIndex || importedFontFile != null) { fontIndex=pos; importedFontFile=null; fontChanged=true; refreshPreview() } } } }; addView(fontSpinner,dp(48))
        addView(txt("20 ملف خط مضمّن محليًا، مع تراخيصها؛ سيُرفق الخط المحدد داخل ZIP.",12f,0xFF929EB5.toInt()))

        section("الخلفية والتدرج والوضع")
        gradientSwitch=Switch(this).apply { text="تدرج الأزرار"; setTextColor(Color.WHITE); isChecked=true; setOnCheckedChangeListener { _,_->refreshPreview() } }; addView(gradientSwitch,dp(48))
        backgroundGradientSwitch=Switch(this).apply { text="تدرج خلفية اللوحة في المعاينة"; setTextColor(Color.WHITE); isChecked=true; setOnCheckedChangeListener { _,_->refreshPreview() } }; addView(backgroundGradientSwitch,dp(48))
        darkSwitch=Switch(this).apply { text="الوضع الداكن"; setTextColor(Color.WHITE); isChecked=true; setOnCheckedChangeListener { _,_->applyPalette() } }; addView(darkSwitch,dp(48))
        addView(button("اختيار صورة خلفية…") { chooseImage(REQUEST_BACKGROUND) })
        addView(txt("شفافية صورة الخلفية",13f,0xFFB7C6E8.toInt())); backgroundOpacity=SeekBar(this).apply { max=100; progress=100; setOnSeekBarChangeListener(seekListener { refreshPreview() }) }; addView(backgroundOpacity)

        section("صور مستقلة لفئات الأزرار")
        addView(txt("يمكن إرفاق صور منفصلة للأصلية والوظيفية والإجراء والضغط. يطبق FUTO الصور وفق قواعد الأصول/المطابقة التي يدعمها محرك الثيم، وليس كصورة اعتباطية لكل حرف.",12f,0xFF929EB5.toInt()))
        addView(button("صورة أزرار الحروف / العادية…") { currentImageRole="normal"; chooseImage(REQUEST_NORMAL) })
        addView(button("صورة الأزرار الوظيفية…") { currentImageRole="functional"; chooseImage(REQUEST_FUNCTIONAL) })
        addView(button("صورة زر الإجراء / الإرسال…") { currentImageRole="action"; chooseImage(REQUEST_ACTION) })
        addView(button("صورة حالة الضغط…") { currentImageRole="pressed"; chooseImage(REQUEST_PRESSED) })
        section("مكتبة الأيقونات — حفظ وتخصيص أيقونات الثيم")
        addView(txt("عند استيراد ثيم مثل Animal تبقى جميع أيقوناته وقواعدها محفوظة. هذه الأزرار تستبدل أصل الأيقونة المحدد فقط عند التصدير.",12f,0xFF929EB5.toInt()))
        addView(button("أيقونة الحذف / Backspace…") { chooseImage(REQUEST_ICON_BACKSPACE) })
        addView(button("أيقونة الإيموجي…") { chooseImage(REQUEST_ICON_EMOJI) })
        addView(button("أيقونة Shift العادية…") { chooseImage(REQUEST_ICON_SHIFT) })
        addView(button("أيقونة Shift المضغوطة…") { chooseImage(REQUEST_ICON_SHIFT_PRESSED) })
        addView(button("أيقونة Enter / الإدخال…") { chooseImage(REQUEST_ICON_ENTER) })

        section("الاستيراد والتصدير")
        addView(button("بدء ثيم جديد فارغ") { startFreshTheme() })
        addView(button("استيراد ثيم FUTO موجود (ZIP)") { chooseImport() })
        addView(button("تصدير ثيم FUTO بصيغة ZIP") { themeName=nameInput.text.toString().trim().ifBlank { "FUTO Theme $themeIndex" }; exportZip() },dp(56))
        addView(button("إعادة ضبط المعاينة") { paletteIndex=0; themeIndex=1; shapeIndex=0; shapeFamilyIndex=0; shapePage=0; if(::shapeFamilySpinner.isInitialized)shapeFamilySpinner.setSelection(0,false); updateShapePage(); fontSpinner.setSelection(0); applyPalette() })
        root.addView(txt("الكتالوجان مولّدان خوارزميًا ويعملان دون إنترنت. التصدير يستخدم بنية Advanced Theme المعروفة في FUTO (theme.txt وأصول الحدود). الميزات التي لا يعرّفها تنسيق FUTO الرسمي، مثل تلوين العربية واللاتينية بشكل مستقل أو صورة خلفية لكل حرف، تُعرض هنا للمعاينة ولا يمكن ضمان ظهورها في محرك الكيبورد دون تعديل المحرك نفسه.",12f,0xFF8894AC.toInt()).apply { setPadding(0,dp(18),0,0) })
        organizeIntoPages()
    }

    private fun organizeIntoPages() {
        val original = (0 until root.childCount).map { root.getChildAt(it) }
        val titles = listOf("الرئيسية", "الألوان", "الأشكال", "الخطوط", "الأصول", "التصدير")
        titles.forEach { title -> editorPages[title] = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(16),dp(18),dp(16),dp(30)) } }
        var current = "الرئيسية"
        root.removeAllViews()
        original.forEach { view ->
            if (view is TextView) {
                val label = view.text?.toString().orEmpty()
                current = when {
                    label.contains("مكتبة الألوان") || label.contains("اسم الثيم") -> if(label.contains("اسم الثيم")) "الرئيسية" else "الألوان"
                    label.contains("مكتبة الثيمات") || label.contains("مكتبة الأشكال") -> "الأشكال"
                    label.contains("الخطوط") -> "الخطوط"
                    label.contains("الخلفية") || label.contains("صور مستقلة") || label.contains("مكتبة الأيقونات") -> "الأصول"
                    label.contains("الاستيراد والتصدير") -> "التصدير"
                    else -> current
                }
            }
            editorPages[current]?.addView(view)
        }
        // The home page keeps the banner and preview; the other pages hold independent editor controls.
        editorPages.values.forEach { page -> pageHost = page }
        root.addView(editorPages["الرئيسية"]!!)
        showEditorPage("الرئيسية")
    }

    private fun showEditorPage(label: String) {
        if (!::root.isInitialized || editorPages.isEmpty()) return
        activePage = label
        root.removeAllViews()
        val page = editorPages[label] ?: editorPages["الرئيسية"]!!
        root.addView(page)
        navItemViews.forEach { (itemLabel, item) ->
            val selected = itemLabel == activePage
            item.setTextColor(if (selected) 0xFF67E8FF.toInt() else 0xFF9AA8C4.toInt())
            item.background = if (selected) bg(0xFF20314A.toInt(), 12f, 0xFF37DFFF.toInt()) else bg(0x00101625, 12f, 0x00101625)
        }
        pageScroll.scrollTo(0, 0)
    }
    private fun seekListener(f:(Int)->Unit)=object:SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){ if(u) f(p) }; override fun onStartTrackingTouch(s:SeekBar?){}; override fun onStopTrackingTouch(s:SeekBar?){} }
    private fun applyThemeRecipe(){ paletteIndex=(themeIndex*37)%6000; shapeIndex=(themeIndex-1)%500; shapeFamilyIndex=shapeIndex/100; shapePage=(shapeIndex%100)/shapesPerPage; if(::shapeFamilySpinner.isInitialized)shapeFamilySpinner.setSelection(shapeFamilyIndex,false); if(::shapeSpinner.isInitialized)updateShapePage(); if(::themeLabel.isInitialized)themeLabel.text="${themePresetName(themeIndex)} • $themeIndex / 5000"; applyPalette() }
    private fun updateShapePage(){
        if(!::shapeSpinner.isInitialized || !::shapePageLabel.isInitialized) return
        val start=shapeFamilyIndex*100+shapePage*shapesPerPage
        val items=(start until min(start+shapesPerPage,(shapeFamilyIndex+1)*100)).map{shapeNames[it]}
        if(shapeIndex !in (start until (start+items.size))) shapeIndex=start
        shapePageLabel.text="${shapeFamilies[shapeFamilyIndex]} • الصفحة ${shapePage+1} / 5 • الأشكال ${start+1}–${start+items.size} / ${shapeFamilies.size*100}"
        shapeSpinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,items)
        shapeSpinner.setSelection((shapeIndex-start).coerceIn(0,items.lastIndex),false)
        if(::shapeGallery.isInitialized){
            shapeGallery.removeAllViews()
            items.forEachIndexed { local, label ->
                if(local%4==0){ shapeGallery.addView(LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }) }
                val row=shapeGallery.getChildAt(shapeGallery.childCount-1) as LinearLayout
                val absolute=start+local
                val tile=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(dp(3),dp(4),dp(3),dp(4)); background=bg(if(absolute==shapeIndex)0xFF263B59.toInt() else 0xFF111827.toInt(),10f) }
                val imageBytes=makePng(customKeyColor,accent,corner,absolute)
                val image=ImageView(this).apply { setImageBitmap(BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.size)); scaleType=ImageView.ScaleType.FIT_CENTER }
                tile.addView(image,LinearLayout.LayoutParams(-1,dp(48)))
                tile.addView(txt("${absolute+1}. ${shapeBaseNames[(absolute%100)/5]}",9f,0xFFE5EEFF.toInt()).apply { gravity=Gravity.CENTER; maxLines=2 })
                tile.setOnClickListener { if(importedThemeEntries!=null && shapeIndex!=absolute)regenerateImportedBorders=true; shapeIndex=absolute; shapeSpinner.setSelection(local); refreshPreview(); updateShapePage() }
                val lp=LinearLayout.LayoutParams(0,dp(82),1f); lp.setMargins(dp(2),dp(2),dp(2),dp(2)); row.addView(tile,lp)
            }
            shapeGallery.invalidate()
        }
    }
    private fun themePresetName(i:Int)=listOf("Aurora","Cyberpunk","Crystal","Obsidian","Forest","Ocean","Solar","Neon","Glass","Quantum")[((i-1)%10)] + " ${i.toString().padStart(4,'0')}"
    private fun goPalette(){ val n=paletteSearch.text.toString().toIntOrNull(); if(n!=null && n in 1..6000){paletteIndex=n-1;applyPalette()}else toast("أدخل رقمًا بين 1 و6000") }
    private fun applyPalette(){ if(importedThemeEntries!=null)regenerateImportedBorders=true; val p=currentPalette(); accent=p[2]; customBgColor=p[0]; customKeyColor=p[1]; keyText=p[3]; if(!::preview.isInitialized)return; colorLabel.text="اللون ${paletteIndex+1} / 6000 • ${hex(accent)}"; colorPreview.background=bg(accent,12f); refreshPreview() }
    private fun showColorPicker(target:String){ currentPickerTarget=target; val wrap=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(8),dp(18),dp(8))}; val hsv=SeekBar(this).apply{max=359;progress=Color.red(accent)*360/256}; val sat=SeekBar(this).apply{max=100;progress=90}; val value=SeekBar(this).apply{max=100;progress=90}; val sample=View(this).apply{background=bg(accent,12f)}; wrap.addView(txt("Hue",13f));wrap.addView(hsv);wrap.addView(txt("Saturation",13f));wrap.addView(sat);wrap.addView(txt("Brightness",13f));wrap.addView(value);wrap.addView(sample,LinearLayout.LayoutParams(-1,dp(38))); val dialog=android.app.AlertDialog.Builder(this).setTitle("اختيار لون مخصص").setView(wrap).setPositiveButton("تطبيق",null).setNegativeButton("إلغاء",null).create(); val update={ val c=Color.HSVToColor(floatArrayOf(hsv.progress.toFloat(),sat.progress/100f,value.progress/100f)); sample.background=bg(c,12f) }; hsv.setOnSeekBarChangeListener(seekListener{update()});sat.setOnSeekBarChangeListener(seekListener{update()});value.setOnSeekBarChangeListener(seekListener{update()}); dialog.setOnShowListener{dialog.getButton(-1).setOnClickListener{val c=Color.HSVToColor(floatArrayOf(hsv.progress.toFloat(),sat.progress/100f,value.progress/100f));when(target){"accent"->accent=c;"background"->customBgColor=c;"key"->customKeyColor=c;"arabic"->arabicText=c;"latin"->latinText=c};if(importedThemeEntries!=null && (target=="accent" || target=="background" || target=="key"))regenerateImportedBorders=true;refreshPreview();dialog.dismiss()}};dialog.show() }
    private fun refreshPreview(){ if(!::preview.isInitialized)return; val p=currentPalette(); val bgc=customBgColor; val keyc=customKeyColor; preview.removeAllViews(); val bgColors=if(::backgroundGradientSwitch.isInitialized&&backgroundGradientSwitch.isChecked) intArrayOf(bgc,blend(bgc,accent)) else intArrayOf(bgc, bgc); preview.background=try { val u=bgImageUri; if(u!=null) openInputStreamSafe(u)?.use { stream -> BitmapFactory.decodeStream(stream)?.let { bitmap -> android.graphics.drawable.BitmapDrawable(resources, bitmap) } } ?: GradientDrawable(GradientDrawable.Orientation.TL_BR,bgColors).apply{cornerRadius=dp(16).toFloat()} else GradientDrawable(GradientDrawable.Orientation.TL_BR,bgColors).apply{cornerRadius=dp(16).toFloat()} } catch (_:Exception) { GradientDrawable(GradientDrawable.Orientation.TL_BR,bgColors).apply{cornerRadius=dp(16).toFloat()} }; if(::backgroundOpacity.isInitialized && bgImageUri!=null) preview.background?.alpha=backgroundOpacity.progress*255/100; val head=txt("اقتراحات  •  الكلمات  •  🎙",12f,if(darkMode())Color.WHITE else Color.BLACK).apply{gravity=Gravity.CENTER;setPadding(0,dp(4),0,dp(7))};preview.addView(head); val keyShapeBytes=makePng(keyc,if(::gradientSwitch.isInitialized && gradientSwitch.isChecked) accent else keyc,corner,shapeIndex); val generatedKeyBitmap=BitmapFactory.decodeByteArray(keyShapeBytes,0,keyShapeBytes.size); val keyShapeBitmap=try { keyImageUris["normal"]?.let { u -> openInputStreamSafe(u)?.use { BitmapFactory.decodeStream(it) } } ?: generatedKeyBitmap } catch (_:Exception) { generatedKeyBitmap }; val rows=listOf("ض ص ث ق ف غ ع ه خ ح ج","ش س ي ب ل ا ت ن م ك","ئ ء ؤ ر لا ى ة و ز ظ","q w e r t y u i o p","a s d f g h j k l","⇧ z x c v b n m ⌫","?123   ,      English      .      ↵"); rows.forEachIndexed{ri,row->val line=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER};row.split(" ").filter{it.isNotBlank()}.forEach{key->val alpha=if(::keyAlpha.isInitialized) keyAlpha.progress*255/100 else 255;val kc=(keyc and 0x00FFFFFF) or (alpha shl 24);val fg=when{key.any{it in '\u0600'..'\u06FF'}->arabicText;key.any{it in 'a'..'z'||it in 'A'..'Z'}->latinText;else->p[3]};val view=TextView(this).apply{text=key;textSize=(if(ri==6)10f else 15f)+(if(::textSizeSeek.isInitialized)textSizeSeek.progress/4f else 0f);gravity=Gravity.CENTER;setTextColor(fg);typeface=try { importedFontFile?.let { Typeface.createFromFile(it) } ?: Typeface.createFromAsset(assets,"fonts/${fontFiles[fontIndex]}") } catch (_:Exception) { Typeface.DEFAULT_BOLD };val gradient=::gradientSwitch.isInitialized&&gradientSwitch.isChecked;val colors=if(gradient)intArrayOf(blend(kc,accent),kc)else intArrayOf(kc);background=android.graphics.drawable.BitmapDrawable(resources,keyShapeBitmap).apply{this.alpha=alpha};setPadding(dp(1),0,dp(1),0)};val height=if(ri==6)38 else 39+(if(::keySize.isInitialized)keySize.progress/4 else 0);val lp=LinearLayout.LayoutParams(0,dp(height),1f);lp.setMargins(dp(1),dp(2),dp(1),dp(2));line.addView(view,lp)};preview.addView(line)}; val note=txt("Aa العربية 123  •  معاينة شكل ${shapeNames[shapeIndex.coerceIn(0,shapeNames.lastIndex)]}",12f,accent).apply{gravity=Gravity.CENTER};preview.addView(note) }
    private var currentImageRole="normal"
    private fun chooseImage(req:Int){ val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="image/*"};startActivityForResult(i,req) }
    private fun startFreshTheme() {
        importedThemeEntries = null; importedThemeOriginalText = null
        changedImageRoles.clear(); changedIconSelectors.clear(); backgroundImageChanged = false; fontChanged = false; regenerateImportedBorders = false
        keyImageUris.keys.forEach { keyImageUris[it] = null }; iconImageUris.clear(); standardIconSelectors.forEach { iconImageUris[it] = null }
        bgImageUri = null; importedFontFile = null; themeName = "Neon Aurora"
        if (::nameInput.isInitialized) nameInput.setText(themeName)
        toast("بدأ ثيم جديد؛ سيُنشأ ZIP مستقل بأصول الأزرار والأيقونات القياسية")
        refreshPreview()
    }

    private fun chooseImport(){ val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/zip"};startActivityForResult(i,REQUEST_IMPORT) }
    private fun exportZip() {
        themeName = nameInput.text.toString().trim().ifBlank { "FUTO Theme $themeIndex" }
        try {
            pendingExportFile?.delete()
            val temp = File(cacheDir, "theme_export_validation.zip")
            FileOutputStream(temp).use { writeThemeZip(it, themeName) }
            val problems = validateThemeArchive(temp)
            if (problems.isNotEmpty()) {
                android.app.AlertDialog.Builder(this).setTitle("لم يجتز الثيم الفحص")
                    .setMessage(problems.take(8).joinToString("\n• ", prefix="• "))
                    .setPositiveButton("حسنًا", null).show()
                temp.delete()
                return
            }
            pendingExportFile = temp
            val report = inspectThemeArchive(temp)
            android.app.AlertDialog.Builder(this)
                .setTitle("فحص الثيم قبل الحفظ")
                .setMessage("الاسم: $themeName\n\n$report\n\nتم اجتياز الفحص البنيوي. الحفظ لا يغني عن التجربة داخل نسخة FUTO Keyboard المستهدفة.")
                .setPositiveButton("حفظ ZIP") { _, _ ->
                    val i = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE); type = "application/zip"
                        putExtra(Intent.EXTRA_TITLE, safeName(themeName) + ".zip")
                    }
                    startActivityForResult(i, REQUEST_EXPORT)
                }
                .setNegativeButton("إلغاء") { _, _ -> pendingExportFile?.delete(); pendingExportFile = null }
                .show()
        } catch (e: Exception) {
            pendingExportFile?.delete(); pendingExportFile = null
            toast("تعذر تجهيز الثيم: ${e.message}")
        }
    }

    @Deprecated("Deprecated in Android") override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if (requestCode == REQUEST_EXPORT && (resultCode != RESULT_OK || data?.data == null)) {
            pendingExportFile?.delete(); pendingExportFile = null
            return
        }
        if(resultCode!=RESULT_OK||data?.data==null)return
        val uri=data.data!!
        when(requestCode){
            REQUEST_IMPORT->{try{importTheme(uri)}catch(e:Exception){toast("تعذر استيراد الثيم: ${e.message}")};return}
            REQUEST_BACKGROUND->{bgImageUri=uri;backgroundImageChanged=true}
            REQUEST_NORMAL->{keyImageUris["normal"]=uri;changedImageRoles.add("normal")}
            REQUEST_FUNCTIONAL->{keyImageUris["functional"]=uri;changedImageRoles.add("functional")}
            REQUEST_ACTION->{keyImageUris["action"]=uri;changedImageRoles.add("action")}
            REQUEST_PRESSED->{keyImageUris["pressed"]=uri;changedImageRoles.add("pressed")}
            REQUEST_ICON_BACKSPACE->{iconImageUris["icon delete_key"]=uri;changedIconSelectors.add("icon delete_key")}
            REQUEST_ICON_EMOJI->{iconImageUris["icon action_emoji"]=uri;changedIconSelectors.add("icon action_emoji")}
            REQUEST_ICON_SHIFT->{iconImageUris["icon shift_key"]=uri;changedIconSelectors.add("icon shift_key")}
            REQUEST_ICON_SHIFT_PRESSED->{iconImageUris["icon shift_key_shifted"]=uri;changedIconSelectors.add("icon shift_key_shifted")}
            REQUEST_ICON_ENTER->{iconImageUris["icon enter_key"]=uri;changedIconSelectors.add("icon enter_key")}
            REQUEST_EXPORT->{
                try {
                    val temp = pendingExportFile ?: throw IllegalStateException("ملف التصدير المؤقت غير موجود؛ أعد محاولة التصدير")
                    val problems = validateThemeArchive(temp)
                    if (problems.isNotEmpty()) throw IllegalArgumentException("فشل فحص ZIP: " + problems.take(4).joinToString("؛ "))
                    (contentResolver.openOutputStream(uri) ?: throw IllegalArgumentException("تعذر فتح ملف التصدير للكتابة")).use { output -> FileInputStream(temp).use { input -> input.copyTo(output) } }
                    temp.delete(); pendingExportFile = null
                    toast("تم حفظ ZIP بعد نجاح فحص القواعد والأصول والخطوط والخلفية")
                } catch(e:Exception) { toast("فشل حفظ ZIP: ${e.message}") }
                return
            }
        }
        toast("تم اختيار الصورة");refreshPreview()
    }
    private fun importTheme(uri:Uri) {
        val entries = LinkedHashMap<String, ByteArray>()
        var totalBytes = 0L
        openInputStreamSafe(uri)?.use { input -> ZipInputStream(input).use { zip ->
            var e = zip.nextEntry
            var count = 0
            while (e != null) {
                count++
                if (count > 512) throw IllegalArgumentException("الثيم يحتوي على عدد ملفات أكبر من الحد الآمن (512)")
                val path = e.name.replace('\\', '/')
                if (path.startsWith("/") || path.split('/').any { it == ".." }) throw IllegalArgumentException("مسار غير آمن داخل ZIP: $path")
                if (!e.isDirectory) {
                    val bytes = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var entrySize = 0L
                    while (true) {
                        val n = zip.read(buffer)
                        if (n < 0) break
                        entrySize += n; totalBytes += n
                        if (entrySize > 32L*1024*1024 || totalBytes > 100L*1024*1024) throw IllegalArgumentException("حجم ملفات الثيم يتجاوز الحد الآمن")
                        bytes.write(buffer, 0, n)
                    }
                    if (entries.put(path, bytes.toByteArray()) != null) throw IllegalArgumentException("اسم ملف مكرر داخل ZIP: $path")
                }
                zip.closeEntry(); e = zip.nextEntry
            }
        } } ?: throw IllegalArgumentException("لا يمكن فتح الملف")
        val theme = entries["theme.txt"]?.toString(Charsets.UTF_8)?.removePrefix("\uFEFF")
            ?: throw IllegalArgumentException("لا يوجد theme.txt في جذر ZIP")
        // Keep all files and the exact source configuration for a loss-minimizing round trip.
        importedThemeEntries = entries
        importedThemeOriginalText = theme
        changedImageRoles.clear(); changedIconSelectors.clear(); backgroundImageChanged = false; fontChanged = false; regenerateImportedBorders = false
        keyImageUris.keys.forEach { keyImageUris[it] = null }
        iconImageUris.clear(); standardIconSelectors.forEach { iconImageUris[it] = null }
        bgImageUri = null; importedFontFile = null

        fun value(key:String):String? = Regex("(?m)^\\s*${Regex.escape(key)}\\s*=\\s*\"((?:\\\\.|[^\"])*)\"").find(theme)?.groupValues?.get(1)?.replace("\\\"", "\"")
        fun color(key:String, fallback:Int):Int { val v=value(key) ?: return fallback; return try { Color.parseColor(v.take(9).let { if (it.length == 9) it.substring(0,7) else it }) } catch(_:Exception){fallback} }
        themeName=value("name") ?: "Imported FUTO Theme"; if(::nameInput.isInitialized) nameInput.setText(themeName)
        accent=color("primary",accent); customBgColor=color("background",customBgColor); customKeyColor=color("keyboard_container",customKeyColor); keyText=color("on_keyboard_container",keyText); arabicText=keyText; latinText=keyText
        Regex("(?m)^\\s*roundedness\\s*=\\s*([0-9.]+)").find(theme.substringAfter("[options]", "" ).substringBefore("["))?.groupValues?.get(1)?.toFloatOrNull()?.let{corner=(it*50f).coerceIn(1f,50f);if(::radiusSeek.isInitialized)radiusSeek.progress=corner.toInt()}

        val borderRules = Regex("(?s)\\[\\[matchrules\\.border\\]\\](.*?)(?=\\[\\[|\\z)")
        val importedRoles = listOf("normal", "pressed", "functional", "action")
        for (role in importedRoles) {
            val rule = borderRules.findAll(theme).map { it.groupValues[1] }.firstOrNull { block ->
                Regex("(?m)^\\s*selector\\s*=\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1)?.trim() == role
            }
            val selectedAsset = rule?.let { Regex("(?m)^\\s*asset\\s*=\\s*\"([^\"]+)\"").find(it)?.groupValues?.get(1) }
            val bytes = selectedAsset?.let { entries[it] }
            if (bytes != null) {
                val safeFileName = (selectedAsset ?: "asset.bin").replace(Regex("[^A-Za-z0-9._-]"), "_")
                val f = File(cacheDir, "import_${role}_${safeFileName}"); f.writeBytes(bytes); keyImageUris[role] = Uri.fromFile(f)
            }
        }
        val iconRulePattern = Regex("(?s)\\[\\[matchrules\\.icon\\]\\](.*?)(?=\\[\\[|\\z)")
        for (selector in standardIconSelectors) {
            val block = iconRulePattern.findAll(theme).map { it.groupValues[1] }.firstOrNull { b -> Regex("(?m)^\\s*selector\\s*=\\s*\"([^\"]+)\"").find(b)?.groupValues?.get(1) == selector }
            val asset = block?.let { Regex("(?m)^\\s*asset\\s*=\\s*\"([^\"]+)\"").find(it)?.groupValues?.get(1) }
            val bytes = asset?.let { entries[it] }
            if (asset != null && bytes != null) { val f=File(cacheDir,"import_icon_${asset.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"),"_")}");f.writeBytes(bytes);iconImageUris[selector]=Uri.fromFile(f) }
        }
        val backgroundName=Regex("(?m)^\\s*image\\s*=\\s*\"([^\"]+)\"").find(theme.substringAfter("[options.background]", "").substringBefore("["))?.groupValues?.get(1)
        if(backgroundName!=null) entries[backgroundName]?.let{bytes->val f=File(cacheDir,"import_background_asset");f.writeBytes(bytes);bgImageUri=Uri.fromFile(f)}
        Regex("(?m)^\\s*opacity\\s*=\\s*([0-9.]+)").find(theme.substringAfter("[options.background]", "").substringBefore("["))?.groupValues?.get(1)?.toFloatOrNull()?.let { opacity -> if (::backgroundOpacity.isInitialized) backgroundOpacity.progress = (opacity.coerceIn(0f,1f)*100).toInt() }
        val fontName=Regex("(?m)^\\s*font\\s*=\\s*\"([^\"]+)\"").find(theme.substringAfter("[options.font]", "").substringBefore("["))?.groupValues?.get(1)
        if(fontName!=null && entries[fontName]!=null){val f=File(cacheDir,"import_font_${fontName.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"),"_")}");f.writeBytes(entries[fontName]!!);importedFontFile=f}
        refreshPreview(); toast("تم استيراد الثيم كاملاً: ${entries.size} ملفًا. ستُحفظ قواعده وأيقوناته وأصوله عند إعادة التصدير.")
    }

    private fun tomlQuoteSafe(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ") + "\""
    private fun tomlColor(value: Int): String = String.format(Locale.US, "#%02X%02X%02XFF", Color.red(value), Color.green(value), Color.blue(value))

    private fun replaceTopLevelToml(text: String, key: String, value: String): String {
        val pattern = Regex("(?m)^${Regex.escape(key)}\\s*=.*$")
        val existing = pattern.find(text)
        if (existing != null) return text.replaceRange(existing.range, "$key = $value")
        val firstSection = Regex("(?m)^\\s*\\[").find(text)
        return if (firstSection != null) text.replaceRange(firstSection.range, "$key = $value\n\n[") else text.trimEnd() + "\n$key = $value\n"
    }

    private fun replaceTomlSectionValue(text: String, section: String, key: String, value: String): String {
        val header = "[$section]"
        val start = text.indexOf(header)
        if (start < 0) return text
        val bodyStart = start + header.length
        val next = text.indexOf("\n[", bodyStart).let { if (it < 0) text.length else it }
        val body = text.substring(bodyStart, next)
        val pattern = Regex("(?m)^[ \t]*${Regex.escape(key)}[ \t]*=.*$")
        val existing = pattern.find(body)
        val newBody = if (existing != null) body.replaceRange(existing.range, "$key = $value") else body.trimEnd() + "\n$key = $value\n"
        return text.substring(0, bodyStart) + newBody + text.substring(next)
    }

    private fun selectorAssets(theme: String, role: String): Set<String> {
        val found = linkedSetOf<String>()
        val rulePattern = Regex("(?s)\\[\\[matchrules\\.border\\]\\](.*?)(?=\\[\\[|\\z)")
        for (block in rulePattern.findAll(theme).map { it.groupValues[1] }) {
            val selector = Regex("(?m)^\\s*selector\\s*=\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1)?.lowercase(Locale.US) ?: continue
            val matches = when (role) {
                "pressed" -> selector.contains("pressed")
                "functional" -> selector.contains("functional") && !selector.contains("pressed")
                "action" -> selector.contains("action") && !selector.contains("pressed")
                else -> selector.startsWith("normal") && !selector.contains("pressed") || selector == "normal"
            }
            if (matches) Regex("(?m)^\\s*asset\\s*=\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1)?.let { found.add(it) }
        }
        return found
    }

    private fun writeThemeZip(out:OutputStream,name:String) {
        val sourceEntries = importedThemeEntries
        val sourceText = importedThemeOriginalText
        if (sourceEntries != null && sourceText != null) {
            var theme: String = sourceText!!
            theme = replaceTopLevelToml(theme, "name", tomlQuoteSafe(name))
            theme = replaceTopLevelToml(theme, "description", tomlQuoteSafe("Edited offline with FUTO Theme Studio"))
            val p = currentPalette(); val bgc = customBgColor; val kc = customKeyColor; val ac = accent; val fg = keyText
            theme = replaceTopLevelToml(theme, "author", tomlQuoteSafe("Neon FUTO Theme Studio"))
            theme = replaceTopLevelToml(theme, "id", tomlQuoteSafe(themeId(name)))
            val colorValues = linkedMapOf(
                "primary" to ac, "on_primary" to fg, "primary_container" to kc, "on_primary_container" to fg, "inverse_primary" to ac,
                "secondary" to p[4], "on_secondary" to fg, "secondary_container" to blend(kc,p[4]), "on_secondary_container" to fg,
                "tertiary" to blend(ac,p[4]), "on_tertiary" to fg, "tertiary_container" to blend(kc,ac), "on_tertiary_container" to fg,
                "background" to bgc, "on_background" to fg, "surface" to bgc, "on_surface" to fg,
                "surface_variant" to blend(bgc,kc), "on_surface_variant" to fg, "surface_tint" to ac,
                "inverse_surface" to fg, "inverse_on_surface" to bgc, "error" to 0xFFC00100.toInt(), "on_error" to Color.WHITE,
                "error_container" to 0xFFFFDAD4.toInt(), "on_error_container" to 0xFF2A1613.toInt(), "outline" to ac,
                "outline_variant" to blend(ac,bgc), "scrim" to Color.BLACK, "surface_bright" to kc, "surface_dim" to bgc,
                "surface_container" to blend(bgc,kc), "surface_container_high" to blend(kc,ac),
                "surface_container_highest" to blend(kc,ac), "surface_container_low" to bgc, "surface_container_lowest" to Color.WHITE,
                "keyboard_surface" to bgc, "keyboard_surface_dim" to bgc, "keyboard_container" to kc,
                "keyboard_container_variant" to blend(kc,ac), "on_keyboard_container" to fg, "keyboard_press" to ac,
                "keyboard_container_pressed" to blend(kc,ac), "on_keyboard_container_pressed" to fg
            )
            for ((key, color) in colorValues) theme = replaceTomlSectionValue(theme, "colors", key, tomlQuoteSafe(tomlColor(color)))
            theme = replaceTomlSectionValue(theme, "options", "roundedness", String.format(Locale.US, "%.2f", (corner / 50f).coerceIn(0f, 1f)))
            val replacements = mutableMapOf<String, ByteArray>()
            if (regenerateImportedBorders) {
                val borderPattern = Regex("(?s)\\[\\[asset\\.border\\]\\](.*?)(?=\\[\\[|\\z)")
                val borderAssets = borderPattern.findAll(sourceText).mapNotNull { block ->
                    Regex("(?m)^\\s*name\\s*=\\s*\"([^\"]+)\"").find(block.value)?.groupValues?.get(1)
                }.distinct()
                val pressedAssets = selectorAssets(sourceText, "pressed")
                val functionalAssets = selectorAssets(sourceText, "functional")
                val actionAssets = selectorAssets(sourceText, "action")
                for ((index, asset) in borderAssets.withIndex()) {
                    val lower = asset.lowercase(Locale.US)
                    val role = when {
                        asset in pressedAssets || lower.contains("press") -> "pressed"
                        asset in actionAssets || lower.contains("action") -> "action"
                        asset in functionalAssets || lower.contains("function") || lower.contains("space") || lower.contains("dark") -> "functional"
                        else -> "normal"
                    }
                    val base = when (role) {
                        "pressed" -> blend(kc, ac)
                        "action" -> blend(kc, ac)
                        "functional" -> blend(kc, p[4])
                        else -> kc
                    }
                    val stroke = when (role) { "functional" -> p[4]; "pressed", "action" -> ac; else -> ac }
                    replacements[asset] = makePng(base, stroke, corner, (shapeIndex + index * 7) % 500)
                }
            }
            if (fontChanged) {
                val selectedFont = fontFiles[fontIndex]
                theme = if (theme.contains("[options.font]")) replaceTomlSectionValue(theme, "options.font", "font", tomlQuoteSafe(selectedFont)) else theme.trimEnd() + "\n\n[options.font]\nfont = ${tomlQuoteSafe(selectedFont)}\n"
                replacements[selectedFont] = assets.open("fonts/$selectedFont").use { it.readBytes() }
            }
            if (theme.contains("[options.background]")) {
                theme = replaceTomlSectionValue(theme, "options.background", "opacity", String.format(Locale.US, "%.2f", if (::backgroundOpacity.isInitialized) backgroundOpacity.progress / 100f else 1f))
            }
            for (role in changedImageRoles) {
                val uri = keyImageUris[role] ?: continue
                val bytes = readImagePng(uri)
                val names = selectorAssets(sourceText, role)
                for (asset in names) if (sourceEntries.containsKey(asset)) replacements[asset] = bytes
                // If this theme has only a generic rule that the selector matcher did not classify, use the exact role rule.
                if (names.isEmpty()) {
                    val roleRule = Regex("(?s)\\[\\[matchrules\\.border\\]\\](.*?)(?=\\[\\[|\\z)").findAll(sourceText).map { it.groupValues[1] }.firstOrNull { block -> Regex("(?m)^\\s*selector\\s*=\\s*\"${Regex.escape(role)}\"\\s*$").containsMatchIn(block) }
                    val asset = roleRule?.let { Regex("(?m)^\\s*asset\\s*=\\s*\"([^\"]+)\"").find(it)?.groupValues?.get(1) }
                    if (asset != null && sourceEntries.containsKey(asset)) replacements[asset] = bytes
                }
            }
            val iconRulePattern = Regex("(?s)\\[\\[matchrules\\.icon\\]\\](.*?)(?=\\[\\[|\\z)")
            for (selector in changedIconSelectors) {
                val uri = iconImageUris[selector] ?: continue
                val bytes = readImagePng(uri)
                for (block in iconRulePattern.findAll(sourceText).map { it.groupValues[1] }) {
                    val foundSelector = Regex("(?m)^\\s*selector\\s*=\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1)
                    if (foundSelector == selector) {
                        val asset = Regex("(?m)^\\s*asset\\s*=\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1)
                        if (asset != null && sourceEntries.containsKey(asset)) replacements[asset] = bytes
                    }
                }
            }
            if (backgroundImageChanged && bgImageUri != null) {
                val newName = "background_image_asset.png"
                val oldSection = theme.substringAfter("[options.background]", "").substringBefore("[")
                val oldName = Regex("(?m)^\\s*image\\s*=\\s*\"([^\"]+)\"").find(oldSection)?.groupValues?.get(1)
                if (oldName != null && sourceEntries.containsKey(oldName)) {
                    theme = Regex("(?m)^(\\s*image\\s*=\\s*)\"${Regex.escape(oldName)}\"").replace(theme) { match -> match.groupValues[1] + "\"" + newName + "\"" }
                    replacements.remove(oldName)
                } else {
                    theme = if (theme.contains("[options.background]")) replaceTomlSectionValue(theme, "options.background", "image", tomlQuoteSafe(newName)) else theme.trimEnd() + "\n\n[options.background]\nimage = \"$newName\"\nopacity = 1.0\n"
                }
                replacements[newName] = readImagePng(bgImageUri!!)
            }
            val replacedBorderAssets = replacements.keys.toSet()
            if (replacedBorderAssets.isNotEmpty()) {
                val assetPattern = Regex("(?s)\\[\\[asset\\.border\\]\\](.*?)(?=\\[\\[|\\z)")
                theme = assetPattern.replace(theme) { match ->
                    val block = match.value
                    val assetName = Regex("(?m)^\\s*name\\s*=\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1)
                    if (assetName != null && assetName in replacedBorderAssets) {
                        Regex("(?m)^[ \t]*background_tint[ \t]*=.*$").replace(block) { "background_tint = \"#FFFFFFFF\"" }
                            .let { updated -> Regex("(?m)^[ \t]*foreground_tint[ \t]*=.*$").replace(updated) { "foreground_tint = \"#FFFFFFFF\"" } }
                    } else block
                }
            }
            ZipOutputStream(out).use { zip ->
                val emitted = hashSetOf<String>()
                for ((entryName, originalBytes) in sourceEntries) {
                    if (entryName == "theme.txt") continue
                    if (!emitted.add(entryName)) continue
                    val bytes = replacements[entryName] ?: originalBytes
                    zip.putNextEntry(ZipEntry(entryName)); zip.write(bytes); zip.closeEntry()
                }
                // A custom background can introduce a new path that wasn't in the original archive.
                for ((entryName, bytes) in replacements) if (emitted.add(entryName)) { zip.putNextEntry(ZipEntry(entryName)); zip.write(bytes); zip.closeEntry() }
                zip.putNextEntry(ZipEntry("theme.txt")); zip.write(theme.toByteArray(Charsets.UTF_8)); zip.closeEntry()
            }
            return
        }

        writeNewFullThemeZip(out, name)
    }

    private fun writeNewFullThemeZip(out: OutputStream, name: String) {
        var theme = assets.open("templates/advanced_theme_template.txt").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val p = currentPalette(); val bgc = customBgColor; val kc = customKeyColor; val ac = accent; val fg = keyText
        theme = replaceTopLevelToml(theme, "name", tomlQuoteSafe(name))
        theme = replaceTopLevelToml(theme, "author", tomlQuoteSafe("FUTO Theme Studio Offline"))
        theme = replaceTopLevelToml(theme, "id", tomlQuoteSafe(themeId(name)))
        theme = replaceTopLevelToml(theme, "version", "1")
        theme = replaceTopLevelToml(theme, "description", tomlQuoteSafe("Created offline with full Advanced Theme asset template"))
        val colorValues = linkedMapOf(
            "primary" to ac, "on_primary" to fg, "primary_container" to kc, "on_primary_container" to fg, "inverse_primary" to ac,
            "secondary" to p[4], "on_secondary" to fg, "secondary_container" to blend(kc,p[4]), "on_secondary_container" to fg,
            "tertiary" to blend(ac,p[4]), "on_tertiary" to fg, "tertiary_container" to blend(kc,ac), "on_tertiary_container" to fg,
            "background" to bgc, "on_background" to fg, "surface" to bgc, "on_surface" to fg,
            "surface_variant" to blend(bgc,kc), "on_surface_variant" to fg, "surface_tint" to ac,
            "inverse_surface" to fg, "inverse_on_surface" to bgc, "error" to 0xFFC00100.toInt(), "on_error" to Color.WHITE,
            "error_container" to 0xFFFFDAD4.toInt(), "on_error_container" to 0xFF2A1613.toInt(), "outline" to ac,
            "outline_variant" to blend(ac,bgc), "scrim" to Color.BLACK, "surface_bright" to kc, "surface_dim" to bgc,
            "surface_container" to blend(bgc,kc), "surface_container_high" to blend(kc,ac),
            "surface_container_highest" to blend(kc,ac), "surface_container_low" to bgc, "surface_container_lowest" to Color.WHITE,
            "keyboard_surface" to bgc, "keyboard_surface_dim" to bgc, "keyboard_container" to kc,
            "keyboard_container_variant" to blend(kc,ac), "on_keyboard_container" to fg, "keyboard_press" to ac,
            "keyboard_container_pressed" to blend(kc,ac), "on_keyboard_container_pressed" to fg
        )
        for ((key, color) in colorValues) theme = replaceTomlSectionValue(theme, "colors", key, tomlQuoteSafe(tomlColor(color)))
        theme = replaceTomlSectionValue(theme, "options", "roundedness", String.format(Locale.US,"%.2f",(corner/50f).coerceIn(0f,1f)))
        val exportFontName = fontFiles[fontIndex]
        theme = replaceTomlSectionValue(theme, "options.font", "font", tomlQuoteSafe(exportFontName))
        if (bgImageUri == null) {
            theme = removeTomlSection(theme, "options.background")
        } else {
            theme = replaceTomlSectionValue(theme, "options.background", "image", tomlQuoteSafe("background_image_asset.png"))
            theme = replaceTomlSectionValue(theme, "options.background", "opacity", String.format(Locale.US,"%.2f",if(::backgroundOpacity.isInitialized)backgroundOpacity.progress/100f else 1f))
        }

        val borderPattern = Regex("(?s)\\[\\[asset\\.border\\]\\](.*?)(?=\\[\\[|\\z)")
        val borderNames = borderPattern.findAll(theme).mapNotNull { block -> Regex("(?m)^\\s*name\\s*=\\s*\"([^\"]+)\"").find(block.value)?.groupValues?.get(1) }.toList()
        val roleAssets = mapOf(
            "normal" to selectorAssets(theme,"normal"), "pressed" to selectorAssets(theme,"pressed"),
            "functional" to selectorAssets(theme,"functional"), "action" to selectorAssets(theme,"action")
        )
        fun roleFor(asset: String): String {
            val lower = asset.lowercase(Locale.US)
            return when {
                roleAssets["pressed"]?.contains(asset) == true || lower.contains("press") -> "pressed"
                roleAssets["action"]?.contains(asset) == true || lower.contains("action") -> "action"
                roleAssets["functional"]?.contains(asset) == true || lower.contains("function") || lower.contains("space") || lower.contains("dark") -> "functional"
                else -> "normal"
            }
        }
        theme = borderPattern.replace(theme) { match ->
            var block = match.value
            block = Regex("(?m)^[ \t]*background_tint[ \t]*=.*$").replace(block) { "background_tint = \"#FFFFFFFF\"" }
            block = Regex("(?m)^[ \t]*foreground_tint[ \t]*=.*$").replace(block) { "foreground_tint = \"#FFFFFFFF\"" }
            block = Regex("(?m)^[ \t]*padding[ \t]*=.*$").replace(block) { "padding = [0.0, 0.0, 0.0, 0.0]" }
            block = Regex("(?m)^[ \t]*slicing[ \t]*=.*$").replace(block) { "slicing = [0.22, 0.22, 0.78, 0.78]" }
            block = Regex("(?m)^[ \t]*gap[ \t]*=.*$").replace(block) { "gap = [0.0, 0.0, 0.0, 0.0]" }
            block
        }
        val iconRules = listOf("icon delete_key" to "Icon-backspace.png", "icon action_emoji" to "Icon-emoji.png", "icon shift_key_shifted" to "Icon-shift-press.png", "icon shift_key" to "Icon-shift.png", "icon enter_key" to "Icon-enter.png")
        ZipOutputStream(out).use { zip ->
            val written = hashSetOf<String>()
            fun put(entryName: String, bytes: ByteArray) {
                if (!written.add(entryName)) return
                zip.putNextEntry(ZipEntry(entryName)); zip.write(bytes); zip.closeEntry()
            }
            put("theme.txt", theme.toByteArray(Charsets.UTF_8))
            put("README.txt", "Created offline by FUTO Theme Studio. Includes full starter selectors, border/icon asset declarations, 15 border images and 5 icon assets.\n".toByteArray())
            put(exportFontName, assets.open("fonts/$exportFontName").use { it.readBytes() })
            for ((index, asset) in borderNames.withIndex()) {
                val role = roleFor(asset)
                val custom = if (role in changedImageRoles) keyImageUris[role] else null
                val bytes = custom?.let { readImagePng(it) } ?: run {
                    val base = when (role) {
                        "pressed" -> blend(kc,ac)
                        "action" -> blend(kc,ac)
                        "functional" -> blend(kc,p[4])
                        else -> kc
                    }
                    val stroke = when (role) { "functional" -> p[4]; "pressed", "action" -> ac; else -> ac }
                    makePng(base,stroke,corner,(shapeIndex+index*7)%500)
                }
                put(asset,bytes)
            }
            for ((selector, asset) in iconRules) {
                val bytes = if (selector in changedIconSelectors) iconImageUris[selector]?.let { readImagePng(it) } else null
                put(asset,bytes ?: makeIconPng(selector,fg))
            }
            if (bgImageUri != null) put("background_image_asset.png",readImagePng(bgImageUri!!))
        }
    }

    private fun inspectThemeArchive(file: File): String {
        ZipFile(file).use { zip ->
            val names = linkedSetOf<String>()
            val e = zip.entries()
            var theme = ""
            while (e.hasMoreElements()) {
                val entry = e.nextElement()
                if (!entry.isDirectory) names.add(entry.name)
                if (entry.name == "theme.txt") theme = zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
            fun count(section: String, kind: String) = Regex("(?s)\\[\\[${Regex.escape(section)}\\.${Regex.escape(kind)}\\]\\]").findAll(theme).count()
            val font = Regex("(?s)\\[options\\.font\\].*?font\\s*=\\s*\"([^\"]+)\"").find(theme)?.groupValues?.get(1) ?: "غير محدد"
            val background = Regex("(?s)\\[options\\.background\\].*?image\\s*=\\s*\"([^\"]+)\"").find(theme)?.groupValues?.get(1) ?: "بدون صورة محددة"
            return "الملفات: ${names.size}\nقواعد الأزرار: ${count("matchrules", "border")}\nأصول الأزرار المعلنة: ${count("asset", "border")}\nقواعد الأيقونات: ${count("matchrules", "icon")}\nأصول الأيقونات المعلنة: ${count("asset", "icon")}\nالخط: $font\nالخلفية: $background"
        }
    }

    /** Structural export gate. It catches broken ZIPs and missing TOML-to-file references before a ZIP is saved. */
    private fun validateThemeArchive(file: File): List<String> {
        val errors = mutableListOf<String>()
        try {
            ZipFile(file).use { zip ->
                val names = linkedSetOf<String>()
                val enumeration = zip.entries()
                var themeText: String? = null
                while (enumeration.hasMoreElements()) {
                    val entry = enumeration.nextElement()
                    if (entry.isDirectory) continue
                    if (!names.add(entry.name)) errors.add("اسم ملف مكرر: ${entry.name}")
                    val normalized = entry.name.replace('\\', '/')
                    if (normalized.startsWith("/") || normalized.split('/').any { it == ".." }) errors.add("مسار غير آمن: ${entry.name}")
                    if (entry.name == "theme.txt") themeText = zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).use { it.readText().removePrefix("\uFEFF") }
                    val lowerName = entry.name.lowercase(Locale.US)
                    if (lowerName.endsWith(".png") || lowerName.endsWith(".webp") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")) {
                        val imageBytes = zip.getInputStream(entry).use { it.readBytes() }
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
                        if (options.outWidth < 1 || options.outHeight < 1) errors.add("صورة تالفة أو غير مدعومة: ${entry.name}")
                    }
                    // Force ZIP CRC/decompression checks for every entry, including preserved assets.
                    zip.getInputStream(entry).use { input -> val buffer = ByteArray(8192); while (input.read(buffer) >= 0) { } }
                }
                val theme = themeText ?: run { errors.add("ملف theme.txt مفقود من جذر ZIP"); return errors }
                for (field in listOf("name", "author", "id", "description")) {
                    if (!Regex("(?m)^\\s*${Regex.escape(field)}\\s*=\\s*\".+\"\\s*$").containsMatchIn(theme)) errors.add("بيانات الثيم ناقصة: $field")
                }
                if (!Regex("(?m)^\\s*version\\s*=\\s*\\d+\\s*$").containsMatchIn(theme)) errors.add("version مفقود أو غير صحيح")
                fun blocks(kind: String, section: String): List<String> = Regex("(?s)\\[\\[${Regex.escape(section)}\\.${Regex.escape(kind)}\\]\\](.*?)(?=\\[\\[|\\z)").findAll(theme).map { it.groupValues[1] }.toList()
                fun field(block: String, key: String): String? = Regex("(?m)^\\s*${Regex.escape(key)}\\s*=\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1)
                val declared = linkedSetOf<String>()
                for (kind in listOf("border", "icon")) {
                    for (block in blocks(kind, "asset")) field(block, "name")?.let { declared.add(it) }
                    for ((index, block) in blocks(kind, "matchrules").withIndex()) {
                        val selector = field(block, "selector")
                        val asset = field(block, "asset")
                        if (selector.isNullOrBlank()) errors.add("قاعدة $kind رقم ${index+1} بلا selector")
                        if (asset.isNullOrBlank()) errors.add("قاعدة $kind رقم ${index+1} بلا asset")
                        else {
                            if (asset !in names) errors.add("ملف أصل غير موجود: $asset")
                            if (asset !in declared && field(block, "asset") != null) {
                                // Declaration checks are repeated after both asset tables have been scanned below.
                            }
                        }
                    }
                }
                for (kind in listOf("border", "icon")) for ((index, block) in blocks(kind, "asset").withIndex()) {
                    val asset = field(block, "name")
                    if (asset.isNullOrBlank()) errors.add("تعريف أصل $kind رقم ${index+1} بلا name")
                    else if (asset !in names) errors.add("تعريف أصل يشير إلى ملف مفقود: $asset")
                }
                for (kind in listOf("border", "icon")) for (block in blocks(kind, "matchrules")) {
                    val asset = field(block, "asset")
                    if (asset != null && asset !in declared) errors.add("الأصل المشار إليه غير معلن: $asset")
                }
                val font = Regex("(?s)\\[options\\.font\\](.*?)(?=\\n\\[|\\z)").find(theme)?.groupValues?.get(1)?.let { field(it, "font") }
                if (font != null && font !in names) errors.add("ملف الخط المختار غير موجود: $font")
                val backgroundSection = Regex("(?s)\\[options\\.background\\](.*?)(?=\\n\\[|\\z)").find(theme)?.groupValues?.get(1)
                val background = backgroundSection?.let { field(it, "image") }
                if (background != null && background !in names) errors.add("ملف الخلفية المختار غير موجود: $background")
            }
        } catch (e: Exception) {
            errors.add("تعذرت قراءة ZIP أو فحصه: ${e.message ?: e.javaClass.simpleName}")
        }
        return errors.distinct()
    }

    private fun removeTomlSection(text: String, section: String): String {
        val header = "[$section]"
        val start = text.indexOf(header)
        if (start < 0) return text
        val next = text.indexOf("\n[", start + header.length)
        return if (next < 0) text.substring(0,start).trimEnd() + "\n" else text.substring(0,start) + text.substring(next + 1)
    }

    private fun makeIconPng(selector: String, color: Int): ByteArray {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.STROKE; strokeWidth = 9f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        when {
            selector.contains("delete_key") -> {
                val path = Path().apply { moveTo(45f,28f); lineTo(100f,28f); lineTo(100f,100f); lineTo(45f,100f); lineTo(14f,64f); close() }
                canvas.drawPath(path, paint); canvas.drawLine(53f,48f,82f,80f,paint); canvas.drawLine(82f,48f,53f,80f,paint)
            }
            selector.contains("action_emoji") -> {
                canvas.drawCircle(64f,64f,43f,paint); paint.style=Paint.Style.FILL; canvas.drawCircle(49f,54f,5f,paint); canvas.drawCircle(79f,54f,5f,paint); paint.style=Paint.Style.STROKE; canvas.drawArc(42f,48f,86f,88f,20f,140f,false,paint)
            }
            selector.contains("shift_key") -> {
                val path = Path().apply { moveTo(64f,18f); lineTo(22f,60f); lineTo(43f,60f); lineTo(43f,104f); lineTo(85f,104f); lineTo(85f,60f); lineTo(106f,60f); close() }
                canvas.drawPath(path,paint); if (selector.contains("shifted")) { paint.style=Paint.Style.FILL; canvas.drawCircle(64f,114f,4f,paint) }
            }
            selector.contains("enter_key") -> {
                val path = Path().apply { moveTo(104f,30f); lineTo(104f,78f); lineTo(26f,78f); lineTo(48f,56f); moveTo(26f,78f); lineTo(48f,100f) }
                canvas.drawPath(path,paint)
            }
        }
        val stream=ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.PNG,100,stream); bitmap.recycle(); return stream.toByteArray()
    }

    private fun imageOrGenerated(role:String, fallback:ByteArray):ByteArray { val u=keyImageUris[role] ?: return fallback; return try { readImagePng(u) } catch (_:Exception) { fallback } }
    private fun openInputStreamSafe(uri: Uri): InputStream? {
        return if (uri.scheme == "file") {
            val path = uri.path ?: throw IllegalArgumentException("مسار الملف غير صالح")
            FileInputStream(File(path))
        } else {
            contentResolver.openInputStream(uri)
        }
    }

    private fun readImagePng(uri:Uri):ByteArray {
        val imageBytes = openInputStreamSafe(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > 32L * 1024 * 1024) throw IllegalArgumentException("حجم الصورة يتجاوز 32 ميغابايت")
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: throw IllegalArgumentException("تعذرت قراءة الصورة")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
        if (bounds.outWidth < 1 || bounds.outHeight < 1) throw IllegalArgumentException("تنسيق الصورة غير مدعوم أو الملف تالف")
        var sample = 1
        while (bounds.outWidth / sample > 2048 || bounds.outHeight / sample > 2048) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
            ?: throw IllegalArgumentException("تعذر فك ترميز الصورة")
        val stream=ByteArrayOutputStream()
        try { bitmap.compress(Bitmap.CompressFormat.PNG,100,stream) } finally { bitmap.recycle() }
        return stream.toByteArray()
    }
    private fun makePng(bgColor:Int,strokeColor:Int,r:Float,shape:Int):ByteArray {
        val idx=shape.coerceIn(0,499); val family=idx/100; val variant=idx%100
        val bmp=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888); val c=Canvas(bmp); val p=Paint(Paint.ANTI_ALIAS_FLAG)
        val path=shapePath(idx,0f,r)
        c.save(); c.rotate(((idx%100)/10-4.5f)*0.55f,64f,64f)
        if(family==2){
            p.color=strokeColor; p.style=Paint.Style.STROKE; p.strokeWidth=8f; p.maskFilter=BlurMaskFilter(7f,BlurMaskFilter.Blur.NORMAL); c.drawPath(path,p); p.maskFilter=null
        }
        if(family==3){
            p.color=Color.argb(110,0,0,0); p.style=Paint.Style.FILL; c.save(); c.translate(0f,7f); c.drawPath(path,p); c.restore()
        }
        p.style=Paint.Style.FILL; p.shader=LinearGradient(0f,0f,128f,128f,bgColor,blend(bgColor,strokeColor),Shader.TileMode.CLAMP); c.drawPath(path,p)
        p.shader=null; p.style=Paint.Style.STROKE; p.strokeWidth=if(family==2) (2.2f+(variant%4)) else if(family==3) 2.5f else 3f; p.color=strokeColor; c.drawPath(shapePath(idx,2f,r),p)
        if(family==2 || variant%5==2){ p.strokeWidth=1.3f; p.color=Color.argb(180,255,255,255); c.drawPath(shapePath(idx,7f,r),p) }
        if(family==3 || variant%5==3){ p.style=Paint.Style.FILL; p.shader=LinearGradient(0f,0f,0f,65f,Color.argb(115,255,255,255),Color.argb(0,255,255,255),Shader.TileMode.CLAMP); c.drawPath(shapePath(idx,8f,r),p); p.shader=null }
        if(variant%10==8){p.style=Paint.Style.STROKE;p.strokeWidth=1f;p.color=Color.argb(75,255,255,255);for(y in 20..112 step 12)c.drawLine(12f,y.toFloat(),116f,y.toFloat(),p)}
        c.restore(); val stream=ByteArrayOutputStream();bmp.compress(Bitmap.CompressFormat.PNG,100,stream);bmp.recycle();return stream.toByteArray()
    }
    private fun shapePath(shape:Int,inset:Float,radius:Float):Path {
        val idx=shape.coerceIn(0,499); val family=idx/100; val v=idx%100
        val l=4f+inset; val t=4f+inset; val rr=124f-inset; val b=124f-inset; val w=rr-l; val h=b-t
        fun points(vararg xy:Float)=Path().apply{var i=0;while(i<xy.size){if(i==0)moveTo(xy[i],xy[i+1])else lineTo(xy[i],xy[i+1]);i+=2};close()}
        val rad=(radius*(0.45f+(v%5)*0.14f)).coerceIn(1f,48f)
        if(family==0){
            return when(v%10){
                0->Path().apply{addRoundRect(RectF(l,t,rr,b),rad,rad,Path.Direction.CW)}
                1->Path().apply{addRoundRect(RectF(l,t,rr,b),h*.47f,h*.47f,Path.Direction.CW)}
                2->Path().apply{addOval(RectF(l,t,rr,b),Path.Direction.CW)}
                3->points(l+v%13,t,rr-v%9,t,rr,t+v%15,rr,b-v%12,rr-v%8,b,l+v%9,b,l,b-v%10,l,t+v%11)
                4->points(l,t,rr,t,rr,b,l,b)
                5->points(l+12,t,rr-12,t,rr,t+12,rr,b-12,rr-12,b,l+12,b,l,b-12,l,t+12)
                6->points(l+w*.5f,t,rr,t+h*.5f,l+w*.5f,b,l,t+h*.5f)
                7->Path().apply{addRoundRect(RectF(l,t+h*.12f,rr,b-h*.12f),rad,rad,Path.Direction.CW)}
                8->Path().apply{addRoundRect(RectF(l+w*.08f,t,rr-w*.08f,b),rad,rad,Path.Direction.CW)}
                else->points(l+w*.18f,t,rr-w*.18f,t,rr,b,l,b)
            }
        }
        if(family==1){
            val sides= when(v%10){0->3;1->4;2->5;3->6;4->7;5->8;6->9;7->10;8->12;else->4}
            val path=Path(); val rot=(v%8)*PI/16-PI/2
            for(i in 0 until sides){val a=2*PI*i/sides+rot;val x=64f+cos(a).toFloat()*(57f-inset);val y=64f+sin(a).toFloat()*(57f-inset);if(i==0)path.moveTo(x,y)else path.lineTo(x,y)};path.close()
            if(v%10==9)return points(l+w*.5f,t,rr,t+h*.36f,l+w*.78f,b,l+w*.22f,b,l,t+h*.36f)
            return path
        }
        if(family==2){
            return when(v%8){
                0->Path().apply{addRoundRect(RectF(l,t,rr,b),rad,rad,Path.Direction.CW)}
                1->points(l+16,t,rr-16,t,rr,t+16,rr,b-16,rr-16,b,l+16,b,l,b-16,l,t+16)
                2->points(l+w*.5f,t,rr,t+h*.5f,l+w*.5f,b,l,t+h*.5f)
                3->points(l,t+h*.15f,l+w*.22f,t+h*.15f,l+w*.3f,t,rr-w*.15f,t,rr,t+h*.18f,rr,b-h*.15f,rr-w*.18f,b,l+w*.2f,b,l,b-h*.2f)
                4->Path().apply{addRoundRect(RectF(l+w*.08f,t+h*.08f,rr-w*.08f,b-h*.08f),rad,rad,Path.Direction.CW)}
                5->points(l+20,t,rr-20,t,rr,t+h*.25f,rr,b-h*.25f,rr-20,b,l+20,b,l,b-h*.25f,l,t+h*.25f)
                6->points(l,t+h*.12f,l+w*.18f,t+h*.12f,l+w*.18f,t,rr-w*.18f,t,rr-w*.18f,t+h*.12f,rr,t+h*.12f,rr,b-h*.12f,rr-w*.18f,b-h*.12f,rr-w*.18f,b,l+w*.18f,b,l+w*.18f,b-h*.12f,l,b-h*.12f)
                else->Path().apply{addRoundRect(RectF(l,t,rr,b),h*.45f,h*.45f,Path.Direction.CW)}
            }
        }
        if(family==3){
            return when(v%8){
                0->Path().apply{addRoundRect(RectF(l,t,rr,b),rad,rad,Path.Direction.CW)}
                1->Path().apply{addRoundRect(RectF(l+w*.08f,t+h*.08f,rr-w*.08f,b-h*.08f),rad,rad,Path.Direction.CW)}
                2->Path().apply{addOval(RectF(l,t,rr,b),Path.Direction.CW)}
                3->points(l+12,t,rr-12,t,rr,t+12,rr,b-12,rr-12,b,l+12,b,l,b-12,l,t+12)
                4->points(l+w*.5f,t,rr,t+h*.35f,rr-w*.12f,b,l+w*.12f,b,l,t+h*.35f)
                5->points(l+w*.25f,t,rr-w*.25f,t,rr,b,l,b)
                6->Path().apply{addRoundRect(RectF(l,t,rr,b),rad*.55f,rad*.55f,Path.Direction.CW)}
                else->points(l,t,rr,t,rr-w*.12f,b,l+w*.12f,b)
            }
        }
        // Creative family: star, heart, shield, arrow, leaf, wave, crown, hex and abstract cutouts.
        return when(v%10){
            0->Path().apply{for(i in 0 until 10){val a=PI*i/5-PI/2;val r0=if(i%2==0)57f-inset else 27f-inset;val x=64f+cos(a).toFloat()*r0;val y=64f+sin(a).toFloat()*r0;if(i==0)moveTo(x,y)else lineTo(x,y)};close()}
            1->points(l+w*.5f,t+h*.15f,rr,t+h*.42f,rr-w*.08f,b,l+w*.5f,b-h*.18f,l,t+h*.42f)
            2->points(l+w*.5f,t,rr,t+h*.25f,rr-w*.12f,b,l+w*.12f,b,l,t+h*.25f)
            3->points(l,t+h*.2f,l+w*.65f,t+h*.2f,l+w*.65f,t,rr,t+h*.5f,l+w*.65f,b,l+w*.65f,b-h*.2f,l,b-h*.2f)
            4->points(l+w*.5f,t,rr,t+h*.5f,l+w*.5f,b,l,t+h*.5f)
            5->points(l+w*.15f,t,rr-w*.15f,t,rr,b-h*.2f,l+w*.7f,b,l,b-h*.2f)
            6->points(l,t+h*.15f,l+w*.25f,t+h*.15f,l+w*.35f,t,rr-w*.1f,t,rr,b,l+w*.1f,b)
            7->points(l+w*.5f,t,rr,t+h*.35f,rr-w*.15f,b,l+w*.15f,b,l,t+h*.35f)
            8->Path().apply{moveTo(l,t+h*.2f);cubicTo(l+w*.2f,t-h*.1f,l+w*.3f,b+h*.1f,l+w*.5f,t+h*.5f);cubicTo(l+w*.7f,t-h*.1f,l+w*.8f,b+h*.1f,rr,t+h*.2f);lineTo(rr,b);lineTo(l,b);close()}
            else->points(l+w*.2f,t,rr-w*.2f,t,rr,b-h*.25f,l+w*.7f,b,l,b-h*.25f)
        }
    }
    private fun polygon(cx:Float,cy:Float,r:Float,n:Int)=Path().apply{for(i in 0 until n){val a=PI*2*i/n-PI/2;val x=cx+cos(a).toFloat()*r;val y=cy+sin(a).toFloat()*r;if(i==0)moveTo(x,y)else lineTo(x,y)};close()}
    private fun polygonDiamond()=Path().apply{moveTo(64f,3f);lineTo(125f,64f);lineTo(64f,125f);lineTo(3f,64f);close()}
    private fun blend(a:Int,b:Int)=Color.rgb((Color.red(a)+Color.red(b))/2,(Color.green(a)+Color.green(b))/2,(Color.blue(a)+Color.blue(b))/2)
    private fun tomlQuote(value:String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ") + "\""
    private fun hex(c:Int)=String.format(Locale.US,"#%06X",0xFFFFFF and c)
    private fun safeName(s:String)=s.replace(Regex("[^A-Za-z0-9._-]+"),"_").trim('_').ifBlank{"FUTO_Theme"}
    private fun themeId(name: String): String {
        val slug = safeName(name).lowercase(Locale.US)
        val hash = Integer.toHexString(name.trim().lowercase(Locale.US).hashCode())
        return "com.futo.themestudio.${slug}_$hash"
    }
    private fun entry(z:ZipOutputStream,n:String,b:ByteArray){z.putNextEntry(ZipEntry(n));z.write(b);z.closeEntry()}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    companion object { const val REQUEST_EXPORT=44;const val REQUEST_BACKGROUND=45;const val REQUEST_IMPORT=50;const val REQUEST_NORMAL=46;const val REQUEST_FUNCTIONAL=47;const val REQUEST_ACTION=48;const val REQUEST_PRESSED=49;const val REQUEST_ICON_BACKSPACE=51;const val REQUEST_ICON_EMOJI=52;const val REQUEST_ICON_SHIFT=53;const val REQUEST_ICON_SHIFT_PRESSED=54;const val REQUEST_ICON_ENTER=55 }
}

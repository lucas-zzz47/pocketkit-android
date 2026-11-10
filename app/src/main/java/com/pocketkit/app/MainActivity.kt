package com.pocketkit.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val purple = Color.rgb(102, 87, 245)
    private val ink = Color.rgb(25, 26, 42)
    private val muted = Color.rgb(126, 129, 146)
    private val bg = Color.rgb(246, 247, 251)
    private lateinit var content: LinearLayout
    private var page = "home"
    private var selectedImage: Uri? = null
    private var imageLabel = ""
    private var quality = 75
    private var qrBitmap: Bitmap? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val prefs by lazy { getSharedPreferences("pocketkit", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor = bg; window.navigationBarColor = bg; showHome() }
    override fun onDestroy() { executor.shutdown(); super.onDestroy() }
    override fun onBackPressed() { if (page != "home") showHome() else super.onBackPressed() }
    private fun dp(n: Int) = (n * resources.displayMetrics.density + .5f).toInt()
    private fun round(color: Int, radius: Int = 18, stroke: Int? = null) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); if (stroke != null) setStroke(dp(1), stroke) }
    private fun label(text: String, size: Float, color: Int = ink, bold: Boolean = false) = TextView(this).apply { this.text = text; textSize = size; setTextColor(color); if (bold) setTypeface(typeface, Typeface.BOLD); includeFontPadding = true }
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    private fun gap(parent: LinearLayout, height: Int) { parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height))) }
    private fun pad(view: View, l: Int, t: Int = l, r: Int = l, b: Int = t) { view.setPadding(dp(l), dp(t), dp(r), dp(b)) }
    private fun button(title: String, onClick: () -> Unit): TextView = label(title, 15f, Color.WHITE, true).apply { gravity = Gravity.CENTER; minHeight = dp(50); background = round(purple, 14); isClickable = true; isFocusable = true; setOnClickListener { onClick() } }
    private fun subtleButton(title: String, onClick: () -> Unit): TextView = label(title, 14f, purple, true).apply { gravity = Gravity.CENTER; minHeight = dp(48); background = round(Color.rgb(238, 236, 255), 14); isClickable = true; isFocusable = true; setOnClickListener { onClick() } }
    private fun edit(hintText: String, lines: Int = 1): EditText = EditText(this).apply { hint = hintText; textSize = 15f; setTextColor(ink); setHintTextColor(Color.rgb(166, 168, 181)); background = round(Color.WHITE, 16, Color.rgb(229, 230, 238)); pad(this, 16); gravity = Gravity.TOP; minLines = lines; if (lines > 1) inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES else setSingleLine(true) }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    private fun layout(title: String? = null, subtitle: String? = null) {
        val scroll = ScrollView(this).apply { isFillViewport = true; setBackgroundColor(bg) }
        content = column().apply { pad(this, 20, 16, 20, 32) }
        scroll.addView(content); setContentView(scroll)
        if (title == null) {
            val head = row(); val mark = label("✓", 19f, Color.WHITE, true).apply { gravity = Gravity.CENTER; background = round(purple, 12) }; head.addView(mark, LinearLayout.LayoutParams(dp(38), dp(38)))
            val brand = label("PocketKit", 20f, ink, true); val brandParams = LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(10) }; head.addView(brand, brandParams)
            head.addView(label("随身工具箱", 12f, muted)); content.addView(head); gap(content, 20)
        } else {
            val head = row(); val back = label("‹", 32f, ink).apply { gravity = Gravity.CENTER; contentDescription = "返回首页"; setOnClickListener { showHome() } }; head.addView(back, LinearLayout.LayoutParams(dp(42), dp(44)))
            val titles = column(); titles.addView(label(title, 20f, ink, true)); if (subtitle != null) titles.addView(label(subtitle, 12f, muted)); val tp = LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(7) }; head.addView(titles, tp); content.addView(head); gap(content, 22)
        }
    }
    private fun record(name: String) {
        val date = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date())
        val old = prefs.getString("recent", "").orEmpty().lines().filter { it.isNotBlank() }
        val list = (listOf("$name · $date") + old).take(5).joinToString("\n")
        prefs.edit().putInt("count", prefs.getInt("count", 0) + 1).putString("recent", list).apply()
    }
    private fun showHome() {
        page = "home"; layout()
        val hero = column().apply { background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(80,65,217), Color.rgb(145,132,255))).apply { cornerRadius = dp(24).toFloat() }; pad(this, 22, 23, 22, 22) }
        hero.addView(label("你的随身效率工具箱", 12f, 0xffe5e0ff.toInt())); gap(hero, 10)
        hero.addView(label("把琐事变简单，\n从一个小工具开始。", 26f, Color.WHITE, true)); gap(hero, 10)
        hero.addView(label("文字、图片、二维码和链接，\n都可以在这里轻松处理。", 13f, 0xffe5e0ff.toInt())); gap(hero, 18)
        val start = label("开始处理文本  →", 14f, purple, true).apply { gravity = Gravity.CENTER; background = round(Color.WHITE, 12); setOnClickListener { showTextTool() } }
        hero.addView(start, LinearLayout.LayoutParams(dp(166), dp(44))); content.addView(hero)
        gap(content, 26); content.addView(label("快捷工具", 19f, ink, true)); gap(content, 6); content.addView(label("选择一个工具，马上开始", 12f, muted)); gap(content, 16)
        val tools = listOf(Triple("Aa", "文本清理", "去空格 · 整理空行"), Triple("▦", "二维码生成", "文字 · 链接转码"), Triple("▧", "图片压缩", "调整质量 · 保存图片"), Triple("↗", "链接解析", "查看结构 · 清理参数"))
        tools.chunked(2).forEachIndexed { rowIndex, pair ->
            val line = row(); pair.forEachIndexed { index, item ->
                val card = column().apply { background = round(Color.WHITE, 19, 0xffededf2.toInt()); pad(this, 16); minimumHeight = dp(132); isClickable = true; isFocusable = true; contentDescription = item.second + "，" + item.third; setOnClickListener { when (item.second) { "文本清理" -> showTextTool(); "二维码生成" -> showQrTool(); "图片压缩" -> showImageTool(); else -> showLinkTool() } } }
                val colors = listOf(0xffffede5.toInt(), 0xffe3f8f1.toInt(), 0xfffff4cb.toInt(), 0xffe9edff.toInt()); val icons = listOf(0xffe87549.toInt(), 0xff38a984.toInt(), 0xffd69b16.toInt(), 0xff6071df.toInt()); val i = rowIndex * 2 + index
                card.addView(label(item.first, 17f, icons[i], true).apply { gravity = Gravity.CENTER; background = round(colors[i], 11) }, LinearLayout.LayoutParams(dp(38), dp(38))); gap(card, 12); card.addView(label(item.second, 15f, ink, true)); gap(card, 3); card.addView(label(item.third, 11f, muted))
                line.addView(card, LinearLayout.LayoutParams(0, -2, 1f).apply { if (index == 0) rightMargin = dp(10) })
            }; content.addView(line); gap(content, 10)
        }
        gap(content, 17); val head = row(); head.addView(label("最近使用", 19f, ink, true), LinearLayout.LayoutParams(0, -2, 1f)); head.addView(label("已完成 ${prefs.getInt("count", 0)} 次", 12f, purple, true)); content.addView(head); gap(content, 14)
        val recent = column().apply { background = round(Color.WHITE, 18, 0xffededf2.toInt()); pad(this, 17) }
        val items = prefs.getString("recent", "").orEmpty().lines().filter { it.isNotBlank() }
        if (items.isEmpty()) recent.addView(label("还没有使用记录，选一个工具试试吧。", 13f, muted)) else items.forEachIndexed { i, value -> if (i > 0) gap(recent, 12); recent.addView(label("•   $value", 13f, ink)) }
        content.addView(recent); gap(content, 20); content.addView(label("本地处理 · 轻巧实用", 11f, muted).apply { gravity = Gravity.CENTER })
    }
    private fun showTextTool() {
        page = "text"; layout("文本清理", "去除多余空格和空行")
        content.addView(label("原始文本", 14f, ink, true)); gap(content, 10)
        val input = edit("在这里输入或粘贴文字…", 9); content.addView(input, LinearLayout.LayoutParams(-1, dp(210))); gap(content, 14)
        content.addView(button("清理文本") {
            val result = TextTools.clean(input.text.toString()); if (result.isEmpty()) { toast("请先输入文字"); return@button }; input.setText(result); input.setSelection(result.length); record("文本清理"); toast("已整理，可以继续编辑或复制")
        }); gap(content, 10)
        content.addView(subtleButton("复制文本") { val text = input.text.toString(); if (text.isBlank()) { toast("没有可复制的内容"); return@subtleButton }; (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("PocketKit", text)); toast("已复制到剪贴板") })
        gap(content, 18); content.addView(label("清理规则：合并连续空格、去除行首行尾空格，并将多余空行合并。", 12f, muted))
    }
    private fun showQrTool() {
        page = "qr"; layout("二维码生成", "将文字或链接转为二维码")
        content.addView(label("内容", 14f, ink, true)); gap(content, 10); val input = edit("输入文字或网址", 4); content.addView(input, LinearLayout.LayoutParams(-1, dp(130))); gap(content, 14)
        val image = ImageView(this).apply { adjustViewBounds = true; setBackgroundColor(Color.WHITE); visibility = View.GONE; contentDescription = "生成的二维码" }; val imageFrame = LinearLayout.LayoutParams(dp(220), dp(220)).apply { gravity = Gravity.CENTER_HORIZONTAL }; content.addView(image, imageFrame); gap(content, 14)
        val save = subtleButton("保存二维码到文件") { if (qrBitmap != null) createFile("image/png", "PocketKit-QR.png", 102) }; save.visibility = View.GONE
        content.addView(button("生成二维码") {
            val value = input.text.toString().trim(); if (value.isEmpty()) { toast("请先输入内容"); return@button }
            try { val matrix = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 600, 600, mapOf(com.google.zxing.EncodeHintType.CHARACTER_SET to "UTF-8", com.google.zxing.EncodeHintType.MARGIN to 2)); val pixels = IntArray(600 * 600) { i -> if (matrix[i % 600, i / 600]) Color.BLACK else Color.WHITE }; qrBitmap = Bitmap.createBitmap(pixels, 600, 600, Bitmap.Config.ARGB_8888); image.setImageBitmap(qrBitmap); image.visibility = View.VISIBLE; save.visibility = View.VISIBLE; record("二维码生成"); (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(input.windowToken, 0) } catch (_: Exception) { toast("内容过长或无法生成二维码") }
        }); gap(content, 10); content.addView(save)
    }
    private fun showImageTool() {
        page = "image"; layout("图片压缩", "压缩后另存为 JPEG")
        content.addView(label("选择图片", 14f, ink, true)); gap(content, 10)
        val info = label(if (imageLabel.isEmpty()) "尚未选择图片" else imageLabel, 14f, muted).apply { background = round(Color.WHITE, 16, 0xffe5e6ee.toInt()); pad(this, 18); minHeight = dp(76); gravity = Gravity.CENTER_VERTICAL }; content.addView(info); gap(content, 12)
        content.addView(subtleButton("从手机选择图片") { val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE) }; startActivityForResult(intent, 100) }); gap(content, 24)
        val qualityLabel = label("输出质量：$quality%", 14f, ink, true); content.addView(qualityLabel); gap(content, 8)
        val seek = SeekBar(this).apply { max = 90; progress = quality - 10; setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) { quality = p + 10; qualityLabel.text = "输出质量：$quality%" }; override fun onStartTrackingTouch(s: SeekBar?) {}; override fun onStopTrackingTouch(s: SeekBar?) {} }) }; content.addView(seek); gap(content, 8)
        content.addView(label("质量越低，文件通常越小。大图会缩放至最长边约 2048 像素。透明区域会以白色背景输出。", 12f, muted)); gap(content, 24)
        content.addView(button("压缩并另存") { if (selectedImage == null) toast("请先选择图片") else createFile("image/jpeg", "PocketKit-compressed.jpg", 101) })
    }
    private fun createFile(type: String, name: String, request: Int) { val i = Intent(Intent.ACTION_CREATE_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); this.type = type; putExtra(Intent.EXTRA_TITLE, name) }; try { startActivityForResult(i, request) } catch (_: Exception) { toast("无法打开文件保存器") } }
    private fun showLinkTool() {
        page = "link"; layout("链接解析", "查看网址结构，清理追踪参数")
        content.addView(label("网址", 14f, ink, true)); gap(content, 10); val input = edit("例如 https://example.com/page?utm_source=…"); input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI; content.addView(input); gap(content, 14)
        val result = label("解析结果会显示在这里", 13f, muted).apply { background = round(Color.WHITE, 16, 0xffe5e6ee.toInt()); pad(this, 16); minHeight = dp(120) }; content.addView(result); gap(content, 14)
        var parsed: Uri? = null
        content.addView(button("解析链接") {
            val value = input.text.toString().trim(); val uri = try { Uri.parse(value) } catch (_: Exception) { null }
            if (uri == null || uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank()) { parsed = null; toast("请输入完整的 http 或 https 链接"); return@button }
            parsed = uri; result.setTextColor(ink); result.text = "协议  ${uri.scheme}\n\n域名  ${uri.host}\n\n路径  ${uri.path?.ifEmpty { "/" } ?: "/"}\n\n参数  ${uri.queryParameterNames.size} 个"; record("链接解析")
        }); gap(content, 10)
        content.addView(subtleButton("复制去追踪参数的链接") {
            val uri = parsed; if (uri == null) { toast("请先解析链接"); return@subtleButton }
            try { val builder = uri.buildUpon().clearQuery(); uri.queryParameterNames.filterNot { it.lowercase(Locale.ROOT).startsWith("utm_") || it.lowercase(Locale.ROOT) in setOf("fbclid", "gclid") }.forEach { key -> uri.getQueryParameters(key).forEach { builder.appendQueryParameter(key, it) } }; val clean = builder.build().toString(); (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("PocketKit URL", clean)); toast("已复制清理后的链接") } catch (_: Exception) { toast("此链接无法清理参数") }
        })
    }
    @Deprecated("Uses Android's document picker")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data); val uri = data?.data ?: return; if (resultCode != RESULT_OK) return
        when (requestCode) {
            100 -> { selectedImage = uri; imageLabel = try { contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else "已选择图片" } ?: "已选择图片" } catch (_: Exception) { "已选择图片" }; showImageTool() }
            102 -> { try { val bitmap = qrBitmap ?: return; contentResolver.openOutputStream(uri)?.use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) } ?: error("No output stream"); toast("二维码已保存") } catch (_: Exception) { toast("保存失败，请重试") } }
            101 -> { val source = selectedImage ?: return; val outputQuality = quality; toast("正在压缩图片…"); executor.execute {
                val success = try {
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }; contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, opts) }; require(opts.outWidth > 0 && opts.outHeight > 0)
                    var sample = 1; while (maxOf(opts.outWidth / sample, opts.outHeight / sample) > 2048) sample *= 2
                    val bitmap = contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) } ?: error("Unable to decode")
                    val flat = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888); android.graphics.Canvas(flat).apply { drawColor(Color.WHITE); drawBitmap(bitmap, 0f, 0f, null) }
                    contentResolver.openOutputStream(uri)?.use { check(flat.compress(Bitmap.CompressFormat.JPEG, outputQuality, it)) } ?: error("Unable to write"); bitmap.recycle(); flat.recycle(); true
                } catch (_: Exception) { false }; runOnUiThread { if (success) { record("图片压缩"); toast("压缩完成，已保存") } else toast("压缩失败，请换一张图片重试") }
            } }
        }
    }
}

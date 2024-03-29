package com.falcon.docxtopdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.falcon.docxtopdf.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.itextpdf.text.Document
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.*
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.*

import com.mycompany.docx2pdf.gui.swing.Main

class MainActivity : AppCompatActivity() {
//    lateinit var mAdView : AdView

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val docIn = "/home/user/docs/sample.docx"
        val docOut = "/home/user/docs/out.pdf"
        val fontsPath = "/home/user/docs/fonts/"
//        Main().
//        val app = Converter(docIn, docOut, fontsPath)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.comingSoonAnimation.visibility = View.INVISIBLE
        setSupportActionBar(binding.toolbar)
        binding.nothingToDisplay.visibility = View.INVISIBLE
        binding.nothingToDisplayLottie.visibility = View.INVISIBLE
        val folder2 = File(this.dataDir, "convertedPDFs")
        if (!folder2.exists()) {
            binding.nothingToDisplay.visibility = View.VISIBLE
            binding.nothingToDisplayLottie.visibility = View.VISIBLE
        }
        if (folder2.listFiles()?.isEmpty() == true) {
            binding.nothingToDisplay.visibility = View.VISIBLE
            binding.nothingToDisplayLottie.visibility = View.VISIBLE
        }
        binding.addButton.setOnClickListener {
            binding.comingSoonAnimation.visibility = View.VISIBLE
            selectPDF()
        }
        val folder = File(this.dataDir, "convertedPDFs")
        folder.mkdirs()
        binding.rcvPDFs.adapter = folder.listFiles()
            ?.let { ConvertedPdfRcvAdapter(it.toList(), this.applicationContext, ::onContentClick, ::shareFile2, ::deleteFile) }
        binding.rcvPDFs.layoutManager = LinearLayoutManager(this)


        // ads
        MobileAds.initialize(this) {}
        val mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
    private fun deleteFile(file: File) {
        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.d("TAG", "deleted from the cache directory")
            } else {
                Log.d("TAG", "could not be deleted from the cache directory")
            }
        } else {
            Log.d("TAG", "does not exist in the cache directory")
        }
        refreshRCV()
    }
    private fun onContentClick(file: File) {
        openFile2(file)
    }
    private fun selectPDF() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        startActivityForResult(intent, 1215)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val myIntent = Intent(this, SettingsActivity::class.java)
                startActivity(myIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1215 -> if (resultCode == RESULT_OK) {
                val uri = data?.data
                val inputStream = contentResolver.openInputStream(uri!!)
                if (inputStream == null) {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                } else {
                    val viewModelJob = Job()
                    val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
                    coroutineScope.launch  {
                        try {
                            val pdf = convertDocxToPdf3(inputStream, getFileName(uri))
                            savePdfInDataDir(pdf)
                            withContext(Dispatchers.Main) {
                                refreshRCV()
                                binding.comingSoonAnimation.visibility = View.VISIBLE
                                binding.comingSoonAnimation.visibility = View.INVISIBLE
                            }
                        } catch (e: Exception) {
                            Log.e("error", e.stackTraceToString())
                        }
                    }
                }
            }
        }
        binding.comingSoonAnimation.visibility = View.INVISIBLE
        super.onActivityResult(requestCode, resultCode, data)
    }

//    fun convertDocxToPdf(inputStream: InputStream): File {
//        val docx = XWPFDocument(inputStream)
//        val pdfFile = File("converted_file.pdf")
//        val pdfDoc = PdfDocument(PdfWriter(pdfFile))
//        val pdfDocument = Document(pdfDoc)
//
//        docx.paragraphs.forEach { paragraph ->
//            pdfDocument.add(Paragraph(paragraph.text))
//            paragraph.runs.forEach { run ->
//                if (run.underlyingRun.hasPictures()) {
//                    run.underlyingRun.embeddedPictures.forEach { picture ->
//                        val imageData = XWPFPictureData(picture.data)
//                        val image = Image(ImageData.create(imageData.data, imageData.contentType))
//                        pdfDocument.add(image)
//                    }
//                }
//            }
//        }
//        pdfDocument.close()
//        return pdfFile
//    }


    private fun refreshRCV() {
        val folder = File(this.dataDir, "convertedPDFs")
        if (folder.listFiles()?.isEmpty() == true) {
            binding.nothingToDisplay.visibility = View.VISIBLE
            binding.nothingToDisplayLottie.visibility = View.VISIBLE

        } else {
            binding.nothingToDisplay.visibility = View.INVISIBLE
            binding.nothingToDisplayLottie.visibility = View.INVISIBLE
        }
        binding.rcvPDFs.adapter = folder.listFiles()
            ?.let { ConvertedPdfRcvAdapter(it.toList(), this.applicationContext, ::onContentClick, ::shareFile2, ::deleteFile) }
        binding.rcvPDFs.layoutManager = LinearLayoutManager(this)
    }

    private fun savePdfInDataDir(pdf: File) {
        val folder = File(this.dataDir, "convertedPDFs")
        try {
            val inputStream = FileInputStream(pdf)
            val pdfName = getNameWithoutExtension(pdf.name) + ".pdf"
            val outputStream = FileOutputStream(File(folder, pdfName))
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getNameWithoutExtension(string: String): String {
         return string.substring(0, string.indexOf("."))
    }

    private fun openFile2(file: File) {
        val myIntent = Intent(Intent.ACTION_VIEW)
        val fileProviderUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        myIntent.data = fileProviderUri
        myIntent.setDataAndType(fileProviderUri, "application/pdf")
        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val j = Intent.createChooser(myIntent, "Choose an application to open with:")
        startActivity(j)
    }

    private fun shareFile2(file: File) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM,  uriFromFile(this,file))
        shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        shareIntent.type = "application/pdf"
        startActivity(Intent.createChooser(shareIntent, "share.."))
    }

    private fun uriFromFile(context:Context, file:File):Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
    }


    private fun convertDocxToPdf2(inputStream: InputStream, fileName: String): File {
        var doc = XWPFDocument()
        try {
            doc = XWPFDocument(inputStream)
        } catch (e: Exception) {
            Log.e("error", e.stackTraceToString())
            Toast.makeText(this, "File Corrupted Error", Toast.LENGTH_SHORT).show()
        }
        val output = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        val fileOutputStream = FileOutputStream(output)
        val document = Document()
        PdfWriter.getInstance(document, fileOutputStream)
        document.open()

        val fontDirectory = File(this.cacheDir, "fonts")
        if (!fontDirectory.exists()) {
            fontDirectory.mkdirs()
        }
        val fontFile = File(fontDirectory, "abc.ttf")
        if (fontFile.exists()) {
            fontFile.delete()
        }

        val fos = FileOutputStream(fontFile)
        val `in` = assets.open("abc.ttf")

        val buffer = ByteArray(1024)

        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            fos.write(buffer, 0, read)
        }
        `in`.close()
        fos.flush()
        fos.close()

        val base = BaseFont.createFont(fontFile.absolutePath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
        val font = Font(base, 11f, Font.BOLD)
        for (paragraph in doc.paragraphs) {
            document.add(Paragraph(paragraph.text, font))
        }
        document.close()
        return output
    }
    private fun convertDocxToPdf3(inputStream: InputStream, fileName: String): File {
        var doc = XWPFDocument()
        try {
            doc = XWPFDocument(inputStream)
        } catch (e: Exception) {
            Log.e("error", e.stackTraceToString())
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "File Corrupted Error", Toast.LENGTH_SHORT).show()
            }
        }
        val cacheDir = this.cacheDir
        val output = File(cacheDir, fileName)
        val fileOutputStream = FileOutputStream(output)
        val document = Document()
        PdfWriter.getInstance(document, fileOutputStream)
        document.open()
        for (paragraph in doc.paragraphs) {
            document.add(Paragraph(paragraph.text))
        }
        document.close()
        return output
    }
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}


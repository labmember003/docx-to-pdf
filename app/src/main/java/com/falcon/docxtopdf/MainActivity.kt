package com.falcon.docxtopdf

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import com.falcon.docxtopdf.databinding.ActivityMainBinding
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.*
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.comingSoonAnimation.visibility = View.INVISIBLE
        setSupportActionBar(binding.toolbar)

//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.buttonFirst.setOnClickListener {
            binding.comingSoonAnimation.visibility = View.VISIBLE
            selectPDF()
        }
        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAnchorView(R.id.fab).setAction("Action", null).show()
            composeEmail("Regarding App " + getString(R.string.app_name))
        }
        val folder = File(this.dataDir, "convertedPDFs")
//        ConvertedPdfRcvAdapter
        folder.mkdirs()
//        val file = File(folder, "test.pdf")
//        file.createNewFile()
        binding.rcvPDFs.adapter = folder.listFiles()
            ?.let { ConvertedPdfRcvAdapter(it.toList(), this.applicationContext) }
        binding.rcvPDFs.layoutManager = LinearLayoutManager(this)
    }
    fun selectPDF() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        startActivityForResult(intent, 1215)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
//                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                val myIntent = Intent(this, SettingsActivity::class.java)
                startActivity(myIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val viewModelJob = Job()
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        when (requestCode) {
            1215 -> if (resultCode == RESULT_OK) {
                val uri = data?.data
                val fileName = uri?.lastPathSegment
                val inputStream = contentResolver.openInputStream(uri!!)
                if (inputStream == null) {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                } else {
                    val viewModelJob = Job()
                    val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )
                    coroutineScope.launch {
                        try {
//                            getFileName(uri)
                            val pdf = convertDocxToPdf2(inputStream, getFileName(uri)?:"error")
                            savePdfInDataDir(pdf)
                            refreshRCV()
                            // TODO LOTTIE ANIMATION START
                            // TODO LOTTIE ANIMATION END
                            binding.comingSoonAnimation.visibility = View.VISIBLE
                            binding.comingSoonAnimation.visibility = View.INVISIBLE
                            openFile2(pdf)
                        } catch (e: Exception) {
                            Log.e("tatti", e.stackTraceToString())
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun refreshRCV() {
        val folder = File(this.dataDir, "convertedPDFs")
        binding.rcvPDFs.adapter = folder.listFiles()
            ?.let { ConvertedPdfRcvAdapter(it.toList(), this.applicationContext) }
        binding.rcvPDFs.layoutManager = LinearLayoutManager(this)
    }

    private fun savePdfInDataDir(pdf: File) {
        val folder = File(this.dataDir, "convertedPDFs")
        try {
            val inputStream = FileInputStream(pdf)
            val outputStream = FileOutputStream(File(folder, pdf.name))
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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



    fun convertDocxToPdf2(inputStream: InputStream, fileName: String): File {
        val doc = XWPFDocument(inputStream)
        Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, "gfds", Toast.LENGTH_SHORT).show()
        return output
    }
    fun getFileName(uri: Uri): String? {
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
    private fun convertDocxToPdf(context: Context, docxUri: Uri) {
        val inputStream = context.contentResolver.openInputStream(docxUri)
        val document = Document()

        try {
            // Create a new PDF file in the app's cache directory
            val pdfFile = File(context.cacheDir, "output.pdf")
            val pdfOutputStream = FileOutputStream(pdfFile)

            // Create a PDF writer to write the document to the output stream
            PdfWriter.getInstance(document, pdfOutputStream)

            // Open the document
            document.open()

            // Read the contents of the Docx file
            val docxBytes = inputStream?.readBytes()

            // Create a new paragraph for each line in the Docx file
            val docxText = String(docxBytes!!)
            val docxLines = docxText.split("\n")
            for (line in docxLines) {
                val paragraph = Paragraph(line)
                paragraph.alignment = Element.ALIGN_LEFT
                document.add(paragraph)
            }

            // Close the document and output stream
            document.close()
            pdfOutputStream.close()
            inputStream.close()

            // Open the converted PDF file
            // Note: You may need to add additional code to handle the opening of the PDF file
            val pdfUri = Uri.fromFile(pdfFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun composeEmail(subject: String) {
        val a = arrayOf("usarcompanion@gmail.com")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, a)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No Mail App Found", Toast.LENGTH_SHORT).show()
        }
    }
}
//.docx     application/vnd.openxmlformats-officedocument.wordprocessingml.document
//.dotx     application/vnd.openxmlformats-officedocument.wordprocessingml.template
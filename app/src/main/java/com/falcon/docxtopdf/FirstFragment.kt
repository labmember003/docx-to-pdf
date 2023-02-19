package com.falcon.docxtopdf

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.falcon.docxtopdf.databinding.FragmentFirstBinding

import android.content.Context
import android.net.Uri
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
//        convertDocxToPdf(requireContext(), URI())
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            (activity as MainActivity).selectPDF()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun convertDocxToPdf(context: Context, docxUri: Uri) {
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
}
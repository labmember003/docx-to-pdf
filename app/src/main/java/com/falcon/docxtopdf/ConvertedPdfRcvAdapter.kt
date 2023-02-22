package com.falcon.docxtopdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class ConvertedPdfRcvAdapter(private val convertedPDFs: List<File>, private val context: Context, private val onContentClick : (File) -> Unit,
                             private val shareFile : (File) -> Unit, private val deleteFile : (File) -> Unit)
    : RecyclerView.Adapter<ConvertedPdfRcvAdapter.ExistingFontViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExistingFontViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.existing_font, parent, false)
        return ExistingFontViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExistingFontViewHolder, position: Int) {
        holder.convertedPDF.text = convertedPDFs[position].name
        holder.convertedPDF.setOnClickListener {
            onContentClick(convertedPDFs[position])
        }
        holder.previewImage.setOnClickListener {
            onContentClick(convertedPDFs[position])
        }
        holder.shareButton.setOnClickListener {
            shareFile(convertedPDFs[position])
        }
        holder.deleteButton.setOnClickListener {
            deleteFile(convertedPDFs[position])
        }
//        val pdfFile = convertedPDFs[position]
//        val pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
//        val page = pdfRenderer.openPage(0)
//        Toast.makeText(context, page.height.toString(), Toast.LENGTH_SHORT).show()
//        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
//        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//        holder.previewImage.setImageBitmap(bitmap)
//        page.close()
//        pdfRenderer.close()
    }

    override fun getItemCount(): Int {
        return convertedPDFs.size
    }
    class ExistingFontViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val convertedPDF: TextView = itemView.findViewById(R.id.convertedPDF)
        val shareButton: ImageView = itemView.findViewById(R.id.shareButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val previewImage: ImageView = itemView.findViewById(R.id.previewImage)
    }
}
package com.falcon.docxtopdf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class ConvertedPdfRcvAdapter(private val convertedPDFs: List<File>, val context: Context, private val onContentClick : (File) -> Unit): RecyclerView.Adapter<ConvertedPdfRcvAdapter.ExistingFontViewHolder>() {
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
        // TODO
//        holder.imgview.setOnClickListener {
//            onContentClick(convertedPDFs[position])
//        }
    }

    override fun getItemCount(): Int {
        return convertedPDFs.size
    }
    class ExistingFontViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val convertedPDF: TextView = itemView.findViewById(R.id.convertedPDF)
//        val fontSelectedCB: CheckBox = itemView.findViewById(R.id.fontCheckBox)
    }

}
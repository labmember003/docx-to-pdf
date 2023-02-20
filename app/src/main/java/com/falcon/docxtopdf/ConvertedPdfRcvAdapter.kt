package com.falcon.docxtopdf

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File



class ConvertedPdfRcvAdapter(private val convertedPDFs: List<File>, val context: Context): RecyclerView.Adapter<ConvertedPdfRcvAdapter.ExistingFontViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExistingFontViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.existing_font, parent, false)
        return ExistingFontViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExistingFontViewHolder, position: Int) {
        holder.convertedPDF.text = convertedPDFs[position].name
//        holder.fontSelectedCB.setOnClickListener {
//            Log.i("catttt", position.toString())
//            Log.i("catttt", existingFilesList[position].name)
//            val folder2 = File(context.dataDir, "activeFonts")
//            val file = File(folder2, existingFilesList[position].name)
//            file.createNewFile()
//            //TODO("COPY FILE FROM EXISTING FILES TO ACTIVE FONT FOLDER")
//            //TODO("SEARCH FILE IN EXISTING FONT FOLDER THEN COPY IT HERE")
//            //TODO("JAAHA BHI USE KRNA HOGA ACTIVE FONTS USE THIS FOLDER (folder2) TO ACCESS FILES")
//
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
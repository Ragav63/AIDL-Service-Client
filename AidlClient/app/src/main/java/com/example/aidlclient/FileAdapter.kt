package com.example.aidlclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.aidlclient.databinding.ItemFileBinding
import com.example.aidlservice.FileItem
import kotlin.apply
import kotlin.collections.toMutableList

class FileAdapter(private val event :(FileItem)-> Unit) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {


    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = diffData.currentList[position]
        holder.binding.tvFileName.text = file.name



        holder.binding.root.setOnClickListener {
            event(file)
        }


    }



    override fun getItemCount(): Int = diffData.currentList.size

    fun submitList(list: List<FileItem>) {
        diffData.submitList(list)
    }

    val diffUtil = object:DiffUtil.ItemCallback<FileItem>(){
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return  oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return  oldItem==newItem
        }

    }

    val diffData = AsyncListDiffer(this,diffUtil)



}
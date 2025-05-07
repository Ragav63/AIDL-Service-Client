package com.example.aidlservice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.aidlservice.databinding.ItemFileBinding

class FileAdapter(private val event :(FileItem, ActionType)-> Unit) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {


    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = diffData.currentList[position]
        holder.binding.tvFileName.text = file.name

        holder.binding.ivDelete.setOnClickListener {
            removeItem(file)
        }

        holder.binding.tvFileName.setOnClickListener {
            event(file, ActionType.EDIT)
        }

        holder.binding.ivVisible.setOnClickListener {
            event(file, ActionType.VIEW)
        }
    }

    private fun removeItem(fileItem: FileItem) {
        val updatedList = diffData.currentList.toMutableList().apply {
            remove(fileItem)
        }
        diffData.submitList(updatedList)
    }

    override fun getItemCount(): Int = diffData.currentList.size

    fun submitList(list: List<FileItem>) {
        diffData.submitList(list)
        notifyDataSetChanged()
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


    enum class ActionType {
        VIEW, EDIT
    }
}
package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.databinding.FragmentAdminCategoriesBinding
import com.example.salmaflorist.databinding.ItemCategoryRowBinding
import com.example.salmaflorist.model.Category

class AdminCategoriesFragment : Fragment() {
    private var _binding: FragmentAdminCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBOpenHelper(requireContext())

        binding.fabAddCategory.setOnClickListener {
            showCategoryDialog()
        }

        loadCategories()
    }

    private fun loadCategories() {
        val categories = dbHelper.getAllCategories()
        val table = binding.tableCategories
        
        // Remove all views except header and divider
        while (table.childCount > 2) {
            table.removeViewAt(2)
        }

        val inflater = LayoutInflater.from(requireContext())

        categories.forEach { category ->
            val rowBinding = ItemCategoryRowBinding.inflate(inflater, table, false)
            rowBinding.tvCategoryName.text = category.name
            
            rowBinding.ivMenu.setOnClickListener { view ->
                showPopupMenu(view, category)
            }
            
            table.addView(rowBinding.root)
        }
    }

    private fun showPopupMenu(view: View, category: Category) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_admin_options, popup.menu)
        
        // Clear and add specific actions for category
        popup.menu.clear()
        popup.menu.add(0, 1, 0, "Edit")
        popup.menu.add(0, 2, 1, "Hapus")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> showCategoryDialog(category)
                2 -> showDeleteConfirmation(category)
            }
            true
        }
        popup.show()
    }

    private fun showCategoryDialog(category: Category? = null) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (category == null) "Tambah Kategori" else "Edit Kategori")

        val input = EditText(requireContext())
        input.setPadding(64, 32, 64, 32)
        input.setText(category?.name)
        builder.setView(input)

        builder.setPositiveButton("Simpan") { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                val success = if (category == null) {
                    dbHelper.addCategory(name)
                } else {
                    dbHelper.updateCategory(category.id, name)
                }

                if (success) {
                    Toast.makeText(requireContext(), "Berhasil!", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(requireContext(), "Gagal!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun showDeleteConfirmation(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori '${category.name}'? Produk di dalam kategori ini juga akan ikut terhapus.")
            .setPositiveButton("Hapus") { _, _ ->
//                if (dbHelper.deleteCategory(category.id)) {
//                    Toast.makeText(requireContext(), "Berhasil dihapus!", Toast.LENGTH_SHORT).show()
//                    loadCategories()
//                } else {
//                    Toast.makeText(requireContext(), "Gagal menghapus!", Toast.LENGTH_SHORT).show()
//                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
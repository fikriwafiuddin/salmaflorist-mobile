package com.example.salmaflorist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salmaflorist.databinding.FragmentContactBinding

class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)

        // Klik Chat Admin (Text) atau seluruh area layout WA
        binding.btnChat.setOnClickListener { bukaWA() }
        binding.layoutWA.setOnClickListener { bukaWA() }

        // Klik Instagram
        binding.layoutIG.setOnClickListener {
            val url = "https://www.instagram.com/salma.floristry"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        return binding.root
    }

    private fun bukaWA() {
        val nomor = "6285808933346"
        val url = "https://wa.me/$nomor"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
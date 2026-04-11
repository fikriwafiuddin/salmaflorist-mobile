package com.example.salmaflorist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.salmaflorist.databinding.FragmentLoginBinding

class LoginFragment : AppCompatActivity() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var dbHelper: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBOpenHelper(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
            } else {
                // Cek ke SQLite
                if (dbHelper.checkUser(email, password)) {
                    Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Email atau Password Salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
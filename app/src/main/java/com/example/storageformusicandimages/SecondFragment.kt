package com.example.storageformusicandimages

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.storageformusicandimages.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSaveToPrefs.setOnClickListener {
            val username = binding.inputName.text.toString()
            val password = binding.inputPassword.text.toString()
            saveToPrefs(username, password)
            Log.e("mylog", "USERNAME = $username")
            Log.e("mylog", "PASSWORD = $password")
            Toast.makeText(requireContext(), "Успех", Toast.LENGTH_SHORT).show()
        }

        binding.buttonLoadFromPrefs.setOnClickListener {
            val (username, password) = loadFromPrefs()
            val message = "Имя: $username, Пароль: $password"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            "encrypted_prefs",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun saveToPrefs(username: String, password: String) {
        val prefs = getEncryptedSharedPreferences()
        with(prefs.edit()) {
            putString("username", username)
            putString("password", password)
            apply()
        }
    }

    private fun loadFromPrefs(): Pair<String?, String?> {
        val prefs = getEncryptedSharedPreferences()
        val username = prefs.getString("username", null)
        val password = prefs.getString("password", null)
        return username to password
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.example.storageformusicandimages.room

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.storageformusicandimages.databinding.FragmentRoomBinding
import kotlinx.coroutines.launch

class RoomFragment : Fragment() {

    private lateinit var viewModel: RoomViewModel
    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[RoomViewModel::class.java]

        // Кнопка для сохранения данных в Room
        binding.buttonSaveToRoom.setOnClickListener {
            val name = binding.inputName.text.toString()
            val password = binding.inputPassword.text.toString()
            viewModel.insertUser(name, password)
            Toast.makeText(requireContext(), "Данные сохранены в Room", Toast.LENGTH_SHORT).show()
        }

        // Кнопка для загрузки данных из Room
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collect { users ->
                // Показываем последний введенный пользователь
                val lastUser = users.lastOrNull()
                val data = lastUser?.content?.split(":") ?: listOf("", "")
                val message = "Имя: ${data[0]}, Пароль: ${data[1]}"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonLoadFromRoom.setOnClickListener {
            val usersLog = viewModel.users.value.joinToString("\n") { user ->
                val data = user.content.split(":")
                "Логин: ${data[0]}, Пароль: ${data[1]}"
            }

            Log.e("mylog RoomUsers", usersLog)
            Toast.makeText(requireContext(), "Данные отправлены в лог", Toast.LENGTH_SHORT).show()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



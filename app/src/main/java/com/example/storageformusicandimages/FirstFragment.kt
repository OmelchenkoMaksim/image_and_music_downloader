package com.example.storageformusicandimages

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.storageformusicandimages.databinding.FragmentFirstBinding
import okhttp3.*
import java.io.IOException
import java.io.InputStream


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val progressBar: ProgressBar by lazy { binding.downloadProgressBar }

    private var urlFromEditText = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    // https://x2download.app/ сработало на этом сайте
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val a = {}
        val b = null

        binding.buttonToSecond.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonDownload.setOnClickListener {
            if (urlFromEditText.length > 7) {
                progressBar.isVisible = true
                if (binding.inputSongUrl.text.toString().length <= 1) {
                    val imageFilename = "image${(Math.random() * 1000).toInt()}.jpg"
                    downloadAndSaveMedia(urlFromEditText, imageFilename, "image/jpeg")
                    Log.e(
                        "mylog",
                        "jpeg скачали строке ${Thread.currentThread().stackTrace[2].lineNumber}"
                    )
                } else {
                    try {
                        val songFilename = "song${(Math.random() * 1000).toInt()}.mp3"
                        downloadAndSaveMedia(urlFromEditText, songFilename, "audio/mp3")
                        Log.e(
                            "mylog",
                            "песню скачали строке ${Thread.currentThread().stackTrace[2].lineNumber}"
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "mylog",
                            "песня не скачалась"
                        )
                    }
                }
                progressBar.isVisible = false
            } else Toast.makeText(
                requireActivity(),
                "Используйте валидный урл с http/s в начале", Toast.LENGTH_SHORT
            ).show()
        }

        binding.inputImageUrl.addTextChangedListener {
            urlFromEditText = it.toString()
            Log.e("mylog", urlFromEditText)
        }

        binding.inputImageUrl.doAfterTextChanged {
            binding.inputSongUrl.text?.clear()
        }

        binding.inputSongUrl.addTextChangedListener {
            urlFromEditText = it.toString()
            Log.e("mylog", urlFromEditText)
            binding.inputImageUrl.text?.clear()
        }

        binding.inputSongUrl.doAfterTextChanged {
            binding.inputImageUrl.text?.clear()
        }
    }

    private fun downloadAndSaveMedia(url: String, filename: String, mimeType: String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    response.body?.let { responseBody ->
//                        val input = responseBody.byteStream()
//                        saveMediaToStorage(input, filename, mimeType)
//                        requireActivity().runOnUiThread {
//                            Log.e("mylog", "Download successful!")
//                        }
//                    }
//                }
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val contentLength = responseBody.contentLength()
                        var bytesReadSoFar: Long = 0
                        Log.e("mylog", "contentLength $contentLength")
                        val input = responseBody.byteStream()
                        saveMediaToStorage(input, filename, mimeType)
                        // ниже onBytesRead для прогресс-бара
//                        { bytesRead ->
//                            bytesReadSoFar += bytesRead
//                            val progress = if (contentLength > 0) {
//                                (bytesReadSoFar * 100 / contentLength).toInt()
//                            } else bytesReadSoFar.toInt()
//                            requireActivity().runOnUiThread {
//                                progressBar.progress = progress
//                            }
//                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Log.e("mylog", "Download failed!")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.e("mylog", "Network error!")
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveMediaToStorage(
        inputStream: InputStream, filename: String,
        mimeType: String,
        // для спиннера - прогресс бара
        onBytesRead: ((bytesRead: Int) -> Unit)? = null
    ) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val mediaUri = requireActivity().contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
        )
        requireActivity().runOnUiThread {
            progressBar.isVisible = true
        }
        requireActivity().contentResolver.openOutputStream(mediaUri!!)?.use { os ->
            val buffer = ByteArray(1024 * 4)  // buffer size of 4KB
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
                // onBytesRead функция для прогресс-бара
                if (onBytesRead != null) {
                    onBytesRead(bytesRead)
                }
            }
        }
        inputStream.close()
        // нотификации
        requireActivity().runOnUiThread {
            progressBar.isVisible = false
            Toast.makeText(requireActivity(), "Completed!", Toast.LENGTH_SHORT).show()
        }
        showNotification(filename, mediaUri)
    }


    @SuppressLint("MissingPermission")
    private fun showNotification(filename: String, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent =
            PendingIntent.getActivity(
                context, 0, intent,
                // С Android 12 при создании PendingIntent вам необходимо указать один из флагов:
                // FLAG_IMMUTABLE или FLAG_MUTABLE. И FLAG_UPDATE_CURRENT не отменяет это правило
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val channelId = "downloadChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Download Channel"
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            val manager = requireActivity().getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setContentTitle("Download Complete")
            .setContentText(filename)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // замените на ваш значок приложения
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(requireContext())
            .notify((Math.random() * 1000).toInt(), notification)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
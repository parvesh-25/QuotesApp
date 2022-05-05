package com.altaf.myquote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.altaf.myquote.databinding.ActivityMainBinding
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getRandomQuote()

        binding.btnAllQuotes.setOnClickListener{
            startActivity(Intent(this@MainActivity, ListQuotesActivity:: class.java))
        }
    }

    // menerapkan LoopJ utk akses web Api yg bbentuk satu JSONObject
    private fun getRandomQuote() {
        // progressBAr digunakan utk memberitahu proses yg sedang berjalan, ketika data sudah tampil ProgressBar akan hilang
        binding.progressBar.visibility = View.VISIBLE
        // utk menggunakan library loopJ, kt cukup mneggunakan AsyncHttpClient utk membuat koneksi ke server secara asynchronous
        val client = AsyncHttpClient()
        val url = "https://quote-api.dicoding.dev/random"
        // krn kt cuma mengambil data (READ), maka kt mneggunakan code client.get(). 2 objek yg dihasilkan AsyncHttpResponseHandler = onSucces dan onFailure
        client.get(url, object : AsyncHttpResponseHandler() {

            // onSucces digunakan jika koneksi berhasil
            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                // Jika koneksi berhasil
                // progressBar akan hilang krn data akan tampil
                binding.progressBar.visibility = View.INVISIBLE

                // mem-parsing JSON dari https://quote-api.dicoding.dev/random
                val result = String(responseBody)
                // membuat Log utk menampilkan response di Logcat, (jika data di logcat ada tp list tdk tampil, kemungkinan salah di saat parsing JSON / saat menampilkan RecyclerView
                Log.d(TAG, result)
                try {
                    // code utk ngambil data API bertipe JSONOBJECT
                    val responseObject = JSONObject(result)

                    // disini kita hanya mengambil en / text nya, dan author/ penulisnya
                    // utk menambil data quote  sesuai dgn tipe data dari value tsb
                    val quote = responseObject.getString("en")
                    // utk ngambil data author
                    val author = responseObject.getString("author")

                    binding.tvQuote.text = quote
                    binding.tvAuthor.text = author

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            // onFailure digunakan jk koneksi gagal
            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {
                // Jika koneksi gagal
                //progressBar akan hilang krn data akan tampil
                binding.progressBar.visibility = View.INVISIBLE

                // statusCode = menggunakan percabangan utk mengganti pesan yg ditampilkan jika terjadi error  saat koneksi ke server
                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    // selain kode diatas, kt menggunaka error.getMessage() utk mendapatkan pesan eror
                    else -> "$statusCode : ${error.message}"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}

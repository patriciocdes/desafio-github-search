package br.com.igorbag.githubsearch.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.infra.RetrofitClient
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView

    private val keyUserName = "user_name"
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)

        sharedPreferences = getSharedPreferences("SP_GITHUB_SEARCH", MODE_PRIVATE)

        setupListeners()
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            saveUserLocal()
            getAllReposByUserName()
        }
    }

    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        sharedPreferences.edit().apply {
            putString(keyUserName, nomeUsuario.text.toString())
            apply()
        }
    }

    private fun showUserName() {
        sharedPreferences.getString(keyUserName, "")?.let {
            nomeUsuario.setText(it)
        }
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        val userName = nomeUsuario.text.toString().trim().let {
            it.ifEmpty {
                return
            }
        }

        val api = RetrofitClient.instance.create(GitHubService::class.java)

        api.getAllRepositoriesByUser(userName)
            .enqueue(object : Callback<List<Repository>> {
                override fun onResponse(call: Call<List<Repository>?>, response: Response<List<Repository>?>) {
                    if (response.isSuccessful) {
                        val repos = response.body() ?: emptyList()
                        setupAdapter(repos)
                    } else {
                        Log.e("GITHUB", "Erro: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Repository>?>, t: Throwable) {
                    Log.e("GITHUB", "Falha: ${t.message}")
                }
            })
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(list)

        listaRepositories.layoutManager = LinearLayoutManager(this)
        listaRepositories.setHasFixedSize(true)
        listaRepositories.adapter = adapter

        adapter.apply {
            itemClickCallback = {
                openBrowser(it.htmlUrl)
            }

            shareClickCallback = {
                shareRepositoryLink(it.htmlUrl)
            }
        }
    }

    // Metodo responsavel por compartilhar o link do repositorio selecionado
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }
}
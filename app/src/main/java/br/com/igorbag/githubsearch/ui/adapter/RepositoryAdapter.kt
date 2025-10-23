package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    var itemClickCallback: (Repository) -> Unit = {}
    var shareClickCallback: (Repository) -> Unit = {}

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.repository_item, parent, false
        )

        return ViewHolder(view)
    }

    // Pega o conteudo da view e troca pela informacao de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repo = repositories[position]

        holder.apply {
            repositoryName.text = repo.name

            share.setOnClickListener {
                shareClickCallback(repo)
            }

            itemView.setOnClickListener {
                itemClickCallback(repo)
            }
        }
    }

    // Pega a quantidade de repositorios da lista
    override fun getItemCount() = repositories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val repositoryName: TextView
        val share: ImageView

        init {
            view.apply {
                repositoryName = view.findViewById(R.id.tv_repository_name)
                share = view.findViewById(R.id.iv_share)
            }
        }
    }
}


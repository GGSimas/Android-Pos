package com.aula.db

import android.content.Context
import com.aula.constants.CONTATOS_DB_NAME
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.*
import timber.log.Timber

class ContatoRepository(val context: Context) {
    fun isContato(telefone: String) : Boolean = context.database.use {
        select(CONTATOS_DB_NAME, "count(*) as total")
                .whereArgs("telefone = {telefone}","telefone" to telefone)
                .parseSingle(object: MapRowParser<Boolean> {
                    override fun parseRow(columns: Map<String, Any?>): Boolean {
                        val total = columns.getValue("total")
                        return total.toString().toInt() > 0;
                    }
                })
    }

    fun findAll() : ArrayList<Contato>  = context.database.use {
        val contatos = ArrayList<Contato>()

        select(CONTATOS_DB_NAME, "id", "email", "endereco", "nome", "telefone","dataNascimento", "site", "foto")
            .parseList(object: MapRowParser<List<Contato>> {
                override fun parseRow(columns: Map<String, Any?>): List<Contato> {
                    contatos.add(Contato(
                        id = columns.getValue("id").toString()?.toLong(),
                        foto = columns.getValue("foto")?.toString(),
                        nome = columns.getValue("nome")?.toString(),
                        endereco = columns.getValue("endereco")?.toString(),
                        telefone = columns.getValue("telefone")?.toString()?.toLong(),
                        dataNascimento = columns.getValue("dataNascimento")?.toString()?.toLong(),
                        email = columns.getValue("email")?.toString(),
                        site = columns.getValue("site")?.toString()
                    ))
                    return contatos
                }
            })
        contatos
    }

    fun create(contato: Contato) = context.database.use {
        insert(CONTATOS_DB_NAME,
            "foto" to contato.foto,
            "nome" to contato.nome,
            "endereco" to contato.endereco,
            "telefone" to contato.telefone,
            "email" to contato.email,
            "site" to contato.site,
            "dataNascimento" to contato.dataNascimento)
    }

    fun update(contato: Contato) = context.database.use {
        val updateResult = update(CONTATOS_DB_NAME,
            "foto" to contato.foto,
            "nome" to contato.nome,
            "endereco" to contato.endereco,
            "telefone" to contato.telefone,
            "email" to contato.email,
            "site" to contato.site)
            .whereArgs("id = {id}","id" to contato.id).exec()
        Timber.d("Update result code is $updateResult")
    }

    fun delete(id: Long) = context.database.use {
        delete(CONTATOS_DB_NAME, "id = {contatoId}", args = *arrayOf("contatoId" to id))
    }

    fun findAll(filter: String) : ArrayList<Contato> = context.database.use {
        val contatos = ArrayList<Contato>()

        select(CONTATOS_DB_NAME, "id", "email", "endereco", "nome", "telefone", "datanascimento", "site", "foto")
            .whereArgs( "nome like {nome}", "nome" to filter)
            .parseList(object: MapRowParser<List<Contato>> {
                override fun parseRow(columns: Map<String, Any?>): List<Contato> {
                    val id = columns.getValue("id")
                    val email = columns.getValue("email")
                    val endereco = columns.getValue("endereco")
                    val nome = columns.getValue("nome")
                    val telefone = columns.getValue("telefone")
                    val datanascimento = columns.getValue("datanascimento")
                    val site = columns.getValue("site")
                    val foto = columns.getValue("foto")

                    contatos.add(Contato(
                        id.toString()?.toLong(),
                        foto?.toString(),
                        nome?.toString(),
                        endereco?.toString(),
                        telefone?.toString()?.toLong(),
                        datanascimento?.toString()?.toLong(),
                        email?.toString(),
                        site?.toString()))
                    return contatos
                }
            })

        contatos
    }

}
package com.aula.agendapos


import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.aula.constants.APP_NAME
import com.aula.db.Contato
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aula.db.ContatoRepository
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var contatos: List<Contato>? = null
    private var contatoSelecionado: Contato? = null
    val MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10
    var receiver: BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(toolbar)

        lista.setOnItemClickListener { _, _, position, id ->
            val intent = Intent(this@MainActivity, ContatoActivity::class.java)
            intent.putExtra("contato", contatos?.get(position))
            startActivity(intent)
        }

        lista.setOnItemLongClickListener { _, _, posicao, _ ->
            Log.i(APP_NAME, "apagar posição: $posicao ")
            contatoSelecionado = contatos?.get(posicao)

            false
        }

        setupPermissions()
        configureReceiver()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_contato_contexto, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.excluir -> {
                AlertDialog.Builder(this@MainActivity)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Deletar")
                        .setMessage("Deseja mesmo deletar ?")
                        .setPositiveButton("Quero"
                        ) { _, _ ->
                            ContatoRepository(this).delete(this.contatoSelecionado!!.id)
                            carregaLista()
                            Toast.makeText(this, "Contato excluido", Toast.LENGTH_LONG).show()
                        }.setNegativeButton("Nao", null).show()

                return false
            }
            R.id.enviasms -> {
                val intentSms = Intent(Intent.ACTION_VIEW)
                intentSms.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                intentSms.data = Uri.parse("sms:" + contatoSelecionado?.telefone)
                intentSms.putExtra("sms_body", "Mensagem")
                item.intent = intentSms
                return false
            }
            R.id.enviaemail -> {
                val intentEmail = Intent(Intent.ACTION_SEND)
                intentEmail.type = "message/rfc822"
                intentEmail.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(contatoSelecionado?.email!!))
                intentEmail.putExtra(Intent.EXTRA_SUBJECT, "Teste de email")
                intentEmail.putExtra(Intent.EXTRA_TEXT, "Corpo da mensagem")
                startActivity(Intent.createChooser(intentEmail, "Selecione a sua aplicação de Email"))
                return false
            }
            R.id.share -> {
                val intentShare = Intent(Intent.ACTION_SEND)
                intentShare.type = "text/plain"
                intentShare.putExtra(Intent.EXTRA_SUBJECT, "Assunto que será compartilhado")
                intentShare.putExtra(Intent.EXTRA_TEXT, "Texto que será compartilhado")
                startActivity(Intent.createChooser(intentShare, "Escolha como compartilhar"))
                return false
            }
            R.id.ligar -> {
                val intentLigar = Intent(Intent.ACTION_DIAL)
                intentLigar.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                intentLigar.data = Uri.parse("tel:" + contatoSelecionado?.telefone)
                item.intent = intentLigar
                return false
            }
            R.id.visualizarmapa -> {
                val gmmIntentUri = Uri.parse("geo:0,0?q=" + contatoSelecionado?.endereco)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
                return false
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.novo ->  {
                val intent = Intent(this, ContatoActivity::class.java)
                startActivity(intent)
                return false
            }

            R.id.sincronizar ->  {
                Toast.makeText(this, "Enviar", Toast.LENGTH_SHORT).show()
                return false
            }

            R.id.receber ->  {
                Toast.makeText(this, "Receber", Toast.LENGTH_SHORT).show()
                return false
            }

            R.id.mapa ->  {
                Toast.makeText(this, "Mapa", Toast.LENGTH_SHORT).show()
                return false
            }

            R.id.preferencias ->  {
                Toast.makeText(this, "Preferencias", Toast.LENGTH_SHORT).show()
                return false
            }

            else -> return super.onOptionsItemSelected(item);
        }
    }

    override fun onResume() {
        super.onResume()
        carregaLista()
        registerForContextMenu(lista)
    }

    private fun carregaLista() {
        contatos = ContatoRepository(this).findAll()
        val adapter= ArrayAdapter(this, android.R.layout.simple_list_item_1, contatos!!)
        lista?.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun setupPermissions() {

        val list = listOf<String>(
                Manifest.permission.RECEIVE_SMS
        )

        ActivityCompat.requestPermissions(this,
                list.toTypedArray(), MY_PERMISSIONS_REQUEST_SMS_RECEIVE);

        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)

        if (permission != PackageManager.GET_SERVICES) {
            Log.i("aula", "Permission to record denied")
        }
    }

    private fun configureReceiver() {
        val filter = IntentFilter()
        filter.addAction("com.aula.agenda.SMSreceiver")
        filter.addAction("android.provider.Telephony.SMS_RECEIVED")
        receiver = SMSReceiver()
        registerReceiver(receiver, filter)
    }
}
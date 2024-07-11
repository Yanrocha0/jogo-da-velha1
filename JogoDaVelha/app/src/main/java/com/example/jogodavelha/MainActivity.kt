package com.example.jogodavelha

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.jogodavelha.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // Vetor bidimensional que representará o tabuleiro de jogo
    val tabuleiro = Array(3) { Array(3) { "" } }

    // Qual o Jogador está jogando
    var jogadorAtual = "X"
    // Modo de Jogo: "amigo" ou "computador"
    var modoDeJogo: String = ""
    // Nível de dificuldade: "facil" ou "dificil"
    var nivelDeDificuldade: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Configuração dos botões de modo de jogo
        binding.buttonJogarContraAmigo.setOnClickListener {
            modoDeJogo = "amigo"
            iniciarJogo()
        }

        binding.buttonJogarContraComputador.setOnClickListener {
            modoDeJogo = "computador"
            binding.buttonFacil.visibility = View.VISIBLE
            binding.buttonDificil.visibility = View.VISIBLE
        }

        // Configuração dos botões de nível de dificuldade
        binding.buttonFacil.setOnClickListener {
            nivelDeDificuldade = "facil"
            iniciarJogo()
        }

        binding.buttonDificil.setOnClickListener {
            nivelDeDificuldade = "dificil"
            iniciarJogo()
        }
    }

    // Função para iniciar o jogo e mostrar os botões do tabuleiro
    private fun iniciarJogo() {
        binding.textViewEstado.visibility = View.VISIBLE
        binding.textViewEstado.text = "Jogador X começa"

        // Mostrar os botões do tabuleiro
        binding.linha1.visibility = View.VISIBLE
        binding.linha2.visibility = View.VISIBLE
        binding.linha3.visibility = View.VISIBLE
        binding.buttonFacil.visibility = View.GONE
        binding.buttonDificil.visibility = View.GONE

        // Inicializar o tabuleiro vazio
        for (i in 0..2) {
            for (j in 0..2) {
                tabuleiro[i][j] = ""
            }
        }

        habilitarBotoes(true)
    }

    // Função associada com todos os botões @param view é o botão clicado
    fun buttonClick(view: View) {
        val buttonSelecionado = view as Button

        // De acordo com o botão clicado, a posição da matriz receberá o Jogador
        when (buttonSelecionado.id) {
            binding.buttonZero.id -> tabuleiro[0][0] = jogadorAtual
            binding.buttonUm.id -> tabuleiro[0][1] = jogadorAtual
            binding.buttonDois.id -> tabuleiro[0][2] = jogadorAtual
            binding.buttonTres.id -> tabuleiro[1][0] = jogadorAtual
            binding.buttonQuatro.id -> tabuleiro[1][1] = jogadorAtual
            binding.buttonCinco.id -> tabuleiro[1][2] = jogadorAtual
            binding.buttonSeis.id -> tabuleiro[2][0] = jogadorAtual
            binding.buttonSete.id -> tabuleiro[2][1] = jogadorAtual
            binding.buttonOito.id -> tabuleiro[2][2] = jogadorAtual
        }

        // Definir imagem de acordo com o jogador atual
        if (jogadorAtual == "X") {
            buttonSelecionado.setBackgroundResource(R.drawable.flamengo)
        } else {
            buttonSelecionado.setBackgroundResource(R.drawable.vasco)
        }

        buttonSelecionado.isEnabled = false

        // Verificar se há um vencedor após a jogada
        val vencedor = verificaVencedor(tabuleiro)

        if (!vencedor.isNullOrBlank()) {
            Toast.makeText(this, "Vencedor: $vencedor", Toast.LENGTH_LONG).show()
            reiniciarJogo()
        } else {
            alternarJogador()
        }
    }

    // Função para alternar o jogador atual
    private fun alternarJogador() {
        jogadorAtual = if (jogadorAtual == "X") "O" else "X"
        binding.textViewEstado.text = "Vez do Jogador $jogadorAtual"

        // Se o modo de jogo for contra o computador e for a vez do "O", faça a jogada do computador
        if (modoDeJogo == "computador" && jogadorAtual == "O") {
            Handler().postDelayed({
                jogadaComputador()
            }, 1000) // Atraso de 1 segundo (1000 milissegundos)
        }
    }

    // Função para a jogada do computador
    private fun jogadaComputador() {
        if (nivelDeDificuldade == "facil") {
            jogadaComputadorFacil()
        } else {
            jogadaComputadorDificil()
        }
    }

    // Função para a jogada do computador no nível fácil
    private fun jogadaComputadorFacil() {
        var rX: Int
        var rY: Int
        var i = 0

        // Inicializar rX e rY para evitar erro de compilação
        rX = 0
        rY = 0

        // Realizar até encontrar uma posição válida no tabuleiro
        while (i < 9) {
            rX = (0..2).random()
            rY = (0..2).random()

            if (tabuleiro[rX][rY] != "X" && tabuleiro[rX][rY] != "O") {
                break
            }

            i++
        }

        tabuleiro[rX][rY] = "O"
        atualizarTabuleiro(rX, rY)

        // Verificar se há um vencedor após a jogada do computador
        val vencedor = verificaVencedor(tabuleiro)

        if (!vencedor.isNullOrBlank()) {
            Toast.makeText(this, "Vencedor: $vencedor", Toast.LENGTH_LONG).show()
            reiniciarJogo()
        } else {
            alternarJogador()
        }
    }

    // Função para a jogada do computador no nível difícil
    private fun jogadaComputadorDificil() {
        val melhorMovimento = minimax(tabuleiro, true)
        val rX = melhorMovimento[1]
        val rY = melhorMovimento[2]
        tabuleiro[rX][rY] = "O"
        atualizarTabuleiro(rX, rY)

        // Verificar se há um vencedor após a jogada do computador
        val vencedor = verificaVencedor(tabuleiro)

        if (!vencedor.isNullOrBlank()) {
            Toast.makeText(this, "Vencedor: $vencedor", Toast.LENGTH_LONG).show()
            reiniciarJogo()
        } else {
            alternarJogador()
        }
    }

    private fun minimax(tabuleiro: Array<Array<String>>, isMaximizing: Boolean): IntArray {
        val vencedor = verificaVencedor(tabuleiro)
        if (vencedor != null) {
            return when (vencedor) {
                "X" -> intArrayOf(-1)
                "O" -> intArrayOf(1)
                "Empate" -> intArrayOf(0)
                else -> intArrayOf(0)
            }
        }

        val movimentos = mutableListOf<IntArray>()

        for (i in 0..2) {
            for (j in 0..2) {
                if (tabuleiro[i][j] == "") {
                    val movimento = IntArray(3)
                    movimento[1] = i
                    movimento[2] = j
                    tabuleiro[i][j] = if (isMaximizing) "O" else "X"
                    val resultado = minimax(tabuleiro, !isMaximizing)
                    movimento[0] = resultado[0]
                    movimentos.add(movimento)
                    tabuleiro[i][j] = ""
                }
            }
        }

        return if (isMaximizing) {
            movimentos.maxByOrNull { it[0] } ?: intArrayOf()
        } else {
            movimentos.minByOrNull { it[0] } ?: intArrayOf()
        }
    }

    private fun atualizarTabuleiro(rX: Int, rY: Int) {
        val posicao = rX * 3 + rY
        when (posicao) {
            0 -> {
                binding.buttonZero.setBackgroundResource(R.drawable.vasco)
                binding.buttonZero.isEnabled = false
            }
            1 -> {
                binding.buttonUm.setBackgroundResource(R.drawable.vasco)
                binding.buttonUm.isEnabled = false
            }
            2 -> {
                binding.buttonDois.setBackgroundResource(R.drawable.vasco)
                binding.buttonDois.isEnabled = false
            }
            3 -> {
                binding.buttonTres.setBackgroundResource(R.drawable.vasco)
                binding.buttonTres.isEnabled = false
            }
            4 -> {
                binding.buttonQuatro.setBackgroundResource(R.drawable.vasco)
                binding.buttonQuatro.isEnabled = false
            }
            5 -> {
                binding.buttonCinco.setBackgroundResource(R.drawable.vasco)
                binding.buttonCinco.isEnabled = false
            }
            6 -> {
                binding.buttonSeis.setBackgroundResource(R.drawable.vasco)
                binding.buttonSeis.isEnabled = false
            }
            7 -> {
                binding.buttonSete.setBackgroundResource(R.drawable.vasco)
                binding.buttonSete.isEnabled = false
            }
            8 -> {
                binding.buttonOito.setBackgroundResource(R.drawable.vasco)
                binding.buttonOito.isEnabled = false
            }
        }
    }

    // Função para verificar se há um vencedor
    fun verificaVencedor(tabuleiro: Array<Array<String>>): String? {
        // Verificar linhas e colunas
        for (i in 0..2) {
            // Verificar se há três itens iguais na linha
            if (tabuleiro[i][0] == tabuleiro[i][1] && tabuleiro[i][1] == tabuleiro[i][2] && tabuleiro[i][0].isNotEmpty()) {
                return tabuleiro[i][0]
            }
            // Verificar se há três itens iguais na coluna
            if (tabuleiro[0][i] == tabuleiro[1][i] && tabuleiro[1][i] == tabuleiro[2][i] && tabuleiro[0][i].isNotEmpty()) {
                return tabuleiro[0][i]
            }
        }
        // Verificar diagonais
        if (tabuleiro[0][0] == tabuleiro[1][1] && tabuleiro[1][1] == tabuleiro[2][2] && tabuleiro[0][0].isNotEmpty()) {
            return tabuleiro[0][0]
        }
        if (tabuleiro[0][2] == tabuleiro[1][1] && tabuleiro[1][1] == tabuleiro[2][0] && tabuleiro[0][2].isNotEmpty()) {
            return tabuleiro[0][2]
        }
        // Verificar empate
        var empate = 0
        for (linha in tabuleiro) {
            for (valor in linha) {
                if (valor == "X" || valor == "O") {
                    empate++
                }
            }
        }
        // Se existem 9 jogadas e não há três letras iguais, houve um empate
        if (empate == 9) {
            return "Empate"
        }
        // Nenhum vencedor
        return null
    }

    // Função para reiniciar o jogo
    private fun reiniciarJogo() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Função para habilitar ou desabilitar os botões do tabuleiro
    private fun habilitarBotoes(habilitar: Boolean) {
        binding.buttonZero.isEnabled = habilitar
        binding.buttonUm.isEnabled = habilitar
        binding.buttonDois.isEnabled = habilitar
        binding.buttonTres.isEnabled = habilitar
        binding.buttonQuatro.isEnabled = habilitar
        binding.buttonCinco.isEnabled = habilitar
        binding.buttonSeis.isEnabled = habilitar
        binding.buttonSete.isEnabled = habilitar
        binding.buttonOito.isEnabled = habilitar
    }
}

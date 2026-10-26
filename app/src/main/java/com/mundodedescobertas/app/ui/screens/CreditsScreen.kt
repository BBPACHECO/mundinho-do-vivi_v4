package com.mundodedescobertas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Texto local incluído no APK; links não são abertos automaticamente. */
@Composable
fun CreditsScreen() {
    val context=LocalContext.current
    var notices by remember {mutableStateOf("Carregando os créditos locais…")}
    LaunchedEffect(context) {
        notices=withContext(Dispatchers.IO) {
            runCatching {
                val base=context.assets.open("legal/creditos.txt").bufferedReader().use{it.readText()}
                val sounds=JSONArray(context.assets.open("dj/credits.json").bufferedReader().use{it.readText()})
                buildString {
                    append(base)
                    append("\n\nGRAVAÇÕES REAIS CC0\n")
                    for(i in 0 until sounds.length()) {
                        val sound=sounds.getJSONObject(i)
                        append("\n${sound.optString("title")}\nAutor: ${sound.optString("author")}\nLicença: ${sound.optString("license")}\nFonte: ${sound.optString("source")}\n")
                    }
                    append("\nEsta versão traz o Corvo como gravação animal. Cachorro, gato, pato, vaca, cavalo, leão, passarinho e macaco ainda precisam de arquivos licenciados adequados. Não são substituídos por imitações sintetizadas.\n")
                    append("\nCâmera: acessórios 2D rastreados e deformação real de imagem. Não é reconstrução 3D; iluminação e ângulo influenciam o resultado. A qualidade dos filtros e do áudio ainda precisa ser validada no Galaxy A57.\n")
                }
            }
                .getOrDefault("Não foi possível abrir os créditos. Consulte o README do projeto.")
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Text("Feito com carinho e respeito",style=MaterialTheme.typography.headlineMedium)
        Text("As atividades, fotos, pinturas e músicas ficam no aparelho. O aplicativo não tem permissão de internet, não envia imagens e não utiliza anúncios ou cadastro.",style=MaterialTheme.typography.bodyLarge)
        Text(notices,style=MaterialTheme.typography.bodyMedium)
    }
}

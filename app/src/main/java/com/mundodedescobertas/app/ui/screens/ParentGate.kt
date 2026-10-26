package com.mundodedescobertas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mundodedescobertas.app.data.AdultChallenge

@Composable
fun ParentGate(onUnlock: () -> Unit) {
    val challenge = remember { AdultChallenge.create() }
    var answer by remember { mutableStateOf("") }
    var incorrect by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).widthIn(max=560.dp),
        verticalArrangement=Arrangement.spacedBy(20.dp),horizontalAlignment=Alignment.Start) {
        Text("Um momento com o responsável",style=MaterialTheme.typography.headlineMedium)
        Text("Peça a um adulto para continuar. Esta verificação simples separa as configurações das brincadeiras; não é uma senha.",style=MaterialTheme.typography.bodyLarge)
        Text("Quanto é ${challenge.first} × ${challenge.second}?",style=MaterialTheme.typography.titleLarge,modifier=Modifier.testTag("adult_challenge"))
        OutlinedTextField(value=answer,onValueChange={answer=it.filter(Char::isDigit).take(3);incorrect=false},
            label={Text("Resposta")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
            isError=incorrect,supportingText={if(incorrect) Text("Vamos tentar outra vez? Peça ajuda a um adulto.")},
            modifier=Modifier.fillMaxWidth().testTag("adult_answer"))
        Button(onClick={if(challenge.accepts(answer)) onUnlock() else incorrect=true},enabled=answer.isNotEmpty(),modifier=Modifier.heightIn(min=52.dp)) { Text("Entrar nas configurações") }
    }
}

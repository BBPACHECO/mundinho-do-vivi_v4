package com.mundodedescobertas.app.dj

/** Source names map directly to embedded, credited PCM WAV recordings. */
data class DjPad(val title:String,val category:String,val source:String)
object DjCatalog {
    val pads=listOf(
        DjPad("Bumbo","Bateria","drum_heavy_kick"),DjPad("Caixa","Bateria","drum_snare_soft"),
        DjPad("Chimbal fechado","Bateria","drum_cymbal_closed"),DjPad("Chimbal aberto","Bateria","drum_cymbal_open"),
        DjPad("Tom grave","Bateria","drum_tom_lo_soft"),DjPad("Tom agudo","Bateria","drum_tom_hi_soft"),
        DjPad("Sino de percussão","Bateria","drum_cowbell"),DjPad("Rufar","Bateria","drum_roll"),
        DjPad("Piano","Musicais","ambi_piano"),DjPad("Guitarra suave","Musicais","guit_harmonics"),
        DjPad("Guitarra em Mi","Musicais","guit_e_fifths"),DjPad("Baixo","Musicais","bass_woodsy_c"),
        DjPad("Sino","Musicais","perc_bell"),DjPad("Sino eletrônico","Musicais","elec_bell"),
        DjPad("Estalo","Musicais","perc_snap"),DjPad("Outro estalo","Musicais","perc_snap2"),
        DjPad("Arroto","Gravações","misc_burp"),DjPad("Corvo","Gravações","misc_crow"),
        DjPad("Porta","Gravações","perc_door"),DjPad("Caixa registradora","Gravações","perc_till"),
        DjPad("Vidro friccionado","Gravações","ambi_glass_rub"),DjPad("Vidro ressoando","Gravações","ambi_glass_hum"),
        DjPad("Sauna","Gravações","ambi_sauna"),DjPad("Scratch de vinil","Gravações","vinyl_scratch"),
        DjPad("Carrilhão digital","Eletrônicos","elec_chime"),DjPad("Bipe","Eletrônicos","elec_beep"),
        DjPad("Blip","Eletrônicos","elec_blip"),DjPad("Ping","Eletrônicos","elec_ping"),
        DjPad("Pop eletrônico","Eletrônicos","elec_pop"),DjPad("Twang","Eletrônicos","elec_twang"),
        DjPad("Gota eletrônica","Eletrônicos","elec_plip"),DjPad("Bolha eletrônica","Eletrônicos","elec_blup"),
    )
    val rhythms=listOf("Hip-hop","Boom bap","Funk instrumental","Eletrônica","House","Pop","Disco","Reggae","Rock","Trap","Lo-fi","Percussão","Dance","Ritmo infantil","Samba instrumental","Batida livre")
    val categories=pads.map{it.category}.distinct()
}

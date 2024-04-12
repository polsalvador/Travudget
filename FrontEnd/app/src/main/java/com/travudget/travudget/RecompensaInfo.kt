package com.travudget.travudget

import java.io.Serializable

data class RecompensaInfo(
    var idRecompensa: String,
    var nomRecompensa: String,
    var preu: Int,
    var codi: String
) : Serializable
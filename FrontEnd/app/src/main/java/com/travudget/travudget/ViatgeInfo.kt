package com.travudget.travudget

import java.util.Date
import java.io.Serializable

data class ViatgeInfo(
    var viatgeId: String,
    var nomViatge: String,
    var dataInici: Date?,
    var dataFi: Date?,
    var divisa: String,
    var pressupostTotal: Int,
    var pressupostVariable: MutableMap<String, Int>,
    var deutes: MutableMap<String, Int>,
    var codi: String,
    var creadorId: Int,
    var participants: List<String>
) : Serializable
package com.travudget.travudget

import java.io.Serializable
import java.util.Date
class DespesaInfo(
    var nomDespesa: String,
    var viatgeId: String?,
    var emailCreador: String?,
    var descripcio: String?,
    var preu: Int,
    var categoria: String,
    var dataInici: Date?,
    var dataFi: Date?,
    var ubicacio_lat: Double?,
    var ubicacio_long: Double?,
    var deutors: Map<String, Int>?
): Serializable
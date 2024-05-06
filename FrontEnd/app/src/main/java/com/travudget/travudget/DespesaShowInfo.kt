package com.travudget.travudget

import java.util.Date
import java.io.Serializable

data class DespesaShowInfo(
    var despesaId: String?,
    var nomDespesa: String,
    var emailCreador: String?,
    var dataInici: Date,
    var preu: Int,
    var categoria: String,
    var deutors: Map<String, Int>?
): Serializable
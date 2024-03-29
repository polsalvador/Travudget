package com.travudget.travudget

import java.util.Date
import java.io.Serializable

class DespesaShowInfo(
    var despesaId: String?,
    var nomDespesa: String,
    var dataInici: Date,
    var preu: Int,
    var categoria: String
): Serializable
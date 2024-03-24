package com.travudget.travudget

import java.util.Date

data class ViatgeInfo(
    val nomViatge: String,
    val dataInici: Date,
    val dataFi: Date?,
    val divisa: String,
    val pressupostTotal: Int,
    val pressupostVariable: List<Any>,
    val deutes: List<Any>,
    val codi: String,
    val creadorId: Int,
    val participantsIds: List<Any>
)
package ar.edu.algo3.monedas.dto

import java.math.BigDecimal

data class Conversion(
    val valorAConvertir: BigDecimal,
    val monedaAConvertir: String
)
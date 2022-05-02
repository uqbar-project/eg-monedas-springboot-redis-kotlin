package ar.edu.algo3.monedas.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

//@RedisHash("Moneda", timeToLive = 60)
@RedisHash
class Moneda {
    @JsonIgnore
    lateinit var cotizacionMoneda: BigDecimal

    @Id
    lateinit var descripcion: String

    fun convertirAPesos(unValor: BigDecimal) =
        (unValor * cotizacionMoneda).setScale(2)

    fun convertirDePesosAMoneda(unValor: BigDecimal) =
        unValor.divide(cotizacionMoneda, 2, RoundingMode.HALF_UP).toString()

    fun getCotizacionDeMoneda() = DecimalFormat("#,###,##0.00").format(cotizacionMoneda).replace(".", ",")

}

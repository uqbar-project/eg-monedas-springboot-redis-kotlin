package ar.edu.algo3.monedas.service

import ar.edu.algo3.monedas.dto.Conversion
import ar.edu.algo3.monedas.errorHandling.NotFoundException
import ar.edu.algo3.monedas.repository.MonedaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MonedaService {

    @Autowired
    lateinit var monedasRepository: MonedaRepository

    fun getMonedas() = monedasRepository.findAll()

    protected fun getMoneda(conversion: Conversion) =
        monedasRepository
            .findById(conversion.monedaAConvertir)
            .orElseThrow { NotFoundException("La moneda a convertir no existe") }

    fun convertirMonedaAPesos(conversion: Conversion) =
        getMoneda(conversion).convertirAPesos(conversion.valorAConvertir)

    fun convertirPesosAMoneda(conversion: Conversion) =
        getMoneda(conversion).convertirDePesosAMoneda(conversion.valorAConvertir)

}
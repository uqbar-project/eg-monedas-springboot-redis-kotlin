package ar.edu.algo3.monedas

import ar.edu.algo3.monedas.domain.Moneda
import ar.edu.algo3.monedas.repository.MonedaRepository
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class MonedasBootstrap : InitializingBean {

    @Autowired
    lateinit var monedaRepository: MonedaRepository

    fun initMonedas() {
        crearMoneda(1,"24.01", "Zloty")
        crearMoneda(2, "1.22", "Rupia india")
        crearMoneda(3, "6.35", "Florín")
    }

    protected fun crearMoneda(idMoneda: Int, valor: String, descripcionMoneda: String) =
        monedaRepository.save(Moneda().apply {
            cotizacionMoneda = BigDecimal(valor)
            descripcion = descripcionMoneda
        })

    override fun afterPropertiesSet() {
        println("************************************************************************")
        println("Running initialization")
        println("************************************************************************")
        initMonedas()
    }

}
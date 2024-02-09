package ar.edu.algo3.monedas.controller

import ar.edu.algo3.monedas.dto.Conversion
import ar.edu.algo3.monedas.service.MonedaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("*")
class MonedaController {

    @Autowired
    lateinit var monedaService: MonedaService

    @GetMapping("/monedas")
    fun getMonedas() = monedaService.getMonedas()

    @PutMapping("/monedaAPesos")
    fun getMonedasAPesos(@RequestBody conversion: Conversion) =
        monedaService.convertirMonedaAPesos(conversion)

    @PutMapping("/pesosAMoneda")
    fun getPesosAMonedas(@RequestBody conversion: Conversion) =
        monedaService.convertirPesosAMoneda(conversion)

}
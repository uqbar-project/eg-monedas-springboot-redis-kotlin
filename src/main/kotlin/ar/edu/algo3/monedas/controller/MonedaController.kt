package ar.edu.algo3.monedas.controller

import ar.edu.algo3.monedas.dto.Conversion
import ar.edu.algo3.monedas.service.MonedaService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class MonedaController {

    @Autowired
    lateinit var monedaService: MonedaService

    @GetMapping("/monedas")
    @ApiOperation("Recupera información de los valores de cada moneda.")
    fun getMonedas() = monedaService.getMonedas()

    @PutMapping("/monedaAPesos")
    @ApiOperation("Convierte un valor de una moneda determinada a pesos. Para conocer la lista de monedas disponibles tenés el endpoint /GET. Se distinguen mayúsculas de minúsculas. Ejemplo: si 1 zloty está 24 pesos, al convertir 10 zlotys obtendremos 240 pesos.")
    fun getMonedasAPesos(@RequestBody conversion: Conversion) =
        monedaService.convertirMonedaAPesos(conversion)

    @PutMapping("/pesosAMoneda")
    @ApiOperation("Convierte un valor en pesos a una moneda determinada a pesos. Para conocer la lista de monedas disponibles tenés el endpoint /GET. Se distinguen mayúsculas de minúsculas. Ejemplo: si 1 zloty está 24 pesos, al convertir 12 pesos obtendremos 0.5 zlotys.")
    fun getPesosAMonedas(@RequestBody conversion: Conversion) =
        monedaService.convertirPesosAMoneda(conversion)

}
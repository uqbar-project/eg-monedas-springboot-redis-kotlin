package ar.edu.algo3.monedas.repository

import ar.edu.algo3.monedas.domain.Moneda
import org.springframework.data.repository.CrudRepository

interface MonedaRepository : CrudRepository<Moneda, String>

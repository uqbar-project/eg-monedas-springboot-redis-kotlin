package ar.edu.algo3.monedas


import ar.edu.algo3.monedas.dto.Conversion
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Dado un controller de monedas")
class MonedaControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    val mapper = ObjectMapper()

    @Test
    fun `convertir de una moneda a pesos - caso feliz`() {
        val conversion = Conversion(BigDecimal(10), "Zloty")
        convertir("/monedaAPesos", conversion)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("240.1"))
    }

    @Test
    fun `convertir de una moneda a pesos inexistente da error`() {
        val conversion = Conversion(BigDecimal(48.02), "patacones")
        convertir("/monedaAPesos", conversion)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `convertir de pesos a una moneda - caso feliz`() {
        val conversion = Conversion(BigDecimal(48.02), "Zloty")
        convertir("/pesosAMoneda", conversion)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("2.0"))
    }

    @Test
    fun `convertir de pesos a una moneda inexistente da error`() {
        val conversion = Conversion(BigDecimal(48.02), "patacones")
        convertir("/pesosAMoneda", conversion)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    protected fun convertir(url: String, conversion: Conversion): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).content(
                mapper.writeValueAsString(conversion)
            )
        )
    }

}
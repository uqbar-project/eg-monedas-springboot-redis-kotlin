package ar.edu.algo3.monedas


import ar.edu.algo3.monedas.dto.Conversion
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.math.BigDecimal
import org.mockito.verification.After
import redis.embedded.RedisServer


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Dado un controller de monedas")
class MonedaControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, String>

    lateinit var redisServer: RedisServer

    val mapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        redisServer = RedisServer.builder().port(6370).build()
        redisServer.start()
    }

    @AfterEach
    fun tearDown(){
        redisServer.stop()
    }

    @Test
    @DisplayName("podemos convertir de una moneda a pesos")
    fun conversionAPesos() {
        val conversion = Conversion(BigDecimal(10), "Zloty")
        val responseEntity = performGet("/monedaAPesos", conversion)
        assertEquals(HttpStatus.OK.value(), responseEntity.status)
        assertEquals("240.10", responseEntity.contentAsString)
    }

    @Test
    @DisplayName("podemos convertir de pesos a una moneda")
    fun conversionAMoneda() {
        val conversion = Conversion(BigDecimal(48.02), "Zloty")
        val responseEntity = performGet("/pesosAMoneda", conversion)
        assertEquals(HttpStatus.OK.value(), responseEntity.status)
        assertEquals("2.00", responseEntity.contentAsString)
    }

    @Test
    @DisplayName("no podemos convertir de una moneda inexistente")
    fun conversionAMonedaInexistente() {
        val conversion = Conversion(BigDecimal(48.02), "patacones")
        val responseEntity = performGet("/pesosAMoneda", conversion)
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.status)
    }

    protected fun performGet(url: String, conversion: Conversion): MockHttpServletResponse {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).content(
                mapper.writeValueAsString(conversion)
            )
        ).andReturn().response
    }

}
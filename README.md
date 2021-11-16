# Ejemplo Monedas con Springboot y Redis

[![build](https://github.com/uqbar-project/eg-monedas-springboot-redis-kotlin/actions/workflows/build.yml/badge.svg)](https://github.com/uqbar-project/eg-monedas-springboot-redis-kotlin/actions/workflows/build.yml) [![codecov](https://codecov.io/gh/uqbar-project/eg-monedas-springboot-redis-kotlin/branch/master/graph/badge.svg?token=XWIXvKlnYK)](https://codecov.io/gh/uqbar-project/eg-monedas-springboot-redis-kotlin)

## Levantar Redis localmente

Una vez que [instalaste Redis](https://redis.io/download) y lo descargaste en una carpeta, hacés el build local y levantás el server y el cliente en dos terminales diferentes:

```bash
make
./src/redis-server
./src/redis-client  # en otra terminal
```

## Levantar Redis dockerizado

```bash
docker pull redis
```

Luego hay que levantar el servicio Redis la primera vez:

```bash
docker run --name redis -p 6379:6379 -d redis
```

Para levantar el servicio una vez que generamos el container simplemente hacemos

```bash
docker start redis
```

Y nos podemos conectar a cualquier cliente Redis al host 127.0.0.1, puerto 6379.

## Carga de datos iniciales

Para cargar los datos iniciales, tenés que ejecutar el script que está en Node (asumimos que ya lo tenés instalado):

```bash
cd datosInicialesNode
node cargaDatosInicial.js 
```

Eso te permitirá crear las claves para monedas como euro, dólar y peso y visualizarlas en algún cliente como [Redis Commander](https://github.com/joeferner/redis-commander).

![Redis Commander](./images/redisCommander.gif)

## Endpoints en springboot

### Objetos de dominio

Nuestro modelo en Springboot no va a trabajar con los datos iniciales que generamos en Node, sino que va a tener una estructura más **opaca**, determinada por las annotations de nuestro objeto de dominio Moneda:

```kt
@RedisHash("Moneda")
class Moneda {
    @JsonIgnore
    lateinit var cotizacionMoneda: BigDecimal

    @Id
    lateinit var descripcion: String
```

- `@RedisHash` es la clave que va a agrupar todos los objetos Moneda en una lista de valores en Redis
- `@Id` es el índice interno por el cual vamos a poder recuperar una moneda (ya que la única consulta que tiene sentido es buscar por identificador, si queremos buscar las monedas cuya cotización es mayor a 5 no es Redis la tecnología más apropiada)

### Repositorio

La interfaz que propone Spring boot con Redis es idéntica a la del modelo relacional (aun cuando su implementación es bastante diferente):

```kt
interface MonedaRepository : CrudRepository<Moneda, String> {}
```

Como la interfaz ya propone `findAll` y `findById` que es lo que queremos hacer, no tenemos que definir mensajes adicionales.

### Service

El service define dos métodos de conversión:

```kt
protected fun getMoneda(conversion: Conversion) =
    monedasRepository
        .findById(conversion.monedaAConvertir)
        .orElseThrow { NotFoundException("La moneda a convertir no existe") }

@Transactional(readOnly = true)
fun convertirMonedaAPesos(conversion: Conversion) =
    getMoneda(conversion).convertirAPesos(conversion.valorAConvertir)

@Transactional(readOnly = true)
fun convertirPesosAMoneda(conversion: Conversion) =
    getMoneda(conversion).convertirDePesosAMoneda(conversion.valorAConvertir)
```

La anotación `@Transactional(readOnly = true)` indica al service que no debe iniciar una transacción. De esa manera utiliza menos recursos.

### Controller

Vamos a definir tres endpoints:

- uno que permita conocer todas las monedas
- otro que convierta de pesos a una moneda en cuestión
- y otro que convierta de la moneda en cuestión a pesos

El primer endpoint se implementa con un método GET, los otros dos si bien no tienen efecto colateral, necesitamos pasar cierta información y nos parece mejor hacerla por el body antes que tener que encadenar en la URL todos los valores por query params.

La implementación de la búsqueda de todas las monedas delega a un service de Springboot:

```kt
@GetMapping("/monedas")
@ApiOperation("Recupera información de los valores de cada moneda.")
fun getMonedas() = monedaService.getMonedas()
```

Tampoco es muy complejo el endpoint de conversión, solo que

- nos fue útil modelar el service para que busque la moneda y haga la conversión
- si no encuentra la moneda el service tira un error NotFoundException que se termina mapeando con un código de error de http 404
- delegamos al objeto de dominio la responsabilidad de la conversión (no olvidarse de dejar que cada objeto cumpla su responsabilidad)

En el controller:

```kt
@PutMapping("/monedaAPesos")
@ApiOperation("Convierte un valor de una moneda determinada a pesos. Para conocer la lista de monedas disponibles tenés el endpoint /GET. Se distinguen mayúsculas de minúsculas. Ejemplo: si 1 zloty está 24 pesos, al convertir 10 zlotys obtendremos 240 pesos.")
fun getMonedasAPesos(@RequestBody conversion: Conversion) =
    monedaService.convertirMonedaAPesos(conversion)
```

La implementación del service ya la hemos presentado.

Por último, en el archivo `ErrorHandling.xtend` definimos la asociación de la excepción con un código de error http:

```kt
@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(override val message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class UserException(override val message: String) : RuntimeException(message)
```

### Testeo de integración

Levantar el servicio de Redis antes de ejecutar los tests de integración, algo que también hacemos en el build del CI. 

Los casos de prueba son:

- la conversión de una moneda X a pesos
- la conversión de pesos a una moneda X
- que al tratar de convertir una moneda inexistente recibimos un código de error 404

Los elementos involucrados en el test de integración son:

- controller
- service
- repositorio
- objetos de dominio mapeados contra Redis

```kt
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Dado un controller de monedas")
class MonedaControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    val mapper = ObjectMapper()

    @Test
    @DisplayName("podemos convertir de una moneda a pesos")
    fun conversionAPesos() {
        val conversion = Conversion(BigDecimal(10), "Zloty")
        val responseEntity = performGet("/monedaAPesos", conversion)
        assertEquals(HttpStatus.OK.value(), responseEntity.status)
        assertEquals("240.10", responseEntity.contentAsString)
    }
```

## Cómo testear la aplicación en Insomnia

Te dejamos [el archivo de Insomnia](./Insomnia_Monedas_Redis.json) con ejemplos para probarlo.

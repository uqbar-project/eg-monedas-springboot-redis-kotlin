# Ejemplo Monedas con Springboot y Redis



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

```xtend
@RedisHash("Moneda")
@Accessors
class Moneda {
	@JsonIgnore
	BigDecimal cotizacionMoneda
	
	@Id
	String descripcion
```

- `@RedisHash` es la clave que va a agrupar todos los objetos Moneda en una lista de valores en Redis
- `@Id` es el índice interno por el cual vamos a poder recuperar una moneda (ya que la única consulta que tiene sentido es buscar por identificador, si queremos buscar las monedas cuya cotización es mayor a 5 no es Redis la tecnología más apropiada)

### Repositorio

La interfaz que propone Spring boot con Redis es idéntica a la del modelo relacional (aun cuando su implementación es bastante diferente):

```xtend
interface MonedasRepository extends CrudRepository<Moneda, String> {}
```

Como la interfaz ya propone `findAll` y `findById` que es lo que queremos hacer, no tenemos que definir mensajes adicionales.

### Controller

Vamos a definir tres endpoints:

- uno que permita conocer todas las monedas
- otro que convierta de pesos a una moneda en cuestión
- y otro que convierta de la moneda en cuestión a pesos

El primer endpoint se implementa con un método GET, los otros dos si bien no tienen efecto colateral, necesitamos pasar cierta información y nos parece mejor hacerla por el body antes que tener que encadenar en la URL todos los valores por query params.

La implementación de la búsqueda de todas las monedas delega a un service de Springboot:

```xtend
	@GetMapping("/monedas")
	@ApiOperation("Recupera información de los valores de cada moneda.")
	def getMonedas() {
		monedasService.getMonedas()
	}
```

El service no tiene demasiada responsabilidad en este caso, solo delega al repositorio:

```xtend
	def getMonedas() {
		this.monedasRepository.findAll
	}
```

Tampoco es muy complejo el endpoint de conversión, solo que

- nos fue útil modelar el service para que busque la moneda y haga la conversión
- si no encuentra la moneda el service tira un error NotFoundException que se termina mapeando con un código de error de http 404
- delegamos al objeto de dominio la responsabilidad de la conversión (no olvidarse de dejar que cada objeto cumpla su responsabilidad)

En el controller:

```xtend
	@PutMapping("/monedaAPesos/")
	@ApiOperation("Convierte un valor de una moneda determinada a pesos. Para conocer la lista de monedas disponibles tenés el endpoint /GET. Se distinguen mayúsculas de minúsculas. Ejemplo: si 1 zloty está 24 pesos, al convertir 10 zlotys obtendremos 240 pesos.")
	def getMonedasAPesos(@RequestBody Conversion conversion) {
		monedasService.convertirMonedaAPesos(conversion)
	}
```

En el service:

```xtend
	protected def Moneda getMoneda(Conversion conversion) {
		monedasRepository
			.findById(conversion.monedaAConvertir)
			.orElseThrow [ new NotFoundException("La moneda a convertir no existe") ]
	}
	
	def convertirMonedaAPesos(ar.edu.unsam.monedas.dto.Conversion conversion) {
		conversion.moneda.convertirAPesos(conversion.valorAConvertir)
	}
```

Recordemos que `conversion.moneda` es un shortcut del extension method `getMoneda` que toma como parámetro un objeto conversión. La conversión es un objeto que sirve para capturar los parámetros necesarios para convertir de una moneda a pesos o viceversa.

Por último, en el archivo `ErrorHandling.xtend` definimos la asociación de la excepción con un código de error http:

```xtend
@ResponseStatus(NOT_FOUND)
class NotFoundException extends RuntimeException {

	new(String message) {
		super(message)
	}
}
```


### Testeo de integración

Antes que nada hay que levantar el servicio de Redis (vía Docker o en forma local como se cuenta al comienzo del README). Entonces sí podemos ejecutar los tests de integración, que cargan la información de conversión de monedas y verifican:

- la conversión de una moneda X a pesos
- la conversión de pesos a una moneda X
- que al tratar de convertir una moneda inexistente recibimos un código de error 404

Los elementos involucrados en el test de integración son:

- controller
- service
- repositorio
- objetos de dominio mapeados contra Redis
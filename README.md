# Taller #1 — API REST de catálogo de productos

**Asignatura:** Servicios Web · Spring Boot
**Caso de estudio:** Café Soluble S.A. — *caso académico ficticio*

> El nombre **Café Soluble S.A.** se utiliza únicamente para contextualizar el aprendizaje en un entorno
> empresarial nicaragüense. La situación, los requerimientos, los datos, la API y la solución descritos
> en este repositorio son ficticios y fueron creados exclusivamente con fines académicos.

---

## Integrantes del equipo

| Integrante | Responsabilidad principal | Aporte técnico |
|---|---|---|
| *(por completar)* | Diseño técnico y modelado | Especificación, modelo `Producto` y commits asociados |
| *(por completar)* | Implementación de endpoints | `ProductoController`, rutas y commits asociados |
| *(por completar)* | Pruebas, documentación y arquitectura | Postman, README, diagrama y commits asociados |

---

## Recurso a modelar: `Producto`

| Atributo | Tipo Java | Descripción |
|---|---|---|
| `id` | `Long` | Identificador único del producto. |
| `nombre` | `String` | Nombre ficticio del producto. |
| `presentacion` | `String` | Presentación comercial. Ejemplo: `50 g`, `100 g`, `200 g`. |
| `categoria` | `String` | Categoría ficticia del producto. |
| `disponible` | `boolean` | Indica si el producto se encuentra disponible. |

Representación JSON de un producto:

```json
{
  "id": 3,
  "nombre": "Café Instantáneo Clásico",
  "presentacion": "100 g",
  "categoria": "Café soluble",
  "disponible": true
}
```

---

## Fase 1 — Especificación técnica de la API

> Esta especificación fue definida **antes** de escribir el controlador. La implementación de la Fase 3
> debe coincidir exactamente con esta tabla.

**URL base:** `http://localhost:8080`

| Operación | Método HTTP | Ruta | Entrada | Respuesta esperada | Código HTTP |
|---|---|---|---|---|---|
| Consultar todos los productos | `GET` | `/api/productos` | No requiere cuerpo. | Colección JSON con todos los productos. | `200 OK` |
| Consultar producto por ID | `GET` | `/api/productos/{id}` | Variable de ruta `id` (numérica). Sin cuerpo. | Un único objeto JSON `Producto`, el que corresponde al `id` solicitado. | `200 OK` |
| Registrar producto | `POST` | `/api/productos` | Cuerpo JSON con `nombre`, `presentacion`, `categoria` y `disponible`. El `id` **no** se envía: lo asigna el servidor. | El producto creado, ya con su `id` asignado por el servidor. | `201 Created` |
| Consultar producto inexistente | `GET` | `/api/productos/{id}` | Variable de ruta `id` que no existe en memoria. | Sin cuerpo de recurso: el servidor informa que el recurso no existe. | `404 Not Found` |

### Ejemplos de intercambio

**1) Consultar la colección**

```
GET /api/productos
```
```json
[
  { "id": 1, "nombre": "Café Instantáneo Clásico", "presentacion": "50 g",  "categoria": "Café soluble", "disponible": true },
  { "id": 2, "nombre": "Café Instantáneo Fuerte",  "presentacion": "100 g", "categoria": "Café soluble", "disponible": true }
]
```
→ `200 OK`

**2) Consultar un producto existente**

```
GET /api/productos/3
```
```json
{ "id": 3, "nombre": "Café Descafeinado", "presentacion": "100 g", "categoria": "Café soluble", "disponible": false }
```
→ `200 OK`

**3) Registrar un producto**

```
POST /api/productos
Content-Type: application/json
```
```json
{ "nombre": "Café con Leche Instantáneo", "presentacion": "200 g", "categoria": "Mezclas", "disponible": true }
```
Respuesta:
```json
{ "id": 9, "nombre": "Café con Leche Instantáneo", "presentacion": "200 g", "categoria": "Mezclas", "disponible": true }
```
→ `201 Created`

**4) Consultar un producto inexistente**

```
GET /api/productos/999
```
→ `404 Not Found` (sin cuerpo de recurso)

---

### Análisis obligatorio

**1. ¿Por qué la ruta utiliza un sustantivo y no una acción?**

Porque en REST la URI identifica **un recurso**, no una operación. El recurso es la cosa sobre la que se
trabaja (los productos), y la acción que se realiza sobre él ya la expresa el método HTTP. Si la ruta
llevara el verbo (`/obtenerProductos`, `/crearProducto`) estaríamos duplicando información que HTTP ya
transmite, y cada operación nueva obligaría a inventar una URI nueva. Con el sustantivo, una sola URI
(`/api/productos`) soporta consultar, registrar, actualizar y eliminar según el método que se use.

**2. ¿Qué diferencia existe entre una URI de colección y una URI de recurso individual?**

- **URI de colección** — `/api/productos`: representa el **conjunto completo** de productos. Un `GET`
  devuelve un arreglo JSON (que puede estar vacío) y un `POST` agrega un elemento *a ese conjunto*.
- **URI de recurso individual** — `/api/productos/{id}`: representa **un solo elemento** identificado por
  su `id`. Un `GET` devuelve un objeto JSON, no un arreglo, y puede fallar con `404` si ese elemento no
  existe (algo que nunca ocurre con la colección: una colección vacía sigue existiendo y responde `200`).

En resumen: la colección siempre existe, el elemento individual puede no existir; la colección devuelve
un arreglo, el elemento devuelve un objeto.

**3. ¿Por qué el método HTTP forma parte del significado de la operación?**

Porque la URI dice *sobre qué* se actúa y el método dice *qué se hace*. `GET /api/productos` y
`POST /api/productos` son la misma URI y sin embargo son dos operaciones distintas: la primera lee y la
segunda crea. El método además comunica el contrato semántico de la operación:

- `GET` es **seguro** (no modifica el estado del servidor) e **idempotente**: repetirlo da el mismo
  resultado. Por eso puede ser cacheado por navegadores y proxies.
- `POST` **no** es seguro ni idempotente: enviarlo dos veces crea dos productos.

Ese contrato es lo que permite que clientes, cachés e intermediarios que no conocen nuestra API sepan
cómo tratar cada petición.

**4. ¿Qué información debe viajar en la URI y cuál en JSON?**

- **En la URI** viaja la información que **identifica** el recurso: `/api/productos/3` — el `3` señala
  *cuál* producto queremos. Es un dato corto, no sensible, y forma parte de la dirección del recurso.
- **En el cuerpo JSON** viaja la información que **describe** el recurso: `nombre`, `presentacion`,
  `categoria`, `disponible`. Son varios campos, de tipos distintos, que constituyen el estado del
  producto y no su dirección.

Por eso el `POST` no lleva `id` en la ruta (todavía no existe el recurso, el servidor lo va a asignar) y
sí lleva todos los atributos en el cuerpo. Y por eso el `GET` por ID lleva el identificador en la ruta y
no requiere cuerpo alguno.

**5. ¿Qué código HTTP permite distinguir una consulta exitosa, una creación y un recurso inexistente?**

| Situación | Código | Qué comunica |
|---|---|---|
| Consulta exitosa | `200 OK` | La petición se procesó y la respuesta contiene el recurso solicitado. El estado del servidor no cambió. |
| Creación exitosa | `201 Created` | La petición se procesó y **se creó un recurso nuevo**. Es más preciso que `200`: informa al cliente que el estado del servidor cambió y que ahora existe un recurso que antes no existía. |
| Recurso inexistente | `404 Not Found` | La petición estaba bien formada, pero el recurso identificado por esa URI no existe. Es un error del cliente (familia `4xx`), no del servidor. |

La distinción importa porque el código de estado es la primera señal que lee el cliente: le permite
decidir si procesar el cuerpo, si reintentar o si mostrar un error, **sin tener que inspeccionar el JSON**.
Un `404` es una respuesta correcta de la API, no una falla: la API funcionó y respondió que ese producto
no está.

---

## Fase 2 — Configuración del proyecto

Proyecto creado en **IntelliJ IDEA** con **Spring Boot**, **Java** y **Maven**, con la dependencia
**Spring Web**. Los datos permanecen **en memoria**: no se utiliza base de datos.

| Elemento | Valor |
|---|---|
| Spring Boot | `3.5.0` |
| Java | `17` |
| Gestor de dependencias | Maven |
| Dependencia | `spring-boot-starter-web` |
| `groupId` | `com.cafesoluble` |
| `artifactId` | `catalogo-productos` |
| Paquete base | `com.cafesoluble.catalogo` |
| Puerto | `8080` |

### Estructura

```
catalogo-productos/
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/cafesoluble/catalogo/
    │   │   └── CatalogoProductosApplication.java   ← clase principal
    │   └── resources/
    │       └── application.properties
    └── test/java/com/cafesoluble/catalogo/
        └── CatalogoProductosApplicationTests.java
```

### La clase principal

`CatalogoProductosApplication.java` es el punto de entrada de la aplicación:

```java
@SpringBootApplication
public class CatalogoProductosApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogoProductosApplication.class, args);
    }
}
```

**¿Qué ocurre cuando se ejecuta?**

1. `@SpringBootApplication` combina tres anotaciones:
   - `@SpringBootConfiguration` — marca la clase como fuente de configuración.
   - `@EnableAutoConfiguration` — activa la autoconfiguración: Spring inspecciona el *classpath* y, al
     encontrar `spring-boot-starter-web`, configura automáticamente Tomcat, el `DispatcherServlet` y
     Jackson (el conversor objeto Java ↔ JSON).
   - `@ComponentScan` — escanea el paquete `com.cafesoluble.catalogo` y sus subpaquetes buscando
     componentes (`@RestController`, `@Service`, `@Component`) para registrarlos en el contenedor.
2. `SpringApplication.run(...)` crea el contexto de aplicación, registra esos componentes e
   **inicia el servidor Tomcat embebido en el puerto 8080**.
3. A partir de ese momento, toda petición HTTP entrante la recibe el `DispatcherServlet`, que la enruta
   al método del controlador cuya anotación coincida con el método HTTP y la ruta solicitados.

### Cómo ejecutar

Desde IntelliJ IDEA: abrir la clase `CatalogoProductosApplication` y ejecutar el método `main`.

Desde la terminal:

```bash
./mvnw spring-boot:run
```

**Evidencia de arranque correcto** — la consola debe mostrar:

```
Tomcat started on port 8080 (http) with context path '/'
Started CatalogoProductosApplication in 0.7 seconds
```

---

## Estado del taller

- [x] **Fase 1** — Especificación técnica de la API y análisis obligatorio.
- [x] **Fase 2** — Proyecto Spring Boot configurado y ejecutándose sin errores.
- [x] **Fase 3** — Modelar `Producto`, cargar 8 productos en memoria e implementar el controlador.
- [x] **Fase 4** — Matriz de 6 pruebas en Postman, documentadas e interpretadas.
- [x] **Fase 5** — Diagrama de arquitectura en draw.io con los dos recorridos.

---

## Fase 3 — Modelado e implementación

La implementación conserva sin cambios la especificación definida en la Fase 1: utiliza las mismas
rutas, métodos HTTP, formas de respuesta y códigos de estado. Por tanto, no fue necesario modificar ni
justificar ninguna desviación del contrato original.

### Cambios realizados y justificación

**1. Se creó `model/Producto.java`.**

La clase representa el recurso del dominio y contiene exactamente los cinco atributos especificados:
`id`, `nombre`, `presentacion`, `categoria` y `disponible`. Se incluyó un constructor vacío para que
Jackson pueda crear objetos a partir del JSON recibido en un `POST`, y un constructor con todos los
campos para cargar claramente los datos iniciales. Los métodos *getter* y *setter* permiten que Jackson
transforme automáticamente objetos Java a JSON y JSON a objetos Java.

**2. Se creó `controller/ProductoController.java` como controlador REST.**

`@RestController` registra la clase como componente web y hace que los valores retornados se escriban
directamente en el cuerpo HTTP como JSON. `@RequestMapping("/api/productos")` centraliza la ruta base,
evita repetirla en cada operación y mantiene un mapeo coherente con la URI de colección acordada en la
Fase 1. El controlador se encuentra bajo el paquete base de la aplicación para que el escaneo automático
de Spring lo detecte sin configuración adicional.

**3. Se cargaron ocho productos ficticios en memoria.**

Se utilizó una `ArrayList<Producto>` porque el alcance del taller exige persistencia temporal y excluye
una base de datos. Los productos se crean en el constructor del controlador con identificadores del 1
al 8, lo que garantiza que la colección esté disponible desde el arranque. Al reiniciar la aplicación,
los productos agregados durante la ejecución se pierden, comportamiento esperado para almacenamiento
en memoria.

**4. Se implementó `GET /api/productos`.**

`@GetMapping` sin una ruta adicional representa la consulta de la colección completa. El método retorna
la lista de productos; Spring y Jackson la convierten en un arreglo JSON y Spring responde
automáticamente con `200 OK`, tal como establece la especificación.

**5. Se implementó `GET /api/productos/{id}`.**

`@PathVariable` obtiene de la URI el identificador solicitado. La lista se recorre hasta encontrar un
producto con ese `id`. Se utilizó `ResponseEntity` porque esta operación tiene dos resultados HTTP
posibles: devuelve el producto y `200 OK` cuando existe, o una respuesta sin cuerpo y `404 Not Found`
cuando no existe.

**6. Se implementó `POST /api/productos`.**

`@RequestBody` convierte el JSON de entrada en un objeto `Producto`. El servidor reemplaza cualquier
`id` recibido y asigna uno propio mediante `AtomicLong`, iniciado en 9 porque los ocho productos
precargados ocupan los identificadores anteriores. Esto mantiene identificadores consecutivos y evita
duplicados si llegan solicitudes simultáneas. El producto se agrega a la colección y se retorna con su
nuevo identificador y el código `201 Created`, diferenciando correctamente una creación de una consulta.

### Estructura agregada

```text
src/main/java/com/cafesoluble/catalogo/
├── controller/
│   └── ProductoController.java
└── model/
    └── Producto.java
```

### Correspondencia con la especificación

| Requisito | Implementación | Resultado |
|---|---|---|
| Consultar la colección | `GET /api/productos` | Arreglo JSON y `200 OK` |
| Consultar por identificador | `GET /api/productos/{id}` | Objeto JSON y `200 OK` |
| Registrar un producto | `POST /api/productos` | Objeto creado con ID y `201 Created` |
| Consultar un ID inexistente | `GET /api/productos/{id}` | Respuesta sin cuerpo y `404 Not Found` |

---

## Fase 4 — Pruebas en Postman

Se creó la colección importable
[`postman/CatalogoProductos-Fase4.postman_collection.json`](postman/CatalogoProductos-Fase4.postman_collection.json)
con seis solicitudes ordenadas y pruebas automáticas. La colección usa la variable `baseUrl` con el
valor `http://localhost:8080` para mantener la dirección centralizada y la variable `createdId` para
pasar a las pruebas posteriores el identificador generado por el `POST`.

### Preparación y ejecución

1. Reiniciar la aplicación para recuperar el estado inicial de ocho productos en memoria.
2. Importar en Postman el archivo `postman/CatalogoProductos-Fase4.postman_collection.json`.
3. Abrir la colección **Café Soluble S.A. - Fase 4**.
4. Seleccionar **Run collection** y ejecutar las solicitudes en el orden numérico definido.
5. Confirmar que todas las aserciones de la pestaña **Test Results** aparezcan aprobadas.

El orden es importante porque la prueba 5 crea el producto con ID 9 y la prueba 6 verifica el cambio
causado por esa solicitud. Si la colección se ejecuta nuevamente sin reiniciar la aplicación,
habrá más de nueve productos y la prueba de cantidad fallará correctamente, pues el estado inicial ya
no será el documentado.

### Matriz de pruebas

Las seis solicitudes se ejecutaron contra la aplicación local el 31 de agosto de 2026, comenzando con
los ocho productos precargados.

| N.º | Objetivo | Método | URL | JSON de entrada | Código HTTP | Respuesta recibida | Conclusión técnica |
|---:|---|---|---|---|---|---|---|
| 1 | Verificar la consulta de la colección completa | `GET` | `/api/productos` | No corresponde | `200 OK` | Arreglo JSON con 8 productos, IDs 1–8 | La API retorna todos los productos en JSON; coincide con la especificación. |
| 2 | Verificar la consulta de un ID existente | `GET` | `/api/productos/3` | No corresponde | `200 OK` | Un objeto: **Café Descafeinado**, ID 3 | Retorna exactamente un recurso y no un arreglo; coincide con la especificación. |
| 3 | Verificar que el primer ID corresponda al recurso solicitado | `GET` | `/api/productos/1` | No corresponde | `200 OK` | Un objeto: **Café Instantáneo Clásico**, ID 1 | La respuesta corresponde al primer recurso solicitado; coincide con la especificación. |
| 4 | Verificar el manejo de un ID inexistente | `GET` | `/api/productos/999` | No corresponde | `404 Not Found` | Sin cuerpo, `Content-Length: 0` | El código comunica coherentemente que el recurso no existe; coincide con la especificación. |
| 5 | Verificar el registro de un producto válido | `POST` | `/api/productos` | JSON mostrado debajo, sin `id` | `201 Created` | Objeto creado con los datos enviados e ID 9 asignado | La API recibe JSON, asigna el ID y comunica la creación; coincide con la especificación. |
| 6 | Verificar que el producto nuevo aparezca en la colección | `GET` | `/api/productos` | No corresponde | `200 OK` | Arreglo con 9 productos que incluye el ID 9 | El producto creado permanece en memoria y aparece en la nueva consulta; coincide con la especificación. |

JSON enviado en la prueba 5:

```json
{
  "nombre": "Café Orgánico Instantáneo",
  "presentacion": "120 g",
  "categoria": "Café orgánico",
  "disponible": true
}
```

Respuesta obtenida:

```json
{
  "id": 9,
  "nombre": "Café Orgánico Instantáneo",
  "presentacion": "120 g",
  "categoria": "Café orgánico",
  "disponible": true
}
```

### Interpretación de los resultados

**Prueba 1 — colección inicial.** El arreglo de ocho elementos confirma que los datos ficticios se
cargan al iniciar la aplicación. El código `200` demuestra que la URI de colección existe y puede
consultarse correctamente.

**Prueba 2 — recurso existente.** La API interpretó el segmento `3` como variable de ruta y devolvió un
solo objeto, no un arreglo. La coincidencia entre el ID solicitado y el ID recibido demuestra que la
búsqueda individual funciona.

**Prueba 3 — primer ID existente.** La respuesta contiene el ID 1 y los datos de Café Instantáneo
Clásico. Esto comprueba específicamente el límite inferior de los identificadores precargados y confirma
que la respuesta corresponde al recurso solicitado.

**Prueba 4 — recurso inexistente.** El código `404` indica que la solicitud estaba bien formada, pero no
existe un producto con ID 999. La respuesta sin cuerpo evita presentar como producto una estructura de
error y respeta el contrato definido en la Fase 1.

**Prueba 5 — creación.** El cliente envió los datos descriptivos sin `id`; el servidor asignó el ID 9,
almacenó el objeto y lo devolvió con `201`. Esto confirma la conversión JSON a Java en la entrada y de
Java a JSON en la respuesta, además de distinguir semánticamente una creación exitosa.

**Prueba 6 — nueva consulta de la colección.** El aumento de ocho a nueve elementos demuestra que el `POST`
modificó el estado compartido de la aplicación. El producto persiste durante la ejecución actual porque
se encuentra en la lista en memoria. Además de comprobar la nueva cantidad, Postman busca el ID guardado
en `createdId` y verifica el nombre, confirmando que el elemento agregado es el que se envió en la prueba 5.

### Justificación de los cambios de esta fase

- Se agregó solamente un archivo de Postman; el código de la API no necesitó cambios porque su
  comportamiento coincide con la especificación.
- Cada solicitud contiene aserciones de código HTTP y contenido, de modo que el resultado no depende
  únicamente de una inspección visual.
- El `POST` guarda dinámicamente su ID en `createdId`; así las pruebas posteriores verifican el recurso
  realmente creado en vez de depender solo de un identificador escrito manualmente.
- Se documentaron preparación, entradas, resultados esperados, resultados reales e interpretación para
  que la ejecución sea repetible y verificable por otra persona.

---

## Fase 5 — Diagrama de arquitectura

El diagrama se encuentra en [`docs/arquitectura.drawio`](docs/arquitectura.drawio) y contiene **dos
páginas**, una por cada recorrido exigido. Para abrirlo: entrar a [draw.io](https://app.diagrams.net),
elegir `File` → `Open From` → `Device` y seleccionar el archivo. Las dos páginas aparecen como pestañas
en la parte inferior del editor.

| Página | Recorrido representado | Petición | Código |
|---|---|---|---|
| Recorrido A | Consulta exitosa | `GET /api/productos/3` | `200 OK` |
| Recorrido B | Recurso inexistente | `GET /api/productos/999` | `404 Not Found` |

### Bloques representados

Ambas páginas muestran la misma arquitectura; lo que cambia es el trayecto dibujado sobre ella.

- **Cliente / Postman** — origen de la petición HTTP y destino de la respuesta. Está fuera del
  contenedor de la aplicación porque es un proceso externo.
- **Aplicación Spring Boot** — contenedor que agrupa lo que vive dentro del proceso Java, rotulado con
  el servidor Tomcat embebido y el puerto 8080.
- **DispatcherServlet** — *Front Controller* de Spring. Recibe toda petición entrante y la enruta al
  método del controlador cuya anotación coincide con el método HTTP y la ruta solicitados.
- **ProductoController** — controlador REST, con sus anotaciones `@RestController`,
  `@RequestMapping("/api/productos")` y `@GetMapping("/{id}")`.
- **Productos en memoria** — el `List<Producto>` con los ocho productos precargados. Se dibuja como
  cilindro por convención de almacenamiento, aunque no es una base de datos: son objetos en memoria que
  se pierden al reiniciar la aplicación.
- **Jackson (ObjectMapper)** — componente que convierte el objeto Java en JSON.
- **HTTP Response** — la respuesta que sale hacia el cliente, rotulada con su código de estado.

### Recorrido A — consulta exitosa (`200 OK`)

| Paso | Qué ocurre |
|---:|---|
| 1 | Postman envía `GET /api/productos/3` al puerto 8080. |
| 2 | El `DispatcherServlet` enruta al método anotado con `@GetMapping("/{id}")` y liga `@PathVariable Long id = 3`. |
| 3 | El controlador busca el ID 3 dentro de la lista en memoria. |
| 4 | Lo encuentra y obtiene el **objeto Java `Producto`**. |
| 5 | `ResponseEntity.ok(producto)` entrega ese objeto a la capa de conversión. |
| 6 | Jackson serializa el objeto a JSON y lo escribe en el cuerpo de la respuesta. |
| 7 | La respuesta viaja a Postman con `200 OK`, `Content-Type: application/json` y el JSON del producto. |

### Recorrido B — recurso inexistente (`404 Not Found`)

| Paso | Qué ocurre |
|---:|---|
| 1 | Postman envía `GET /api/productos/999` al puerto 8080. |
| 2 | El `DispatcherServlet` enruta **al mismo método** y liga `@PathVariable Long id = 999`. |
| 3 | El controlador busca el ID 999 dentro de la lista en memoria. |
| 4 | No lo encuentra: no se obtiene ningún objeto Java. |
| 5 | `ResponseEntity.notFound().build()` construye una respuesta sin cuerpo; Jackson **no se invoca**. |
| 6 | La respuesta viaja a Postman con `404 Not Found` y sin cuerpo. |

### Qué cambia entre ambos recorridos

Los pasos 1 a 3 son **idénticos**: misma URI, mismo método HTTP, mismo `DispatcherServlet`, mismo método
del controlador y misma variable de ruta. La diferencia aparece en un único punto —el resultado de la
búsqueda— y de ahí se derivan todas las demás.

| | Recorrido A | Recorrido B |
|---|---|---|
| Resultado de la búsqueda | Encuentra el producto | No encuentra nada |
| Objeto Java obtenido | Un `Producto` | Ninguno |
| Participación de Jackson | Se ejecuta y serializa el objeto | Nunca se invoca: no hay nada que convertir |
| Cuerpo de la respuesta | JSON del producto | Vacío |
| Código de estado | `200 OK` | `404 Not Found` |
| Pasos totales | 7 | 6 |

En el código esa bifurcación es una sola expresión del método `obtenerPorId`:

```java
return productos.stream()
        .filter(producto -> producto.getId().equals(id))
        .findFirst()
        .map(ResponseEntity::ok)                              // recorrido A
        .orElseGet(() -> ResponseEntity.notFound().build());  // recorrido B
```

El `map` corresponde al recorrido A y el `orElseGet` al recorrido B. Por eso el diagrama del recorrido B
muestra el bloque de Jackson atenuado y con borde punteado: forma parte de la arquitectura, pero en ese
trayecto no llega a ejecutarse.

Conviene subrayar que el `404` **no representa una falla de la aplicación**. La petición estaba bien
formada, el servidor la procesó correctamente y respondió que el recurso identificado por esa URI no
existe. Por eso pertenece a la familia `4xx` —error del cliente, que solicitó algo inexistente— y no a
la familia `5xx`, reservada para errores del servidor.

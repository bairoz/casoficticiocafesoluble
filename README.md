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
- [ ] **Fase 4** — Matriz de 6 pruebas en Postman, documentadas e interpretadas.
- [ ] **Fase 5** — Diagrama de arquitectura en draw.io con los dos recorridos.

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

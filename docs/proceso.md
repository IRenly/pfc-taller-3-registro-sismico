# Algoritmo Factorial con Recursión de Cola

## Definición del Algoritmo

```Scala
def factorial(n: Int): BigInt = {
  @annotation.tailrec
  def loop(x: Int, acumulador: BigInt): BigInt = {
    if (x <= 1) acumulador
    else loop(x - 1, acumulador * x)
  }
  loop(n, 1)
}
```

* La función `factorial` calcula el factorial de un número `n` utilizando **recursión de cola**.
* La función interna `loop` es la que hace la recursión:

  * Recibe dos parámetros:

    * `x`: el valor actual decreciente hasta llegar a 1.
    * `acumulador`: donde se guarda el resultado parcial en cada paso.
* El decorador `@annotation.tailrec` obliga a que la función sea optimizada como recursión de cola, es decir, **no se acumulan llamados en la pila**.

## Explicación paso a paso

### Caso base

```Scala
if (x <= 1) acumulador
```

Cuando `x` llega a `1`, la función retorna directamente el valor acumulado, evitando más llamadas.

### Caso recursivo

```Scala
loop(x - 1, acumulador * x)
```

En cada llamada:

* Se reduce el valor de `x` en 1.
* Se multiplica el acumulador por `x` y se pasa a la siguiente iteración.
* Como es recursión de cola, la llamada recursiva es la **última instrucción** en ejecutarse, lo que permite a Scala optimizar la pila.

---

## Llamados de pila en recursión de cola

Ejemplo:

```Scala
factorial(5)
```

### Paso 1: Llamada inicial

```Scala
loop(5, 1)
```

### Paso 2: Primera iteración

```Scala
loop(4, 5)   // acumulador = 1 * 5
```

### Paso 3: Segunda iteración

```Scala
loop(3, 20)  // acumulador = 5 * 4
```

### Paso 4: Tercera iteración

```Scala
loop(2, 60)  // acumulador = 20 * 3
```

### Paso 5: Cuarta iteración

```Scala
loop(1, 120) // acumulador = 60 * 2
```

### Paso 6: Caso base

```Scala
return 120
```

---

## Diferencia con recursión normal

* En **recursión normal** cada llamada queda en la pila esperando a que termine la siguiente, lo que puede causar desbordamiento si `n` es muy grande.
* En **recursión de cola**, el compilador transforma el proceso en un **bucle optimizado**, por lo que no se guarda cada llamada en la pila y el algoritmo puede ejecutarse para valores muy grandes sin problema.

---

## Ejemplo de uso

```Scala
val resultado = factorial(5)
println(resultado)  // 120
```

El resultado de `factorial(5)` es `120`.


## Diagrama de llamados de pila con recursión de cola

```mermaid
sequenceDiagram
    participant Main as factorial(5)
    participant L1 as loop(5, 1)
    participant L2 as loop(4, 5)
    participant L3 as loop(3, 20)
    participant L4 as loop(2, 60)
    participant L5 as loop(1, 120)

    Main->>L1: llamada inicial
    L1->>L2: tail call con (4, 5)
    L2->>L3: tail call con (3, 20)
    L3->>L4: tail call con (2, 60)
    L4->>L5: tail call con (1, 120)
    L5-->>Main: return 120
```

## Punto 3: consultas seguras

Código del punto (en `RegistroSismico.scala`):

```scala
def conteoSeguro(r: Registro): Map[String, Int] =
  r.groupBy(_.region).map { case (region, sismos) => (region, sismos.length) }
    .withDefaultValue(0)

def maximaDe(r: Registro, region: String): Option[Double] =
  (for (s <- r if s.region == region) yield s.magnitud).maxOption

def regionMasActiva(r: Registro): Option[String] =
  r.groupBy(_.region).map { case (region, sismos) => (region, sismos.length) }
    .toList.sortBy { case (region, n) => (-n, region) }
    .headOption.map { case (region, _) => region }
```

Las tres funciones evitan las excepciones de dos maneras: `conteoSeguro` le da
al mapa un valor por defecto, y `maximaDe` y `regionMasActiva` devuelven un
`Option` en lugar de fallar cuando no hay respuesta. No se usa `var`, `while`
ni `return`.

### Traducción de la expresión `for` de `maximaDe`

El `for` con `if` y `yield` no es una construcción propia: el compilador la
traduce a `withFilter` y `map`.

```scala
for (s <- r if s.region == region) yield s.magnitud
```

se traduce a

```scala
r.withFilter(s => s.region == region).map(s => s.magnitud)
```

El guarda `if` pasa a `withFilter` y el `yield` pasa a `map`. Después,
`maxOption` toma la lista de magnitudes: si está vacía devuelve `None` y si no
lo está devuelve `Some` del mayor.

### Traza de `conteoSeguro(registro)`

Con el registro de siete sismos del material:

1. `r.groupBy(_.region)` agrupa los sismos por región:

   | región | sismos del grupo |
      |---|---|
   | `"Valle"` | 3 sismos (4.2, 3.8, 5.3) |
   | `"Santander"` | 2 sismos (5.1, 4.7) |
   | `"Choco"` | 2 sismos (6.0, 3.1) |

2. `.map { case (region, sismos) => (region, sismos.length) }` reemplaza cada
   grupo por su tamaño: `Map("Valle" -> 3, "Santander" -> 2, "Choco" -> 2)`.
3. `.withDefaultValue(0)` envuelve ese mapa. Las claves que están siguen
   respondiendo su conteo, y toda clave que no está responde `0` en lugar de
   lanzar `NoSuchElementException`.

Entonces `conteoSeguro(registro)("Valle")` da `3` y
`conteoSeguro(registro)("Amazonas")` da `0`. El valor por defecto solo actúa
con `apply`: `contains("Amazonas")` sigue siendo `false`, porque la clave no
se agrega al mapa.

### Traza de `maximaDe(registro, "Choco")` y `maximaDe(registro, "Amazonas")`

| llamada | magnitudes de la región (`for`) | `maxOption` |
|---|---|---|
| `maximaDe(registro, "Choco")` | `List(6.0, 3.1)` | `Some(6.0)` |
| `maximaDe(registro, "Amazonas")` | `List()` | `None` |

### Traza de `regionMasActiva(registro)`

1. Se agrupa y se cuenta como en `conteoSeguro`, y se pasa a lista de parejas.
   El orden de esa lista no está garantizado por el mapa, por ejemplo:
   `List(("Santander", 2), ("Valle", 3), ("Choco", 2))`.
2. `sortBy { case (region, n) => (-n, region) }` ordena por conteo
   descendente (por eso el signo menos) y, en empate, por nombre ascendente:
   `List(("Valle", 3), ("Choco", 2), ("Santander", 2))`.
3. `headOption` toma la primera pareja, `Some(("Valle", 3))`, y la última
   transformación se queda con el nombre: `Some("Valle")`.

Con el registro vacío no hay grupos, la lista está vacía y `headOption` da
`None`.

```mermaid
flowchart LR
    R[registro] --> G["groupBy(_.region)"]
    G --> C["map: región a conteo"]
    C --> L[toList]
    L --> S["sortBy (-n, región)"]
    S --> H[headOption]
    H --> O["Some(región) o None"]
```



---

## Punto 4: réplicas perezosas

Código del punto (en `RegistroSismico.scala`):

```scala
def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0

def replicas(magnitud: Double): LazyList[Double] =
  redondear(magnitud) #:: replicas(magnitud * 0.8)

def replicasSensibles(magnitud: Double, minimo: Double): List[Double] =
  replicas(magnitud).takeWhile(_ > minimo).toList
```

Ninguna función usa `var`, `while` ni `return`. La recursión de `replicas` no usa
una expresión `for`, sino el constructor perezoso `#::` de `LazyList`.

### ¿Cómo funciona `redondear`?

Multiplica el valor por `100.0`, redondea al entero más cercano con `Math.round`
y vuelve a dividir por `100.0`. Así conserva exactamente dos cifras decimales
(por ejemplo, `redondear(4.352) = 4.35`).

### Traza paso a paso de `replicas(5.0).take(4).toList`

El operador `#::` (`LazyList.cons`) recibe su segundo argumento **por nombre**
(*call-by-name*). Eso significa que la llamada recursiva `replicas(magnitud * 0.8)`
queda guardada como una promesa sin evaluar (*thunk*) hasta que el consumidor
pida explícitamente el siguiente elemento:

1. **Paso 0 (creación inicial):** `replicas(5.0)` construye la celda con cabeza
   `redondear(5.0) = 5.0` y cola suspendida `replicas(4.0)` (aún no se llama).
2. **`take(4)` pide el 1.er elemento:** entrega `5.0` (quedan 3 por pedir) y
   fuerza la cola `replicas(4.0)`.
3. **`take(4)` pide el 2.º elemento:** `replicas(4.0)` calcula `redondear(4.0) = 4.0`
   con cola suspendida `replicas(3.2)`. Entrega `4.0` (quedan 2 por pedir).
4. **`take(4)` pide el 3.er elemento:** `replicas(3.2)` calcula `redondear(3.2) = 3.2`
   con cola suspendida `replicas(2.56)`. Entrega `3.2` (queda 1 por pedir).
5. **`take(4)` pide el 4.º elemento:** `replicas(2.56)` calcula `redondear(2.56) = 2.56`
   con cola suspendida `replicas(2.048)`. Entrega `2.56` (quedan 0 por pedir).
6. **Detención:** Como el contador de `take(4)` llega a cero, nunca se fuerza
   `replicas(2.048)`. Finalmente, `.toList` materializa `List(5.0, 4.0, 3.2, 2.56)`.

| Paso | Magnitud sin redondear | Valor emitido (`redondear`) | Cola suspendida | ¿Se evalúa? |
|---|---|---|---|---|
| 1 | `5.0` | `5.0` | `replicas(4.0)` | Sí (pedida por `take`) |
| 2 | `4.0` | `4.0` | `replicas(3.2)` | Sí (pedida por `take`) |
| 3 | `3.2` | `3.2` | `replicas(2.56)` | Sí (pedida por `take`) |
| 4 | `2.56` | `2.56` | `replicas(2.048)` | **No** (`take(4)` termina) |

```mermaid
flowchart LR
    N0["5.0 #:: thunk"] -->|1. pedido| N1["4.0 #:: thunk"]
    N1 -->|2. pedido| N2["3.2 #:: thunk"]
    N2 -->|3. pedido| N3["2.56 #:: thunk"]
    N3 -.->|"4. take(4) para: no evalua replicas(2.048)"| N4["replicas(2.048) sin evaluar"]
```

### ¿Por qué la secuencia perezosa no agota la memoria aunque sea infinita?

1. **Evaluación bajo demanda:** Una colección estricta (`List`) necesitaría
   calcular toda la cola antes de devolver el resultado, provocando un bucle
   infinito y un desbordamiento de pila (`StackOverflowError`) o memoria
   (`OutOfMemoryError`). Con `LazyList`, `#::` solo construye un nodo cuando el
   consumidor (`take`, `takeWhile` o `head`) solicita la siguiente posición.
2. **Sin retención innecesaria de memoria:** En Scala, `LazyList` evalúa de
   forma perezosa tanto la cabeza como la cola y no retiene toda la cadena en
   memoria si el programa no conserva una referencia al inicio de la secuencia
   (por ejemplo, en `replicas(5.0).take(200).toList` o dentro de
   `replicasSensibles`), permitiendo al recolector de basura liberar los nodos
   ya recorridos. Solo quedan en memoria los elementos finalmente devueltos en la
   lista resultado.

### Traza paso a paso de `replicasSensibles`

`replicasSensibles(magnitud, minimo)` encadena `takeWhile(_ > minimo)` sobre el
flujo infinito `replicas(magnitud)` y luego convierte el prefijo aceptado a
`List` con `.toList`.

- **Caso `replicasSensibles(5.0, 3.0)`:**
  1. Pide el elemento 1: `5.0 > 3.0` (verdadero) -> se incluye, pide el siguiente.
  2. Pide el elemento 2: `4.0 > 3.0` (verdadero) -> se incluye, pide el siguiente.
  3. Pide el elemento 3: `3.2 > 3.0` (verdadero) -> se incluye, pide el siguiente.
  4. Pide el elemento 4: `2.56 > 3.0` (falso) -> `takeWhile` corta la secuencia inmediatamente sin calcular `2.05` ni los siguientes.
  5. Resultado: `List(5.0, 4.0, 3.2)`.

- **Caso `replicasSensibles(2.0, 3.0)` (y umbral exacto `replicasSensibles(3.0, 3.0)`):**
  1. Pide solo el primer elemento (`2.0` o `3.0`).
  2. Como la condición es estricta (`> minimo`), falla de inmediato y `takeWhile`
     devuelve vacío sin forzar ninguna llamada recursiva a la cola.
  3. Resultado: `List()`.

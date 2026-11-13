# Informe de Corrección - Taller 3

Fundamentos de Programación Funcional y Concurrente  
Universidad del Valle

## Punto 1: Consultas con Expresiones For

### 1.1 `fuertes( )`

La función devuelve las regiones de los sismos con magnitud que supera el umbral, conservando el orden y los repetidos.

**Especificación:**

$$
fuertes(r,u) = [s.region | s ∈ r, s.magnitud > u]
$$

**Demostración.**

**Caso base:** `r = Nil`.

El `for` no tiene elementos que recorrer y produce `Nil`, que coincide con una lista de registros vacía.

**Caso de inducción:** `r = s :: t`, si `fuertes(t,u)` contiene exactamente las regiones de los sismos de `t` cuya magnitud supera `u`.

- Si `s.magnitud > u`, el guarda se cumple y:

  ```math
  fuertes(s :: t,u) = s.region :: fuertes(t,u)
  ```

  Se agrega la región de `s`, que cumple la condición, seguida de las regiones seleccionadas de `t`.

- Si `s.magnitud <= u`, el guarda falla y:

  ```math
  fuertes(s :: t,u) = fuertes(t,u)
  ```

  No se agrega ninguna región y se conservan las seleccionadas de `t`.

En ambos casos, el resultado contiene exactamente las regiones que cumplen la condición, en el orden original.

**Independencia del orden.**

No depende del orden. Si `r'`es una permutación de `r`, ambas listas contienen las mismas regiones con las mismas multiplicidades, aunque posiblemente en posiciones diferentes.

### 1.2 `enRegion( )`

La función devuelve las magnitudes de los sismos de la región indicada.

**Especificación:**

```math
enRegion(r,z) = [s.magnitud | s ∈ r, z aparece en s.region]
```

**Demostración.**

**Caso base:** `r = Nil`.

Al igual que en `fuertes( )`, el `for` no tiene elementos que recorrer y produce `Nil`. Ante una lista de registros vacía, el resultado también es una lista vacía.

**Caso de inducción:** `r = s :: t`, si `enRegion(t,z)` contiene las magnitudes de los sismos de `t` cuya región contiene `z`.

- Si `s.region.indexOf(z) >= 0`, el guarda se cumple y:

  ```math
  enRegion(s :: t,z) = s.magnitud :: enRegion(t,z)
  ```

  Se agrega la magnitud de `s`, pues su región contiene `z`.

- Si `s.region.indexOf(z) < 0`, el guarda falla y:

  ```math
  enRegion(s :: t,z) = enRegion(t,z)
  ```

  No se agrega la magnitud de `s` y se conservan las seleccionadas de `t`.

En ambos casos, el resultado contiene exactamente las magnitudes que cumplen la condición, en el orden original.

**Independencia del orden.**

Si `r'` es una permutación de `r`, ambas listas contienen las mismas magnitudes con las mismas multiplicidades, aunque posiblemente en posiciones diferentes.

### 1.3 `paresCercanos( )`

La función devuelve parejas de regiones distintas que tienen sismos con una diferencia entre sus magnitudes de máximo $d$.

**Especificación:**

```math
paresCercanos(r,d) = {(x,y) | x != y, existen a,b ∈ r tales que:
a.region = x, b.region = y y |a.magnitud - b.magnitud| <= d}
```

**Demostración.**

**Caso base:** `r = Nil`.

No hay sismos ni regiones que comparar. Por tanto, el resultado es un conjunto vacío.

**Caso de inducción:** `r = s :: t`, suponiendo que el resultado para `t` contiene todas las parejas que cumplen la condición.

- Si `s.region` ya aparece en `t`, `distinct` evita repetir la región. Pero el nuevo sismo puede hacer que aparezcan nuevas parejas al cumplir la condición de cercanía.
- Si `s.region` no aparece en `t`, se agrega una nueva región y se compara con las demás para encontrar parejas que cumplan la condición.

En ambos casos se consideran todas las parejas válidas y `distinct` evita repeticiones.

**Independencia del orden.**

Sea `r'` una permutación de `r`.

Las mismas regiones y combinaciones de sismos se consideran independientemente del orden de entrada. Además, el valor absoluto hace que la comparación de magnitudes no dependa de cuál sismo se considere primero.
## Punto 2 : Agrupar por mapas

1. Objetivo
   El Punto 2 consiste en construir tres consultas sobre un registro
   sísmico:
- porRegion: agrupa los sismos según su región.
- conteoPorRegion: calcula cuántos sismos hay en cada región.
- magnitudMaxima: obtiene la mayor magnitud registrada en cada
  región.
  El modelo de datos utilizado es el proporcionado por el taller:
  case class Fecha(anio: Int, mes: Int)

case class Sismo(
fecha: Fecha,
region: String,
magnitud: Double,
profundidad: Int
)

type Registro = List[Sismo]
2. Implementación
   def porRegion(r: Registro): Map[String, List[Sismo]] = {
   r.groupBy(_.region)
   }

def conteoPorRegion(r: Registro): Map[String, Int] = {
porRegion(r).map {
case (region, sismos) => region -> sismos.size
}
}

def magnitudMaxima(r: Registro): Map[String, Double] = {
porRegion(r).map {
case (region, sismos) =>
region -> sismos.map(_.magnitud).max
}
}
Las tres funciones utilizan operaciones de colecciones inmutables. No se
emplean variables mutables, ciclos iterativos ni instrucciones return.
3. Argumento de corrección
   Sea (R) el registro de entrada y sea (G) el conjunto de regiones que
   aparecen en él. Para cada región (g \in {=tex}G), definimos:

\(R_g = {s `\in `{=tex}R `\mid `{=tex}s.region = g}\)

Es decir, (R_g) contiene todos los sismos del registro cuya región es
(g).
3.1. Corrección de porRegion
La operación:
r.groupBy(_.region)
construye un mapa cuya llave es la región de cada sismo y cuyo valor es
la colección de sismos que tienen esa región.

Por tanto, para cada región (g) que aparece en el registro, el mapa
cumple:

\(porRegion(R)(g) = R_g\)

Además, no se crean llaves para regiones que no aparecen en el registro.
Si el registro está vacío, no existe ninguna región que agrupar y el
resultado es:

\(porRegion(\[\)) = Map() ]

Así, cada sismo queda asociado a la llave correspondiente a su región.
3.2. Corrección de conteoPorRegion
La función parte del mapa producido por porRegion y transforma cada
par región-lista en un par región-cantidad:
porRegion(r).map {
case (region, sismos) => region -> sismos.size
}
Como sismos contiene exactamente los eventos de una región, su tamaño
corresponde al número de sismos de esa región. Formalmente:

\(conteoPorRegion(R)(g) = \|R_g\|\)

Por consiguiente, el valor asociado a cada región es su cantidad exacta
de registros.

Si (R) está vacío, porRegion(R) produce un mapa vacío. La operación
map sobre ese mapa también produce un mapa vacío:

\(conteoPorRegion(\[\)) = Map() ]
3.3. Corrección de magnitudMaxima
La función transforma cada lista de sismos en la magnitud más alta de
esa lista:
porRegion(r).map {
case (region, sismos) =>
region -> sismos.map(_.magnitud).max
}
Primero, sismos.map(_.magnitud) obtiene las magnitudes de los sismos
de una región. Después, max selecciona el mayor valor. Por tanto:
undefined

## Punto 3: consultas seguras

Sea $r$ un registro (una lista de sismos). Para una región $g$ se define el
conjunto de sus sismos $r_g = \{\, s \in r \mid s.\text{region} = g \,\}$.

### `conteoSeguro`

**Especificación.** Para toda región $g$ (esté o no en $r$):

```math
\text{conteoSeguro}(r)(g) = |r_g|
```

**Argumento.** Hay dos casos.

- Si $g$ aparece en $r$, `groupBy` crea la clave $g$ con la lista de todos los
  sismos de región $g$, es decir $r_g$. El `map` reemplaza esa lista por su
  longitud, así que la clave $g$ responde $|r_g|$.
- Si $g$ no aparece en $r$, la clave no existe en el mapa. `withDefaultValue(0)`
  hace que `apply` responda $0$, y $|r_g| = 0$ porque $r_g = \emptyset$.

Ambos casos cumplen la especificación, y ninguno lanza excepción. El caso base
del registro vacío queda cubierto por el segundo caso para toda región.

**Independencia del orden.** El grupo $r_g$ y su tamaño solo dependen de qué
sismos hay en $r$ y no de la posición en que aparecen. Si $r'$ es una
permutación de $r$, entonces $|r'_g| = |r_g|$ para toda $g$, y el mapa
resultante es el mismo.

### `maximaDe`

**Especificación.**

```math
\text{maximaDe}(r, g) =
\begin{cases}
\text{None} & \text{si } r_g = \emptyset \\
\text{Some}\big(\max \{\, s.\text{magnitud} \mid s \in r_g \,\}\big) & \text{si } r_g \neq \emptyset
\end{cases}
```

**Corrección del `for` por inducción sobre la lista.** Sea
$M(r, g)$ la lista que produce `for (s <- r if s.region == g) yield s.magnitud`.
Se demuestra que $M(r, g)$ contiene exactamente las magnitudes de los sismos de
$r_g$, en el orden en que aparecen.

**Caso base:** $r = \text{Nil}$. El `for` no tiene elementos que recorrer y da
$\text{Nil}$, que es la lista de magnitudes de $r_g = \emptyset$.

**Caso de inducción:** $r = s :: t$, con la hipótesis de inducción sobre $t$:
$M(t, g)$ es la lista de magnitudes de $t_g$.

- Si $s.\text{region} = g$, el guarda se cumple y
  $M(s :: t, g) = s.\text{magnitud} :: M(t, g)$, que son las magnitudes de
  $(s :: t)_g$.
- Si $s.\text{region} \neq g$, el guarda falla y $M(s :: t, g) = M(t, g)$, que
  son las magnitudes de $(s :: t)_g = t_g$.

Por inducción estructural, la propiedad vale para todo registro.

**Cierre.** `maxOption` sobre una lista vacía devuelve `None` y sobre una lista
no vacía devuelve `Some` del mayor elemento. Como $M(r, g)$ es vacía
exactamente cuando $r_g = \emptyset$, el resultado coincide con la
especificación en ambos casos.

### `regionMasActiva`

**Especificación.** Si $r = \emptyset$ el resultado es `None`. Si no, es
`Some(g)` donde $g$ es la región con $|r_g|$ máximo y, entre las que empatan,
la menor en orden alfabético.

**Argumento.** Sea $G$ el conjunto de regiones de $r$, que son las claves de
`groupBy`; cada región aparece una sola vez en la lista de parejas
$(g, |r_g|)$. La función ordena con la clave $k(g) = (-|r_g|,\ g)$, comparada
lexicográficamente.

- Si $r = \emptyset$, $G = \emptyset$, la lista de parejas es vacía y
  `headOption` da `None`.
- Si $r \neq \emptyset$, $G$ es no vacío. Como las regiones son distintas, las
  claves $k(g)$ son todas distintas y el orden lexicográfico es total
  estricto sobre ellas. `sortBy` deja primero la pareja de clave mínima.
- Esa clave mínima $k(g^\ast)$ cumple, por la primera componente, que
  $-|r_{g^\ast}| \leq -|r_g|$ para toda $g$, es decir $|r_{g^\ast}| \geq |r_g|$:
  $g^\ast$ tiene el conteo máximo. Si otra región $g$ tiene el mismo conteo,
  la primera componente empata y decide la segunda: $g^\ast \leq g$
  alfabéticamente.

Entonces $g^\ast$ es la región más activa con la regla de desempate pedida.
Como el resultado no depende del orden de las parejas que entrega el mapa,
sino solo de la clave $k$, tampoco depende del orden de $r$.

---

## Punto 4: réplicas perezosas

Como `replicas(m)` genera una secuencia potencialmente infinita, no tiene sentido
enunciar la corrección sobre una terminación total de la estructura completa.
Cada elemento se calcula únicamente cuando se pide; por lo tanto, la propiedad
se enuncia y se demuestra sobre cualquier prefijo finito de los primeros $n$
elementos solicitados.

### `replicas`

**Especificación.** Sea $R(m, i)$ el término en la posición $i \in \mathbb{N}$
(contando desde $i = 0$) de la secuencia de réplicas que inicia en la magnitud
$m$:

```math
R(m, i) = \text{redondear}(m \cdot 0.8^i)
```

donde $\text{redondear}(x) = \text{Math.round}(x \cdot 100.0) / 100.0$.

**Teorema (corrección sobre los primeros $n$ elementos).** Para toda magnitud
$m \in \mathbb{R}$ y todo $n \in \mathbb{N}$:

```math
\text{replicas}(m).\text{take}(n).\text{toList} == [R(m, 0), R(m, 1), \ldots, R(m, n - 1)]
```

**Demostración por inducción sobre $n$:**

- **Caso base ($n = 0$):**
  `take(0)` devuelve la secuencia vacía $\text{Nil}$ sin forzar ni la cabeza ni
  la cola de `replicas(m)`, lo cual coincide con la lista vacía de longitud $0$
  ($[]$).

- **Hipótesis de inducción (HI):**
  Suponemos que para todo $m' \in \mathbb{R}$, pedir los primeros $k$ elementos
  cumple:

```math
\text{replicas}(m').\text{take}(k).\text{toList} == [R(m', 0), R(m', 1), \ldots, R(m', k - 1)]
```

- **Paso inductivo ($n = k + 1$, con $k \geq 0$):**
  Por definición de `replicas(m)` con el constructor perezoso `#::`, el primer
  elemento es $\text{redondear}(m) = R(m, 0)$ y la cola suspendida es
  `replicas(m * 0.8)`. Al extraer $k + 1$ elementos:

```math
\begin{aligned}
\text{replicas}(m).\text{take}(k + 1).\text{toList}
&\to \text{redondear}(m) :: \text{replicas}(m \cdot 0.8).\text{take}(k).\text{toList} \\
&\overset{\text{HI}}{=} R(m, 0) :: [R(m \cdot 0.8, 0), R(m \cdot 0.8, 1), \ldots, R(m \cdot 0.8, k - 1)]
\end{aligned}
```

  Como $R(m \cdot 0.8, i) = \text{redondear}((m \cdot 0.8) \cdot 0.8^i) = \text{redondear}(m \cdot 0.8^{i+1}) = R(m, i + 1)$:

```math
= [R(m, 0), R(m, 1), \ldots, R(m, k)]
```

- **Terminación sobre prefijos:** Cada paso de `take(n)` consume exactamente un
  elemento de la secuencia y reduce el contador $n$ en $1$ hasta llegar a $0$.
  Como la cola de `#::` es por nombre, nunca se evalúan más de $n$ pasos y la
  función termina en exactamente $n$ iteraciones.

### `replicasSensibles`

**Especificación.** Dado un umbral mínimo $\text{min}$, la función debe retornar
el prefijo más largo de réplicas cuyo valor redondeado supere estrictamente a
$\text{min}$:

```math
\text{replicasSensibles}(m, \text{min}) = [R(m, 0), R(m, 1), \ldots, R(m, k - 1)]
```

donde $k \geq 0$ es el menor índice tal que $R(m, k) \leq \text{min}$.

**Argumento.**
1. `takeWhile(_ > minimo)` recorre `replicas(m)` en orden desde $i = 0$
   verificando la desigualdad estricta $R(m, i) > \text{min}$ sobre cada
   elemento emitido.
2. Si $R(m, 0) \leq \text{min}$ (es decir, incluso el sismo inicial no supera
   el umbral), el predicado falla en el primer paso ($k = 0$) y retorna `Nil`.
3. En otro caso, toma consecutivamente los elementos $R(m, 0), \ldots, R(m, k-1)$
   y se detiene en el primer índice $k$ donde $R(m, k) \leq \text{min}$, sin
   evaluar ningún término posterior ($i > k$).

**Terminación.** Para toda magnitud $m$ y todo umbral físico $\text{min} \geq 0$:
como $0 < 0.8 < 1$, la sucesión geométrica $m \cdot 0.8^i$ converge
estrictamente a $0$ ($\lim_{i \to \infty} m \cdot 0.8^i = 0$). Al aplicarse el
redondeo únicamente al emitir cada elemento ($\text{redondear}(m \cdot 0.8^i)$)
y no sobre el argumento recursivo, existe un índice finito $i_0$ tal que
$m \cdot 0.8^{i_0} < 0.005$, punto en el cual $R(m, i_0) = 0.0 \leq \text{min}$.
Esto garantiza que `takeWhile` siempre encuentra una condición de parada en un
número finito de pasos, incluso para umbrales muy pequeños como $0.01$.

## Punto 5: Generación de Informes
### `informe`
La función devuelve un mapa donde cada región tiene un resumen con su cantidad de sismos, magnitud máxima y cantidad de réplicas que superan el mínimo establecido.

Sea `r` un registro de sismos y sea `G` el conjunto de regiones presentes en él.

Para cada región `g` se define:

$$
r_g = \{s \in r \mid s.region = g\}
$$

También se define la magnitud máxima de cada región:

$$
M_g = \max\{s.magnitud \mid s \in r_g\}
$$

**Especificación**

Para toda región `g` perteneciente a `G`:

$$
informe(r,min)(g) =
Resumen(
|r_g|,
M_g,
|replicasSensibles(M_g,min)|
)
$$

Es decir, el resumen debe contener el número de sismos de la región, su magnitud máxima y la cantidad de réplicas que superan el mínimo.

**Demostración**

**Caso base:** `r = Nil`.

Si el registro está vacío, no existen regiones ni sismos que procesar.

Por lo tanto:

- `conteoSeguro(r)` produce un mapa vacío con valor por defecto `0`.
- `magnitudMaxima(r)` produce un mapa vacío, porque no hay magnitudes para calcular máximos.
- `magMax.map(...)` no tiene elementos que recorrer y devuelve otro mapa vacío.

Entonces:

$$
informe(Nil,min) = Map()
$$

Esto coincide con la especificación, porque no existen regiones para las cuales construir un resumen.

**Caso de inducción:** `r = s :: t`.

Suponemos que las operaciones utilizadas calculan correctamente los conteos, máximos y cantidades de réplicas para los sismos de cada región.

Sea `g` una región presente en `r`.

Al ejecutar:

```scala
val conteo = conteoSeguro(r)
val magMax = magnitudMaxima(r)
```

Se obtienen dos resultados:

$$
conteo(g) = |r_g|
$$

$$
magMax(g) = M_g
$$

Ahora se evalúa:

```scala
magMax.map{
  case(region, magMax) =>
    region -> Resumen(
      conteo(region),
      magMax,
      replicasSensibles(magMax,minimo).size
    )
}
```
Para la región `g`, el resultado es:

$$
Resumen(
|r_g|,
M_g,
|replicasSensibles(M_g,min)|
)
$$

Cada componente coincide con la especificación:

- conteo(region) devuelve la cantidad de sismos de la región.

- magMax contiene la magnitud máxima de esa región.

- replicasSensibles(magMax,minimo).size cuenta las réplicas cuya magnitud supera el mínimo.

El resumen construido para `g` es correcto.

Como map realiza esta operación para cada pareja del mapa de magnitudes máximas, se construye un resumen correcto para todas las regiones presentes en el registro.

Concluimos que:

$$
\forall g \in G:
$$

$$
informe(r,min)(g) =
Resumen(
|r_g|,
M_g,
|replicasSensibles(M_g,min)|
)
$$

**Independencia del orden**
Sea `r'` una permutación de `r`.

$$
informe(r,min) = informe(r',min)
$$

Para esto se consideran las tres operaciones utilizadas:

* Conteo: las permutaciones no modifican la cantidad de sismos de cada región. Por lo tanto:

$$
|r_g| = |r'_g|
$$

* Magnitud máxima: el máximo de un conjunto de magnitudes no cambia por modificar su orden. Entonces:

$$
M_g = M'_g
$$

* Réplicas: como se utiliza la misma magnitud máxima y el mismo mínimo, se obtiene la misma lista de réplicas sensibles y, por lo tanto, la misma cantidad:

$$
|replicasSensibles(M_g,min)| =
|replicasSensibles(M'_g,min)|
$$

Como los tres valores del resumen son iguales para cada región, los mapas resultantes también son iguales.

Por lo tanto:

$$
informe(r,min) = informe(r',min)
$$
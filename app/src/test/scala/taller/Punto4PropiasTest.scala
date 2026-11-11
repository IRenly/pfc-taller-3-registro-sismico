package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Punto4PropiasTest extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  test("replicas: primeras cinco replicas de un sismo de 8.5 con redondeo") {
    assert(replicas(8.5).take(5).toList == List(8.5, 6.8, 5.44, 4.35, 3.48))
  }

  test("replicas: 500 elementos mantienen orden decreciente sin agotar memoria") {
    val quinientas = replicas(7.0).take(500).toList
    assert(quinientas.length == 500)
    assert(quinientas.head == 7.0)
    assert(quinientas.last == 0.0)
    assert(quinientas.zip(quinientas.tail).forall { case (a, b) => a >= b })
  }

  test("replicas: rebanado perezoso con drop y redondeo a dos decimales") {
    val muestra = replicas(6.75)
    assert(muestra.drop(3).take(4).toList == List(3.46, 2.76, 2.21, 1.77))
    assert(muestra.take(15).toList.forall(v => v == redondear(v)))
  }

  test("replicasSensibles: el umbral estricto detiene antes de una replica igual al minimo") {
    assert(replicasSensibles(5.0, 3.2) == List(5.0, 4.0))
    val bajas = replicasSensibles(1.0, 0.01)
    assert(bajas.nonEmpty && bajas.forall(_ > 0.01))
  }

  test("replicasSensibles: coincide con el prefijo esperado para un sismo de 7.5") {
    val esperadas = List(7.5, 6.0, 4.8, 3.84)
    assert(replicasSensibles(7.5, 3.5) == esperadas)
    assert(replicasSensibles(7.5, 3.5) == replicas(7.5).take(4).toList)
  }
}

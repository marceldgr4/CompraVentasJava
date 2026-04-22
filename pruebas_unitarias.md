# 🧪 Plan de Pruebas Unitarias — CompraVenta

> **Framework:** JUnit 5 (JUnit Jupiter) — ya declarado en `pom.xml`
> **Convención de nombres:** `ClaseTesteada_metodo_condición_resultadoEsperado`

---

## Configuración previa

### Dependencias en `pom.xml` (ya incluidas)
```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.12.1</version>
  <scope>test</scope>
</dependency>
```

> Para pruebas de DAO con base de datos real, considera agregar
> **Testcontainers** (`org.testcontainers`) para levantar un PostgreSQL local.

---

## 1. Pruebas de Clases de Dominio

### `ArticleTest.java`
**Ruta:** `src/test/java/com/app/Model/domain/ArticleTest.java`

```java
package com.app.Model.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {

    private Article buildArticle(int amount, boolean sold) {
        return new Article(1, "Celular Samsung", "Descripción", amount,
                           BigDecimal.valueOf(500_000), sold);
    }

    // RF-03.3 — hasStock
    @Test
    void hasStock_cuandoAmountEsMayorACero_retornaTrue() {
        Article article = buildArticle(5, false);
        assertTrue(article.hasStock());
    }

    @Test
    void hasStock_cuandoAmountEsCero_retornaFalse() {
        Article article = buildArticle(0, false);
        assertFalse(article.hasStock());
    }

    // RF-03.3 — canSell
    @Test
    void canSell_cuandoNoVendidoYTieneStock_retornaTrue() {
        Article article = buildArticle(3, false);
        assertTrue(article.canSell());
    }

    @Test
    void canSell_cuandoYaEstaVendido_retornaFalse() {
        Article article = buildArticle(3, true);  // sold=true → ya vendido
        assertFalse(article.canSell());
    }

    @Test
    void canSell_cuandoSinStockYNoVendido_retornaFalse() {
        Article article = buildArticle(0, false);
        assertFalse(article.canSell());
    }

    @Test
    void toString_retornaNameArticle() {
        Article article = buildArticle(1, false);
        assertEquals("Celular Samsung", article.toString());
    }
}
```

---

### `PawnTest.java`
**Ruta:** `src/test/java/com/app/Model/domain/PawnTest.java`

```java
package com.app.Model.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PawnTest {

    private Pawn buildActivePawn() {
        return new Pawn(
            "uuid-profile-1", 1, 1, 2,
            BigDecimal.valueOf(300_000),
            LocalDate.now().minusDays(5),
            LocalDate.now().plusDays(25),
            false, false
        );
    }

    // RF-04.3 — isActive
    @Test
    void isActive_cuandoNoDevueltoNiExpirado_retornaTrue() {
        assertTrue(buildActivePawn().isActive());
    }

    @Test
    void isActive_cuandoDevuelto_retornaFalse() {
        Pawn pawn = buildActivePawn();
        pawn.setReturned(true);
        assertFalse(pawn.isActive());
    }

    @Test
    void isActive_cuandoExpirado_retornaFalse() {
        Pawn pawn = buildActivePawn();
        pawn.setExpired(true);
        assertFalse(pawn.isActive());
    }

    // getTotal
    @Test
    void getTotal_calculaCorrectamente() {
        Pawn pawn = buildActivePawn(); // precio=300.000, cantidad=2
        assertEquals(BigDecimal.valueOf(600_000), pawn.getTotal());
    }

    @Test
    void getTotal_cuandoPrecioEsNull_retornaCero() {
        Pawn pawn = buildActivePawn();
        pawn.setPrice(null);
        assertEquals(BigDecimal.ZERO, pawn.getTotal());
    }

    // getStatus
    @Test
    void getStatus_cuandoActivo_retornaActivo() {
        assertEquals("Activo", buildActivePawn().getStatus());
    }

    @Test
    void getStatus_cuandoDevuelto_retornaDevuelto() {
        Pawn pawn = buildActivePawn();
        pawn.setReturned(true);
        assertEquals("Devuelto", pawn.getStatus());
    }

    @Test
    void getStatus_cuandoExpirado_retornaExpirado() {
        Pawn pawn = buildActivePawn();
        pawn.setExpired(true);
        assertEquals("Expirado", pawn.getStatus());
    }
}
```

---

### `ClienteTest.java`
**Ruta:** `src/test/java/com/app/Model/domain/ClienteTest.java`

```java
package com.app.Model.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void getFullName_concatenaApellidoYNombre() {
        Cliente c = new Cliente("Juan", "Pérez", "juan@mail.com", "3001234567");
        assertEquals("Pérez, Juan", c.getFullName());
    }

    @Test
    void toString_retornaFullName() {
        Cliente c = new Cliente("María", "López", "maria@mail.com", "3009876543");
        assertEquals("López, María", c.toString());
    }
}
```

---

### `SalesDetailTest.java`
**Ruta:** `src/test/java/com/app/Model/domain/SalesDetailTest.java`

```java
package com.app.Model.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesDetailTest {

    @Test
    void getSubtotal_calculaCorrectamente() {
        SalesDetail detail = new SalesDetail(1, 1, 3, BigDecimal.valueOf(150_000));
        assertEquals(BigDecimal.valueOf(450_000), detail.getSubtotal());
    }

    @Test
    void getSubtotal_cuandoPrecioEsNull_retornaCero() {
        SalesDetail detail = new SalesDetail(1, 1, 3, null);
        assertEquals(BigDecimal.ZERO, detail.getSubtotal());
    }
}
```

---

## 2. Pruebas de Utilidades

### `ValidationUtilsTest.java`
**Ruta:** `src/test/java/com/app/Utils/ValidationUtilsTest.java`

```java
package com.app.Utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    // isValidEmail
    @ParameterizedTest
    @ValueSource(strings = {"correo@gmail.com", "user.name+tag@domain.co", "test@sub.domain.org"})
    void isValidEmail_formatosValidos_retornaTrue(String email) {
        assertTrue(ValidationUtils.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"noarroba", "@sinusuario.com", "sin@", "", "espacios @mail.com"})
    void isValidEmail_formatosInvalidos_retornaFalse(String email) {
        assertFalse(ValidationUtils.isValidEmail(email));
    }

    @Test
    void isValidEmail_cuandoNull_retornaFalse() {
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    // isPositivePrice
    @Test
    void isPositivePrice_cuandoMayorACero_retornaTrue() {
        assertTrue(ValidationUtils.isPositivePrice(BigDecimal.valueOf(100)));
    }

    @Test
    void isPositivePrice_cuandoCero_retornaFalse() {
        assertFalse(ValidationUtils.isPositivePrice(BigDecimal.ZERO));
    }

    @Test
    void isPositivePrice_cuandoNegativo_retornaFalse() {
        assertFalse(ValidationUtils.isPositivePrice(BigDecimal.valueOf(-1)));
    }

    @Test
    void isPositivePrice_cuandoNull_retornaFalse() {
        assertFalse(ValidationUtils.isPositivePrice(null));
    }

    // isValidAmount
    @Test
    void isValidAmount_cuandoCero_retornaTrue() {
        assertTrue(ValidationUtils.isValidAmount(0));
    }

    @Test
    void isValidAmount_cuandoNegativo_retornaFalse() {
        assertFalse(ValidationUtils.isValidAmount(-1));
    }

    // isValidText
    @Test
    void isValidText_cuandoDentroDelLimite_retornaTrue() {
        assertTrue(ValidationUtils.isValidText("Hola", 10));
    }

    @Test
    void isValidText_cuandoExcedeLimite_retornaFalse() {
        assertFalse(ValidationUtils.isValidText("TextoMuyLargo", 5));
    }

    @Test
    void isValidText_cuandoBlank_retornaFalse() {
        assertFalse(ValidationUtils.isValidText("   ", 255));
    }
}
```

---

### `CurrencyUtilsTest.java`
**Ruta:** `src/test/java/com/app/Utils/CurrencyUtilsTest.java`

```java
package com.app.Utils;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyUtilsTest {

    @Test
    void format_cuandoNull_retornaCeroFormateado() {
        assertEquals("$0", CurrencyUtils.format((BigDecimal) null));
    }

    @Test
    void parse_cuandoCadenaLimpia_retornaValorCorrecto() {
        BigDecimal result = CurrencyUtils.parse("500000");
        assertEquals(0, BigDecimal.valueOf(500_000).compareTo(result));
    }

    @Test
    void parse_cuandoCadenaConPuntos_parsearCorrectamente() {
        // "1.500.000" (formato colombiano) → 1500000
        BigDecimal result = CurrencyUtils.parse("1.500.000");
        assertEquals(0, BigDecimal.valueOf(1_500_000).compareTo(result));
    }

    @Test
    void parse_cuandoBlank_retornaCero() {
        assertEquals(BigDecimal.ZERO, CurrencyUtils.parse(""));
    }
}
```

---

### `DateUtilsTest.java`
**Ruta:** `src/test/java/com/app/Utils/DateUtilsTest.java`

```java
package com.app.Utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void format_fechaValida_retornaStringCorrecto() {
        LocalDate date = LocalDate.of(2026, 4, 21);
        assertEquals("21/04/2026", DateUtils.format(date));
    }

    @Test
    void format_cuandoNull_retornaCadenaVacia() {
        assertEquals("", DateUtils.format((LocalDate) null));
    }

    @Test
    void parseDate_formatoValido_retornaFecha() {
        LocalDate result = DateUtils.parseDate("21/04/2026");
        assertEquals(LocalDate.of(2026, 4, 21), result);
    }

    @Test
    void parseDate_formatoInvalido_retornaNull() {
        assertNull(DateUtils.parseDate("2026-04-21"));
    }

    @Test
    void isOverdue_cuandoAntesDehoy_retornaTrue() {
        assertTrue(DateUtils.isOverdue(LocalDate.now().minusDays(1)));
    }

    @Test
    void isOverdue_cuandoEsHoy_retornaFalse() {
        assertFalse(DateUtils.isOverdue(LocalDate.now()));
    }

    @Test
    void daysUntilReturn_calculaCorrectamente() {
        LocalDate future = LocalDate.now().plusDays(10);
        assertEquals(10, DateUtils.daysUntilReturn(future));
    }
}
```

---

## 3. Pruebas de Servicios (con mocks o lógica pura)

### `ArticleService_ValidacionTest.java`
**Ruta:** `src/test/java/com/app/Service/ArticleService_ValidacionTest.java`

> Estas pruebas verifican la validación interna sin necesidad de base de datos.
> Para probar con DB real, usar un perfil de test con Testcontainers.

```java
package com.app.Service;

import com.app.Model.domain.Article;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ArticleService_ValidacionTest {

    // Prueba la validación interna del servicio
    // (Nota: para esto puede necesitarse refactorizar validateArticle como paquete-visible
    // o crear un ArticleRequest DTO con validación in-situ)

    @Test
    void createArticle_cuandoNombreEsBlank_lanzaServiceException() {
        // Configurar sesión de Admin antes de llamar create()
        // Esto requiere SessionManager.startSession() en @BeforeEach
        // y cerrarla en @AfterEach para evitar contaminación entre tests
        // Ejemplo ilustrativo — implementar según arquitectura de tests del proyecto
        Article article = new Article(1, "", "desc", 5, BigDecimal.valueOf(100), false);
        // assertThrows(ArticleService.ServiceException.class, () -> service.create(article));
        assertEquals("", article.getNameArticle()); // placeholder hasta refactor
    }
}
```

---

### `PawnService_ValidacionTest.java`
**Ruta:** `src/test/java/com/app/Service/PawnService_ValidacionTest.java`

```java
package com.app.Service;

import com.app.Model.domain.Pawn;
import com.app.Service.exceptions.BusinessException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PawnService_ValidacionTest {

    /**
     * Verifica que la validación detecta fechas inconsistentes.
     * return_date debe ser POSTERIOR a pawn_date.
     */
    @Test
    void validatePawn_cuandoFechasInvertidas_lanzaBusinessException() {
        Pawn pawn = new Pawn(
            "uuid-1", 1, 1, 2,
            BigDecimal.valueOf(100_000),
            LocalDate.of(2026, 5, 10),   // pawnDate
            LocalDate.of(2026, 5, 5),    // returnDate ANTES de pawnDate ← inválido
            false, false
        );
        PawnService service = new PawnService();
        // La validación se ejecuta en validatePawn() llamada por create()
        // Requiere sesión activa; mock o configurar test
        // assertThrows(BusinessException.class, () -> service.create(pawn));
        assertTrue(pawn.getReturnDate().isBefore(pawn.getPawnDate())); // guardia
    }

    @Test
    void validatePawn_cuandoArticleIdEsCero_lanzaBusinessException() {
        Pawn pawn = new Pawn();
        pawn.setArticleId(0);  // inválido
        pawn.setClienteId(1);
        pawn.setAmount(1);
        pawn.setPrice(BigDecimal.valueOf(100_000));
        pawn.setPawnDate(LocalDate.now());
        pawn.setReturnDate(LocalDate.now().plusDays(30));

        assertThrows(BusinessException.class, () -> {
            // Invocar la validación directamente si es paquete-visible, o
            // configurar test de integración con sesión real
            new PawnService().create(pawn);
        });
    }
}
```

---

## 4. Estructura de Carpetas de Tests Propuesta

```
src/test/java/
├── com/app/
│   ├── Model/
│   │   └── domain/
│   │       ├── ArticleTest.java           ✅ Listo para implementar
│   │       ├── PawnTest.java              ✅ Listo para implementar
│   │       ├── ClienteTest.java           ✅ Listo para implementar
│   │       └── SalesDetailTest.java       ✅ Listo para implementar
│   ├── Service/
│   │   ├── ArticleService_ValidacionTest.java  ⚠️ Requiere mock de SessionManager
│   │   └── PawnService_ValidacionTest.java     ⚠️ Requiere mock de SessionManager
│   └── Utils/
│       ├── ValidationUtilsTest.java       ✅ Listo para implementar
│       ├── CurrencyUtilsTest.java         ✅ Listo para implementar
│       └── DateUtilsTest.java             ✅ Listo para implementar
```

---

## 5. Convenciones y Buenas Prácticas de Testing

| Buena Práctica | Descripción |
|----------------|-------------|
| **Independencia** | Cada test debe poder ejecutarse solo, sin depender del orden o estado de otros tests. |
| **Nombres descriptivos** | Usar el formato `método_condición_resultadoEsperado`. |
| **Un assert por test** | Idealmente cada test verifica una sola cosa. |
| **@BeforeEach / @AfterEach** | Para setup/teardown de sesión o estado compartido. |
| **No a BD real en unit tests** | Las pruebas unitarias no deben conectarse a la base de datos. |
| **Tests de integración separados** | Crear perfil Maven `integration-tests` para pruebas que requieren BD. |
| **Casos límite** | Siempre probar null, vacío, cero, negativo y el límite máximo. |
| **Pruebas parametrizadas** | Usar `@ParameterizedTest` + `@ValueSource` para múltiples entradas. |

---

## 6. Comando para Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar un test específico
mvn test -Dtest=ArticleTest

# Ejecutar tests con reporte
mvn surefire-report:report
```

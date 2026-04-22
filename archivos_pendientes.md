# 📋 Archivos Pendientes de Implementación — CompraVenta

> Revisión realizada el **21 de abril de 2026**. Los archivos listados aquí tienen el cuerpo de la clase vacío o incompleto.

---

## 🔴 Prioridad Alta — Críticos para compilar / ejecutar

### 1. `ArticleViewModel.java`
**Ruta:** `src/main/java/com/app/ViewModel/ArticleViewModel.java`
**Estado:** Clase vacía — no tiene ningún método.

**Qué debe implementar:**
```java
public class ArticleViewModel extends BaseViewModel {
    private final ArticleService articleService = new ArticleService();
    private List<Article> articles = new ArrayList<>();
    private List<Article> filtered   = new ArrayList<>();

    // Métodos a implementar:
    void loadAll()                          // Carga todos los artículos
    void search(String name)                // Filtra por nombre
    void loadAvailableForPawn()             // Artículos con stock
    Article createArticle(Article a)        // Delega a ArticleService.create()
    void editArticle(Article a)             // Delega a ArticleService.edit()
    void addStock(int articleId, int qty)   // Aumentar inventario
    void removeStock(int articleId, int qty)// Reducir inventario
    void removeArticle(int articleId)       // Eliminar artículo
    List<Article> getArticles()
    List<Article> getFiltered()
}
```

**Buenas prácticas:**
- Extender `BaseViewModel` y notificar observers tras cada operación.
- Capturar `ServiceException` y re-lanzarla o enviarla al `UINotifier`.
- Usar `AsyncTaskExecutor` para no bloquear el EDT.

---

### 2. `AuthViewModel.java`
**Ruta:** `src/main/java/com/app/ViewModel/AuthViewModel.java`
**Estado:** Clase vacía — no tiene ningún método.

**Qué debe implementar:**
```java
public class AuthViewModel extends BaseViewModel {
    private final AuthService authService = new AuthService();

    // Métodos a implementar:
    void login(String email, String password)    // Llama AuthService.Login()
    void logout()                                // Llama AuthService.logout()
    void registerEmployee(String email,
                          String password,
                          String fullName)      // Solo Admin
    boolean isLoggedIn()
    String getCurrentUserName()
    RolUser getCurrentRole()
}
```

**Buenas prácticas:**
- El login debe ejecutarse en un `SwingWorker` / `AsyncTaskExecutor`.
- Notificar `"Login_Success"` o `"Login_Error"` a los observers.
- Limpiar la contraseña en memoria tras el intento.

---

### 3. `BaseRepository.java`
**Ruta:** `src/main/java/com/app/Repositories/BaseRepository.java`
**Estado:** Clase concreta vacía — debería ser una interfaz genérica o clase base.

**Qué debe implementar:**
```java
public interface BaseRepository<T, ID> {
    List<T>       findAll()              throws Exception;
    Optional<T>   findById(ID id)        throws Exception;
    T             save(T entity)         throws Exception;
    boolean       update(T entity)       throws Exception;
    boolean       delete(ID id)          throws Exception;
}
```

**Buenas prácticas:**
- Convertir a interfaz genérica `<T, ID>`.
- Las interfaces `ArticleRepository`, `ClienteRepository`, etc. deben extender esta.

---

## 🟡 Prioridad Media — Funcionalidad incompleta

### 4. `ArticleRespositoryImpl.java`
**Ruta:** `src/main/java/com/app/Repositories/impl/ArticleRespositoryImpl.java`
**Estado:** Todos los métodos retornan `List.of()`, `Optional.empty()` o `false`/`null`.

**Todos los métodos deben delegar al DAO correspondiente:**
```java
@Override
public List<Article> findAll() throws Exception {
    return articleDao.findAll();
}
@Override
public Optional<Article> findById(long id) throws Exception {
    return articleDao.findById((int) id);
}
// ... y así para findByName, findBySold, save, update, delete, updateAmount
```

> **Nota:** El contrato de `ArticleRepository.findBySold()` recibe un `String`
> pero el DAO recibe `boolean`. Debe corregirse: cambiar la firma de la interfaz a `boolean`.

---

### 5. `PawnRespositoryImpl.java`
**Ruta:** `src/main/java/com/app/Repositories/impl/PawnRespositoryImpl.java`
**Estado:** Todos los métodos retornan valores vacíos/falsos.

**Debe delegar a `PawnDao`** (igual que `ArticleRespositoryImpl`).

---

### 6. `ProfileRespositoryImpl.java`
**Ruta:** `src/main/java/com/app/Repositories/impl/ProfileRespositoryImpl.java`
**Estado:** Todos los métodos retornan vacíos.

**Debe delegar a `ProfileDao`.**

---

### 7. Interfaces de Repositorios con firmas incorrectas

| Interfaz | Problema |
|----------|----------|
| `ArticleRepository.findBySold(String)` | Debería ser `findBySold(boolean)` — el DAO usa `boolean` |
| `ClienteRepository.updateActive(boolean)` | Falta el parámetro `id` — ¿a qué cliente se actualiza? |
| `PawnRespository.findByCliente(String)` y `findBylast_name(String)` | El DAO no tiene estos métodos; usar `findByProfile(String)` |
| `ProfileReposiitory` | Tiene typo: doble `i` en el nombre del archivo |

---

## 🟠 Prioridad Media — Lógica de ventas sin implementar (Sale / SalesDetail)

### 8. `SaleDao.java` — **FALTA CREAR**
**Ruta propuesta:** `src/main/java/com/app/Model/Dao/SaleDao.java`

El sistema tiene los modelos `Sale` y `SalesDetail` pero **no tiene DAO ni Service para Ventas**. Sin esto no es posible registrar ventas.

**Métodos mínimos a implementar:**
```java
Sale      save(Sale sale)                       // INSERT en sales + details (transacción)
Optional<Sale> findById(int id)
List<Sale>    findAll()
List<Sale>    findByCliente(int clienteId)
List<Sale>    findByProfile(String profileId)
List<Sale>    findByDateRange(LocalDate from, LocalDate to)
boolean       delete(int id)
```

> **Importante:** La creación de una venta debe ser **transaccional** (usar `DataBaseManeger.runInTransaction()`): insertar en `sales` y en `sales_details` en la misma transacción o hacer rollback.

---

### 9. `SaleService.java` — **FALTA CREAR**
**Ruta propuesta:** `src/main/java/com/app/Service/SaleService.java`

**Responsabilidades:**
- Validar stock antes de procesar la venta.
- Llamar `ArticleService.removeStock()` por cada línea de detalle.
- Persistir `Sale` + `SalesDetail` en una transacción.
- Verificar permisos de rol.

---

### 10. `SaleViewModel.java` — **FALTA CREAR**
**Ruta propuesta:** `src/main/java/com/app/ViewModel/SaleViewModel.java`

**Responsabilidades:**
- Exponer la lista de ventas a la UI.
- Manejar el carrito de compra temporal antes de confirmar la venta.
- Notificar a los observers tras cada operación.

---

## 🔵 Prioridad Baja — Mejoras y paneles de UI

### 11. `ClientePanel.java` — **FALTA CREAR**
El panel de gestión de clientes no existe en la UI aunque `ClienteService` está implementado.

### 12. `SalePanel.java` — **FALTA CREAR**
Panel de registro y listado de ventas, incluyendo un carrito de compra.

### 13. `ClienteDialog.java`
**Ruta:** `src/main/java/com/app/UI/dialogs/ClienteDialog.java`
Verificar que el formulario llame correctamente a `ClienteService.create()` y `update()`.

---

## ✅ Archivos Corregidos en Esta Revisión

| Archivo | Corrección Aplicada |
|---------|---------------------|
| `Cliente.java` | Tipo `created_ad` corregido (Date → Timestamp), campos privados, `getFullName()` |
| `Article.java` | Constructor inválido eliminado, `canSell()` corregido, camelCase |
| `Pawn.java` | Constructor con `boolean profile_id` (error tipo) corregido |
| `ClienteDao.java` | SQL `VALUE` → `VALUES`, `findById` retornaba `Optional<Object>`, 3 params para 2 `?` |
| `AuthResponse.java` | `@JsonProperty("acceso_token")` → `"access_token"` |
| `SessionManager.java` | Agrega `getInstance()` que faltaba |
| `PawnService.java` | `SessionManager.isEmployee()` era llamado como instancia (es estático) |
| `ClienteService.java` | `throws Exception` genérico → `ServiceException`, normalizado |
| `AuthException.java` | No extendía `Exception` (clase vacía) — corregido |
| `BusinessException.java` | No extendía nada (clase vacía) → `RuntimeException` |
| `Sale.java` | `profile_id` era `int` (debería ser `String`/UUID), `Date` → `LocalDateTime` |
| `SalesDetail.java` | `unit_price` era `int` (debería ser `BigDecimal`), `getSubtotal()` agregado |
| `DataBaseManeger.java` | Implementado con soporte de transacciones |
| `pom.xml` | `mainClass` incorrecto, JUnit 3 → JUnit 5, BCrypt agregado, surefire plugin |
| `CurrencyUtils.java` | Implementado (estaba vacío) |
| `DateUtils.java` | Implementado (estaba vacío) |
| `StringUltils.java` | Implementado (estaba vacío) |
| `ValidationUtils.java` | Implementado (estaba vacío) |
| `CollectionUtils.java` | Implementado (estaba vacío) |
| `UIAsyncCallback.java` | Implementado (estaba vacío) |
| `LoggerFactory.java` | Implementado como `AppLoggerFactory` (estaba vacío) |

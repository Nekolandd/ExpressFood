# ExpressFood

Aplicación Android de pedidos de comida con arquitectura **Offline First**, desarrollada en Kotlin con Firebase, Room y WorkManager.

**Repositorio:** [github.com/Nekolandd/ExpressFood](https://github.com/Nekolandd/ExpressFood)  
**Firebase:** proyecto `expressfood-dp`  
**Versión:** `1.0` (`versionCode` 1) · `minSdk` 24 · `targetSdk` / `compileSdk` 36

---

## Descripción

ExpressFood es una app móvil de delivery que conecta **clientes** con un restaurante virtual. Los clientes pueden:

- Iniciar sesión con Google
- Explorar el menú con filtros por nombre e ingrediente
- Agregar productos al carrito y procesar órdenes (con impuesto del 13 %)
- Consultar el historial de pedidos y un reporte de consumo mensual
- Usar la app **sin conexión**: las órdenes se guardan localmente y se sincronizan al reconectar

Los **administradores** pueden:

- Ver todas las órdenes en tiempo real con filtros por estado, cliente y fecha
- Cambiar el estado de los pedidos (Pendiente → En camino → Entregada / Cancelada)
- Consultar reportes de ventas e ingresos
- Administrar el catálogo de productos (crear, editar, eliminar, habilitar/deshabilitar)

La app implementa arquitectura **MVVM + Repository**, cache local con **Room**, backend en **Firestore** y sincronización automática con **WorkManager**.

---

## Instrucciones de uso

### 1. Instalar y ejecutar

1. Clona el repositorio e importa el proyecto en Android Studio.
2. Coloca `google-services.json` en la carpeta `app/` (descárgalo de [Firebase Console](https://console.firebase.google.com) → proyecto **expressfood-dp**).
3. Registra el SHA-1 de debug en Firebase (ver sección [Solución error código 10](#solución-error-código-10-al-iniciar-con-google)).
4. Conecta un dispositivo o emulador (API 24+) y ejecuta la app, o compila con:

```powershell
.\gradlew.bat assembleDebug
```

### 2. Flujo del cliente

1. En la pantalla de login, pulsa **Continuar con Google** e inicia sesión con una cuenta **cliente** (ver [Credenciales de prueba](#credenciales-de-prueba)).
2. En **Menú**, busca productos por nombre o ingrediente y agrégalos al carrito.
3. Ve a **Carrito**, revisa subtotal, impuesto (13 %) y total, luego pulsa **Procesar Orden**.
4. En **Mis Órdenes**, filtra por estado y observa cómo cambian los badges cuando el admin actualiza el pedido.
5. En **Reporte**, consulta el total gastado por día y el acumulado del mes.
6. Para cerrar sesión, usa el ícono de logout en la barra superior.

### 3. Flujo del administrador

1. Inicia sesión con una cuenta **admin** (rol `"admin"` en Firestore).
2. En **Órdenes**, filtra por estado, nombre de cliente o fecha; cambia el estado de pedidos pendientes o en camino.
3. En **Reporte**, revisa cantidad de órdenes e ingresos por fecha y el total mensual.
4. En **Productos**, crea, edita, elimina o deshabilita ítems del menú.

### 4. Probar modo offline

1. Inicia sesión como cliente con conexión activa (para cachear el menú).
2. Activa **modo avión** o desactiva Wi‑Fi/datos.
3. Verifica que la toolbar muestre **OFFLINE**.
4. Agrega productos al carrito y procesa una orden → debe guardarse localmente.
5. Restaura la conexión → la toolbar vuelve a **ONLINE** y la orden se sincroniza con Firestore.

### 5. Promover un usuario a administrador

1. Inicia sesión una vez con la cuenta deseada (se crea el documento en Firestore).
2. Firebase Console → Firestore → colección `users` → documento `{uid}`.
3. Cambia el campo `role` de `"client"` a `"admin"`.
4. Cierra sesión, borra datos de la app (Ajustes → Apps → ExpressFood → Almacenamiento) e inicia sesión de nuevo.

---

## Credenciales de prueba

La app **no tiene usuario ni contraseña propios**. El acceso es exclusivamente con **Google Sign-In** (cuenta Gmail del evaluador).

### Cómo evaluar la app

| Rol | Cómo acceder | Qué probar |
| --- | --- | --- |
| **Cliente** | Inicia sesión con **cualquier cuenta Gmail** | Menú, carrito, órdenes, reporte personal, modo offline |
| **Administrador** | Usa una Gmail tuya (o del equipo), luego promuévela a admin en Firestore (ver abajo) | Panel de órdenes, reportes globales, CRUD de productos |

**No es necesario compartir correos ni contraseñas personales.** Cada evaluador usa su propia cuenta Google; la app crea el usuario en Firestore al primer login con rol `client` por defecto.

### Probar el rol administrador

1. Inicia sesión una vez con la Gmail que quieras usar como admin.
2. Firebase Console → Firestore → colección `users` → documento con tu `{uid}`.
3. Cambia `role` de `"client"` a `"admin"`.
4. Cierra sesión en la app, borra datos de la app (Ajustes → Apps → ExpressFood → Almacenamiento) e inicia sesión de nuevo.

> Si el rol no cambia, la app puede tener el rol cacheado en `UserSessionStore`. Borrar datos fuerza una lectura fresca desde Firestore.

### Alternativa para el equipo (cuenta de prueba dedicada)

Si el docente exige **una sola cuenta compartida**, creen un Gmail **nuevo solo para el proyecto** (por ejemplo `expressfood.una2026@gmail.com`), no usen correos personales. Esa contraseña sí se comparte con el evaluador; el acceso sigue siendo el botón **Continuar con Google** de la app.

### Datos del backend

| Recurso | Valor |
| --- | --- |
| Proyecto Firebase | `expressfood-dp` |
| Package Android | `com.example.expressfood` |
| Método de autenticación | Google Sign-In |
| Rol por defecto al registrarse | `client` |

### Menú precargado (seed)

Al primer arranque, si Firestore está vacío, se crean automáticamente:

| Producto | Precio |
| --- | --- |
| Pizza Margarita | $12.99 |
| Hamburguesa Clásica | $9.50 |
| Sushi Variado | $18.00 |
| Ensalada César | $8.25 |
| Tacos Mexicanos | $10.75 |

---

## Tecnologías

| Tecnología | Uso |
| --- | --- |
| Kotlin 2.0 | Lenguaje principal |
| Android Gradle Plugin 9.1 | Build del proyecto |
| ViewBinding | Vinculación de layouts |
| Material Components | UI (chips, bottom nav, dialogs) |
| Firebase Authentication | Login con Google |
| Firebase Firestore | Backend remoto con persistencia offline nativa |
| Room (SQLite) | Cache local de menú, carrito y órdenes |
| KSP | Procesamiento de anotaciones de Room |
| RecyclerView | Listas de menú, carrito, órdenes y reportes |
| MVVM + Repository | Arquitectura de capas |
| Coroutines + Flow | Operaciones asíncronas y streams reactivos |
| WorkManager | Sincronización periódica e inmediata |
| Coil | Carga de imágenes por URL |
| SharedPreferences | Sesión de usuario (`UserSessionStore`) |
| JUnit, Mockito, MockK, Robolectric | Pruebas unitarias |
| JaCoCo | Cobertura de código (objetivo ≥ 60 %) |
| GitHub Actions | CI/CD con APK firmado y releases |

---

## Arquitectura

```
ExpressFoodApplication   → Inicializa Room, Firestore, repos y workers
ui/
  login/                 → LoginActivity
  client/                → ClientActivity (bottom nav del cliente)
  admin/                 → AdminActivity, fragments y ViewModels del admin
  menu/                  → Menú y filtros
  cart/                  → Carrito y procesamiento de órdenes
  orders/                → Mis órdenes, reporte del cliente
  common/                → BaseConnectivityActivity, StatusBadgeHelper
  ExpressFoodViewModelFactory.kt
domain/model/            → User, Product, Order, CartItem, OrderStatus, UserRole
data/
  local/                 → Room (entities, DAOs, AppDatabase, Mapper)
  local/UserSessionStore → Rol y datos de sesión en SharedPreferences
  remote/                → Firestore data sources, ProductSeedData
  repository/            → Repositorios (fuente híbrida local + remota)
worker/                  → SyncMenuWorker, SyncOrdersWorker, SyncScheduler
util/                    → Filtros, cálculos, conectividad, reportes
```

### Flujo de login

1. `LoginActivity` autentica con Google (Firebase Auth).
2. `UserRepository.ensureUserExists()` consulta Firestore (servidor primero, caché si falla) y crea el usuario si no existe.
3. Rol por defecto: `client`.
4. El usuario se guarda en `UserSessionStore` (SharedPreferences).
5. Redirección según rol:
   - `client` → `ClientActivity`
   - `admin` → `AdminActivity`

### Room (cache local)

| Entidad | Contenido |
| --- | --- |
| `ProductEntity` | Menú cacheado |
| `CartItemEntity` | Carrito local |
| `OrderEntity` / `OrderItemEntity` | Órdenes offline (`synced` indica si ya se subieron) |

### Firestore (colecciones)

- **`users`** — `name`, `email`, `role` (`client` \| `admin`)
- **`products`** — `name`, `price`, `ingredients`, `estimatedTime`, `rating`, `imageUrl`, `enabled`
- **`orders`** — `userId`, `userName`, `createdAt`, `status`, `subtotal`, `tax`, `total`, `items[]`

### Offline First

| Escenario | Comportamiento |
| --- | --- |
| Menú | Cacheado en Room vía `SyncMenuWorker` |
| Crear orden sin internet | Guardada en Room con `synced=false` |
| Recuperar conexión | `SyncScheduler` encola sync inmediato; `SyncOrdersWorker` sube pendientes |
| Cambio de estado (admin) | Listener de Firestore actualiza Room en el cliente en tiempo real |
| Indicador UI | ONLINE / OFFLINE en toolbar |

### WorkManager

| Worker | Frecuencia | Función |
| --- | --- | --- |
| `SyncMenuWorker` | Cada 6 h (+ al conectar) | Descarga productos de Firestore → Room |
| `SyncOrdersWorker` | Cada 15 min (+ al conectar) | Sube órdenes pendientes y descarga del usuario |
| `SyncScheduler` | Al recuperar conexión | Encadena menú → órdenes |

---

## Pantallas

### Cliente (`ClientActivity`)

1. **Menú** — RecyclerView, SearchView (nombre + ingrediente), contador de ítems en carrito
2. **Carrito** — cantidades, subtotal, impuesto 13 %, total, procesar orden
3. **Mis Órdenes** — filtros por estado con badges de color
4. **Reporte** — total por día y acumulado mensual del usuario

### Admin (`AdminActivity`)

1. **Panel de Órdenes** — órdenes en tiempo real, filtros por estado / cliente / fecha
2. **Reporte** — cantidad e ingresos por fecha, acumulado mensual
3. **Productos (CRUD)** — crear, editar, eliminar, habilitar/deshabilitar

### Badges de estado

| Estado | Etiqueta UI | Color |
| --- | --- | --- |
| `CREATED` | Creada | Amarillo |
| `PENDING` | Pendiente | Amarillo |
| `ON_THE_WAY` | En camino | Azul |
| `DELIVERED` | Entregada | Verde |
| `CANCELLED` | Cancelada | Rojo |

---

## Instalación (desarrollo)

### Requisitos

- Android Studio reciente (compatible con AGP 9.x)
- JDK 17 (Gradle/CI; el bytecode del app usa Java 11)
- Cuenta Firebase con Authentication (Google) y Firestore habilitados

### Solución error código 10 al iniciar con Google

Si aparece **"10:"** o un toast de configuración, el SHA-1 de tu PC no está registrado en Firebase.

1. Obtén tu SHA-1 de debug:
   ```powershell
   .\gradlew.bat signingReport
   ```
2. [Firebase Console](https://console.firebase.google.com) → proyecto **expressfood-dp** → ⚙️ **Configuración del proyecto**
3. En **Tus apps** → app Android `com.example.expressfood` → **Agregar huella digital**
4. Pega el SHA-1 y guarda
5. Descarga el nuevo `google-services.json` y reemplázalo en `app/`
6. Firebase → **Authentication** → **Sign-in method** → activa **Google**
7. **Build → Clean Project** y vuelve a ejecutar la app

---

## CI/CD

Pipeline en [.github/workflows/android-ci.yml](.github/workflows/android-ci.yml):

| Job | Qué hace |
| --- | --- |
| `build-and-test` | `lintDebug`, `testDebugUnitTest`, `jacocoTestReport` |
| `build-apk` | Compila `assembleRelease` firmado, sube APK como artifact |
| `release` | Solo en tags `v*` — publica el APK en GitHub Releases |

```bash
git tag v1.0.1
git push origin v1.0.1
```

---

## Pruebas unitarias

**10 tests** en `app/src/test/`:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat jacocoTestReport
```

Reporte HTML: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

| # | Test | Archivo |
| --- | --- | --- |
| 1 | `filterByName_returnsMatchingItems` | `util/ProductFilterTest.kt` |
| 2 | `filterByIngredient_returnsMatchingItems` | `util/ProductFilterTest.kt` |
| 3 | `calculateFromCart_returnsSubtotalTaxAndTotal` | `util/OrderCalculatorTest.kt` |
| 4 | `groupOrdersByDate_groupsOrdersAndSumsTotals` | `util/ReportHelperTest.kt` |
| 5 | `monthlyAccumulated_sumsOrdersInMonth` | `util/ReportHelperTest.kt` |
| 6 | `setNameFilter_showsMatchingProducts` | `ui/menu/MenuViewModelTest.kt` |
| 7 | `setStatusFilter_returnsMatchingOrders` | `ui/orders/ClientOrdersViewModelTest.kt` |
| 8 | `processOrder_clearsCartOnSuccess` | `ui/cart/CartViewModelTest.kt` |
| 9 | `addProduct_insertsWhenNotInCart` | `data/repository/CartRepositoryTest.kt` |
| 10 | `createOrderFromCart_offlineMarksOrderAsNotSynced` | `data/repository/OrderRepositoryTest.kt` |

---

## Estructura de paquetes

```
com.example.expressfood
├── ExpressFoodApplication.kt
├── data.local / data.remote / data.repository
├── domain.model
├── ui.login / ui.client / ui.admin / ui.menu / ui.cart / ui.orders / ui.common
├── worker
└── util
```

---

## Licencia

Proyecto académico — ExpressFood.

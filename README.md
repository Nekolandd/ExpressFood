# ExpressFood

Aplicación Android de pedidos de comida con arquitectura **Offline First**, desarrollada en Kotlin con Firebase, Room y WorkManager.

**Repositorio:** [github.com/Nekolandd/ExpressFood](https://github.com/Nekolandd/ExpressFood)  
**Firebase:** proyecto `expressfood-dp`

## Descripción

ExpressFood permite a **clientes** explorar un menú, agregar productos al carrito, crear órdenes y consultar reportes de consumo. Los **administradores** gestionan órdenes en tiempo real, consultan reportes de ingresos y administran el catálogo de productos (CRUD).

## Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| Firebase Authentication | Login con Google |
| Firebase Firestore | Backend remoto (`users`, `products`, `orders`) |
| Room (SQLite) | Cache local y persistencia offline |
| RecyclerView | Listas de menú, carrito, órdenes |
| MVVM + Repository | Arquitectura de capas |
| WorkManager | Sincronización automática |
| Coil | Carga de imágenes por URL |
| GitHub Actions | CI/CD con APK firmado y releases |

## Arquitectura

```
ui/          → Activities, Fragments, ViewModels, Adapters
domain/      → Modelos de dominio (User, Product, Order, etc.)
data/
  local/     → Room (entities, DAOs, AppDatabase)
  remote/    → Firestore data sources
  repository/→ Repositorios (fuente única de verdad híbrida)
worker/      → SyncMenuWorker, SyncOrdersWorker, SyncScheduler
util/        → Filtros, cálculos, conectividad, reportes
```

### Flujo de login

1. `LoginActivity` autentica con Google (Firebase Auth).
2. Se verifica/crea el usuario en Firestore (`users/{uid}`), consultando el servidor primero.
3. Rol por defecto: `client`.
4. Redirección según rol:
   - `client` → `ClientActivity`
   - `admin` → `AdminActivity`

### Room (cache local)

| Entidad | Contenido |
|---|---|
| `ProductEntity` | Menú cacheado |
| `CartItemEntity` | Carrito local |
| `OrderEntity` / `OrderItemEntity` | Órdenes offline |

Room es la fuente inmediata para el cliente. Firestore es el backend remoto.

### Firestore (colecciones)

- `users` — id, name, email, role
- `products` — catálogo con `imageUrl`
- `orders` — órdenes con items embebidos

### Offline First

| Escenario | Comportamiento |
|---|---|
| Menú | Cacheado en Room vía `SyncMenuWorker` |
| Crear orden sin internet | Guardada en Room con `synced=false` |
| Recuperar conexión | `SyncScheduler` encola sync inmediato; `SyncOrdersWorker` sube pendientes y descarga cambios |
| Cambio de estado (admin) | Listener de Firestore actualiza Room en el cliente en tiempo real |
| Indicador UI | ONLINE / OFFLINE en toolbar |

### WorkManager y sincronización

- **SyncMenuWorker** — descarga productos de Firestore → Room (`NetworkType.CONNECTED`)
- **SyncOrdersWorker** — sube órdenes con `synced=false` y descarga órdenes remotas del usuario
- **SyncScheduler** — al volver la conexión, encola ambos workers en cadena (menú → órdenes)

**Flujo bidireccional de órdenes:**

1. Cliente crea orden → Room (inmediato) → Firestore si hay red.
2. Admin cambia estado → Firestore → listener en cliente → Room.
3. Sin conexión → órdenes en cola local hasta que `SyncOrdersWorker` las suba.

## Pantallas

### Cliente (`ClientActivity`)

1. **Menú** — RecyclerView, SearchView (nombre + ingrediente), badge de carrito
2. **Carrito** — cantidades, subtotal, impuesto 13%, total, procesar orden
3. **Mis Órdenes** — filtros por estado con badges de color
4. **Reporte** — total por día y acumulado mensual

### Admin (`AdminActivity`)

1. **Panel de Órdenes** — órdenes en tiempo real, filtros, cambio de estado (Pendiente / En camino / Entregada / Cancelada)
2. **Reporte** — cantidad e ingresos por fecha, acumulado mensual
3. **Productos (CRUD)** — crear, editar, eliminar, deshabilitar

### Badges de estado

| Estado | Color |
|---|---|
| PENDING | Amarillo |
| ON_THE_WAY | Azul |
| DELIVERED | Verde |
| CANCELLED | Rojo |

## Seed inicial

Si Firestore no tiene productos, se crean automáticamente:

- Pizza Margarita
- Hamburguesa Clásica
- Sushi Variado
- Ensalada César
- Tacos Mexicanos

Cada uno incluye imagen pública (Unsplash), precio, ingredientes, rating y tiempo estimado.

## Instalación

### Requisitos

- Android Studio (Ladybug o superior)
- JDK 17
- Cuenta Firebase con Authentication (Google) y Firestore habilitados

### Pasos

1. Clonar el repositorio
2. Colocar `google-services.json` en `app/` (no se sube al repo; descargarlo de Firebase Console)
3. Configurar SHA-1 en Firebase Console (obligatorio para Google Sign-In)
4. Abrir en Android Studio y sincronizar Gradle
5. Ejecutar en emulador o dispositivo (API 24+)

```powershell
.\gradlew.bat assembleDebug
```

### Error código 10 al iniciar con Google

Si aparece **"10:"** o un toast de configuración, el SHA-1 de tu PC no está registrado en Firebase.

1. Obtén tu SHA-1 de debug:
   ```powershell
   .\gradlew.bat signingReport
   ```
   En Windows, si `keytool` no está en PATH:
   ```powershell
   & "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
2. [Firebase Console](https://console.firebase.google.com) → proyecto **expressfood-dp** → ⚙️ **Configuración del proyecto**
3. En **Tus apps** → app Android `com.example.expressfood` → **Agregar huella digital**
4. Pega el SHA-1 (debug para desarrollo; release para APK de producción) y guarda
5. Descarga el nuevo `google-services.json` y reemplázalo en `app/`
6. Firebase → **Authentication** → **Sign-in method** → activa **Google**
7. **Build → Clean Project** y vuelve a ejecutar la app

> Cada máquina de desarrollo y el keystore de release tienen SHA-1 distintos. Registra todos los que uses.

## Crear un usuario admin

Por defecto todos los usuarios nuevos tienen `role: "client"`. Para promover a admin:

1. Iniciar sesión una vez con la cuenta deseada (esto crea el documento en Firestore)
2. Firebase Console → Firestore → colección `users` → documento `{uid}`
3. Cambiar el campo `role` de `"client"` a `"admin"`
4. Cerrar sesión, **borrar datos de la app** (Ajustes → Apps → ExpressFood → Almacenamiento) e iniciar sesión de nuevo

> Si el rol no cambia, la app puede tener el rol cacheado en Room. Borrar datos de la app fuerza una lectura fresca desde Firestore.

## CI/CD

Pipeline en [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml):

| Job | Qué hace |
|---|---|
| `build-and-test` | `lintDebug`, `testDebugUnitTest`, `jacocoTestReport`, sube reporte de cobertura |
| `build-apk` | Decodifica secrets, compila `assembleRelease` firmado, sube APK como artifact |
| `release` | Solo en tags `v*` — publica el APK en GitHub Releases |

### Secrets de GitHub (Settings → Secrets → Actions)

| Secret | Descripción |
|---|---|
| `GOOGLE_SERVICES_JSON` | Contenido de `app/google-services.json` en Base64 |
| `RELEASE_KEYSTORE_BASE64` | Keystore `.jks` en Base64 |
| `KEYSTORE_PASSWORD` | Contraseña del keystore |
| `KEY_ALIAS` | Alias de la clave (ej. `expressfood`) |
| `KEY_PASSWORD` | Contraseña de la clave |

### Publicar una release

```bash
git tag v1.0.1
git push origin v1.0.1
```

El workflow genera automáticamente un GitHub Release con el APK firmado.

## Pruebas unitarias

**10 tests** en `app/src/test/` que cubren la lógica principal del enunciado:

| # | Test | Archivo | Qué valida |
|---|---|---|---|
| 1 | `filterByName_returnsMatchingItems` | `util/ProductFilterTest.kt` | Filtro del menú por nombre |
| 2 | `filterByIngredient_returnsMatchingItems` | `util/ProductFilterTest.kt` | Filtro del menú por ingrediente |
| 3 | `calculateFromCart_returnsSubtotalTaxAndTotal` | `util/OrderCalculatorTest.kt` | Subtotal, impuesto 13% y total |
| 4 | `groupOrdersByDate_groupsOrdersAndSumsTotals` | `util/ReportHelperTest.kt` | Reporte agrupado por día |
| 5 | `monthlyAccumulated_sumsOrdersInMonth` | `util/ReportHelperTest.kt` | Acumulado mensual |
| 6 | `setNameFilter_showsMatchingProducts` | `ui/menu/MenuViewModelTest.kt` | ViewModel del menú |
| 7 | `setStatusFilter_returnsMatchingOrders` | `ui/orders/ClientOrdersViewModelTest.kt` | Filtro de órdenes del cliente |
| 8 | `processOrder_clearsCartOnSuccess` | `ui/cart/CartViewModelTest.kt` | Procesar orden y vaciar carrito |
| 9 | `addProduct_insertsWhenNotInCart` | `data/repository/CartRepositoryTest.kt` | Agregar producto al carrito |
| 10 | `createOrderFromCart_offlineMarksOrderAsNotSynced` | `data/repository/OrderRepositoryTest.kt` | Orden offline con `synced=false` |

```powershell
# Ejecutar los 10 tests
.\gradlew.bat testDebugUnitTest

# Generar reporte JaCoCo
# HTML en app/build/reports/jacoco/jacocoTestReport/html/index.html
.\gradlew.bat jacocoTestReport
```

Cobertura JaCoCo configurada sobre clases con lógica de negocio (filtros, cálculos, reportes, repositorios, ViewModels).

## Estructura de paquetes

```
com.example.expressfood
├── data.local
├── data.remote
├── data.repository
├── domain.model
├── ui.login
├── ui.menu
├── ui.cart
├── ui.orders
├── ui.admin
├── ui.client
├── worker
└── util
```

## Licencia

Proyecto académico — Grupo Estrella, ExpressFood.

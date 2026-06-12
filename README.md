# ExpressFood

Aplicación Android de pedidos de comida con arquitectura **Offline First**, desarrollada en Kotlin con Firebase, Room y WorkManager.

## Descripción

ExpressFood permite a clientes explorar un menú, agregar productos al carrito, crear órdenes y consultar reportes de consumo. Los administradores gestionan órdenes en tiempo real, consultan reportes de ingresos y administran el catálogo de productos (CRUD).

## Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| Firebase Authentication | Login con Google |
| Firebase Firestore | Backend remoto (users, products, orders) |
| Room (SQLite) | Cache local y persistencia offline |
| RecyclerView | Listas de menú, carrito, órdenes |
| MVVM + Repository | Arquitectura de capas |
| WorkManager | Sincronización automática |
| Coil | Carga de imágenes por URL |
| GitHub Actions | CI/CD |

## Arquitectura

```
ui/          → Activities, Fragments, ViewModels, Adapters
domain/      → Modelos de dominio (User, Product, Order, etc.)
data/
  local/     → Room (entities, DAOs, AppDatabase)
  remote/    → Firestore data sources
  repository/→ Repositorios (fuente única de verdad híbrida)
worker/      → SyncMenuWorker, SyncOrdersWorker
util/        → Filtros, cálculos, conectividad, reportes
```

### Flujo de login

1. `LoginActivity` autentica con Google (Firebase Auth).
2. Se verifica/crea el usuario en Firestore (`users/{uid}`).
3. Rol por defecto: `client`.
4. Redirección según rol:
   - `client` → `ClientActivity`
   - `admin` → `AdminActivity`

### Room (cache local)

- **ProductEntity** — menú cacheado
- **CartItemEntity** — carrito local
- **OrderEntity / OrderItemEntity** — órdenes offline

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
| Recuperar conexión | `SyncOrdersWorker` sube órdenes pendientes y descarga cambios del usuario |
| Cambio de estado (admin) | Firestore listener actualiza Room en el cliente en tiempo real |
| Indicador UI | ONLINE / OFFLINE en toolbar |

### WorkManager

- **SyncMenuWorker** — descarga productos de Firestore → Room (`NetworkType.CONNECTED`)
- **SyncOrdersWorker** — sube órdenes con `synced=false` y descarga órdenes remotas del usuario (`NetworkType.CONNECTED`)

### Sincronización de órdenes (bidireccional)

1. **Cliente crea orden** → Room (inmediato) → Firestore si hay red.
2. **Admin cambia estado** → Firestore → listener en cliente → Room.
3. **Sin conexión** → órdenes quedan en cola local hasta `SyncOrdersWorker`.

## Pantallas

### Cliente (`ClientActivity`)

1. **Menú** — RecyclerView, SearchView (nombre + ingrediente), badge de carrito
2. **Carrito** — cantidades, subtotal, impuesto 13%, total, procesar orden
3. **Mis Órdenes** — filtros por estado con badges de color
4. **Reporte** — total por día y acumulado mensual

### Admin (`AdminActivity`)

1. **Panel de Órdenes** — todas las órdenes en tiempo real, filtros, cambio de estado
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
2. Colocar `google-services.json` en `app/`
3. Configurar SHA-1 en Firebase Console (obligatorio para Google Sign-In)
4. Abrir en Android Studio y sincronizar Gradle
5. Ejecutar en emulador o dispositivo (API 24+)

```bash
./gradlew assembleDebug
```

### Error código 10 al iniciar con Google

Si aparece **"10:"** o un toast de configuración, el SHA-1 de tu PC no está registrado en Firebase.

1. Obtén tu SHA-1 de debug:
   ```bash
   .\gradlew.bat signingReport
   ```
2. Copia el valor **SHA1** de la variante `debug` (ejemplo en este equipo):
   `91:B4:9C:4E:2C:BD:9B:98:F0:1D:E7:EC:56:15:14:8A:1F:33:25:B8`
3. [Firebase Console](https://console.firebase.google.com) → proyecto **expressfood-dp** → ⚙️ **Configuración del proyecto**
4. En **Tus apps** → app Android `com.example.expressfood` → **Agregar huella digital**
5. Pega el SHA-1 y guarda
6. Descarga el nuevo `google-services.json` y reemplázalo en `app/`
7. En Firebase → **Authentication** → **Sign-in method** → activa **Google**
8. **Build → Clean Project** y vuelve a ejecutar la app

## Crear un usuario admin

Por defecto todos los usuarios nuevos tienen `role: "client"`. Para promover a admin:

1. Iniciar sesión una vez con la cuenta deseada
2. En Firebase Console → Firestore → colección `users`
3. Editar el documento del usuario (`{uid}`)
4. Cambiar el campo `role` de `"client"` a `"admin"`
5. Cerrar sesión y volver a iniciar sesión

## CI/CD

Pipeline en `.github/workflows/android-ci.yml`:

1. `lintDebug`
2. `testDebugUnitTest`
3. `assembleRelease`
4. Subida del APK como artifact

## Pruebas unitarias

Cobertura mínima objetivo: **60%** sobre clases con lógica.

| Test | Archivo |
|---|---|
| Filtro por nombre / ingrediente | `ProductFilterTest` |
| RecyclerView Adapters | `ProductAdapterTest`, `CartAdapterTest` |
| ViewModels cliente | `CartViewModelTest`, `MenuViewModelTest`, `ClientOrdersViewModelTest`, `ClientReportViewModelTest` |
| ViewModels admin | `AdminViewModelsTest` |
| Cálculo subtotal/impuesto/total | `OrderCalculatorTest` |
| Reportes por día / mes | `ReportHelperTest` |
| Repositories | `ProductRepositoryTest`, `OrderRepositoryTest`, `CartRepositoryTest`, `UserRepositoryTest` |
| Mappers y dominio | `MapperTest`, `DomainModelTest` |
| Sesión offline | `UserSessionStoreTest` |
| Extensiones UI | `ViewModelFactoryTest` |

```bash
# Ejecutar tests
.\gradlew.bat testDebugUnitTest

# Generar reporte JaCoCo (HTML en app/build/reports/jacoco/jacocoTestReport/html/index.html)
.\gradlew.bat jacocoTestReport
```

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

Proyecto académico — ExpressFood.

# Microframeworks‑WEB — Mini framework HTTP en Java (Maven)

Mini framework HTTP en **Java 17** (proyecto **Maven**) **sin frameworks externos** para:

- Servir **archivos estáticos**.
- Definir **rutas GET** con **lambdas**.
- Leer **parámetros de consulta** (`?name=...`).
- Endpoint demo **/stocks** (proxy a **AlphaVantage**) que **siempre devuelve JSON**.

> El proyecto respeta la estructura **Maven** (`src/main/java|resources`, `src/test/java`). El repositorio **NO** debe contener la carpeta `target/` ni artefactos del IDE; **`.gitignore`** ya los cubre. Probado en Windows con Git Bash e IntelliJ.

---

## 📁 Estructura

```
src/
 └─ main/
     ├─ java/
     │   ├─ com/mycompany/httpserver/
     │   │   ├─ HttpServer.java       # núcleo del framework (router)
     │   │   ├─ HttpRequest.java      # acceso a query params
     │   │   ├─ HttpResponse.java     # placeholder para headers/estado
     │   │   └─ Service.java          # interfaz funcional (lambda)
     │   └─ com/mycompany/webapplication/
     │       └─ WebAplication.java    # ejemplo de uso (app demo)
     └─ resources/
         └─ static/                   # web app (frontend)
             ├─ index.html
             ├─ css/styles.css
             ├─ js/app.js
             └─ images/logo.png
└─ src/test/java/com/mycompany/httpserver/
   ├─ HttpRequestQueryTest.java       # parsing y URL-decoding de query params
   ├─ HttpServerInternalsTest.java    # normalización de rutas y estáticos
   └─ HttpServerIntegrationTest.java  # server en vivo: rutas y index estático
```

---

## 🔧 Requisitos

- **Java** 21 (JDK 21)
- **Maven** 3.9+

---

## 🚀 Cómo correr

### Opción 1) Maven + `exec:java`

```bash
# Compilar (con tests)
mvn -q -DskipTests=false clean package

# Ejecutar la app de ejemplo (puerto 35000)
mvn -q -Dexec.mainClass=com.mycompany.webapplication.WebAplication exec:java

# O ejecutar el servidor "puro" (por defecto 8080)
mvn -q -Dexec.mainClass=com.mycompany.httpserver.HttpServer exec:java
```

### Opción 2) Scripts

```bash
# Windows
./run.bat

# Linux/macOS
./run.sh
```

### Opción 3) **IntelliJ IDEA (Run/Debug)**

1. **WebAplication (recomendado para la demo web):**  
   - Run → *Edit Configurations…* → **+ Application**.  
   - *Main class*: `com.mycompany.webapplication.WebAplication`  
   - *Program arguments*: `35000` (o el puerto que prefieras).  
   - *Environment variables* (opcional para `/stocks`): `ALPHAVANTAGE_API_KEY=TU_KEY`  
   - `Run` ▶

2. **HttpServer (núcleo del microframework):**  
   - *Main class*: `com.mycompany.httpserver.HttpServer`  
   - Sin argumentos (o un puerto si tu implementación lo permite).  
   - `Run` ▶

La app quedará disponible en: **http://localhost:35000/** (o el puerto que indiques).

---

## 🧩 API del microframework

### 1) Definir rutas GET con **lambdas**
```java
HttpServer.get("/app/hello", (HttpRequest req, HttpResponse resp) ->
    "{"message":"Hello " + req.getValues("name") + ""}"
);
```

### 2) Leer parámetros de consulta
```java
String name = req.getValues("name");         // "Ana"
Map<String, String> all = req.getQueryMap(); // {name=Ana, x=1}
```

### 3) Servir archivos estáticos
```java
HttpServer.staticfiles("/static"); // copia /src/main/resources/static -> /target/classes/static
```
> Los estáticos se sirven con **tipos MIME** correctos y soporte de **archivos binarios** (CSS, PNG, JS, etc.).

---

## 🧪 Pruebas automatizadas (JUnit)

Ejecuta todas las pruebas:
```bash
mvn -q test
```
Salida esperada (resumen):
```
[INFO] Results:
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

**Clases de prueba incluidas (puedes correrlas con botón Run en IntelliJ):**
- `HttpRequestQueryTest` — parsing y URL-decoding de query params.  
- `HttpServerInternalsTest` — normalización de rutas y configuración de estáticos.  
- `HttpServerIntegrationTest` — levanta el server y valida `/app/test` e `index.html`.

> Nota: si también usas `java.net.http.HttpResponse` en tests de cliente, evita choques de nombres con `com.mycompany.httpserver.HttpResponse` usando nombres completamente calificados en el cliente HTTP.

---

## 🧪 Endpoints demo y ejemplos

### Estáticos
- `GET /` → `index.html`  
- `GET /css/styles.css`, `/js/app.js`, `/images/logo.png`

### REST (GET)
```java
// WebAplication.java
HttpServer.staticfiles("/static");

HttpServer.get("/app/hello", (req, resp) ->
    "{"message":"Hello " + escape(req.getValues("name")) + ""}"
);

HttpServer.get("/app/pi", (req, resp) ->
    "{"pi":"" + Math.PI + ""}"
);

// Proxy demo a AlphaVantage (si no hay API key usa "demo" y fuerza IBM)
HttpServer.get("/stocks", (req, resp) -> { /* ...ver código del repo... */ });

HttpServer.startServer(new String[]{"35000"});
```

- `GET /app/hello?name=John` → `{"message":"Hello John"}`  
- `GET /app/pi` → `{"pi":"3.141592653589793"}`  
- `GET /stocks?symbol=fb`
  - Con **tu API key**: devuelve el JSON de AlphaVantage.
  - Con **demo key**: AlphaVantage solo permite **IBM**. El framework devuelve:
    ```json
    {
      "notice":"demo key in use; AlphaVantage solo permite 'IBM'. Requested='fb', served='IBM'.",
      "data": { ... JSON de AlphaVantage ... }
    }
    ```

#### cURL rápido
```bash
curl -i http://localhost:35000/index.html
curl -i "http://localhost:35000/app/hello?name=John"
curl -i http://localhost:35000/app/pi
curl -i "http://localhost:35000/stocks?symbol=IBM"
```

---

## 🔑 API Key (AlphaVantage)

Para datos reales en `/stocks` (cualquier símbolo), define `ALPHAVANTAGE_API_KEY`.

**Windows PowerShell**
```powershell
$env:ALPHAVANTAGE_API_KEY="TU_KEY"
```

**CMD**
```cmd
set ALPHAVANTAGE_API_KEY=TU_KEY
```

**IntelliJ IDEA**  
Run → Edit Configurations → Environment variables → `ALPHAVANTAGE_API_KEY=TU_KEY`

---

## 🛠️ Diseño / Detalles técnicos

- **Router case‑insensitive**: las rutas se guardan y buscan en minúsculas (`/App/Hello` ≡ `/app/hello`).
- **Lambdas**: `Service` es una interfaz funcional (`String handle(HttpRequest, HttpResponse)`).
- **Query params**: `HttpRequest#getValues("name")` (URL‑decoded) y `getQueryMap()`.
- **Binarios seguros**: los estáticos se envían con `OutputStream` y `Content‑Length`.
- **MIME**: detección por extensión con *fallback* `application/octet-stream`.
- **Contenido JSON**: los handlers devuelven `application/json; charset=utf-8`.

---

## ✅ Checklist del enunciado (entregables)

- [x] **Deployed on GitHub** (ver comandos abajo).
- [x] **Complete `.gitignore`** (ignora `target/`, `.idea/`, etc.).
- [x] **Has README.md** (este documento).
- [x] **Contains no unnecessary files** (elimina `.idea/`, `target/` si aparecen).
- [x] **Has a `pom.xml`**.
- [x] **Respects Maven structure**.
- [x] **Does not contain the `target/` folder**.
- [x] **Meets functional requirements**: `get()`, query params, `staticfiles()`.
- [x] **README** con instalación/uso y **evidencia de tests**.
- [x] **Has automated tests** (JUnit).
- [x] **Repository can be cloned and executed**.

---

## 🩺 Troubleshooting

- **404 en `/app/hello`** → verifica que la ruta esté registrada y usa minúsculas (el router es case‑insensitive, pero conviene consistencia).
- **Mensaje “demo…” en `/stocks`** → estás usando la key `demo`. Configura tu API key o prueba con `symbol=IBM`.
- **Puerto ocupado** → usa otro puerto como argumento o cierra instancias previas.
- **Conflicto de `HttpResponse` en tests** → si usas `java.net.http.HttpResponse`, referencia esa clase con su nombre totalmente calificado para evitar choque con `com.mycompany.httpserver.HttpResponse`.

---

## 📝 Licencia

Incluye `LICENSE` (MIT u otra que defina tu curso).

---

**Fuentes consolidadas de los README originales del proyecto.**

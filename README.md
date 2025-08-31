# Microframeworks-WEB — HTTP Server + Mini-IoC (Java 21, Maven)

A tiny educational HTTP server + reflection-based **IoC microframework** in **pure Java** (no external web frameworks).  
It serves **static files** (HTML/CSS/JS/PNG) and maps **`@RestController` + `@GetMapping`** methods with **`@RequestParam`** into HTTP routes.

> Built for the “Arquitecturas de Servidores de Aplicaciones / IoC & Reflection” workshop.

---

## ✨ Features

- Minimal **HTTP server** (non-concurrent, single thread) with static asset pipeline.
- **IoC via annotations**: `@RestController`, `@GetMapping`, `@RequestParam`.
- Load controllers **by argument** (initial version) or **scan package** (final version).
- Simple parameter binding (String/int/long/boolean) with default values.
- Demo endpoints: `/app/hello`, `/greeting`, `/stocks`.
- Maven project layout, JDK 21.

---

## 🚀 Quickstart

### Requirements
- **JDK 21**
- **Maven 3.9+**

### Build
```bash
mvn -q clean package
```

### Run (two modes)

**A) Scan classpath (final version):**
```bash
# Default port 36000 (override with -DPORT=35000)
java -DPORT=35000 -cp target/classes com.mycompany.microframework.core.MicroSpringBoot
```

**B) Explicit controllers (initial version):**
```bash
java -DPORT=35000 -cp target/classes   com.mycompany.microframework.core.MicroSpringBoot   com.mycompany.webapp.controllers.GreetingController   com.mycompany.webapp.controllers.HelloApiController   com.mycompany.webapp.controllers.StocksApiController
```

> Alternative demo main (static site only):  
> `com.mycompany.webapplication.WebAplication` (use a **different port** if MicroSpringBoot is running).

---

## 🧩 Endpoints

- **Static:**  
  `GET /` → `index.html`  
  `GET /css/styles.css`, `/js/app.js`, `/images/logo.png`

- **Hello (JSON):**  
  `GET /app/hello?name=John` → `{"ok":true,"message":"Hola John"}`  
  Aliases also mapped: `/hello`, `/api/hello`

- **Greeting (String):**  
  `GET /greeting?name=Daniel` → `Hola Daniel`

- **Stocks (JSON mock):**  
  `GET /stocks?symbol=ibm` → deterministic dummy price  
  Aliases also mapped: `/api/stocks`, `/app/stocks`  
  (Front-end reads `/stocks?symbol=…`)

---

## 🛠️ Project Structure

```
src/
 └─ main/
     ├─ java/
     │   ├─ com/mycompany/httpserver/                # HTTP kernel
     │   ├─ com/mycompany/microframework/annotations # @RestController, @GetMapping, @RequestParam
     │   ├─ com/mycompany/microframework/core        # MicroSpringBoot, RouteRegistry, ClassScanner
     │   └─ com/mycompany/webapp/controllers         # Greeting, Hello, Stocks (demo)
     └─ resources/
         └─ static/                                  # index.html, css, js, images
```

---

## 🧠 How the IoC works (mini spec)

- `@RestController` on class → component to load.
- `@GetMapping("/path")` on **methods returning `String`** → HTTP GET route.
- Method args:
  - `@RequestParam("name", defaultValue="World") String name`
  - Optional injection of `HttpRequest` / `HttpResponse`.
- **Registration order**:
  1) Build **`RouteRegistry`** and **register controllers** (from args or by scanning `com.mycompany.webapp`).
  2) Start the HTTP server **after** routes are registered.
- Query extraction is robust (`getQueryMap()`, single-param getters, or parsing the URL) so `/greeting?name=Maira` and `/stocks?symbol=fb` work reliably.

---

## 🧪 Tests

```bash
mvn -q test
```

---

## 🩺 Troubleshooting

- **`Not Found: /greeting`** on port *X*: you likely launched `WebAplication` (static-only) instead of `MicroSpringBoot`. Kill the process and run MicroSpringBoot.
  ```powershell
  netstat -ano | findstr :35000
  taskkill /PID <PID_LISTENING> /F
  ```
- **Port already in use** (`BindException`): stop the existing run or change `-DPORT`.
- **PowerShell errors** when copying console logs: lines like `[ioc] map GET …` are **logs**, not commands.
- **Maven error “project references itself”**: ensure your `pom.xml` does **not** declare the project as a dependency of itself; use a single module with tests only.
- **CRLF/LF warnings**: harmless; Git normalizes line endings.

---

## 📄 License
MIT (or the license your course requires).

---

# 🇪🇸 README (Resumen en Español)

**Micro servidor HTTP + mini-IoC con anotaciones** (`@RestController`, `@GetMapping`, `@RequestParam`) en **Java 21** y **Maven**. Sirve estáticos y expone endpoints de ejemplo: `/app/hello`, `/greeting`, `/stocks`.

### Ejecutar
```bash
mvn -q clean package

# Escaneo (versión final)
java -DPORT=35000 -cp target/classes com.mycompany.microframework.core.MicroSpringBoot

# Por argumentos (versión inicial)
java -DPORT=35000 -cp target/classes com.mycompany.microframework.core.MicroSpringBoot   com.mycompany.webapp.controllers.GreetingController   com.mycompany.webapp.controllers.HelloApiController   com.mycompany.webapp.controllers.StocksApiController
```

### Endpoints
- `/` (index.html estático)
- `/app/hello?name=Ana` → JSON  
- `/greeting?name=Ana` → `Hola Ana` (texto)
- `/stocks?symbol=ibm` → JSON con precio “dummy” (y alias `/api/stocks`, `/app/stocks`)

### Problemas comunes
- **404 en `/greeting`**: estás ejecutando `WebAplication`. Usa `MicroSpringBoot` o cambia el puerto.  
- **Puerto ocupado**: cierra procesos previos (`taskkill`) o usa otro `-DPORT`.

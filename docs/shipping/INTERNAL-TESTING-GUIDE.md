# Guía Paso a Paso — Prueba Interna + Prueba Cerrada en Google Play

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field      | Value                                              |
|------------|----------------------------------------------------|
| **Fecha**  | 2026-03-08                                         |
| **AAB**    | `app/build/outputs/bundle/release/app-release.aab` |
| **Versión**| 1.0.0 (versionCode 4)                              |

---

## Diferencia entre Internal y Closed Testing

| | Internal Testing | Closed Testing |
|---|---|---|
| **Qué es** | Prueba privada para ti y tu equipo | Prueba más amplia que desbloquea producción |
| **Testers** | 1-100, los que quieras | **12+ testers obligatorios** |
| **Revisión de Google** | **No** — disponible en minutos | Sí — puede tardar horas |
| **Requisito 14 días** | No | **Sí** — 14 días activo para solicitar producción |
| **Cuándo usarla** | **Ahora** — probar tú + 1 persona | **Después** — cuando estés listo para ir a producción |

**Flujo recomendado:**
1. 🟢 **Internal Testing** (ahora) → tú + 1 persona → smoke test real
2. 🔵 **Closed Testing** (cuando estés satisfecho) → 12+ testers → 14 días → producción

---

## 📍 El AAB está aquí

```
Santoro/app/build/outputs/bundle/release/app-release.aab
```

Si necesitas regenerarlo: `./gradlew bundleRelease`

---

# 🟢 FASE 1 — Internal Testing (ahora)

---

## Paso 1 — Crear la app en Google Play Console (si no existe)

1. Ve a [Google Play Console](https://play.google.com/console)
2. **Crear aplicación**
3. Rellena:
   - Nombre: **Santoro**
   - Idioma predeterminado: **English (United States)**
   - Tipo: **App**
   - Gratuita
4. Acepta las declaraciones y pulsa **Crear aplicación**

---

## Paso 2 — Subir el AAB a Internal Testing

1. Play Console → **Testing → Internal testing**
2. Pulsa **Create new release**
3. **App signing:** si es la primera vez, acepta **Google Play App Signing** (recomendado)
4. **Upload** → selecciona `app/build/outputs/bundle/release/app-release.aab`
5. **Release name:** `1.0.0 (3)`
6. **Release notes** (solo para ti, pon lo que quieras):

```
Internal smoke test — v1.0.0
```

7. Pulsa **Review release** → **Start rollout to Internal testing**

---

## Paso 3 — Añadir testers (tú + 1 persona)

1. Play Console → **Internal testing → Testers**
2. **Create email list** → nombre: "Internal Testers"
3. Añade **tu Gmail** + el Gmail de la otra persona
4. **Save** → copia el **opt-in link**
5. Envía el link manualmente (WhatsApp, Telegram, email) — **Google NO envía invitación automática**
6. Cada tester abre el opt-in link **desde el móvil** (con la cuenta Gmail que añadiste)
7. Pulsa "Aceptar" → espera 2-5 min → instala desde Play Store

> ⚠️ **Debe ser un dispositivo real**, no emulador. Y el Play Store debe estar con la cuenta Gmail que añadiste.

### Mensaje para enviar al tester

**Español:**
```
¡Hola! 👋

Estoy probando mi app de cine "Santoro" antes de lanzarla en Google Play. ¿Me echas una mano? Solo son 2 minutos:

1. Abre este enlace desde tu móvil Android: [PEGAR OPT-IN LINK]
2. Pulsa "Aceptar" para unirte a la prueba
3. Espera 2-5 minutos y pulsa el enlace de instalación que aparece
4. Instala la app y ábrela

Es una app para buscar películas, guardarlas en tu watchlist y marcar las que ya has visto. Puedes iniciar sesión con Google o usarla como invitado.

Si ves algo raro o algo que no funciona, dime. ¡Gracias! 🙏
```

**English:**
```
Hey! 👋

I'm testing my movie app "Santoro" before launching on Google Play. Can you help me out? Just 2 minutes:

1. Open this link on your Android phone: [PASTE OPT-IN LINK]
2. Tap "Accept" to join the test
3. Wait 2-5 minutes, then tap the install link
4. Install the app and open it

It's an app to browse movies, save them to a watchlist, and track what you've watched. You can sign in with Google or use it as a guest.

If you spot anything weird or broken, let me know. Thanks! 🙏
```

---

## Paso 4 — Smoke test en dispositivo real

Ejecuta estos flujos en el móvil con la build de Play Store:

### Flujos críticos
- [ ] Fresh install → Login → Sign in with Google → Home
- [ ] Fresh install → Continue as Guest → Home → Browse
- [ ] Search → resultados → tap película → detail carga
- [ ] Movie Detail → mark Watched → aparece en Watched
- [ ] Movie Detail → add Watchlist → aparece en Watchlist
- [ ] Watchlist → swipe to remove → eliminado
- [ ] Watched → stats dashboard muestra datos correctos
- [ ] Settings → cambiar tema → aplica inmediatamente
- [ ] Settings → cambiar idioma → app reinicia en idioma seleccionado
- [ ] Settings → sign out → vuelve a Login
- [ ] Settings → delete account → confirmación → vuelve a Login

### Edge cases
- [ ] Modo avión → browse muestra datos cacheados
- [ ] Modo avión → Watchlist y Watched funcionan offline
- [ ] Pull-to-refresh sin internet → no crash
- [ ] Búsqueda sin resultados → empty state
- [ ] Navegación atrás → no pantallas en blanco
- [ ] Rotar dispositivo → estado preservado

### Rendimiento
- [ ] Cold start < 3 segundos
- [ ] Scroll fluido en browse (sin jank)
- [ ] Navegar adelante/atrás 10+ veces → sin leaks evidentes

---

## Paso 5 — Iterar

Si encuentras bugs:
1. Corrígelos en el código
2. `./gradlew bundleRelease`
3. Play Console → Internal testing → **Create new release** → sube el nuevo AAB
4. Los testers se actualizan automáticamente

Repite hasta que el smoke test esté limpio.

---

# 🔵 FASE 2 — Closed Testing (cuando el smoke test esté OK)

> Esta fase arranca el reloj de 14 días que necesitas para solicitar producción.

---

## Paso 6 — Completar App Content (obligatorio para Closed Testing)

Estos formularios se completan en **Play Console → App content**:

**Privacy Policy:**
1. Play Console → **App content → Privacy policy**
2. URL: `https://asensiodev.github.io/Santoro/legal/PRIVACY-POLICY`

**App access:**
1. Play Console → **App content → App access**
2. Selecciona: **All functionality is available without special access**
   - (La app permite "Continue as Guest", no requiere credenciales especiales)

**Ads:**
1. Play Console → **App content → Ads**
2. Selecciona: **No, my app does not contain ads**

**Content rating:**
1. Play Console → **App content → Content rating**
2. Inicia el cuestionario IARC
3. Respuestas clave:
   - ¿Contenido generado por usuarios? **No**
   - ¿Contiene violencia/sangre/etc.? **No** a todo
   - ¿Compras in-app? **No**
   - ¿Publicidad? **No**
4. Resultado esperado: **PEGI 3 / Everyone**

**Target audience:**
1. Play Console → **App content → Target audience and content**
2. Público objetivo: **18 y más** (no dirigida a niños)
3. ¿Contiene anuncios? **No**

**Data safety:**
1. Play Console → **App content → Data safety**
2. Respuestas:

| Pregunta | Respuesta |
|----------|-----------|
| ¿La app recoge o comparte datos de usuario? | **Sí** |
| ¿Se cifran los datos en tránsito? | **Sí** (Firebase usa HTTPS) |
| ¿Los usuarios pueden solicitar borrado de datos? | **Sí** (Delete Account en Settings) |

Datos recogidos:

| Dato | Categoría | Propósito | ¿Obligatorio? | ¿Compartido? |
|------|-----------|-----------|----------------|--------------|
| Email address | Personal info | Account management | Sí (para login Google) | No |
| Name | Personal info | Account management | Sí (para login Google) | No |
| App interactions | App activity | Analytics | Sí | No |
| Crash logs | App info and performance | App functionality | Sí | No |

> Marca: datos NO vendidos, NO usados para publicidad.

---

## Paso 7 — Configurar la ficha de tienda (Store Listing)

Ahora sí necesitas la ficha completa:

### 7.1 Información principal

| Campo | Valor |
|-------|-------|
| **App name** | Santoro |
| **Short description** (≤80 chars) | Ver textos en §A.1 |
| **Full description** (≤4000 chars) | Ver textos en §A.2 |
| **Category** | Entertainment |

### 7.2 Gráficos

| Asset | Requisito |
|-------|-----------|
| **App icon** | 512×512 PNG — ya existe: `core/design-system/src/main/ic_launcher-playstore.png` |
| **Feature graphic** | 1024×500 PNG — haz uno en Figma/Canva con el logo + nombre + fondo oscuro |
| **Screenshots** | Mínimo 2 para teléfono. Capturas de: Browse, Movie Detail, Watchlist, Watched Stats |

---

## Paso 8 — Crear Closed Testing release

1. Play Console → **Testing → Closed testing**
2. Pulsa **Create track** (si no existe) → nombre: "Beta"
3. **Create new release** → sube el mismo AAB (o uno más reciente)
4. **Release name:** `1.0.0 (2)`
5. **Release notes:**

**English:**
```
First beta release of Santoro — your personal movie companion.

What's included:
• Browse trending, popular, top rated, and upcoming movies
• Full movie details: cast, crew, tagline, ratings
• Mark movies as Watched with stats dashboard
• Save movies to your Watchlist
• Smart search with recent and trending suggestions
• Cloud sync with Google Sign-In
• Offline support
• Light, dark, and system themes
• English and Spanish

Please report any issues to the developer.
```

**Español:**
```
Primera versión beta de Santoro — tu compañero personal de cine.

Incluye:
• Explorar películas en tendencia, populares, mejor valoradas y próximas
• Detalle completo: reparto, equipo, tagline, valoraciones
• Marcar películas como vistas con panel de estadísticas
• Guardar películas en tu Watchlist
• Búsqueda inteligente con sugerencias recientes y en tendencia
• Sincronización en la nube con Google Sign-In
• Soporte offline
• Temas claro, oscuro y del sistema
• Inglés y español

Por favor, reporta cualquier problema al desarrollador.
```

6. Pulsa **Review release** → **Start rollout to Closed testing**

---

## Paso 9 — Invitar 12+ testers

1. Play Console → **Closed testing → Testers**
2. **Create email list** → nombre: "Santoro Beta Testers"
3. Añade **12+ emails** de Gmail (amigos, familia, colegas)
4. **Save** → copia el **opt-in link**

### Mensaje para enviar a los testers

**Español:**
```
¡Hola! 👋

Estoy a punto de lanzar mi app de cine "Santoro" en Google Play y necesito tu ayuda para la fase de prueba.

¿Qué necesito de ti? Solo 2 minutos:

1. Abre este enlace desde tu móvil Android: [PEGAR OPT-IN LINK]
2. Pulsa "Aceptar" para unirte a la prueba
3. Espera 2-5 minutos y pulsa el enlace de instalación que aparece (o busca "Santoro" en Play Store)
4. Instala la app y ábrela una vez

¡Eso es todo! No necesitas usarla a diario ni hacer nada más. Solo necesito que la instales para cumplir el requisito de Google Play.

Si quieres echar un vistazo: es una app para buscar películas, guardarlas en tu watchlist y marcar las que ya has visto. Puedes iniciar sesión con Google o usarla como invitado.

¡Gracias! 🙏
```

**English:**
```
Hey! 👋

I'm about to launch my movie app "Santoro" on Google Play and need your help with beta testing.

What do I need? Just 2 minutes:

1. Open this link on your Android phone: [PASTE OPT-IN LINK]
2. Tap "Accept" to join the test
3. Wait 2-5 minutes, then tap the install link (or search "Santoro" in Play Store)
4. Install the app and open it once

That's it! You don't need to use it daily or do anything else. I just need installs to meet Google Play's requirements.

If you're curious: it's an app to browse movies, save them to a watchlist, and track what you've watched. You can sign in with Google or use it as a guest.

Thanks! 🙏
```

---

## Paso 10 — Esperar 14 días

| Hito | Fecha estimada |
|------|----------------|
| Subir AAB a Closed Testing | Cuando smoke test OK |
| 12+ testers opt-in | 1-4 días después |
| 14 días completados | **+14 días desde la subida** |
| Solicitar acceso a producción | Ese mismo día |

**Durante la espera:**
- Monitoriza **Crashlytics** para crashes
- Revisa el **Pre-launch report** en Play Console (Google ejecuta tests automáticos)
- Puedes subir nuevos AABs si encuentras bugs — los testers se actualizan automáticamente
- Prepara los **screenshots** finales para la ficha de producción

---

## Paso 11 — Solicitar acceso a producción

1. Tras 14 días + 12 testers → aparece la opción en Play Console
2. **Production → Create new release** → sube el mismo AAB (o uno más reciente)
3. Completa la ficha de tienda con screenshots finales
4. **Submit for review** → Google tarda 1-7 días en revisar

---

## Apéndice A — Textos para Google Play

### A.1 Short Description (EN) — 80 chars

```
Discover, track, and organize your movie journey. Your personal film companion.
```

### A.1b Short Description (ES) — 80 chars

```
Descubre, marca y organiza tu viaje cinéfilo. Tu compañero personal de cine.
```

### A.2 Full Description (EN) — Google Play

```
Santoro is your personal movie companion — discover new films, track what you've watched, and build your perfect watchlist.

🔍 DISCOVER
Browse trending, popular, top rated, and upcoming movies. Filter by genre. Explore full movie details including cast, crew, tagline, and ratings.

✅ TRACK WHAT YOU WATCH
Mark movies as Watched and see your stats: total movies, hours watched, streaks, and more in a beautiful stats dashboard.

📌 BUILD YOUR WATCHLIST
Save movies for later with one tap. Swipe to remove when you're done. Your watchlist, your rules.

🔎 SMART SEARCH
Find any movie instantly with real-time search. Get suggestions from your recent searches and what's trending right now.

☁️ SYNC ACROSS DEVICES
Sign in with Google to sync your Watched and Watchlist across all your Android devices. Or use it as a guest — your data stays on your device.

📴 WORKS OFFLINE
Browse cached movies and manage your lists even without internet. Changes sync automatically when you're back online.

🎨 YOUR STYLE
Choose between light, dark, or system theme. Available in English and Spanish.

Santoro uses data from The Movie Database (TMDB) but is not endorsed or certified by TMDB.
```

### A.2b Full Description (ES) — Google Play

```
Santoro es tu compañero personal de cine — descubre nuevas películas, lleva el control de lo que has visto y crea tu watchlist perfecta.

🔍 DESCUBRE
Explora películas en tendencia, populares, mejor valoradas y próximos estrenos. Filtra por género. Consulta el detalle completo: reparto, equipo técnico, tagline y valoraciones.

✅ CONTROLA LO QUE VES
Marca películas como vistas y consulta tus estadísticas: total de películas, horas vistas, rachas y más en un panel de estadísticas.

📌 CREA TU WATCHLIST
Guarda películas para ver más tarde con un solo toque. Desliza para eliminar cuando quieras. Tu lista, tus reglas.

🔎 BÚSQUEDA INTELIGENTE
Encuentra cualquier película al instante con búsqueda en tiempo real. Recibe sugerencias de tus búsquedas recientes y de lo que es tendencia ahora.

☁️ SINCRONIZACIÓN EN LA NUBE
Inicia sesión con Google para sincronizar tus listas de Vistos y Watchlist en todos tus dispositivos Android. O úsala como invitado — tus datos se quedan en tu dispositivo.

📴 FUNCIONA SIN CONEXIÓN
Navega por películas en caché y gestiona tus listas incluso sin internet. Los cambios se sincronizan automáticamente cuando vuelvas a estar online.

🎨 A TU ESTILO
Elige entre tema claro, oscuro o del sistema. Disponible en inglés y español.

Santoro utiliza datos de The Movie Database (TMDB) pero no está avalada ni certificada por TMDB.
```

### A.3 Categoría y tags

| Campo | Valor |
|-------|-------|
| Category | Entertainment |
| Tags sugeridos | movies, watchlist, movie tracker, film, cinema |


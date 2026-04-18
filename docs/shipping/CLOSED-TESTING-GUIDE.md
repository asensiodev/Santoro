# Guía — Prueba Cerrada (Closed Testing)

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field      | Value                                              |
|------------|----------------------------------------------------|
| **Fecha**  | 2026-04-18                                         |
| **AAB**    | `app/build/outputs/bundle/release/app-release.aab` |
| **Versión**| 1.0.14 (versionCode 19)                             |

---

## Qué es Closed Testing

Prueba más amplia que **desbloquea el acceso a producción**. Google exige:

1. La pista de prueba cerrada debe estar **activa ≥ 14 días** con un AAB publicado.
2. Al menos **12 testers** deben haber aceptado la invitación (opt-in).
3. **Ambas condiciones deben cumplirse a la vez** para poder solicitar producción.

> Ejemplo: si subes el AAB hoy (día 0) y consigues 12 opt-ins en el día 3, sigues esperando hasta el día 14. Si llegas al día 14 con solo 10 opt-ins, esperas hasta tener 12.

---

## Requisito previo

- [x] Haber completado el smoke test en Internal Testing (`INTERNAL-TESTING-GUIDE.md`)

---

## Paso 1 — Completar App Content (obligatorio)

Estos formularios se completan en **Play Console → App content**:

**Privacy Policy:**
1. Play Console → **App content → Privacy policy**
2. URL: `https://asensiodev.github.io/Santoro/legal/PRIVACY-POLICY`

- [x] Privacy Policy configurada en Play Console

**App access:**
1. Play Console → **App content → App access**
2. Selecciona: **All functionality is available without special access**
   - (La app permite "Continue as Guest", no requiere credenciales especiales)

- [x] App access configurado

**Ads:**
1. Play Console → **App content → Ads**
2. Selecciona: **No, my app does not contain ads**

- [x] Declaración de anuncios completada

**Content rating:**
1. Play Console → **App content → Content rating**
2. Inicia el cuestionario IARC
3. Respuestas clave:
   - ¿Contenido generado por usuarios? **No**
   - ¿Contiene violencia/sangre/etc.? **No** a todo
   - ¿Compras in-app? **No**
   - ¿Publicidad? **No**
4. Resultado esperado: **PEGI 3 / Everyone**

- [x] Content rating completado

**Target audience:**
1. Play Console → **App content → Target audience and content**
2. Público objetivo: **18 y más** (no dirigida a niños)
3. ¿Contiene anuncios? **No**

- [ ] Target audience configurado

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

- [ ] Data safety completado

**User-generated content (Contenido generado por usuarios):**
1. Play Console → **App content → User-generated content**
2. ¿Tu app permite que los usuarios generen o compartan contenido visible para otros? **No**
   - (Los usuarios solo guardan películas en listas privadas, no publican ni comparten contenido con otros usuarios)

- [ ] User-generated content completado

**Contenido en línea (Online content):**
1. Play Console → **App content → Contenido en línea**
2. ¿La aplicación incluye contenido que no forma parte de la descarga inicial pero al que se puede acceder desde la app? → **Sí**
   - (Santoro carga datos de películas, posters, sinopsis, reparto, etc. desde la API de TMDB en tiempo real)

- [ ] Contenido en línea completado

---

## Paso 2 — Configurar la ficha de tienda (Store Listing)

### 2.1 Información principal

| Campo | Valor |
|-------|-------|
| **App name** | Santoro |
| **Short description** (≤80 chars) | Ver textos en §Apéndice A |
| **Full description** (≤4000 chars) | Ver textos en §Apéndice A |
| **Category** | Entertainment |

- [ ] App name configurado
- [ ] Short description (EN + ES)
- [ ] Full description (EN + ES)
- [ ] Categoría seleccionada

### 2.2 Gráficos

| Asset | Requisito |
|-------|-----------|
| **App icon** | 512×512 PNG — ya existe: `core/design-system/src/main/ic_launcher-playstore.png` |
| **Feature graphic** | 1024×500 PNG — haz uno en Figma/Canva con el logo + nombre + fondo oscuro |
| **Screenshots** | Mínimo 2 para teléfono. Capturas de: Browse, Movie Detail, Watchlist, Watched Stats |

- [ ] App icon subido (512×512)
- [ ] Feature graphic creado y subido (1024×500)
- [ ] Screenshots de teléfono subidos (mínimo 2)

---

## Paso 3 — Crear la release de Closed Testing

> ⚠️ **Sube el AAB AHORA**, aunque tengas 0 testers. El reloj de 14 días empieza cuando publicas la release.

1. Play Console → **Testing → Closed testing**
2. Pulsa **Create track** (si no existe) → nombre: "Beta"
3. **Create new release** → sube el AAB
4. **Release name:** `1.0.14 (19)`
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

- [ ] Track de Closed Testing creado
- [ ] AAB subido
- [ ] Release notes añadidas (EN + ES)
- [ ] Release publicada (rollout iniciado)
- [ ] 📅 Fecha de publicación: ____/__/____ (apúntala — los 14 días cuentan desde aquí)

---

## Paso 4 — Invitar testers

### 4.1 Cómo funciona la invitación (paso a paso)

Play Console **NO envía emails automáticamente**. El flujo completo es:

```
TÚ consigues emails → los añades a Play Console → Play Console genera UN link de opt-in
→ TÚ envías ese link a cada tester → el tester abre el link → acepta → instala
```

En detalle:

1. **Consigue emails de Gmail** por cualquier canal (WhatsApp, Discord, web, en persona...)
2. **Añade los emails a Play Console:**
   - Play Console → Closed testing → pestaña **Testers**
   - **Create email list** → nombre: "Santoro Beta Testers"
   - Pega los emails (uno por línea). Puedes volver a editar la lista para añadir más en cualquier momento.
   - **Save changes**
3. **Copia el link de opt-in** que aparece en la parte inferior de la sección Testers
4. **Envía el link a cada tester** por el canal que prefieras:
   - A amigos/familia → WhatsApp o Telegram directo
   - A gente de Discord → DM con el link
   - A gente de la web → email con el link
5. **El tester:**
   - Abre el link en su móvil Android
   - Pulsa "Aceptar" / "Become a tester"
   - Espera 2-5 minutos
   - Pulsa el enlace de instalación que aparece
   - Instala y abre la app

> 💡 **El link de opt-in es el mismo para todos los testers.** No necesitas un link diferente por persona. Pero el tester SOLO puede acceder si su email está en la lista de Play Console.

> ⚠️ El tester debe abrir el link con la **misma cuenta de Google** que tú añadiste a la lista. Si usa otra cuenta, no le aparecerá la opción de instalar.

- [ ] Lista de emails creada en Play Console
- [ ] Link de opt-in copiado

### 4.2 Estrategia de captación — llegar a 12 rápido

Combina varios canales en paralelo:

| Canal | Cómo captar emails | Emails esperados |
|-------|-------------------|-----------------|
| **Amigos y familia** | WhatsApp/Telegram: "¿me das tu Gmail para probar mi app?" → cuando te lo dan, lo añades a Play Console y le mandas el link de opt-in | 4-6 |
| **Discord** | Publica el post de §4.4 en canales de beta testing o dev Android. Pide que te manden su Gmail por DM. Los añades a Play Console y les mandas el link. | 4-8 |
| **Web / Google Forms** | Crea un formulario pidiendo el Gmail (ver §4.5). Revisa respuestas, añade a Play Console, manda link. | 2-5 |
| **Redes sociales** | Post en Twitter/X, LinkedIn, Reddit (r/androiddev, r/playmyapp). Misma dinámica: pedir Gmail → añadir → mandar link. | 2-4 |

**Resumen del flujo para CADA tester, venga del canal que venga:**

```
1. Le pides su Gmail (por WhatsApp, DM de Discord, formulario web, etc.)
2. Añades su Gmail a la lista de testers en Play Console
3. Le mandas el link de opt-in (por el mismo canal donde te dio el email)
4. El tester acepta e instala
```

- [ ] Mensajes enviados a amigos/familia
- [ ] Post publicado en Discord
- [ ] Google Form creado (opcional)
- [ ] Post en redes sociales (opcional)

### 4.3 Mensaje para pedir el email y luego enviar el link

**Hay DOS momentos de contacto:**

**MOMENTO 1 — Pedir el email (cuando aún no lo tienes):**

Español:
```
¡Hola! 👋

Estoy lanzando "Santoro", una app Android para descubrir películas, crear tu watchlist
y llevar el control de todo lo que ves. Necesito testers para la fase de prueba cerrada
en Google Play.

¿Me puedes echar una mano? Solo necesito tu email de Gmail y 2 minutos de tu tiempo
para instalar la app en tu móvil Android. Te mando las instrucciones en cuanto
te agregue.

¡Gracias! 🙏
```

English:
```
Hi! 👋

I'm launching "Santoro", an Android app to discover movies, build your watchlist,
and track everything you watch. I need testers for the closed testing phase on Google Play.

Can you help me out? I just need your Gmail address and 2 minutes of your time
to install the app on your Android phone. I'll send you the instructions as soon as
I add you.

Thanks! 🙏
```

**MOMENTO 2 — Enviar el link de opt-in (cuando ya lo has añadido a Play Console):**

Español:
```
¡Ya te he agregado! Aquí tienes los pasos:

1. Abre este enlace desde tu móvil Android: [PEGAR OPT-IN LINK]
2. Pulsa "Aceptar" para unirte a la prueba
3. Espera 2-5 minutos y pulsa el enlace de instalación
4. Instala la app y ábrela

La app permite iniciar sesión con Google o usarla como invitado (sin registro).
Disponible en español e inglés, con tema claro y oscuro.

Si encuentras algún problema o tienes sugerencias, escríbeme.
¡Gracias por la ayuda! 🙏
```

English:
```
You're in! Here are the steps:

1. Open this link on your Android phone: [PASTE OPT-IN LINK]
2. Tap "Accept" to join the test
3. Wait 2-5 minutes, then tap the install link
4. Install the app and open it

The app supports Google Sign-In or guest mode (no registration needed).
Available in English and Spanish, with light and dark themes.

If you find any issues or have suggestions, let me know.
Thanks for the help! 🙏
```

### 4.4 Post para Discord / comunidades online

Para servidores de Discord (#beta-testing, #app-showcase, #promote-your-app) o Reddit (r/androiddev, r/playmyapp):

**English (Discord/Reddit):**
```
🎬 Looking for 12 Android testers — movie companion app "Santoro"

Hey everyone! I'm an indie Android dev and I need testers for my app's
closed testing phase on Google Play (requirement to go to production).

What is Santoro?
• Browse trending, popular, and upcoming movies (powered by TMDB)
• Save movies to your Watchlist
• Mark movies as Watched with stats dashboard
• Cloud sync with Google Sign-In (or use as guest)
• Offline support, dark/light themes, English & Spanish

What I need from you:
1. Drop your Gmail in the comments (or DM me)
2. I'll add you and send you an opt-in link
3. Accept + install on your Android phone (~2 min)

Feedback is welcome but not required. Thanks! 🙏
```

**Español (Discord):**
```
🎬 Busco 12 testers Android — app de cine "Santoro"

¡Hola! Soy desarrollador Android indie y necesito testers para la fase de prueba
cerrada en Google Play (requisito para publicar en producción).

¿Qué es Santoro?
• Explora películas en tendencia, populares y próximos estrenos (datos de TMDB)
• Guarda películas en tu Watchlist
• Marca películas como vistas con panel de estadísticas
• Sincronización con Google Sign-In (o modo invitado)
• Soporte offline, tema claro/oscuro, inglés y español

¿Qué necesito?
1. Déjame tu Gmail en los comentarios (o por DM)
2. Te agrego y te mando un enlace de opt-in
3. Aceptas e instalas en tu móvil Android (~2 min)

Se agradece feedback pero no es obligatorio. ¡Gracias! 🙏
```

### 4.5 Web para recoger emails (opcional)

**Opción A — Google Forms (0 min de desarrollo):**
1. Crea un Google Form con un solo campo: "Tu email de Gmail"
2. Comparte el enlace del formulario en Discord, redes, etc.
3. Revisa las respuestas → añade los emails a la lista de Play Console → manda el link de opt-in

**Opción B — Landing sencilla (si tienes GitHub Pages u hosting):**
1. Página estática con: nombre de la app, 1-2 screenshots, descripción breve, y un botón que enlace al Google Form
2. Dominio sugerido: `santoro.asensiodev.com` o `asensiodev.github.io/santoro-beta`

### 4.6 Seguimiento de testers

Lleva un control simple (Google Sheets o Notion):

| # | Email | Canal | Añadido a Play Console | Opt-in aceptado | Instalado |
|---|-------|-------|------------------------|-----------------|-----------|
| 1 | amigo1@gmail.com | WhatsApp | ✅ | ✅ | ✅ |
| 2 | discord_user@gmail.com | Discord | ✅ | ⏳ | — |
| ... | | | | | |

> ⚠️ **NO uses Google Groups.** La sincronización es lenta, a veces los miembros no se detectan como testers, y si el grupo es público puede unirse gente no deseada. La **lista de emails de Play Console** es más fiable y puedes editarla en cualquier momento.

### 4.7 Si un tester no puede instalar

- Verificar que aceptó la invitación con la **misma cuenta Gmail** que usa en Play Store
- Esperar 5-10 minutos después de aceptar
- La app **no aparece buscando en Play Store** → deben usar el link de instalación que aparece tras aceptar el opt-in
- Debe ser un **dispositivo Android real**, no emulador

---

## Paso 5 — Esperar 14 días y verificar

| Hito | Fecha estimada | Estado |
|------|----------------|--------|
| Subir AAB a Closed Testing | **Hoy** (cuanto antes) | [ ] |
| 12+ testers opt-in | 1-4 días después | [ ] |
| 14 días completados | **+14 días desde la subida** | [ ] |
| Solicitar acceso a producción | Ese mismo día | [ ] |

**Durante la espera:**
- [ ] Monitorizar **Crashlytics** para crashes
- [ ] Revisar el **Pre-launch report** en Play Console
- [ ] Preparar los **screenshots** finales para la ficha de producción
- [ ] Subir nuevos AABs si se encuentran bugs

Cuando se cumplan ambas condiciones (14 días + 12 opt-ins) → pasa a producción (`PRODUCTION-GUIDE.md`).

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


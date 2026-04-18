# Guía — Prueba Interna (Internal Testing)

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field      | Value                                              |
|------------|----------------------------------------------------|
| **Fecha**  | 2026-04-18                                         |
| **AAB**    | `app/build/outputs/bundle/release/app-release.aab` |
| **Versión**| 1.0.14 (versionCode 19)                             |
| **Estado** | ✅ Completada                                       |

---

## Qué es Internal Testing

Prueba privada para ti y tu equipo (1-100 testers). No requiere revisión de Google, la build está disponible en minutos. No tiene requisito de 14 días ni mínimo de testers.

**Objetivo:** Smoke test en dispositivo real antes de pasar a prueba cerrada.

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
5. **Release name:** `1.0.14 (19)`
6. **Release notes:**

```
Internal smoke test — v1.0.14
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

Repite hasta que el smoke test esté limpio. Cuando esté OK → pasa a **Closed Testing** (`CLOSED-TESTING-GUIDE.md`).

# Repetición de pruebas: cuentas y onboarding

Fecha: 2026-09-14. Referencia: [FIP-023](../plan/FIP-023-single-active-account-isolation.md) y [FIP-022](../plan/FIP-022-client-account-deletion.md).

El usuario confirmó que las pruebas manuales realizadas funcionaron. No se registró el detalle de cada escenario ni del artefacto utilizado. Las casillas siguientes se dejan pendientes para repetirlas y registrar resultados en la próxima sesión; no indican fallos conocidos.

## Preparación

- Emulador con Google Play Services y conexión a Internet. Se utilizó Pixel_9a API 37 durante la validación automatizada.
- Dos cuentas de prueba A y B con películas diferentes. Para vinculación sin colisión, usar una cuenta Google que todavía no tenga una cuenta en Santoro.
- Para eliminar cuentas, usar una cuenta desechable: la operación elimina datos reales en Firebase.
- Ejecutar el build `debug` desde Android Studio. Paquete actual: `com.asensiodev.santoro.debug`.
- No borrar datos entre los pasos de persistencia o cambio de cuenta. No confundir Debug con la versión instalada desde Play ni con los tests que usan repositorios fake.

Para detener y volver a abrir Debug sin borrar datos:

```bash
adb shell am force-stop com.asensiodev.santoro.debug
adb shell am start -n com.asensiodev.santoro.debug/com.asensiodev.santoro.MainActivity
```

Si hay varios dispositivos conectados, añadir `-s <serial>` después de `adb`.

## 1. Confirmación antes de entrar como invitado

- [ ] Desde Login, comprobar que el aviso no está impreso en la landing.
- [ ] Pulsar «Continuar como invitado»: aparece el diálogo y todavía no se inicia sesión.
- [ ] Cancelar o pulsar Back: seguir en Login.
- [ ] Volver a pulsar y confirmar: entrar como invitado.
- [ ] Comprobar legibilidad en español e inglés y con tamaño de letra aumentado.

El aviso debe distinguir vinculación que conserva listas de cambio a otra cuenta existente que no las fusiona.

## 2. Persistencia de los dos onboardings

- [ ] Descartar la bienvenida al invitado, detener y abrir Debug: no debe reaparecer.
- [ ] Abrir Movie Detail. Si aparece la explicación de los botones, pulsar «Entendido».
- [ ] Volver a la misma película: no debe reaparecer.
- [ ] Abrir una película diferente: no debe reaparecer.
- [ ] Detener y abrir Debug, entrar de nuevo en Movie Detail: no debe reaparecer.

Son dos preferencias independientes. Back en Movie Detail no marca el tooltip como visto; borrar datos o desinstalar puede reiniciar ambas preferencias. El agente comprobó el descarte explícito del tooltip y su persistencia tras reiniciar Debug sin reproducir el fallo reportado.

## 3. Vinculación de invitado sin colisión

- [ ] Crear una lista como invitado con una película vista y otra en Watchlist.
- [ ] Vincular una cuenta Google que no tenga otra cuenta en Santoro.
- [ ] Comprobar que ambas películas se conservan y aparece la confirmación de vinculación.
- [ ] Verificar que Profile deja de mostrar al usuario como invitado y habilita las acciones de cuenta registrada.
- [ ] Reiniciar la app y comprobar de nuevo perfil y películas.
- [ ] Si se inspecciona Firebase, comprobar que el UID se conserva y que las películas pertenecen a ese UID.

Esta comprobación también cubre la actualización del estado observado del perfil, que no queda demostrada solo por el resultado exitoso de la llamada de vinculación.

## 4. Colisión con una cuenta Google existente

- [ ] Desde una sesión de invitado con listas identificables, intentar vincular Google a una cuenta ya existente en Santoro.
- [ ] Leer el aviso: explica pérdida de las listas del invitado, ausencia de fusión y posterior inicio de sesión.
- [ ] Cancelar: conservar sesión y películas del invitado.
- [ ] Repetir y confirmar: volver a Login.
- [ ] Iniciar sesión explícitamente con Google: mostrar sus listas, sin incorporar las del invitado.

## 5. Logout A → Login → B

- [ ] Entrar con A, añadir películas identificables y confirmar que llegaron a Firestore antes del logout.
- [ ] Cerrar sesión: ver Login. Back no debe recuperar el contenido autenticado de A.
- [ ] Entrar con B: no ver las películas exclusivas de A en Watched, Watchlist ni indicadores de búsqueda.
- [ ] Añadir una película exclusiva de B y esperar su sincronización.
- [ ] Volver a entrar con A: recuperar sus listas, sin la película exclusiva de B.
- [ ] Comprobar en Firestore que no se mezclaron las películas de los dos UIDs.

Una modificación local muy reciente que todavía no se haya subido puede perderse al cerrar sesión: es una limitación aceptada. El cambio directo entre dos UIDs autenticados no es una función soportada; el flujo de usuario pasa por logout y Login.

## 6. Eliminación de cuenta y bloqueo visual

- [ ] Con una cuenta desechable, iniciar «Eliminar cuenta» y cancelar la selección de credencial: la sesión y las listas deben seguir disponibles.
- [ ] Repetir, confirmar la credencial y completar el borrado.
- [ ] Durante el trabajo remoto, comprobar fondo opaco e indicador de carga: no debe verse el perfil por debajo.
- [ ] Back, toques y navegación por teclado no deben permitir interactuar con el contenido bloqueado.
- [ ] Al terminar, comprobar Login, ausencia del usuario en Firebase Auth y eliminación de sus documentos Firestore.
- [ ] Entrar con otra cuenta: no ver películas de la cuenta eliminada.

La recuperación del marcador local y sus fallos tienen tests automatizados. Repetir una interrupción controlada antes de limpiar Room requiere un entorno de prueba preparado o un breakpoint; no considerar un cierre al azar como prueba de esa ventana concreta.

## 7. Conservación de datos sin cambio de cuenta

- [ ] Con películas guardadas y una sesión activa, detener y abrir Debug: conservar sesión y listas.
- [ ] Repetir sin conexión: las listas locales siguen disponibles.
- [ ] Recuperar conexión: la app sigue operativa y sincroniza con la misma cuenta.

## Validaciones externas antes de publicar una release

- [ ] Revisar que CI del commit publicado termine correctamente, incluida instrumentación API 35.
- [ ] Probar upgrade desde el artefacto Internal anterior al nuevo, manteniendo cuenta y datos; usar artefactos compatibles en paquete y firma.
- [ ] Verificar que la misma cuenta conserva sus listas después del upgrade.
- [ ] Repetir login, logout y navegación con el artefacto firmado distribuido por Play Internal.
- [ ] Registrar identificador de build/versión, dispositivo, API, idioma y resultado antes de promover a Production.

Un commit/push no implica que esas comprobaciones externas estén realizadas.

## Registro de la repetición

| Dato | Resultado |
|---|---|
| Fecha | Pendiente |
| Commit / versión / variante | Pendiente |
| Dispositivo y API | Pendiente |
| Escenarios completados | Pendiente |
| Escenarios omitidos y motivo | Pendiente |
| Incidencias y pasos de reproducción | Pendiente |

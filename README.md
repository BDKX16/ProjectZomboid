# ProjectZomboid PRD — Cultivo regenerativo

Mod de Fabric para Minecraft 26.3 que simula el cultivo regenerativo: estaciones, clima según el bioma,
invernaderos, salud del suelo, policultivos, comidas con efectos y un manual con la ruta de cultivo.

## Desarrollo

Requisitos: JDK 25 (Gradle lo busca solo; ver `gradle/gradle-daemon-jvm.properties`).

| Comando | Qué hace |
|---|---|
| `./gradlew runClient` | Abre Minecraft con el mod |
| `./gradlew runServer` | Servidor dedicado de prueba |
| `./gradlew build` | Compila, corre los tests y genera el jar en `build/libs/` |
| `./gradlew test` | Solo los tests unitarios |
| `./gradlew runDatagen` | Regenera recursos (modelos, recetas, idiomas) |

## Fases

### Fase 1 — Estaciones y clima ✅

**Calendario (hemisferio sur).** La fecha sale del reloj del mundo, así que `/time`, dormir, etc. la mueven.

- El año empieza el **1 de septiembre** en **primavera**.
  Primavera = sep-nov, verano = dic-feb, otoño = mar-may, invierno = jun-ago.
- Cada estación dura **15 días de Minecraft** por defecto (año = 60 días ≈ 20 h de juego; meses de 5 días).
- Los días cambian a medianoche (00:00).

**Clima.** Temperatura en °C, calculada a partir de:

- La temperatura base del bioma (llanura ≈ 15 °C de media anual, desierto ≈ 25 °C, nevado ≈ 1 °C).
- Una curva estacional de ±9 °C (pico a mitad del verano, mínimo a mitad del invierno).
- Una variación diaria aleatoria pero **fija para cada mundo**: el pronóstico siempre se cumple.
- La altura: −0,6 °C cada 10 bloques por encima de y=70.
- El ciclo diario: más calor a las 15 h, más frío de madrugada.

Rangos: **Helada** (< 0 °C), **Frío** (0-10), **Templado** (10-24), **Cálido** (24-32), **Calor extremo** (> 32).

**Comandos**

| Comando | Qué hace |
|---|---|
| `/season` | Estación, día, fecha, hora y temperatura donde estás |
| `/season forecast [días]` | Pronóstico (mín/máx, rango, aviso de helada); 7 días por defecto, hasta 30 |
| `/season set <spring\|summer\|autumn\|winter>` | Avanza al inicio de esa estación (solo operadores) |
| `/gamerule projectzomboid_prd:season_length <días>` | Cambia la duración de cada estación (1-120) |

**HUD.** Arriba a la izquierda: estación y día, fecha y hora, temperatura local. Se oculta con F1 y con F3.

**Código:** `season/` (`SeasonCalendar`, `ClimateModel`, `SeasonManager`), `command/SeasonCommand`,
`client/hud/SeasonHud`. Tests en `src/test/.../season/`.

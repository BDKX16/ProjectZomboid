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
| `./gradlew runGameTest` | Tests en un servidor real sin ventana (registros, datos, mixins) |
| `./gradlew runClientGameTest` | Abre el juego, crea un mundo y guarda capturas en `build/run/clientGameTest/screenshots/` |
| `./gradlew runDatagen` | Regenera modelos, loot tables, recetas y features en `src/main/generated/` |

Los gametests aceptan automáticamente el EULA de Minecraft solo para esa ejecución de prueba (`eula = true` en
`build.gradle`).

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

### Fase 2 — Cultivos que dependen de la estación ✅

Cada cultivo tiene un **perfil** que dice en qué estaciones crece y qué temperaturas tolera. Por ahora aplica a
los cultivos vanilla; los del mod (fase 3) usan el mismo sistema.

| Cultivo | Familia | Estaciones | Temp. (mín / óptima / máx) | Tolera heladas |
|---|---|---|---|---|
| Trigo | Gramíneas | otoño, invierno, primavera | 0 / 10-22 / 30 °C | sí |
| Papa | Solanáceas | primavera, verano | 6 / 15-24 / 32 °C | no |
| Zanahoria | Umbelíferas | otoño, invierno, primavera | 3 / 12-22 / 28 °C | sí |
| Remolacha | Amarantáceas | otoño, primavera | 4 / 12-24 / 30 °C | sí |

**Cómo afecta al crecimiento** (en cada *random tick* del cultivo):

- **Fuera de estación:** no crece.
- **Temperatura media del día:** crece al 100 % dentro del rango óptimo, cada vez más lento hacia el mínimo o el
  máximo, y nada fuera de ellos.
- **Helada** (temperatura actual < 0 °C) en plantas que no la toleran: la planta retrocede una etapa
  (25 % por tick) y, si ya estaba recién plantada, muere.
- **Calor extremo** (4 °C por encima de su máximo): se marchita igual que con la helada.
- **Polvo de hueso:** no hace nada si la planta no puede crecer en ese momento (se gasta igual).

**Comando de diagnóstico:** `/season crop <x y z>` muestra a qué porcentaje de la velocidad normal está creciendo
el cultivo en esa posición y por qué (fuera de estación, frío, helada…).

**Perfiles en JSON (modificables por datapack).** Están en
`data/<namespace>/projectzomboid_prd/crop_profile/<nombre>.json`:

```json
{
  "block": "minecraft:wheat",
  "family": "grasses",
  "seasons": ["autumn", "winter", "spring"],
  "temperature": { "min": 0, "optimal_min": 10, "optimal_max": 22, "max": 30 },
  "frost_tolerant": true,
  "growth_rate": 1.0,
  "regrow_age": 5
}
```

`growth_rate` (0-1) hace más lentos los cultivos de ciclo largo. `regrow_age` (opcional, fase 4) permite cosechar
con tijeras de poda. Familias: `grasses`, `nightshades`, `brassicas`,
`legumes`, `umbellifers`, `amaranths`, `composites`, `cucurbits`, `alliums`, `mints` (se usan para la rotación de la fase 6).

**Código:** `crop/` (`CropProfile`, `CropProfiles`, `GrowthCalculator`, `CropClimate`) y `mixin/CropBlockMixin`.

### Fase 3 — Cultivos nuevos y plantas silvestres ✅

**8 cultivos nuevos.** Cada uno tiene semillas, producto cosechado y una versión silvestre.

| Cultivo | Producto | Familia | Estaciones | Temp. (mín / óptima / máx) | Helada | Velocidad | Silvestre en |
|---|---|---|---|---|---|---|---|
| Lechuga | Lechuga (comida) | Compuestas | otoño, invierno, primavera | 2 / 10-20 / 26 °C | tolera | 100 % | bosques |
| Rúcula | Rúcula (comida) | Brásicas | otoño, invierno, primavera | 0 / 8-20 / 27 °C | tolera | 100 % | llanuras, praderas |
| Brócoli | Brócoli (comida) | Brásicas | otoño, invierno | 2 / 10-20 / 26 °C | tolera | 60 % | taigas |
| Tomate | Tomate (comida) | Solanáceas | primavera, verano | 10 / 18-28 / 35 °C | no | 60 % | sabanas, junglas |
| Soja | Porotos de soja (comida) | Leguminosas | primavera, verano | 10 / 20-30 / 36 °C | no | 70 % | llanuras, sabanas |
| Girasol | Cabeza de girasol | Compuestas | primavera, verano | 8 / 18-30 / 38 °C | no | 70 % | llanuras (y de girasoles) |
| Albahaca | Albahaca (comida) | Lamiáceas | primavera, verano | 12 / 20-30 / 36 °C | no | 80 % | junglas |
| Zapallo | Zapallo (comida) | Cucurbitáceas | primavera, verano | 12 / 20-30 / 36 °C | no | 50 % | junglas, bosques |

La regla general: los de **estación fría** (lechuga, rúcula, brócoli) se siembran en otoño-invierno, y los de
**estación cálida** (tomate, soja, girasol, albahaca, zapallo) en primavera-verano. En verano la lechuga no solo deja
de crecer: en las tardes calurosas se marchita.

**Cómo se consiguen las semillas**

- **Plantas silvestres:** aparecen solas al generar el mundo, en parches, según el bioma de la tabla. Al romperlas
  dan semillas. No se pueden colocar a mano (no tienen ítem).
- **Cosechando:** un cultivo maduro da el producto y semillas (Fortuna aumenta las semillas); uno sin madurar
  devuelve la semilla.
- **Crafteo:** 1 producto → semillas (2 en general, 3 de tomate y zapallo, 4 de girasol).

**Solo en tierra preparada.** Las semillas únicamente se plantan en tierra arada (farmland), como el trigo.

**Pestaña creativa** "Cultivo regenerativo" con todas las semillas y productos.

Las texturas actuales son provisorias (pixel art generado); los modelos van en 4 etapas visuales para las 8 edades.

**Código:** `block/` (`SeasonalCropBlock`, `WildPlantBlock`), `registry/ModCrops`, `worldgen/ModWorldGen`,
datagen en `client/datagen/`, perfiles en `data/projectzomboid_prd/projectzomboid_prd/crop_profile/`.
Gametest en `src/gametest/` (perfiles cargados, features registradas, semillas solo en farmland y el mixin aplicado).

### Fase 4 — Invernaderos y salas de cultivo subterráneas ✅

El mod mira los bloques alrededor de cada cultivo para saber **dónde está creciendo**. `/season crop <x y z>`
lo muestra en la línea "Lugar".

**Invernadero.** Tiene que cumplir dos condiciones:
- **Techo de vidrio:** el primer bloque sobre el cultivo, hasta 12 bloques arriba, es vidrio o panel de vidrio
  (tag `projectzomboid_prd:greenhouse_glass`). Las luces de cultivo no cuentan como techo.
- **Paredes:** en las 4 direcciones hay un bloque sólido (pared, vidrio, puerta…) a 8 bloques o menos.

Efectos:
- Ignora las estaciones: solo importa la temperatura.
- Adentro hace **+8 °C** que afuera.
- Con una **fuente de calor** cerca (fogata, horno, ahumador, alto horno, magma o lava, a 4 bloques o menos) suma
  **+6 °C** más. Sirve para no perder cultivos sensibles a la helada en invierno.
- **Con extractor** (ver abajo) nunca pasa de **26 °C**.
- **Sin extractor** no hay tope de temperatura. Además, **en verano, fuera de los biomas nevados**, crece **a la
  mitad** ("ventilación insuficiente") y en las tardes de calor las plantas se pueden marchitar.
- **En desiertos y badlands no funciona:** se sobrecalienta (+8 °C sin tope), no protege de las estaciones y
  quema los cultivos. Tag de biomas: `projectzomboid_prd:greenhouse_overheats`.

**Sala de cultivo subterránea.** Es la forma de cultivar fuera de estación en el desierto. Necesita:
1. **Estar bajo tierra:** sin cielo arriba, y con un techo que no sea de vidrio.
2. **Luz de cultivo** a 2 bloques o menos hacia los costados y 1 a 5 bloques por encima del cultivo.
3. **Extractor de aire** a 8 bloques o menos con un **respiradero**: la columna sobre el extractor tiene que estar
   libre hasta la superficie. Puede ser aire, barrotes de hierro o trampillas (tag `projectzomboid_prd:vent_passable`).

Si cumple todo, mantiene **20 °C constantes** todo el año y en cualquier bioma. Sin un extractor que funcione el
aire se vicia y **no crece nada**.

**Bloques nuevos**

| Bloque | Receta | Uso |
|---|---|---|
| Luz de cultivo | `IRI` / `GLG` — I hierro, R redstone, G panel de vidrio, L piedra luminosa | Panel de LEDs **colgado del techo** con dos cables (modelo 3D). Solo se coloca debajo de un bloque y se cae si se lo sacan. Luz 15; habilita la sala de cultivo |
| Extractor de aire | `IBI` / `RCR` / `IBI` — I hierro, B barrotes de hierro, R redstone, C lingote de cobre | Ventila invernaderos y salas (necesita pico). Arriba tiene un **panel solar**: se regula solo, sin redstone ni energía. Por eso necesita la columna libre hasta el cielo. **El ventilador gira** (textura animada) cuando el respiradero está libre; lo revisa cada 2 segundos |

**Tijeras de poda** (`I I` / ` I ` / `S S` — I hierro, S palo; 238 usos). Algunas plantas se cosechan **sin
arrancarlas**. Con las tijeras sobre la planta madura cae el producto (1-2) y la planta vuelve a una etapa anterior
para producir otra vez:

| Planta | Vuelve a la etapa | Se cosecha con tijeras |
|---|---|---|
| Tomate, zapallo, brócoli (brotes laterales) | 5 de 7 | sí |
| Albahaca, lechuga, rúcula (corte y rebrote) | 4 de 7 | sí |
| Soja, girasol | — | no: se rompen para cosechar |

Si la planta no está madura o se cosecha una sola vez, las tijeras avisan en pantalla y no hacen nada. Romper la
planta a mano sigue dando la cosecha normal. Se configura en el perfil de cada cultivo con `"regrow_age"`
(si falta, la planta se cosecha una sola vez).

**Código:** `greenhouse/` (`Shelter`, `ShelterDetector`), `item/PruningShearsItem`, `registry/ModBlocks`, `registry/ModTags`, tags en
`data/projectzomboid_prd/tags/`. Gametest `ShelterGameTest`: arma un invernadero, le agrega un extractor, lo
convierte en sala subterránea y le tapa el respiradero, comprobando el resultado en cada paso.
`PruningShearsGameTest`: cosecha un tomate con tijeras (rebrota) y comprueba que la soja no se puede.

### Fase 5 — Estación climática ✅

**Bloque "Estación climática"** (receta `CKC` / `GRG` / `III`: C lingote de cobre, K reloj, G panel de vidrio,
R redstone, I hierro; se rompe con pico). Con click derecho abre una pantalla con los datos **del lugar donde
está puesta** (bioma y altura):

- **Hoy:** estación y día, fecha, hora, temperatura actual (grande, con el color del rango) y el nombre del bioma.
- **Pronóstico de 7 días:** mínima y máxima, una barra de temperatura de −15 a 40 °C con el color del rango y ❄
  los días con helada. Como el clima es determinístico, **el pronóstico siempre se cumple**.
- **Sembrar ahora al aire libre:** cultivos que están en su estación, con temperatura media de los próximos 7 días
  adecuada y sin heladas en el pronóstico si no las toleran. Pasando el mouse sobre un ícono se ve el nombre.
- **Preparar semillas para la próxima estación:** cultivos que todavía no van pero sí en la estación siguiente, y
  cuántos días faltan.

La pantalla se actualiza sola cada segundo y no pausa el juego. Los datos se calculan en el cliente con el clima y
los perfiles sincronizados: el servidor solo avisa que se abrió la estación.

**Código:** `block/WeatherStationBlock`, `crop/PlantingAdvisor` (lógica de recomendaciones, con tests),
`network/OpenWeatherStationPayload`, `client/screen/WeatherStationScreen`. El gametest de cliente
`ScreenshotClientGameTest` saca capturas del HUD y de la pantalla en invierno y en verano.

### Extra — Termohigrómetro, humedad, extractores y hongos ✅

**Humedad relativa.** Se calcula igual que la temperatura (determinística, por lugar y hora):
- Depende de cuánto llueve en el bioma: desierto ≈ 15-25 %, llanura ≈ 55 %, jungla/pantano ≈ 80 %.
- Es más alta en invierno y de madrugada, y más baja en verano y a la tarde, con una variación diaria.
- Dentro de un refugio, **las plantas evaporan agua** (ver capacidad de los extractores, abajo):
  - **Invernadero bien ventilado:** 45-75 %.
  - **Invernadero sin extractores suficientes:** hasta +25 % según cuántas plantas queden sin ventilar.
    Un invernadero chico con 2 o 3 plantas casi no se humedece; uno grande y cerrado, sí.
  - **Sala de cultivo:** de 60 % (bien ventilada) a 95 % (sin ventilación).

**Capacidad de los extractores.** Cada extractor ventila **20 plantas**. Se cuentan los cultivos y los extractores
que funcionan alrededor de cada planta: 8 bloques a los costados, 2 hacia abajo y 5 hacia arriba. Con menos
extractores de los necesarios, el lugar queda **parcialmente ventilado**:
- la humedad sube en proporción a las plantas sin cubrir;
- el tope de 26 °C del invernadero se aplica solo en parte;
- la penalización de verano va del 50 % (sin extractores) al 100 % (con todos los necesarios).

`/season crop` y el termohigrómetro muestran "extractores X/Y" (los que hay / los que hacen falta).

**Hongos.** En un **invernadero o sala de cultivo con más de 85 % de humedad**, cada cultivo tiene un 5 % de
chance por random tick de convertirse en un **cultivo con moho**:
- no da cosecha;
- **contagia** a los cultivos vecinos (30 % por tick, a uno de los 4 lados) mientras la humedad siga alta.

Al aire libre no hay hongos. La solución es poner más extractores. El aire seco no penaliza el crecimiento.

**Termohigrómetro** (`CGC` / `CRC` / `CKC` — C lingote de cobre, G panel de vidrio, R redstone, K brújula). Es un
dispositivo tipo celular. **Mientras lo tenés en la mano** (principal o secundaria) aparece una pantallita abajo a
la derecha, que se actualiza cada medio segundo, con los datos de donde estás parado:
- temperatura y humedad;
- qué tipo de lugar detecta (aire libre, invernadero con o sin extractor, sala de cultivo…);
- un diagnóstico:

| Consejo | Cuándo |
|---|---|
| Falta un extractor con respiradero | Sala subterránea con luces pero sin ventilación |
| El invernadero necesita extractor | Invernadero cerrado en verano (fuera de biomas nevados) |
| Extractor recomendado | Invernadero cerrado en otras estaciones (se acumula humedad) |
| Se sobrecalienta: usá una sala subterránea | Invernadero en desierto o badlands |
| Falta luz de cultivo | Bajo tierra sin luz de cultivo cerca |
| Helada / calor extremo | Menos de 0 °C / más de 32 °C |
| Faltan extractores (X plantas, hay Y, hacen falta Z) | Hay extractores pero no alcanzan para todas las plantas |
| Riesgo de hongos | Más de 85 % de humedad en un invernadero o sala |
| Aire muy seco | Menos de 30 % (solo informativo) |
| Condiciones OK | Nada que corregir |

**Código:** `greenhouse/MicroClimate` (medición), `greenhouse/ClimateAdvice` (diagnóstico, con tests),
`greenhouse/Mold` y `block/MoldyCropBlock` (hongos), `Shelter.Ventilation` (capacidad),
`ClimateModel.humidityAt`, `block/GrowLightBlock`, `block/ExtractorBlock`, `client/hud/ThermohygrometerHud`. El
modelo de la luz está hecho a mano en `assets/projectzomboid_prd/models/block/grow_light.json`. El `downfall` de los
biomas se lee gracias a `projectzomboid_prd.classtweaker`. Gametest `EquipmentGameTest`: la luz se cae sin techo, y
el ventilador se prende y se apaga según el respiradero. `VentilationGameTest`: 25 cultivos necesitan 2
extractores (con uno, 50 % de ventilación), y en una sala sin ventilación el tomate se cubre de moho. Las capturas del cliente incluyen una sala de cultivo con y
sin ventilación.

### Fase 6 — Suelo regenerativo ✅

Cada bloque de **tierra arada** guarda su propio estado. Se guarda con el mundo, dentro del chunk, y sigue ahí
aunque vuelvas a arar.

| Dato | Rango | Qué hace |
|---|---|---|
| Nitrógeno | 0-100 (empieza en 60) | Fertilidad: con 50 o más, crecimiento al 100 %; en 0, solo al 40 % |
| Materia orgánica | 0-100 (empieza en 30) | Hasta +15 % de crecimiento y libera nitrógeno con cada cosecha |
| Historial | últimas 3 familias | Para la rotación |
| Mulch | cosechas restantes | Protección contra heladas y aporte de materia orgánica |

**Cosechar cambia el suelo.** Pasa al romper un cultivo maduro o al cortarlo con las tijeras:
- se suma o se resta el nitrógeno del cultivo (`"nitrogen"` en su perfil);
- la materia orgánica libera 1 de nitrógeno cada 20 puntos y se descompone 2 puntos;
- la familia se anota en el historial.

| Cultivo | Nitrógeno por cosecha |
|---|---|
| **Soja** (leguminosa) | **+20** (fija nitrógeno del aire) |
| Albahaca, rúcula, lechuga, zanahoria | −4 a −6 |
| Remolacha, trigo, girasol | −8 a −10 |
| Tomate, papa | −12 |
| Brócoli, zapallo | −14 |

**Rotación.** Volver a plantar la misma familia donde se cosechó la última vez → **−30 %**. Tres cosechas
seguidas de la misma familia (monocultivo) → **−50 %**. La idea es alternar, por ejemplo tomate → soja → lechuga.

**Asociación de cultivos (policultivo).** Se miran los cultivos a 2 bloques o menos, al mismo nivel:
- **planta compañera** cerca → **+15 %**;
- **antagonista** cerca → **−25 %**;
- **3 especies distintas o más** (contando la propia) → **+10 %**.

| Cultivo | Compañeras | Antagonistas |
|---|---|---|
| Tomate | albahaca, zanahoria | papa, brócoli |
| Albahaca | tomate | — |
| Lechuga | zanahoria, rúcula, remolacha | — |
| Rúcula | lechuga | — |
| Brócoli | lechuga, remolacha | tomate |
| Soja | trigo, zapallo | — |
| Zapallo | soja, girasol | — |
| Girasol | zapallo | papa |
| Papa | — | tomate, girasol |
| Trigo | soja | — |
| Zanahoria | lechuga, tomate | — |
| Remolacha | lechuga, brócoli | — |

Todo esto está en los perfiles JSON (`"companions"`, `"antagonists"`, `"nitrogen"`), así que se puede cambiar con
datapacks.

**Bloques e ítems nuevos**

| | Receta | Uso |
|---|---|---|
| **Compostera** | `P P` / `PDP` / `PPP` — P tablas, D tierra | Se llena como el compostador (acepta los productos y semillas del mod); cuando está lista da **2 de compost** en vez de polvo de hueso. Las tolvas no pueden sacar polvo de hueso de ella |
| **Compost** | de la compostera | Click derecho en tierra arada (o en el cultivo): **+25 materia orgánica, +15 nitrógeno** |
| **Mulch de paja** | 3 trigo → 2 | Cubre la tierra arada por **4 cosechas**: los cultivos no sufren heladas de hasta −5 °C y cada cosecha suma +3 de materia orgánica |
| **Kit de análisis de suelo** | frasco + papel + redstone | Click derecho en tierra arada o en un cultivo: nitrógeno, materia orgánica, fertilidad, mulch, historial, rotación, vecinos y efecto total |

Los productos y semillas del mod también se pueden echar al **compostador vanilla**.

**Corrección:** durante las fases 4 y 5 las recetas de la **estación climática**, las **tijeras de poda** y el
**termohigrómetro** no se estaban generando (un error de codificación en un script de edición). Ya están, y el test
`RecipesTest` comprueba que exista la receta de cada bloque y herramienta.

**Código:** `soil/` (`SoilData`, `SoilRules` con tests, `SoilManager`), `block/CompostBinBlock`,
`item/SoilAmendmentItem`, `item/SoilTestKitItem`. Gametest `SoilGameTest`: las cosechas cambian el suelo, el
compost lo recupera, la soja fija nitrógeno, se detectan compañeras/antagonistas y la compostera da compost.

### Fase 7 — Comidas compuestas y dieta variada ✅

Las verduras se cocinan en comidas que dan efectos según lo que tienen. Todas se apilan de a 16, y las que van en
bowl o botella te los devuelven al terminar.

| Comida | Receta (sin forma) | Hambre | Efecto |
|---|---|---|---|
| **Hamburguesa** | pan + carne cocida + lechuga + tomate | **20 (barra llena)** | — |
| **Ensalada verde** | bowl + lechuga + rúcula + tomate | 6 | **Vitalidad**: +10 % de velocidad, 2 min |
| **Brócoli al vapor** | bowl + 2 brócoli + albahaca | 6 | Cura veneno, hambre y náuseas + **Inmunidad** contra ellos, 3 min |
| **Guiso de soja** | bowl + 2 soja + zanahoria + papa | 8 | Fuerza I, 2 min |
| **Sopa de zapallo** | bowl + zapallo + papa | 8 | **Calor**: la nieve polvo no te congela, 4 min |
| **Semillas de girasol tostadas** | flor de girasol en horno, ahumador o fogata | 3 | Absorción, 30 s (se comen rápido) |
| **Pasta al pesto** | bowl + 2 trigo + 2 albahaca + girasol | 8 | Prisa I, 3 min |
| **Jugo de tomate** | botella + 2 tomates | 4 | Regeneración I, 10 s |

**Efectos propios:** Vitalidad, Inmunidad, Calor y Bien nutrido, cada uno con su ícono.

**Dieta variada.** Comer **4 verduras distintas en un mismo día** de Minecraft te da **Bien nutrido**: +2
corazones de vida máxima durante un día.
- Cuentan las verduras del mod crudas y las de cada comida (la ensalada suma lechuga, rúcula y tomate).
- También cuentan las de vanilla: zanahoria, papa (también asada), remolacha (también en sopa) y zapallo (pastel
  de calabaza).
- Repetir la misma verdura no suma. Lo comido hace más de un día se olvida.
- Se guarda por jugador y se reinicia al morir.

**Código:** `effect/ModEffects`, `registry/ModFoods`, `food/Diet` (con tests) y `food/DietTracker`. Mixins nuevos:
`FoodPropertiesMixin` registra cada comida y `LivingEntityMixin` hace que no te congeles con Calor. El gametest
`FoodGameTest` comprueba que la hamburguesa llena el hambre, que la ensalada da +10 % de velocidad y devuelve el bowl,
que el brócoli cura y bloquea el veneno, que la sopa evita el congelamiento y que la dieta variada da +2 corazones.

### Fase 7.5 — Más cultivos, secado y curado ✅

**10 cultivos nuevos.** Cada uno tiene planta silvestre, semillas y perfil de clima:

| Cultivo | Estaciones | Familia | Notas | Silvestre en |
|---|---|---|---|---|
| Cebolla | otoño–primavera | aliáceas | resiste heladas | llanuras, prados |
| Ajo | otoño–invierno | aliáceas | se planta con dientes; resiste heladas | prados, colinas, cerezos |
| Cannabis | primavera–verano | cannabáceas | | sabanas, prados |
| Tabaco | primavera–verano | solanáceas | antagonista del tomate y la papa | selvas, sabanas |
| Café | primavera–otoño | rubiáceas | perenne (se cosecha con tijeras); muy sensible al frío | selvas |
| Ají | primavera–verano | solanáceas | perenne con tijeras | sabanas, badlands |
| Lúpulo | primavera–verano | cannabáceas | perenne con tijeras | bosques |
| Yerba mate | primavera–otoño | aquifoliáceas | perenne con tijeras | selvas, bosques |
| Maíz | primavera–verano | gramíneas | consume mucho nitrógeno (−16) | llanuras, sabanas |
| Poroto | primavera–verano | leguminosas | fija nitrógeno (+18) | llanuras, bosques |

**Las tres hermanas:** maíz, poroto y zapallo son compañeros entre sí (+15 % cada uno, más el bonus de policultivo).

**Secadero** (`SSS` / `H H` / `S S` — S palos, H hilo). Click derecho con la cosecha para colgarla (hasta 16).
Click con la mano vacía muestra cómo va (porcentaje, temperatura, humedad, velocidad y horas que faltan) y, cuando
está listo, la descuelga. Agachado + click la descuelga en cualquier momento.

El secado depende del aire donde está el secadero, el mismo que mide el termohigrómetro:
- **Humedad:** con 50 % seca a velocidad normal, con 30 % ×1,5, con 70 % ×0,5 y con 90 % no seca.
- **Temperatura:** debajo de 5 °C ×0,4, entre 5 y 15 °C ×0,7, más de 30 °C ×1,3.
- **Lluvia:** si le llueve, no seca.
- **Moho:** con humedad de 85 % o más (o lluvia) durante un día entero, la cosecha se arruina y sirve solo para
  compost. Un rato de aire seco hace que vuelva a empezar la cuenta.

En climas húmedos conviene secar bajo techo con extractores. El bloque muestra si está vacío, secando, seco o
arruinado.

| Fresco | Seco | Días |
|---|---|---|
| Ajo / cebolla | ajo / cebolla curados | 2 |
| Hoja de tabaco | tabaco seco | 2 |
| Cogollo de cannabis | cannabis seco | 1,5 |
| Cerezas de café | café verde | 3 |
| Ají | ají seco | 2 |
| Lúpulo | lúpulo seco | 1 |
| Hojas de yerba | yerba seca | 1 |
| Choclo | maíz seco | 2 |

**Frasco de curado** (` L ` / `V V` / `VVV` — L losa de madera, V vidrio). Lo seco se cura con el tiempo y gana
**calidad (★ a ★★★)**:
- Click con la mano vacía muestra el día y las estrellas.
- Agachado + click lo saca. Si todavía no llegó a ★, sale tal como entró.
- La calidad pasa a lo que se craftea con el producto (si se mezclan calidades, queda la menor).
- Cada estrella alarga los efectos: ★ ×1, ★★ ×1,5, ★★★ ×2.

| Entra | Sale | ★ / ★★ / ★★★ (días) |
|---|---|---|
| Tabaco seco | tabaco curado | 3 / 6 / 10 |
| Cannabis seco | cannabis curado | 2 / 4 / 7 |
| Yerba seca | yerba estacionada | 2 / 5 / 9 |
| Mosto | cerveza | 2 / 4 / 6 |

**Productos y comidas nuevas**

| Item | Receta | Efecto |
|---|---|---|
| Café tostado | café verde en horno, ahumador o fogata | — |
| Café | botella + 2 café tostado + balde de agua | Prisa I 3 min + Velocidad 30 s |
| Mate (calabaza) | zapallo + pepita de hierro | se devuelve al tomar |
| Mate | mate + yerba estacionada + balde de agua | Prisa I 2 min (según calidad) |
| Mosto | botella + lúpulo seco + 2 trigo | se fermenta en el frasco |
| Cerveza | del frasco | Absorción 2 min (según calidad) + mareo corto |
| Ají molido | ají seco → 2 | — |
| Chimichurri | botella + ajo curado + ají molido + albahaca | — |
| Asado con chimichurri | 2 carnes cocidas + chimichurri | 14 de hambre, Fuerza I 3 min + Regeneración |
| Locro | bowl + maíz seco + porotos + zapallo + ají molido + cebolla curada | 10 de hambre, Calor 6 min + Resistencia 2 min (cuenta 5 verduras para la dieta) |
| Choclo asado | choclo en horno, ahumador o fogata | 6 de hambre |
| Porro | cannabis curado + papel | **Relajado** 90 s (según calidad) |
| Brownie | cannabis curado + cacao + trigo + huevo + azúcar → 2 | **Relajado** 3 min (según calidad) |
| Cigarro | 2 tabaco curado | Prisa I 1 min (según calidad) |

**Relajado** (efecto nuevo):
- −15 % de velocidad, cura medio corazón cada 4 s y da hambre ("bajón").
- Mientras dura, un halo verde-violeta tiñe los bordes de la pantalla.

**Corrección del mulch (pedido en el juego):**
- **Se ve:** la tierra con mulch pasa a ser un bloque de **tierra arada con paja**. Cuando se gastan las 4 cosechas
  vuelve a ser tierra arada común. Si se pisa o se seca y vuelve a ser tierra, el mulch se pierde.
- **Uno por suelo:** el mulch de paja solo se puede poner en tierra que no tenga. Si ya tiene, avisa "Este suelo ya
  tiene mulch" y no gasta el ítem.

**Código:**
- `processing/`: `DryingRules` y `Quality` (con tests), `ProcessingRecipes` y `QualityEffectsConsumeEffect`.
- `block/`: `DryingRackBlock`, `CuringJarBlock` y `MulchedFarmlandBlock`, más los block entities en `block/entity/`.
- `registry/ModComponents` (componente `quality`) y `effect/RelaxedEffect`.
- Mixin `ShapelessRecipeMixin`, para que la calidad pase al crafteo.
- Cliente: `hud/RelaxedHud`.
- Gametest `ProcessingGameTest`: secado, moho, curado con estrellas, calidad que alarga efectos y mulch visible y
  único.

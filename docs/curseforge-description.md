# Regenerative Agriculture

**Farm with the seasons.** Every crop has its time of year, the climate depends on the biome, the soil remembers what
you grew on it, and the best harvests come from rotation, companion planting and compost. On top of that: greenhouses
and underground grow rooms, drying and curing, a three-block microbrewery, mate with a thermos, meals with effects, and
a farming manual with a route of milestones to guide you.

![The new crops in the field](SCREENSHOT_URL_new_crops)
<!-- screenshot: new_crops.png -->

---

## 🌦️ Seasons and climate

- **A calendar with seasons.** It runs in the southern hemisphere: spring, summer, autumn and winter, **15 Minecraft
  days each** (adjustable with the game rule `regenerative_agriculture:season_length`). The date follows the world
  clock, so sleeping moves it forward too.
- **Real temperatures by biome.** The temperature in °C comes from the biome, the season, the height and the time of
  day, plus a daily variation that is fixed per world, so **the forecast always comes true**.
- **A small HUD** shows the season, the date and the temperature where you stand.
- **`/season` command:** today's weather, a forecast for the coming days, and `/season set` to jump to any season.

![HUD with season, date and temperature](SCREENSHOT_URL_hud)
<!-- screenshot: hud_winter.png -->

### Weather station
A block that shows today's weather and a **7-day forecast** with frost warnings. It also suggests **what to plant
now** and what to prepare for the next season.

![Weather station: winter forecast](SCREENSHOT_URL_weather_station_winter)
![Weather station: summer forecast](SCREENSHOT_URL_weather_station_summer)
<!-- screenshots: weather_station_winter.png, weather_station_summer.png -->

---

## 🌱 Crops

**18 new crops**, each with 4 hand-drawn growth stages:
- lettuce, tomato, arugula, broccoli, soybean, sunflower, basil, squash;
- onion, garlic, cannabis, tobacco, coffee, chili, hop, yerba mate, corn, bean.

- **Every crop has a profile:** the seasons it grows in, an optimal temperature range, frost tolerance and growth speed.
  Outside its season a crop stalls, and frost damages it.
- **Vanilla crops follow the seasons too:** wheat, potatoes, carrots and beetroot.
- **Wild plants are the source of seeds.** Each crop grows wild in its own biomes, and seeds only go on farmland.
  You can also turn a harvest back into seeds at the crafting table.
- **Pruning shears** harvest perennial crops (tomato, squash, coffee, chili, hop, yerba mate) **without breaking the
  plant**, so they grow back.
- **Crop profiles are data-driven** (JSON), so datapacks can change any value.

![Crop sheet in the manual](SCREENSHOT_URL_crop_sheet)
<!-- screenshot: manual_crop_sheet.png -->

---

## 🏡 Greenhouses and grow rooms

- **Greenhouse:** glass roof and walls. Inside it is **8 °C warmer and there is no frost**, so you can grow out of
  season. A heat source inside (campfire, furnace, magma) adds more.
- **Summer overheating:** in summer, outside snowy biomes, a greenhouse needs **extractors** or it overheats.
  Greenhouses don't work in deserts.
- **Underground grow room:** grow lights hang from the ceiling (1 to 5 blocks above the plants). The room stays at a
  steady **20 °C** all year.
- **Solar extractor:** it needs a free vent shaft to the surface. Plants give off water, so you need **one extractor
  per 20 plants**. The solar panel on top runs it, and **the fan spins** when it's working.
- **Mold:** above **85 % humidity** crops go moldy, and the mold spreads to its neighbours.
- **Thermo-hygrometer:** a handheld gadget that shows the temperature and humidity where you stand, and **tells you
  what your setup is missing** (more extractors, a grow light, ventilation).

![Grow room with lights and a working extractor](SCREENSHOT_URL_grow_room_ok)
![Blocked vent: the thermo-hygrometer warns you](SCREENSHOT_URL_grow_room_no_vent)
![Mold in a muggy room](SCREENSHOT_URL_grow_room_mold)
<!-- screenshots: grow_room_ok.png, grow_room_no_vent.png, grow_room_mold.png -->

---

## 🪱 Living soil

Every farmland block keeps track of its **nitrogen**, its **organic matter** and the **last plant families** grown on it.

- **Fertility:** harvests use up nitrogen (corn and squash a lot). **Legumes (soy, beans) put it back.** On exhausted
  soil everything grows at 40 % speed.
- **Crop rotation:** the same family again in the same spot gives −30 %. Three times in a row counts as monoculture,
  −50 %.
- **Companion planting:** a companion within 2 blocks gives +15 % and an antagonist −25 %. Three different species
  around give a **polyculture bonus**. The *three sisters* (corn, beans, squash) help each other.
- **Compost bin:** works like a composter but gives **compost**, which restores organic matter and nitrogen.
- **Straw mulch:** you can see it on the soil. It protects crops from light frost for 4 harvests and feeds the soil.
- **Soil test kit:** shows everything above on any crop, plus the total effect on growth.

![Soil analysis](SCREENSHOT_URL_soil_analysis)
<!-- screenshot: soil_analysis.png -->

---

## 🍲 Cooking and a varied diet

Meals with effects tied to their ingredients:

- **Hamburger:** fills the whole hunger bar.
- **Green salad:** *Vitality*, +10 % speed.
- **Steamed broccoli:** *Immunity* against poison, hunger and nausea.
- **Soy stew:** Strength.
- **Squash soup:** *Warmth*, so powder snow won't freeze you.
- **Pesto pasta:** Haste.
- **Tomato juice:** Regeneration.
- **Locro** (corn, beans, squash, chili, onion): Warmth and Resistance.
- **Asado with chimichurri:** Strength and Regeneration.
- **Roasted corn** and **roasted sunflower seeds**.
- **Coffee:** Haste and Speed.

**Varied diet:** eat **4 different vegetables in one day** to become *Well Nourished* (+2 hearts for a day).

![Meals and effects](SCREENSHOT_URL_food)
<!-- screenshot: food_inventory.png -->

---

## 🍂 Drying and curing

- **Drying rack:** hang up to 16 of a harvest and **watch it dry** on the rack. Speed depends on the air around the
  rack: dry, mild air is fastest. Rain stops it, and a damp day spoils the harvest.
  - Garlic and onion cure in the air.
  - Tobacco, cannabis, coffee, chili, hops, yerba mate and corn dry.
  - Wheat dries into malt; squash dries into the gourd for mate.
- **Curing jar:** dried tobacco, cannabis and yerba **gain quality over the days** (★ to ★★★). The quality carries over
  to whatever you craft with them and makes its effects last longer.

![Drying racks: fresh, dried, spoiled](SCREENSHOT_URL_drying_racks)
<!-- screenshot: drying_racks.png -->

---

## 🍺 Microbrewery

Three machines that must stand **side by side**: mash tun → brew kettle (over a fire) → fermenter. The wort flows from
one to the next on its own.

1. **Mash tun:** a water bucket + 4 malt. It leaves spent grain behind, which goes to compost.
2. **Brew kettle:** add 2 dried hops and put a fire under it. It boils with bubbles and steam.
3. **Fermenter:** add yeast and wait **7 days**. Fermentation quality depends on the temperature: **16–22 °C is
   ideal**.

- **Hydrometer:** read the gravity, the alcohol %, the day and the expected quality while it ferments.
- **Animated 3D models** for every stage.

![The microbrewery at work](SCREENSHOT_URL_brewery)
<!-- screenshot: brewery.png -->

---

## 🧉 Mate

- **The mate:** cure a squash into a gourd, then add an iron nugget for the straw. The result is a **reusable 3D
  mate**.
- **Loading it:** hold yerba in your other hand to load it (10 servings).
- **Serving it:** with a **thermos of hot water in your other hand**, the mate is served again after every sip. Without
  the thermos it lasts one serving.
- **The thermos:** fill it at any water source, then heat it in a furnace to get **water at 85 °C** (10 servings).

![Mate and thermos in hand](SCREENSHOT_URL_mate)
<!-- screenshot: mate_in_hands.png -->

### Smokables
- **Cigar** (cured tobacco): Haste.
- **Joint** and **brownie** (cured cannabis): *Relaxed*. It slows you down a bit, heals slowly, gives you the munchies
  and tints the edges of the screen.
- Better quality makes the effects last longer.

![Relaxed effect](SCREENSHOT_URL_relaxed)
<!-- screenshot: relaxed.png -->

---

## 📖 The farming manual

Every player gets it on first join. It's an open book with bookmarks:

- **Route:** a **map of linked nodes** (like a skill tree) with 23 milestones, from your first wild seeds to craft beer.
  Each node is a real advancement, and the route also appears as its own tab in the advancements screen.
- **Seasons:** which crops grow in each season.
- **Crops:** a full sheet for every crop.
- **Kitchen:** every meal, its effect and its vegetables.
- **Chapters:** soil, climate and greenhouses, drying and curing, brewing and mate.

![Manual: the route](SCREENSHOT_URL_manual_route)
![Manual: seasons](SCREENSHOT_URL_manual_seasons)
![Manual: kitchen](SCREENSHOT_URL_manual_kitchen)
<!-- screenshots: manual_route.png, manual_seasons.png, manual_kitchen.png -->

---

## 🧑‍🌾 Villagers and sounds

- **Farmers** buy your produce and, as they level up, sell seeds, compost, mulch and tools. At master level they buy
  cured tobacco, beer and aged yerba.
- **Wandering traders** sell the rare seeds: coffee, yerba mate, tobacco, cannabis, hops, chili, garlic and corn.
- **Custom sound events** for sipping mate, the thermos, smoking, the boiling kettle and the bubbling fermenter.

---

## 📋 Requirements

- Minecraft **26.3**
- Fabric Loader **0.19.5+**
- **Fabric API** (required)
- Java **25**
- Install it on **both client and server** in multiplayer.
- Languages: **English** and **Spanish** (Argentina, Spain, Mexico).

This is an early version (**0.1.0**), so balance may change with feedback. Bug reports and suggestions are welcome!

---

## 🇦🇷 En español

**Regenerative Agriculture** es un mod de agricultura regenerativa:

- **Estaciones y clima:** estaciones del hemisferio sur y clima real según el bioma, con pronóstico.
- **Cultivos:** 18 cultivos nuevos que solo crecen en su estación (los de vanilla también siguen las estaciones). Las
  plantas silvestres son la fuente de semillas.
- **Invernaderos y salas de cultivo:** invernaderos, y salas subterráneas con luces de cultivo y extractores solares.
  Con mucha humedad aparecen hongos.
- **Suelo vivo:** nitrógeno, materia orgánica, rotación de cultivos, plantas compañeras, compost y mulch.
- **Cocina:** comidas con efectos y bonus por dieta variada.
- **Secado y curado:** secadero y frasco de curado con calidad de ★ a ★★★.
- **Microcervecería:** macerador, olla y fermentador, con densímetro.
- **Mate:** mate con termo a 85 °C.
- **Manual:** un libro con una ruta de logros en forma de árbol de nodos.
- **Intercambios:** con granjeros y con el comerciante errante.

Requiere Minecraft 26.3, Fabric Loader 0.19.5+, Fabric API y Java 25.

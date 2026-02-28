Этот серверный мод предназачен для перемещения игрока по игровым серверам через прокси ядро velocity

Мод создан для neoforge 1.21.1 актуальной версии (возможно и надругих версий будет тоже работать если в meta-imf/neoforge поменять или добавить после запятого версию, но это неточно)

В этом моде есть фишка создания своего соунтрека
Пожалуста не исползуйте мой мод как машиа соунтреков 👉👈

Мод привязывается автоматически через Bungecoord то сеть когда вы подключаете к серверу прокси Velocity через др. мод совместимости

Тут будет туториал что да как
Когда вы запускаете мой серверный мод то создаёт в config файл "vpp-portals"
При открытие файла "vpp-portals" вы можете заметить такую картину

{
"portals": {
"lobby": {
"server": "lobby",
"timer": 100,
"trigger": {
"scan_radius": 4.0,
"offset": "~5, ~, ~5"
},
"visuals": {
"particle": "minecraft:portal",
"orbit_rad": 1.2,
"orbit_speed": 0.1
},
"melody": [
"1/block.beehive.enter-1.0-1.0-player",
"100/entity.enderman.teleport-1.0-1.0-world"
]
}
}
}

Portal - это все ваши теги привязки со своими настройками
lobby - это ваш тег
server - это куда толжен отправить вас
timer - это таймер отчёта о телепортации
triger - это основная чать тригера для игрока
scan_radius - это радиус тригера отчёта телепортации
offset - это кординаты куда должно откинуть игрока до телепортации чтобы он появился туда куда вы укажите
visual - это эксперементальная или даже удобная часть визуала
particle - выбираете какие партиклы должны спавнится при тригере (если будет пусто то мод проигнорирует visual)
orbit_rad - радиус круга чатиц
orbit_speed - скорость вращения частиц
melody - это фишка моего мода куда вы прописываете свои мелодии

Конфигурация спавна чатиц скрыта в моём коде для оптимищации сетевого стека и всёравно вам это и ненужно и достаточно этого
потому вы влюбой момент можете через датапаки или комадные блоки восоздать свои прикольные анимации из партикл и не только

Тут я раскажу как работает melody
Мелодии срабатывают в момент когда тики сравниваются из конфигурации мелодий
То есть например 20 tick таймера = 20 tick мелодии
Когда они сравнялись то из "20/entity.enderman.teleport-1.0-1.0-world" после / срабатывает entity.enderman.teleport-1.0-1.0-world
Для мелодии таймер работает в плюсовую сторону и привязана к основному таймеру который работает в минусовую сторону
То есть для мелодии будет тики идти от 0 tick до указаного timer, а в таймере отчёта в обратку от указаного timer до 0 tick

Внимание 20 tick = 1 сек (для чайников)

Давайте теперь обясню что за что и как работает мелодия например по примеру "100/entity.enderman.teleport-1.0-1.5-world"
100 - это тик для сравнивания
entity.enderman.teleport - это звук из майна (сюда можете выбрать любой звук из майна даже из мода например Create)
1.0 - после "-" позади звука это громкость (0.5-2.0)
1.5 - после "-" позади грокости это тональность, то есть от низкой к высокой ноте (0.5-2.0)
Далее это откуда идёт звук:
- world это от тебя во весь мир
- player это у тебя в голове

Вот вам пример melody
"1/block.note_block.bass-0.5-0.8-world",
"1/block.note_block.snare-0.5-0.6-world",
"1/block.note_block.harp-1.0-0.7-world",
"5/block.note_block.harp-1.12-0.7-world",
"9/block.note_block.bass-0.5-0.8-world",
"9/block.note_block.snare-0.5-0.6-world",
"9/block.note_block.harp-1.19-0.8-world",
"13/block.note_block.harp-1.26-0.8-world",
"17/block.note_block.bass-0.63-0.8-world",
"17/block.note_block.snare-0.5-0.6-world",
"17/block.note_block.harp-1.34-0.9-world",
"21/block.note_block.harp-1.5-0.9-world",
"25/block.note_block.bass-0.5-0.8-world",
"25/block.note_block.snare-0.5-0.6-world",
"25/block.note_block.harp-1.0-0.7-world",
"29/block.note_block.harp-1.12-0.7-world",
"33/block.note_block.bass-0.5-0.8-world",
"33/block.note_block.snare-0.5-0.6-world",
"33/block.note_block.harp-1.19-0.8-world",
"37/block.note_block.harp-1.26-0.8-world",
"41/block.note_block.bass-0.63-0.8-world",
"41/block.note_block.snare-0.5-0.6-world",
"41/block.note_block.harp-1.34-0.9-world",
"45/block.note_block.harp-1.5-0.9-world",
"49/block.note_block.bass-0.5-0.8-world",
"49/block.note_block.snare-0.5-0.6-world",
"49/block.note_block.harp-1.0-0.7-world",
"53/block.note_block.harp-1.12-0.7-world",
"57/block.note_block.bass-0.5-0.8-world",
"57/block.note_block.snare-0.5-0.6-world",
"57/block.note_block.harp-1.19-0.8-world",
"61/block.note_block.harp-1.26-0.8-world",
"65/block.note_block.bass-0.63-0.8-world",
"65/block.note_block.snare-0.5-0.6-world",
"65/block.note_block.harp-1.34-0.9-world",
"69/block.note_block.harp-1.5-0.9-world",
"73/block.note_block.bass-0.71-0.9-world",
"73/block.note_block.snare-0.5-0.6-world",
"73/block.note_block.harp-1.5-1.0-world",
"77/block.note_block.harp-1.5-0.9-world",
"81/block.note_block.bass-0.5-0.8-world",
"81/block.note_block.snare-0.5-0.6-world",
"81/block.note_block.harp-1.0-0.7-world"

Если что вы можете текста размещять так
"81/block.note_block.snare-0.5-0.6-world", "81/block.note_block.snare-0.5-0.6-world", "81/block.note_block.harp-1.0-0.7-world",
"89/block.note_block.harp-1.0-0.7-world"
Вообщем без разницы

Самое главно Если трек прривышает например timer 100 на 100 тиков (то есть ваша мелодия на 200 тиков)
то ту половину он неуслышит (не волнуйтесь никакого звукового бага небудет)

И напоследок могу сказать что в конфиг можно запихать и второй тег и будет выглядить вот так

{
"portals": {
"lobby": {
"server": "lobby",
"timer": 100,
"trigger": {
"scan_radius": 4.0,
"offset": "~5, ~, ~5"
},
"visuals": {
"particle": "minecraft:portal",
"orbit_rad": 1.2,
"orbit_speed": 0.1
},
"melody": [
"1/block.beehive.enter-1.0-1.0-player",
"100/entity.enderman.teleport-1.0-1.0-world"
]
},
"Hi": {
"server": "minigames",
"timer": 100,
"trigger": {
"scan_radius": 3.0,
"offset": "~5, ~, ~5"
},
"visuals": {
"particle": "minecraft:cherry_leaves",
"orbit_rad": 3.0,
"orbit_speed": 0.1
},
"melody": [
"1/block.beehive.enter-1.0-1.0-player",
"100/entity.enderman.teleport-1.0-1.0-world"
]
}
}





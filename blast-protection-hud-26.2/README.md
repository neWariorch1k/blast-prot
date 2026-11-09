# Blast Protection Indicator — порт на Minecraft 26.2 (Fabric)

Исходный мод: Blast Protection Indicator 2.0.0 для 26.3, автор Ivan, лицензия MIT (см. `LICENSE`).
Здесь он перенесён на Minecraft 26.2. Функциональность не менялась:
индикатор у прицела, показывающий, есть ли на штанах Blast Protection, звуковое оповещение,
экран настроек (клавиша или Mod Menu), горячие клавиши.

## Что изменено для 26.2

* Коды клавиш: в 26.3 они на SDL, в 26.2 на GLFW (Esc в окне назначения клавиши теперь 256, а не 41).
* Сообщение «включено/выключено» показывается через `gui.hud.setOverlayMessage`.
* Версии зависимостей: Fabric Loader 0.20.1, Fabric API 0.161.0+26.2, Mod Menu 20.0.3.
* Версия мода: 2.0.0+26.2, зависимость `minecraft: ~26.2`.

## Сборка

Нужен JDK 25. `gradle wrapper --gradle-version 9.5.1`, затем `./gradlew build`.
Готовый мод: `build/libs/blast-protection-hud-2.0.0+26.2.jar`.
Либо загрузите проект на GitHub: автосборка описана в `.github/workflows/build.yml`.

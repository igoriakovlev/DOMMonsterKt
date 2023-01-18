# DomMonsterKt

Kotlin Wasm port of [dom-monster](https://raw.githubusercontent.com/Rich-Harris/dom-monster/)

## Build
* Install Kotlin compiler that supports latest Wasm backend.
* Execute ./gradlew :compileDevelopmentExecutableKotlinWasm
* Install Canary Chrome that supports Wasm GC proposal
* Run Chrome Canary with Wasm GC proposal support (i.e. --js-flags="--experimental-wasm-gc")
* Open ./build/js/packages/DOMMonsterKt-wasm/kotlin/index.html 

## License
MIT.
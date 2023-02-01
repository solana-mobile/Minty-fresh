<div align="center">
    <img src = "assets/banner.png" width = "800px" height="200px" />
</div>

<div align="center">
    <a href="http://makeapullrequest.com">
        <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square" />
    </a>
    <a href="https://developer.android.com/jetpack/androidx/versions/all-channel">
        <img src="https://img.shields.io/badge/Jetpack%20Compose-1.3.3-brightgreen" />
    </a>
    <a href="https://github.com/solana-mobile/Minty-fresh/network">
        <img src="https://img.shields.io/github/forks/solana-mobile/Minty-fresh/" />
    </a>
    <a href="https://github.com/solana-mobile/Minty-fresh/stargazers">
        <img src="https://img.shields.io/github/stars/solana-mobile/Minty-fresh" />
    </a>
    <a href="https://github.com/solana-mobile/Minty-fresh/issues">
        <img src="https://img.shields.io/github/issues/solana-mobile/Minty-fresh" />
    </a>
    <a href="https://github.com/solana-mobile/Minty-fresh/blob/main/LICENSE.md">
        <img src="https://img.shields.io/github/license/solana-mobile/Minty-fresh" />
    </a> 
    <a href="https://twitter.com/solanamobile">
        <img src="https://img.shields.io/twitter/url?label=follow&style=social&url=https%3A%2F%2Ftwitter.com%2Fsolanamobile" />
    </a>
</div>

<br />

# Minty Fresh

A quick, snappy sentence hyping Minty Fresh! (We should write this as a team)

<!-- This would be a great place to include our store download badge -->

## Take a Look 

| Screenshot 1 | Screenshot 2 | Screenshot 3 | Screenshot 4 |
|---|---|---|---|
<img src="assets/device_shot_1.png" width="250"> | <img src="assets/device_shot_2.png" width="250"> | <img src="assets/device_shot_3.png" width="250"> | <img src="assets/device_shot_4.png" width="250"> |

## Technologies

Minty Fresh is built using 100% native Kotlin Android code, and is meant to demonstrate a best-in-breed implementation of native mobile dApp. Some implementation highlights:

- Extensive use of [Mobile Wallet Adapter](https://github.com/solana-mobile/mobile-wallet-adapter)
- 100% Jetpack Compose UI w/ Material 3
- Coroutines with Flow & StateFlow
- Multi-module app
- Hilt Dependency Injection
- Room local database

## Building & Running

```shell
git clone git@github.com:solana-mobile/Minty-fresh.git
cd Minty-fresh
```

When opening the project in Android Studio, be sure to select the `build.gradle.kts` file from the project root.

## Implementation Overview

Minty Fresh is a multi-module Android application, which means that the source code is broken up into logical code packages that variously depend on each other. 

Roughly speaking, the modules are separated out based on "layers" of app logic, e.g.; UI modules, business logic, networking/data storage modules and the like.

The following is a module dependency diagram, showing the modules that exist in the app and how they depend on each other:

<!-- Will need to generate an exported image of the document as it cannot be embedded here -->

Each module in the application has its own readme file, which is linked here:

### App & Screen Modules

- [Root Application module](tree/main/app)
- [Photo selection "gallery"](tree/tree/main/ui/gallery)
- ["My Mints" Screens](tree/main/ui/mymints)
- [NFT minting screen](tree/main/ui/nftMint)
- [Wallet Connect Button](tree/main/ui/walletConnectButton)
- [Shared composables](tree/main/ui/commonComposable)

### Logic Modules

- [NFT minting logic](tree/main/libs/mintycore)
- [Persistence/local storage](tree/main/libs/persistence)
- [Networking interfaces](tree/main/libs/networkInterface)
- [Networking implementation](tree/main/libs/networkInterfaceImpl)
- [Network interface bindings](tree/main/libs/networkConfigs)

## License

[Apache 2.0](https://github.com/solana-mobile/Minty-fresh/blob/main/LICENSE.md)
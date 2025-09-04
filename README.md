# Summoning Wand

Language: [简体中文](README_zh.md) | **English**

<div align="center">
<img src="src/main/resources/icon.png" alt="icon.png"/>
</div>

This mod adds only one item - the Summoning Wand, which can teleport bound entities to your location<br/>

* **For:** Casual players who don't want to transport entities manually or type `/tp` commands
* **Use Cases:** Summoning mounts/vehicles, transporting mobs, ...
* **Development Environment:**
    * Minecraft Version: 1.20.1
    * Forge MDK Version: 47.4.0
* **Supported Versions:**
<div align="center">

| Minecraft | Forge |        NeoForge         |
|:---------:|:-----:|:-----------------------:|
|  1.20.1   | 47.x* | Theoretically Supported |
</div>

\* Tested on 47.4.0 and 47.3.0, theoretically supports all 47.x versions

## Features
[//]: # (Demo Video：[Bilibili]&#40;&#41;)

### Item Information
* Name: Summoning Wand
* ID: `summoningwand:summoning_wand`
* Creative Tab: Tools & Utilities
* Durability: 132
* Crafting Recipe:
<div align="center">
<img src="res/recipe.png" alt="recipe.png">
</div>

### Usage
* **Binding Entities**: Hold the wand in the designated hand (default: **off-hand**) and right-click an entity to bind it.
    * Unbound wand texture: <span><img src="res/summoning_wand_32x.png" alt="summoning_wand.png" /></span>
    * Bound wand texture: <span><img src="res/summoning_wand_activated_32x.png" alt="summoning_wand_activated.png" /></span> (with the display name turning yellow and followed by bound entity's name/type)
    * Binds via UUID, meaning the binding won't lose after logging out and logging in again.
    * Default valid target entities: Non-player mobs, falling blocks, minecarts, boats, etc.
* **Teleporting Entities**: Hold the wand in the designated hand (default: **main hand**) and right-click a block to teleport the bound entity.
    * The mechanism of teleportation is essentially the same as `/tp` command, but includes passengers; Hold `Shift` to prevent passengers from being teleported.
    * Entities in unloaded chunks cannot be teleported.
    * The target position can be the interacted block's location or the one adjacent to the interacted face (see [`SummoningWandItem.java`](src/main/java/xyc/summoningwand/SummoningWandItem.java)).
    * NO SAFETY CHECKS (suffocation, fall damage, fire/lava) are conducted, so keep cautious when teleporting living entities!
    * Default valid target blocks: Non-colliding blocks (flowers, torches, liquids, etc.), partial blocks (depending on your config, see [`SummoningWandItem.java`](src/main/java/xyc/summoningwand/SummoningWandItem.java)).
* **Clearing Bindings**: Place wand in crafting grid to reset (**will remove enchantments**).
* **Item Tooltip**: When hovering your mouse over the summoning wand item in the inventory:
    * Press `Shift` to show brief usage guide.
    * Displays the owner's name, and the bound entity's name/type if bound.
* **Commands**: Use `/summoningwand` to query or **manually reload configs** (designed to replace Forge's unreliable auto-reload system):

  ```
  /summoningwand config <allClients|client|common> <GET|REFRESH>
  ```

* `<allClients|client|common>`
  - `allClients`: Manage all players' client configs (requires OP 4)
  - `client`: Manage your client config
  - `common`: Manage common config (refreshing requires OP 4)
* `<GET|REFRESH>`
  - `GET`: View configs
  - `REFRESH`: Reload configs

> Entity/block interaction events trigger on both client and server, but the server cannot directly read client configs.<br/>
> This mod synchronizes client configs to the server when you log in or manually refresh your local config, so that your wand-hand config works correctly on the server.

## Configuration
See [`Config.java`](src/main/java/xyc/summoningwand/Config.java)

## Mod Integration
### Curtain
* Can bind and teleport fake players (`EntityPlayerMPFake`)

### Yes Steve Model
* Uses arm animations of hoes (`#yes_steve_model:hoes`)

## Tested Mod Compatibilities
### Create
* Can bind and teleport assembled minecarts
* Cannot bind other kinds of dynamic structures (windmills, trains, etc.)

### Immersive Aircraft
* Can bind and teleport aircraft in this mod

## Notes
This is my first Minecraft mod and a learning project for Forge/NeoForge development.<br/>
Submit an [issue](https://github.com/Xyc1596/SummoningWand/issues) <!--or comment on the [demo video]()--> if you find bugs or have suggestions.
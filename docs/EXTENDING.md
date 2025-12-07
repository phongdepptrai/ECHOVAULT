# Extending EchoVault

This document outlines how to extend the game content using the new data-driven architecture.

## 1. Adding a New Projectile Type

1.  **Define the Motion**: If new logic is needed, implement `ProjectileMotion` in `com.echovault.combat.projectile`.
2.  **Define the Visual**: If new art is needed, implement `ProjectileVisual` or add to `AssetGenerator`.
3.  **Register the Definition**:
    In `ProjectileFactory.init()`, add your definition:
    ```java
    ProjectileDef myProj = new ProjectileDef();
    myProj.speed = 400f;
    myProj.damage = 1;
    myProj.motionType = "spiral";
    myProj.visualType = "orb_red";
    Registry.projectiles.put("my_proj", myProj);
    ```

## 2. Adding a New Enemy

1.  **Add Archetype**: Add enum to `EnemyArchetype`.
2.  **Generate Assets**: Update `AssetGenerator` to generate textures for the new archetype (or reuse existing).
3.  **Define Behavior**: Create a class extending `Enemy` (or reuse generic `Enemy` with specific `AIState`).
4.  **Register Animations**: Ensure `AnimationBank` has an entry for your enemy key.

## 3. Adding a New Boss

1.  **Create Class**: Extend `Boss`.
2.  **Define Phases**: In `initPhases()`, use `PatternFactory` to sequence attacks.
    ```java
    phases.add(new Phase(0.5f) {
        public void update(...) {
             PatternFactory.get("ring_fire").execute(roomManager, boss.position, ...);
        }
    });
    ```

## 4. Creating a New Pattern

1.  **Implement**: Create a class implementing `AttackPattern` or add a builder method in `PatternFactory`.
2.  **Use Projectiles**:
    ```java
    ProjectileFactory.spawn(roomManager, "my_proj", position, velocity);
    ```

## 5. Architecture Overview

*   **`anim/`**: Handles sprite animations using state machines.
*   **`combat/projectile/`**: Decouples how a bullet moves (`Motion`) from how it looks (`Visual`).
*   **`pattern/`**: High-level attack logic (fans, rings) independent of specific enemies.
*   **`core/Registry`**: Central lookup for definitions.

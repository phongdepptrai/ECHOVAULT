# EchoVault Animation Tool

A procedural animation generator for EchoVault.

## Running the Tool

### GUI Mode
Launch the interactive preview and export tool:
```bash
./gradlew lwjgl3:run
```

### CLI Mode
Export animations from the command line (headless):
```bash
./gradlew headless:run --args="--out=export --frameSize=64"
```

### CLI Arguments
- `--out=<path>` : Output directory (default: `export`)
- `--frameSize=<int>` : Size of frames in px (default: `64`)
- `--only=<anim1,anim2>` : Comma-separated list of animations (default: `all`)
- `--dirs=<up,down...>` : Comma-separated list of directions (default: `all`)
- `--seed=<long>` : Random seed (default: `12345`)

# GRASSMARLIN

GRASSMARLIN provides IP network situational awareness of industrial control systems (ICS) and Supervisory Control and Data Acquisition (SCADA) networks to support network security. Passively map, and visually display, an ICS/SCADA network topology while safely conducting device discovery, accounting, and reporting on these critical cyber-physical systems.

## Java Migration (October 2025)

**GRASSMARLIN has been migrated from Java 8 to modern Java!**

### Requirements
- **Java 11 or newer** (Java 11, 17 LTS, 21 LTS, or newer)
- Previously required Java 8 only

### What Changed
- Migrated from `javax.xml.bind` to `jakarta.xml.bind` (Jakarta EE 9+)
- Updated all JAXB dependencies to Jakarta JAXB 3.0+
- Fixed deprecated APIs removed in Java 11+
- Compatible with all modern Java versions

### Building from Source

```bash
cd GM3

# Build with Ant (requires Java 11+)
ant -buildfile build-ant.xml jar

# Run the application
java -jar build/app/GrassMarlin.jar
```

For detailed migration information, see [GM3/MIGRATION_SUMMARY.md](./GM3/MIGRATION_SUMMARY.md).

## Documentation

GrassMarlin v3.2 User Guide:
* [Download PDF](https://github.com/iadgov/GRASSMARLIN/raw/master/GRASSMARLIN%20User%20Guide.pdf)
* [View PDF on GitHub](https://github.com/iadgov/GRASSMARLIN/blob/master/GRASSMARLIN%20User%20Guide.pdf)

A [presentation on GRASSMARLIN](http:github.com/iadgov/GRASSMARLIN/blob/master/GRASSMARLIN_Briefing_20170210.pptx) is also available.

## Release

Download the [latest release](https://github.com/iadgov/GRASSMARLIN/releases/latest).

File hashes are located in [FileHash.md](./FileHash.md).

## License

See [LICENSE.md](./LICENSE.md).

## Disclaimer

See [DISCLAIMER.md](./DISCLAIMER.md).


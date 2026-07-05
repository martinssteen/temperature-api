# temperature-api

A small Spring Boot (Kotlin) service that collects temperature and humidity
readings from MQTT-enabled sensors, stores them in PostgreSQL, and exposes the
latest reading per sensor over a simple HTTP API.

## Disclaimer
This is only a hobby project. I built this for fun, to learn, or for my own home setup.

Please do not look at this code and think that it is a statement about my coding skills, or the quality of my code. **Especially not my commit messages, they are absolutely horrendous.**

This project will probably not run on your machine without some tinkering, since I built it around my own setup. You're very welcome to take it, poke around, and steal whatever's useful, if anything.

This repository is also actually a mirror of my own local instance of Git (Gitea) that our home-server runs, so action workflows are created specifically for Gitea.

I'd rather put something imperfect out into the world than keep it hidden until it's perfect. Because honestly, if I didn't, I'd never share anything. :)

## What it does

1. **Ingest** — The app subscribes to one or more MQTT topics. Each message is
   routed by the last segment of its topic:
    - `.../temperature:0` → parsed as a temperature reading
    - `.../humidity:0` → parsed as a humidity reading
    - anything else is ignored
2. **Store** — Readings are written to the `temperatures` and `humidity` tables,
   each row tagged with a `source` and a `recorded_at` timestamp.
3. **Serve** — `GET /temperature` returns the most recent temperature and
   humidity for every known source.

### Topic & payload format

The source name is taken from the **second** segment of the MQTT topic
(`prefix/<source>/.../context`), and payloads are JSON in the
[Shelly](https://shelly.com/) style:

| Reading     | Topic suffix    | JSON field | Example payload |
|-------------|-----------------|------------|-----------------|
| Temperature | `temperature:0` | `tC`       | `{"tC": 21.4}`  |
| Humidity    | `humidity:0`    | `rh`       | `{"rh": 48.0}`  |

For example, a message on `temperatures/livingroom/sensor/temperature:0` with
payload `{"tC": 21.4}` records 21.4 °C for source `livingroom`.

## Tech stack

- Kotlin 2.3 on Spring Boot 4.1 (Java 17)
- Spring Integration MQTT (Eclipse Paho client)
- Spring Data JDBC + PostgreSQL
- Flyway for schema migrations
- Maven (wrapper included)

## Configuration

All configuration is supplied through environment variables (see
[`.env.example`](.env.example)):

| Variable      | Description                            |
|---------------|----------------------------------------|
| `DB_URL`      | JDBC URL for PostgreSQL.               |
| `DB_USERNAME` | Database user                          |
| `DB_PASSWORD` | Database password                      |
| `MQTT_URL`    | MQTT broker URL                        |
| `MQTT_TOPICS` | Comma-separated topics to subscribe to |

The database schema (`temperatures` and `humidity` tables) is created
automatically by Flyway on startup — see
[`V1__INIT.sql`](src/main/resources/db/migration/V1__INIT.sql).

## AI Disclaimer
The static temperatures.html page to be used with our home server homepage was generated with the help of Claude Code.
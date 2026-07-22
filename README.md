# home-api

A single collected place for the random bits of backend I need for my home
server. Rather than spinning up a new service every time I want to store a
reading, expose a small endpoint, or serve a little page, this is one Spring
Boot (Kotlin) app that I keep adding features to as the need shows up.

Think of it as a home-server junk drawer with a tidy front: one deployable, one
database, one place to look.

## Disclaimer
This is only a hobby project. I built this for fun, to learn, or for my own home setup.

Please do not look at this code and think that it is a statement about my coding skills, or the quality of my code. **Especially not my commit messages, they are absolutely horrendous.**

This project will probably not run on your machine without some tinkering, since I built it around my own setup. You're very welcome to take it, poke around, and steal whatever's useful, if anything.

This repository is also actually a mirror of my own local instance of Git (Gitea) that our home-server runs, so action workflows are created specifically for Gitea.

I'd rather put something imperfect out into the world than keep it hidden until it's perfect. Because honestly, if I didn't, I'd never share anything. :)

## The idea

One app, many little features. Each feature is self-contained — it might listen
to something, store something, and/or serve something — but they all share the
same runtime, config, and PostgreSQL database. When I need a new bit of
home-server plumbing, it goes in here instead of becoming yet another service to
deploy and babysit.

## Features


### 🌡️ Temperature & humidity

Collects temperature and humidity readings from MQTT-enabled sensors, stores
them in PostgreSQL, and serves them over HTTP (plus a small static page).

1. **Ingest** — The app subscribes to one or more MQTT topics. Each message is
   routed by the last segment of its topic:
    - `.../temperature:0` → parsed as a temperature reading
    - `.../humidity:0` → parsed as a humidity reading
    - anything else is ignored
2. **Store** — Readings are written to the `temperatures` and `humidity` tables,
   each row tagged with a `source` and a `recorded_at` timestamp.
3. **Serve** — `GET /temperature` returns the most recent temperature and
   humidity for every known source, with historic data also available.

#### Topic & payload format

The source name is taken from the **second** segment of the MQTT topic
(`prefix/<source>/.../context`), and payloads are JSON in the
[Shelly](https://shelly.com/) style:

| Reading     | Topic suffix    | JSON field | Example payload |
|-------------|-----------------|------------|-----------------|
| Temperature | `temperature:0` | `tC`       | `{"tC": 21.4}`  |
| Humidity    | `humidity:0`    | `rh`       | `{"rh": 48.0}`  |

For example, a message on `temperatures/livingroom/sensor/temperature:0` with
payload `{"tC": 21.4}` records 21.4 °C for source `livingroom`.

_More features will land here over time — this section is meant to grow._

## Tech stack

- Kotlin 2.3 on Spring Boot 4.1 (Java 17)
- Spring Integration MQTT (Eclipse Paho client)
- Spring Data JDBC + PostgreSQL
- Thymeleaf for the odd static/served page
- Flyway for schema migrations
- Maven (wrapper included)

## Configuration

All configuration is supplied through environment variables (see
[`.env.example`](.env.example)):

| Variable      | Description                            | Used by      |
|---------------|----------------------------------------|--------------|
| `DB_URL`      | JDBC URL for PostgreSQL.               | All features |
| `DB_USERNAME` | Database user                          | All features |
| `DB_PASSWORD` | Database password                      | All features |
| `MQTT_URL`    | MQTT broker URL                        | Temperature  |
| `MQTT_TOPICS` | Comma-separated topics to subscribe to | Temperature  |

The database schema is managed by Flyway and applied automatically on startup —
see the migrations under
[`src/main/resources/db/migration`](src/main/resources/db/migration). Each new
feature adds its own migration(s).

### Docker Passthrough
```bash
socat   TCP-LISTEN:23750,bind=127.0.0.1,reuseaddr,fork   EXEC:"ssh truenas_admin@192.168.50.137 docker system dial-stdio",nofork
```

## AI Disclaimer
Parts of this project (such as the static `temperatures.html` page used with our
home-server homepage) were generated with the help of Claude Code. This README
was also written with the help of Claude Code.

# Sarkari Setu (Spring Boot 4 + Thymeleaf)

Sarkari job portal: public site + admin panel + mock tests + tools. Only server-rendered Thymeleaf pages (no React, no SPA).

> Not compiled or run in the environment where it was written (no Maven access). Run it once; if any error appears, fix or send it back.

> Full documentation of every feature: **docs/DOCUMENTATION.md**

## Run
1. Java 17+ and Maven.
2. `mvn spring-boot:run` then open http://localhost:8080
3. Admin: http://localhost:8080/admin  (first login is in `site.properties`: `site.seed-admin-email` / `site.seed-admin-password`). **Change the password first** (Admin > My profile).

First start creates the H2 database in `./data`, 8 sections, 4 tools, 5 basic pages (About, Privacy Policy, Disclaimer, Terms, FAQs) and about 124 sample posts.
To reload everything: stop the app, delete `./data`, start again.

## Config files (all changeable without touching code)
| File | What it holds |
|---|---|
| `site.properties` | domain, name, tagline, contact, social links, analytics id, share image, cookie bar, trending count, page size, filter tags, which sections show on the home page, first admin, demo on/off |
| `application.properties` | port, database (H2 / MySQL), logging, upload size |
| `demo-data.txt` | the ~124 sample posts, one per line (format explained inside) |
| `mock-tests.txt` | mock tests and questions (format explained inside) |

Copy any of these next to the jar (same file name) to override the built-in copy without rebuilding.

## Public site
Home (hero search, category tiles, Trending now, Closing soon with days left, latest per section, Blog, email alerts, FAQs, disclaimer),
section lists (filter chips, sort, numbered pagination, breadcrumbs), post page (placeholder design: stage timeline, fee by category,
vacancy table, save, copy link, print, WhatsApp/Telegram share), search with live suggestions, Saved posts (browser only),
Mock Tests (timer, score, explanations), Tools (age, photo/signature resizer, percentage, typing test), Contact Us, basic pages,
light/dark switch, cookie notice, back-to-top, skip link, print styles, favicon, Open Graph and JSON-LD tags, sitemap.xml, robots.txt, feed.xml (RSS).
The recruitment post page is a placeholder: edit `templates/post.html` (the comment at the top lists every field).

## Admin
Dashboard (counts, most viewed, closing soon, posts per section), posts (draft / published / unpublished, search, section filter, pagination,
preview, **duplicate**, 5-point manual verification before publish, editor toolbar), Media (image upload), sections, pages, tools on/off,
trending pins, messages, subscribers (CSV), users and roles (Editor / Super Admin), activity log, **Page hits and logs**, site settings,
change password, logout. Every button has a hover tip. Wrong password 5 times locks that account for 10 minutes.

## Logs
Every page request is logged: `GET /s/jobs -> 200 (12 ms) user=- ip=...` (css/js/images skipped), to the console and `./logs/sarkari.log`.
Admin (Super Admin) > Page hits and logs shows each route with hits, errors, average time, last called, "Never called", missing pages (404) and the last 100 requests.

## Notes
- Post body and pages accept HTML (th:utext); only trusted admins can write it.
- The default Privacy Policy and Terms are templates: read and adjust them before launch.
- Uploaded images live in `./uploads` (back it up). Trending uses total views, pinned first.
- Not built yet: automatic notification fetcher, Telegram auto-posting, sending emails to subscribers, Hindi content.

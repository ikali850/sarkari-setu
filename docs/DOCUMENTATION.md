# Sarkari Setu: Complete Documentation

Version 0.1.0 · Spring Boot 4.0.9 · Java 17+ · Thymeleaf (server-rendered, no React or SPA)

> **Status note.** This project was written without being compiled or run in the environment where it was created (no Maven access).
> Run it once with `mvn spring-boot:run`. If a compile or startup error appears, fix it or send it back. Everything below describes what the code is written to do.

---

## Table of contents

1. [What this project is](#1-what-this-project-is)
2. [Quick start](#2-quick-start)
3. [Project structure](#3-project-structure)
4. [Configuration](#4-configuration)
5. [The public website](#5-the-public-website)
6. [The admin panel](#6-the-admin-panel)
7. [Roles and permissions](#7-roles-and-permissions)
8. [Writing a post (editor guide)](#8-writing-a-post-editor-guide)
9. [Data model](#9-data-model)
10. [Data files: sample posts and mock tests](#10-data-files-sample-posts-and-mock-tests)
11. [Logging and page tracking](#11-logging-and-page-tracking)
12. [Security](#12-security)
13. [SEO and sharing](#13-seo-and-sharing)
14. [URL reference](#14-url-reference)
15. [How-to guides](#15-how-to-guides)
16. [Deployment](#16-deployment)
17. [Troubleshooting](#17-troubleshooting)
18. [Limits and what is not built yet](#18-limits-and-what-is-not-built-yet)
19. [Glossary](#19-glossary)

---

## 1. What this project is

Sarkari Setu is a website for government job seekers. It has two parts:

* **Public website**: visitors read recruitment posts (notification, admit card, result, answer key, syllabus, admission, schemes, blog), search, take free mock tests and use small tools.
* **Admin panel**: your team writes, verifies and publishes posts, manages sections, pages, images, users and watches which pages are visited.

**Main idea:** one recruitment = one page. The same page is updated as the recruitment moves through its **stages** (Notification, Apply online, Admit card, Exam, Result). Different posts inside one notification can have different stages (for example, the admit card for Post A can come earlier than Post B).

**Technology**

| Part | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 4.0.9 (Web MVC, Data JPA, Security, Validation) |
| Pages | Thymeleaf templates, plain CSS, small vanilla JavaScript files |
| Database | H2 file database by default, MySQL by changing 3 lines |
| Build | Maven |

There is no React, Angular or other SPA framework. Every page is rendered on the server.

---

## 2. Quick start

### 2.1 Requirements

* Java 17 or newer (`java -version`)
* Maven 3.9+ (`mvn -v`)
* Internet on the first build (to download libraries)

### 2.2 Run

```bash
cd sarkari-setu
mvn spring-boot:run
```

Open:

* Website: <http://localhost:8080>
* Admin: <http://localhost:8080/admin>

### 2.3 First login

The first Super Admin is created from `site.properties`:

| Setting | Default |
|---|---|
| `site.seed-admin-email` | `admin@example.com` |
| `site.seed-admin-password` | `ChangeMe@123` |

**Change this password immediately** (Admin > My profile > Change password), and change the values in `site.properties` before going live.

### 2.4 What happens on the first start

When the database is empty the app creates:

| Item | Count | Condition |
|---|---|---|
| Super Admin user | 1 | only if there are no users |
| Sections | 8 (Latest Jobs, Admit Card, Result, Syllabus, Admission, Answer Key, Scheme, Blog) | only if there are no sections |
| Tools | 4 (Age Calculator, Photo and Signature Resizer, Percentage Calculator, Typing Speed Test) | only if there are no tools |
| Basic pages | 5 (About Us, Privacy Policy, Disclaimer, Terms and Conditions, FAQs) | only if there are no pages |
| Sample posts | about 124, loaded from `demo-data.txt` | only if `site.seed-demo=true` and there are no posts |

Mock tests (5 tests, 50 questions) are read from `mock-tests.txt` every time the app starts. They are not stored in the database.

### 2.5 Start fresh

Stop the app, delete the `./data` folder (and `./uploads` if you want to remove images), start again. Everything above is created again.

---

## 3. Project structure

```
sarkari-setu/
├── pom.xml                         Maven build file
├── README.md                       Short guide
├── docs/DOCUMENTATION.md           This file
└── src/main/
    ├── java/com/sarkarisetu/
    │   ├── SarkariSetuApplication.java    Starts the app
    │   ├── config/
    │   │   ├── SiteProperties.java        Reads site.properties (name, domain, links...)
    │   │   └── WebConfig.java             Serves uploaded images at /uploads/...
    │   ├── model/                         Database tables (entities)
    │   │   ├── AppUser, Section, Post, Tool, StaticPage,
    │   │   └── ContactMessage, Subscriber, ActivityLog
    │   ├── repo/                          Database access (Spring Data repositories)
    │   ├── security/
    │   │   ├── SecurityConfig.java        Which URLs need which role
    │   │   ├── AppUserDetailsService.java Loads users for login
    │   │   ├── LoginAttempts.java         Lock after 5 wrong passwords
    │   │   └── LoginListener.java         Last login time, login logs
    │   ├── service/
    │   │   ├── DataSeeder.java            First-run data
    │   │   ├── DemoPosts.java             Loads demo-data.txt
    │   │   ├── MockTestService.java       Loads mock-tests.txt
    │   │   ├── PostForm.java, PostService.java   Editor form <-> Post
    │   │   └── PageStats.java             In-memory page hit counters
    │   └── web/
    │       ├── PublicController.java      Public pages
    │       ├── MockController.java        Mock tests
    │       ├── AdminController.java       Admin pages (posts, sections, users...)
    │       ├── MediaController.java       Image upload
    │       ├── LogController.java         "Page hits and logs" page
    │       ├── RequestLogFilter.java      Logs every page request
    │       ├── GlobalModel.java           Data every page needs (menus, site config, user)
    │       └── Counted.java               Small helper for the dashboard chart
    └── resources/
        ├── application.properties         Technical settings (port, database, logging)
        ├── site.properties                Site settings (domain, name, links...)
        ├── demo-data.txt                  Sample posts
        ├── mock-tests.txt                 Mock tests
        ├── static/
        │   ├── css/style.css              All styles (public + admin, light + dark)
        │   ├── favicon.svg
        │   └── js/                        site.js, mock.js, tools/*.js
        └── templates/
            ├── fragments/                 layout.html (page frame), parts.html (reusable pieces)
            ├── index, list, post, search, more, saved, page, contact, error .html
            ├── mock/                      Mock test list and test page
            ├── tools/                     Tools list and the 4 tools
            └── admin/                     All admin pages
```

**Where do I change what?**

| I want to change... | Edit this |
|---|---|
| Site name, domain, links | `site.properties` |
| Colours, spacing, fonts | `static/css/style.css` |
| The look of a recruitment post | `templates/post.html` |
| Home page layout | `templates/index.html` |
| Header, footer, menu | `templates/fragments/layout.html` |
| Sample posts | `demo-data.txt` |
| Mock test questions | `mock-tests.txt` |
| Database | `application.properties` |

---

## 4. Configuration

### 4.1 Three levels of settings

1. `application.properties`: technical settings (port, database, logging, upload size).
2. `site.properties`: everything about your site (name, domain, links, counts).
3. **Override files next to the jar**: a file with the same name placed beside the jar (`./site.properties`, `./demo-data.txt`, `./mock-tests.txt`) is used instead of the built-in copy. No rebuild needed.

Restart the app after changing any setting.

### 4.2 `site.properties`

| Key | Default | What it does |
|---|---|---|
| `site.name` | Sarkari Setu | Name in the header, footer, page titles, emails |
| `site.tagline` | Every sarkari recruitment, tracked stage by stage | Home page headline and default description |
| `site.base-url` | http://localhost:8080 | **Your real domain**, no trailing slash. Used for canonical links, sitemap, RSS, sharing |
| `site.contact-email` | contact@example.com | Shown on Contact Us; fills `{email}` in pages |
| `site.phone` | (empty) | Shown on Contact Us if filled; fills `{phone}` |
| `site.address` | (empty) | Shown on Contact Us if filled; fills `{address}` |
| `site.telegram-url` | (empty) | Footer link, home button, floating "Join Telegram" button |
| `site.whatsapp-url` | (empty) | Footer link |
| `site.instagram-url` | (empty) | Footer link |
| `site.youtube-url` | (empty) | Footer link |
| `site.analytics-id` | (empty) | Google Analytics id like `G-XXXXXXX`. Empty = no tracking script |
| `site.theme-color` | #1d3fbf | Browser address-bar colour on phones |
| `site.footer-note` | Information is collected from official sources... | Line shown in the footer |
| `site.disclaimer-note` | We are not government employees... | Yellow disclaimer box on home and post pages |
| `site.og-image` | (empty) | Full address of the image shown when links are shared (1200 x 630 is best) |
| `site.cookie-notice` | true | Shows or hides the cookie bar |
| `site.trending-count` | 10 | How many posts in "Trending now" |
| `site.page-size` | 12 | Posts per page on section lists and search |
| `site.home-sections` | answer-key,syllabus,admission | Slugs of the extra sections shown on the home page (6 posts each) |
| `site.filter-tags` | 10th,12th,Graduate,ITI,Diploma,Railway,Police,Bank | Filter chips on section pages |
| `site.seed-admin-name` | Super Admin | Name of the first admin |
| `site.seed-admin-email` | admin@example.com | Email of the first admin |
| `site.seed-admin-password` | ChangeMe@123 | Password of the first admin |
| `site.seed-demo` | true | Load sample posts on first start |
| `site.demo-file` | ./demo-data.txt | Path of your own sample-post file (used if it exists) |

### 4.3 `application.properties`

| Key | Default | Meaning |
|---|---|---|
| `server.port` | 8080 | Port |
| `spring.datasource.url` | `jdbc:h2:file:./data/sarkari` | Database location |
| `spring.jpa.hibernate.ddl-auto` | update | Creates/updates tables automatically |
| `spring.jpa.open-in-view` | true | Lets templates load lists (fees, stages) lazily |
| `spring.thymeleaf.cache` | false | Set **true** in production |
| `server.servlet.session.timeout` | 60m | Admin session length |
| `logging.*` | see section 11 | Log levels and file |
| `spring.servlet.multipart.max-file-size` | 5MB | Largest image upload |

### 4.4 Using MySQL

In `application.properties`, comment the three H2 lines and uncomment the MySQL lines:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sarkari?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=change-me
```

The MySQL driver is already in `pom.xml`. Tables are created automatically on first start.

---

## 5. The public website

### 5.1 Layout shared by all pages

* **Header**: site name, menu (Home, the TAB sections, More, Mock Tests, Tools, Saved), search box with live suggestions, light/dark switch.
* **Phone bottom tab bar**: Home, the TAB sections (by default Latest Jobs, Admit Card, Result), More.
* **Footer**: links to basic pages (About, Privacy Policy, Disclaimer, Terms, FAQs), Contact Us, RSS, and your social links; footer note and copyright line.
* **Extras**: skip-to-content link, cookie bar, back-to-top button, floating "Join Telegram" button (only if `site.telegram-url` is set).
* **Light / dark**: follows the device setting; the switch in the header overrides it and remembers the choice in the browser.

### 5.2 Home page (`/`)

From top to bottom:

1. **Hero**: headline, search box, and counts of published posts in each TAB section (for example "36 Latest Jobs").
2. **Category tiles**: the first six visible sections, plus **Mock Tests** and **Tools**.
3. **Trending now**: ranked list. Pinned posts first, then most viewed. Top 3 get gold, silver, bronze badges. On phones only the top 5 are shown with a "Show all" button; on desktop it is two columns.
4. **Closing soon**: published posts whose last date is within the next 7 days (up to 8), with "N days left".
5. **Section cards**: the TAB sections with their 5 newest posts each.
6. **Extra section cards**: the sections listed in `site.home-sections` with 6 posts each.
7. **From the Blog**: 3 newest posts of the section whose slug is `blog`.
8. **Email alerts box**.
9. **FAQs**: content of the basic page with slug `faq`.
10. **Disclaimer box**: text from `site.disclaimer-note`.

Each post row shows the title, "Updated on <date>" and a coloured status chip on the right.

### 5.3 Section list pages (`/s/{slug}`)

Example: `/s/jobs`, `/s/admit-card`.

* Breadcrumb, section name and post count.
* **Filter chips** (from `site.filter-tags`): a post matches when its **Filter tags** field contains the word.
* **Sort**: Latest (newest published first), Most viewed, Oldest.
* **Numbered pagination**: Prev 1 … 4 5 [6] 7 8 … 20 Next. Page size is `site.page-size`.
* Hidden sections show a 404 page.

### 5.4 Post page (`/post/{slug}`)

Only **published** posts are visible. Each visit adds 1 to the view counter (used by Trending).

The page shows, in this order (blocks that have no data are hidden):

* Breadcrumb, title, "Updated on", view count, organisation.
* **Status banner**: the current stage (see 5.4.1) and, when the last date is within 14 days, "N days left".
* **Buttons**: Save, Copy link, Print, WhatsApp, Telegram.
* **Important details**: organisation, total posts, last date, and every "extra detail" row.
* **Stages**: a timeline with a dot, stage name, "applies to" (if not all posts), date text, optional link and a Done / Live / Upcoming chip.
* **Application fee** table (one row per category).
* **Vacancy details** table (post, UR, OBC, SC, ST, EWS, automatic total).
* **Your write-up** (the HTML body).
* "Verified by <name>" line, disclaimer box, related posts (5 newest in the same section).

The look of this page is intentionally a simple placeholder. Replace everything inside `<main>` of `templates/post.html`. The comment at the top of that file lists every field you can use.

#### 5.4.1 How the status chip is chosen

The **current stage** is: the first stage marked **Live**; if none, the first **Upcoming**; if none, the last **Done**.
The chip text is `Stage name: State` (for example "Admit card: Live"). If a post has no stages, the chip is "Apply open" or "Closed" based on the last date; if there is no last date there is no chip.
Colours: Done = green, Live = blue, Upcoming = grey.

#### 5.4.2 "Updated on"

Shows the published date. For a post that was never published it shows the last edit date.

### 5.5 Search (`/search?q=...`)

* Looks for the words in the **title**, case-insensitive, among published posts.
* **Live suggestions** appear under the header search box after 2 letters (up to 6 titles, most viewed first). `Esc` closes them.
* The results page has numbered pagination and is marked `noindex` so search engines do not index it.

### 5.6 Saved posts (`/saved`)

* The **Save** button on a post stores the post's title and address **in the visitor's own browser** (localStorage). Nothing is sent to the server.
* The Saved page lists them. Clearing browser data removes them.

### 5.7 More (`/more`)

Tiles for the MORE sections, Mock Tests, every enabled tool, Saved posts, FAQs and Contact Us.

### 5.8 Mock tests (`/mock-tests`, `/mock-tests/{slug}`)

* List of tests with number of questions and minutes.
* **Test page**: press **Start**, the timer begins, choose one option per question, **Submit** (or time runs out and it submits automatically).
* After submitting: score (for example 7 / 10), percentage with a short message, correct answers in green, wrong choices in red, and the explanation for each question.
* No negative marking. Scores are **not stored**.
* Questions come from `mock-tests.txt` (format in section 10.2). The correct answers are part of the page, so this is a practice tool, not a secure exam.
* Included: General Knowledge, Quantitative Aptitude, English, Reasoning, Computer Basics (10 questions each).

### 5.9 Free tools (`/tools`, `/tools/{slug}`)

All four tools run entirely in the visitor's browser. Nothing is uploaded.

| Tool | Address | How it works |
|---|---|---|
| **Age Calculator** | `/tools/age-calculator` | Date of birth and "age as on" date (default today) give years, months and days. Optional minimum age, maximum age and relaxation years show "within / above the limit". |
| **Photo and Signature Resizer** | `/tools/photo-resizer` | Choose an image, set width, height and maximum KB. The image is drawn on a white background and saved as JPEG; quality is lowered from 92% in steps of 5% until it fits the KB limit (minimum 10%). Preview and download (`resized.jpg`). If the limit cannot be reached it says so and suggests smaller dimensions. |
| **Percentage Calculator** | `/tools/percentage-calculator` | Marks obtained / total marks, and CGPA x multiplier (default 9.5; use your university's rule). |
| **Typing Speed Test** | `/tools/typing-test` | One of three sample texts, 60 seconds starting at the first key. Shows WPM (correct characters / 5 per minute) and accuracy. |

Super Admins can switch tools on or off, rename them, change the description and order (Admin > Tools).

### 5.10 Contact Us (`/contact`)

* Form: name, email, subject (optional), message. Limits: name 100, email 150, subject 150, message 3000 characters.
* A hidden "website" field is a **spam trap**: if a bot fills it, the message is silently dropped.
* Messages appear in Admin > Messages. The visitor sees "Thanks, your message was sent."
* The page also lists your email, phone, address and Telegram link (if set).

### 5.11 Email alerts box

* Stores the email address in Admin > Subscribers. Duplicates are ignored.
* **Emails are not sent yet.** Only collection is built (see section 18).

### 5.12 Basic pages (`/p/{slug}`)

About Us (`about`), Privacy Policy (`privacy-policy`), Disclaimer (`disclaimer`), Terms (`terms`), FAQs (`faq`). Admins can edit, hide, delete or add pages. Inside the text you can use placeholders that fill from `site.properties`:

`{site}` `{domain}` `{email}` `{phone}` `{address}`

The default Privacy Policy and Terms are **templates**. Read and adjust them before launch.

### 5.13 Error page

Any missing page or error shows a friendly page with links to Home and Search. Details of the error are never shown to visitors.

### 5.14 Other front-end features

| Feature | Notes |
|---|---|
| Cookie notice | Bar at the bottom until the visitor presses OK (remembered in the browser). Turn off with `site.cookie-notice=false`. |
| Back to top | Appears after scrolling 600 px. |
| Print | The Print button (and Ctrl+P) hides header, footer and buttons for a clean printout. |
| Accessibility | Skip link, visible keyboard focus, labelled buttons and inputs, `aria-current` on the active menu item. |
| Responsive | Phone bottom tab bar, swipe strips, full-width search, tables scroll sideways. |
| Fonts | Inter from Google Fonts with system fallback. |

---

## 6. The admin panel

Open `/admin`. Log in with email and password. After login you land on the Dashboard.

### 6.1 Screen layout

* **Left sidebar** (top strip on phones):
  * Dashboard, All posts, + New post
  * **Sections**: every section with its post count; click to list only those posts
  * **Content**: Media
  * **Inbox**: Messages (red badge = unread)
  * **Manage** (Super Admin only): Manage sections, Pages, Tools, Trending, Subscribers, Users and roles, Page hits and logs, Site settings
* **Top bar**: View site (new tab), your avatar, name and role, My profile, Logout.
* **Hover tips**: every button and link shows a short explanation when you hover.
* Green or red message strips show the result of your last action.

### 6.2 Login, logout, lockout

* Login at `/admin/login`. Logout is the button in the top bar (a secure POST).
* **5 wrong passwords** for the same email within 10 minutes lock that account for the rest of the 10 minutes. The login page shows the same generic error message.
* Disabled users cannot log in.
* Last login time is stored and shown in Users and roles.

### 6.3 Dashboard (`/admin`)

* Counters: Published, Drafts, Unpublished, Unread messages, Subscribers (Super Admin).
* Buttons: + New post, All posts.
* **Most viewed**: top 5 published posts.
* **Closing in 7 days**: posts whose last date is near.
* **Published posts per section**: a small bar chart.
* **Drafts and unpublished posts**: the 8 most recently edited, with Edit buttons.
* **Recent activity** (Super Admin): the last 10 actions of all users.

### 6.4 All posts (`/admin/posts`)

* **Status chips** with counts: All, Published, Draft, Unpublished.
* **Search** by title and **section filter**.
* 15 posts per page with numbered pagination.
* Columns: Title, Section, Status, Updated, By.
* **Row buttons**

| Button | What it does |
|---|---|
| Edit | Opens the editor |
| Preview | Shows the post exactly as visitors will see it (even drafts), in a new tab |
| Duplicate | Creates a **draft** copy (title gets "(copy)", views reset, all fees, extras, stages and vacancies copied). Useful for next year's notification |
| Unpublish | Hides a live post (status Unpublished) |
| Publish | Makes a non-live post live. Works only if the post was verified before (all 5 checks ticked at least once) and has a section; otherwise you are told to open the editor |
| Delete | Super Admin only. Deletes for good after a confirmation |

### 6.5 Post editor (`/admin/posts/new`, `/admin/posts/{id}/edit`)

Full guide in section 8.

### 6.6 Media (`/admin/media`)

* Upload **png, jpg, jpeg, gif or webp** images up to 5 MB. The app checks both the file extension and that the file is an image.
* Files are saved in `./uploads` with a random name and served at `/uploads/<name>`.
* Each image shows a thumbnail, its address and a **Copy address** button. Super Admins can delete images.
* To use an image in a post: upload it here, copy the address, press the **Image** button in the editor toolbar and paste the address.

### 6.7 Manage sections (`/admin/sections`) (Super Admin)

A **section** is a menu item and list page (Latest Jobs, Admit Card...). Each section card has:

| Field | Meaning |
|---|---|
| Name | Shown on the site |
| Slug | The address part: `/s/<slug>`. Letters, numbers and hyphens only |
| Kind | RECRUITMENTS, STAGE_LIST or POSTS. For now this is a label for you; it does not change behaviour |
| Shown in | **Bottom tab** (also top menu, home cards and hero counts) or **More menu**. Use at most 3 or 4 bottom tabs |
| Order | Smaller number comes first |
| Visible | Untick to hide the section from the site (posts stay) |

* **Add a new section** with the form below the list. It appears on the site automatically. No code change.
* A section can be **deleted only when it has no posts** (otherwise hide it).
* The slugs `jobs`, `blog` and `faq` (a page) are used in a few places: Trending's "All jobs" link points to `/s/jobs`, and the home "From the Blog" box reads the section with slug `blog`.

### 6.8 Pages (`/admin/pages`) (Super Admin)

List, add, edit, hide or delete basic pages (About, Privacy Policy...). Fields: Title, Slug, Content (HTML), Visible, Show link in footer. Placeholders `{site}`, `{domain}`, `{email}`, `{phone}`, `{address}` fill from `site.properties`. The Contact Us page is built in (`/contact`) and is not part of this list.

### 6.9 Tools (`/admin/tools`) (Super Admin)

Each tool card: Name, Description, Order and "Visible on the site". The address (slug) cannot be changed. Adding a brand-new tool needs code (see 15.8).

### 6.10 Trending (`/admin/trending`) (Super Admin)

Lists the top 20 published posts (pinned first, then by views). **Pin** keeps a post at the top of "Trending now"; **Unpin** returns it to automatic order. The count of items shown on the site is `site.trending-count`.

### 6.11 Messages (`/admin/messages`)

All Contact Us messages, newest first, with name, email (click to reply), date, subject and text. **Mark handled** / **Mark as new** toggles the status. The sidebar badge counts messages not yet handled.

### 6.12 Subscribers (`/admin/subscribers`) (Super Admin)

List of collected emails with date, and **Download CSV** (`email,subscribed`).

### 6.13 Users and roles (`/admin/users`) (Super Admin)

* Table: Name, Email, Role, Status, Last login.
* **Disable / Enable** a user (you cannot disable yourself).
* **Reset** a user's password (minimum 8 characters).
* **Add a user**: name, email, password (minimum 8 characters), role (Editor or Super Admin).
* **Recent activity** table (who did what and when).

### 6.14 Page hits and logs (`/admin/logs`) (Super Admin)

See section 11.

### 6.15 Site settings (`/admin/settings`) (Super Admin)

Read-only view of the values currently loaded from `site.properties`, plus links to `sitemap.xml` and `robots.txt`. To change a value, edit the file and restart.

### 6.16 My profile (`/admin/profile`)

Shows your name, email and role. **Change password**: enter current password and a new one (minimum 8 characters).

### 6.17 Activity log

Every important admin action is recorded (saved or published a post, unpublish, duplicate, delete, section/page/tool changes, pins, user changes, password resets). Super Admins see the latest 10 on the Dashboard and in Users and roles.

---

## 7. Roles and permissions

There are two roles.

| Action | Editor | Super Admin |
|---|:--:|:--:|
| Log in, Dashboard, My profile, change own password | yes | yes |
| See all posts, search, filter | yes | yes |
| Create, edit, save drafts | yes | yes |
| Publish, unpublish | yes | yes |
| Preview, duplicate | yes | yes |
| Upload images (Media) | yes | yes |
| Read and handle Messages | yes | yes |
| **Delete a post** | no | yes |
| **Delete an image** | no | yes |
| Manage sections, pages, tools | no | yes |
| Trending pins | no | yes |
| Subscribers and CSV | no | yes |
| Users and roles (create, disable, reset password) | no | yes |
| Page hits and logs, activity log, site settings | no | yes |

The rules are enforced **on the server**. An Editor who types a Super Admin address gets "403 Forbidden"; hiding menu items is only for convenience.

---

## 8. Writing a post (editor guide)

### 8.1 The idea

A post is a flexible page. Only a **title** and a **section** are required. Everything else is optional, so each notification can have exactly the fields it needs (for example different fees for different categories). Blocks you leave empty simply do not appear on the site.

### 8.2 Status life cycle

```
New post --Save draft--> DRAFT --Publish (5 checks)--> PUBLISHED --Unpublish--> UNPUBLISHED
                           ^                                |                       |
                           +---------- Save changes --------+----- Publish --------+
```

* **Draft**: only admins can see it (use Preview).
* **Published**: live on the site, in lists, search, sitemap and RSS.
* **Unpublished**: taken down; can be published again.

### 8.3 Editor sections

**1. Basics**

| Field | Notes |
|---|---|
| Title | Required |
| Slug | Part of the address (`/post/<slug>`). Leave empty to create from the title. Lowercase letters, numbers, hyphens. If taken, `-2`, `-3`... is added. Changing it later changes the post's address |
| Section | Required to publish |
| Filter tags | Comma separated, for example `Graduate, Police, UP`. Used by the filter chips |
| Organisation | For example UP Police Recruitment Board |
| Total posts | Text, so you can write `2,606` or `Eligibility test` |
| Last date | Date picker. Drives "Apply open", "Closed", "Closing soon" and "N days left" |
| Short summary | Up to 500 characters. Used for search-result text, sharing and RSS |

**2. Application fee by category**: one line per category.

```
Category | Amount | Note
General / OBC / EWS | 400 | Online payment
SC / ST | 200
Female (all categories) | 100
```
Amount is shown with a ₹ sign. The note is optional.

**3. Extra details**: any label and value.

```
Mode of exam | Offline, OMR sheet
Exam centres | All districts of UP
Age limit | 21-28 years
```

**4. Posts and vacancy**: one line per post. Separate columns with `|`, a comma or a tab (so you can paste straight from Excel). Total is calculated automatically.

```
Post | UR | OBC | SC | ST | EWS
Sub-Inspector (Civil Police) | 1100 | 594 | 396 | 40 | 220
Platoon Commander (PAC) | 120 | 65 | 43 | 4 | 24
```

**5. Stages**: one line per stage. This is what lets different posts of one notification move at different speeds.

```
Stage | Applies to | Date or text | Status | Link
Notification | All posts | 18 Aug 2026 | Done
Admit card | Sub-Inspector (Civil Police) | 20 Nov 2026 | Live | https://official-site/admit
Admit card | Platoon Commander (PAC) | Not yet | Upcoming
```

* **Applies to**: write `All posts` or the exact post name from the vacancy list. Empty means All posts.
* **Status**: `Done`, `Live` or `Upcoming`. Empty means Upcoming.
* **Date or text**: free text ("Now", "Not yet", "Out now", a date).
* **Link**: optional; shown as "Open link" on the page.
* The order of lines is the order on the page. Put them in the real order of the process.

**6. Your write-up**: the main text. **HTML is allowed.** The toolbar inserts HTML around your selected text:

| Button | Inserts |
|---|---|
| H2 | A heading |
| Bold | Bold text |
| List | A bullet list |
| Link | A link (type the address between the quotes) |
| Image | An image tag (upload the image first in Media) |
| Table | A table with a header row |
| Note | A highlighted note line |

**7. Manual verification**: five checkboxes.

1. Matched with the official notification PDF
2. Dates double-checked
3. Fee amounts checked category by category
4. Links open the official site
5. Preview checked on mobile and desktop

The **Publish** button stays disabled until all five are ticked. The server checks this too.

### 8.4 Buttons at the bottom

| Button | Result |
|---|---|
| Save draft (or Save changes if already live) | Saves. A new post becomes a Draft; a live post stays live |
| Publish (or Save and keep published) | Saves and makes it live, **only if** a section is chosen and all 5 checks are ticked. Otherwise it is saved without publishing and a red message explains why |
| Preview (top right) | Opens the post as visitors will see it |
| Unpublish (live posts only) | Takes the post down |

On the first publish the **published date** is set. Later edits do not change it. "Verified by" shows the name of the person who published.

### 8.5 A good daily workflow

1. **New notification arrives** on the official site. Open the previous year's post, press **Duplicate**, change title, dates, vacancy, fees. Fill stages. Add your write-up.
2. Check against the official PDF. Tick the five boxes. Preview on phone and desktop. **Publish**.
3. **When the admit card is released**: open the post, change that stage's Status to `Live` (and add the link), update the earlier stage to `Done`, **Save changes**. The home page, lists, Trending and the status chip all update by themselves.
4. When the result comes, repeat step 3.
5. Use **Unpublish** instead of delete when a post should disappear for a while.

---

## 9. Data model

Tables are created automatically (`ddl-auto=update`).

| Table | Purpose | Main fields |
|---|---|---|
| `app_user` | Admin users | name, email (unique), password hash (BCrypt), role (EDITOR / SUPER_ADMIN), enabled, last login |
| `section` | Menu sections | name, slug (unique), kind, placement (TAB / MORE), sort order, visible |
| `post` | All posts | title, slug (unique), section, status, organisation, total posts, last date, tags, summary, body (text), views, pinned, verified, verified by, author, created / updated / published time |
| `post_fee` | Fee rows | post, category, amount, note |
| `post_extra` | Extra details | post, label, detail |
| `post_stage` | Stages | post, name, applies to, when text, state, link |
| `post_vacancy` | Vacancy rows | post, post name, ur, obc, sc, st, ews |
| `tool` | Tools | name, slug (unique), description, enabled, sort order |
| `static_page` | Basic pages | title, slug (unique), body, visible, in footer |
| `contact_message` | Contact form | name, email, subject, message, created, handled |
| `subscriber` | Email alerts | email (unique), created |
| `activity_log` | Admin actions | who, action, time |

Relations: a post belongs to one section and has one author; fees, extras, stages and vacancies belong to a post (and are deleted with it).

---

## 10. Data files: sample posts and mock tests

### 10.1 `demo-data.txt` (sample posts)

* Loaded on first start when `site.seed-demo=true` and the database has no posts.
* One post per line, 14 columns separated by `|`. Lines starting with `#` are ignored. All names, numbers and dates are made up.
* The shipped file has about 124 posts: Latest Jobs 40, Admit Card 18, Result 18, Blog 13, Answer Key 9, Syllabus 9, Admission 9, Scheme 8. About 10% are drafts or unpublished so you can test the admin.

| # | Column | Notes |
|---|---|---|
| 1 | section slug | `jobs`, `admit-card`, `result`, `syllabus`, `admission`, `answer-key`, `scheme`, `blog` |
| 2 | title | |
| 3 | organisation | |
| 4 | filter tags | comma separated |
| 5 | total posts text | |
| 6 | last date as days from today | blank = none, negative = already closed |
| 7 | status | `PUBLISHED`, `DRAFT` or `UNPUBLISHED` |
| 8 | views | number (used by Trending) |
| 9 | published N days ago | number |
| 10 | fees | `Category~Amount~Note;Category~Amount~Note` |
| 11 | extras | `Label~Detail;Label~Detail` |
| 12 | vacancies | `Post~UR~OBC~SC~ST~EWS;Post~UR~OBC~SC~ST~EWS` |
| 13 | stages | `Stage~Applies to~When~State~Link;...` |
| 14 | summary | |

Use `{d-7}` or `{d+10}` inside text for a date 7 days ago or 10 days ahead, so sample data always looks fresh.
Bad lines are skipped and a warning is written to the log (line number and reason).

To use your own copy without rebuilding, put `demo-data.txt` next to the jar (or set `site.demo-file`).

### 10.2 `mock-tests.txt`

```
@test slug | Title | minutes | short description
Question text | Option A | Option B | Option C | Option D | correct letter (A-D) | short explanation
```

* A line starting with `@test ` starts a new test; the lines after it are its questions.
* Exactly four options. The correct letter is A, B, C or D. The explanation is optional.
* Do not use the `|` character inside text. Lines starting with `#` are ignored.
* Read at every start. A `mock-tests.txt` next to the jar overrides the built-in file.

---

## 11. Logging and page tracking

### 11.1 Request log

Every page request writes one line, to the console and to `./logs/sarkari.log`:

```
2026-11-26 10:15:02 INFO  PAGE  GET /s/jobs?sort=views -> 200 (14 ms) user=- ip=203.0.113.5
2026-11-26 10:15:09 WARN  PAGE  GET /old-page -> 404 (2 ms) user=- ip=203.0.113.5
```

* Levels: 2xx and 3xx = INFO, 4xx = WARN, 5xx = ERROR.
* `user` is the admin's email when logged in, `-` for visitors.
* CSS, JavaScript, images, favicon and `/error` are skipped to keep the log readable.
* The file rolls over daily and at 10 MB; the last 14 days are kept.
* Other loggers: `LOGIN` (login ok / failed), `com.sarkarisetu` (startup, seeding, uploads).
* Control the level with `logging.level.PAGE` (INFO, WARN, DEBUG) in `application.properties`.

### 11.2 "Page hits and logs" screen (`/admin/logs`, Super Admin)

Counters live in memory and start from zero at every restart (the log file keeps the history).

* **Cards**: total requests, known pages (routes), pages never called, missing pages (404).
* **All pages table**: every page pattern the app knows (for example `/post/{slug}`, `/s/{slug}`, `/admin/posts`), with hits, errors, average milliseconds, last status and last call time. A **Never called** tag marks routes nobody has opened yet, so you can see what is unused.
* **Missing pages (404)**: addresses people asked for that do not exist, with counts. Useful for fixing broken links.
* **Last 100 requests**: newest first.

---

## 12. Security

| Topic | What is in place |
|---|---|
| Passwords | Stored as BCrypt hashes |
| Login | Form login, session timeout 60 minutes, lock for 10 minutes after 5 wrong passwords |
| Roles | Enforced on the server for every admin URL (section 7) |
| CSRF | Every form posts a CSRF token (Spring Security default); logout is a POST |
| Clickjacking | Frames allowed from the same site only |
| Admin pages | `noindex, nofollow` and blocked in `robots.txt` |
| Uploads | Only png, jpg, jpeg, gif, webp, checked by extension and content type, random file names, 5 MB limit |
| Contact form | Length limits and a hidden spam-trap field |
| Error pages | No internal details are shown |
| Page logs | IP address and logged-in email are written to the log file; mention logging in your Privacy Policy |

**Things you must do yourself**

* Change the default admin password and email before going live.
* Use HTTPS in production (section 16).
* Post bodies and pages accept **HTML** and are shown as written. Only give accounts to people you trust.
* Back up the database and `./uploads`.

---

## 13. SEO and sharing

| Feature | Where |
|---|---|
| Page title and description | Every page (`<title>`, meta description from the post summary or site tagline) |
| Canonical link | Every page (uses `site.base-url`) |
| Open Graph and Twitter tags | Title, description, site name, URL and optional image (`site.og-image`) |
| JSON-LD | `WebSite` with search action on the home page, `Article` on each post |
| `sitemap.xml` | Home, Contact, Mock Tests list and tests, Tools, sections, footer pages, all published posts |
| `robots.txt` | Blocks `/admin/` and `/search`, points to the sitemap |
| `feed.xml` (RSS) | Latest 30 published posts |
| Search results and Saved pages | `noindex` |
| Favicon | `static/favicon.svg` (replace with your logo) |

After you set your real domain in `site.base-url`, submit `https://your-domain/sitemap.xml` in Google Search Console.

---

## 14. URL reference

### 14.1 Public

| Method | Address | Page |
|---|---|---|
| GET | `/` | Home |
| GET | `/s/{slug}?page=&tag=&sort=` | Section list |
| GET | `/post/{slug}` | Post |
| GET | `/search?q=&page=` | Search results |
| GET | `/search/suggest?q=` | Search suggestions (JSON) |
| GET | `/more` | More menu |
| GET | `/saved` | Saved posts |
| GET | `/mock-tests` | Mock test list |
| GET | `/mock-tests/{slug}` | A mock test |
| GET | `/tools` | Tools list |
| GET | `/tools/{slug}` | A tool |
| GET | `/p/{slug}` | Basic page |
| GET / POST | `/contact` | Contact form |
| POST | `/subscribe` | Email alerts |
| GET | `/sitemap.xml`, `/robots.txt`, `/feed.xml` | SEO files |
| GET | `/uploads/{file}` | Uploaded image |

### 14.2 Admin (all under `/admin`)

| Method | Address | Who | Purpose |
|---|---|---|---|
| GET | `/admin/login` | anyone | Login page |
| POST | `/admin/login`, `/admin/logout` | anyone / logged in | Log in and out |
| GET | `/admin` | Editor, SA | Dashboard |
| GET | `/admin/posts` | Editor, SA | Post list |
| GET | `/admin/posts/new`, `/admin/posts/{id}/edit` | Editor, SA | Editor |
| POST | `/admin/posts/save` | Editor, SA | Save or publish |
| POST | `/admin/posts/{id}/publish`, `/unpublish`, `/duplicate` | Editor, SA | Status and copy |
| POST | `/admin/posts/{id}/delete` | SA | Delete |
| GET | `/admin/posts/{id}/preview` | Editor, SA | Preview |
| GET / POST | `/admin/media`, `/admin/media/upload` | Editor, SA | Images |
| POST | `/admin/media/delete` | SA | Delete image |
| GET | `/admin/messages` | Editor, SA | Contact messages |
| POST | `/admin/messages/{id}/handled` | Editor, SA | Mark handled |
| GET / POST | `/admin/profile`, `/admin/profile/password` | Editor, SA | Own account |
| GET / POST | `/admin/sections`, `/sections/save`, `/sections/{id}/delete` | SA | Sections |
| GET / POST | `/admin/pages`, `/pages/new`, `/pages/{id}/edit`, `/pages/save`, `/pages/{id}/delete` | SA | Basic pages |
| GET / POST | `/admin/tools`, `/tools/save` | SA | Tools |
| GET / POST | `/admin/trending`, `/trending/{id}/pin` | SA | Pins |
| GET | `/admin/subscribers`, `/subscribers/export.csv` | SA | Subscribers |
| GET / POST | `/admin/users`, `/users/save`, `/users/{id}/toggle`, `/users/{id}/password` | SA | Users |
| GET | `/admin/logs` | SA | Page hits and logs |
| GET | `/admin/settings` | SA | Site settings |

SA = Super Admin.

---

## 15. How-to guides

### 15.1 Change the domain and site details
Edit `site.properties` (`site.base-url`, `site.name`, `site.contact-email`, social links), then restart.

### 15.2 Add a new section (for example Scholarship)
Admin > Manage sections > Add a new section: name, kind POSTS, shown in "More menu", save. It appears in More and as `/s/scholarship`. To show it on the home page add its slug to `site.home-sections`. To make it a bottom tab, set "Shown in" to Bottom tab (keep 3 or 4 tabs).

### 15.3 Publish a recruitment
See section 8.5.

### 15.4 Add a basic page (for example Refund Policy)
Admin > Pages > + New page. Add text, keep "Show link in the footer" ticked. It is available at `/p/<slug>`.

### 15.5 Change colours and design
Open `static/css/style.css`. Colours are CSS variables at the top (`--acc` main colour, `--bg`, `--card`, `--ink`...). The dark theme variables are in the `@media (prefers-color-scheme:dark)` block and the `[data-theme="dark"]` block. The header gradient and hero gradient are near the bottom of the file.

### 15.6 Design the recruitment post page yourself
Edit `templates/post.html`. Keep the `th:` attributes you need; the list of available fields is in the comment at the top. Use `th:text` for text and `th:utext` for the HTML body. Spring reloads templates without restart while `spring.thymeleaf.cache=false`.

### 15.7 Add or change a mock test
Edit `mock-tests.txt` (format in 10.2), restart.

### 15.8 Add a new tool
A new tool needs code:
1. Create `templates/tools/<slug>.html` (copy an existing tool as a model) and `static/js/tools/<slug>.js`.
2. Add a `Tool` row: for a fresh database add a line in `DataSeeder` (where the four tools are created); for an existing database insert one row in the `tool` table (name, slug, description, enabled, sort_order).
3. Restart. Admin > Tools can then rename, reorder or hide it.

### 15.9 Replace the sample posts with real ones
1. Set `site.seed-demo=false` in `site.properties`.
2. Delete the `./data` folder once (to remove sample posts) and start; or delete the sample posts one by one in Admin (Super Admin).
3. Write real posts.

### 15.10 Move from H2 to MySQL
See 4.4. Existing H2 data is not copied automatically; start fresh or export and import it yourself.

### 15.11 Back up
* H2: stop the app and copy `./data` and `./uploads`.
* MySQL: `mysqldump sarkari > backup.sql` and copy `./uploads`.
* Also keep your `site.properties`, `demo-data.txt` and `mock-tests.txt`.

### 15.12 Reset a forgotten admin password
There is no "forgot password" page yet. Another Super Admin can reset it in Users and roles. If nobody can log in: stop the app, delete `./data`, set the admin email/password in `site.properties`, start again (this erases content, so only do this on a new site) or update the `password_hash` column in the `app_user` table with a BCrypt hash.

---

## 16. Deployment

1. **Build**: `mvn clean package` creates `target/sarkari-setu-0.1.0.jar`.
2. **Run**: `java -jar target/sarkari-setu-0.1.0.jar` (in a folder that contains your `site.properties` override, `./data`, `./uploads`, `./logs`).
3. **Production settings**
   * `spring.thymeleaf.cache=true`
   * `site.base-url=https://your-domain`
   * Strong admin password; `site.seed-demo=false`
   * MySQL for a real site
   * If you run behind Nginx or a similar proxy, add `server.forward-headers-strategy=framework` so the real client IP and https are used.
4. **HTTPS**: put Nginx (or Caddy) in front, with a free Let's Encrypt certificate, and proxy to `localhost:8080`.
5. **Keep it running**: create a systemd service (or a Docker container) that restarts the jar after a crash or reboot.
6. **Check**: open `/sitemap.xml`, `/robots.txt`, `/feed.xml`, log in to `/admin`, and look at Page hits and logs.

---

## 17. Troubleshooting

| Problem | Likely cause and fix |
|---|---|
| `mvn` cannot download libraries | No internet or a proxy. Check the network, then run again |
| Port 8080 already in use | Change `server.port` or stop the other program |
| Compile error after copying the project | Send the exact message. The project was not compiled where it was written |
| Cannot log in | Wrong password, disabled account, or **locked for 10 minutes** after 5 failures. Wait, or ask another Super Admin to reset the password |
| Admin pages show 403 | Your role is Editor and the page is Super Admin only |
| Sample posts did not appear | The database already had posts, or `site.seed-demo=false`. Stop, delete `./data`, start |
| "Database may be already in use" (H2) | Two copies of the app use the same `./data`. Stop the other one |
| Uploaded image shows broken | The app runs from a different folder than the one that holds `./uploads`. Start it from the same folder every time |
| A post is not visible on the site | Its status is not Published, or no section is chosen. Check Admin > All posts |
| Publish button is disabled | Tick all five verification checks |
| "Not published: choose a section..." | A section was not selected, or a check was missing |
| Home page shows fewer sections | A section is hidden, or its slug is not in `site.home-sections` |
| Search finds nothing | Search looks only in titles of published posts |
| Changes to a template do not show | Hard-refresh the browser; check `spring.thymeleaf.cache=false` in development |
| Wrong domain in sitemap or share links | Set `site.base-url` and restart |
| Log file is missing | It is created at `./logs/sarkari.log` after the first request |
| Template error page (500) | Look in `logs/sarkari.log` for the line with `ERROR` and the template name |

---

## 18. Limits and what is not built yet

**Not built**

* Automatic fetching of new notifications from official websites.
* Telegram or WhatsApp auto-posting.
* **Sending emails** to subscribers (addresses are collected only). No notification to people who "follow" a recruitment.
* Hindi (or any second language) content and language switch.
* "Forgot password" by email.
* Scheduled publishing (publish at a future time).
* Unique-visitor counting and long-term analytics (use Google Analytics through `site.analytics-id`).

**Current limits**

* Trending uses **total** views (not "last 24 hours").
* Page-hit counters are in memory and reset on restart (the log file keeps history).
* The admin posts list loads all posts to filter them. Fine for thousands of posts, slow for a very large site.
* Mock test answers are visible in the page source (practice only). Scores are not saved.
* H2 is a single-process database; use MySQL for a busy site.
* Section "Kind" is a label only.
* New tools need code (15.8).
* Uploaded images are stored on the server disk (`./uploads`), not in the database.
* Privacy Policy and Terms are templates and are not legal advice.

**Ideas for later**: email and Telegram alerts when a stage changes, notification fetcher with a review queue, Redis-based "last 24 hours" trending, scheduled publishing, image resizing on upload, Hindi content.

---

## 19. Glossary

| Word | Meaning |
|---|---|
| Section | A menu item and list page (Latest Jobs, Result...) |
| Post | One recruitment, admit card, result, blog article and so on |
| Stage | One step of a recruitment (Notification, Apply online, Admit card, Exam, Result) |
| Slug | The address part made from a name, for example `up-police-si-2026` |
| Draft | A post not visible to visitors |
| Published | A post visible to visitors |
| Unpublished | A post that was live and then taken down |
| Verified | All five manual checks were ticked |
| Pin | Force a post to stay at the top of Trending |
| Badge / chip | The small coloured label showing the current status |
| Editor | Role that writes and publishes posts |
| Super Admin | Role that manages everything |
| Seed / sample data | Data created automatically on the first start |
| H2 | The small built-in database used by default |
| Thymeleaf | The template engine that builds the pages on the server |

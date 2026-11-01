# CraveDrop

CraveDrop is a Java web app for discovering Bengaluru restaurants, browsing menus, and placing food-delivery orders. It includes customer accounts, restaurant-specific menus, a shopping cart, wishlists, order history, and admin tools for managing delivery.

![CraveDrop food delivery](src/main/webapp/assets/site/hero-v2.jpg)

## Features

- Browse and search a catalog of **30 restaurants and 180 dishes**, with cuisine, dietary, rating, and delivery-time filters.
- Register, sign in, reset passwords, and update account details.
- Add dishes to a persistent wishlist and build a cart for one restaurant at a time.
- Check out with an itemized total, then view order history and delivery status.
- View customer and admin dashboards; admins can manage delivery partners, assignments, and order status.
- Browse locally bundled restaurant and dish photography.

## Tech stack

| Component | Technology |
| --- | --- |
| Language | Java 17 |
| Web | Jakarta Servlet 6.0, JSP, JSTL |
| Application server | Apache Tomcat 10.1 |
| Database | MySQL 8 |
| Build | Maven |

## Run locally

### Prerequisites

- JDK 17
- Apache Maven
- MySQL Server 8
- Apache Tomcat 10.1

Tomcat 10.1 is required for the Jakarta Servlet 6.0 APIs used by the app.

### 1. Get the code and create the database

```bash
git clone https://github.com/nisargatk21/CraveDrop-.git
cd CraveDrop-
```

In MySQL Workbench, open and execute [`db/schema.sql`](db/schema.sql). This creates the `cravedrop` database and its tables.

### 2. Configure database access

The app reads database settings from environment variables. Defaults are intended for a local development database:

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/cravedrop` | MySQL JDBC connection URL |
| `DB_USER` | `root` | MySQL username |
| `DB_PASS` | *(empty)* | MySQL password |

Set these variables in the environment used to start Tomcat. For example, configure them in your IDE's Tomcat launch settings, then restart the server.

### 3. Build and deploy

Build the WAR from the project root:

```bash
mvn package
```

The packaged app is `target/cravedrop.war`. Copy it to Tomcat's `webapps` directory and start Tomcat. Open:

```text
http://localhost:8080/cravedrop/app/home
```

The first request to a restaurant menu imports or updates the bundled catalog from `src/main/resources/catalog/`.

## Optional: run from Eclipse

1. Import the folder containing `pom.xml` using **File → Import → Maven → Existing Maven Projects**.
2. Configure an **Apache Tomcat 10.1** server and add the `CraveDrop_FoodDeliver` project.
3. Set `DB_URL`, `DB_USER`, and `DB_PASS` in the Tomcat launch configuration if your MySQL credentials differ from the defaults.
4. Start Tomcat and visit `http://localhost:8080/cravedrop/app/home`.

## Admin access

Admin privileges must be granted manually; public sign-up cannot create an admin account.

1. Register an account through the app.
2. In `db/admin-setup.sql`, replace **both** occurrences of the example email with that account's email.
3. Execute the script in MySQL Workbench and sign in again.
4. Open **Profile → Admin dashboard**, or visit `/cravedrop/app/admin`.

Admins can review dashboard totals, add delivery partners, assign partners to open orders, and advance order status.

## Email, catalog, and demo behavior

- **Password reset:** without SMTP settings, reset links are printed to the Tomcat log for local development. To send email, set `SMTP_HOST`, `SMTP_PORT` (defaults to `587`), `SMTP_USER`, and `SMTP_PASS` in Tomcat's environment.
- **Catalog:** the 30 restaurants and 180 restaurant-specific dishes are bundled demo data. Prices, ratings, and delivery times are not live restaurant information.
- **Checkout:** the app calculates 5% GST, a ₹40 delivery fee, and an ₹8 platform fee. Payment options are demonstrations only; no payment gateway is connected.
- **Images:** app imagery is bundled locally. Refer to [`src/main/webapp/IMAGE_CREDITS.md`](src/main/webapp/IMAGE_CREDITS.md) for image sources and licenses.

## Troubleshooting

- **Database connection fails:** confirm MySQL is running, `db/schema.sql` has been executed, and the `DB_*` values are available to the Tomcat process.
- **404 or servlet class errors:** confirm the deployed context is `cravedrop`, redeploy `target/cravedrop.war`, and check the Tomcat logs.
- **Jakarta/Servlet API errors:** use Tomcat 10.1, not Tomcat 9.
- **Java compilation errors:** confirm Maven is using JDK 17.

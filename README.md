# CraveDrop — Eclipse run folder

**Eclipse project name:** `CraveDrop_FoodDeliver`  
**Web application context:** `cravedrop`  
**Java:** 17 · **Servlet/JSP:** Jakarta Servlet 6.0 · **Server:** Tomcat 10.1 · **Database:** MySQL 8 · **Build:** Maven

This folder contains the complete Eclipse/Maven project: Java source, JSP/CSS, the 30-restaurant / 180-menu catalog, database schema, and Eclipse metadata. Menu catalog rows are imported into MySQL automatically on the first restaurant-menu request. The local `eclipse-pom.xsd` avoids Eclipse attempting to fetch an external POM schema; Maven remains responsible for parsing and building the actual project model.

## Before importing

Install or have available:

- JDK 17 (the source and Maven release remain compatible with Java 17)
- Eclipse IDE for Enterprise Java and Web Developers (includes Maven and Tomcat web tooling)
- Apache Tomcat 10.1
- MySQL Server 8.x and MySQL Workbench

## 1. Create the database

Start MySQL. In MySQL Workbench, open `db/schema.sql`, connect to your local MySQL server, and click **Execute** (lightning bolt). This creates the `cravedrop` database and its tables.

The app defaults to `jdbc:mysql://localhost:3306/cravedrop`, MySQL user `root`, and a blank password. If your MySQL account uses a password, set these variables for the Eclipse Tomcat server process:

| Variable | Example |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/cravedrop` |
| `DB_USER` | `root` |
| `DB_PASS` | your MySQL password |

In Eclipse, open the **Servers** view, double-click your Tomcat server, choose **Open launch configuration**, then add the variables under **Environment**. Restart Tomcat after changing them.

## 2. Import into Eclipse

1. Unzip this folder somewhere convenient, for example `C:\dev\CraveDrop_FoodDeliver`.
2. In Eclipse, choose **File → Import → Maven → Existing Maven Projects**.
3. Set **Root Directory** to the unzipped `CraveDrop_FoodDeliver` folder (the one containing `pom.xml`).
4. Select the project and click **Finish**. Eclipse displays its project name as `CraveDrop_FoodDeliver`.
5. If Maven asks to download dependencies, allow it. If needed, right-click the project and choose **Maven → Update Project**.
6. If Eclipse still shows a Java Resources error, right-click the project, choose **Maven → Update Project**, select **Force Update of Snapshots/Releases**, then choose **Project → Clean**. In **Properties → Java Build Path → Libraries**, check that a `JRE System Library` and `Maven Dependencies` are present. The project uses Eclipse's workspace default JRE; Maven compiles against Java 17 APIs. Use **Window → Show View → Problems** to read any remaining marker details.

## 3. Set up Tomcat and run

1. In **Window → Show View → Servers**, choose **New → Server → Apache → Tomcat v10.1 Server**.
2. Point Eclipse to your Tomcat 10.1 installation and select the configured JDK 17 runtime (the project source and Maven release remain Java 17 compatible).
3. Add project `CraveDrop_FoodDeliver` to the server and finish the wizard.
4. Right-click the server and choose **Start** (or **Run**).
5. Open **http://localhost:8080/cravedrop/app/home**.

The project uses Jakarta Servlet 6.0, so Tomcat 10.1 is required. The first Maven build may need internet access to download libraries from the project’s `pom.xml`.

## Dashboards and administrator access

After signing in, open **Profile → Customer dashboard** to see order count, delivered orders, total spend, and the three most recent orders. These figures come from that customer's saved MySQL orders. The home page now includes a menu-inspired section with six different signature dishes from six different restaurants, followed by cuisine/category shortcuts and the restaurant directory.

The **Admin dashboard** is only shown to accounts whose database role is `admin`. To create one, first register an account through the normal sign-up form, edit the email placeholder in `db/admin-setup.sql`, then execute that script in MySQL Workbench. Sign in again and use **Profile → Admin dashboard** or open `/app/admin`. The dashboard reads customer/owner totals, menu-item and order counts, in-progress orders, saved order revenue, and recent customer accounts from MySQL. It lists the bundled restaurants and unique dish catalog, lets admins add delivery partners and assign available partners to open orders, and lets admins update delivery steps through Placed → Preparing → Out for delivery → Delivered. Customers see the new status in their order history. Existing databases automatically receive the delivery-partner table and nullable assignment column when the admin dashboard is first opened. Regular sign-up cannot create an administrator account.

The login, sign-up, password reset, reset-link, and change-password screens each use their own locally bundled food-photo background. All account pages continue to work without external image hosts.

## Catalog and email

The restaurant and dish catalog is in `src/main/resources/catalog/`. It contains 30 restaurants and 180 restaurant-specific menu items (6 per restaurant), grouped into food categories. The app seeds or updates those rows in the `menu_items` table automatically; retired catalog rows are hidden, and repeated starts do not create duplicate active dishes. Menu pricing, ratings and delivery times are bundled portfolio-demo values and are not connected to live restaurant feeds.

The project includes 30 distinct, original AI-generated restaurant-cover photos, 180 distinct dish photos, and locally bundled site/account food backgrounds under `src/main/webapp/assets/`. Restaurant covers are cuisine-matched and contain no third-party logos or brand marks. The homepage hero is an original AI-generated food photograph with no logos or brand marks. All are bundled locally, so the app does not rely on image hotlinks at runtime. Sources, creators, and the individual Creative Commons/public-domain licenses are listed in `src/main/webapp/IMAGE_CREDITS.md`; keep that file with the project when you share it.

Password reset works in development mode without email configuration: the reset link appears in Eclipse’s Tomcat console. To send mail, configure `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, and `SMTP_PASS` for the Tomcat server process.

The shopping cart accepts items from one restaurant at a time, as on common food-ordering services. Checkout itemizes 5% GST, a ₹40 delivery fee, and a ₹8 platform fee; the grand total is recalculated on the server and saved with the order. Payment choices are demo options and do not connect to a payment gateway. A completed order is saved with its line items in a database transaction; the success page verifies the order belongs to the signed-in account and shows the saved order total. Order history displays each signed-in customer's orders in chronological order with a gap-free sequence starting at 1; internal database IDs remain unchanged.

## Troubleshooting

- **Database connection error:** confirm MySQL is running, `db/schema.sql` completed, and `DB_URL`, `DB_USER`, and `DB_PASS` match your local server.
- **404 at the app URL, `NoSuchFileException` under Eclipse `wtpwebapps`, or `ClassNotFoundException` for `FrontController`:** stop Tomcat first. Right-click the project and choose **Maven → Update Project**, then **Project → Clean** to regenerate `target/classes`. In the Servers view use **Add and Remove…** to remove and re-add `CraveDrop_FoodDeliver`, then right-click Tomcat and choose **Clean** before starting. Do not run `mvn clean` while Tomcat is using the project output. Confirm the deployed context is `cravedrop`, then open `/cravedrop/app/home`.
- **Servlet/Jakarta class errors:** confirm the server runtime is Tomcat 10.1, not Tomcat 9.
- **Java version errors:** set the workspace default JRE to an installed JDK, then right-click the project and run **Maven → Update Project**. Maven compiles with the Java 17 API release.
- **Project name `CraveDrop_FoodDeliver` already exists in the workspace:** import it into a different Eclipse workspace or remove the old project from the workspace without deleting its files, then import again.

## Reference-inspired updates
- Restaurant menus support one-click add and inline quantity steppers. Cart changes are sent with Fetch and update the header count and floating subtotal without a page reload.
- Customers can save and remove dishes from a persistent wishlist. The `wishlist_items` table is created automatically on first use; no manual SQL change is needed for existing databases.
- Menus keep their existing restaurant-specific dishes and local food photos. The cart still enforces the single-restaurant-per-order rule and quantity limits.
- The admin operations page follows the reference dashboard structure, with live summary cards, restaurant and dish catalog tables, recent customer accounts, delivery-partner registration and assignment, and working order-status controls.
- Import the folder as the Maven project `CraveDrop_FoodDeliver`; the packaged WAR is in `dist/cravedrop.war`.

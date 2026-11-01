# DAO and data design

## Request and data flow

`JSP views → FrontController servlet → AuthService / OrderService → DAO classes → Db (JDBC) → MySQL`

The servlet handles `/app/*`, validates request inputs through the services, and forwards view data to JSPs under `src/main/webapp/WEB-INF/views`. The JSPs render the response. SQL is kept in DAO classes and uses prepared statements.

## Data access classes

| Class | Tables / data | Responsibility |
| --- | --- | --- |
| `Db` | MySQL connection | Opens JDBC connections using `DB_URL`, `DB_USER`, and `DB_PASS`; supplies defaults for local development. |
| `UserDAO` | `users` | Finds users by email/id or active reset token, creates accounts, updates password hashes, and stores reset-token expiry. |
| `RestaurantDAO` | `restaurants.tsv` | Loads the 30-card restaurant catalog and local image paths. |
| `MenuDAO` | `menu_items`, catalog TSV files | Loads the 180 restaurant-specific menu items, upgrades older menu tables, idempotently updates the catalog, searches menu items, supplies distinct home-page picks, and excludes retired catalog rows from listing and cart lookups. |
| `OrderDAO` | `orders`, `order_items` | Saves an order and its line items in one transaction, reads history scoped to the signed-in user, and verifies ownership before displaying an order-confirmation page. |
| `AdminDashboardDAO` | `users`, `menu_items`, `orders` | Supplies aggregate counts and the latest eight database-backed orders for authenticated administrators. It only reads data. |

`AuthService` validates registration and password changes and hashes passwords with BCrypt. A successful password update refreshes the user record held in the active session. `OrderService` calculates totals from database prices, enforces one restaurant per cart, validates checkout fields, and delegates order persistence to `OrderDAO`.

The customer dashboard computes its summary from the signed-in user's order history. The admin route checks the session user's `admin` role before asking `AdminDashboardDAO` for metrics. Public registration never accepts the admin role; an administrator account is promoted manually by an operator using `db/admin-setup.sql`.

## Relationships

- `users (1) → (many) orders`
- `orders (1) → (many) order_items`

Each order item stores the dish name, quantity, and price at checkout time. The cart stays in the HTTP session until checkout. Payment choices are delivery-method labels only; the app does not process or store card details. Restaurant/menu catalogue rows and ratings are bundled seed data rather than a live feed from restaurant platforms.

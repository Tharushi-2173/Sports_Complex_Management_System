## Sports Complex Management System (Java Swing + MySQL)

A desktop application to manage users, facilities, bookings, payments, reports, maintenance, and feedback for a sports complex. Built with pure Java (Swing), JDBC (no ORM, no UI frameworks), and MySQL.

### Features
- User Management: Admin, Coach, Member CRUD, role-based access.
- Facility Management: Add/update/remove facilities, availability, pricing.
- Booking & Scheduling: Book, cancel, reschedule, prevent double-booking.
- Payments: Calculate costs, discounts, receipts, history.
- Reports: Usage and income reports, CSV export.
- Maintenance & Feedback: Requests tracking and feedback capture.
- Authentication: Secure login, role-based dashboards.
- Data Management: Local MySQL storage, CSV export.

### Tech Stack
- Java 8+ (Swing UI, JDBC)
- MySQL 8+
- Ant build (NetBeans-compatible)

### Setup
1) Install MySQL and create database/user.
2) Import schema and sample data:
```bash
mysql -u root -p < schema.sql
```
3) Configure DB credentials in `src/resources/db.properties`:
```
db.url=jdbc:mysql://localhost:3306/sports_complex?useSSL=false&serverTimezone=UTC
db.user=root
db.password=your_password
```
4) Ensure MySQL Connector/J is on the classpath (e.g., add `mysql-connector-j-8.x.x.jar` to Ant libraries).
5) Run the app entry point `ui.AppLauncher`.
6) Login with `admin@scms.local` / `admin123` (change password immediately). Password hashing uses SHA-256 with random salt for new users.

### Project Structure
- `config` – configuration loading
- `db` – JDBC connection manager
- `model` – domain models (users, facilities, bookings, payments, maintenance)
- `dao` – DAO interfaces
- `dao.impl` – JDBC implementations
- `service` – business services
- `service.impl` – service implementations
- `util` – helpers (hashing, dates, csv)
- `ui` – Swing entry + navigation
- `ui.screens` – Swing screens (login, dashboard, users, facilities, bookings, payments, reports, maintenance, feedback)

### Credentials
- Default Admin: `admin@scms.local` / `admin123` (created in schema sample data). Change immediately in production.

### Notes
- This is a modular, layered architecture. You can extend DAOs/services independently.
- Error handling aims to be explicit and visible to operators.

### Reporting & Exports
- Generate usage/income reports in Reports screen (select date range/year and click Generate).
- Export Payments CSV: use the button in Reports; choose save location. CSV is RFC4180-compatible where needed (quoted fields when necessary).

### Receipts
- In Payments screen, select a payment row and click "Generate Receipt for Selected".
- A `.txt` file is created in the working directory with receipt details (ID, booking, amount, discount, method, reference, paid time).



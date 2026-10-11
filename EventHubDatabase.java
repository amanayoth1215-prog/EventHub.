import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/** MySQL data-access class for the EventHub college project. */
class EventHubDatabase
{
    private static final String DEFAULT_DATABASE_URL =
            "jdbc:mysql://localhost:3306/eventhub?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DATABASE_URL = setting(
            "eventhub.db.url", "EVENTHUB_DB_URL", DEFAULT_DATABASE_URL);
    private static final String DATABASE_USER = setting(
            "eventhub.db.user", "EVENTHUB_DB_USER", "root");
    private static final String DATABASE_PASSWORD = setting(
            "eventhub.db.password", "EVENTHUB_DB_PASSWORD", "");

    private static String setting(String property, String environment, String fallback)
    {
        String value = System.getProperty(property);
        if (value == null || value.trim().isEmpty())
            value = System.getenv(environment);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static Connection openConnection() throws Exception
    {
        return DriverManager.getConnection(DATABASE_URL,"root","9a9B6c//");
    }

    EventHubDatabase() throws Exception
    {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users ("
                    + "email VARCHAR(320) NOT NULL PRIMARY KEY, name VARCHAR(255) NOT NULL, "
                    + "password VARCHAR(255) NOT NULL, role VARCHAR(32) NOT NULL) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS events ("
                    + "name VARCHAR(255) NOT NULL, event_date VARCHAR(100) NOT NULL, "
                    + "venue VARCHAR(255) NOT NULL, description TEXT NOT NULL, "
                    + "price DECIMAL(12,2) NOT NULL, capacity INT NOT NULL, "
                    + "ticket_type VARCHAR(100) NOT NULL, organizer_email VARCHAR(320) NOT NULL, "
                    + "PRIMARY KEY (name, organizer_email), "
                    + "CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_email) REFERENCES users(email)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bookings ("
                    + "id VARCHAR(100) NOT NULL PRIMARY KEY, attendee_email VARCHAR(320) NOT NULL, "
                    + "event_name VARCHAR(255) NOT NULL, event_date VARCHAR(100) NOT NULL, "
                    + "venue VARCHAR(255) NOT NULL, ticket_type VARCHAR(100) NOT NULL, "
                    + "quantity INT NOT NULL, amount INT NOT NULL, status VARCHAR(32) NOT NULL, "
                    + "check_in_status VARCHAR(32) NOT NULL, organizer_email VARCHAR(320) NOT NULL, "
                    + "INDEX idx_bookings_organizer (organizer_email), "
                    + "INDEX idx_bookings_attendee (attendee_email), "
                    + "CONSTRAINT fk_bookings_attendee FOREIGN KEY (attendee_email) REFERENCES users(email), "
                    + "CONSTRAINT fk_bookings_organizer FOREIGN KEY (organizer_email) REFERENCES users(email)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    SavedData load() throws Exception
    {
        SavedData data = new SavedData();
        boolean foundAnyData = false;
        try (Connection connection = openConnection(); Statement statement = connection.createStatement())
        {
            try (ResultSet rows = statement.executeQuery("SELECT name, email, password, role FROM users"))
            {
                while (rows.next())
                {
                    foundAnyData = true;
                    String role = rows.getString("role");
                    if ("ORGANIZER".equalsIgnoreCase(role))
                        data.users.add(new Organizer(rows.getString("name"), rows.getString("email"), rows.getString("password")));
                    else
                        data.users.add(new Attendee(rows.getString("name"), rows.getString("email"), rows.getString("password")));
                }
            }
            try (ResultSet rows = statement.executeQuery(
                    "SELECT name, event_date, venue, description, price, capacity, ticket_type, organizer_email FROM events"))
            {
                while (rows.next())
                {
                    foundAnyData = true;
                    data.events.add(new Event(rows.getString("name"), rows.getString("event_date"),
                            rows.getString("venue"), rows.getString("description"), rows.getDouble("price"),
                            rows.getInt("capacity"), rows.getString("ticket_type"), rows.getString("organizer_email")));
                }
            }
            try (ResultSet rows = statement.executeQuery(
                    "SELECT id, attendee_email, event_name, event_date, venue, ticket_type, quantity, amount, check_in_status, organizer_email FROM bookings"))
            {
                while (rows.next())
                {
                    foundAnyData = true;
                    Booking booking = new Booking(rows.getString("id"), rows.getString("attendee_email"),
                            rows.getString("event_name"), rows.getString("event_date"), rows.getString("venue"),
                            rows.getString("ticket_type"), rows.getInt("quantity"), rows.getInt("amount"),
                            rows.getString("organizer_email"));
                    booking.setCheckInStatus(rows.getString("check_in_status"));
                    data.bookings.add(booking);
                }
            }
        }
        return foundAnyData ? data : null;
    }

    /** Write all three current collections together in one database transaction. */
    void save(Vector<User> users, Vector<Event> events, Vector<Booking> bookings) throws Exception
    {
        try (Connection connection = openConnection())
        {
            connection.setAutoCommit(false);
            try
            {
                try (Statement statement = connection.createStatement())
                {
                    statement.executeUpdate("DELETE FROM bookings");
                    statement.executeUpdate("DELETE FROM events");
                    statement.executeUpdate("DELETE FROM users");
                }
                saveUsers(connection, users);
                saveEvents(connection, events);
                saveBookings(connection, bookings);
                connection.commit();
            }
            catch (Exception ex)
            {
                connection.rollback();
                throw ex;
            }
        }
    }

    private void saveUsers(Connection connection, Vector<User> users) throws Exception
    {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)"))
        {
            for (User user : users)
            {
                statement.setString(1, user.getName());
                statement.setString(2, user.getEmail());
                statement.setString(3, user.getPassword());
                statement.setString(4, user.getRole());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveEvents(Connection connection, Vector<Event> events) throws Exception
    {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO events (name, event_date, venue, description, price, capacity, ticket_type, organizer_email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"))
        {
            for (Event event : events)
            {
                statement.setString(1, event.getName());
                statement.setString(2, event.getDate());
                statement.setString(3, event.getVenue());
                statement.setString(4, event.getDescription());
                statement.setDouble(5, event.getPrice());
                statement.setInt(6, event.getCapacity());
                statement.setString(7, event.getTicketType());
                statement.setString(8, event.getOrganizerEmail());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveBookings(Connection connection, Vector<Booking> bookings) throws Exception
    {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO bookings (id, attendee_email, event_name, event_date, venue, ticket_type, quantity, amount, status, check_in_status, organizer_email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"))
        {
            for (Booking booking : bookings)
            {
                statement.setString(1, booking.getId());
                statement.setString(2, booking.getEmail());
                statement.setString(3, booking.getEventName());
                statement.setString(4, booking.getDate());
                statement.setString(5, booking.getVenue());
                statement.setString(6, booking.getTicketType());
                statement.setInt(7, booking.getQuantity());
                statement.setInt(8, booking.getAmount());
                statement.setString(9, booking.getStatus());
                statement.setString(10, booking.getCheckInStatus());
                statement.setString(11, booking.getOrganizerEmail());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    static class SavedData
    {
        Vector<User> users = new Vector<User>();
        Vector<Event> events = new Vector<Event>();
        Vector<Booking> bookings = new Vector<Booking>();
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;



/* =========================================================
   EVENTHUB MAIN CLASS
   ========================================================= */

// INHERITANCE: EventHub extends JFrame to use the Swing window features.
public class EventHub extends JFrame
{
    private Vector<User> users = new Vector<User>();
    private Vector<Event> events = new Vector<Event>();
    private Vector<Booking> bookings = new Vector<Booking>();
    // MySQL keeps this project's data after the Swing window closes.
    private EventHubDatabase database;

    // POLYMORPHISM: User reference can refer to an Attendee or Organizer object.
    private User currentUser;

    private Color navy = new Color(25, 45, 80);
    private Color blue = new Color(55, 105, 190);
    private Color light = new Color(245, 247, 250);

    // CONSTRUCTOR: Initializes the EventHub window and application data.
    public EventHub()
    {
        setTitle("EventHub - Event Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try
        {
            database = new EventHubDatabase();
            EventHubDatabase.SavedData savedData = database.load();

            if (savedData == null)
            {
                loadDemoData();
                saveData();
            }
            else
            {
                users = savedData.users;
                events = savedData.events;
                bookings = savedData.bookings;
            }
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not open the local database.\n"
                    + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        showLogin();
    }


    // Save the three existing collections as one MySQL database snapshot.
    private void saveData()
    {
        if (database == null)
            return;

        try
        {
            database.save(users, events, bookings);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save your changes.\n"
                    + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /* =====================================================
       DEMO DATA
       ===================================================== */

    private void loadDemoData()
    {
        users.add(new Attendee(
                "Aman A",
                "aman@gmail.com",
                "1234"
        ));

        users.add(new Organizer(
                "EventHub Organizer",
                "organizer@eventhub.com",
                "1234"
        ));

        events.add(new Event(
                "Tech Fest 2026",
                "20-10-2026",
                "GEC Kozhikode",
                "Technology exhibition, coding events and workshops.",
                299,
                100,
                "Student",
                "organizer@eventhub.com"
        ));

        events.add(new Event(
                "Entrepreneurship Summit",
                "25-10-2026",
                "Main Auditorium",
                "Startup ideas, founders and entrepreneurship sessions.",
                499,
                80,
                "General",
                "organizer@eventhub.com"
        ));
    }


    /* =====================================================
       LOGIN
       ===================================================== */

    private void showLogin()
    {
        getContentPane().removeAll();

        JPanel main = new JPanel(new GridLayout(1, 2));

        JPanel left = new JPanel();
        left.setBackground(navy);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(
                BorderFactory.createEmptyBorder(
                        150, 60, 80, 40));

        JLabel logo = label("EVENTHUB", 38, true);
        logo.setForeground(Color.WHITE);

        JLabel text = label(
                "Discover  •  Book  •  Experience",
                17, false);
        text.setForeground(Color.WHITE);

        JLabel sub = label(
                "Event Booking and Management System",
                15, false);
        sub.setForeground(Color.WHITE);

        left.add(logo);
        left.add(Box.createVerticalStrut(15));
        left.add(text);
        left.add(Box.createVerticalStrut(30));
        left.add(sub);


        JPanel right = new JPanel();
        right.setBackground(Color.WHITE);
        right.setBorder(
                BorderFactory.createEmptyBorder(
                        90, 90, 70, 90));

        right.setLayout(
                new BoxLayout(right, BoxLayout.Y_AXIS));

        right.add(label("Welcome Back", 30, true));
        right.add(label(
                "Login to your EventHub account",
                14, false));

        right.add(Box.createVerticalStrut(25));

        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();

        email.setMaximumSize(
                new Dimension(420, 38));

        password.setMaximumSize(
                new Dimension(420, 38));

        right.add(label("Email", 13, true));
        right.add(email);

        right.add(Box.createVerticalStrut(12));

        right.add(label("Password", 13, true));
        right.add(password);

        right.add(Box.createVerticalStrut(15));

        JLabel roleLabel = label(
                "Login as", 13, true);

        right.add(roleLabel);

        final String[] selectedRole =
                {"ATTENDEE"};

        JButton attendeeButton =
                button("Attendee");

        JButton organizerButton =
                button("Organizer");

        JPanel rolePanel =
                new JPanel(new GridLayout(1, 2, 10, 0));

        rolePanel.setOpaque(false);
        rolePanel.setMaximumSize(
                new Dimension(420, 40));

        rolePanel.add(attendeeButton);
        rolePanel.add(organizerButton);

        right.add(rolePanel);

        right.add(Box.createVerticalStrut(20));

        JButton login = button("LOGIN");
        login.setMaximumSize(
                new Dimension(420, 42));

        right.add(login);

        right.add(Box.createVerticalStrut(10));

        JButton register =
                new JButton("Create New Account");

        register.setAlignmentX(
                Component.CENTER_ALIGNMENT);

        right.add(register);


        attendeeButton.addActionListener(
                new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                selectedRole[0] = "ATTENDEE";
            }
        });


        organizerButton.addActionListener(
                new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                selectedRole[0] = "ORGANIZER";
            }
        });


        login.addActionListener(
                new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                loginUser(
                        email.getText().trim(),
                        new String(
                                password.getPassword()),
                        selectedRole[0]);
            }
        });


        register.addActionListener(
                new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                registerUser();
            }
        });


        main.add(left);
        main.add(right);

        add(main);

        revalidate();
        repaint();
    }


    /* =====================================================
       LOGIN VALIDATION
       ===================================================== */

    private void loginUser(
            String email,
            String password,
            String role)
    {
        if (email.isEmpty() ||
                password.isEmpty())
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter email and password.");

            return;
        }


        for (int i = 0;
                i < users.size();
                i++)
        {
            User user =
                    users.elementAt(i);

            if (user.getEmail()
                    .equalsIgnoreCase(email)
                    &&
                user.getPassword()
                    .equals(password)
                    &&
                user.getRole()
                    .equals(role))
            {
                currentUser = user;

                user.loginSuccess();

                if (role.equals("ATTENDEE"))
                    attendeeDashboard();
                else
                    organizerDashboard();

                return;
            }
        }


        JOptionPane.showMessageDialog(
                this,
                "Invalid email, password or role.");
    }


    /* =====================================================
       REGISTER ACCOUNT
       ===================================================== */

    private void registerUser()
    {
        JTextField name =
                new JTextField();

        JTextField email =
                new JTextField();

        JPasswordField password =
                new JPasswordField();


        String[] types =
        {
            "Attendee",
            "Organizer"
        };


        JComboBox<String> accountType =
                new JComboBox<String>(types);


        Object[] fields =
        {
            "Name:", name,
            "Email:", email,
            "Password:", password,
            "Account Type:", accountType
        };


        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        fields,
                        "Create Account",
                        JOptionPane.OK_CANCEL_OPTION);


        if (result != JOptionPane.OK_OPTION)
            return;


        String n = name.getText().trim();

        String em =
                email.getText().trim();

        String p =
                new String(
                        password.getPassword());


        if (n.isEmpty() ||
                em.isEmpty() ||
                p.isEmpty())
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Please fill all fields.");

            return;
        }


        for (int i = 0;
                i < users.size();
                i++)
        {
            if (users.elementAt(i)
                    .getEmail()
                    .equalsIgnoreCase(em))
            {
                JOptionPane.showMessageDialog(
                        this,
                        "Email already registered.");

                return;
            }
        }


        if (accountType.getSelectedItem()
                .toString()
                .equals("Organizer"))
        {
            users.add(
                    new Organizer(
                            n, em, p));
        }
        else
        {
            users.add(
                    new Attendee(
                            n, em, p));
        }

        saveData();


        JOptionPane.showMessageDialog(
                this,
                "Account created successfully.\n"
                + "You can now login.");
    }


    /* =====================================================
       ATTENDEE DASHBOARD
       ===================================================== */

    private void attendeeDashboard()
    {
        JPanel main =
                new JPanel(
                        new BorderLayout());

        main.add(
                sidebar("ATTENDEE"),
                BorderLayout.WEST);


        JPanel content =
                pagePanel(
                        "Welcome, "
                        + currentUser.getName(),
                        "Browse and book upcoming events.");


        content.add(
                statCard(
                        "Available Events",
                        "" + events.size()));

        content.add(
                Box.createVerticalStrut(20));

        content.add(
                label(
                        "Upcoming Events",
                        20, true));

        content.add(
                Box.createVerticalStrut(10));


        for (int i = 0;
                i < events.size();
                i++)
        {
            content.add(
                    eventCard(
                            events.elementAt(i),
                            false));

            content.add(
                    Box.createVerticalStrut(10));
        }


        main.add(
                new JScrollPane(content),
                BorderLayout.CENTER);

        setContentPane(main);

        revalidate();
        repaint();
    }


    private boolean isOrganizerEvent(Event event)
    {
        return event.getOrganizerEmail()
                .equalsIgnoreCase(currentUser.getEmail());
    }

    private boolean isOrganizerBooking(Booking booking)
    {
        return booking.getOrganizerEmail()
                .equalsIgnoreCase(currentUser.getEmail());
    }

    private Vector<Event> getOrganizerEvents()
    {
        Vector<Event> result = new Vector<Event>();

        for (int i = 0; i < events.size(); i++)
        {
            if (isOrganizerEvent(events.elementAt(i)))
                result.add(events.elementAt(i));
        }

        return result;
    }

    private Vector<Booking> getOrganizerBookings()
    {
        Vector<Booking> result = new Vector<Booking>();

        for (int i = 0; i < bookings.size(); i++)
        {
            if (isOrganizerBooking(bookings.elementAt(i)))
                result.add(bookings.elementAt(i));
        }

        return result;
    }


    /* =====================================================
       ORGANIZER DASHBOARD
       ===================================================== */

    private void organizerDashboard()
    {
        JPanel main =
                new JPanel(
                        new BorderLayout());

        main.add(
                sidebar("ORGANIZER"),
                BorderLayout.WEST);


        JPanel content =
                pagePanel(
                        "Organizer Dashboard",
                        "Create and manage your events.");


        JPanel stats =
                new JPanel(
                        new GridLayout(1, 3, 10, 0));

        stats.setOpaque(false);


        stats.add(
                statCard(
                        "Events",
                        "" + getOrganizerEvents().size()));

        stats.add(
                statCard(
                        "Bookings",
                        "" + getOrganizerBookings().size()));

        stats.add(
                statCard(
                        "Checked In",
                        "" + checkedInCount()));


        content.add(stats);

        content.add(
                Box.createVerticalStrut(20));


        JButton create =
                button("+ Create New Event");

        create.setMaximumSize(
                new Dimension(250, 42));


        create.addActionListener(
                new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createEvent();
            }
        });


        content.add(create);

        content.add(
                Box.createVerticalStrut(20));

        content.add(
                label(
                        "Your Events",
                        20, true));

        content.add(
                Box.createVerticalStrut(10));


        for (int i = 0;
                i < events.size();
                i++)
        {
            if (!isOrganizerEvent(events.elementAt(i)))
                continue;

            content.add(
                    eventCard(
                            events.elementAt(i),
                            true));

            content.add(
                    Box.createVerticalStrut(10));
        }


        main.add(
                new JScrollPane(content),
                BorderLayout.CENTER);

        setContentPane(main);

        revalidate();
        repaint();
    }


    /* =====================================================
       SIDEBAR
       ===================================================== */

    private JPanel sidebar(String role)
    {
        JPanel side =
                new JPanel();

        side.setPreferredSize(
                new Dimension(210, 0));

        side.setBackground(navy);

        side.setLayout(
                new BoxLayout(
                        side,
                        BoxLayout.Y_AXIS));

        side.setBorder(
                BorderFactory.createEmptyBorder(
                        25, 15, 25, 15));


        JLabel logo =
                label("EVENTHUB", 24, true);

        logo.setForeground(Color.WHITE);

        logo.setAlignmentX(
                Component.CENTER_ALIGNMENT);

        side.add(logo);

        side.add(
                Box.createVerticalStrut(30));


        if (role.equals("ATTENDEE"))
        {
            addNavButton(
                    side,
                    "Dashboard",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    attendeeDashboard();
                }
            });


            addNavButton(
                    side,
                    "Browse Events",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    browseEvents();
                }
            });


            addNavButton(
                    side,
                    "My Bookings",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    bookingHistory();
                }
            });


            addNavButton(
                    side,
                    "Digital Tickets",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    digitalTickets();
                }
            });


            addNavButton(
                    side,
                    "Profile",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    profile();
                }
            });
        }
        else
        {
            addNavButton(
                    side,
                    "Dashboard",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    organizerDashboard();
                }
            });


            addNavButton(
                    side,
                    "Create Event",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    createEvent();
                }
            });


            addNavButton(
                    side,
                    "Manage Events",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    manageEvents();
                }
            });


            addNavButton(
                    side,
                    "Attendees",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    attendeeList();
                }
            });


            addNavButton(
                    side,
                    "QR Check-in",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    checkIn();
                }
            });


            addNavButton(
                    side,
                    "Profile",
                    new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    profile();
                }
            });
        }


        side.add(
                Box.createVerticalGlue());


        JButton logout =
                new JButton("Logout");


        logout.setMaximumSize(
                new Dimension(180, 40));


        logout.addActionListener(
                new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                currentUser = null;
                showLogin();
            }
        });


        side.add(logout);

        return side;
    }


    private void addNavButton(
            JPanel panel,
            String text,
            ActionListener action)
    {
        JButton b =
                new JButton(text);

        b.setMaximumSize(
                new Dimension(180, 40));

        b.addActionListener(action);

        panel.add(b);

        panel.add(
                Box.createVerticalStrut(8));
    }


    /* =====================================================
       EVENT CARD
       ===================================================== */

    private JPanel eventCard(
            final Event event,
            boolean organizer)
    {
        JPanel card =
                new JPanel(
                        new BorderLayout(15, 5));

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(
                                15, 15, 15, 15)));


        JPanel info =
                new JPanel();

        info.setOpaque(false);

        info.setLayout(
                new BoxLayout(
                        info,
                        BoxLayout.Y_AXIS));


        info.add(
                label(
                        event.getName(),
                        19, true));

        info.add(
                label(
                        event.getDate()
                        + " | "
                        + event.getVenue(),
                        13, false));

        info.add(
                label(
                        "Price: Rs. "
                        + event.getPrice()
                        + " | Seats: "
                        + event.getCapacity(),
                        13, false));


        card.add(
                info,
                BorderLayout.CENTER);


        if (organizer)
        {
            JButton manage =
                    button("Manage");

            manage.addActionListener(
                    new ActionListener()
            {
                public void actionPerformed(
                        ActionEvent e)
                {
                    manageEvent(event);
                }
            });

            card.add(
                    manage,
                    BorderLayout.EAST);
        }
        else
        {
            JButton view =
                    button("View & Book");

            view.addActionListener(
                    new ActionListener()
            {
                public void actionPerformed(
                        ActionEvent e)
                {
                    eventDetails(event);
                }
            });

            card.add(
                    view,
                    BorderLayout.EAST);
        }


        return card;
    }


    /* =====================================================
       BROWSE EVENTS
       ===================================================== */

    private void browseEvents()
    {
        JPanel content =
                pagePanel(
                        "Browse Events",
                        "Find and book upcoming events.");


        for (int i = 0;
                i < events.size();
                i++)
        {
            content.add(
                    eventCard(
                            events.elementAt(i),
                            false));

            content.add(
                    Box.createVerticalStrut(10));
        }


        showAttendeePage(content);
    }


    /* =====================================================
       EVENT DETAILS
       ===================================================== */

    private void eventDetails(
            final Event event)
    {
        JPanel panel =
                new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        30, 40, 30, 40));

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS));


        panel.add(
                label(
                        event.getName(),
                        28, true));

        panel.add(
                Box.createVerticalStrut(15));


        panel.add(
                label(
                        "Date: "
                        + event.getDate(),
                        15, false));

        panel.add(
                label(
                        "Venue: "
                        + event.getVenue(),
                        15, false));

        panel.add(
                label(
                        "Ticket Type: "
                        + event.getTicketType(),
                        15, false));

        panel.add(
                label(
                        "Price: Rs. "
                        + event.getPrice(),
                        15, false));

        panel.add(
                label(
                        "Available Seats: "
                        + event.getCapacity(),
                        15, false));


        JTextArea description =
                new JTextArea(
                        event.getDescription());

        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);


        panel.add(
                Box.createVerticalStrut(15));

        panel.add(description);

        panel.add(
                Box.createVerticalStrut(20));


        JButton book =
                button("Book Ticket");


        book.addActionListener(
                new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                bookEvent(event);
            }
        });


        panel.add(book);


        showAttendeePage(panel);
    }


    /* =====================================================
       BOOK EVENT
       ===================================================== */

    private void bookEvent(
            final Event event)
    {
        if (event.getCapacity() <= 0)
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Tickets are sold out.");

            return;
        }


        JTextField quantity =
                new JTextField("1");


        String[] payments =
        {
            "UPI",
            "Card",
            "Cash"
        };


        JComboBox<String> payment =
                new JComboBox<String>(
                        payments);


        Object[] fields =
        {
            "Event: " + event.getName(),
            "Price: Rs. " + event.getPrice(),
            "Quantity:", quantity,
            "Payment:", payment
        };


        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        fields,
                        "Book Ticket",
                        JOptionPane.OK_CANCEL_OPTION);


        if (result != JOptionPane.OK_OPTION)
            return;


        try
        {
            int q =
                    Integer.parseInt(
                            quantity.getText());


            if (q <= 0 ||
                    q > event.getCapacity())
            {
                throw new NumberFormatException();
            }


            int total =
                    (int)
                    (q * event.getPrice());


            Payment selectedPayment;


            if (payment.getSelectedItem()
                    .toString()
                    .equals("UPI"))
            {
                selectedPayment =
                        new UPIPayment();
            }
            else if (
                    payment.getSelectedItem()
                    .toString()
                    .equals("Card"))
            {
                selectedPayment =
                        new CardPayment();
            }
            else
            {
                selectedPayment =
                        new CashPayment();
            }


            selectedPayment.pay(total);


            String id =
                    "EH"
                    + (1000
                    + bookings.size()
                    + 1);


            Booking booking =
                    new Booking(
                            id,
                            currentUser.getEmail(),
                            event.getName(),
                            event.getDate(),
                            event.getVenue(),
                            event.getTicketType(),
                            q,
                            total,
                            event.getOrganizerEmail());


            bookings.add(booking);


            event.setCapacity(
                    event.getCapacity() - q);

            saveData();


            JOptionPane.showMessageDialog(
                    this,
                    "Booking successful!\n"
                    + "Booking ID: "
                    + id
                    + "\nAmount: Rs. "
                    + total);


            digitalTicket(booking);
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid quantity.");
        }
    }


    /* =====================================================
       BOOKING HISTORY
       ===================================================== */

    private void bookingHistory()
    {
        JPanel content =
                pagePanel(
                        "My Bookings",
                        "Your booking history.");


        Vector<Booking> mine =
                getUserBookings();


        if (mine.size() == 0)
        {
            content.add(
                    label(
                            "No bookings yet.",
                            16, false));
        }


        for (int i = 0;
                i < mine.size();
                i++)
        {
            final Booking b =
                    mine.elementAt(i);


            JPanel card =
                    new JPanel(
                            new BorderLayout());


            card.setBackground(Color.WHITE);

            card.setBorder(
                    BorderFactory.createLineBorder(
                            Color.LIGHT_GRAY));


            JPanel info =
                    new JPanel();

            info.setOpaque(false);

            info.setLayout(
                    new BoxLayout(
                            info,
                            BoxLayout.Y_AXIS));


            info.add(
                    label(
                            b.getEventName(),
                            18, true));

            info.add(
                    label(
                            "Booking ID: "
                            + b.getId(),
                            13, false));

            info.add(
                    label(
                            "Date: "
                            + b.getDate(),
                            13, false));

            info.add(
                    label(
                            "Amount: Rs. "
                            + b.getAmount(),
                            13, false));


            card.add(
                    info,
                    BorderLayout.CENTER);


            JButton ticket =
                    button("Ticket");


            ticket.addActionListener(
                    new ActionListener()
            {
                public void actionPerformed(
                        ActionEvent e)
                {
                    digitalTicket(b);
                }
            });


            card.add(
                    ticket,
                    BorderLayout.EAST);


            content.add(card);

            content.add(
                    Box.createVerticalStrut(10));
        }


        showAttendeePage(content);
    }


    /* =====================================================
       GET USER BOOKINGS
       ===================================================== */

    private Vector<Booking> getUserBookings()
    {
        Vector<Booking> result =
                new Vector<Booking>();


        for (int i = 0;
                i < bookings.size();
                i++)
        {
            Booking b =
                    bookings.elementAt(i);


            if (b.getEmail()
                    .equalsIgnoreCase(
                            currentUser.getEmail()))
            {
                result.add(b);
            }
        }


        return result;
    }


    /* =====================================================
       DIGITAL TICKET
       ===================================================== */

    private void digitalTickets()
    {
        JPanel content =
                pagePanel(
                        "Digital Tickets",
                        "Your tickets.");


        Vector<Booking> mine =
                getUserBookings();


        for (int i = 0;
                i < mine.size();
                i++)
        {
            final Booking b =
                    mine.elementAt(i);


            JButton ticket =
                    button(
                            b.getEventName()
                            + " - "
                            + b.getId());


            ticket.setMaximumSize(
                    new Dimension(
                            700, 45));


            ticket.addActionListener(
                    new ActionListener()
            {
                public void actionPerformed(
                        ActionEvent e)
                {
                    digitalTicket(b);
                }
            });


            content.add(ticket);

            content.add(
                    Box.createVerticalStrut(10));
        }


        showAttendeePage(content);
    }


    private void digitalTicket(
            Booking b)
    {
        JPanel panel =
                new JPanel(
                        new BorderLayout(20, 10));


        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        30, 40, 30, 40));


        JPanel info =
                new JPanel();

        info.setOpaque(false);

        info.setLayout(
                new BoxLayout(
                        info,
                        BoxLayout.Y_AXIS));


        info.add(
                label(
                        "DIGITAL TICKET",
                        26, true));

        info.add(
                Box.createVerticalStrut(15));

        info.add(
                label(
                        "Event: "
                        + b.getEventName(),
                        16, true));

        info.add(
                label(
                        "Date: "
                        + b.getDate(),
                        14, false));

        info.add(
                label(
                        "Venue: "
                        + b.getVenue(),
                        14, false));

        info.add(
                label(
                        "Booking ID: "
                        + b.getId(),
                        14, false));

        info.add(
                label(
                        "Ticket Type: "
                        + b.getTicketType(),
                        14, false));

        info.add(
                label(
                        "Quantity: "
                        + b.getQuantity(),
                        14, false));

        info.add(
                label(
                        "Amount: Rs. "
                        + b.getAmount(),
                        14, false));

        info.add(
                label(
                        "Status: "
                        + b.getStatus(),
                        14, false));


        panel.add(
                info,
                BorderLayout.CENTER);


        panel.add(
                new QRPanel(),
                BorderLayout.EAST);


        showAttendeePage(panel);
    }


    /* =====================================================
       CREATE EVENT
       ===================================================== */

    private void createEvent()
    {
        JTextField name =
                new JTextField();

        JTextField date =
                new JTextField();

        JTextField venue =
                new JTextField();

        JTextField price =
                new JTextField();

        JTextField capacity =
                new JTextField();


        String[] types =
        {
            "Student",
            "General",
            "VIP"
        };


        JComboBox<String> type =
                new JComboBox<String>(types);


        JTextArea description =
                new JTextArea(4, 20);


        Object[] fields =
        {
            "Event Name:", name,
            "Date:", date,
            "Venue:", venue,
            "Ticket Price:", price,
            "Capacity:", capacity,
            "Ticket Type:", type,
            "Description:",
            new JScrollPane(description)
        };


        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        fields,
                        "Create Event",
                        JOptionPane.OK_CANCEL_OPTION);


        if (result != JOptionPane.OK_OPTION)
            return;


        try
        {
            String n =
                    name.getText().trim();

            String d =
                    date.getText().trim();

            String v =
                    venue.getText().trim();


            double p =
                    Double.parseDouble(
                            price.getText().trim());


            int c =
                    Integer.parseInt(
                            capacity.getText().trim());


            if (n.isEmpty()
                    || d.isEmpty()
                    || v.isEmpty()
                    || p < 0
                    || c <= 0)
            {
                throw new NumberFormatException();
            }


            Event event =
                    new Event(
                            n,
                            d,
                            v,
                            description.getText(),
                            p,
                            c,
                            type.getSelectedItem()
                                    .toString(),
                            currentUser.getEmail());


            events.add(event);

            saveData();


            JOptionPane.showMessageDialog(
                    this,
                    "Event created successfully!");


            organizerDashboard();
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid price and capacity.");
        }
    }


    /* =====================================================
       MANAGE EVENTS
       ===================================================== */

    private void manageEvents()
    {
        JPanel content =
                pagePanel(
                        "Manage Events",
                        "Edit or delete events.");


        for (int i = 0;
                i < events.size();
                i++)
        {
            Event event = events.elementAt(i);

            if (!isOrganizerEvent(event))
                continue;

            content.add(
                    eventCard(
                            event,
                            true));

            content.add(
                    Box.createVerticalStrut(10));
        }


        showOrganizerPage(content);
    }


    private void manageEvent(
            final Event event)
    {
        String[] options =
        {
            "Edit",
            "Delete",
            "View Attendees",
            "Cancel"
        };


        int choice =
                JOptionPane.showOptionDialog(
                        this,
                        "Manage: "
                        + event.getName(),
                        "Manage Event",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]);


        if (choice == 0)
            editEvent(event);

        else if (choice == 1)
        {
            int answer =
                    JOptionPane.showConfirmDialog(
                            this,
                            "Delete this event?",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION);


            if (answer ==
                    JOptionPane.YES_OPTION)
            {
                events.remove(event);
                saveData();
                organizerDashboard();
            }
        }
        else if (choice == 2)
        {
            attendeeListForEvent(event);
        }
    }


    /* =====================================================
       EDIT EVENT
       ===================================================== */

    private void editEvent(
            final Event event)
    {
        JTextField name =
                new JTextField(
                        event.getName());

        JTextField date =
                new JTextField(
                        event.getDate());

        JTextField venue =
                new JTextField(
                        event.getVenue());

        JTextField price =
                new JTextField(
                        "" + event.getPrice());

        JTextField capacity =
                new JTextField(
                        "" + event.getCapacity());


        JTextArea description =
                new JTextArea(
                        event.getDescription());


        Object[] fields =
        {
            "Event Name:", name,
            "Date:", date,
            "Venue:", venue,
            "Price:", price,
            "Capacity:", capacity,
            "Description:",
            new JScrollPane(description)
        };


        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        fields,
                        "Edit Event",
                        JOptionPane.OK_CANCEL_OPTION);


        if (result != JOptionPane.OK_OPTION)
            return;


        try
        {
            double p =
                    Double.parseDouble(
                            price.getText());

            int c =
                    Integer.parseInt(
                            capacity.getText());


            if (name.getText()
                    .trim().isEmpty()
                    || date.getText()
                    .trim().isEmpty()
                    || venue.getText()
                    .trim().isEmpty()
                    || p < 0
                    || c <= 0)
            {
                throw new NumberFormatException();
            }


            event.setName(
                    name.getText().trim());

            event.setDate(
                    date.getText().trim());

            event.setVenue(
                    venue.getText().trim());

            event.setPrice(p);

            event.setCapacity(c);

            event.setDescription(
                    description.getText());

            saveData();


            JOptionPane.showMessageDialog(
                    this,
                    "Event updated successfully!");


            organizerDashboard();
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid values.");
        }
    }


    /* =====================================================
       ATTENDEE LIST
       ===================================================== */

    private void attendeeList()
    {
        JPanel content =
                pagePanel(
                        "Attendee List",
                        "All confirmed bookings.");


        for (int i = 0;
                i < bookings.size();
                i++)
        {
            Booking b = bookings.elementAt(i);

            if (!isOrganizerBooking(b))
                continue;

            content.add(
                    attendeeCard(b));

            content.add(
                    Box.createVerticalStrut(8));
        }


        showOrganizerPage(content);
    }


    private void attendeeListForEvent(
            Event event)
    {
        if (!isOrganizerEvent(event))
            return;

        JPanel content =
                pagePanel(
                        "Attendees - "
                        + event.getName(),
                        "Registered attendees.");


        boolean found = false;


        for (int i = 0;
                i < bookings.size();
                i++)
        {
            Booking b =
                    bookings.elementAt(i);


            if (isOrganizerBooking(b)
                    && b.getEventName().equals(event.getName())
                    && b.getOrganizerEmail().equalsIgnoreCase(
                            event.getOrganizerEmail()))
            {
                content.add(
                        attendeeCard(b));

                content.add(
                        Box.createVerticalStrut(8));

                found = true;
            }
        }


        if (!found)
        {
            content.add(
                    label(
                            "No attendees yet.",
                            15, false));
        }


        showOrganizerPage(content);
    }


    private JPanel attendeeCard(
            Booking b)
    {
        JPanel card =
                new JPanel();

        card.setBackground(Color.WHITE);

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS));

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(
                                12, 15, 12, 15)));


        card.add(
                label(
                        "Event: "
                        + b.getEventName(),
                        16, true));

        card.add(
                label(
                        "Email: "
                        + b.getEmail(),
                        13, false));

        card.add(
                label(
                        "Booking ID: "
                        + b.getId(),
                        13, false));

        card.add(
                label(
                        "Ticket: "
                        + b.getTicketType()
                        + " | Quantity: "
                        + b.getQuantity(),
                        13, false));

        card.add(
                label(
                        "Check-in: "
                        + b.getCheckInStatus(),
                        13, false));


        return card;
    }


    /* =====================================================
       QR CHECK-IN
       ===================================================== */

    private void checkIn()
    {
        JPanel content =
                pagePanel(
                        "QR Check-in",
                        "Enter a booking ID to check in.");


        JTextField id =
                new JTextField();

        id.setMaximumSize(
                new Dimension(500, 38));


        JButton check =
                button("Check In");


        content.add(
                label(
                        "Booking ID",
                        14, true));

        content.add(id);

        content.add(
                Box.createVerticalStrut(15));

        content.add(check);


        check.addActionListener(
                new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String bookingId =
                        id.getText().trim();


                for (int i = 0;
                        i < bookings.size();
                        i++)
                {
                    Booking b =
                            bookings.elementAt(i);


                    if (b.getId()
                            .equalsIgnoreCase(
                                    bookingId))
                    {
                        if (!isOrganizerBooking(b))
                        {
                            JOptionPane.showMessageDialog(
                                    EventHub.this,
                                    "This booking does not belong to your event.");
                            return;
                        }
                        if (b.getCheckInStatus()
                                .equals("CHECKED IN"))
                        {
                            JOptionPane.showMessageDialog(
                                    EventHub.this,
                                    "Already checked in.");
                        }
                        else
                        {
                            b.setCheckInStatus(
                                    "CHECKED IN");

                            saveData();

                            JOptionPane.showMessageDialog(
                                    EventHub.this,
                                    "Check-in successful!");
                        }

                        return;
                    }
                }


                JOptionPane.showMessageDialog(
                        EventHub.this,
                        "Booking ID not found.");
            }
        });


        showOrganizerPage(content);
    }


    /* =====================================================
       PROFILE
       ===================================================== */

    private void profile()
    {
        JPanel content =
                pagePanel(
                        "My Profile",
                        "Account information.");


        JPanel card =
                new JPanel();

        card.setBackground(Color.WHITE);

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS));

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        25, 30, 25, 30));


        card.add(
                label(
                        "Name: "
                        + currentUser.getName(),
                        17, true));

        card.add(
                Box.createVerticalStrut(10));

        card.add(
                label(
                        "Email: "
                        + currentUser.getEmail(),
                        15, false));

        card.add(
                Box.createVerticalStrut(10));

        card.add(
                label(
                        "Account Type: "
                        + currentUser.getRole(),
                        15, false));


        content.add(card);


        if (currentUser.getRole()
                .equals("ATTENDEE"))
        {
            showAttendeePage(content);
        }
        else
        {
            showOrganizerPage(content);
        }
    }


    /* =====================================================
       PAGE HELPERS
       ===================================================== */

    private JPanel pagePanel(
            String title,
            String subtitle)
    {
        JPanel panel =
                new JPanel();

        panel.setBackground(light);

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        35, 45, 30, 45));


        panel.add(
                label(
                        title,
                        28, true));

        panel.add(
                Box.createVerticalStrut(5));

        panel.add(
                label(
                        subtitle,
                        14, false));

        panel.add(
                Box.createVerticalStrut(25));


        return panel;
    }


    private void showAttendeePage(
            JPanel content)
    {
        JPanel main =
                new JPanel(
                        new BorderLayout());


        main.add(
                sidebar("ATTENDEE"),
                BorderLayout.WEST);


        main.add(
                new JScrollPane(content),
                BorderLayout.CENTER);


        setContentPane(main);

        revalidate();
        repaint();
    }


    private void showOrganizerPage(
            JPanel content)
    {
        JPanel main =
                new JPanel(
                        new BorderLayout());


        main.add(
                sidebar("ORGANIZER"),
                BorderLayout.WEST);


        main.add(
                new JScrollPane(content),
                BorderLayout.CENTER);


        setContentPane(main);

        revalidate();
        repaint();
    }


    private JPanel statCard(
            String title,
            String value)
    {
        JPanel panel =
                new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createLineBorder(
                        Color.LIGHT_GRAY));

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS));


        JLabel valueLabel =
                label(
                        value,
                        25, true);

        valueLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT);


        JLabel titleLabel =
                label(
                        title,
                        13, false);

        titleLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT);


        panel.add(
                Box.createVerticalGlue());

        panel.add(valueLabel);

        panel.add(titleLabel);

        panel.add(
                Box.createVerticalGlue());


        return panel;
    }


    private int checkedInCount()
    {
        int count = 0;

        for (int i = 0; i < bookings.size(); i++)
        {
            Booking b = bookings.elementAt(i);

            if (isOrganizerBooking(b)
                    && b.getCheckInStatus().equals("CHECKED IN"))
                count++;
        }

        return count;
    }



    private JLabel label(
            String text,
            int size,
            boolean bold)
    {
        JLabel label =
                new JLabel(text);


        if (bold)
        {
            label.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            size));
        }
        else
        {
            label.setFont(
                    new Font(
                            "Arial",
                            Font.PLAIN,
                            size));
        }


        return label;
    }


    private JButton button(
            String text)
    {
        JButton button =
                new JButton(text);


        button.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        13));


        button.setFocusPainted(false);

        button.setBackground(blue);

        button.setForeground(Color.WHITE);


        return button;
    }


    /* =====================================================
       MAIN METHOD
       ===================================================== */

    public static void main(String[] args)
    {
        new EventHub().setVisible(true);
    }
}


/* =========================================================
   USER ABSTRACT CLASS
   Abstraction + Encapsulation
   ========================================================= */

// ABSTRACTION: User defines common properties and methods for all users.
abstract class User implements java.io.Serializable
{
    private String name;
    private String email;
    private String password;
    private String role;


    User(
            String name,
            String email,
            String password,
            String role)
    {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }


    public String getName()
    {
        return name;
    }


    public String getEmail()
    {
        return email;
    }


    public String getPassword()
    {
        return password;
    }


    public String getRole()
    {
        return role;
    }


    abstract void loginSuccess();
}


/* =========================================================
   ATTENDEE CLASS
   Inheritance: Attendee extends User
   ========================================================= */

// INHERITANCE: Attendee inherits common features from User.
class Attendee extends User
{
    // CONSTRUCTOR: Creates an Attendee object.
    Attendee(
            String name,
            String email,
            String password)
    {
        super(
                name,
                email,
                password,
                "ATTENDEE");
    }


    // METHOD OVERRIDING: This subclass provides its own implementation.
    @Override
    void loginSuccess()
    {
        System.out.println(
                "Attendee logged in.");
    }
}


/* =========================================================
   ORGANIZER CLASS
   Inheritance: Organizer extends User
   ========================================================= */

// INHERITANCE: Organizer inherits common features from User.
class Organizer extends User
{
    // CONSTRUCTOR: Creates an Organizer object.
    Organizer(
            String name,
            String email,
            String password)
    {
        super(
                name,
                email,
                password,
                "ORGANIZER");
    }


    // METHOD OVERRIDING: This subclass provides its own implementation.
    @Override
    void loginSuccess()
    {
        System.out.println(
                "Organizer logged in.");
    }
}


/* =========================================================
   EVENT CLASS
   Stores event information
   ========================================================= */

// ENCAPSULATION: Event data is private and accessed through getters/setters.
class Event implements java.io.Serializable
{
    private String name;
    private String date;
    private String venue;
    private String description;
    private double price;
    private int capacity;
    private String ticketType;
    // ENCAPSULATION: Private field stores the owner of this event.
    private String organizerEmail;


    // CONSTRUCTOR: Creates an Event object.
    Event(
            String name,
            String date,
            String venue,
            String description,
            double price,
            int capacity,
            String ticketType,
            String organizerEmail)
    {
        this.name = name;
        this.date = date;
        this.venue = venue;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.ticketType = ticketType;
        this.organizerEmail = organizerEmail;
    }


    public String getName()
    {
        return name;
    }


    public String getDate()
    {
        return date;
    }


    public String getVenue()
    {
        return venue;
    }


    public String getDescription()
    {
        return description;
    }


    public double getPrice()
    {
        return price;
    }


    public int getCapacity()
    {
        return capacity;
    }


    public String getTicketType()
    {
        return ticketType;
    }

    public String getOrganizerEmail()
    {
        return organizerEmail;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public void setDate(String date)
    {
        this.date = date;
    }


    public void setVenue(String venue)
    {
        this.venue = venue;
    }


    public void setDescription(
            String description)
    {
        this.description = description;
    }


    public void setPrice(double price)
    {
        this.price = price;
    }


    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }
}


/* =========================================================
   BOOKING CLASS
   ========================================================= */

// ENCAPSULATION: Booking data is kept private and accessed through methods.
class Booking implements java.io.Serializable
{
    private String organizerEmail;
    private String id;
    private String email;
    private String eventName;
    private String date;
    private String venue;
    private String ticketType;
    private int quantity;
    private int amount;
    private String status;
    private String checkInStatus;


    // CONSTRUCTOR: Creates a Booking object.
    Booking(
            String id,
            String email,
            String eventName,
            String date,
            String venue,
            String ticketType,
            int quantity,
            int amount,
            String organizerEmail)
    {
        this.id = id;
        this.email = email;
        this.eventName = eventName;
        this.date = date;
        this.venue = venue;
        this.ticketType = ticketType;
        this.quantity = quantity;
        this.amount = amount;
        this.organizerEmail = organizerEmail;

        this.status = "CONFIRMED";

        this.checkInStatus =
                "NOT CHECKED IN";
    }


    public String getId()
    {
        return id;
    }


    public String getEmail()
    {
        return email;
    }


    public String getOrganizerEmail()
    {
        return organizerEmail;
    }


    public String getEventName()
    {
        return eventName;
    }


    public String getDate()
    {
        return date;
    }


    public String getVenue()
    {
        return venue;
    }


    public String getTicketType()
    {
        return ticketType;
    }


    public int getQuantity()
    {
        return quantity;
    }


    public int getAmount()
    {
        return amount;
    }


    public String getStatus()
    {
        return status;
    }


    public String getCheckInStatus()
    {
        return checkInStatus;
    }


    public void setCheckInStatus(
            String status)
    {
        this.checkInStatus = status;
    }
}


/* =========================================================
   PAYMENT ABSTRACT CLASS
   Abstraction + Polymorphism
   ========================================================= */

// ABSTRACTION: Payment declares common payment operations.
abstract class Payment
{
    abstract void pay(int amount);

    abstract String getMethod();
}


/* =========================================================
   UPI PAYMENT CLASS
   ========================================================= */

// INHERITANCE + POLYMORPHISM: UPI payment inherits Payment and overrides its methods.
class UPIPayment extends Payment
{
    // METHOD OVERRIDING: This subclass provides its own implementation.
    @Override
    void pay(int amount)
    {
        System.out.println(
                "UPI payment: Rs. "
                + amount);
    }


    // METHOD OVERRIDING: This subclass provides its own implementation.
    @Override
    String getMethod()
    {
        return "UPI";
    }
}


/* =========================================================
   CARD PAYMENT CLASS
   ========================================================= */

// INHERITANCE + POLYMORPHISM: Card payment inherits Payment and overrides its methods.
class CardPayment extends Payment
{
    // METHOD OVERRIDING: This subclass provides its own implementation.
    @Override
    void pay(int amount)
    {
        System.out.println(
                "Card payment: Rs. "
                + amount);
    }


    // METHOD OVERRIDING: This subclass provides its own implementation.
    @Override
    String getMethod()
    {
        return "Card";
    }
}


/* =========================================================
   CASH PAYMENT CLASS
   ========================================================= */

// INHERITANCE + POLYMORPHISM: Cash payment inherits Payment and overrides its methods.
class CashPayment extends Payment
{
    // METHOD OVERRIDING: This subclass provides its own implementation.
    @Override
    void pay(int amount)
    {
        System.out.println(
                "Cash payment: Rs. "
                + amount);
    }


    // METHOD OVERRIDING: This subclass provides its own implementation.
    @Override
    String getMethod()
    {
        return "Cash";
    }
}


/* =========================================================
   QR PANEL CLASS
   Simple QR-style visual for the digital ticket
   ========================================================= */

// INHERITANCE: QRPanel extends JPanel to create a custom Swing component.
class QRPanel extends JPanel
{
    QRPanel()
    {
        setPreferredSize(
                new Dimension(180, 180));

        setBackground(Color.WHITE);

        setBorder(
                BorderFactory.createLineBorder(
                        Color.BLACK));
    }


    protected void paintComponent(
            Graphics g)
    {
        super.paintComponent(g);

        g.setColor(Color.BLACK);


        int size = 8;

        int[][] pattern =
        {
            {1,1,1,1,1,0,1,0,1,1,0,1,1,1,1,1},
            {1,0,0,0,1,0,1,1,0,1,0,1,1,0,0,1},
            {1,0,1,0,1,1,0,1,1,0,1,1,1,0,1,1},
            {1,0,0,0,1,0,1,0,0,1,0,1,1,0,0,1},
            {1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1},
            {0,0,0,0,0,1,1,0,1,1,0,0,0,0,0,0},
            {1,1,0,1,1,0,1,1,0,1,1,0,1,1,0,1},
            {0,1,1,0,1,1,0,1,1,0,0,1,1,0,1,1},
            {1,0,1,1,0,0,1,0,1,1,1,0,1,0,1,0},
            {1,1,0,0,1,1,0,1,0,1,0,1,0,1,1,1},
            {0,1,1,1,0,1,1,0,1,0,1,1,1,0,0,1},
            {1,0,0,1,1,0,1,1,0,1,0,0,1,1,1,0},
            {1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1},
            {1,0,0,0,1,0,1,0,0,1,1,1,1,0,0,1},
            {1,0,1,0,1,1,0,1,1,0,1,1,1,0,1,1},
            {1,0,0,0,1,0,1,1,0,1,0,1,1,0,0,1},
            {1,1,1,1,1,0,1,0,1,1,0,1,1,1,1,1}
        };


        int startX = 22;
        int startY = 22;


        for (int i = 0;
                i < pattern.length;
                i++)
        {
            for (int j = 0;
                    j < pattern[i].length;
                    j++)
            {
                if (pattern[i][j] == 1)
                {
                    g.fillRect(
                            startX + j * size,
                            startY + i * size,
                            size,
                            size);
                }
            }
        }


        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        11));


        g.drawString(
                "EVENTHUB",
                55,
                170);
    }
}

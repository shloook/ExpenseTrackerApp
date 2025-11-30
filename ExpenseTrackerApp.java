// ExpenseTrackerApp.java
// Premium-styled single-file Expense Tracker (OpenJDK 24). Clean version, normal button height.

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.RoundRectangle2D;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.*;
import java.util.stream.Collectors;

public class ExpenseTrackerApp {

    private static final String DATA_FILE = "expenses.csv";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] DEFAULT_CATEGORIES =
            {"Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Subscriptions", "Other"};

    // MODEL
    static final class Expense {
        double amount;
        String category;
        LocalDate date;
        String note;
        UUID id;

        Expense(double amount, String category, LocalDate date, String note) {
            this.amount = amount;
            this.category = category;
            this.date = date;
            this.note = note == null ? "" : note;
            this.id = UUID.randomUUID();
        }

        Expense(UUID id, double amount, String category, LocalDate date, String note) {
            this.amount = amount;
            this.category = category;
            this.date = date;
            this.note = note == null ? "" : note;
            this.id = id;
        }

        String toCsvLine() {
            return String.format("%s,%.2f,%s,%s,%s",
                    id, amount, escapeCsv(category), date.format(DATE_FMT), escapeCsv(note));
        }

        static String escapeCsv(String s) {
            if (s == null) return "";
            if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
                return "\"" + s.replace("\"", "\"\"") + "\"";
            }
            return s;
        }

        static Expense fromCsv(String line) {
            try {
                List<String> p = parseCsvLine(line);
                return new Expense(
                        UUID.fromString(p.get(0)),
                        Double.parseDouble(p.get(1)),
                        p.get(2),
                        LocalDate.parse(p.get(3), DATE_FMT),
                        p.get(4)
                );
            } catch (Exception e) {
                return null;
            }
        }

        // CSV parser
        static List<String> parseCsvLine(String line) {
            List<String> out = new ArrayList<>();
            StringBuilder cur = new StringBuilder();
            boolean q = false;

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (q) {
                    if (c == '"') {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            cur.append('"'); i++;
                        } else q = false;
                    } else cur.append(c);
                } else {
                    if (c == '"') q = true;
                    else if (c == ',') { out.add(cur.toString()); cur.setLength(0); }
                    else cur.append(c);
                }
            }
            out.add(cur.toString());
            return out;
        }
    }

    // TABLE MODEL
    static final class ExpenseTableModel extends javax.swing.table.AbstractTableModel {

        private final String[] cols = {"Date", "Category", "Note", "Amount (₹)"};
        private final List<Expense> expenses;

        ExpenseTableModel(List<Expense> list) { this.expenses = list; }

        @Override public int getRowCount() { return expenses.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Class<?> getColumnClass(int c) { return c == 3 ? Double.class : String.class; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override
        public Object getValueAt(int r, int c) {
            Expense e = expenses.get(r);
            return switch (c) {
                case 0 -> e.date.format(DATE_FMT);
                case 1 -> e.category;
                case 2 -> e.note;
                case 3 -> e.amount;
                default -> "";
            };
        }

        Expense get(int r) { return expenses.get(r); }

        void add(Expense e) { expenses.add(0, e); fireTableRowsInserted(0, 0); }
        void remove(int r) { expenses.remove(r); fireTableRowsDeleted(r, r); }

        List<Expense> all() { return expenses; }

        void setAll(List<Expense> list) {
            expenses.clear();
            expenses.addAll(list);
            fireTableDataChanged();
        }
    }

    // UI STATE
    private final JFrame frame = new JFrame("Expense Tracker");
    private final ExpenseTableModel tableModel = new ExpenseTableModel(new ArrayList<>());
    private final JTable table = new JTable(tableModel);

    private final JTextField amountField = new JTextField();
    private final JComboBox<String> categoryBox = new JComboBox<>(DEFAULT_CATEGORIES);
    private final JTextField dateField = new JTextField(LocalDate.now().format(DATE_FMT));
    private final JTextField noteField = new JTextField();

    private final JLabel totalLabel = new JLabel("Total: ₹0.00");

    private final JButton addBtn = new PremiumButton("Add");
    private final JButton deleteBtn = new PremiumButton("Delete");
    private final JButton exportBtn = new PremiumButton("Export");

    // CONSTRUCTOR
    public ExpenseTrackerApp() {
        applyGlobalFont(14f);
        setupUI();
        loadExpenses();
        updateTotal();
    }

    private void applyGlobalFont(float size) {
        FontUIResource f = new FontUIResource("SansSerif", Font.PLAIN, Math.round(size));
        UIManager.getDefaults().keySet().forEach(k -> {
            Object v = UIManager.get(k);
            if (v instanceof FontUIResource) UIManager.put(k, f);
        });
    }

    // BUILD UI
    private void setupUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(960, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(12,12));
        frame.getRootPane().setBorder(new EmptyBorder(12,12,12,12));

        // HEADER (clean)
        RoundedPanel header = new RoundedPanel();
        header.setPadding(14);
        header.setLayout(new BorderLayout());
        JLabel title = new JLabel("Expense Tracker");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.add(title, BorderLayout.WEST);
        frame.add(header, BorderLayout.NORTH);

        // LEFT FORM
        RoundedPanel left = new RoundedPanel();
        left.setPadding(12);
        left.setPreferredSize(new Dimension(360, 0));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(formRow("Amount (₹)", amountField));

        categoryBox.setPrototypeDisplayValue("MMMMMMMMMMMM");
        categoryBox.setRenderer(new ComboRenderer());
        left.add(formRow("Category", categoryBox));

        left.add(formRow("Date (YYYY-MM-DD)", dateField));
        left.add(formRow("Note", noteField));

        JPanel btnRow = new JPanel(new GridLayout(1,3,10,10));
        btnRow.setOpaque(false);

        styleButton(addBtn, new Color(48, 140, 240));
        styleButton(deleteBtn, new Color(231,76,60));
        styleButton(exportBtn, new Color(46, 204, 113));

        btnRow.add(addBtn);
        btnRow.add(deleteBtn);
        btnRow.add(exportBtn);

        left.add(Box.createVerticalStrut(8));
        left.add(btnRow);
        left.add(Box.createVerticalStrut(12));

        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 14f));
        left.add(totalLabel);

        frame.add(left, BorderLayout.WEST);

        // TABLE
        table.setRowHeight(34);
        table.setIntercellSpacing(new Dimension(6,6));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        JTableHeader th = table.getTableHeader();
        th.setPreferredSize(new Dimension(th.getPreferredSize().width, 34));
        th.setFont(new Font("SansSerif", Font.BOLD, 13));

        table.setDefaultRenderer(Object.class, new StripeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(10,10,10,10));
        frame.add(scroll, BorderLayout.CENTER);

        // FOOTER (clean)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6,6,6,6));
        footer.add(new JLabel("Saved to: " + Paths.get(DATA_FILE).toAbsolutePath()), BorderLayout.WEST);
        frame.add(footer, BorderLayout.SOUTH);

        // EVENTS
        addBtn.addActionListener(e -> addExpense());
        deleteBtn.addActionListener(e -> deleteExpense());
        exportBtn.addActionListener(e -> exportCSV());

        table.getSelectionModel().addListSelectionListener(this::onSelectionChanged);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r >= 0) {
                        r = table.convertRowIndexToModel(r);
                        Expense ex = tableModel.get(r);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                                new StringSelection(ex.note), null);
                        JOptionPane.showMessageDialog(frame, "Note copied.");
                    }
                }
            }
        });

        frame.getRootPane().setDefaultButton(addBtn);
        frame.setVisible(true);
    }

    private JPanel formRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(130,22));
        p.add(l, BorderLayout.WEST);
        comp.setPreferredSize(new Dimension(220,28));
        p.add(comp, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        return p;
    }

    private void styleButton(JButton b, Color color) {
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setBorder(new EmptyBorder(6,10,6,10));  // ✔ normal height
        b.setFocusPainted(false);
    }

    // ACTIONS
    private void addExpense() {
        try {
            double amt = Double.parseDouble(amountField.getText().trim());
            if (amt < 0) throw new Exception();

            LocalDate dt = LocalDate.parse(dateField.getText().trim(), DATE_FMT);
            String cat = Objects.toString(categoryBox.getSelectedItem());
            String note = noteField.getText().trim();

            Expense e = new Expense(amt, cat, dt, note);
            tableModel.add(e);
            saveExpenses();
            amountField.setText("");
            noteField.setText("");
            updateTotal();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input.");
        }
    }

    private void deleteExpense() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        row = table.convertRowIndexToModel(row);
        int c = JOptionPane.showConfirmDialog(frame, "Delete this entry?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            tableModel.remove(row);
            saveExpenses();
            updateTotal();
        }
    }

    private void exportCSV() {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File("expenses-export.csv"));
        if (ch.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(ch.getSelectedFile())) {
                pw.println("id,amount,category,date,note");
                for (Expense e : tableModel.all()) pw.println(e.toCsvLine());
                JOptionPane.showMessageDialog(frame, "Exported.");
            } catch (Exception ignored) {}
        }
    }

    private void saveExpenses() {
        try (PrintWriter pw = new PrintWriter(DATA_FILE)) {
            pw.println("id,amount,category,date,note");
            for (Expense e : tableModel.all()) pw.println(e.toCsvLine());
        } catch (Exception ignored) {}
    }

    private void loadExpenses() {
        try {
            Path p = Paths.get(DATA_FILE);
            if (!Files.exists(p)) return;

            List<String> lines = Files.readAllLines(p);
            List<Expense> list = lines.stream()
                    .skip(1)
                    .map(Expense::fromCsv)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Collections.reverse(list);
            tableModel.setAll(list);
        } catch (Exception ignored) {}
    }

    private void updateTotal() {
        double t = tableModel.all().stream().mapToDouble(e -> e.amount).sum();
        totalLabel.setText("Total: ₹" + String.format("%.2f", t));
    }

    private void onSelectionChanged(ListSelectionEvent e) {
        deleteBtn.setEnabled(table.getSelectedRow() >= 0);
    }

    // ROUNDED PANEL
    static class RoundedPanel extends JPanel {
        private int pad = 10;
        void setPadding(int p) { pad = p; }
        RoundedPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0,
                    new Color(255,255,255), 0, getHeight(), new Color(245,246,248));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            g2.setColor(new Color(220,220,225));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // PREMIUM BUTTON (normal height)
    static class PremiumButton extends JButton {
        PremiumButton(String s) {
            super(s);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, getBackground().brighter(),
                    0, getHeight(), getBackground().darker()
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g2.setColor(getForeground());
            g2.setFont(getFont().deriveFont(Font.BOLD));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
        }
    }

    // COMBO RENDERER
    static class ComboRenderer extends JLabel implements ListCellRenderer<String> {
        ComboRenderer() { setOpaque(true); setBorder(new EmptyBorder(6,8,6,8)); }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);
            setBackground(isSelected ? new Color(220,235,255) : Color.WHITE);
            setForeground(Color.BLACK);
            return this;
        }
    }

    // TABLE STRIPES
    static class StripeRenderer extends DefaultTableCellRenderer {
        private final Color even = new Color(250,250,252);
        private final Color odd = new Color(245,247,250);

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Component cp = super.getTableCellRendererComponent(t, v, sel, f, r, c);
            cp.setBackground(sel ? new Color(195,215,255) : (r % 2 == 0 ? even : odd));
            setBorder(new EmptyBorder(6,8,6,8));
            return cp;
        }
    }

    // MAIN
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerApp::new);
    }
}

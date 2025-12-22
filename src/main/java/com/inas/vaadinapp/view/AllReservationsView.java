package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Reservation;
import com.inas.vaadinapp.entity.ReservationStatus;
import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.ReservationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Toutes les réservations - Admin")
@Route("admin/reservations")
public class AllReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final ReservationService reservationService;

    private Grid<Reservation> grid;
    private ListDataProvider<Reservation> dataProvider;

    // Filtres
    private ComboBox<ReservationStatus> statusFilter;
    private TextField codeFilter;
    private TextField userFilter;
    private TextField eventFilter;
    private DatePicker dateMinFilter;
    private DatePicker dateMaxFilter;

    // Statistiques
    private Div totalCard;
    private Div confirmedCard;
    private Div pendingCard;
    private Div cancelledCard;
    private Div revenueCard;
    private Div placesCard;

    public AllReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8f9fa");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Vérifier admin
        var current = VaadinSession.getCurrent().getAttribute(User.class);
        if (current == null || current.getRole() != Role.ADMIN) {
            event.rerouteTo("login");
            return;
        }

        // Construire la vue seulement si l'authentification est OK
        if (getComponentCount() == 0) {
            createHeader();
            createStats();
            createFilters();
            createGrid();
            createActions();
            loadData();
        }
    }

    private void createHeader() {
        H1 title = new H1("Toutes les réservations");
        title.getStyle().set("color", "#333").set("margin", "0");
        Span subtitle = new Span("Vue globale de toutes les réservations de la plateforme");
        subtitle.getStyle().set("color", "#666");
        add(title, subtitle);
    }

    private void createStats() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        totalCard = createStatCard("🎫 Total", "0");
        confirmedCard = createStatCard("✅ Confirmées", "0");
        pendingCard = createStatCard("⏳ En attente", "0");
        cancelledCard = createStatCard("❌ Annulées", "0");
        revenueCard = createStatCard("💰 Revenus", "0.00 €");
        placesCard = createStatCard("👥 Places réservées", "0");

        stats.add(totalCard, confirmedCard, pendingCard, cancelledCard, revenueCard, placesCard);
        add(stats);
    }

    private Div createStatCard(String label, String value) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("padding", "1rem")
                .set("min-width", "160px")
                .set("text-align", "center");

        Span title = new Span(label);
        title.getStyle().set("color", "#666").set("font-size", "0.9rem");
        H2 val = new H2(value);
        val.getStyle().set("margin", "0").set("color", "#333");

        card.add(title, val);
        return card;
    }

    private void createFilters() {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(true);
        box.setSpacing(true);
        box.setWidthFull();
        box.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setClearButtonVisible(true);

        codeFilter = new TextField("Code réservation");
        codeFilter.setClearButtonVisible(true);

        userFilter = new TextField("Utilisateur (nom/email)");
        userFilter.setClearButtonVisible(true);

        eventFilter = new TextField("Événement (titre)");
        eventFilter.setClearButtonVisible(true);

        dateMinFilter = new DatePicker("Date min");
        dateMaxFilter = new DatePicker("Date max");

        HorizontalLayout row1 = new HorizontalLayout(statusFilter, codeFilter, userFilter, eventFilter);
        row1.setWidthFull();
        HorizontalLayout row2 = new HorizontalLayout(dateMinFilter, dateMaxFilter);
        row2.setWidthFull();

        Button apply = new Button("Appliquer", new Icon(VaadinIcon.FILTER));
        apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        apply.addClickListener(e -> applyFilters());

        Button reset = new Button("Réinitialiser", new Icon(VaadinIcon.REFRESH));
        reset.addClickListener(e -> resetFilters());

        HorizontalLayout actions = new HorizontalLayout(apply, reset);

        box.add(new Span("Filtres avancés"), row1, row2, actions);
        add(box);
    }

    private void createGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.setWidthFull();

        grid.addColumn(Reservation::getCodeReservation).setHeader("Code").setSortable(true).setAutoWidth(true);
        grid.addColumn(r -> r.getClient() != null ? r.getClient().getPrenom() + " " + r.getClient().getNom() : "N/A")
                .setHeader("Utilisateur").setAutoWidth(true);
        grid.addColumn(r -> r.getClient() != null ? r.getClient().getEmail() : "N/A")
                .setHeader("Email").setAutoWidth(true);
        grid.addColumn(r -> r.getEvent() != null ? r.getEvent().getTitre() : "N/A")
                .setHeader("Événement").setAutoWidth(true);
        grid.addColumn(r -> r.getStatus() != null ? r.getStatus().toString() : "")
                .setHeader("Statut").setAutoWidth(true);
        grid.addColumn(Reservation::getNbPlaces).setHeader("Places").setSortable(true).setAutoWidth(true);
        grid.addColumn(r -> String.format("%.2f €", r.getMontantTotal())).setHeader("Montant").setSortable(true).setAutoWidth(true);
        grid.addColumn(r -> r.getDateReservation() != null ? r.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                .setHeader("Date").setSortable(true).setAutoWidth(true);

        grid.addComponentColumn(this::createActions).setHeader("Actions").setAutoWidth(true);

        add(grid);
    }

    private HorizontalLayout createActions(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button view = new Button(new Icon(VaadinIcon.EYE));
        view.getElement().setAttribute("title", "Voir détails");
        view.addClickListener(e -> showDetails(reservation));

        actions.add(view);
        return actions;
    }

    private void createActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button export = new Button("Exporter CSV", new Icon(VaadinIcon.DOWNLOAD));
        export.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        export.addClickListener(e -> exportCsv());

        actions.add(export);
        add(actions);
    }

    private void loadData() {
        List<Reservation> reservations = reservationService.findAllReservations().stream()
                .sorted((r1, r2) -> r2.getDateReservation().compareTo(r1.getDateReservation()))
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(reservations);
        grid.setDataProvider(dataProvider);

        updateStats(reservations);
    }

    private void updateStats(List<Reservation> reservations) {
        long total = reservations.size();
        long pending = reservations.stream().filter(r -> r.getStatus() == ReservationStatus.EN_ATTENTE).count();
        long confirmed = reservations.stream().filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE).count();
        long cancelled = reservations.stream().filter(r -> r.getStatus() == ReservationStatus.ANNULEE).count();
        int places = reservations.stream()
                .filter(r -> r.getStatus() != ReservationStatus.ANNULEE)
                .mapToInt(Reservation::getNbPlaces).sum();
        double revenue = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal).sum();

        setCardValue(totalCard, String.valueOf(total));
        setCardValue(confirmedCard, String.valueOf(confirmed));
        setCardValue(pendingCard, String.valueOf(pending));
        setCardValue(cancelledCard, String.valueOf(cancelled));
        setCardValue(placesCard, String.valueOf(places));
        setCardValue(revenueCard, String.format("%.2f €", revenue));
    }

    private void setCardValue(Div card, String value) {
        card.getChildren()
                .filter(c -> c instanceof H2)
                .findFirst()
                .ifPresent(h -> ((H2) h).setText(value));
    }

    private void applyFilters() {
        dataProvider.clearFilters();
        dataProvider.addFilter(reservation -> {
            if (statusFilter.getValue() != null && reservation.getStatus() != statusFilter.getValue()) return false;

            if (codeFilter.getValue() != null && !codeFilter.getValue().trim().isEmpty()) {
                if (reservation.getCodeReservation() == null ||
                        !reservation.getCodeReservation().toLowerCase().contains(codeFilter.getValue().toLowerCase().trim())) {
                    return false;
                }
            }

            if (userFilter.getValue() != null && !userFilter.getValue().trim().isEmpty()) {
                String term = userFilter.getValue().toLowerCase().trim();
                String name = reservation.getClient() != null
                        ? (reservation.getClient().getPrenom() + " " + reservation.getClient().getNom()).toLowerCase()
                        : "";
                String email = reservation.getClient() != null && reservation.getClient().getEmail() != null
                        ? reservation.getClient().getEmail().toLowerCase() : "";
                if (!name.contains(term) && !email.contains(term)) return false;
            }

            if (eventFilter.getValue() != null && !eventFilter.getValue().trim().isEmpty()) {
                String term = eventFilter.getValue().toLowerCase().trim();
                String title = reservation.getEvent() != null && reservation.getEvent().getTitre() != null
                        ? reservation.getEvent().getTitre().toLowerCase() : "";
                if (!title.contains(term)) return false;
            }

            if (dateMinFilter.getValue() != null) {
                LocalDate date = reservation.getDateReservation() != null ? reservation.getDateReservation().toLocalDate() : null;
                if (date == null || date.isBefore(dateMinFilter.getValue())) return false;
            }

            if (dateMaxFilter.getValue() != null) {
                LocalDate date = reservation.getDateReservation() != null ? reservation.getDateReservation().toLocalDate() : null;
                if (date == null || date.isAfter(dateMaxFilter.getValue())) return false;
            }

            return true;
        });
    }

    private void resetFilters() {
        statusFilter.clear();
        codeFilter.clear();
        userFilter.clear();
        eventFilter.clear();
        dateMinFilter.clear();
        dateMaxFilter.clear();
        applyFilters();
    }

    private void showDetails(Reservation reservation) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Détails réservation");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        content.add(new Span("Code : " + reservation.getCodeReservation()));
        content.add(new Span("Utilisateur : " + (reservation.getClient() != null
                ? reservation.getClient().getPrenom() + " " + reservation.getClient().getNom()
                : "N/A")));
        content.add(new Span("Email : " + (reservation.getClient() != null ? reservation.getClient().getEmail() : "N/A")));
        content.add(new Span("Événement : " + (reservation.getEvent() != null ? reservation.getEvent().getTitre() : "N/A")));
        content.add(new Span("Statut : " + reservation.getStatus()));
        content.add(new Span("Places : " + reservation.getNbPlaces()));
        content.add(new Span("Montant : " + String.format("%.2f €", reservation.getMontantTotal())));
        content.add(new Span("Date : " + (reservation.getDateReservation() != null
                ? reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "N/A")));
        if (reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty()) {
            content.add(new Span("Commentaire : " + reservation.getCommentaire()));
        }

        dialog.add(content);

        Button close = new Button("Fermer", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(close);

        dialog.open();
    }

    private void exportCsv() {
        List<Reservation> items = dataProvider.getItems().stream().collect(Collectors.toList());

        StringBuilder csv = new StringBuilder();
        csv.append("Code,Utilisateur,Email,Evenement,Statut,Places,Montant,Date\\n");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Reservation r : items) {
            csv.append(r.getCodeReservation()).append(",");
            csv.append("\"").append(r.getClient() != null ? r.getClient().getPrenom() + " " + r.getClient().getNom() : "N/A").append("\",");
            csv.append(r.getClient() != null ? r.getClient().getEmail() : "").append(",");
            csv.append("\"").append(r.getEvent() != null ? r.getEvent().getTitre() : "N/A").append("\",");
            csv.append(r.getStatus()).append(",");
            csv.append(r.getNbPlaces()).append(",");
            csv.append(String.format("%.2f", r.getMontantTotal())).append(",");
            csv.append(r.getDateReservation() != null ? r.getDateReservation().format(df) : "").append("\\n");
        }

        StreamResource resource = new StreamResource("reservations.csv",
                () -> new ByteArrayInputStream(csv.toString().getBytes()));
        Anchor download = new Anchor(resource, "Télécharger CSV");
        download.getElement().setAttribute("download", true);
        download.getStyle().set("display", "none");
        add(download);
        download.getElement().callJsFunction("click");
        remove(download);

        Notification.show("Export CSV généré", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}


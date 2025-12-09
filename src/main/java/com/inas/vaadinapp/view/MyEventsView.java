package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.EventService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Mes Événements - EventManager")
@Route("organizer/events")
public class MyEventsView extends VerticalLayout {

    private final EventService eventService;

    private ListDataProvider<Event> dataProvider;
    private Grid<Event> grid;
    private ComboBox<EventStatus> statusFilter;

    public MyEventsView(EventService eventService) {
        this.eventService = eventService;

        // Vérifier si l'utilisateur est connecté et est un organisateur
        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        if (currentUser.getRole() != com.inas.vaadinapp.entity.Role.ORGANIZER &&
            currentUser.getRole() != com.inas.vaadinapp.entity.Role.ADMIN) {
            UI.getCurrent().navigate("dashboard");
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8f9fa");

        createHeader();
        createFiltersSection();
        createGridSection();
        loadEvents(currentUser.getId());
    }

    /* -------------------- HEADER -------------------- */

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("Mes Événements");
        title.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "0.5rem")
                .set("text-align", "center");

        Span subtitle = new Span("Gérez tous vos événements depuis cette interface");
        subtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "1.1rem")
                .set("text-align", "center");

        // Bouton créer un événement
        Button createButton = new Button("Créer un événement", new Icon(VaadinIcon.PLUS));
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("color", "white");
        createButton.addClickListener(e -> UI.getCurrent().navigate("organizer/event/new"));

        header.add(title, subtitle, createButton);
        add(header);
    }

    /* -------------------- FILTRES -------------------- */

    private void createFiltersSection() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setPadding(true);
        filtersSection.setSpacing(true);
        filtersSection.setWidthFull();
        filtersSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H3 filtersTitle = new H3("🔍 Filtrer par statut");

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setItemLabelGenerator(status -> getStatusLabel(status));
        statusFilter.setClearButtonVisible(true);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button resetBtn = new Button("Réinitialiser", new Icon(VaadinIcon.REFRESH));
        resetBtn.addClickListener(e -> {
            statusFilter.clear();
            applyFilters();
        });

        filtersSection.add(filtersTitle, new HorizontalLayout(statusFilter, resetBtn));
        add(filtersSection);
    }

    /* -------------------- GRID -------------------- */

    private void createGridSection() {
        grid = new Grid<>(Event.class, false);
        configureGrid();
        grid.setSizeFull();

        VerticalLayout gridContainer = new VerticalLayout();
        gridContainer.setPadding(false);
        gridContainer.setSpacing(false);
        gridContainer.setSizeFull();
        gridContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("padding", "1rem");

        gridContainer.add(grid);
        add(gridContainer);
    }

    private void configureGrid() {
        // Titre
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true)
                .setSortable(true);

        // Catégorie
        grid.addColumn(event -> event.getCategorie().toString())
                .setHeader("Catégorie")
                .setAutoWidth(true);

        // Date de début
        grid.addColumn(event ->
                event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ).setHeader("Date de début")
                .setAutoWidth(true)
                .setSortable(true);

        // Statut avec style
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Statut")
                .setAutoWidth(true);

        // Places réservées / totales avec indicateur visuel
        grid.addComponentColumn(this::createCapacityColumn)
                .setHeader("Places réservées")
                .setAutoWidth(true);

        // Prix
        grid.addColumn(event -> String.format("%.2f €", event.getPrixUnitaire()))
                .setHeader("Prix")
                .setAutoWidth(true);

        // Actions
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Actions")
                .setAutoWidth(true);
    }

    private Span createStatusBadge(Event event) {
        Span badge = new Span(getStatusLabel(event.getStatus()));
        badge.getStyle()
                .set("padding", "0.25rem 0.5rem")
                .set("border-radius", "12px")
                .set("font-size", "0.85rem")
                .set("font-weight", "bold")
                .set("color", "white")
                .set("background-color", getStatusColor(event.getStatus()));

        return badge;
    }

    private VerticalLayout createCapacityColumn(Event event) {
        VerticalLayout capacityLayout = new VerticalLayout();
        capacityLayout.setPadding(false);
        capacityLayout.setSpacing(false);
        capacityLayout.setAlignItems(Alignment.START);

        int reserved = eventService.getAvailablePlaces(event.getId());
        int total = event.getCapaciteMax();
        int occupied = total - reserved;

        // Texte des places
        Span capacityText = new Span(occupied + " / " + total + " places");
        capacityText.getStyle().set("font-size", "0.9rem");

        // Barre de progression
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMin(0);
        progressBar.setMax(total);
        progressBar.setValue(occupied);
        progressBar.setWidth("100px");

        // Couleur selon le taux de remplissage
        double fillRate = (double) occupied / total;
        if (fillRate >= 0.9) {
            progressBar.getStyle().set("--lumo-primary-color", "#dc3545"); // Rouge
        } else if (fillRate >= 0.7) {
            progressBar.getStyle().set("--lumo-primary-color", "#ffc107"); // Jaune
        } else {
            progressBar.getStyle().set("--lumo-primary-color", "#28a745"); // Vert
        }

        capacityLayout.add(capacityText, progressBar);
        return capacityLayout;
    }

    private HorizontalLayout createActionButtons(Event event) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Bouton Voir
        Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
        viewBtn.getElement().setAttribute("title", "Voir les détails");
        viewBtn.addClickListener(e -> UI.getCurrent().navigate("event/" + event.getId()));

        // Bouton Modifier
        Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
        editBtn.getElement().setAttribute("title", "Modifier l'événement");
        editBtn.addClickListener(e -> UI.getCurrent().navigate("organizer/event/" + event.getId()));

        // Bouton Publier (seulement si BROUILLON)
        Button publishBtn = new Button(new Icon(VaadinIcon.CHECK));
        publishBtn.getElement().setAttribute("title", "Publier l'événement");
        publishBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        publishBtn.setVisible(event.getStatus() == EventStatus.BROUILLON);
        publishBtn.addClickListener(e -> publishEvent(event));

        // Bouton Annuler (seulement si PUBLIE)
        Button cancelBtn = new Button(new Icon(VaadinIcon.CLOSE));
        cancelBtn.getElement().setAttribute("title", "Annuler l'événement");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.setVisible(event.getStatus() == EventStatus.PUBLIE);
        cancelBtn.addClickListener(e -> cancelEvent(event));

        // Bouton Voir réservations
        Button reservationsBtn = new Button(new Icon(VaadinIcon.TICKET));
        reservationsBtn.getElement().setAttribute("title", "Voir les réservations");
        reservationsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        reservationsBtn.addClickListener(e -> UI.getCurrent().navigate("organizer/event/reservations/" + event.getId()));

        // Bouton Supprimer (seulement si BROUILLON et pas de réservations)
        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.getElement().setAttribute("title", "Supprimer l'événement");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.setVisible(event.getStatus() == EventStatus.BROUILLON && event.getReservations().isEmpty());
        deleteBtn.addClickListener(e -> deleteEvent(event));

        actions.add(viewBtn, editBtn, reservationsBtn, publishBtn, cancelBtn, deleteBtn);
        return actions;
    }

    /* -------------------- ACTIONS -------------------- */

    private void publishEvent(Event event) {
        try {
            User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
            eventService.publishEvent(event.getId(), currentUser.getId());

            Notification notification = new Notification("Événement publié avec succès !", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();

            refreshData(currentUser.getId());
        } catch (Exception e) {
            Notification notification = new Notification("Erreur lors de la publication : " + e.getMessage(), 5000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
        }
    }

    private void cancelEvent(Event event) {
        try {
            User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
            eventService.cancelEvent(event.getId(), currentUser.getId());

            Notification notification = new Notification("Événement annulé avec succès !", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();

            refreshData(currentUser.getId());
        } catch (Exception e) {
            Notification notification = new Notification("Erreur lors de l'annulation : " + e.getMessage(), 5000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
        }
    }

    private void deleteEvent(Event event) {
        try {
            eventService.deleteEvent(event.getId());

            Notification notification = new Notification("Événement supprimé avec succès !", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();

            refreshData(VaadinSession.getCurrent().getAttribute(User.class).getId());
        } catch (Exception e) {
            Notification notification = new Notification("Erreur lors de la suppression : " + e.getMessage(), 5000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
        }
    }

    /* -------------------- UTILITAIRES -------------------- */

    private void loadEvents(Long userId) {
        List<Event> events = eventService.findAll().stream()
                .filter(e -> e.getOrganisateur() != null && e.getOrganisateur().getId().equals(userId))
                .sorted(Comparator.comparing(Event::getDateCreation).reversed())
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(events);
        grid.setDataProvider(dataProvider);
    }

    private void applyFilters() {
        dataProvider.setFilter(event -> {
            if (statusFilter.getValue() != null) {
                return event.getStatus() == statusFilter.getValue();
            }
            return true;
        });
    }

    private void refreshData(Long userId) {
        loadEvents(userId);
        applyFilters();
    }

    private String getStatusLabel(EventStatus status) {
        switch (status) {
            case BROUILLON: return "Brouillon";
            case PUBLIE: return "Publié";
            case ANNULE: return "Annulé";
            case TERMINE: return "Terminé";
            default: return status.toString();
        }
    }

    private String getStatusColor(EventStatus status) {
        switch (status) {
            case BROUILLON: return "#6c757d";
            case PUBLIE: return "#28a745";
            case ANNULE: return "#dc3545";
            case TERMINE: return "#17a2b8";
            default: return "#666";
        }
    }
}

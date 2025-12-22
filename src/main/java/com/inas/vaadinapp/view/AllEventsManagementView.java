package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Category;
import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.EventService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Gestion des événements - Admin")
@Route("admin/events")
public class AllEventsManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private ListDataProvider<Event> dataProvider;
    private Grid<Event> grid;

    private final ComboBox<Category> categoryFilter = new ComboBox<>("Catégorie");
    private final ComboBox<EventStatus> statusFilter = new ComboBox<>("Statut");
    private final TextField organizerFilter = new TextField("Organisateur (nom/email)");
    private final TextField villeFilter = new TextField("Ville");
    private final DatePicker dateMinFilter = new DatePicker("Date début min");
    private final DatePicker dateMaxFilter = new DatePicker("Date début max");
    private final NumberField prixMinFilter = new NumberField("Prix min (€)");
    private final NumberField prixMaxFilter = new NumberField("Prix max (€)");
    private final TextField keywordFilter = new TextField("Mot-clé titre");

    public AllEventsManagementView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8f9fa");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Vérification ADMIN
        User current = VaadinSession.getCurrent().getAttribute(User.class);
        if (current == null || current.getRole() != Role.ADMIN) {
            event.rerouteTo("login");
            return;
        }

        // Construire la vue seulement si l'authentification est OK
        if (getComponentCount() == 0) {
            createHeader();
            createFilters();
            createGrid();
            loadEvents();
        }
    }

    private void createHeader() {
        H1 title = new H1("Gestion des événements (Admin)");
        title.getStyle().set("color", "#333").set("margin-bottom", "0.5rem");

        Span subtitle = new Span("Vue globale de tous les événements de tous les organisateurs");
        subtitle.getStyle().set("color", "#666");

        add(title, subtitle);
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

        HorizontalLayout row1 = new HorizontalLayout(categoryFilter, statusFilter, organizerFilter, keywordFilter);
        row1.setWidthFull();

        HorizontalLayout row2 = new HorizontalLayout(villeFilter, dateMinFilter, dateMaxFilter);
        row2.setWidthFull();

        HorizontalLayout row3 = new HorizontalLayout(prixMinFilter, prixMaxFilter);
        row3.setWidthFull();

        Button apply = new Button("Appliquer", new Icon(VaadinIcon.FILTER));
        apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        apply.addClickListener(e -> applyFilters());

        Button reset = new Button("Réinitialiser", new Icon(VaadinIcon.REFRESH));
        reset.addClickListener(e -> resetFilters());

        HorizontalLayout actions = new HorizontalLayout(apply, reset);

        configureFilterFields();
        box.add(new Span("Filtres"), row1, row2, row3, actions);
        add(box);
    }

    private void configureFilterFields() {
        categoryFilter.setItems(Category.values());
        categoryFilter.setClearButtonVisible(true);

        statusFilter.setItems(EventStatus.values());
        statusFilter.setClearButtonVisible(true);

        organizerFilter.setPlaceholder("Nom ou email organisateur");
        organizerFilter.setClearButtonVisible(true);

        keywordFilter.setPlaceholder("Mot clé dans le titre");
        keywordFilter.setClearButtonVisible(true);

        villeFilter.setPlaceholder("Ville");
        villeFilter.setClearButtonVisible(true);

        prixMinFilter.setMin(0);
        prixMaxFilter.setMin(0);
        prixMinFilter.setStep(1);
        prixMaxFilter.setStep(1);
    }

    private void createGrid() {
        grid = new Grid<>(Event.class, false);
        grid.setWidthFull();

        grid.addColumn(Event::getTitre).setHeader("Titre").setSortable(true).setAutoWidth(true);
        grid.addColumn(e -> e.getCategorie() != null ? e.getCategorie().toString() : "").setHeader("Catégorie").setAutoWidth(true);
        grid.addColumn(e -> e.getOrganisateur() != null ? e.getOrganisateur().getEmail() : "N/A").setHeader("Organisateur").setAutoWidth(true);
        grid.addColumn(e -> e.getDateDebut() != null ? e.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                .setHeader("Date début").setSortable(true).setAutoWidth(true);
        grid.addColumn(e -> e.getStatus() != null ? e.getStatus().toString() : "").setHeader("Statut").setAutoWidth(true);
        grid.addColumn(e -> String.format("%.2f €", e.getPrixUnitaire() != null ? e.getPrixUnitaire() : 0.0)).setHeader("Prix").setAutoWidth(true);
        grid.addColumn(e -> e.getVille()).setHeader("Ville").setAutoWidth(true);
        grid.addComponentColumn(this::createActions).setHeader("Actions").setAutoWidth(true);

        add(grid);
    }

    private HorizontalLayout createActions(Event event) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button view = new Button(new Icon(VaadinIcon.EYE));
        view.getElement().setAttribute("title", "Voir");
        view.addClickListener(e -> UI.getCurrent().navigate("event/" + event.getId()));

        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.getElement().setAttribute("title", "Modifier");
        edit.addClickListener(e -> UI.getCurrent().navigate("organizer/event/" + event.getId()));

        Button publish = new Button(new Icon(VaadinIcon.CHECK));
        publish.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        publish.setVisible(event.getStatus() == EventStatus.BROUILLON);
        publish.getElement().setAttribute("title", "Publier");
        publish.addClickListener(e -> publishEvent(event));

        Button cancel = new Button(new Icon(VaadinIcon.CLOSE));
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.setVisible(event.getStatus() == EventStatus.PUBLIE);
        cancel.getElement().setAttribute("title", "Annuler");
        cancel.addClickListener(e -> cancelEvent(event));

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        delete.setVisible(event.getReservations() == null || event.getReservations().isEmpty());
        delete.getElement().setAttribute("title", "Supprimer");
        delete.addClickListener(e -> deleteEvent(event));

        actions.add(view, edit, publish, cancel, delete);
        return actions;
    }

    private void loadEvents() {
        List<Event> events = eventService.findAll().stream()
                .sorted(Comparator.comparing(Event::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(events);
        grid.setDataProvider(dataProvider);
    }

    private void applyFilters() {
        dataProvider.clearFilters();
        dataProvider.addFilter(event -> {
            if (categoryFilter.getValue() != null && event.getCategorie() != categoryFilter.getValue()) return false;
            if (statusFilter.getValue() != null && event.getStatus() != statusFilter.getValue()) return false;
            if (villeFilter.getValue() != null && !villeFilter.getValue().trim().isEmpty()) {
                if (event.getVille() == null || !event.getVille().toLowerCase().contains(villeFilter.getValue().toLowerCase().trim())) {
                    return false;
                }
            }
            if (keywordFilter.getValue() != null && !keywordFilter.getValue().trim().isEmpty()) {
                if (event.getTitre() == null || !event.getTitre().toLowerCase().contains(keywordFilter.getValue().toLowerCase().trim())) {
                    return false;
                }
            }
            if (organizerFilter.getValue() != null && !organizerFilter.getValue().trim().isEmpty()) {
                String term = organizerFilter.getValue().toLowerCase().trim();
                if (event.getOrganisateur() == null ||
                        (!(event.getOrganisateur().getEmail() != null && event.getOrganisateur().getEmail().toLowerCase().contains(term)) &&
                                !(event.getOrganisateur().getNom() != null && event.getOrganisateur().getNom().toLowerCase().contains(term)) &&
                                !(event.getOrganisateur().getPrenom() != null && event.getOrganisateur().getPrenom().toLowerCase().contains(term)))) {
                    return false;
                }
            }
            if (dateMinFilter.getValue() != null && event.getDateDebut() != null) {
                if (event.getDateDebut().toLocalDate().isBefore(dateMinFilter.getValue())) return false;
            }
            if (dateMaxFilter.getValue() != null && event.getDateDebut() != null) {
                if (event.getDateDebut().toLocalDate().isAfter(dateMaxFilter.getValue())) return false;
            }
            if (prixMinFilter.getValue() != null && event.getPrixUnitaire() != null) {
                if (event.getPrixUnitaire() < prixMinFilter.getValue()) return false;
            }
            if (prixMaxFilter.getValue() != null && event.getPrixUnitaire() != null) {
                if (event.getPrixUnitaire() > prixMaxFilter.getValue()) return false;
            }
            return true;
        });
    }

    private void resetFilters() {
        categoryFilter.clear();
        statusFilter.clear();
        organizerFilter.clear();
        villeFilter.clear();
        dateMinFilter.clear();
        dateMaxFilter.clear();
        prixMinFilter.clear();
        prixMaxFilter.clear();
        keywordFilter.clear();
        applyFilters();
    }

    private void publishEvent(Event event) {
        try {
            eventService.publishEvent(event.getId(), event.getOrganisateur().getId());
            Notification.show("Événement publié", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadEvents();
            applyFilters();
        } catch (Exception ex) {
            Notification.show("Erreur: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void cancelEvent(Event event) {
        try {
            eventService.cancelEvent(event.getId(), event.getOrganisateur().getId());
            Notification.show("Événement annulé", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadEvents();
            applyFilters();
        } catch (Exception ex) {
            Notification.show("Erreur: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteEvent(Event event) {
        try {
            eventService.deleteEvent(event.getId());
            Notification.show("Événement supprimé", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadEvents();
            applyFilters();
        } catch (Exception ex) {
            Notification.show("Erreur: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}


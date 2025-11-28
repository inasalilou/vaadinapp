package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Category;
import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.service.EventService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Route("events")
public class EventListView extends VerticalLayout {

    private final EventService eventService;

    private final Grid<Event> grid = new Grid<>(Event.class, false);

    private final ComboBox<Category> categoryField = new ComboBox<>("Catégorie");
    private final TextField villeField = new TextField("Ville");
    private final DatePicker dateMinField = new DatePicker("Date min");
    private final DatePicker dateMaxField = new DatePicker("Date max");
    private final NumberField prixMaxField = new NumberField("Prix max (€)");
    private final TextField keywordField = new TextField("Mot-clé");

    public EventListView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Événements disponibles");

        // Boutons login / register
        Button loginBtn = new Button("Se connecter",
                e -> UI.getCurrent().navigate("login"));
        Button registerBtn = new Button("S'inscrire",
                e -> UI.getCurrent().navigate("register"));
        HorizontalLayout authBar = new HorizontalLayout(loginBtn, registerBtn);

        configureFilters();
        configureGrid();

        // Ligne de filtres
        HorizontalLayout filters = new HorizontalLayout(
                categoryField, villeField, dateMinField, dateMaxField,
                prixMaxField, keywordField
        );
        filters.setWidthFull();
        filters.setAlignItems(Alignment.END);

        Button applyBtn = new Button("Filtrer", e -> applyFilters());
        Button resetBtn = new Button("Réinitialiser", e -> resetFilters());
        HorizontalLayout actions = new HorizontalLayout(applyBtn, resetBtn);

        add(title, authBar, filters, actions, grid);

        loadEvents();
    }

    /* -------------------- CONFIGURATION UI -------------------- */

    private void configureFilters() {
        categoryField.setItems(Category.values());
        categoryField.setPlaceholder("Toutes");

        villeField.setPlaceholder("Ex: Paris");

        prixMaxField.setMin(0.0);
        prixMaxField.setStep(5.0);

        keywordField.setPlaceholder("Titre contient…");
    }

    private void configureGrid() {
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true);

        grid.addColumn(Event::getCategorie)
                .setHeader("Catégorie")
                .setAutoWidth(true);

        grid.addColumn(Event::getVille)
                .setHeader("Ville")
                .setAutoWidth(true);

        grid.addColumn(Event::getDateDebut)
                .setHeader("Date de début")
                .setAutoWidth(true);

        grid.addColumn(Event::getPrix)
                .setHeader("Prix (€)")
                .setAutoWidth(true);

        // Colonne avec bouton "Réserver"
        grid.addComponentColumn(event -> {
            Button reserverBtn = new Button("Réserver");
            reserverBtn.addClickListener(click ->
                    UI.getCurrent().navigate("event/" + event.getId())
            );
            return reserverBtn;
        }).setHeader("Actions").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");
    }

    /* -------------------- CHARGEMENT / FILTRES -------------------- */

    private void loadEvents() {
        List<Event> events = eventService.findAll().stream()
                // 🔴 ICI on ne teste plus isPublic(), on vérifie simplement le statut
                .filter(e -> e.getStatus() == EventStatus.PUBLIE)
                .collect(Collectors.toList());

        grid.setItems(events);
    }

    private void applyFilters() {
        String ville = villeField.getValue();
        Category cat = categoryField.getValue();
        LocalDate dmin = dateMinField.getValue();
        LocalDate dmax = dateMaxField.getValue();
        Double prixMax = prixMaxField.getValue();
        String keyword = keywordField.getValue();

        List<Event> events = eventService.findAll().stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLIE)
                .filter(e -> ville == null || ville.isBlank()
                        || (e.getVille() != null
                        && e.getVille().toLowerCase().contains(ville.toLowerCase())))
                .filter(e -> cat == null || e.getCategorie() == cat)
                .filter(e -> {
                    if (dmin == null) return true;
                    LocalDateTime start = e.getDateDebut();
                    return start == null || !start.isBefore(dmin.atStartOfDay());
                })
                .filter(e -> {
                    if (dmax == null) return true;
                    LocalDateTime start = e.getDateDebut();
                    return start == null || !start.isAfter(dmax.atTime(23, 59));
                })
                .filter(e -> prixMax == null || e.getPrix() <= prixMax)
                .filter(e -> keyword == null || keyword.isBlank()
                        || (e.getTitre() != null
                        && e.getTitre().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());

        grid.setItems(events);
    }

    private void resetFilters() {
        categoryField.clear();
        villeField.clear();
        dateMinField.clear();
        dateMaxField.clear();
        prixMaxField.clear();
        keywordField.clear();
        loadEvents();
    }
}

package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
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
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Gestion des utilisateurs - Admin")
@Route("admin/users")
public class UserManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;

    private Grid<User> grid;
    private ListDataProvider<User> dataProvider;

    private TextField searchField;
    private ComboBox<Role> roleFilter;
    private ComboBox<String> statusFilter;

    private int pageSize = 15;
    private int currentPage = 0;
    private List<User> allUsers;

    public UserManagementView(UserService userService) {
        this.userService = userService;

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
            createFilters();
            createGrid();
            createPaginationControls();
            loadData();
        }
    }

    private void createHeader() {
        H1 title = new H1("Gestion des utilisateurs");
        title.getStyle().set("color", "#333").set("margin", "0");

        add(title);
    }

    private void createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);

        searchField = new TextField("Recherche");
        searchField.setPlaceholder("Nom, prénom ou email");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> applyFilters());

        roleFilter = new ComboBox<>("Rôle");
        roleFilter.setItems(Role.values());
        roleFilter.setItemLabelGenerator(role -> role.name());
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems("Actif", "Inactif");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button reset = new Button("Réinitialiser", new Icon(VaadinIcon.REFRESH));
        reset.addClickListener(e -> {
            searchField.clear();
            roleFilter.clear();
            statusFilter.clear();
            applyFilters();
        });

        filters.add(searchField, roleFilter, statusFilter, reset);
        add(filters);
    }

    private void createGrid() {
        grid = new Grid<>(User.class, false);
        grid.setPageSize(pageSize);

        grid.addColumn(u -> u.getNom() + " " + u.getPrenom())
                .setHeader("Nom")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(User::getEmail)
                .setHeader("Email")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(u -> u.getRole().name())
                .setHeader("Rôle")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(u -> u.getDateInscription() != null
                ? u.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "")
                .setHeader("Date inscription")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Statut")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createActions)
                .setHeader("Actions")
                .setAutoWidth(true);

        grid.setWidthFull();
        add(grid);
    }

    private void createPaginationControls() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.setSpacing(true);

        Button prev = new Button("Précédent", e -> changePage(currentPage - 1));
        Button next = new Button("Suivant", e -> changePage(currentPage + 1));

        prev.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        next.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        pagination.add(prev, next);
        add(pagination);
    }

    private void changePage(int page) {
        if (page < 0) return;
        int maxPage = (int) Math.ceil((double) allUsers.size() / pageSize) - 1;
        if (page > maxPage) return;
        currentPage = page;
        applyFilters();
    }

    private Span createStatusBadge(User user) {
        Span badge = new Span(user.getActif() ? "Actif" : "Inactif");
        badge.getStyle()
                .set("padding", "0.25rem 0.5rem")
                .set("border-radius", "12px")
                .set("font-size", "0.85rem")
                .set("font-weight", "bold")
                .set("color", "white")
                .set("background-color", user.getActif() ? "#28a745" : "#dc3545");
        return badge;
    }

    private HorizontalLayout createActions(User user) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button details = new Button(new Icon(VaadinIcon.EYE));
        details.getElement().setAttribute("title", "Voir détails");
        details.addClickListener(e -> showDetails(user));

        Button toggle = new Button(new Icon(user.getActif() ? VaadinIcon.BAN : VaadinIcon.CHECK));
        toggle.getElement().setAttribute("title", user.getActif() ? "Désactiver" : "Activer");
        toggle.addThemeVariants(user.getActif() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS);
        toggle.addClickListener(e -> toggleStatus(user));

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.setItems(Role.values());
        roleCombo.setValue(user.getRole());
        roleCombo.setWidth("140px");
        roleCombo.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() != user.getRole()) {
                changeRole(user, e.getValue());
            }
        });

        actions.add(details, toggle, roleCombo);
        return actions;
    }

    private void showDetails(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Détails utilisateur");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        content.add(new Span("Nom : " + user.getNom() + " " + user.getPrenom()));
        content.add(new Span("Email : " + user.getEmail()));
        content.add(new Span("Rôle : " + user.getRole()));
        content.add(new Span("Statut : " + (user.getActif() ? "Actif" : "Inactif")));
        content.add(new Span("Date inscription : " +
                (user.getDateInscription() != null
                        ? user.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "N/A")));
        content.add(new Span("Téléphone : " + (user.getTelephone() != null ? user.getTelephone() : "N/A")));

        dialog.add(content);

        Button close = new Button("Fermer", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(close);

        dialog.open();
    }

    private void toggleStatus(User user) {
        try {
            userService.toggleAccountStatus(user.getId(), !user.getActif());
            Notification.show("Statut mis à jour", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadData();
        } catch (Exception ex) {
            Notification.show("Erreur : " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void changeRole(User user, Role newRole) {
        try {
            userService.updateUserRole(user.getId(), newRole);
            Notification.show("Rôle mis à jour", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadData();
        } catch (Exception ex) {
            Notification.show("Erreur : " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadData() {
        allUsers = userService.findUsersWithFilters(null, null, null);
        currentPage = 0;
        applyFilters();
    }

    private void applyFilters() {
        List<User> filtered = allUsers;

        // Recherche
        if (searchField.getValue() != null && !searchField.getValue().trim().isEmpty()) {
            String term = searchField.getValue().toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(u -> (u.getNom() + " " + u.getPrenom()).toLowerCase().contains(term)
                            || u.getEmail().toLowerCase().contains(term))
                    .collect(Collectors.toList());
        }

        // Rôle
        if (roleFilter.getValue() != null) {
            filtered = filtered.stream()
                    .filter(u -> u.getRole() == roleFilter.getValue())
                    .collect(Collectors.toList());
        }

        // Statut
        if (statusFilter.getValue() != null) {
            boolean shouldBeActive = statusFilter.getValue().equals("Actif");
            filtered = filtered.stream()
                    .filter(u -> u.getActif() == shouldBeActive)
                    .collect(Collectors.toList());
        }

        // Pagination basique
        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        if (fromIndex >= filtered.size() && filtered.size() > 0) {
            currentPage = 0;
            fromIndex = 0;
            toIndex = Math.min(pageSize, filtered.size());
        }
        List<User> pageItems = filtered.subList(fromIndex, toIndex);

        dataProvider = new ListDataProvider<>(pageItems);
        grid.setDataProvider(dataProvider);
    }
}


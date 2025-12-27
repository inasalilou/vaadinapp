# 🛡️ Admin Structure - Complete Guide

## 📋 Table of Contents
1. [Admin Dashboard Overview](#admin-dashboard-overview)
2. [Admin Pages & Routes](#admin-pages--routes)
3. [Navigation Structure](#navigation-structure)
4. [Features by Page](#features-by-page)
5. [Access Control](#access-control)
6. [Quick Reference](#quick-reference)

---

## 🎯 Admin Dashboard Overview

**Route:** `/admin/dashboard`  
**File:** `AdminDashboardView.java`

The Admin Dashboard is the central hub for all administrative functions. It provides:
- **Real-time statistics** about the platform
- **Quick access** to all admin pages
- **Analytics and insights** about users, events, and reservations
- **Platform-wide metrics** and KPIs

### Dashboard Sections

#### 1. **Header Section**
- Title: "🛡️ Administration - EventManager"
- Subtitle: "Vue d'ensemble globale de la plateforme"
- Last update timestamp

#### 2. **Main Statistics Cards** (4 cards)
- 👥 **Total Users**: All registered users
- 📅 **Total Events**: All events created
- 🎫 **Total Reservations**: All reservations made
- 💰 **Total Revenue**: Total revenue generated

#### 3. **Detailed Analytics** (4 tables)
- 👥 **Users by Role**: Breakdown of ADMIN, ORGANIZER, CLIENT with percentages
- 📅 **Events by Status**: BROUILLON, PUBLIE, ANNULE, TERMINE with percentages
- 🎫 **Reservations by Status**: EN_ATTENTE, CONFIRMEE, ANNULEE with confirmation rate
- 💰 **Financial Metrics**: Total revenue, current month revenue, places reserved, average basket

#### 4. **Detailed Statistics Cards** (4 cards)
- **Users by Role Details**: Counts and organizer percentage
- **Events by Status Details**: Counts and success rate
- **Reservations by Status Details**: Counts, places reserved, revenue generated
- **Platform Metrics**: Active users, total events, total reservations, revenue, events per user, revenue per event

#### 5. **Admin Actions** (5 buttons)
Navigation buttons to access other admin pages:
- 👥 **Gérer les utilisateurs** → `/admin/users`
- 📅 **Gérer les événements** → `/admin/events`
- 🎫 **Gérer les réservations** → `/admin/reservations`
- ⚙️ **Paramètres système** → `/admin/settings` (not implemented yet)
- 📥 **Exporter les données** → Export functionality (placeholder)

---

## 📄 Admin Pages & Routes

### 1. **Admin Dashboard**
- **Route:** `/admin/dashboard`
- **File:** `AdminDashboardView.java`
- **Purpose:** Main admin hub with statistics and navigation
- **Features:**
  - Platform-wide statistics
  - Analytics and insights
  - Quick navigation to all admin pages

### 2. **User Management**
- **Route:** `/admin/users`
- **File:** `UserManagementView.java`
- **Purpose:** Manage all users in the system
- **Features:**
  - View all users in a grid
  - Search by name or email
  - Filter by role (ADMIN, ORGANIZER, CLIENT)
  - Filter by status (Actif, Inactif)
  - View user details
  - Toggle user active/inactive status
  - Change user role
  - Pagination (15 users per page)

### 3. **Events Management**
- **Route:** `/admin/events`
- **File:** `AllEventsManagementView.java`
- **Purpose:** Manage all events across all organizers
- **Features:**
  - View all events in a grid
  - Advanced filtering:
    - By category
    - By status (BROUILLON, PUBLIE, ANNULE, TERMINE)
    - By organizer (name or email)
    - By city
    - By date range (start date min/max)
    - By price range (min/max)
    - By keyword in title
  - Actions per event:
    - 👁️ View event details
    - ✏️ Edit event
    - ✅ Publish event (if draft)
    - ❌ Cancel event (if published)
    - 🗑️ Delete event (if no reservations)

### 4. **Reservations Management**
- **Route:** `/admin/reservations`
- **File:** `AllReservationsView.java`
- **Purpose:** View and manage all reservations platform-wide
- **Features:**
  - Statistics cards:
    - 🎫 Total reservations
    - ✅ Confirmed reservations
    - ⏳ Pending reservations
    - ❌ Cancelled reservations
    - 💰 Total revenue
    - 👥 Total places reserved
  - View all reservations in a grid
  - Advanced filtering:
    - By status
    - By reservation code
    - By user (name or email)
    - By event title
    - By date range
  - View reservation details
  - Export to CSV

### 5. **System Settings** (Not Implemented)
- **Route:** `/admin/settings`
- **Status:** ⚠️ Route exists but view not created
- **Note:** Button in dashboard navigates here, but page doesn't exist yet

---

## 🗺️ Navigation Structure

### From Admin Dashboard

```
Admin Dashboard (/admin/dashboard)
├── 👥 Gérer les utilisateurs → /admin/users
├── 📅 Gérer les événements → /admin/events
├── 🎫 Gérer les réservations → /admin/reservations
├── ⚙️ Paramètres système → /admin/settings (not implemented)
└── 📥 Exporter les données → (in-page action)
```

### Direct URL Access

All admin pages can be accessed directly via URL:
- `http://localhost:8080/admin/dashboard`
- `http://localhost:8080/admin/users`
- `http://localhost:8080/admin/events`
- `http://localhost:8080/admin/reservations`
- `http://localhost:8080/admin/settings` (will show error/redirect)

---

## ⚙️ Features by Page

### 👥 User Management (`/admin/users`)

#### Grid Columns
1. **Nom**: Full name (nom + prénom)
2. **Email**: User email address
3. **Rôle**: ADMIN, ORGANIZER, or CLIENT
4. **Date inscription**: Registration date
5. **Statut**: Active/Inactive badge (green/red)
6. **Actions**: Action buttons

#### Filters
- **Recherche**: Search by name or email (real-time)
- **Rôle**: Filter by ADMIN, ORGANIZER, CLIENT
- **Statut**: Filter by Actif or Inactif
- **Réinitialiser**: Clear all filters

#### Actions per User
- **👁️ View Details**: Opens dialog with:
  - Full name
  - Email
  - Role
  - Status
  - Registration date
  - Phone number
- **🔄 Toggle Status**: Activate/Deactivate user account
- **📝 Change Role**: Dropdown to change user role (ADMIN, ORGANIZER, CLIENT)

#### Pagination
- 15 users per page
- Previous/Next buttons
- Automatic page adjustment

---

### 📅 Events Management (`/admin/events`)

#### Grid Columns
1. **Titre**: Event title
2. **Catégorie**: Event category
3. **Organisateur**: Organizer email
4. **Date début**: Start date and time
5. **Statut**: Event status
6. **Prix**: Unit price in dh
7. **Ville**: City
8. **Actions**: Action buttons

#### Filters (Advanced)
- **Catégorie**: All event categories
- **Statut**: BROUILLON, PUBLIE, ANNULE, TERMINE
- **Organisateur**: Search by organizer name or email
- **Mot-clé titre**: Search keyword in title
- **Ville**: Filter by city
- **Date début min/max**: Date range filter
- **Prix min/max**: Price range filter (dh)
- **Appliquer**: Apply filters
- **Réinitialiser**: Clear all filters

#### Actions per Event
- **👁️ View**: Navigate to event detail page (`/event/{id}`)
- **✏️ Edit**: Navigate to event edit page (`/organizer/event/{id}`)
- **✅ Publish**: Publish draft event (only visible if status is BROUILLON)
- **❌ Cancel**: Cancel published event (only visible if status is PUBLIE)
- **🗑️ Delete**: Delete event (only visible if no reservations exist)

---

### 🎫 Reservations Management (`/admin/reservations`)

#### Statistics Cards (Top)
- **🎫 Total**: Total number of reservations
- **✅ Confirmées**: Confirmed reservations count
- **⏳ En attente**: Pending reservations count
- **❌ Annulées**: Cancelled reservations count
- **💰 Revenus**: Total revenue from confirmed reservations
- **👥 Places réservées**: Total places reserved (excluding cancelled)

#### Grid Columns
1. **Code**: Reservation code
2. **Utilisateur**: Client full name
3. **Email**: Client email
4. **Événement**: Event title
5. **Statut**: Reservation status
6. **Places**: Number of places
7. **Montant**: Total amount in dh
8. **Date**: Reservation date and time
9. **Actions**: Action buttons

#### Filters
- **Statut**: EN_ATTENTE, CONFIRMEE, ANNULEE
- **Code réservation**: Search by reservation code
- **Utilisateur**: Search by user name or email
- **Événement**: Search by event title
- **Date min/max**: Date range filter
- **Appliquer**: Apply filters
- **Réinitialiser**: Clear all filters

#### Actions
- **👁️ View Details**: Opens dialog with:
  - Reservation code
  - User information
  - Event information
  - Status
  - Number of places
  - Amount
  - Date
  - Comments (if any)
- **📥 Export CSV**: Export filtered reservations to CSV file

---

## 🔒 Access Control

### Authentication & Authorization

All admin pages implement `BeforeEnterObserver` to check:
1. **User is logged in**: Redirects to `/login` if not authenticated
2. **User is ADMIN**: Redirects to `/dashboard` if not admin

### Security Implementation

```java
@Override
public void beforeEnter(BeforeEnterEvent event) {
    User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
    if (currentUser == null) {
        event.rerouteTo("login");
        return;
    }
    if (currentUser.getRole() != Role.ADMIN) {
        event.rerouteTo("dashboard");
        return;
    }
    // Build page content
}
```

### Protected Routes
- ✅ `/admin/dashboard` - Admin only
- ✅ `/admin/users` - Admin only
- ✅ `/admin/events` - Admin only
- ✅ `/admin/reservations` - Admin only
- ⚠️ `/admin/settings` - Route exists but page not implemented

---

## 📊 Quick Reference

### Admin Routes Summary

| Route | Page | Status | Features |
|-------|------|--------|----------|
| `/admin/dashboard` | Admin Dashboard | ✅ Complete | Statistics, Analytics, Navigation |
| `/admin/users` | User Management | ✅ Complete | CRUD, Filters, Role Management |
| `/admin/events` | Events Management | ✅ Complete | View All, Advanced Filters, Actions |
| `/admin/reservations` | Reservations Management | ✅ Complete | View All, Statistics, CSV Export |
| `/admin/settings` | System Settings | ❌ Not Implemented | - |

### Key Features Summary

| Feature | Dashboard | Users | Events | Reservations |
|---------|-----------|-------|--------|--------------|
| View All | ✅ | ✅ | ✅ | ✅ |
| Search/Filter | ❌ | ✅ | ✅ | ✅ |
| Statistics | ✅ | ❌ | ❌ | ✅ |
| Edit/Modify | ❌ | ✅ | ✅ | ❌ |
| Export | ✅ (placeholder) | ❌ | ❌ | ✅ (CSV) |
| Pagination | ❌ | ✅ | ❌ | ❌ |

### Navigation Flow

```
Login as Admin
    ↓
Admin Dashboard (/admin/dashboard)
    ├─→ User Management (/admin/users)
    ├─→ Events Management (/admin/events)
    ├─→ Reservations Management (/admin/reservations)
    └─→ System Settings (/admin/settings) [Not Implemented]
```

---

## 🎨 UI/UX Features

### Design Elements
- **Color Scheme**: Light gray background (#f8f9fa), white cards
- **Icons**: Vaadin Icons throughout
- **Cards**: Rounded corners, shadows for depth
- **Badges**: Color-coded status indicators
- **Notifications**: Success/Error notifications for actions

### Responsive Design
- Full-width layouts
- Auto-width columns in grids
- Flexible horizontal/vertical layouts
- Mobile-friendly components

---

## 🔧 Technical Details

### Technologies Used
- **Vaadin Flow**: UI framework
- **Spring Boot**: Backend framework
- **JPA/Hibernate**: Database access
- **BCrypt**: Password hashing
- **H2 Database**: In-memory database

### Key Classes
- `AdminDashboardView`: Main dashboard
- `UserManagementView`: User management
- `AllEventsManagementView`: Events management
- `AllReservationsView`: Reservations management
- `UserService`: User business logic
- `EventService`: Event business logic
- `ReservationService`: Reservation business logic

---

## 🚀 Getting Started

### Access Admin Pages

1. **Login as Admin**:
   - Go to: `http://localhost:8080/login`
   - Email: `admin@test.com`
   - Password: `admin123`

2. **Navigate to Dashboard**:
   - After login, go to: `http://localhost:8080/admin/dashboard`
   - Or use the navigation buttons

3. **Explore Admin Features**:
   - Click any action button in the dashboard
   - Or navigate directly via URL

### Testing Admin Features

1. **User Management**:
   - Search for users
   - Change user roles
   - Activate/deactivate accounts

2. **Events Management**:
   - Filter events by various criteria
   - Publish draft events
   - Cancel published events

3. **Reservations Management**:
   - View all reservations
   - Filter by status, user, event
   - Export data to CSV

---

## 📝 Notes

- **System Settings**: The route exists but the page is not implemented yet. Clicking the button will navigate but show an error.
- **Export Data**: Dashboard export is a placeholder (shows notification).
- **Pagination**: Only User Management has pagination (15 per page).
- **Real-time Updates**: Statistics update when data changes, but may require page refresh.

---

## 🐛 Known Limitations

1. **Settings Page**: Not implemented
2. **Dashboard Export**: Placeholder only
3. **No Breadcrumbs**: No navigation breadcrumbs between pages
4. **No Back Button**: No easy way to return to dashboard from sub-pages
5. **Statistics Refresh**: May require manual page refresh to see latest data

---

## 💡 Future Enhancements

Potential improvements:
- Implement System Settings page
- Add breadcrumb navigation
- Add "Back to Dashboard" button on all admin pages
- Real-time statistics updates
- Advanced export options (PDF, Excel)
- User activity logs
- System health monitoring
- Email notifications for admin actions


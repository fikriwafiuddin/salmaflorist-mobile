# Implementation Plan - Admin Dashboard UI

This plan describes the implementation of the Admin Dashboard, including statistics, a 7-day order chart, and a table of recent orders.

## User Review Required

> [!NOTE]
> Since there is no chart library (like MPAndroidChart) currently in the project and I shouldn't add new dependencies without permission, I will implement a custom `SimpleLineChartView` to visualize the 7-day order data.

## Proposed Changes

### Data Layer

#### [DBOpenHelper.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/data/DBOpenHelper.kt)
- Add `getDashboardStats()` to get today's order count and total income.
- Add `getOrdersLast7Days()` to get order counts for the last 7 days.
- Add `getRecentOrders(limit: Int)` to get the most recent orders.

### UI Layer - Custom Views

#### [NEW] [SimpleLineChartView.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/view/SimpleLineChartView.kt)
- A custom view to draw a simple line chart for the 7-day order history.

### UI Layer - Admin Dashboard

#### [fragment_admin_dashboard.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/layout/fragment_admin_dashboard.xml)
- Update layout with ScrollView containing:
    - Statistics Cards (Today's Orders, Today's Income).
    - Chart Section (Last 7 Days Orders).
    - Recent Orders Section (Table/List).

#### [AdminDashboardFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminDashboardFragment.kt)
- Update fragment logic to fetch data from `DBOpenHelper` and populate the UI.

#### [NEW] [item_recent_order_row.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/layout/item_recent_order_row.xml)
- Layout for a single row in the recent orders table.

#### [NEW] [RecentOrderAdapter.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/adapter/RecentOrderAdapter.kt)
- Adapter for the recent orders list in the dashboard.

## Verification Plan

### Manual Verification
- Log in as admin and verify the Dashboard loads.
- Verify "Total Pesanan Hari Ini" and "Total Pemasukan Hari Ini" show correct data (seed data might need updating to include "today").
- Verify the Line Chart displays correctly.
- Verify the Recent Orders table shows exactly 5 (or fewer if not available) orders with correct columns.

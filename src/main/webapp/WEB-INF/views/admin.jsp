<%@ include file="_h.jspf" %>
<div id="main" role="main">
<div class="admin-shell">
  <aside class="admin-sidebar" aria-label="Admin navigation">
    <a class="admin-side-brand" href="${ctx}/app/admin"><span>&#10022;</span> CraveDrop <small>ADMIN</small></a>
    <a class="admin-side-link active" href="#overview">&#9638; Dashboard</a>
    <a class="admin-side-link" href="#restaurants">&#127968; Restaurants</a>
    <a class="admin-side-link" href="#menus">&#127860; Menus</a>
    <a class="admin-side-link" href="#orders">&#128230; Orders</a>
    <a class="admin-side-link" href="#customers">&#128101; Customers</a>
    <a class="admin-side-link" href="#partners">&#128666; Delivery partners</a>
    <a class="admin-side-link" href="${ctx}/app/logout">&#8618; Sign out</a>
    <div class="admin-side-note">Live operations<br><strong>Bengaluru</strong></div>
  </aside>
  <section class="admin-content" id="overview">
    <div class="admin-topline"><div><p class="eyebrow">CRAVEDROP OPERATIONS</p><h1>Welcome back, <c:out value="${user.name}"/></h1><p>Here's what's happening across your food delivery app.</p></div><span class="admin-access">&#128274; Administrator access</span></div>
    <c:if test="${param.updated eq '1'}"><p class="msg ok" role="status">Order status updated. The customer can now see the new delivery step.</p></c:if>
    <c:if test="${param.assigned eq '1'}"><p class="msg ok" role="status">Delivery partner assigned to the order.</p></c:if>
    <c:if test="${param.partnerAdded eq '1'}"><p class="msg ok" role="status">Delivery partner added.</p></c:if>
    <c:if test="${param.partnerError eq '1'}"><p class="msg err" role="alert">Enter a name, 10-digit phone number, and delivery area. Phone numbers must be unique.</p></c:if>
    <c:if test="${param.partnerDuplicate eq '1'}"><p class="msg err" role="alert">That phone number is already registered to a delivery partner.</p></c:if>
    <c:if test="${param.assignError eq '1'}"><p class="msg err" role="alert">That assignment could not be saved. Choose an available partner for an active order.</p></c:if>
    <div class="metric-grid admin-metrics">
      <article class="metric-card"><span class="metric-icon">&#128101;</span><span class="metric-label">Customers</span><strong>${stats.customers}</strong><small>Registered customer accounts</small></article>
      <article class="metric-card"><span class="metric-icon purple">&#127968;</span><span class="metric-label">Restaurants</span><strong>${fn:length(adminRestaurants)}</strong><small>In the Bengaluru directory</small></article>
      <article class="metric-card"><span class="metric-icon green">&#127860;</span><span class="metric-label">Menu items</span><strong>${fn:length(adminMenuItems)}</strong><small>Restaurant-specific dishes</small></article>
      <article class="metric-card"><span class="metric-icon">&#128230;</span><span class="metric-label">All orders</span><strong>${stats.orders}</strong><small>${stats.openOrders} still in progress</small></article>
      <article class="metric-card"><span class="metric-icon green">&#8377;</span><span class="metric-label">Order value</span><strong>&#8377;<fmt:formatNumber value="${stats.revenue}" maxFractionDigits="0"/></strong><small>Saved order totals</small></article>
      <article class="metric-card"><span class="metric-icon purple">&#127970;</span><span class="metric-label">Restaurant owners</span><strong>${stats.owners}</strong><small>Registered owner accounts</small></article>
    </div>

    <section class="admin-section" id="orders">
      <div class="section-heading dashboard-heading"><div><h2>Order management</h2><p>Update a delivery step; customers see it in order history. Add a courier in Delivery partners to enable assignment.</p></div><a href="${ctx}/app/orders">Customer orders <span aria-hidden="true">&#8594;</span></a></div>
      <c:if test="${empty recentAdminOrders}"><article class="empty dashboard-empty"><div class="empty-bowl">&#128230;</div><h2>No orders recorded yet</h2><p>New orders appear here as customers check out.</p></article></c:if>
      <c:if test="${not empty recentAdminOrders}"><div class="admin-table-wrap"><table class="admin-table"><thead><tr><th>Order</th><th>Customer</th><th>Placed</th><th>Total</th><th>Status</th><th>Delivery partner</th><th>Update</th></tr></thead><tbody>
        <c:forEach var="o" items="${recentAdminOrders}"><tr><td><strong>#${o.id}</strong></td><td><strong><c:out value="${o.customer}"/></strong><small><c:out value="${o.email}"/></small></td><td><fmt:formatDate value="${o.at}" pattern="d MMM, h:mm a"/></td><td><strong>&#8377;<fmt:formatNumber value="${o.total}" minFractionDigits="2"/></strong></td><td><span class="badge"><c:out value="${o.status}"/></span></td><td><c:choose><c:when test="${not empty o.deliveryPartner}"><c:out value="${o.deliveryPartner}"/></c:when><c:otherwise><span class="admin-unassigned">Unassigned</span></c:otherwise></c:choose><c:choose><c:when test="${empty availableDeliveryPartners and o.deliveryPartnerId eq 0}"><small class="admin-no-partner"><c:choose><c:when test="${empty deliveryPartners}">No couriers added.</c:when><c:otherwise>All couriers are assigned.</c:otherwise></c:choose> <a href="#partners">Manage couriers</a></small></c:when><c:otherwise><form class="admin-status-form admin-assign-form" action="${ctx}/app/admin/partner-assign" method="post"><input type="hidden" name="orderId" value="${o.id}"/><label class="sr" for="partner-${o.id}">Delivery partner for order ${o.id}</label><select id="partner-${o.id}" name="partnerId" required><option value="">Choose partner</option><c:forEach var="d" items="${deliveryPartners}"><c:if test="${d.status eq 'Available' or d.id eq o.deliveryPartnerId}"><option value="${d.id}" ${d.id eq o.deliveryPartnerId?'selected':''}><c:out value="${d.name}"/></option></c:if></c:forEach></select><button class="btn sm" type="submit">Assign</button></form></c:otherwise></c:choose></td><td><form class="admin-status-form" action="${ctx}/app/admin/order-status" method="post"><input type="hidden" name="orderId" value="${o.id}"/><label class="sr" for="status-${o.id}">Status for order ${o.id}</label><select id="status-${o.id}" name="status"><option value="Placed" ${o.status eq 'Placed'?'selected':''}>Placed</option><option value="Preparing" ${o.status eq 'Preparing'?'selected':''}>Preparing</option><option value="Out for delivery" ${o.status eq 'Out for delivery'?'selected':''}>Out for delivery</option><option value="Delivered" ${o.status eq 'Delivered'?'selected':''}>Delivered</option></select><button class="btn sm" type="submit">Save</button></form></td></tr></c:forEach>
      </tbody></table></div></c:if>
    </section>

    <section class="admin-section" id="restaurants">
      <div class="section-heading dashboard-heading"><div><h2>Restaurant directory</h2><p>All <strong>${fn:length(adminRestaurants)}</strong> unique restaurants currently listed.</p></div><a href="${ctx}/app/restaurants">Open storefront <span aria-hidden="true">&#8594;</span></a></div>
      <div class="admin-table-wrap"><table class="admin-table"><thead><tr><th>Restaurant</th><th>Cuisine</th><th>Area</th><th>Rating</th><th>Delivery</th></tr></thead><tbody><c:forEach var="r" items="${adminRestaurants}"><tr><td><strong><c:out value="${r.name}"/></strong></td><td><c:out value="${r.cuisine}"/></td><td><c:out value="${r.area}"/></td><td>&#9733; <fmt:formatNumber value="${r.rating}" pattern="0.0"/></td><td>${r.minutes} min</td></tr></c:forEach></tbody></table></div>
    </section>

    <section class="admin-section" id="menus">
      <div class="section-heading dashboard-heading"><div><h2>Menu catalog</h2><p>Unique restaurant-specific dishes from the active catalog.</p></div></div>
      <div class="admin-table-wrap"><table class="admin-table"><thead><tr><th>Dish</th><th>Restaurant</th><th>Category</th><th>Price</th><th>Type</th></tr></thead><tbody><c:forEach var="m" items="${adminMenuItems}" varStatus="st"><c:if test="${st.index lt 18}"><tr><td><strong><c:out value="${m.name}"/></strong></td><td><c:out value="${m.restaurant}"/></td><td><c:out value="${m.category}"/></td><td>&#8377;<fmt:formatNumber value="${m.price}" minFractionDigits="0"/></td><td><c:out value="${m.vegetarian?'Veg':'Non-veg'}"/></td></tr></c:if></c:forEach></tbody></table></div>
      <p class="admin-footnote">Showing up to 18 of ${fn:length(adminMenuItems)} active dishes. Menu photos and descriptions remain linked to each restaurant.</p>
    </section>

    <section class="admin-section" id="customers">
      <div class="section-heading dashboard-heading"><div><h2>Recent accounts</h2><p>Latest customers and restaurant owners.</p></div></div>
      <div class="admin-table-wrap"><table class="admin-table"><thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Account type</th><th>Joined</th></tr></thead><tbody><c:forEach var="a" items="${recentAdminUsers}"><tr><td><strong><c:out value="${a.name}"/></strong></td><td><c:out value="${a.email}"/></td><td><c:out value="${a.phone}"/></td><td><c:out value="${a.role}"/></td><td><fmt:formatDate value="${a.at}" pattern="d MMM yyyy"/></td></tr></c:forEach></tbody></table></div>
    </section>
    <section class="admin-section" id="partners">
      <div class="section-heading dashboard-heading"><div><h2>Delivery partners</h2><p>Add couriers and assign available partners to open orders.</p></div></div>
      <form class="admin-partner-form" action="${ctx}/app/admin/partner-add" method="post"><div><label for="partner-name">Partner name</label><input id="partner-name" name="name" maxlength="80" required placeholder="Full name"/></div><div><label for="partner-phone">Phone</label><input id="partner-phone" name="phone" inputmode="numeric" pattern="[0-9]{10}" maxlength="10" required placeholder="10-digit phone"/></div><div><label for="partner-area">Delivery area</label><input id="partner-area" name="area" maxlength="100" required placeholder="Neighbourhood"/></div><button class="btn" type="submit">Add delivery partner</button></form>
      <c:if test="${empty deliveryPartners}"><p class="empty">No delivery partners added yet. Add a real partner above to enable order assignment.</p></c:if>
      <c:if test="${not empty deliveryPartners}"><div class="admin-table-wrap"><table class="admin-table"><thead><tr><th>Partner</th><th>Phone</th><th>Area</th><th>Availability</th></tr></thead><tbody><c:forEach var="d" items="${deliveryPartners}"><tr><td><strong><c:out value="${d.name}"/></strong></td><td><c:out value="${d.phone}"/></td><td><c:out value="${d.area}"/></td><td><span class="badge"><c:out value="${d.status}"/></span></td></tr></c:forEach></tbody></table></div></c:if>
    </section>
    <p class="admin-footnote">This dashboard displays live MySQL order and account data. Admin access is granted through the database setup script; public sign-up cannot create an administrator.</p>
  </section>
</div>
</div>
<%@ include file="_f.jspf" %>


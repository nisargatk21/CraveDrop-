<%@ include file="_h.jspf" %>
<div id="main" role="main">
<section class="wrap dashboard-page">
 <div class="dashboard-welcome"><div><p class="eyebrow">Your CraveDrop account</p><h1>Good to see you, <span><c:out value="${user.name}"/></span></h1><p>Your orders and delivery updates, all in one place.</p></div><a class="btn" href="${ctx}/app/restaurants">Find your next favourite</a></div>
 <div class="metric-grid customer-metrics"><article class="metric-card"><span class="metric-icon">&#128722;</span><span class="metric-label">Orders placed</span><strong>${orderCount}</strong><small>Since you joined CraveDrop</small></article><article class="metric-card"><span class="metric-icon green">&#10003;</span><span class="metric-label">Delivered</span><strong>${deliveredCount}</strong><small>Meals brought to your door</small></article><article class="metric-card"><span class="metric-icon purple">&#8377;</span><span class="metric-label">Total spent</span><strong>&#8377;<fmt:formatNumber value="${spent}" maxFractionDigits="0"/></strong><small>Across your completed orders</small></article></div>
 <div class="section-heading dashboard-heading"><div><h2>Recent orders</h2><p>Follow your latest CraveDrop moments.</p></div><a href="${ctx}/app/orders">View order history <span aria-hidden="true">&#8594;</span></a></div>
 <c:if test="${empty recentOrders}"><article class="empty dashboard-empty"><div class="empty-bowl">&#127836;</div><h2>Your table is waiting</h2><p>Explore local kitchens and place your first order to see it here.</p><a class="btn" href="${ctx}/app/restaurants">Explore restaurants</a></article></c:if>
 <c:forEach var="o" items="${recentOrders}"><article class="panel customer-order-card"><div class="row"><div><small><fmt:formatDate value="${o.at}" pattern="d MMM yyyy, h:mm a"/></small><h3>Order #${o.number}</h3></div><span class="badge"><c:out value="${o.status}"/></span></div><p><c:forEach var="line" items="${o.lines}" varStatus="st"><c:if test="${st.index>0}">, </c:if><c:out value="${line.name}"/> &times; ${line.qty}</c:forEach></p><div class="customer-order-bottom"><strong>&#8377;<fmt:formatNumber value="${o.total}" minFractionDigits="2"/></strong><span><c:out value="${o.payment}"/></span><a href="${ctx}/app/orders">Order details <span aria-hidden="true">&#8594;</span></a></div></article></c:forEach>
</section>
</div>
<%@ include file="_f.jspf" %>


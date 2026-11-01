<%@ include file="_h.jspf" %>
<div id="main" role="main">
<div class="wrap"><h1>Your orders</h1>
<c:if test="${not empty param.placed}"><p class="msg ok" role="status">🎉 Order #<c:out value="${param.placed}"/> placed! The restaurant is getting started.</p></c:if>
<c:if test="${empty orders}"><p class="empty">No orders yet. <a href="${ctx}/app/home">Order something tasty</a></p></c:if>
<c:forEach var="o" items="${orders}"><article class="panel receipt"><div class="row"><h2>Order #${o.number}</h2><span class="badge"><c:out value="${o.status}"/></span></div>
<c:set var="cur" value="${o.status=='Placed'?0:o.status=='Preparing'?1:o.status=='Delivered'?3:2}"/>
<ol class="track" aria-label="Order status"><c:forEach var="s" items="${fn:split('Placed,Preparing,Out for delivery,Delivered',',')}" varStatus="st"><li class="${st.index<=cur?'done':''}" ${st.index==cur?'aria-current="step"':''}>${s}</li></c:forEach></ol>
<small><fmt:formatDate value="${o.at}" pattern="d MMM yyyy, h:mm a"/> &middot; <c:out value="${o.payment}"/> &middot; <c:out value="${o.address}"/></small>
<ul class="items"><c:forEach var="l" items="${o.lines}"><li><span>${l.qty} &times; <c:out value="${l.name}"/></span><span>&#8377;<fmt:formatNumber value="${l.qty*l.price}" pattern="#,##0.00"/></span></li></c:forEach></ul>
<p class="total">Total <strong>&#8377;<fmt:formatNumber value="${o.total}" pattern="#,##0.00"/></strong></p></article></c:forEach></div>
</div>
<%@ include file="_f.jspf" %>



<%@ include file="_h.jspf" %>
<div id="main" role="main">
<div class="wrap confirmation-wrap"><section class="panel confirmation" aria-live="polite"><span class="success-check" role="img" aria-label="Order successful">&#10003;</span><span class="eyebrow">ORDER #<c:out value="${order.number}"/></span><h1>Order placed successfully!</h1><p>Your order is in the kitchen queue. You can follow its status and items from your order history.</p><ul class="confirmation-items"><c:forEach var="line" items="${order.lines}"><li><span>${line.qty} &times; <c:out value="${line.name}"/></span><strong>&#8377;<fmt:formatNumber value="${line.qty*line.price}" pattern="#,##0.00"/></strong></li></c:forEach></ul><div class="success-total">Order total <strong>&#8377;<fmt:formatNumber value="${order.total}" pattern="#,##0.00"/></strong></div><div class="confirmation-actions"><a class="btn" href="${ctx}/app/orders">Track this order</a><a class="btn ghost" href="${ctx}/app/home">Explore restaurants</a></div></section></div>
</div>
<%@ include file="_f.jspf" %>



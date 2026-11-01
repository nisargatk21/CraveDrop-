<%@ include file="_h.jspf" %>
<div id="main" role="main">
<div class="wrap cart-page"><a class="back-link" href="${ctx}/app/home">&#8592; Continue browsing</a><h1>My Cart</h1>
<c:if test="${not empty cartNotice}"><p class="msg err" role="alert"><c:out value="${cartNotice}"/></p></c:if>
<c:if test="${not empty error}"><p class="msg err" role="alert"><c:out value="${error}"/></p></c:if>
<c:choose><c:when test="${empty lines}"><p class="empty">Your cart is empty. <a href="${ctx}/app/home">Browse the menu</a></p></c:when><c:otherwise>
<section class="panel receipt" aria-label="Cart items"><c:forEach var="l" items="${lines}"><div class="line">
<img src="${ctx}/${l.item.image}" alt=""><div><h3><c:out value="${l.item.name}"/></h3><small>&#8377;<fmt:formatNumber value="${l.item.price}" pattern="#,##0.00"/> each</small></div>
<form method="post" action="${ctx}/app/cart/set" class="qty"><input type="hidden" name="id" value="${l.item.id}"><label class="sr" for="q${l.item.id}">Quantity for ${fn:escapeXml(l.item.name)}</label>
<input id="q${l.item.id}" type="number" name="qty" min="0" max="20" value="${l.qty}"><button class="btn sm ghost" type="submit">Update</button></form>
<strong>&#8377;<fmt:formatNumber value="${l.total}" pattern="#,##0.00"/></strong>
<form method="post" action="${ctx}/app/cart/set"><input type="hidden" name="id" value="${l.item.id}"><input type="hidden" name="qty" value="0"><button class="link" type="submit" aria-label="Remove ${fn:escapeXml(l.item.name)}">Remove</button></form></div></c:forEach>
<p class="total">Subtotal <strong>&#8377;<fmt:formatNumber value="${total}" pattern="#,##0.00"/></strong></p><div class="cart-actions"><a class="btn ghost" href="${ctx}/app/home">Add more items</a><a class="btn" href="${ctx}/app/checkout">Proceed to checkout &#8594;</a></div></section>
</c:otherwise></c:choose></div>
</div>
<%@ include file="_f.jspf" %>



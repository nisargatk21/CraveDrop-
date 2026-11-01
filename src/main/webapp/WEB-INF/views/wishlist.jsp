<%@ include file="_h.jspf" %>
<div id="main" role="main">
<section class="wrap saved-page">
  <div class="section-heading"><div><p class="eyebrow">Your personal shortlist</p><h1>Saved dishes</h1><p>Keep the dishes you love close by.</p></div><a class="view-all" href="${ctx}/app/restaurants">Explore restaurants &#8594;</a></div>
  <c:choose>
    <c:when test="${empty savedItems}">
      <div class="empty saved-empty"><span aria-hidden="true">&#9829;</span><h2>No saved dishes yet</h2><p>Tap &quot;Save dish&quot; on any menu item and it will appear here.</p><a class="btn" href="${ctx}/app/restaurants">Find something delicious</a></div>
    </c:when>
    <c:otherwise>
      <div class="saved-grid">
        <c:forEach var="item" items="${savedItems}">
          <c:url var="savedMenuUrl" value="/app/menu"><c:param name="restaurant" value="${item.restaurant}"/></c:url>
          <article class="saved-card">
            <a class="saved-photo" href="${savedMenuUrl}"><img src="${ctx}/${item.image}" alt="${fn:escapeXml(item.name)}"></a>
            <div class="saved-copy">
              <span class="dish-category"><c:out value="${item.restaurant}"/> &middot; <c:out value="${item.category}"/></span>
              <h2><c:out value="${item.name}"/></h2>
              <p><c:out value="${item.description}"/></p>
              <strong class="menu-price">&#8377;<fmt:formatNumber value="${item.price}" pattern="#,##0"/></strong>
              <div class="saved-actions"><a class="btn sm" href="${savedMenuUrl}">View menu</a><form method="post" action="${ctx}/app/wishlist/toggle" data-favorite-form data-saved-item><input type="hidden" name="id" value="${item.id}"><button type="submit" class="favorite-button is-saved" aria-pressed="true"><span aria-hidden="true">&#9829;</span> Remove</button></form></div>
            </div>
          </article>
        </c:forEach>
      </div>
      <p class="menu-feedback" data-menu-feedback role="status" aria-live="polite"></p>
    </c:otherwise>
  </c:choose>
</section>
</div>
<%@ include file="_f.jspf" %>


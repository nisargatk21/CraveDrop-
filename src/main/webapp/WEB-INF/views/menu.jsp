<%@ include file="_h.jspf" %>
<div id="main" role="main">
<div class="wrap menu-page">
  <a class="back-link" href="${ctx}/app/home">&#8592; All restaurants</a>
  <div class="menu-heading"><div><span class="eyebrow">${restaurant.minutes}&ndash;${restaurant.minutes + 8} min &middot; ${restaurant.area}</span><h1><c:out value="${restaurant.name}"/></h1><p><c:out value="${restaurant.cuisine}"/> <span class="rating">&#9733; <fmt:formatNumber value="${restaurant.rating}" pattern="0.0"/></span></p></div>
    <form class="menu-search" action="${ctx}/app/menu" method="get"><input type="hidden" name="restaurant" value="${fn:escapeXml(restaurant.name)}"><label class="sr" for="food-search">Search this menu</label><input id="food-search" name="q" placeholder="Search this menu" value="${fn:escapeXml(param.q)}"><button type="submit">Search</button></form>
  </div>
  <nav class="menu-filters" aria-label="Filter dishes">
    <c:url var="menuAll" value="/app/menu"><c:param name="restaurant" value="${restaurant.name}"/><c:if test="${not empty param.q}"><c:param name="q" value="${param.q}"/></c:if></c:url>
    <c:url var="menuVeg" value="/app/menu"><c:param name="restaurant" value="${restaurant.name}"/><c:param name="veg" value="true"/><c:if test="${not empty param.q}"><c:param name="q" value="${param.q}"/></c:if></c:url>
    <c:url var="menuNonVeg" value="/app/menu"><c:param name="restaurant" value="${restaurant.name}"/><c:param name="veg" value="false"/><c:if test="${not empty param.q}"><c:param name="q" value="${param.q}"/></c:if></c:url>
    <a class="${empty param.veg?'selected':''}" href="${menuAll}">All dishes</a><a class="${param.veg=='true'?'selected':''}" href="${menuVeg}"><span class="veg-mark"></span> Veg</a><a class="${param.veg=='false'?'selected':''}" href="${menuNonVeg}"><span class="veg-mark nonveg"></span> Non-veg</a>
  </nav>
  <div class="menu-feedback" data-menu-feedback role="status" aria-live="polite"></div>
  <h2 class="menu-section-title">Explore the menu <span class="menu-count"><c:out value="${fn:length(items)}"/> dishes</span></h2>
  <div class="menu-list"><c:set var="activeCategory" value=""/><c:forEach var="i" items="${items}">
    <c:set var="itemId" value="${i.id}"/><c:set var="itemQty" value="${cartQuantities[itemId]}"/><c:set var="itemSaved" value="${wishlistFlags[itemId]}"/>
    <c:if test="${activeCategory ne i.category}"><h3 class="menu-category-title"><c:out value="${i.category}"/></h3><c:set var="activeCategory" value="${i.category}"/></c:if>
    <article class="menu-row"><div class="menu-row-copy"><span class="veg-mark ${i.vegetarian?'':'nonveg'}" aria-label="${i.vegetarian?'Vegetarian':'Non-vegetarian'}"></span><h3><c:out value="${i.name}"/></h3><strong class="menu-price">&#8377;<fmt:formatNumber value="${i.price}" pattern="#,##0"/></strong><p><c:out value="${i.description}"/></p>
      <form class="favorite-form" method="post" action="${ctx}/app/wishlist/toggle" data-favorite-form><input type="hidden" name="id" value="${itemId}"><button class="favorite-button ${itemSaved?'is-saved':''}" type="submit" aria-pressed="${itemSaved}"><span aria-hidden="true">&#9829;</span> <span data-favorite-label>${itemSaved?'Saved':'Save dish'}</span></button></form>
    </div><div class="menu-row-photo"><img src="${ctx}/${i.image}" alt="${fn:escapeXml(i.name)}"><div class="menu-cart-action" data-menu-action data-item-id="${itemId}"><form method="post" action="${ctx}/app/cart/add" data-cart-form><input type="hidden" name="id" value="${itemId}"><button type="submit" class="menu-add ${itemQty gt 0?'is-hidden':''}">ADD <span aria-hidden="true">&#43;</span></button></form>
      <div class="menu-stepper ${itemQty gt 0?'':'is-hidden'}" data-menu-stepper><button type="button" data-qty-step="-1" aria-label="Remove one">&#8722;</button><b data-item-qty>${empty itemQty?0:itemQty}</b><button type="button" data-qty-step="1" aria-label="Add one">&#43;</button></div>
    </div></div></article>
  </c:forEach><c:if test="${empty items}"><p class="empty">No dishes match your search.</p></c:if></div>
</div>
</div>
<%@ include file="_f.jspf" %>


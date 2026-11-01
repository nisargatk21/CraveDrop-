<%@ include file="_h.jspf" %>
<div id="main" role="main">

<c:if test="${not listingOnly}">
  <section class="hero">
    <div class="hero-copy">
      <p class="eyebrow">CRAVEDROP <span>&middot;</span> BENGALURU</p>
      <h1>Welcome to <span>CraveDrop</span></h1>
      <h2>Good Food. Good Mood.</h2>
      <p>Discover the best restaurants around you and get your favourites delivered fresh.</p>
      <form class="search" action="${ctx}/app/home" method="get" role="search">
        <label class="sr" for="q">Search for restaurants, cuisines or dishes</label>
        <span class="search-icon" aria-hidden="true">&#8981;</span>
        <input id="q" name="q" placeholder="Search for restaurants, cuisines or dishes..." value="${fn:escapeXml(param.q)}">
        <button type="submit" aria-label="Search">&#8981;</button>
      </form>
    </div>
  </section>

  <section class="home-menu wrap" aria-labelledby="home-menu-title">
    <div class="section-heading">
      <div>
        <p class="eyebrow">A little inspiration</p>
        <h2 id="home-menu-title">Popular picks, fresh from the menu</h2>
        <p>Six signature dishes from six different kitchens around Bengaluru.</p>
      </div>
      <a class="view-all" href="${ctx}/app/restaurants">Explore all restaurants <span aria-hidden="true">&#8594;</span></a>
    </div>

    <nav class="category-chips" aria-label="Browse food categories">
      <c:forEach var="category" items="${categories}">
        <c:url var="categoryUrl" value="/app/home">
          <c:param name="q" value="${category}" />
        </c:url>
        <a href="${categoryUrl}"><c:out value="${category}" /> <span aria-hidden="true">&#8599;</span></a>
      </c:forEach>
    </nav>

    <div class="dish-picks">
      <c:forEach var="item" items="${featured}">
        <c:url var="dishRestaurant" value="/app/menu">
          <c:param name="restaurant" value="${item.restaurant}" />
        </c:url>
        <article class="dish-pick">
          <a class="dish-pick-image" href="${dishRestaurant}">
            <img src="${ctx}/${item.image}" alt="${fn:escapeXml(item.name)}">
          </a>
          <div class="dish-pick-copy">
            <span class="dish-category"><c:out value="${item.category}" /></span>
            <h3><c:out value="${item.name}" /></h3>
            <p><c:out value="${item.restaurant}" /></p>
            <div class="dish-pick-bottom">
              <strong>&#8377;<fmt:formatNumber value="${item.price}" maxFractionDigits="0" /></strong>
              <a href="${dishRestaurant}">View menu <span aria-hidden="true">&#8594;</span></a>
            </div>
          </div>
        </article>
      </c:forEach>
    </div>
  </section>
</c:if>

<div class="wrap restaurant-wrap" id="restaurants">
  <nav class="filters" aria-label="Restaurant filters">
    <button class="filter active" data-filter="all">All</button>
    <button class="filter" data-filter="veg">Veg</button>
    <button class="filter" data-filter="nonveg">Non Veg</button>
    <button class="filter" data-filter="fast">Fast Delivery</button>
    <button class="filter" data-filter="rating">Rating 4+</button>
    <button class="filter" data-filter="time">Under 30 Min</button>
  </nav>

  <div class="restaurant-grid" id="restaurant-grid">
    <c:forEach var="r" items="${restaurants}">
      <c:set var="restaurantName" value="${r.name}" />
      <c:set var="restaurantTerms" value="${menuSearch[restaurantName]}" />
      <c:url var="restaurantMenu" value="/app/menu">
        <c:param name="restaurant" value="${restaurantName}" />
      </c:url>
      <article class="restaurant-card"
        data-name="${fn:escapeXml(fn:toLowerCase(restaurantName))} ${fn:escapeXml(fn:toLowerCase(r.cuisine))} ${fn:escapeXml(fn:toLowerCase(r.area))} ${fn:escapeXml(fn:toLowerCase(restaurantTerms))}"
        data-veg="${r.veg}"
        data-rating="${r.rating}"
        data-minutes="${r.minutes}">
        <a class="restaurant-photo"
          aria-label="View ${fn:escapeXml(restaurantName)} menu"
          href="${restaurantMenu}"
          style="background-image:url('${ctx}/${r.image}')"></a>
        <div class="restaurant-info">
          <div class="restaurant-title">
            <h3><c:out value="${restaurantName}" /></h3>
            <span class="rating">&#9733; <fmt:formatNumber value="${r.rating}" pattern="0.0" /></span>
          </div>
          <p><c:out value="${r.cuisine}" /></p>
          <div class="restaurant-location"><span class="pin">&#9679;</span> <c:out value="${r.area}" />, Bengaluru</div>
          <div class="restaurant-footer">
            <span class="delivery-time"><span aria-hidden="true">&#9201;</span> ${r.minutes} mins</span>
            <a href="${restaurantMenu}">View Menu <span aria-hidden="true">&#8594;</span></a>
          </div>
        </div>
      </article>
    </c:forEach>
  </div>
  <p class="empty" id="no-results" hidden="hidden">No restaurants match those filters. Try another search.</p>
</div>

<script>
(() => {
  const cards = [...document.querySelectorAll('.restaurant-card')];
  const filters = [...document.querySelectorAll('.filter')];
  const search = document.querySelector('#q');
  let mode = 'all';

  function update() {
    const query = (search?.value || '').trim().toLowerCase();
    let visibleCount = 0;
    cards.forEach(card => {
      const matchesName = card.dataset.name.includes(query);
      const matchesFilter = mode === 'all'
        || (mode === 'veg' && card.dataset.veg === 'true')
        || (mode === 'nonveg' && card.dataset.veg === 'false')
        || (mode === 'fast' && Number(card.dataset.minutes) <= 25)
        || (mode === 'rating' && Number(card.dataset.rating) >= 4)
        || (mode === 'time' && Number(card.dataset.minutes) < 30);
      card.hidden = !(matchesName && matchesFilter);
      if (!card.hidden) visibleCount++;
    });
    document.querySelector('#no-results').hidden = visibleCount !== 0;
  }

  filters.forEach(button => button.addEventListener('click', () => {
    filters.forEach(filter => filter.classList.remove('active'));
    button.classList.add('active');
    mode = button.dataset.filter;
    update();
  }));
  search?.addEventListener('input', update);
  update();
})();
</script>
<script>
(() => {
  const cards = [...document.querySelectorAll('.restaurant-card')];
  document.documentElement.classList.add('reveal-enabled');
  if (!('IntersectionObserver' in window)) {
    cards.forEach(card => card.classList.add('is-visible'));
    return;
  }
  const observer = new IntersectionObserver(entries => entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add('is-visible');
      observer.unobserve(entry.target);
    }
  }), { threshold: 0.08, rootMargin: '0px 0px 30px 0px' });
  cards.forEach((card, index) => {
    card.style.setProperty('--reveal-delay', `${(index % 3) * 65}ms`);
    observer.observe(card);
  });
})();
</script>

</div>
<%@ include file="_f.jspf" %>


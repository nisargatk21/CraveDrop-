<%@ include file="_h.jspf" %>
<main id="main">
<section class="card" aria-labelledby="t"><div class="auth-brand"><span class="brand-mark">&#10022;</span><span>Crave<span>Drop</span></span></div><p class="auth-tagline">Good food. Good mood.</p><h1 id="t"><c:out value="${title}"/></h1>
<c:if test="${param.registered=='1'}"><p class="msg ok" role="status">Account created. Please sign in.</p></c:if>
<c:if test="${param.reset=='1'}"><p class="msg ok" role="status">Password updated. Please sign in.</p></c:if>
<c:if test="${not empty ok}"><p class="msg ok" role="status"><c:out value="${ok}"/></p></c:if>
<c:if test="${not empty error}"><p class="msg err" role="alert"><c:out value="${error}"/></p></c:if>
<form method="post" data-busy>
<c:if test="${mode=='signup'}"><label for="name">Full name</label><input id="name" name="name" required autocomplete="name" value="${fn:escapeXml(param.name)}">
<label for="phone">Phone (10 digits)</label><input id="phone" name="phone" required pattern="\d{10}" inputmode="numeric" autocomplete="tel" value="${fn:escapeXml(param.phone)}"></c:if>
<c:if test="${mode=='signup'||mode=='login'||mode=='forgot'}"><label for="email">Email</label><input id="email" type="email" name="email" required autocomplete="email" value="${fn:escapeXml(param.email)}"></c:if>
<c:if test="${mode=='signup'}"><label for="role">Account type</label><select id="role" name="role"><option value="customer">Customer</option><option value="owner" ${param.role=='owner'?'selected':''}>Restaurant owner</option></select></c:if>
<c:if test="${mode=='password'}"><label for="current">Current password</label><input id="current" type="password" name="current" required autocomplete="current-password"></c:if>
<c:if test="${mode!='forgot'}"><label for="pw">${mode=='reset'||mode=='password'?'New password':'Password'}</label><input id="pw" type="password" name="password" required autocomplete="${mode=='login'?'current-password':'new-password'}"></c:if>
<c:if test="${mode=='reset'||mode=='password'}"><label for="confirm">Confirm new password</label><input id="confirm" type="password" name="confirm" required autocomplete="new-password"></c:if>
<c:if test="${mode=='reset'}"><input type="hidden" name="token" value="${fn:escapeXml(param.token)}"></c:if>
<c:if test="${mode=='login'}"><input type="hidden" name="next" value="${fn:escapeXml(param.next)}"></c:if>
<button class="btn wide" type="submit"><c:out value="${btn}"/></button></form>
<p class="alt"><c:choose>
<c:when test="${mode=='login'}"><a href="${ctx}/app/forgot">Forgot password?</a> &middot; New here? <a href="${ctx}/app/signup">Create an account</a></c:when>
<c:when test="${mode=='signup'}">Already have an account? <a href="${ctx}/app/login">Sign in</a></c:when>
<c:otherwise><a href="${ctx}/app/login">Back to sign in</a></c:otherwise></c:choose></p></section>
</main>
<%@ include file="_f.jspf" %>



package com.cravedrop.web;
import com.cravedrop.dao.AdminDashboardDAO; import com.cravedrop.dao.MenuDAO; import com.cravedrop.dao.RestaurantDAO; import com.cravedrop.dao.WishlistDAO; import com.cravedrop.model.Models.*; import com.cravedrop.service.*;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import java.sql.SQLException; import java.util.*;
public class FrontController extends HttpServlet {
 private static final long serialVersionUID = 1L;
 final AuthService auth=new AuthService(); final MenuDAO menu=new MenuDAO(); final RestaurantDAO restaurants=new RestaurantDAO(); final OrderService orders=new OrderService(); final AdminDashboardDAO adminDashboard=new AdminDashboardDAO(); final WishlistDAO wishlist=new WishlistDAO();
 void view(HttpServletRequest rq,HttpServletResponse rs,String v) throws ServletException,IOException {rs.setContentType("text/html;charset=UTF-8");rs.setCharacterEncoding("UTF-8");rq.getRequestDispatcher("/WEB-INF/views/"+v+".jsp").forward(rq,rs);}
 void authView(HttpServletRequest rq,HttpServletResponse rs,String mode,String title,String btn) throws ServletException,IOException {
  rq.setAttribute("mode",mode);rq.setAttribute("title",title);rq.setAttribute("btn",btn);rq.setAttribute("bodyClass","auth auth-"+mode);view(rq,rs,"auth");}
 void cartView(HttpServletRequest rq,HttpServletResponse rs,Map<Integer,Integer> cart) throws SQLException,ServletException,IOException {
  var l=orders.lines(cart);var validIds=new HashSet<Integer>();var restaurantNames=new HashSet<String>();
  for(var line:l){validIds.add(line.item().id());restaurantNames.add(line.item().restaurant());}
  cart.keySet().retainAll(validIds);rq.setAttribute("cartCount",cart.values().stream().mapToInt(i->i).sum());
  rq.setAttribute("lines",l);rq.setAttribute("total",orders.subtotal(l));rq.setAttribute("payments",OrderService.PAYMENTS);rq.setAttribute("cartSubtotal",orders.subtotal(l));
  Object notice=rq.getSession().getAttribute("cartNotice");
  if(notice==null&&restaurantNames.size()>1)notice="Your saved cart contains items from multiple restaurants. Remove items until only one restaurant remains before checkout.";
  rq.setAttribute("cartNotice",notice);rq.getSession().removeAttribute("cartNotice");view(rq,rs,"cart");}
 @SuppressWarnings("unchecked") static Map<Integer,Integer> cartOf(HttpSession s){var c=(Map<Integer,Integer>)s.getAttribute("cart");if(c==null){c=new LinkedHashMap<>();s.setAttribute("cart",c);}return c;}
 private static boolean wantsJson(HttpServletRequest rq){return "XMLHttpRequest".equalsIgnoreCase(rq.getHeader("X-Requested-With"))||rq.getHeader("Accept")!=null&&rq.getHeader("Accept").contains("application/json");}
 private void cartJson(HttpServletRequest rq,HttpServletResponse rs,Map<Integer,Integer> cart,boolean ok,String message,int status) throws SQLException,IOException {
  var lines=orders.lines(cart);int count=cart.values().stream().mapToInt(Integer::intValue).sum();double subtotal=orders.subtotal(lines);
  rs.setStatus(status);rs.setContentType("application/json;charset=UTF-8");String safe=message==null?"":message.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ").replace("\r"," ");
  rs.getWriter().write("{\"ok\":"+ok+",\"count\":"+count+",\"subtotal\":"+String.format(java.util.Locale.US,"%.2f",subtotal)+",\"message\":\""+safe+"\"}");
 }
 private void returnToMenu(HttpServletRequest rq,HttpServletResponse rs,String ctx) throws IOException {
  String ref=rq.getHeader("Referer");String prefix=rq.getScheme()+"://"+rq.getServerName()+":"+rq.getServerPort()+ctx+"/app/";
  rs.sendRedirect(ref!=null&&ref.startsWith(prefix)?ref:ctx+"/app/cart");
 }
 @Override protected void service(HttpServletRequest rq,HttpServletResponse rs) throws ServletException,IOException {
  rq.setCharacterEncoding("UTF-8"); String p=rq.getPathInfo()==null?"/home":rq.getPathInfo(); boolean post="POST".equals(rq.getMethod());
  var s=rq.getSession(); var u=(User)s.getAttribute("user");
  if(u!=null&&(p.equals("/admin")||p.startsWith("/admin/"))){try{u=auth.findById(u.id());if(u==null)s.removeAttribute("user");else s.setAttribute("user",u);}catch(SQLException e){e.printStackTrace();rs.sendError(500,"Could not verify your account role. Check the database connection.");return;}}
  var cart=cartOf(s); String ctx=rq.getContextPath(); String next=rq.getParameter("next");
  rq.setAttribute("cartCount",cart.values().stream().mapToInt(i->i).sum());
  try{rq.setAttribute("cartSubtotal",orders.subtotal(orders.lines(cart))); switch(p){
   case "/home" -> {rq.setAttribute("restaurants",restaurants.list());rq.setAttribute("menuSearch",menu.searchIndex());rq.setAttribute("categories",menu.categories());rq.setAttribute("featured",menu.featured(6));view(rq,rs,"home");}
   case "/restaurants" -> {rq.setAttribute("listingOnly",true);rq.setAttribute("restaurants",restaurants.list());rq.setAttribute("menuSearch",menu.searchIndex());view(rq,rs,"home");}
   case "/menu" -> {String name=rq.getParameter("restaurant");var restaurant=restaurants.find(name==null?"":name);
     if(restaurant==null){rs.sendError(404,"Restaurant not found");return;}
     rq.setAttribute("restaurant",restaurant);rq.setAttribute("items",menu.listForRestaurant(restaurant.name(),rq.getParameter("q"),rq.getParameter("veg")));
     rq.setAttribute("cartQuantities",new HashMap<>(cart));var savedIds=u==null?Set.<Integer>of():new HashSet<>(wishlist.itemIds(u.id()));var savedFlags=new HashMap<Integer,Boolean>();savedIds.forEach(id->savedFlags.put(id,Boolean.TRUE));rq.setAttribute("wishlistIds",savedIds);rq.setAttribute("wishlistFlags",savedFlags);view(rq,rs,"menu");}
   case "/signup" -> {if(post){String e=auth.register(rq.getParameter("name"),rq.getParameter("email"),rq.getParameter("phone"),rq.getParameter("password"),rq.getParameter("role"));
     if(e==null){rs.sendRedirect(ctx+"/app/login?registered=1");return;}rq.setAttribute("error",e);}authView(rq,rs,"signup","Create your account","Sign up");}
   case "/login" -> {if(post){var x=auth.login(rq.getParameter("email"),rq.getParameter("password"));
     if(x!=null){rq.changeSessionId();rq.getSession().setAttribute("user",x);rs.sendRedirect(next!=null&&next.startsWith("/app/")&&!next.startsWith("//")?ctx+next:ctx+"/app/home");return;}
     rq.setAttribute("error","Incorrect email or password.");}authView(rq,rs,"login","Welcome back","Sign in");}
   case "/logout" -> {s.invalidate();rs.sendRedirect(ctx+"/app/login");}
   case "/forgot" -> {if(post){auth.startReset(rq.getParameter("email"),rq.getScheme()+"://"+rq.getServerName()+":"+rq.getServerPort()+ctx);
     rq.setAttribute("ok","If that email has an account, a reset link is on its way (valid for 1 hour).");}authView(rq,rs,"forgot","Forgot your password?","Send reset link");}
   case "/reset" -> {if(post){String e=auth.reset(rq.getParameter("token"),rq.getParameter("password"),rq.getParameter("confirm"));
     if(e==null){rs.sendRedirect(ctx+"/app/login?reset=1");return;}rq.setAttribute("error",e);}authView(rq,rs,"reset","Choose a new password","Update password");}
   case "/password" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/password");return;}
     if(post){String e=auth.change(u,rq.getParameter("current"),rq.getParameter("password"),rq.getParameter("confirm"));
      if(e==null){rq.getSession().setAttribute("user",auth.findById(u.id()));rq.setAttribute("ok","Password changed successfully.");}else rq.setAttribute("error",e);}authView(rq,rs,"password","Change password","Change password");}
   case "/cart/add" -> {int id;try{id=Integer.parseInt(rq.getParameter("id"));}catch(NumberFormatException e){if(wantsJson(rq)){cartJson(rq,rs,cart,false,"Invalid menu item.",400);return;}rs.sendError(400,"Invalid menu item");return;}
     var selected=menu.findByIds(List.of(id)).get(id);if(selected==null){if(wantsJson(rq)){cartJson(rq,rs,cart,false,"Menu item not found.",404);return;}rs.sendError(404,"Menu item not found");return;}
     var existing=menu.findByIds(cart.keySet());var restaurantsInCart=new HashSet<String>();existing.values().forEach(item->restaurantsInCart.add(item.restaurant()));
     if(!restaurantsInCart.isEmpty()&&(!restaurantsInCart.contains(selected.restaurant())||restaurantsInCart.size()>1)){String message="Your cart already contains food from "+String.join(", ",restaurantsInCart)+". Finish that order before adding from another restaurant.";s.setAttribute("cartNotice",message);if(wantsJson(rq)){cartJson(rq,rs,cart,false,message,409);return;}rs.sendRedirect(ctx+"/app/cart");return;}
     if(existing.isEmpty())cart.clear();cart.merge(id,1,(a,b)->Math.min(20,a+b));if(wantsJson(rq)){cartJson(rq,rs,cart,true,"Added to cart.",200);return;}returnToMenu(rq,rs,ctx);}
   case "/cart/set" -> {int id,q;try{id=Integer.parseInt(rq.getParameter("id"));q=Integer.parseInt(rq.getParameter("qty"));}catch(NumberFormatException e){if(wantsJson(rq)){cartJson(rq,rs,cart,false,"Invalid quantity.",400);return;}rs.sendError(400,"Invalid quantity");return;}
     if(!cart.containsKey(id)&&q>0){if(wantsJson(rq)){cartJson(rq,rs,cart,false,"Item is not in your cart.",404);return;}rs.sendError(404,"Item is not in your cart");return;}
     if(q<=0)cart.remove(id);else cart.put(id,Math.min(q,20));if(wantsJson(rq)){cartJson(rq,rs,cart,true,"Cart updated.",200);return;}rs.sendRedirect(ctx+"/app/cart");}
   case "/wishlist/toggle" -> {if(u==null){if(wantsJson(rq)){rs.setStatus(401);rs.setContentType("application/json;charset=UTF-8");rs.getWriter().write("{\"ok\":false,\"message\":\"Sign in to save dishes.\"}");return;}rs.sendRedirect(ctx+"/app/login?next=/app/wishlist");return;}
     int id;try{id=Integer.parseInt(rq.getParameter("id"));}catch(NumberFormatException e){rs.sendError(400,"Invalid menu item");return;}if(!menu.findByIds(List.of(id)).containsKey(id)){rs.sendError(404,"Menu item not found");return;}boolean saved=wishlist.toggle(u.id(),id);
     if(wantsJson(rq)){rs.setContentType("application/json;charset=UTF-8");rs.getWriter().write("{\"ok\":true,\"saved\":"+saved+",\"message\":\""+(saved?"Saved to your favourites.":"Removed from your favourites.")+"\"}");return;}returnToMenu(rq,rs,ctx);}
   case "/wishlist" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/wishlist");return;}var ids=wishlist.itemIds(u.id());rq.setAttribute("savedItems",new ArrayList<>(menu.findByIds(ids).values()));rq.setAttribute("wishlistIds",new HashSet<>(ids));view(rq,rs,"wishlist");}
   case "/cart" -> cartView(rq,rs,cart);
   case "/checkout" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/checkout");return;}
     var lines=orders.lines(cart);if(lines.isEmpty()){rs.sendRedirect(ctx+"/app/cart");return;}
     var price=orders.breakdown(lines);rq.setAttribute("lines",lines);rq.setAttribute("subtotal",price.subtotal());rq.setAttribute("gst",price.gst());
     rq.setAttribute("deliveryFee",price.deliveryFee());rq.setAttribute("platformFee",price.platformFee());rq.setAttribute("total",price.grandTotal());rq.setAttribute("payments",OrderService.PAYMENTS);
     if(post){try{int id=orders.place(u,cart,rq.getParameter("address"),rq.getParameter("phone"),rq.getParameter("payment"));cart.clear();rs.sendRedirect(ctx+"/app/confirmed?id="+id);return;}
       catch(IllegalArgumentException e){rq.setAttribute("error",e.getMessage());}}
     view(rq,rs,"checkout");}
   case "/confirmed" -> {if(u==null){rs.sendRedirect(ctx+"/app/login");return;}try{int id=Integer.parseInt(rq.getParameter("id"));var order=orders.findForUser(u.id(),id);if(order==null){rs.sendError(404,"Order not found");return;}rq.setAttribute("order",order);view(rq,rs,"confirmed");}catch(NumberFormatException e){rs.sendError(404,"Order not found");}}
   case "/orders" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/orders");return;}rq.setAttribute("orders",orders.history(u.id()));view(rq,rs,"orders");}
   case "/dashboard" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/dashboard");return;}
     var history=orders.history(u.id());double spent=history.stream().mapToDouble(Order::total).sum();long delivered=history.stream().filter(o->"Delivered".equals(o.status())).count();
     rq.setAttribute("customerOrders",history);rq.setAttribute("orderCount",history.size());rq.setAttribute("deliveredCount",delivered);rq.setAttribute("spent",spent);
     rq.setAttribute("recentOrders",history.subList(Math.max(0,history.size()-3),history.size()));view(rq,rs,"dashboard");}
   case "/admin" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/admin");return;}if(!"admin".equals(u.role())){rs.sendError(403,"Administrator access is required. Signed-in account role: "+u.role()+".");return;}
     adminDashboard.ensureDeliverySchema();var partners=adminDashboard.deliveryPartners();rq.setAttribute("stats",adminDashboard.stats());rq.setAttribute("adminRestaurants",restaurants.list());rq.setAttribute("adminMenuItems",menu.list(null,null,null));rq.setAttribute("recentAdminOrders",adminDashboard.recentOrders());rq.setAttribute("recentAdminUsers",adminDashboard.recentUsers());rq.setAttribute("deliveryPartners",partners);rq.setAttribute("availableDeliveryPartners",partners.stream().filter(partner->"Available".equals(partner.status())).toList());view(rq,rs,"admin");}
   case "/admin/order-status" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/admin");return;}if(!"admin".equals(u.role())){rs.sendError(403,"Administrator access is required. Signed-in account role: "+u.role()+".");return;}if(!post){rs.sendError(405,"POST is required.");return;}adminDashboard.ensureDeliverySchema();
     try{int orderId=Integer.parseInt(rq.getParameter("orderId"));String status=rq.getParameter("status");if(!adminDashboard.updateOrderStatus(orderId,status)){rs.sendError(404,"Order not found.");return;}rs.sendRedirect(ctx+"/app/admin?updated=1#orders");}
     catch(IllegalArgumentException e){rs.sendError(400,"Choose a valid order and status.");}}
   case "/admin/partner-add" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/admin");return;}if(!"admin".equals(u.role())){rs.sendError(403,"Administrator access is required. Signed-in account role: "+u.role()+".");return;}if(!post){rs.sendError(405,"POST is required.");return;}
     adminDashboard.ensureDeliverySchema();try{adminDashboard.addDeliveryPartner(rq.getParameter("name"),rq.getParameter("phone"),rq.getParameter("area"));rs.sendRedirect(ctx+"/app/admin?partnerAdded=1#partners");}catch(IllegalArgumentException e){rs.sendRedirect(ctx+"/app/admin?partnerError=1#partners");}catch(SQLException e){if(e.getSQLState()!=null&&e.getSQLState().startsWith("23")){rs.sendRedirect(ctx+"/app/admin?partnerDuplicate=1#partners");return;}throw e;}}
   case "/admin/partner-assign" -> {if(u==null){rs.sendRedirect(ctx+"/app/login?next=/app/admin");return;}if(!"admin".equals(u.role())){rs.sendError(403,"Administrator access is required. Signed-in account role: "+u.role()+".");return;}if(!post){rs.sendError(405,"POST is required.");return;}
     adminDashboard.ensureDeliverySchema();try{adminDashboard.assignPartner(Integer.parseInt(rq.getParameter("orderId")),Integer.parseInt(rq.getParameter("partnerId")));rs.sendRedirect(ctx+"/app/admin?assigned=1#orders");}catch(IllegalArgumentException e){rs.sendRedirect(ctx+"/app/admin?assignError=1#orders");}}
   default -> rs.sendError(404);
  }}catch(SQLException e){e.printStackTrace();rs.sendError(500,"Database error. Check DB_URL / DB_USER / DB_PASS and that db/schema.sql was loaded.");}
 }
}
